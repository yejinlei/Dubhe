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

import oneflow as flow
from util.model_weights import modelWeight

def _batch_norm(inputs, name=None, trainable=True):
    return flow.layers.batch_normalization(
        inputs=inputs,
        axis=1,
        momentum=0.997,
        epsilon=1.001e-5,
        center=True,
        scale=True,
#        gamma_initializer=0,
        gamma_regularizer=flow.regularizers.l1(1e-4),
        trainable=trainable,
        name=name,
    )


def conv2d_layer(
    name,
    input,
    filters,
    kernel_size=1,
    strides=1,
    padding="VALID",
    data_format="NCHW",
    dilation_rate=1,
    activation="Relu",
    use_bias=True,
    weight_initializer=flow.variance_scaling_initializer(2, 'fan_out', 'random_normal', data_format="NCHW"),
    bias_initializer=flow.zeros_initializer(),
    bn=True,
):
    weight_shape = (filters, input.shape[1], kernel_size, kernel_size)
    weight = flow.get_variable(
        name + "_weight",
        shape=weight_shape,
        dtype=input.dtype,
        initializer=weight_initializer,
    )
    output = flow.nn.conv2d(
        input, weight, strides, padding, data_format, dilation_rate, name=name
    )
    if use_bias:
        bias = flow.get_variable(
            name + "_bias",
            shape=(filters,),
            dtype=input.dtype,
            initializer=bias_initializer,
        )
        output = flow.nn.bias_add(output, bias, data_format)

    if activation is not None:
        if activation == "Relu":
            if bn:
                output = _batch_norm(output, name + "_bn")
                output = flow.nn.relu(output)
            else:
                output = flow.nn.relu(output)		
        else:
            raise NotImplementedError

    return output


def lenet(images, cfg, optimizer, trainable=True, need_transpose=False, 
          training=True, wd=1.0/32768, model_weight=True, bn=True):
    if need_transpose:
        images = flow.transpose(images, name="transpose", perm=[0, 3, 1, 2])
    conv0 = conv2d_layer(name="conv0", input=images, filters=cfg[0], kernel_size=5,
                         padding="VALID", strides=1, bn=bn)
    pool0 = flow.nn.max_pool2d(conv0, 2, 2, "VALID", "NCHW", name="pool0")

    conv1 = conv2d_layer(name="conv1", input=pool0, filters=cfg[1], kernel_size=5,
                         padding="VALID", strides=1, bn=bn)    
    pool1 = flow.nn.max_pool2d(conv1, 2, 2, "VALID", "NCHW", name="pool1")
       
    pool1 = flow.reshape(pool1, [pool1.shape[0], -1])
    # pool1 = flow.reshape(images, [images.shape[0], -1])
    dense0 = flow.layers.dense(
        inputs=pool1,
        units=cfg[2],
        activation=flow.nn.relu,
        use_bias=True,
        kernel_initializer=flow.random_normal_initializer(mean=0, stddev=0.1),
        trainable=trainable,
        name="dense0")

    dense1 = flow.layers.dense(
        inputs=dense0,
        units=cfg[3],
        activation=flow.nn.relu,
        use_bias=True,
        kernel_initializer=flow.random_normal_initializer(mean=0, stddev=0.1),
        trainable=trainable,
        name="dense1")
    
    dense2 = flow.layers.dense(
        inputs=dense1,
        units=cfg[4],
        use_bias=True,
        kernel_initializer=flow.random_normal_initializer(mean=0, stddev=0.1),
        trainable=trainable,
        name="dense2")
#    flow.watch(fc8)
    
    def getTypeAndShape(inputs,units):
        in_shape = inputs.shape
        in_num_axes = len(in_shape)
        inputs = (flow.reshape(inputs, (-1, in_shape[-1])) if in_num_axes > 2 else inputs)
        shape=(units, inputs.shape[1])
        dtype=inputs.dtype
        return shape,dtype
    
    if model_weight == True:
        modelWeight.addConv(index=0, dtype=conv0.dtype,
                            shape1=(cfg[0], images.shape[1], 5, 5), shape2=(cfg[0],),
                            optimizer=optimizer)
        modelWeight.addConv(index=1, dtype=conv1.dtype,
                            shape1=(cfg[1], conv0.shape[1], 5, 5), shape2=(cfg[1],),
                            optimizer=optimizer)      
        shape_list = []
        dtype_list = []
        shape_weight, dtype = getTypeAndShape(pool1, cfg[2])
        shape_list.append(shape_weight)
        dtype_list.append(dtype)
        shape_weight, dtype = getTypeAndShape(dense0, cfg[3])
        shape_list.append(shape_weight)
        dtype_list.append(dtype)
        shape_weight, dtype = getTypeAndShape(dense1, cfg[4])
        shape_list.append(shape_weight)
        dtype_list.append(dtype)
        modelWeight.addDense(dtype_old=dtype_list, shape=shape_list,
                             optimizer=optimizer, dense_num=3)

    return dense2
