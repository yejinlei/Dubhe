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
from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import os
import math

import oneflow as flow

import util.config as configs
from util.util import Snapshot, Summary, InitNodes, Metric, LoadCfg, LoadData
from util.job_function_util import get_train_config, get_val_config
import model.cnn.resnet_model as resnet_model
import model.cnn.vgg_model as vgg_model
import model.cnn.alexnet_model as alexnet_model
import model.cnn.lenet_model as lenet_model
import model.dnn.dnn_model as dnn_model
from util.model_weights import modelWeight


parser = configs.get_parser()
args = parser.parse_args()
configs.print_args(args)


total_device_num = args.num_nodes * args.gpu_num_per_node
train_batch_size = total_device_num * args.batch_size_per_device
val_batch_size = total_device_num * args.val_batch_size_per_device
(C, H, W) = args.image_shape
epoch_size = math.ceil(args.num_examples / train_batch_size)
num_val_steps = int(args.num_val_examples / val_batch_size)


model_dict = {"resnet": resnet_model.resnet50,
              "vgg": vgg_model.vgg,
              "alexnet": alexnet_model.alexnet,
              "alexnet_simple": alexnet_model.alexnet_simple,
              "lenet": lenet_model.lenet,
              "dnn_2": dnn_model.dnn_2,
              "dnn_4": dnn_model.dnn_4,}

flow.config.gpu_device_num(args.gpu_num_per_node)
flow.config.enable_debug_mode(True)

if args.use_boxing_v2:
    flow.config.collective_boxing.nccl_fusion_threshold_mb(8)
    flow.config.collective_boxing.nccl_fusion_all_reduce_use_buffer(False)


def label_smoothing(labels, classes, eta, dtype):
    assert classes > 0
    assert eta >= 0.0 and eta < 1.0

    return flow.one_hot(labels, depth=classes, dtype=dtype,
                        on_value=1 - eta + eta / classes, off_value=eta/classes)

@flow.global_function("train", get_train_config(args))
def TrainNet():
    cfg = LoadCfg(args=args, model_load_dir=args.model_load_dir, load_type='train')
    labels, images = LoadData(args, 'train')
    if args.model in ("resnet", "vgg", "alexnet", "alexnet_simple", "lenet"):
        logits = model_dict[args.model](images, cfg, optimizer=args.model_update,
                                        need_transpose=False if args.train_data_dir else True,
                                        bn=args.bn)
    else:
        logits = model_dict[args.model](images, cfg, optimizer=args.model_update)
    if args.label_smoothing > 0:
        one_hot_labels = label_smoothing(labels, args.num_classes, args.label_smoothing, logits.dtype)
        loss = flow.nn.softmax_cross_entropy_with_logits(one_hot_labels, logits, name="softmax_loss")
    else:
        loss = flow.nn.sparse_softmax_cross_entropy_with_logits(labels, logits, name="softmax_loss")
    
#    lr_scheduler = flow.optimizer.PiecewiseConstantScheduler([], [args.learning_rate])
#    flow.optimizer.SGD(lr_scheduler, momentum=args.mom).minimize(loss)
    flow.losses.add_loss(loss)
    predictions = flow.nn.softmax(logits)
    outputs = {"loss": loss, "predictions": predictions, "labels": labels}
#    outputs = {"loss": loss, "predictions": predictions, "labels": labels, 'logits':logits}
    return outputs


@flow.global_function("predict", get_val_config(args))
def InferenceNet():
    cfg = LoadCfg(args=args, model_load_dir=args.model_load_dir, load_type='test')
    labels, images = LoadData(args, 'test')
    if args.model in ("resnet", "vgg", "alexnet", "alexnet_simple", "lenet"):
        logits = model_dict[args.model](images, cfg, optimizer=args.model_update,
                                        need_transpose=False if args.train_data_dir else True,
                                        model_weight=False, bn=args.bn)
    else:
        logits = model_dict[args.model](images, cfg, optimizer=args.model_update, model_weight=False)
    
    predictions = flow.nn.softmax(logits)
    outputs = {"predictions": predictions, "labels": labels}
    return outputs


def main():
    InitNodes(args)

    flow.env.grpc_use_no_signal()
    flow.env.log_dir(args.log_dir)

    summary = Summary(args.log_dir, args)  
    snapshot = Snapshot(args.model_save_dir, args.model_load_dir)
    #open log file
    log_file = open("./log/log_"+args.model+"_"+args.data_type+"_"+args.log_type+".txt", "w")
    if not args.before_result_dir:
        args.before_result_dir = "./log/before"
    if not args.after_result_dir:
        args.after_result_dir = "./log/after"

    for epoch in range(args.num_epochs):
        #config callback func during training
        metric = Metric(desc='train', calculate_batches=args.loss_print_every_n_iter,
                        summary=summary, save_summary_steps=epoch_size,
                        batch_size=train_batch_size, loss_key='loss')
        #training...(epoch times = epoch_size)
        for i in range(epoch_size):
            TrainNet().async_get(metric.metric_cb(epoch, i))

        if args.val_data_dir:
            #config callback func during testing
            metric = Metric(desc='validation', calculate_batches=num_val_steps, summary=summary,
                            save_summary_steps=num_val_steps, batch_size=val_batch_size)
            #tesing
            for i in range(num_val_steps):
                InferenceNet().async_get(metric.metric_cb(epoch, i, args=args, log_file=log_file))
        if epoch % args.model_save_every_n_epoch == 0:
            snapshot.save('epoch_{}'.format(epoch))
            flow.sync_default_session()
    #save last_snapeshot and model weight
    snapshot.save('last')
    flow.sync_default_session()
    weights_profile_path = os.path.join(args.model_save_dir, "weights_profile_path")
    modelWeight.save(weights_profile_path)


if __name__ == "__main__":
    os.system("rm -rf {0}".format(args.model_save_dir))
    main()
