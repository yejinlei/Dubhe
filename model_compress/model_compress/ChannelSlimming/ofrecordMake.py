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
import oneflow.core.record.record_pb2 as ofrecord
import six
import struct
import numpy as np
import json
import os
import argparse

parser = argparse.ArgumentParser()

parser.add_argument("--dataName", default="randomData1",
                    type=str, help="my data name")

args = parser.parse_args()

#%%
def int32_feature(value):
    if not isinstance(value, (list, tuple)):
        value = [value]
    return ofrecord.Feature(int32_list=ofrecord.Int32List(value=value))

def int64_feature(value):
    if not isinstance(value, (list, tuple)):
        value = [value]
    return ofrecord.Feature(int64_list=ofrecord.Int64List(value=value))


def float_feature(value):
    if not isinstance(value, (list, tuple)):
        value = [value]
    return ofrecord.Feature(float_list=ofrecord.FloatList(value=value))


def double_feature(value):
    if not isinstance(value, (list, tuple)):
        value = [value]
    return ofrecord.Feature(double_list=ofrecord.DoubleList(value=value))


def bytes_feature(value):
    if not isinstance(value, (list, tuple)):
        value = [value]
    if not six.PY2:
        if isinstance(value[0], str):
            value = [x.encode() for x in value]
    return ofrecord.Feature(bytes_list=ofrecord.BytesList(value=value))

#%% 随机生成3*32*32大小的训练集1000个，测试集200个，数值范围0-1
def createRandomData_1():
    data_train = np.random.random((1000, 3*32*32))
    label_train = np.random.randint(0, 10, (1000))
    np.around(data_train, 4)
    dict_train = {}
    dict_train["data"] = data_train.tolist()
    dict_train["label"] = label_train.tolist()
    dict_train["shape"] = [3, 32, 32]
    with open("./myData/randomData1/train.json", "w") as f_train:
        json.dump(dict_train, f_train, indent=4)
    data_test = np.random.random((200, 3*32*32))
    label_test = np.random.randint(0, 10, (200))
    np.around(data_test, 4)
    dict_test = {}
    dict_test["data"] = data_test.tolist()
    dict_test["label"] = label_test.tolist()
    dict_test["shape"] = [3, 32, 32]
    with open("./myData/randomData1/test.json", "w") as f_test:
        json.dump(dict_test, f_test, indent=4)
        
  #%% 随机生成3*32*32大小的训练集1000个，测试集200个，数值范围1-255  
def createRandomData_255():
    data_train = np.random.randint(1, 255, (1000, 3*32*32))
    label_train = np.random.randint(0, 10, (1000))
    np.around(data_train, 4)
    dict_train = {}
    dict_train["data"] = data_train.tolist()
    dict_train["label"] = label_train.tolist()
    dict_train["shape"] = [3, 32, 32]
    with open("./myData/randomData255_small/train.json", "w") as f_train:
        json.dump(dict_train, f_train, indent=4)
    data_test = np.random.randint(1, 255, (200, 3*32*32))
    label_test = np.random.randint(0, 10, (200))
    np.around(data_test, 4)
    dict_test = {}
    dict_test["data"] = data_test.tolist()
    dict_test["label"] = label_test.tolist()
    dict_test["shape"] = [3, 32, 32]
    with open("./myData/randomData255_small/test.json", "w") as f_test:
        json.dump(dict_test, f_test, indent=4)

#%% cal mean, std
def mean_std(data, shape):
    data_reshape = data.reshape(-1, shape[0], shape[1], shape[2])
    mean_list,std_list = [],[]
    for i in range(shape[0]):
        mean = np.mean(data_reshape[:,i,:,:])
        std = np.std(data_reshape[:,i,:,:])
        if mean <= 1:
            mean_list.append(np.around(mean*255, 2))
            std_list.append(np.around(std*255, 2))
        else:
            mean_list.append(np.around(mean, 2))
            std_list.append(np.around(std, 2))
    return mean_list, std_list

#%% data转ofData
def data2of_part(datas, labels, save_path):
    f = open(save_path, "wb")
    for loop in range(0, len(labels)):
        image = datas[loop].tolist()
        label = [labels[loop]]

        topack = {
            'images': float_feature(image),
            'labels': int32_feature(label),
        }

        ofrecord_features = ofrecord.OFRecord(feature=topack)
        serilizedBytes = ofrecord_features.SerializeToString()

        length = ofrecord_features.ByteSize()

        f.write(struct.pack("q", length))
        f.write(serilizedBytes)

    print("Write ofData to", save_path)
    f.close()

#%% load mydata and write ofData
def data2of(dataName):
    # load/save path
    load_path_train = "./myData/" + dataName + "/train.json"
    load_path_test = "./myData/" + dataName + "/test.json"
    save_path_train = "./ofData/" + dataName + "/train/"
    save_path_test = "./ofData/" + dataName + "/test/"
    if not os.path.exists(save_path_train):
        os.makedirs(save_path_train)
        print("create folder", save_path_train)
    if not os.path.exists(save_path_test):
        os.makedirs(save_path_test)
        print("create folder", save_path_test)
    
    # load data
    with open(load_path_train) as f_train:
        train_dict = json.load(f_train)
    with open(load_path_test) as f_test:
        test_dict = json.load(f_test)
    data_train = np.array(train_dict["data"])
    label_train = np.array(train_dict["label"])
    data_test = np.array(test_dict["data"])
    label_test = np.array(test_dict["label"])
    data = np.append(data_train, data_test, axis=0)
    label = np.append(label_train, label_test)
    
    # data 2 ofData
    data2of_part(data_train, label_train, save_path_train+"part-00000")
    data2of_part(data_test, label_test, save_path_test+"part-00000")
    
    # write meta information
    shape = train_dict["shape"]
    mean_list, std_list = mean_std(data, shape)
    dict_meta = {}
    dict_meta["num_classes"] = len(set(label))
    dict_meta["image_shape"] = shape
    dict_meta["rgb_mean"] = mean_list
    dict_meta["rgb_std"] = std_list
    dict_meta["num_examples"] = data_train.shape[0]
    dict_meta["num_val_examples"] = data_test.shape[0]
    with open("./ofData/" + dataName + "/meta.json", "w") as f_meta:
        json.dump(dict_meta, f_meta, indent=4)
    print("Write meta infomation to", "./ofData/" + dataName + "/meta.json")
    


def main():
    # load_path = "./myData/data_batch_1"
    # d = unpickle_cifar(load_path)
    # data = d[b'data']
    # print(type(data))
    # labels = d[b'labels']
    # print(data.shape)
    
    # createRandomData_1()
    # createRandomData_255()
    
    dataName = args.dataName
    data2of(dataName)

    
if __name__ == "__main__":
    main()
