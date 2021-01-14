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
parser.add_argument("--prune_method", default='bn',
                    type=str, help="method of prune(channel_prune_bn, channel_prune_conv)")
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
        if args.bn.lower() in ('yes', 'true', 't', 'y', '1'):
            nameList.append(name+"_bn-beta-v")
            nameList.append(name+"_bn-beta-m")
            nameList.append(name+"_bn-gamma-v")
            nameList.append(name+"_bn-gamma-m")
    #momentum时多加的参数
    elif args.optimizer == 'momentum':
        nameList.append(name+"_weight-momentum")
        nameList.append(name+"_bias-momentum")
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
    lastRemoveIndexs_shortcut = []
    beforePrune = 0
    afterPrune = 0
    pruneName = ''
    
    if "bn" in args.prune_method:
        pruneName = "_bn-gamma"
    elif "conv" in args.prune_method or args.prune_method=="random":
        pruneName = "_weight"
    
    for name, profile_dict in weights_dict.items():
        if name.startswith("conv") and name.endswith(pruneName) and \
        "stem" not in name and "shortcut" not in name:
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
            
            if len(removeIndexs) == len(a):
                removeIndexs = np.delete(removeIndexs, 0)
          
            if name == "conv47"+pruneName:
                fcRemoveIndexs = removeIndexs
                fcDivideNum = 2048

            #待剪枝层的名字列表
            name = name.split("_")[0].split("-")[0]
            nameList = []
            nameList = makeNameList(pruneName, nameList, name)

            #除了shortcut层的真正剪枝
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
            
            #resnet模型剪枝shortcut
            #addName是shortcut层的数字后缀
            addName = ""
            #获取conv层name中的编号数字
            n = int(name.split("_")[0].split("-")[0].replace("conv", ""))
            if (n+1)%3 == 0:
                n = int((n+1)/3)
                if n <= 3:
                    addName = "0_" + str(n-1)
                elif n <= 7:
                    addName = "1_" + str(n-4)
                elif n <= 13:
                    addName = "2_" + str(n-8)
                elif n <= 16:
                    addName = "3_" + str(n-14)
                name = "conv_shortcut" + addName
                #shortcut的conv层待剪枝层的名字列表
                #nameList_shortcut是裁剪所有的名字列表
                nameList_shortcut = []
                nameList_shortcut = makeNameList(pruneName, nameList_shortcut, name)             
                
                #resnet模型的shortcut真正剪枝
                for name in nameList_shortcut:
                    a, dtype, shape = name2array(name, weights_dict)
                    if name.endswith("weight") or name.endswith("weight-v") or \
                        name.endswith("weight-m") or name.endswith("weight-momentum"):
                            b = np.delete(a, removeIndexs, 0)
                            b = np.delete(b, lastRemoveIndexs_shortcut, 1)                       
                    else:
                        b = np.delete(a, removeIndexs)
                    print(name+" pruned: shape from", a.shape, "-->", b.shape)
                    if args.model_save_dir:
                        folder = os.path.join(args.model_save_dir, "model", name)
                        _SaveWeightBlob2File(b, folder, 'out')
                    modelWeight.add(name, list(dtype_dict.keys())[list(dtype_dict.values()).index(dtype)], b.shape)
                lastRemoveIndexs_shortcut = removeIndexs
                
        #复制stem层             
        elif "stem" in name:
            a, dtype, shape = name2array(name, weights_dict)
            b = a
            print(name+" copy")
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
