# !/usr/bin/env python
# -*- coding:utf-8 -*-

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
=============================================================
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import argparse
from datetime import datetime


def get_parser(parser=None):
    def str_list(x):
        return x.split(',')

    def int_list(x):
        return list(map(int, x.split(',')))

    def float_list(x):
        return list(map(float, x.split(',')))

    if parser is None:
        parser = argparse.ArgumentParser("flags for cnn benchmark")

    parser.add_argument(
        "--image_path",
        type=str,
        default='tiger.jpg',
        help="image path")
    parser.add_argument(
        "--model_load_dir",
        type=str,
        default=None,
        help="model load directory")

    # for data process
    parser.add_argument(
        "--num_classes",
        type=int,
        default=1000,
        help="num of pic classes")
    parser.add_argument(
        '--rgb-mean',
        type=float_list,
        default=[
            123.68,
            116.779,
            103.939],
        help='a tuple of size 3 for the mean rgb')
    parser.add_argument(
        '--rgb-std',
        type=float_list,
        default=[
            58.393,
            57.12,
            57.375],
        help='a tuple of size 3 for the std rgb')

    parser.add_argument(
        "--log_dir",
        type=str,
        default="./output",
        help="log info save directory")
    return parser


def print_args(args):
    print("=".ljust(66, "="))
    print(
        "Running {}: num_gpu_per_node = {}, num_nodes = {}.".format(
            'ResNet50 V1.5',
            1,
            1))
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
