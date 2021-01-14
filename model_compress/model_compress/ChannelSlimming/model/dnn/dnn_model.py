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
import oneflow as flow
from util.model_weights import modelWeight

# 这是一个具有 2 层隐藏层的 DNN 神经网络，第 1 层使用 relu 激活函数，第 2 层不使用激活函数

def dnn_2(input_tensor, cfg, optimizer, model_weight=True, trainable=True):
    input_tensor = flow.reshape(input_tensor, [input_tensor.shape[0], -1])
    dense0 = flow.layers.dense(
        inputs=input_tensor,
        units=cfg[0],
        activation=flow.nn.relu, use_bias=True,
        kernel_initializer=flow.random_normal_initializer(mean=0, stddev=0.1),
        trainable=trainable,
        name="dense0")

    dense1 = flow.layers.dense(
        inputs=dense0,
        units=cfg[1],
        activation=None,
        use_bias=True,
        kernel_initializer=flow.random_normal_initializer(mean=0, stddev=0.1),
        trainable=trainable,
        name="dense1")
    
    def getTypeAndShape(inputs,units):
        in_shape = inputs.shape
        in_num_axes = len(in_shape)
        inputs = (flow.reshape(inputs, (-1, in_shape[-1])) if in_num_axes > 2 else inputs)
        shape=(units, inputs.shape[1])
        dtype=inputs.dtype
        return shape,dtype
    
    if model_weight == True:
        shape_list = []
        dtype_list = []
        shape_weight, dtype = getTypeAndShape(input_tensor, cfg[0])
        shape_list.append(shape_weight)
        dtype_list.append(dtype)
        shape_weight, dtype = getTypeAndShape(dense0, cfg[1])
        shape_list.append(shape_weight)
        dtype_list.append(dtype)
        modelWeight.addDense(dtype_old=dtype_list, shape=shape_list,
                             optimizer=optimizer, dense_num=2)
    return dense1
        
def dnn_4(input_tensor, cfg, optimizer, model_weight=True, trainable=True):
    input_tensor = flow.reshape(input_tensor, [input_tensor.shape[0], -1])
    dense0 = flow.layers.dense(
        inputs=input_tensor,
        units=cfg[0],
        activation=flow.nn.relu, use_bias=True,
        kernel_initializer=flow.random_normal_initializer(mean=0, stddev=0.1),
        trainable=trainable,
        name="dense0")
    
    dense1 = flow.layers.dense(
        inputs=dense0,
        units=cfg[1],
        activation=flow.nn.relu, use_bias=True,
        kernel_initializer=flow.random_normal_initializer(mean=0, stddev=0.1),
        trainable=trainable,
        name="dense1")
    
    dense2 = flow.layers.dense(
        inputs=dense1,
        units=cfg[2],
        activation=flow.nn.relu, use_bias=True,
        kernel_initializer=flow.random_normal_initializer(mean=0, stddev=0.1),
        trainable=trainable,
        name="dense2")

    dense3 = flow.layers.dense(
        inputs=dense2,
        units=cfg[3],
        activation=None,
        use_bias=True,
        kernel_initializer=flow.random_normal_initializer(mean=0, stddev=0.1),
        trainable=trainable,
        name="dense3")
    
    def getTypeAndShape(inputs,units):
        in_shape = inputs.shape
        in_num_axes = len(in_shape)
        inputs = (flow.reshape(inputs, (-1, in_shape[-1])) if in_num_axes > 2 else inputs)
        shape=(units, inputs.shape[1])
        dtype=inputs.dtype
        return shape,dtype
    
    if model_weight == True:
        shape_list = []
        dtype_list = []
        shape_weight, dtype = getTypeAndShape(input_tensor, cfg[0])
        shape_list.append(shape_weight)
        dtype_list.append(dtype)
        shape_weight, dtype = getTypeAndShape(dense0, cfg[1])
        shape_list.append(shape_weight)
        dtype_list.append(dtype)
        shape_weight, dtype = getTypeAndShape(dense1, cfg[2])
        shape_list.append(shape_weight)
        dtype_list.append(dtype)
        shape_weight, dtype = getTypeAndShape(dense2, cfg[3])
        shape_list.append(shape_weight)
        dtype_list.append(dtype)
        modelWeight.addDense(dtype_old=dtype_list, shape=shape_list,
                             optimizer=optimizer, dense_num=4)
    return dense3
