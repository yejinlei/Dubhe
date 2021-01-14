"""
Copyright 2020 The OneFlow Authors. All rights reserved.

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
import argparse
import shutil
import re


def str2bool(v):
    if v.lower() in ('yes', 'true', 't', 'y', '1'):
        return True
    elif v.lower() in ('no', 'false', 'f', 'n', '0'):
        return False
    else:
        raise argparse.ArgumentTypeError('Unsupported value encountered.')


parser = argparse.ArgumentParser()
parser.add_argument("--teacher_model", default=None, type=str, help="The teacher model dir.")
parser.add_argument("--student_model", default=None, type=str, help="The student model dir.")
parser.add_argument("--layer_list", default="2,6,10", type=str,
                    help="the set of intermediate layers to distill knowledge from")
args = parser.parse_args()

# args.layer_list =
args.layer_list = [int(i) for i in args.layer_list.split(',')]
args.layer_num = len(args.layer_list)

student_filelist = []


def subString(template):
    rule = r'bert-encoder-layer_(.*?)-'
    slotList = re.findall(rule, template)
    return slotList


def CopyFile(filepath, newPath):
    if not os.path.exists(newPath):
        os.makedirs(newPath)
    fileNames = os.listdir(filepath)
    for file in fileNames:
        newDir = os.path.join(filepath,file)
        if os.path.isfile(newDir):
            newFile = os.path.join(newPath, file)
            shutil.copyfile(newDir, newFile)
        else:
            if not os.path.exists(os.path.join(newPath, file)):
                os.makedirs(os.path.join(newPath, file))
            CopyFile(newDir, os.path.join(newPath, file))


if not os.path.exists(args.student_model):
    os.makedirs(args.student_model)


for a, b, c in os.walk(args.teacher_model):
    for subdir in b:
        if str(subdir[-2:]) == '-v' or str(subdir[-2:]) == '-m':
            continue
        teacher_layer_num = subString(subdir)
        # print('| teacher_layer_num: {}'.format(teacher_layer_num))
        if len(teacher_layer_num) == 0:
            teacher_model_subdir = os.path.join(args.teacher_model, subdir)
            student_model_subdir = os.path.join(args.student_model, subdir)
            # print('| teacher model subdir: {} | student model subdir: {}'.format(
            #     teacher_model_subdir, student_model_subdir))
            CopyFile(teacher_model_subdir, student_model_subdir)
        else:
            teacher_layer_num = int(teacher_layer_num[0])
            teacher_model_source_subdir = os.path.join(args.teacher_model, subdir)
            teacher_model_target_subdir = os.path.join(args.student_model, subdir)
            CopyFile(teacher_model_source_subdir, teacher_model_target_subdir)
            # print('| teacher_layer_num: {}'.format(teacher_layer_num))
            # print(subdir, subdir.split('layer', 1))
            prefix, suffix = subdir.split('layer', 1)
            student_subdir = prefix + 'student-layer' + suffix
            # student_subdir = 'student-' + subdir
            # print('| student_subdir: ', student_subdir)
            if teacher_layer_num in args.layer_list:
                student_layer_num = args.layer_list.index(teacher_layer_num)
                rule = r'bert-encoder-layer_(.*?)-'
                x = re.sub(rule, 'bert-encoder-layer_{}-'.format(str(student_layer_num)), student_subdir)
                # print('| x: ', x)
                teacher_model_subdir = os.path.join(args.teacher_model, subdir)
                student_model_subdir = os.path.join(args.student_model, x)
                # print('| teacher model subdir: {} | student model subdir: {}'.format(teacher_model_subdir,
                #                                                                      student_model_subdir))
                CopyFile(teacher_model_subdir, student_model_subdir)
