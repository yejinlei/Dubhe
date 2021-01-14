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
from util.model_weights import modelWeight
import util.prune_algorithm as pa


parser = argparse.ArgumentParser()
dtype_dict={2:np.float32,
            3:np.float64,
            4:np.int8,
            5:np.int32,
            6:np.int64,
            9:np.float16}

parser.add_argument("--bn", default=False,
                    type=str, help="Whether to use use bn layer")
parser.add_argument("--prune_method", default='bn', type=str, 
                    help="method of prune(bn, conv_avg, random...)")
parser.add_argument("--model_load_dir", default = './output/snapshots/model_base/snapshot_last',
                    type = str, required = False, help = "Path of base oneflow model")
parser.add_argument("--model_save_dir", default = './output/snapshots/model_prune', type = str,
                    required = False, help = "Path to the output OneFlow model.")
parser.add_argument("--percent", default = 0.7, type = float, required = False,
                    help = "scale sparse rate (default: 0.7)")
parser.add_argument("--optimizer", type=str, default="momentum", required=False, 
                    help="sgd, adam, momentum")
args = parser.parse_args()

def _SaveWeightBlob2File(blob, folder, var):
    if not os.path.exists(folder):
        os.makedirs(folder)
    filename = os.path.join(folder, var)

    f = open(filename, 'wb')
    f.write(blob.tobytes())
    f.close()

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

#制作待剪枝的namelist
def makeNameList(pruneName, nameList, name):
    if pruneName == '_bn-gamma':
        nameList.append(name+"_weight")
    elif pruneName == "_weight":
        if args.bn.lower() in ('yes', 'true', 't', 'y', '1'):
            nameList.append(name+"_bn-gamma")
    nameList.append(name+pruneName)
    nameList.append(name+"_bias")
    #是否添加对应bn层参数
    if args.bn.lower() in ('yes', 'true', 't', 'y', '1'):
        nameList.append(name+"_bn-beta")
        nameList.append(name+"_bn-moving_variance")
        nameList.append(name+"_bn-moving_mean")
    #adam时多加的参数
    if args.optimizer == 'adam':
        nameList.append(name+"_weight-v")
        nameList.append(name+"_weight-m")
        nameList.append(name+"_bias-v")
        nameList.append(name+"_bias-m")
        #是否添加adam时对应bn层参数           
        if args.bn.lower() in ('yes', 'true', 't', 'y', '1'):
            nameList.append(name+"_bn-beta-v")
            nameList.append(name+"_bn-beta-m")
            nameList.append(name+"_bn-gamma-v")
            nameList.append(name+"_bn-gamma-m")
    #momentum时多加的参数
    elif args.optimizer == 'momentum':
        nameList.append(name+"_weight-momentum")
        nameList.append(name+"_bias-momentum")
        #是否添加momentum时对应bn层参数
        if args.bn.lower() in ('yes', 'true', 't', 'y', '1'):
            nameList.append(name+"_bn-beta-momentum")
            nameList.append(name+"_bn-gamma-momentum")
    else:
        if args.optimizer != 'sgd':
            print('Error: optimizer!')
    return nameList
    
def prune():
    # 获取对应剪枝方法的thre阈值
    if args.prune_method == 'bn':
        thre = pa.get_pruneThre_bn()
    elif args.prune_method == 'conv_avg':
        thre = pa.get_pruneThre_conv_avg()
    elif args.prune_method == 'conv_all':
        thre = pa.get_pruneThre_conv_all()
    elif args.prune_method == 'conv_max':
        thre = pa.get_pruneThre_conv_max()
    
    of_weight_path = args.model_load_dir.rsplit("/",1)[0] + "/weights_profile_path"
    weights_dict = modelWeight.load(of_weight_path)
    
    modelWeight.weights_dict = {}
    
    fcRemoveIndexs = []
    fcDivideNum = 0
    removeIndexs = []
    lastRemoveIndexs = []
    beforePrune = 0
    afterPrune = 0
    pruneName = ''
    
    if "bn" in args.prune_method:
        pruneName = "_bn-gamma"
    elif "conv" in args.prune_method or args.prune_method=="random":
        pruneName = "_weight"
    
    for name, profile_dict in weights_dict.items():
        if name.startswith("conv") and name.endswith(pruneName):
            a, dtype, shape = name2array(name, weights_dict)
            lastRemoveIndexs = removeIndexs
            #获取对应剪枝方法removeIndexs
            if args.prune_method == 'bn':
                removeIndexs = pa.get_removeIndex_bn(a, thre)
            elif args.prune_method == "conv_avg":
                removeIndexs = pa.get_removeIndex_conv_avg(a, shape, thre)
            elif args.prune_method == "conv_all":
                removeIndexs = pa.get_removeIndex_conv_all(a, shape, thre)
            elif args.prune_method == "conv_max":
                removeIndexs = pa.get_removeIndex_conv_max(a, shape, thre)
            elif args.prune_method == "random":
                removeIndexs = pa.get_removeIndex_random(shape)
            elif args.prune_method == "conv_similarity":
                removeIndexs = pa.get_removeIndex_conv_similarity(a, shape)
            elif args.prune_method == "bn_similarity":
                removeIndexs = pa.get_removeIndex_bn_similarity(a, shape)
            elif args.prune_method == "conv_threshold":
                removeIndexs = pa.get_removeIndex_conv_threshold(a, shape, threSet=0.06)
                     
            # print(removeIndexs)
            
            if len(removeIndexs) == len(a):
                removeIndexs = np.delete(removeIndexs, 0)
            
            if name == "conv12"+pruneName:
                fcRemoveIndexs = removeIndexs
                fcDivideNum = 512

            #待剪枝层的名字列表
            name = name.split("_")[0].split("-")[0]
            nameList = []
            nameList = makeNameList(pruneName, nameList, name)

            #真正剪枝
            for name in nameList:
                a, dtype, shape = name2array(name, weights_dict)
                if name.endswith("weight") or name.endswith("weight-v") or \
                   name.endswith("weight-m") or name.endswith("weight-momentum"):
                    b = np.delete(a, removeIndexs, 0)
                    b = np.delete(b, lastRemoveIndexs, 1)
                    if name.endswith("weight"):
                        beforePrune += a.shape[0]
                        afterPrune += b.shape[0]
                        
                else:
                    b = np.delete(a, removeIndexs)
                
                print(name+" pruned: shape from", a.shape, "-->", b.shape)
                if args.model_save_dir:
                    folder = os.path.join(args.model_save_dir, "model", name)
                    _SaveWeightBlob2File(b, folder, 'out')
                modelWeight.add(name, list(dtype_dict.keys())[list(dtype_dict.values()).index(dtype)], b.shape)
    
        #第一个dense0层剪枝
        elif name.startswith("dense"):
            if name in ['dense0-weight', 'dense0-weight-v',
                        'dense0-weight-m', 'dense0-weight-momentum']:
                fcRemoveIndexsNew = []
                a, dtype, shape = name2array(name, weights_dict)
                num = int(a.shape[1]/fcDivideNum)
                
                for index in fcRemoveIndexs:
                    fcRemoveIndexsNew += [index+fcDivideNum*i for i in range(num)]
                b = np.delete(a, fcRemoveIndexsNew, 1)
            else:
                a, dtype, shape = name2array(name, weights_dict)
                b = a
            print(name+" pruned: shape from", a.shape, "-->", b.shape)
            if args.model_save_dir:
                folder = os.path.join(args.model_save_dir, "model", name)
                _SaveWeightBlob2File(b, folder, 'out')
            modelWeight.add(name, list(dtype_dict.keys())[list(dtype_dict.values()).index(dtype)], b.shape)
    
    print("Pruning done! Number of channel from", beforePrune, "-->", afterPrune)
    print("Real Pruning rate:", 100*(beforePrune-afterPrune)/beforePrune, "%")
    weights_profile_path = os.path.join(args.model_save_dir, "weights_profile_path")
    modelWeight.save(weights_profile_path)
    os.system('cp -r {0}/System-Train-TrainStep-TrainNet {1}/System-Train-TrainStep-TrainNet '.format(args.model_load_dir, os.path.join(args.model_save_dir, "model")))
                    
def main():
    prune()

if __name__ == "__main__":
    main()
