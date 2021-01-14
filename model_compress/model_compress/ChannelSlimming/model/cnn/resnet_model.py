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

BLOCK_COUNTS = [3, 4, 6, 3]
NAME_NUMBER = 0

#一个conv层
def _conv2d(name,
            input,
            filters,
            kernel_size,
            strides=1,
            padding="SAME",
            data_format="NCHW",
            dilations=1,
            use_bias=True,
            trainable=True,
            weight_initializer=flow.variance_scaling_initializer(data_format="NCHW"),
            bias_initializer=flow.zeros_initializer()):
    
    weight = flow.get_variable(name + "_weight",
                               shape=(filters, input.shape[1], kernel_size, kernel_size),
                                      dtype=input.dtype,
                                      initializer=weight_initializer,
                                      trainable=trainable)
    output = flow.nn.conv2d(input, weight, strides, padding, data_format, dilations, name=name)
    
    if use_bias:
        bias = flow.get_variable(name + "_bias",
                                 shape=(filters,),
                                 dtype=input.dtype,
                                 initializer=bias_initializer,)
        output = flow.nn.bias_add(output, bias, data_format)
    
    return output

#一个bn层
def _batch_norm(inputs, name=None, trainable=True):
    return flow.layers.batch_normalization(
        inputs=inputs,
        axis=1,
        momentum=0.997,
        epsilon=1.001e-5,
        center=True,
        scale=True,
        trainable=trainable,
        name=name,
    )

#conv, bn, relu层
def conv2d_affine(input, name, filters, kernel_size, strides, bn, activation=None):
    # input data_format must be NCHW, cannot check now
    padding = "SAME" if strides > 1 or kernel_size > 1 else "VALID"
    output = _conv2d(name, input, filters, kernel_size, strides, padding)
#    print(name)
    if bn:
        output = _batch_norm(output, name + "_bn")
    if activation == "Relu":
        output = flow.nn.relu(output)

    return output

#三个conv2d_affine(conv, bn, relu层)
def bottleneck_transformation(input, filter1, filter2, filter3,
                              strides, bn, model_weight, optimizer):
    global NAME_NUMBER
    a = conv2d_affine(input, "conv"+str(NAME_NUMBER), filter1, 1, 1, bn, activation="Relu",)
    #添加conv的model weight
    if model_weight == True:
        modelWeight.addConv(index=NAME_NUMBER,
                            dtype=input.dtype,
                            shape1=(filter1, input.shape[1], 1, 1),
                            shape2=(filter1,),
                            optimizer=optimizer)
    NAME_NUMBER += 1
    
    b = conv2d_affine(a, "conv"+str(NAME_NUMBER), filter2, 3, strides, bn, activation="Relu",)
    #添加conv的model weight
    if model_weight == True:
        modelWeight.addConv(index=NAME_NUMBER,
                            dtype=a.dtype,
                            shape1=(filter2, a.shape[1], 3, 3),
                            shape2=(filter2,),
                            optimizer=optimizer)
    NAME_NUMBER += 1
    
    c = conv2d_affine(b, "conv"+str(NAME_NUMBER), filter3, 1, 1, bn)
    #添加conv的model weight
    if model_weight == True:
        modelWeight.addConv(index=NAME_NUMBER,
                            dtype=b.dtype,
                            shape1=(filter3, b.shape[1], 1, 1),
                            shape2=(filter3,),
                            optimizer=optimizer)
    NAME_NUMBER += 1
#    print(a.shape, b.shape, c.shape, strides)
    return c


def residual_block(input, index, i, filter1, filter2, filter3, 
                   strides_init, bn, model_weight, optimizer):
#    if strides_init != 1 or block_name == "res2_0":
#        #一个conv2d_affine(conv, bn, relu层)
#        shortcut = conv2d_affine(input, block_name + "_branch1", 1, 1, filter3, 1, strides_init)
#    else:
#        shortcut = input
    #对输入做变换，使得和三层oncv的输出shape相同，可以相加
    shortcut = conv2d_affine(input, "conv_shortcut"+str(index)+"_"+str(i), filter3, 3, 
                             strides_init, bn)
    #shortcut层添加model weight
    if model_weight == True:
        modelWeight.addConv(index="_shortcut"+str(index)+"_"+str(i),
                            dtype=input.dtype,
                            shape1=(filter3, input.shape[1], 3, 3),
                            shape2=(filter3,),
                            optimizer=optimizer)
    #三个conv2d_affine(conv, bn, relu层)
    bottleneck = bottleneck_transformation(input, filter1, filter2, filter3, 
                                           strides_init, bn, model_weight, optimizer)
#    print(bottleneck.shape, shortcut.shape, strides_init, i)
    return flow.nn.relu(bottleneck + shortcut)


def residual_stage(input, index, counts, cfg, bn, model_weight, optimizer, stride_init=2):
    output = input
    for i in range(counts):
#        block_name = "%s_%d" % (stage_name, i)
        output = residual_block(output, index, i, cfg[i*3+0], cfg[i*3+1], cfg[i*3+2], 
                                stride_init if i == 0 else 1, bn, model_weight, optimizer)
    return output

#resnet50主体结构
def resnet_conv_x_body(input, cfg, bn, model_weight, optimizer, on_stage_end=lambda x: x):
    output = input
    for index, (counts, cfg_i) in enumerate(
        zip(BLOCK_COUNTS, cfg)
    ):
        #stage_name为res2/res3/res4/res5
#        stage_name = "res%d" % (i + 2)
        output = residual_stage(output, index, counts, cfg_i, bn, model_weight,
                                optimizer, 1 if index == 0 else 2)
        on_stage_end(output)
    return output

#最初的卷积层
def resnet_stem(input, bn, model_weight, optimizer):
    conv_stem = _conv2d("conv_stem", input, 64, 7, 2)
    if bn:
        conv_stem = _batch_norm(conv_stem, "conv_stem_bn")
    conv_stem = flow.nn.relu(conv_stem)
    pool1 = flow.nn.max_pool2d(
        conv_stem, ksize=3, strides=2, padding="VALID", data_format="NCHW", name="pool1",
    )
    #最初的卷积层添加model weight
    if model_weight == True:
        modelWeight.addConv(index="_stem", dtype=input.dtype,
                            shape1=(64, input.shape[1], 7, 7),
                            shape2=(64,),
                            optimizer=optimizer)
    return pool1

def resnet50(images, cfg, optimizer, trainable=True, need_transpose=False, 
             model_weight=True, bn=True):
    if need_transpose:
        images = flow.transpose(images, name="transpose", perm=[0, 3, 1, 2])

    global NAME_NUMBER
    NAME_NUMBER = 0
    stem = resnet_stem(images, bn, model_weight, optimizer)
    body = resnet_conv_x_body(stem, cfg, bn, model_weight, optimizer, lambda x: x)
    pool5 = flow.nn.avg_pool2d(
        body, ksize=7, strides=1, padding="VALID", data_format="NCHW", name="pool5",
    )
    pool5 = flow.reshape(pool5, [pool5.shape[0], -1])
    dense0 = flow.layers.dense(
        inputs=pool5,
        units=cfg[4],
        use_bias=True,
        kernel_initializer=flow.xavier_uniform_initializer(),
        bias_initializer=flow.zeros_initializer(),
        trainable=trainable,
        name="dense0",)
        
    def getTypeAndShape(inputs,units):
        in_shape = inputs.shape
        in_num_axes = len(in_shape)
        inputs = (flow.reshape(inputs, (-1, in_shape[-1])) if in_num_axes > 2 else inputs)
        shape=(units, inputs.shape[1])
        dtype=inputs.dtype
        return shape,dtype
        
    #添加dense层的Model weight
    if model_weight == True:
        shape_list = []
        dtype_list = []
        shape_weight, dtype = getTypeAndShape(pool5, cfg[4])
        shape_list.append(shape_weight)
        dtype_list.append(dtype)
        modelWeight.addDense(dtype_old=dtype_list, shape=shape_list,
                             optimizer=optimizer, dense_num=1)

    return dense0
