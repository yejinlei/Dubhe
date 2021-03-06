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

import argparse
from datetime import datetime


from util.optimizer_util import add_optimizer_args
from util.ofrecord_util import add_ofrecord_args


def get_parser(parser=None):
    def str_list(x):
        return x.split(',')

    def int_list(x):
        return list(map(int, x.split(',')))

    def float_list(x):
        return list(map(float, x.split(',')))

    def str2bool(v):
        if v.lower() in ('yes', 'true', 't', 'y', '1'):
            return True
        elif v.lower() in ('no', 'false', 'f', 'n', '0'):
            return False
        else:
            raise argparse.ArgumentTypeError('Unsupported value encountered.')

    if parser is None:
        parser = argparse.ArgumentParser("flags for cnn benchmark")

    parser.add_argument("--dtype", type=str,
                        default='float32', help="float16 float32")

    # resouce
    parser.add_argument("--gpu_num_per_node", type=int, default=1)
    parser.add_argument('--num_nodes', type=int, default=1,
                        help='node/machine number for training')
    parser.add_argument('--node_ips', type=str_list, default=['192.168.1.13', '192.168.1.14'],
                        help='nodes ip list for training, devided by ",", length >= num_nodes')

    parser.add_argument("--model", type=str, default="vgg",
                        help="vgg, alexnet, lenet")
    parser.add_argument(
        '--use_fp16',
        type=str2bool,
        nargs='?',
        const=True,
        help='Whether to use use fp16'
    )
    parser.add_argument(
        '--use_boxing_v2',
        type=str2bool,
        nargs='?',
        const=True,
        help='Whether to use boxing v2'
    )

    # train and validaion
#    parser.add_argument("--default_dir", type=str,
#                        default='', help="use default model dir to save and load (train / refine)")
    parser.add_argument("--bn", type=str2bool,
                        default=False, help="Whether to use use bn layer")
    parser.add_argument("--data_type", type=str,
                        default='imageNet', help="type of dataser (imageNet / cifar10...)")
    parser.add_argument("--log_type", type=str,
                        default='base_model', help="type of log (base_model/prune_model)")
    parser.add_argument('--num_epochs', type=int,
                        default=90, help='number of epochs')
    parser.add_argument("--model_load_dir", type=str,
                        default=None, help="model load directory if need")
    parser.add_argument("--batch_size_per_device", type=int, default=64)
    parser.add_argument("--val_batch_size_per_device", type=int, default=8)

    # inference
    parser.add_argument("--image_path", type=str, default='tiger.jpg', help="image path")

    # for data process
    parser.add_argument("--num_classes", type=int, default=1000, help="num of pic classes")
    parser.add_argument("--num_examples", type=int,
                        default=300000, help="train pic number")
    parser.add_argument("--num_val_examples", type=int,
                        default=50000, help="validation pic number")
    parser.add_argument('--rgb_mean', type=float_list, default=[123.68, 116.779, 103.939],
                        help='a tuple of size 3 for the mean rgb')
    parser.add_argument('--rgb_std', type=float_list, default=[58.393, 57.12, 57.375],
                        help='a tuple of size 3 for the std rgb')
    parser.add_argument("--input_layout", type=str,
                        default='NHWC', help="NCHW or NHWC")
    parser.add_argument('--image_shape', type=int_list, default=[3, 224, 224],
                        help='the image shape feed into the network')
    parser.add_argument('--label_smoothing', type=float, default=0.1, help='label smoothing factor')

    # snapshot
    parser.add_argument("--model_save_dir", type=str,
                        default="./output/snapshots/model_save-{}".format(
                            str(datetime.now().strftime("%Y%m%d%H%M%S"))),
                        help="model save directory",
                        )

    # log, save and loss print
    parser.add_argument("--log_dir", type=str,default="./output", help="log info save directory")
    parser.add_argument("--before_result_dir", type=str,default="", help="the save directory of results")
    parser.add_argument("--after_result_dir", type=str, default="", help="the save directory of results")

    parser.add_argument("--loss_print_every_n_iter", type=int, default=1,
                        help="print loss every n iteration")
    parser.add_argument("--model_save_every_n_epoch", type=int, default=10,
                        help="save model every n epoch",)
    add_ofrecord_args(parser)
    add_optimizer_args(parser)
    return parser


def print_args(args):
    print("=".ljust(66, "="))
    print("Running {}: num_gpu_per_node = {}, num_nodes = {}.".format(
        args.model, args.gpu_num_per_node, args.num_nodes))
    print("=".ljust(66, "="))
    for arg in vars(args):
        print("{} = {}".format(arg, getattr(args, arg)))
    print("-".ljust(66, "-"))
    print("Time stamp: {}".format(
        str(datetime.now().strftime("%Y-%m-%d-%H:%M:%S"))))


if __name__ == '__main__':
    parser = get_parser()
    args = parser.parse_args()
    print_args(args)
