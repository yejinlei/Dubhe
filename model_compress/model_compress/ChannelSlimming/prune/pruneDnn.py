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
def makeNameList(nameList, name):
    nameList.append(name+"-weight")
    nameList.append(name+"-bias")
    #adam时多加的参数
    if args.optimizer == 'adam':
        nameList.append(name+"-weight-v")
        nameList.append(name+"-weight-m")
        nameList.append(name+"-bias-v")
        nameList.append(name+"-bias-m")
    #momentum时多加的参数
    elif args.optimizer == 'momentum':
        nameList.append(name+"-weight-momentum")
        nameList.append(name+"-bias-momentum")
    else:
        if args.optimizer != 'sgd':
            print('Error: optimizer!')
    return nameList
    
def prune():
    #获的剪枝的阈值
    thre = pa.get_pruneThre_fc()
    of_weight_path = args.model_load_dir.rsplit("/",1)[0] + "/weights_profile_path"
    weights_dict = modelWeight.load(of_weight_path)
    
    modelWeight.weights_dict = {}

    removeIndexs = []
    lastRemoveIndexs = []
    beforePrune = 0
    afterPrune = 0
    dictLen = len(weights_dict)
    numDiv = 0
    if args.optimizer == 'adam':
        numDiv = 6
    elif args.optimizer == 'momentum':
        numDiv = 4
    else:
        numDiv = 2
    
    for name, profile_dict in weights_dict.items():
        if name.startswith("dense") and name.endswith("-weight"):
            if name.startswith("dense"+str(int(dictLen/numDiv)-1)) and name.endswith("-weight"):
                lastRemoveIndexs = removeIndexs
                removeIndexs = []
            else:
                a, dtype, shape = name2array(name, weights_dict)
                lastRemoveIndexs = removeIndexs
                #获取对应剪枝方法removeIndexs
                removeIndexs = pa.get_removeIndex_fc(a, shape, thre)
            
                if len(removeIndexs) == len(a):
                    removeIndexs = np.delete(removeIndexs, 0)

            #待剪枝层的名字列表
            name = name.split("_")[0].split("-")[0]
            nameList = []
            nameList = makeNameList(nameList, name)

            #真正剪枝
            i = 0
            for name in nameList:   
                a, dtype, shape = name2array(name, weights_dict)                  
                if "weight" in name:
                    b = np.delete(a, removeIndexs, 0)
                    b = np.delete(b, lastRemoveIndexs, 1) 
                else:
                    b = np.delete(a, removeIndexs)
                if i == 0:
                    beforePrune += a.shape[0]
                    afterPrune += b.shape[0]
                print(name+" pruned: shape from", a.shape, "-->", b.shape)
                if args.model_save_dir:
                    folder = os.path.join(args.model_save_dir, "model", name)
                    _SaveWeightBlob2File(b, folder, 'out')
                modelWeight.add(name, list(dtype_dict.keys())[list(dtype_dict.values()).index(dtype)], b.shape)
                i += 1
    
    print("Pruning done! Number of channel from", beforePrune, "-->", afterPrune)
    print("Real Pruning rate:", 100*(beforePrune-afterPrune)/beforePrune, "%")
    weights_profile_path = os.path.join(args.model_save_dir, "weights_profile_path")
    modelWeight.save(weights_profile_path)
    os.system('cp -r {0}/System-Train-TrainStep-TrainNet {1}/System-Train-TrainStep-TrainNet '.format(args.model_load_dir, os.path.join(args.model_save_dir, "model")))
                    
def main():
    prune()

if __name__ == "__main__":
    main()
