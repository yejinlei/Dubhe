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
import numpy as np
import os
from .model_weights import modelWeight
import random

parser = argparse.ArgumentParser()
dtype_dict={2:np.float32,
            3:np.float64,
            4:np.int8,
            5:np.int32,
            6:np.int64,
            9:np.float16}

parser.add_argument("--bn", default=False,
                    type=str, help="Whether to use use bn layer")
parser.add_argument("--prune_method", default='bn',
                    type=str, help="method of prune(bn, conv_avg, random...)")
parser.add_argument("--model_load_dir", default = './output/snapshots/model_base/snapshot_last',
                    type = str, required = False, help = "Path of base oneflow model")
parser.add_argument("--model_save_dir", default = './output/snapshots/model_prune', type = str,
                    required = False, help = "Path to the output OneFlow model.")
parser.add_argument("--percent", default = 0.7, type = float, required = False,
                    help = "scale sparse rate (default: 0.7)")
parser.add_argument("--optimizer", type=str, default="momentum", required=False, 
                    help="sgd, adam, momentum")
args = parser.parse_args()


def _LoadWeightBlob2Numpy(shape, folder, dtype):
    if not os.path.exists(folder):
        print('fail to find', folder)
    filename = os.path.join(folder, 'out')
    f = open(filename, 'r')
    n = np.fromfile(f, dtype=dtype)
    n = n.reshape(shape)
    f.close()
    return n

def name2array(name, weights_dict):
    folder=os.path.join(args.model_load_dir, name)
    profile_dict = weights_dict[name]
    shape=profile_dict["shape"]
    dtype=profile_dict["dtype"] 
    dtype=dtype_dict[dtype]
    array = _LoadWeightBlob2Numpy(shape,folder,dtype)
    return array, dtype, shape

# conv_avg剪枝方法：conv层weight的平均值作为缩放因子，获得对应阈值
def get_pruneThre_conv_avg():
    of_weight_path = args.model_load_dir.rsplit("/",1)[0] + "/weights_profile_path"
    weights_dict = modelWeight.load(of_weight_path)

    totalArray = []
    for name, profile_dict in weights_dict.items():
        if name.endswith("_weight") and "stem" not in name and "shortcut" not in name:
            array, dtype, shape = name2array(name, weights_dict)
            array = array.tolist()
            array_rank = []
            for i in range(0, shape[0]):
                array_i = array[i]
                array_i_faltten = [abs(m3) for m1 in array_i for m2 in m1 for m3 in m2]
                array_rank.append(sum(array_i_faltten)/(shape[1]*shape[2]*shape[3]))
            totalArray = totalArray + array_rank
    totalArray.sort()
    threIndex = int(len(totalArray) * args.percent)
    thre = totalArray[threIndex]
    print("threshold:", thre)
    return thre

# conv_all剪枝方法：conv层weight的总和作为缩放因子，获得对应阈值
def get_pruneThre_conv_all():
    of_weight_path = args.model_load_dir.rsplit("/",1)[0] + "/weights_profile_path"
    weights_dict = modelWeight.load(of_weight_path)

    totalArray = []
    for name, profile_dict in weights_dict.items():
        if name.endswith("_weight") and "stem" not in name and "shortcut" not in name:
            array, dtype, shape = name2array(name, weights_dict)
            array = array.tolist()
            array_rank = []
            for i in range(0, shape[0]):
                array_i = array[i]
                array_i_faltten = [abs(m3) for m1 in array_i for m2 in m1 for m3 in m2]
                array_rank.append(sum(array_i_faltten))
            totalArray = totalArray + array_rank
    totalArray.sort()
    threIndex = int(len(totalArray) * args.percent)
    thre = totalArray[threIndex]
    print("threshold:", thre)
    return thre

# conv_max剪枝方法：conv层weight的最大值作为缩放因子，获得对应阈值
def get_pruneThre_conv_max():
    of_weight_path = args.model_load_dir.rsplit("/",1)[0] + "/weights_profile_path"
    weights_dict = modelWeight.load(of_weight_path)

    totalArray = []
    for name, profile_dict in weights_dict.items():
        if name.endswith("_weight") and "stem" not in name and "shortcut" not in name:
            array, dtype, shape = name2array(name, weights_dict)
            array = array.tolist()
            array_rank = []
            for i in range(0, shape[0]):
                array_i = array[i]
                array_i_faltten = [abs(m3) for m1 in array_i for m2 in m1 for m3 in m2]
                array_rank.append(max(array_i_faltten))
            totalArray = totalArray + array_rank
    totalArray.sort()
    threIndex = int(len(totalArray) * args.percent)
    thre = totalArray[threIndex]
    print("threshold:", thre)
    return thre
  
# bn剪枝方法：bn层weight作为缩放因子，获得对应阈值
def get_pruneThre_bn():
    of_weight_path = args.model_load_dir.rsplit("/",1)[0] + "/weights_profile_path"
    weights_dict = modelWeight.load(of_weight_path)

    totalArray = []
    for name, profile_dict in weights_dict.items():
        if name.endswith("_bn-gamma") and "stem" not in name and "shortcut" not in name:
            array, dtype, shape = name2array(name, weights_dict)
            array = array.tolist()
            totalArray = totalArray + array
    totalArray.sort()
    threIndex = int(len(totalArray) * args.percent)
    thre = totalArray[threIndex]
    print("threshold:", thre)
    return thre

#获得剪枝dnn层weight的阈值
def get_pruneThre_fc():
    of_weight_path = args.model_load_dir.rsplit("/",1)[0] + "/weights_profile_path"
    weights_dict=modelWeight.load(of_weight_path)

    dictLen = len(weights_dict)
    numDiv = 0
    if args.optimizer == 'adam':
        numDiv = 6
    elif args.optimizer == 'momentum':
        numDiv = 4
    else:
        numDiv = 2

    totalArray = []
    for name, profile_dict in weights_dict.items():
        if name.startswith("dense"+str(int(dictLen/numDiv)-1)):
            continue
        if name.endswith("-weight"):
            array, dtype, shape = name2array(name, weights_dict)
            array = array.tolist()
            array_rank = []
            for i in range(0, shape[0]):
                array_i = array[i]
                array_i_faltten = [abs(m1) for m1 in array_i]
                array_rank.append(sum(array_i_faltten)/shape[1])
            totalArray = totalArray + array_rank
    # print(totalArray, len(totalArray))
    totalArray.sort()
    threIndex = int(len(totalArray) * args.percent)
    thre = totalArray[threIndex]
    print("threshold:", thre)
    return thre

# 获得fc剪枝方法对应的removeIndexs
def get_removeIndex_fc(a, shape, thre):
    a_rank = []
    for i in range(0, shape[0]):
        a_i = a[i]
        a_i_faltten = [abs(m1) for m1 in a_i]
        a_rank.append(sum(a_i_faltten)/shape[1])
    removeIndexs = np.where(np.array(a_rank)<thre)[0]
    return removeIndexs


# 获得bn剪枝方法对应的removeIndexs
def get_removeIndex_bn(a, thre):
    removeIndexs = np.where(a<thre)[0]
    return removeIndexs

# 获得conv_avg剪枝方法对应的removeIndexs
def get_removeIndex_conv_avg(a, shape, thre):
    a_rank = []
    for i in range(0, shape[0]):
        a_i = a[i]
        a_i_faltten = [abs(m3) for m1 in a_i for m2 in m1 for m3 in m2]
        #每一个通道的conv值的权重
        a_rank.append(sum(a_i_faltten)/(shape[1]*shape[2]*shape[3]))
    removeIndexs = np.where(np.array(a_rank)<thre)[0]
    return removeIndexs

# 获得conv_all剪枝方法对应的removeIndexs
def get_removeIndex_conv_all(a, shape, thre):
    a_rank = []
    for i in range(0, shape[0]):
        a_i = a[i]
        a_i_faltten = [abs(m3) for m1 in a_i for m2 in m1 for m3 in m2]
        #每一个通道的conv值的权重
        a_rank.append(sum(a_i_faltten))
    removeIndexs = np.where(np.array(a_rank)<thre)[0]
    return removeIndexs

# 获得conv_max剪枝方法对应的removeIndexs
def get_removeIndex_conv_max(a, shape, thre):
    a_rank = []
    for i in range(0, shape[0]):
        a_i = a[i]
        a_i_faltten = [abs(m3) for m1 in a_i for m2 in m1 for m3 in m2]
        #每一个通道的conv值的权重
        a_rank.append(max(a_i_faltten))
    removeIndexs = np.where(np.array(a_rank)<thre)[0]
    return removeIndexs

# 随机选取removeIndexs
def get_removeIndex_random(shape):
    removeIndexs = sorted(random.sample(range(shape[0]), int(shape[0]*args.percent)))
    return removeIndexs

# 获得conv_similarity剪枝方法对应的removeIndexs
def get_removeIndex_conv_similarity(a, shape):
    removeIndexs = []   
    while len(removeIndexs) <= shape[0]*args.percent:
        a_rank = []
        # 计算每一个元素和其他所有元素的相似度
        for i in range(0, shape[0]):
            # 已经移除的元素不再考虑
            if i in removeIndexs:
                continue
            a_i = a[i]
            a_i_faltten = [abs(m3) for m1 in a_i for m2 in m1 for m3 in m2]
            min_similarity = float("inf")
            for j in range(0, shape[0]):
                # 已经移除的元素不再考虑
                if j in removeIndexs+[i]:
                    continue
                a_j = a[j]
                a_j_faltten = [abs(m3) for m1 in a_j for m2 in m1 for m3 in m2]
                similarity = sum([(n1-n2)**2 for n1,n2 in zip(a_i_faltten,a_j_faltten)])
                if similarity < min_similarity:
                    min_similarity = similarity
            a_rank.append(min_similarity)
        # 选取相似度最小的添加到removeIndexs中
        removeIndexs.append(a_rank.index(min(a_rank)))
        # print(removeIndexs)
    removeIndexs = sorted(removeIndexs)
    return removeIndexs

# 获得bn_similarity剪枝方法对应的removeIndexs
def get_removeIndex_bn_similarity(a, shape):
    removeIndexs = []   
    while len(removeIndexs) <= shape[0]*args.percent:
        a_rank = []
        # 计算每一个元素和其他所有元素的相似度
        for i in range(0, shape[0]):
            # 已经移除的元素不再考虑
            if i in removeIndexs:
                continue
            a_i = a[i]
            min_similarity = float("inf")
            for j in range(0, shape[0]):
                # 已经移除的元素不再考虑
                if j in removeIndexs+[i]:
                    continue
                a_j = a[j]
                similarity = (a_i-a_j)**2
                if similarity < min_similarity:
                    min_similarity = similarity
            a_rank.append(min_similarity)
        # 选取相似度最小的添加到removeIndexs中
        removeIndexs.append(a_rank.index(min(a_rank)))
        # print(removeIndexs)
    removeIndexs = sorted(removeIndexs)
    return removeIndexs

# 获得conv_threshold剪枝方法对应的removeIndexs
# 此thre是人为设置的，不是通过thre函数得到的
def get_removeIndex_conv_threshold(a, shape, threSet):
    a_rank = []
    for i in range(0, shape[0]):
        a_i = a[i]
        a_i_faltten = [abs(m3) for m1 in a_i for m2 in m1 for m3 in m2]
        thre_sum = 0
        for n in a_i_faltten:
            if n < threSet:
                thre_sum += 1
        a_rank.append(thre_sum)
    threIndex = int(len(a_rank) * args.percent)
    thre = sorted(a_rank)[threIndex]               
    removeIndexs = np.where(np.array(a_rank)<thre)[0]
    return removeIndexs

                    
def main():
    thre = get_pruneThre_bn()
    print(thre)

if __name__ == "__main__":
    main()
