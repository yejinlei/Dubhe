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
import time
import numpy as np
import pandas as pd
import oneflow as flow
import util.ofrecord_util as ofrecord_util
from util.model_weights import modelWeight
import json

def InitNodes(args):
    if args.num_nodes > 1:
        assert args.num_nodes <= len(args.node_ips)
        flow.env.ctrl_port(12138)
        nodes = []
        for ip in args.node_ips:
            addr_dict = {}
            addr_dict["addr"] = ip
            nodes.append(addr_dict)

        flow.env.machine(nodes)
        
# laod cfg (model structure)
def LoadCfg(args, model_load_dir, load_type):
    if model_load_dir:
        if args.model == "resnet":
            assert os.path.isdir(model_load_dir)
            of_weight_path = model_load_dir.rsplit("/",1)[0] + "/weights_profile_path" 
            cfg_temp = []
            cfg = []
            weights_dict = modelWeight.load(of_weight_path)
            for name, profile_dict in weights_dict.items():
                if name.endswith("weight") and "stem" not in name and "shortcut" not in name:
                    shape=profile_dict["shape"]
                    cfg_temp.append(shape[0])
            cfg.append(cfg_temp[0:9])
            cfg.append(cfg_temp[9:21])
            cfg.append(cfg_temp[21:39])
            cfg.append(cfg_temp[39:48])
            cfg.append(cfg_temp[48])
            if load_type == 'train':
                modelWeight.weights_dict = {}
            
        else:            
            assert os.path.isdir(model_load_dir)
            of_weight_path = model_load_dir.rsplit("/",1)[0] + "/weights_profile_path" 
            cfg = []
            weights_dict = modelWeight.load(of_weight_path)
            for name, profile_dict in weights_dict.items():
                if name.endswith("weight"):
                    shape=profile_dict["shape"]
                    cfg.append(shape[0])
#           print(load_type, modelWeight.weights_dict)
            if load_type == 'train':
                modelWeight.weights_dict = {}
    else:
        if args.model == 'vgg':
            # cfg = [64, 64, 128, 128, 256, 256, 256, 512, 512, 512, 512, 512, 512, 4096, 4096, args.num_classes]
            cfg = [64, 64, 128, 128, 256, 256, 256, 512, 512, 512, 512, 512, 512, 512, 128, args.num_classes]
        elif args.model == 'alexnet':
            cfg = [96, 256, 384, 384, 256, 4096, 4096, args.num_classes]
        elif args.model == 'alexnet_simple':
            cfg = [24, 96, 192, 192, 96, 1024, 1024, args.num_classes]
        elif args.model == 'lenet':
            cfg = [6, 16, 120, 84, args.num_classes]
        elif args.model ==  "resnet":
            cfg = [[64, 64, 256, 64, 64, 256, 64, 64, 256],
                   [128, 128, 512, 128, 128, 512, 128, 128, 512, 128, 128, 512],
                   [256, 256, 1024, 256, 256, 1024, 256, 256, 1024, 256, 256, 1024, 256, 256, 1024, 256, 256, 1024],
                   [512, 512, 2048, 512, 512, 2048, 512, 512, 2048], args.num_classes]
        elif args.model == 'dnn_2':
            cfg = [128, args.num_classes]
        elif args.model == 'dnn_4':
            cfg = [4096, 256, 128, args.num_classes]
        else:
            cfg = []
    if load_type == 'train':
        print('Model structure:', cfg)   
    return cfg

# laod cfg(model structure)
def LoadData(args, load_type):
#    total_device_num = args.num_nodes * args.gpu_num_per_node
#    train_batch_size = total_device_num * args.batch_size_per_device
#    val_batch_size = total_device_num * args.val_batch_size_per_device
    if load_type == 'train':
        if args.train_data_dir:
            assert os.path.exists(args.train_data_dir)
            print("Loading data from {}".format(args.train_data_dir))
            if args.data_type == 'imageNet':
                (labels, images) = ofrecord_util.load_imagenet_for_training(args)
            elif args.data_type == 'cifar10' or args.data_type == 'cifar100':
                (labels, images) = ofrecord_util.load_cifar_for_training(args)
            elif args.data_type == 'mnist' or args.data_type == 'mnist_32':
                (labels, images) = ofrecord_util.load_mnist_for_training(args)
            elif args.data_type == 'svhn':
                (labels, images) = ofrecord_util.load_svhn_for_training(args)
            elif args.data_type == 'random':
                (labels, images) = ofrecord_util.load_synthetic(args)
            else:
                (labels, images) = ofrecord_util.load_mydata_for_training(args)
        else:
            print("Loading synthetic data.")
            (labels, images) = ofrecord_util.load_synthetic(args)
    elif load_type == 'test':
        if args.val_data_dir:
            assert os.path.exists(args.val_data_dir)
            print("Loading data from {}".format(args.val_data_dir))
            if args.data_type == 'imageNet':
                (labels, images) = ofrecord_util.load_imagenet_for_validation(args)
            elif args.data_type == 'cifar10' or args.data_type == 'cifar100':
                (labels, images) = ofrecord_util.load_cifar_for_training(args)
            elif args.data_type == 'mnist' or args.data_type == "mnist_32":
                (labels, images) = ofrecord_util.load_mnist_for_validation(args)
            elif args.data_type == 'svhn':
                (labels, images) = ofrecord_util.load_svhn_for_validation(args)
            elif args.data_type == 'random':
                (labels, images) = ofrecord_util.load_synthetic(args)
            else:
                (labels, images) = ofrecord_util.load_mydata_for_training(args)
        else:
            print("Loading synthetic data.")
            (labels, images) = ofrecord_util.load_synthetic(args)
    else:
        print("Loading synthetic data.")
        (labels, images) = ofrecord_util.load_synthetic(args)
    return labels, images

#get save path and load path of model
#def getSaveLoadDir(args):
#    if args.default_dir == 'train':
#        model_save_dir = './output/snapshots/model_base'
#        if args.data_type == 'imageNet':
#            if args.model == 'vgg':
#                model_load_dir = './model_init/vgg/model_init_imageNet/of_init_model'
#            elif args.model == 'alexnet':
#                model_load_dir = './model_init/alexnet/model_init_imageNet/of_init_model'
#            elif args.model == 'lenet':
#                model_load_dir = './model_init/lenet/model_init_imageNet/of_init_model'
#        elif args.data_type == 'cifar10':
#            if args.model == 'vgg':
#                model_load_dir = './model_init/vgg/model_init_cifar10/of_init_model'
#            elif args.model == 'alexnet':
#                model_load_dir = './model_init/alexnet/model_init_cifar10/of_init_model'
#            elif args.model == 'lenet':
#                model_load_dir = './model_init/lenet/model_init_cifar10/of_init_model'
#    elif args.default_dir == 'refine':
#        model_save_dir = './output/snapshots/model_refine'
#        model_load_dir = './output/snapshots/model_prune/model'
#    else:
#        model_save_dir = args.model_save_dir
#        model_load_dir = args.model_load_dir
#    return model_save_dir, model_load_dir

class Snapshot(object):
    def __init__(self, model_save_dir, model_load_dir):
        self._model_save_dir = model_save_dir
        self._check_point = flow.train.CheckPoint()
        if model_load_dir:
            assert os.path.isdir(model_load_dir)
            print("Restoring model from {}.".format(model_load_dir))
            self._check_point.load(model_load_dir)
        else:
            self._check_point.init()
            self.save('initial_model')
            print("Init model on demand.")

    def save(self, name):
        snapshot_save_path = os.path.join(self._model_save_dir, "snapshot_{}".format(name))
        if not os.path.exists(snapshot_save_path):
            os.makedirs(snapshot_save_path)
        print("Saving model to {}.".format(snapshot_save_path))
        self._check_point.save(snapshot_save_path)


class Summary(object):
    def __init__(self, log_dir, config, filename='summary.csv'):
        self._filename = filename
        self._log_dir = log_dir
        if not os.path.exists(log_dir): os.makedirs(log_dir)
        self._metrics = pd.DataFrame({"epoch":0, "iter": 0, "legend": "cfg", "note": str(config)}, index=[0])

    def scalar(self, legend, value, epoch, step=-1):
        # TODO: support rank(which device/gpu)
        df = pd.DataFrame(
            {"epoch": epoch, "iter": step, "legend": legend, "value": value, "rank": 0},
            index=[0])
        self._metrics = pd.concat([self._metrics, df], axis=0, sort=False)

    def save(self):
        save_path = os.path.join(self._log_dir, self._filename)
        self._metrics.to_csv(save_path, index=False)


class StopWatch(object):
    def __init__(self):
        pass

    def start(self):
        self.start_time = time.time()
        self.last_split = self.start_time

    def split(self):
        now = time.time()
        duration = now - self.last_split
        self.last_split = now
        return duration

    def stop(self):
        self.stop_time = time.time()

    def duration(self):
        return self.stop_time - self.start_time


def match_top_k(predictions, labels, top_k=1):
    max_k_preds = np.argpartition(predictions.numpy(), -top_k)[:, -top_k:]
    match_array = np.logical_or.reduce(max_k_preds==labels.reshape((-1, 1)), axis=1)
    num_matched = match_array.sum()
    return num_matched, match_array.shape[0]


class Metric(object):
    def __init__(self, summary=None, save_summary_steps=-1, desc='train', calculate_batches=-1,
                 batch_size=256, top_k=6, prediction_key='predictions', label_key='labels',
                 loss_key=None):
        self.summary = summary
        self.save_summary = isinstance(self.summary, Summary)
        self.save_summary_steps = save_summary_steps
        self.desc = desc
        self.calculate_batches = calculate_batches
        self.top_k = top_k
        self.prediction_key = prediction_key
        self.label_key = label_key
        self.loss_key = loss_key
        self.teacher_model_size = 0
        self.student_model_size = 0
        if loss_key:
            self.fmt = "{}: epoch {}, iter {}, loss: {:.6f}, accuracy(top1): {:.6f}, accuracy(topk): {:.6f}, samples/s: {:.3f}"
        else:
            self.fmt = "{}: epoch {}, iter {}, accuracy(top1): {:.6f}, accuracy(topk): {:.6f}, samples/s: {:.3f}"

        self.timer = StopWatch()
        self.timer.start()
        self._clear()

    def _clear(self):
        self.top_1_num_matched = 0
        self.top_k_num_matched = 0
        self.num_samples = 0.0

    def metric_cb(self, epoch, step, args=None, log_file=None):
        def callback(outputs):
            if step == 0: self._clear()
            if self.prediction_key:
                num_matched, num_samples = match_top_k(outputs[self.prediction_key],
                                                       outputs[self.label_key])
                self.top_1_num_matched += num_matched
                num_matched, _ = match_top_k(outputs[self.prediction_key],
                                             outputs[self.label_key], self.top_k)
                self.top_k_num_matched += num_matched
            else:
                num_samples = outputs[self.label_key].shape[0]

            self.num_samples += num_samples

            if (step + 1) % self.calculate_batches == 0:
                throughput = self.num_samples / self.timer.split()
                if self.prediction_key:
                    top_1_accuracy = self.top_1_num_matched / self.num_samples
                    top_k_accuracy = self.top_k_num_matched / self.num_samples
                else:
                    top_1_accuracy = 0.0
                    top_k_accuracy = 0.0

                if self.loss_key:
                    loss = outputs[self.loss_key].mean()
                    print(self.fmt.format(self.desc, epoch, step + 1, loss, top_1_accuracy,
                                          top_k_accuracy, throughput))
#                    print(outputs[self.prediction_key].numpy(), 
#                          outputs[self.label_key].numpy(),
#                          outputs['logits'].numpy())
                    if self.save_summary:
                        self.summary.scalar(self.desc+"_" + self.loss_key, loss, epoch, step)
                else:
                    print('*'*106)
                    print(self.fmt.format(self.desc, epoch, step + 1, top_1_accuracy,
                                          top_k_accuracy, throughput))


                    if self.desc=='validation':

                        def getdirsize(dir):
                            size = 0
                            for root, dirs, files in os.walk(dir):
                                for name in files:
                                    if str(root[-2:]) == '-v' or str(root[-2:]) == '-m':
                                        pass
                                    else:
                                        tmp = os.path.getsize(os.path.join(root, name))
                                        size += tmp
                                # size += sum([os.path.getsize(os.path.join(root, name)) for name in files])
                            return size
                        model_size = 0
                        if args.log_type == 'base_model':
                            if os.path.exists(os.path.join(args.model_save_dir,'snapshot_initial_model')):
                                self.teacher_model_size = getdirsize(os.path.join(args.model_save_dir,'snapshot_initial_model'))
                            elif os.path.exists(os.path.join(args.model_save_dir,'snapshot_last')):
                                self.teacher_model_size = getdirsize(os.path.join(args.model_save_dir,'snapshot_last'))
                            elif os.path.exists(os.path.join(args.model_save_dir,'snapshot_epoch_0')):
                                self.teacher_model_size = getdirsize(os.path.join(args.model_save_dir,'snapshot_epoch_0'))
                            else:
                                print('Error, not find {}'.format(args.model_save_dir))
                            model_size = self.teacher_model_size  # 获取teacher model大小, 即 model_base/snapshot_initial_model 文件夹大小
                        elif args.log_type == 'prune_model':
                            if os.path.exists(args.model_load_dir):
                                self.student_model_size = getdirsize(args.model_load_dir)
                            else:
                                print('Error, not find {}'.format(args.model_load_dir))
                            model_size = self.student_model_size  # 获取student model大小，即 model_prune/model 文件夹大小

                        save_dict = {"accuracy": "%.2f" % top_1_accuracy,
                                     "top_k_accuracy": "%.2f" % top_k_accuracy,
                                     "top_k": "%d" % self.top_k,
                                     "modelSize": "%d" % (model_size / 1024 / 1024),
                                     "reasoningTime": "%.2f" % throughput
                                     }  # samples/second

                        if args.log_type == 'base_model':
                            if not os.path.exists(args.before_result_dir):
                                os.makedirs(args.before_result_dir)
                            with open(os.path.join(args.before_result_dir, "results_eval.json"), "w") as f:
                                json.dump(save_dict, f)
                        if args.log_type == 'prune_model':
                            if not os.path.exists(args.after_result_dir):
                                os.makedirs(args.after_result_dir)
                            with open(os.path.join(args.after_result_dir, "results_eval.json"), "w") as f:
                                json.dump(save_dict, f)
                    if log_file:
                        log_file.write("epoch"+str(epoch)+" top_1_accuracy: "+str(top_1_accuracy)+\
                                            "; top_k_accuracy: "+str(top_k_accuracy)+"; "+str(throughput)+"samples/s\n")
                    print('*'*106)

                self._clear()
                if self.save_summary:
                    self.summary.scalar(self.desc + "_throughput", throughput, epoch, step)
                    if self.prediction_key:
                        self.summary.scalar(self.desc + "_top_1", top_1_accuracy, epoch, step)
                        self.summary.scalar(self.desc + "_top_{}".format(self.top_k),
                                            top_k_accuracy, epoch, step)

            if self.save_summary:
                if (step + 1) % self.save_summary_steps == 0:
                    self.summary.save()
                    

        return callback


