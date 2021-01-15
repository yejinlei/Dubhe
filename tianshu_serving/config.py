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

import ast
import os
import argparse
from datetime import datetime


def get_parser(parser=None):
    PROJECT_ROOT = os.path.dirname(os.path.abspath(__file__))

    def str_list(x):
        return x.split(',')

    def int_list(x):
        resize_list = list(map(int, x.replace('[', '').replace(']', '').split(',')))
        if len(resize_list) < 2:
            return [resize_list[0], resize_list[0]]
        elif len(resize_list) == 2:
            return list(map(int, x.replace('[', '').replace(']', '').split(',')))
        else:
            raise argparse.ArgumentTypeError('Unsupported value encountered.')

    def str2bool(v):
        if v.lower() in ('yes', 'true', 't', 'y', '1'):
            return True
        elif v.lower() in ('no', 'false', 'f', 'n', '0'):
            return False
        else:
            raise argparse.ArgumentTypeError('Unsupported value encountered.')

    if parser is None:
        parser = argparse.ArgumentParser("flags for cnn benchmark")

    parser.add_argument("--platform", type=str, default='pytorch', help="platform")
    parser.add_argument("--dtype", type=str, default='float32', help="float16 float32")
    parser.add_argument("--gpu_num_per_node", type=int, default=1)
    parser.add_argument('--num_nodes', type=int, default=1, help='node/machine number for training')
    parser.add_argument('--node_ips', type=str_list, default=['192.168.1.13', '172.17.0.7', '192.168.1.14'],
                        help='nodes ip list for training, devided by ",", length >= num_nodes')
    parser.add_argument("--model_name", type=str, default="default", help="model name")
    parser.add_argument("--signature_name", type=str, default='serving_default', help="tensorflow signature name")
    parser.add_argument("--model_structure", type=str, default="model", help="pytorch model structure")
    parser.add_argument("--job_name", type=str, default="inference", help="oneflow job name")
    parser.add_argument("--prepare_mode", type=str, default="tfhub",
                        help="tensorflow prepare mode(tfhub、caffe、tf、torch)")
    parser.add_argument("--use_gpu", type=ast.literal_eval, default=True, help="is use gpu")
    parser.add_argument('--channel_last', type=str2bool, nargs='?', const=False,
                        help='Whether to use use channel last mode(nhwc)')
    parser.add_argument("--model_path", type=str, default="/usr/local/model/pytorch_models/resnet50/",
                        help="model load directory if need")
    parser.add_argument("--image_path", type=str, default='/usr/local/data/fish.jpg', help="image path")
    parser.add_argument("--reshape_size", type=int_list, default='[224]',
                        help="The reshape size of the image(eg. 224)")
    parser.add_argument("--num_classes", type=int, default=1000, help="num of pic classes")
    parser.add_argument("--log_dir", type=str, default=PROJECT_ROOT + '/logs',
                        help="log info save directory")
    parser.add_argument("--level", type=str, default="debug", help="level for logging")
    parser.add_argument("--host", type=str, default="0.0.0.0", help="The host of the server(eg. 0.0.0.0)")
    parser.add_argument("--port", default=int(5000), help="The port of the server(eg. 5000)", type=int)
    parser.add_argument("--enable_tls", type=ast.literal_eval, default=False, help="If enable use grpc tls")
    parser.add_argument("--secret_crt", type=str, default=PROJECT_ROOT + '/tls_crt/server.crt', help="TLS crt file")
    parser.add_argument("--secret_key", type=str, default=PROJECT_ROOT + '/tls_crt/server.key', help="TLS key file")
    parser.add_argument("--model_config_file", type=str, default="", help="The file of the model config(eg. '')")
    parser.add_argument("--enable_distributed", type=ast.literal_eval, default=False, help="If enable use distributed "
                                                                                           "environment")
    parser.add_argument("--input_path", type=str, default="/usr/local/data/images/", help="images path")
    parser.add_argument("--output_path", type=str, default="/usr/local/output_path/", help="json path")

    return parser


def print_args(args):
    print("=".ljust(66, "="))
    print("Running {}: num_gpu_per_node = {}, num_nodes = {}.".format(
        args.model_name, args.gpu_num_per_node, args.num_nodes))
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
