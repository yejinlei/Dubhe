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
import argparse
import json
from datetime import datetime


def str2bool(v):
    if v.lower() in ('yes', 'true', 't', 'y', '1'):
        return True
    elif v.lower() in ('no', 'false', 'f', 'n', '0'):
        return False
    else:
        raise argparse.ArgumentTypeError('Unsupported value encountered.')


parser = argparse.ArgumentParser()

parser.add_argument("--model", default="alexnet",
                    type=str, help="Model")
parser.add_argument("--data_type", default="cifar10",
                    type=str, help="Dataset name")
parser.add_argument("--bn", type=str2bool,
                    default=True, help="Whether to use use bn layer")
parser.add_argument("--percent", default="0.5",
                    type=str, help="scale sparse rate (default: 0.7)")
parser.add_argument("--prune_method", default='bn',
                    type=str, help="method of prune(bn, conv_avg, random...)")
parser.add_argument("--step", default='123',
                    type=str, help="choose steps from train, prune, refine")
parser.add_argument("--dataset_dir", type=str, default="./ofData/cifar10", help="dataset info load directory")
# snapshot
parser.add_argument("--model_save_dir", type=str, default="./models", help="model save directory", )
# log, save and loss print
parser.add_argument("--model_dir", type=str, default="./model", help="model info save directory")
parser.add_argument("--log_dir", type=str, default="./log", help="log info save directory")
parser.add_argument("--before_result_dir", type=str, default="./result/before", help="the save directory of results")
parser.add_argument("--after_result_dir", type=str, default="./result/after", help="the save directory of results")

args = parser.parse_args()


def getCommand():
    model = args.model
    data_type = args.data_type
    dataset_dir = args.dataset_dir
    model_save_dir = args.model_save_dir
    log_dir = args.log_dir
    before_result_dir = args.before_result_dir
    after_result_dir = args.after_result_dir
    num_classes, train_data_part_num, val_data_part_num = "", "", ""
    image_shape, image_size, resize_shorter = "", "", ""
    rgb_mean, rgb_std, num_examples, num_val_examples = "", "", "", ""
    bn = args.bn
    prune = ""
    percent = args.percent
    prune_method = args.prune_method

    if "dnn" in args.model:
        bn = "False"
        prune = "Dnn"
    elif args.model == "lenet":
        prune = "Lenet"
    elif "alexnet" in args.model:
        prune = "Alexnet"
    elif args.model == "vgg":
        prune = "Vggnet"
    elif args.model == "resnet":
        prune = "Resnet"

    if data_type == "cifar10":
        num_classes, train_data_part_num, val_data_part_num = "10", "5", "1"
        image_shape, image_size, resize_shorter = "3,32,32", "32", "32"
        rgb_mean, rgb_std = "124.95,122.65,114.75", "61.252,60.767,65.852"
        num_examples, num_val_examples = "50000", "10000"
    elif data_type == "cifar100":
        num_classes, train_data_part_num, val_data_part_num = "100", "5", "1"
        image_shape, image_size, resize_shorter = "3,32,32", "32", "32"
        rgb_mean, rgb_std = "124.95,122.65,114.75", "61.252,60.767,65.852"
        num_examples, num_val_examples = "50000", "10000"
    elif data_type == "mnist":
        num_classes, train_data_part_num, val_data_part_num = "10", "6", "1"
        image_shape, image_size, resize_shorter = "1,28,28", "28", "32"
        rgb_mean, rgb_std = "33.3285", "78.5655"
        num_examples, num_val_examples = "60000", "10000"
    elif data_type == "svhn":
        num_classes, train_data_part_num, val_data_part_num = "10", "1", "1"
        image_shape, image_size, resize_shorter = "32,32,3", "32", "32"
        rgb_mean, rgb_std = "111.61,113.16,120.57", "50.50,51.26,50.24"
        num_examples, num_val_examples = "73257", "26032"
    elif data_type == "imageNet":
        num_classes, train_data_part_num, val_data_part_num = "1000", "30", "2"
        image_shape, image_size, resize_shorter = "3,224,224", "224", "256"
        rgb_mean, rgb_std = "123.68,116.779,103.939", "58.393,57.12,57.375"
        num_examples, num_val_examples = "64000", "6400"
    else:
        with open(dataset_dir + "/meta.json") as f_meta:
            dict_meta = json.load(f_meta)
        shape = dict_meta["image_shape"]
        mean_list = dict_meta["rgb_mean"]
        std_list = dict_meta["rgb_std"]

        num_classes = str(dict_meta["num_classes"])
        train_data_part_num, val_data_part_num = "1", "1"
        image_shape = str(shape[0]) + "," + str(shape[1]) + "," + str(shape[2])
        image_size, resize_shorter = str(shape[1]), str(shape[1])
        rgb_mean, rgb_std = "", ""
        for mean in mean_list:
            rgb_mean += str(mean) + ","
        rgb_mean = rgb_mean.strip(",")
        for std in std_list:
            rgb_std += str(std) + ","
        rgb_std = rgb_std.strip(",")
        num_examples = dict_meta["num_examples"]
        num_val_examples = dict_meta["num_val_examples"]

    command1 = "python3 ./train_val.py \
                --model={0} \
                --data_type={1} \
                --log_type=base_model \
                --model_update=adam \
                --num_classes={2} \
                --train_data_dir={13}/train \
                --train_data_part_num={3} \
                --val_data_dir={13}/test \
                --val_data_part_num={4} \
                --num_nodes=1 \
                --gpu_num_per_node=1 \
                --loss_print_every_n_iter=1 \
                --label_smoothing=0 \
                --warmup_epochs=0 \
                --lr_decay=None \
                --image_shape={5} \
                --image_size={6} \
                --resize_shorter={7} \
                --rgb_mean={8} \
                --rgb_std={9} \
                --num_examples={10} \
                --num_val_examples={11} \
                --batch_size_per_device=32 \
                --val_batch_size_per_device=32 \
                --learning_rate=0.001 \
                --bn={12} \
                --num_epochs=2 \
                --model_save_every_n_epoch=10 \
                --model_save_dir={16}/model_base \
                --log_dir={14} \
                --before_result_dir={15}" \
        .format(model, data_type, num_classes, train_data_part_num,
                val_data_part_num, image_shape, image_size,
                resize_shorter, rgb_mean, rgb_std,
                num_examples, num_val_examples, bn, dataset_dir, log_dir, before_result_dir, model_save_dir)

    command2 = "python3 ./prune/prune{0}.py \
                --percent={1} \
                --optimizer=adam \
                --prune_method={2} \
                --bn={3} \
                --model_load_dir={4}/model_base/snapshot_last \
                --model_save_dir={4}/model_prune" \
        .format(prune, percent, prune_method, bn, model_save_dir)

    if "dnn" in args.model:
        command2 = "python3 ./prune/prune{0}.py \
                    --percent={1} \
                    --optimizer=adam \
                    --model_load_dir={2}/model_base/snapshot_last \
                    --model_save_dir={2}/model_prune" \
            .format(prune, percent, model_save_dir)

    command3 = "python3 ./train_val.py \
                --model={0} \
                --data_type={1} \
                --log_type=prune_model \
                --model_update=adam \
                --num_classes={2} \
                --train_data_dir={13}/train \
                --train_data_part_num={3} \
                --val_data_dir={13}/test \
                --val_data_part_num={4} \
                --num_nodes=1 \
                --gpu_num_per_node=1 \
                --loss_print_every_n_iter=1 \
                --label_smoothing=0 \
                --warmup_epochs=0 \
                --lr_decay=None \
                --image_shape={5} \
                --image_size={6} \
                --resize_shorter={7} \
                --rgb_mean={8} \
                --rgb_std={9} \
                --num_examples={10} \
                --num_val_examples={11} \
                --batch_size_per_device=32 \
                --val_batch_size_per_device=32 \
                --learning_rate=0.001 \
                --bn={12} \
                --num_epochs=2 \
                --model_save_every_n_epoch=10 \
                --model_save_dir={15}/model_refine \
                --model_load_dir={15}/model_prune/model \
                --log_dir={14} \
                --after_result_dir={16}" \
        .format(model, data_type, num_classes, train_data_part_num,
                val_data_part_num, image_shape, image_size,
                resize_shorter, rgb_mean, rgb_std,
                num_examples, num_val_examples, bn, dataset_dir, log_dir, model_save_dir, after_result_dir)

    return command1, command2, command3


def main():
    command1, command2, command3 = getCommand()
    step = args.step
    # print(command1)
    if "1" in step:
        os.system(command1)
    if "2" in step:
        os.system(command2)
    if "3" in step:
        os.system(command3)


if __name__ == "__main__":
    main()
