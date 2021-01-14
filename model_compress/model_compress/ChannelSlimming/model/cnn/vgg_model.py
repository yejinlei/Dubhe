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
    kernel_size=3,
    strides=1,
    padding="SAME",
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
#               flow.watch(output)
                output = flow.nn.relu(output)
            else:
                output = flow.nn.relu(output)		
        else:
            raise NotImplementedError

    return output


def _conv_block(in_blob, index, filters, conv_times, optimizer, model_weight, bn=True):
    conv_block = []
    conv_block.insert(0, in_blob)
    for i in range(conv_times):
        conv_i = conv2d_layer(
            name="conv{}".format(index),
            input=conv_block[i],
            filters=filters[index],
            kernel_size=3,
            strides=1,
            bn=bn,
        )
        if model_weight == True:
            modelWeight.addConv(index=index,
                                dtype=conv_block[i].dtype,
                                shape1=(filters[index], conv_block[i].shape[1], 3, 3),
                                shape2=(filters[index],),
                                optimizer=optimizer)
#            shape_weight=(filters[index], conv_block[i].shape[1], 3, 3)
#            modelWeight.add("conv{}".format(index)+'-weight',conv_block[i].dtype,shape_weight)
#            modelWeight.add("conv{}".format(index)+'-bias',conv_block[i].dtype,(filters,))
#            modelWeight.add("conv{}".format(index)+'_bn-gamma',conv_block[i].dtype,(filters,))
#            modelWeight.add("conv{}".format(index)+'_bn-beta',conv_block[i].dtype,(filters,))
#            modelWeight.add("conv{}".format(index)+'_bn-moving_variance',conv_block[i].dtype,(filters,))
#            modelWeight.add("conv{}".format(index)+'_bn-moving_mean',conv_block[i].dtype,(filters,))
        conv_block.append(conv_i)
        index += 1

    return conv_block


def vgg(images, cfg, optimizer, trainable=True, need_transpose=False, 
        training=True, wd=1.0/32768, model_weight=True, bn=True):
    if need_transpose:
        images = flow.transpose(images, name="transpose", perm=[0, 3, 1, 2])
    conv1 = _conv_block(images, 0, cfg, 2, optimizer, model_weight, bn=bn)
    pool1 = flow.nn.max_pool2d(conv1[-1], 2, 2, "VALID", "NCHW", name="pool1")
    
    conv2 = _conv_block(pool1, 2, cfg, 2, optimizer, model_weight, bn=bn)
    pool2 = flow.nn.max_pool2d(conv2[-1], 2, 2, "VALID", "NCHW", name="pool2")

    conv3 = _conv_block(pool2, 4, cfg, 3, optimizer, model_weight, bn=bn)
    pool3 = flow.nn.max_pool2d(conv3[-1], 2, 2, "VALID", "NCHW", name="pool3")

    conv4 = _conv_block(pool3, 7, cfg, 3, optimizer, model_weight, bn=bn)
    pool4 = flow.nn.max_pool2d(conv4[-1], 2, 2, "VALID", "NCHW", name="pool4")

    conv5 = _conv_block(pool4, 10, cfg, 3, optimizer, model_weight, bn=bn)
    pool5 = flow.nn.max_pool2d(conv5[-1], 2, 2, "VALID", "NCHW", name="pool5")

    pool5 = flow.reshape(pool5, [pool5.shape[0], -1])
    dense0 = flow.layers.dense(
        inputs=pool5,
        units=cfg[13],
        activation=flow.nn.relu,
        use_bias=True,
        kernel_initializer=flow.random_normal_initializer(mean=0, stddev=0.1),
        trainable=trainable,
        name="dense0",
    )

    dense1 = flow.layers.dense(
        inputs=dense0,
        units=cfg[14],
        activation=flow.nn.relu,
        use_bias=True,
        kernel_initializer=flow.random_normal_initializer(mean=0, stddev=0.1),
        trainable=trainable,
        name="dense1",
    )
    
    dense2 = flow.layers.dense(
        inputs=dense1,
        units=cfg[15],
        use_bias=True,
        kernel_initializer=flow.random_normal_initializer(mean=0, stddev=0.1),
        trainable=trainable,
        name="dense2",
    )
#    flow.watch(fc8)
    
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
        shape_weight, dtype = getTypeAndShape(pool5, cfg[13])
        shape_list.append(shape_weight)
        dtype_list.append(dtype)
        shape_weight, dtype = getTypeAndShape(dense0, cfg[14])
        shape_list.append(shape_weight)
        dtype_list.append(dtype)
        shape_weight, dtype = getTypeAndShape(dense1, cfg[15])
        shape_list.append(shape_weight)
        dtype_list.append(dtype)
        modelWeight.addDense(dtype_old=dtype_list, shape=shape_list,
                             optimizer=optimizer, dense_num=3)


    # shape_weight,dtype=getTypeAndShape(pool5,4096)
    # modelWeight.add('fc1'+'-weight',dtype,shape_weight)
    # modelWeight.add('fc1'+'-bias',dtype,(4096,))

    # shape_weight,dtype=getTypeAndShape(fc6,4096)
    # modelWeight.add('fc2'+'-weight',dtype,shape_weight)
    # modelWeight.add('fc2'+'-bias',dtype,(4096,))

    # shape_weight,dtype=getTypeAndShape(fc7,1000)
    # modelWeight.add('fc_final'+'-weight',dtype,shape_weight)
    # modelWeight.add('fc_final'+'-bias',dtype,(1000,))

    return dense2
