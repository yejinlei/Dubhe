"""
Copyright 2020 Tianshu AI Platform. All Rights Reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""

import os
import math
import numpy as np
import sys

curPath = os.path.abspath(os.path.dirname(__file__))
rootPath = os.path.split(curPath)[0]
sys.path.append(os.path.abspath(os.path.join(os.getcwd(), "./src")))

import oneflow as flow

from util import Snapshot, Summary, InitNodes, Metric, CreateOptimizer, GetFunctionConfig, getdirsize, remove_optimizer_params, remove_teacher_params

import config as configs
from sklearn.metrics import accuracy_score, matthews_corrcoef, precision_score, recall_score, f1_score
import argparse
import shutil
import tempfile
from knowledge_distill_util import LSTMStudentForSequenceClassification, BertForSequenceClassification, BertStudentForSequenceClassification, soft_cross_entropy, mseloss, layer_distill, att_distill, pred_distill
import time
import json

def str2bool(v):
    if v.lower() in ('yes', 'true', 't', 'y', '1'):
        return True
    elif v.lower() in ('no', 'false', 'f', 'n', '0'):
        return False
    else:
        raise argparse.ArgumentTypeError('Unsupported value encountered.')

parser = configs.get_parser()
parser.add_argument("--task_name", type=str, default='CoLA')
parser.add_argument("--teacher_model", default=None, type=str, help="The teacher model dir.")
parser.add_argument("--student_model", default=None, type=str, help="The student model dir.")
parser.add_argument("--total_model", default=None, type=str, help="The student model dir.")

parser.add_argument('--num_epochs', type=int, default=3, help='number of epochs')
parser.add_argument("--train_data_dir", type=str, default=None)
parser.add_argument("--train_data_dir_lstm", type=str, default=None)
parser.add_argument("--train_data_prefix", type=str, default='train.of_record-')
parser.add_argument("--train_example_num", type=int, default=88614,
                    help="example number in dataset")
parser.add_argument("--batch_size_per_device", type=int, default=32)
parser.add_argument("--train_data_part_num", type=int, default=1,
                    help="data part number in dataset")
parser.add_argument("--eval_data_dir", type=str, default=None)
parser.add_argument("--eval_data_dir_lstm", type=str, default=None)
parser.add_argument("--eval_data_prefix", type=str, default='eval.of_record-')
parser.add_argument("--eval_example_num", type=int, default=10833,
                    help="example number in dataset")
parser.add_argument("--eval_batch_size_per_device", type=int, default=64)
parser.add_argument("--eval_data_part_num", type=int, default=1,
                    help="data part number in dataset")
parser.add_argument("--result_dir", type=str, default="", help="the save directory of results")

#
parser.add_argument("--student_num_hidden_layers", type=int, default=24)
parser.add_argument("--student_num_attention_heads", type=int, default=16)
parser.add_argument("--student_max_position_embeddings", type=int, default=512)
parser.add_argument("--student_type_vocab_size", type=int, default=2)
parser.add_argument("--student_vocab_size", type=int, default=30522)
parser.add_argument("--student_attention_probs_dropout_prob", type=float, default=0.1)
parser.add_argument("--student_hidden_dropout_prob", type=float, default=0.1)
parser.add_argument("--student_hidden_size_per_head", type=int, default=64)
parser.add_argument("--student_hidden_size", type=int, default=768)
parser.add_argument("--student_seq_length", type=int, default=32, help="the max seq length for studet")

parser.add_argument("--teacher_num_hidden_layers", type=int, default=24)
parser.add_argument("--teacher_num_attention_heads", type=int, default=16)
parser.add_argument("--teacher_max_position_embeddings", type=int, default=512)
parser.add_argument("--teacher_type_vocab_size", type=int, default=2)
parser.add_argument("--teacher_vocab_size", type=int, default=30522)
parser.add_argument("--teacher_attention_probs_dropout_prob", type=float, default=0.1)
parser.add_argument("--teacher_hidden_dropout_prob", type=float, default=0.1)
parser.add_argument("--teacher_hidden_size_per_head", type=int, default=64)
parser.add_argument("--teacher_hidden_size", type=int, default=768)

parser.add_argument("--kd_alpha", type=float, default=0.1)

parser.add_argument('--temperature', type=float, default=1.)
parser.add_argument('--aug_train',  type=str2bool, nargs='?', const=False, help='using augmented training set?')

parser.add_argument('--serve_for_online',  type=str2bool, nargs='?', const=False, help='if serve for online, then after training, will delete the teacher params and optimizer parmas from model_save_dir')

args = parser.parse_args()

task_name = args.task_name.lower()

if args.aug_train:
    args.train_data_dir = args.train_data_dir.replace('train','train_aug')

batch_size = args.num_nodes * args.gpu_num_per_node * args.batch_size_per_device
eval_batch_size = args.num_nodes * args.gpu_num_per_node * args.eval_batch_size_per_device

epoch_size = math.ceil(args.train_example_num / batch_size)
num_eval_steps = math.ceil(args.eval_example_num / eval_batch_size)
args.iter_num = epoch_size * args.num_epochs
configs.print_args(args)

glue_output_modes = {
    "cola": "classification",
    "mnli": "classification",
    "mnli-mm": "classification",
    "mrpc": "classification",
    "sst-2": "classification",
    "sts-b": "regression",
    "qqp": "classification",
    "qnli": "classification",
    "rte": "classification",
    "wnli": "classification",
}

acc_tasks = ["mnli", "mrpc", "sst-2", "qqp", "qnli", "rte"]
corr_tasks = ["sts-b"]
mcc_tasks = ["cola"]

output_mode = glue_output_modes[args.task_name.lower()]

def BertDecoder(
    data_dir, batch_size, data_part_num, seq_length, part_name_prefix, shuffle=True
):
    with flow.scope.placement("cpu", "0:0"):
        ofrecord = flow.data.ofrecord_reader(data_dir,
                                             batch_size=batch_size,
                                             data_part_num=data_part_num,
                                             part_name_prefix=part_name_prefix,
                                             random_shuffle=shuffle,
                                             shuffle_after_epoch=shuffle)
        blob_confs = {}
        def _blob_conf(name, shape, dtype=flow.int32):
            blob_confs[name] = flow.data.OFRecordRawDecoder(ofrecord, name, shape=shape, dtype=dtype)

        _blob_conf("input_ids", [seq_length])
        _blob_conf("input_mask", [seq_length])
        _blob_conf("segment_ids", [seq_length])
        _blob_conf("label_ids", [1])
        _blob_conf("is_real_example", [1])

        return blob_confs

def get_tensor_data(
    batch_size,
    data_part_num,
    data_dir,
    part_name_prefix,
    seq_length,
    shuffle=True
):
    decoders = BertDecoder(
        data_dir, batch_size, data_part_num, seq_length, part_name_prefix, shuffle=shuffle
    )
    return decoders

def student_model(input_ids, input_mask, segment_ids,is_train=True):
    hidden_size = args.student_hidden_size ##64 * args.num_attention_heads  # , H = 64, size per head
    # args.student_hidden_size_per_head = hidden_size / args.student_num_attention_heads
    # print('input_ids:',input_ids.shape)

    logits = LSTMStudentForSequenceClassification(
        input_ids_blob=input_ids,
        input_mask_blob=input_mask,
        token_type_ids_blob=segment_ids,
        label_blob=None,
        vocab_size=args.student_vocab_size,
        seq_length=args.student_seq_length,
        hidden_size=hidden_size,
        intermediate_size=400,
        num_hidden_layers=args.student_num_hidden_layers,
        is_student=True,
        is_train=is_train
    )
    return logits


def teacher_model(input_ids,input_mask,segment_ids,is_train):
    teacher_hidden_size = args.teacher_hidden_size ##64 * args.num_attention_heads  # , H = 64, size per head
    args.teacher_hidden_size_per_head = teacher_hidden_size / args.teacher_num_attention_heads
    intermediate_size = teacher_hidden_size * 4
    logits, reps, atts = BertForSequenceClassification(
        input_ids_blob=input_ids,
        input_mask_blob=input_mask,
        token_type_ids_blob=segment_ids,
        label_blob=None,
        vocab_size=args.vocab_size,
        seq_length=args.seq_length,
        hidden_size=teacher_hidden_size,
        num_hidden_layers=args.teacher_num_hidden_layers,
        num_attention_heads=args.teacher_num_attention_heads,
        intermediate_size=intermediate_size,
        hidden_act="gelu",
        hidden_dropout_prob=args.teacher_hidden_dropout_prob,
        attention_probs_dropout_prob=args.teacher_attention_probs_dropout_prob,
        max_position_embeddings=args.teacher_max_position_embeddings,
        type_vocab_size=args.teacher_type_vocab_size,
        initializer_range=0.02,
        is_student=False,
        is_train=is_train
    )
    return logits, reps, atts

@flow.global_function(type='train', function_config=GetFunctionConfig(args))
def DistilJob():
    train_dataset = get_tensor_data(
        batch_size,
        args.train_data_part_num,
        args.train_data_dir,
        args.train_data_prefix,
        args.seq_length,
        False
    )
    train_dataset_lstm = get_tensor_data(
        batch_size,
        args.train_data_part_num,
        args.train_data_dir_lstm,
        args.train_data_prefix,
        args.student_seq_length,
        False
    )
    student_logits = student_model(train_dataset_lstm['input_ids'], train_dataset_lstm['input_mask'], train_dataset_lstm['segment_ids'],is_train=True)
    teacher_logits, teacher_reps, teacher_atts = teacher_model(train_dataset['input_ids'], train_dataset['input_mask'], train_dataset['segment_ids'],is_train=False)
    if output_mode == "classification":
        cls_loss = pred_distill(args, student_logits, teacher_logits)
    elif output_mode == "regression":
        """
        todo
        loss_mse = MSELoss()
        cls_loss = loss_mse(student_logits.view(-1), label_ids.view(-1))
        """
        pass

    loss_ce = flow.nn.sparse_softmax_cross_entropy_with_logits(
        logits=student_logits, labels=train_dataset_lstm['label_ids']
    )

    loss = loss_ce * (1-args.kd_alpha) + cls_loss * args.kd_alpha

    flow.losses.add_loss(loss)

    opt = CreateOptimizer(args)
    opt.minimize(loss)

    return {'loss': loss}

#
@flow.global_function(type='predict', function_config=GetFunctionConfig(args))
def StudentBertGlueEvalTrainJob():
    train_dataset = get_tensor_data(
        batch_size,
        args.train_data_part_num,
        args.train_data_dir_lstm,
        args.train_data_prefix,
        args.student_seq_length,
        shuffle=False
    )
    student_logits = student_model(train_dataset['input_ids'], train_dataset['input_mask'], train_dataset['segment_ids'],is_train=False)

    return student_logits, train_dataset['label_ids']

@flow.global_function(type='predict', function_config=GetFunctionConfig(args))
def StudentBertGlueEvalValJob():
    dev_dataset = get_tensor_data(
        eval_batch_size,
        args.eval_data_part_num,
        args.eval_data_dir_lstm,
        args.eval_data_prefix,
        args.student_seq_length,
        shuffle=False
    )
    student_logits= student_model(dev_dataset['input_ids'], dev_dataset['input_mask'], dev_dataset['segment_ids'],is_train=False)

    return student_logits, dev_dataset['label_ids']

#
def run_eval_job(eval_job_func, num_steps, desc='train'):
    labels = []
    predictions = []
    start_time = time.time()
    for index in range(num_steps):
        logits, label = eval_job_func().get()
        predictions.extend(list(logits.numpy().argmax(axis=1)))
        labels.extend(list(label))
    end_time = time.time()
    cost_time = end_time - start_time
    print('cost time: {} s'.format(cost_time))

    model_size = getdirsize(args.model_save_dir)
    print('model_size: %d Mbytes' % (model_size / 1024 / 1024))  # Mbytes

    accuracy = accuracy_score(labels, predictions)
    mcc = matthews_corrcoef(labels, predictions)
    precision = precision_score(labels, predictions)
    recall = recall_score(labels, predictions)
    f_1 = f1_score(labels, predictions)
    save_dict = {"accuracy":"%.2f" % accuracy,
                 "MCC":"%.2f" % mcc,
                 "precision": "%.2f" % precision,
                 "recall": "%.2f" % recall,
                 "f_1": "%.2f" % f_1,
                 "modelSize":"%d" % (model_size/1024/1024),
                 "reasoningTime":"%.2f" % (args.eval_example_num / cost_time)} # sample/second

    if args.result_dir == "":
        args.result_dir = args.model_save_dir
    if not os.path.exists(args.result_dir):
        os.makedirs(args.result_dir)
    with open(os.path.join(args.result_dir, 'results_{}.json'.format(desc)), "w") as f:
        json.dump(save_dict, f)

    def metric_fn(predictions, labels):
        return {
            "accuracy": accuracy,
            "matthews_corrcoef": mcc,
            "precision": precision,
            "recall": recall,
            "f1": f_1,
        }

    metric_dict = metric_fn(predictions, labels)
    print(desc, ', '.join('{}: {:.3f}'.format(k, v) for k, v in metric_dict.items()))
    return metric_dict

def main():
    flow.config.gpu_device_num(args.gpu_num_per_node)
    flow.env.log_dir(args.log_dir)

    InitNodes(args)

    check_point = flow.train.CheckPoint()
    check_point.init()

    summary = Summary(args.log_dir, args)
    if not os.path.exists(args.model_save_dir):
        os.makedirs(args.model_save_dir)
    import shutil
    if args.do_train:
        print('Loading model...')
        check_point.load(args.teacher_model)

        print('Start training...')
        global_step = 0
        best_dev_acc = 0.0
        print('epoch_size:',epoch_size)
        print('args.iter_num:',args.iter_num)
        for epoch in range(args.num_epochs):
            metric = Metric(desc='finetune', print_steps=args.loss_print_every_n_iter, summary=summary,
                            batch_size=batch_size, keys=['loss'])

            for step in range(epoch_size):
                loss = DistilJob().get()
                if step % 10 == 0:
                    print('step/epoch_size:{}/{}   epoch:{}'.format(step,epoch_size,epoch))
                    print('loss:',loss['loss'].mean())
                # DistilJob().async_get(metric.metric_cb(step, epoch=epoch))

                # DistilJob().get(metric.metric_cb(step))

                # global_step += 1
                # if (global_step + 1) % args.model_save_every_n_iter == 0:
                # if (global_step + 1) % 1 == 0:
                #     print('global_step:',global_step)
                #     if not os.path.exists(args.model_save_dir):
                #         os.makedirs(args.model_save_dir)
                #     snapshot_save_path = os.path.join(
                #         args.model_save_dir, "snapshot_%d" % (global_step + 1)
                #     )
                #     print("Saving model to {}.".format(snapshot_save_path))
                #     check_point.save(snapshot_save_path)
    #
            print('EvalTrainJob...')
            run_eval_job(StudentBertGlueEvalTrainJob, epoch_size, desc='train')
            print('EvalValJob...')
            result = run_eval_job(StudentBertGlueEvalValJob, num_eval_steps, desc='eval')

            save_model = False
            if task_name in acc_tasks and result['accuracy'] > best_dev_acc:
                best_dev_acc = result['accuracy']
                save_model = True

            # if task_name in corr_tasks and result['corr'] > best_dev_acc:
            #     best_dev_acc = result['corr']
            #     save_model = True

            if task_name in mcc_tasks and result['matthews_corrcoef'] > best_dev_acc:
                best_dev_acc = result['matthews_corrcoef']
                save_model = True
                print('Best result:', result)

            if save_model:
                if os.path.exists(args.model_save_dir):
                    import shutil
                    shutil.rmtree(args.model_save_dir)
                if not os.path.exists(args.model_save_dir):
                    os.makedirs(args.model_save_dir)
                snapshot_save_path = os.path.join(args.model_save_dir)
                print("Saving best model to {}".format(snapshot_save_path))
                check_point.save(snapshot_save_path)
                flow.sync_default_session()

        if args.save_last_snapshot:
            snapshot_save_path = args.model_save_dir
            if os.path.exists(args.model_save_dir):
                import shutil
                shutil.rmtree(args.model_save_dir)
            print("Saving model to {}".format(snapshot_save_path))
            check_point.save(snapshot_save_path)
            flow.sync_default_session()

        if args.serve_for_online:
            print('Deleting the teacher params and the optimizer parmas from model_save_dir...')
            remove_teacher_params(args.model_save_dir)

    if args.do_eval:
        print('Loading model...')
        print(args.model_save_dir)

        if not args.do_train:
            check_point.load(args.model_save_dir)
        print('Evaluation...')
        run_eval_job(StudentBertGlueEvalValJob, num_eval_steps, desc='eval')
    # if args.save_last_snapshot:
    #     snapshot.save("last_snapshot")



if __name__ == "__main__":
    main()
