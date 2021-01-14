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

import os
import time
import argparse
from datetime import datetime

import test_global_storage
import oneflow as flow
import numpy as np
np.set_printoptions(suppress=True)

def _FullyConnected(input_blob,weight_blob,bias_blob):

    output_blob = flow.matmul(input_blob, weight_blob)
    if bias_blob:
        output_blob = flow.nn.bias_add(output_blob, bias_blob)
    return output_blob


def lstm(input,units,return_sequence=False,initial_state=None,direction='forward',layer_index=0, is_train=True):
    '''
       input: sequence input tensor with shape [batch_size,sequence_length,embedding size]
       units: hidden units numbers
    '''
    batch_size=input.shape[0]
    seq_len=input.shape[1]
    input_size = input.shape[2]
    
    dtype = flow.float32
    with flow.scope.namespace('layer'+str(layer_index)):
        with flow.scope.namespace(direction):
            weight_blob_i = flow.get_variable(
                name='input' + '-weight',
                shape=[input_size, units],
                dtype=dtype,
                trainable=is_train,
                initializer=flow.glorot_normal_initializer())

            weight_blob_ih = flow.get_variable(
                name='input' + '-h-weight',
                shape=[units, units],
                dtype=dtype,
                trainable=is_train,
                initializer=flow.glorot_normal_initializer())

            bias_blob_i = flow.get_variable(
                name='input' + '-bias',
                shape=[units],
                dtype=dtype,
                trainable=is_train,
                initializer=flow.constant_initializer(0.0))

            weight_blob_f = flow.get_variable(
                name='forget' + '-weight',
                shape=[input_size, units],
                dtype=dtype,
                trainable=is_train,
                initializer=flow.glorot_normal_initializer())

            weight_blob_fh = flow.get_variable(
                name='forget' + '-h-weight',
                shape=[units, units],
                dtype=dtype,
                trainable=is_train,
                initializer=flow.glorot_normal_initializer())

            bias_blob_f = flow.get_variable(
                name='forget' + '-bias',
                shape=[units],
                dtype=dtype,
                trainable=is_train,
                initializer=flow.constant_initializer(0.0))

            weight_blob_c = flow.get_variable(
                name='cell' + '-weight',
                shape=[input_size, units],
                dtype=dtype,
                trainable=is_train,
                initializer=flow.glorot_normal_initializer())

            weight_blob_ch = flow.get_variable(
                name='cell' + '-h-weight',
                shape=[units, units],
                dtype=dtype,
                trainable=is_train,
                initializer=flow.glorot_normal_initializer())

            bias_blob_c = flow.get_variable(
                name='cell' + '-bias',
                shape=[units],
                dtype=dtype,
                trainable=is_train,
                initializer=flow.constant_initializer(0.0))

            weight_blob_o = flow.get_variable(
                name='output' + '-weight',
                shape=[input_size, units],
                dtype=dtype,
                trainable=is_train,
                initializer=flow.glorot_normal_initializer())

            weight_blob_oh = flow.get_variable(
                name='output' + '-h-weight',
                shape=[units, units],
                dtype=dtype,
                trainable=is_train,
                initializer=flow.glorot_normal_initializer())

            bias_blob_o = flow.get_variable(
                name='output' + '-bias',
                shape=[units],
                dtype=dtype,
                trainable=is_train,
                initializer=flow.constant_initializer(0.0))
    
    flow.watch(weight_blob_i, test_global_storage.Setter("weight_blob_i"))
    flow.watch(weight_blob_f, test_global_storage.Setter("weight_blob_f"))
    flow.watch(weight_blob_c, test_global_storage.Setter("weight_blob_c"))
    flow.watch(weight_blob_o, test_global_storage.Setter("weight_blob_o"))

    flow.watch(weight_blob_ih, test_global_storage.Setter("weight_blob_ih"))
    flow.watch(weight_blob_fh, test_global_storage.Setter("weight_blob_fh"))   
    flow.watch(weight_blob_ch, test_global_storage.Setter("weight_blob_ch"))   
    flow.watch(weight_blob_oh, test_global_storage.Setter("weight_blob_oh"))  

    flow.watch(bias_blob_i, test_global_storage.Setter("bias_blob_i"))  
    flow.watch(bias_blob_f, test_global_storage.Setter("bias_blob_f"))  
    flow.watch(bias_blob_c, test_global_storage.Setter("bias_blob_c"))  
    flow.watch(bias_blob_o, test_global_storage.Setter("bias_blob_o"))  

    def step_function(input,states):
        
        hx=states[0]
        cx=states[1]

        x_i = _FullyConnected(input,weight_blob_i,bias_blob_i) # input gate
        mark_int=x_i
        x_f = _FullyConnected(input,weight_blob_f,bias_blob_f) # forget gate
        x_c = _FullyConnected(input,weight_blob_c,bias_blob_c) # cell state
        x_o = _FullyConnected(input,weight_blob_o,bias_blob_o) # output gate

        h_i = _FullyConnected(hx,weight_blob_ih,None)
        h_f = _FullyConnected(hx,weight_blob_fh,None)
        h_c = _FullyConnected(hx,weight_blob_ch,None)
        h_o = _FullyConnected(hx,weight_blob_oh,None)


        x_i = x_i + h_i
        x_f = x_f+h_f
        x_c = x_c+h_c
        x_o = x_o+h_o

        x_i = flow.math.sigmoid(x_i)
        x_f = flow.math.sigmoid(x_f)
        cellgate = flow.math.tanh(x_c)
        x_o = flow.math.sigmoid(x_o)

        cy = x_f * cx + x_i * cellgate

        hy = x_o * flow.math.tanh(cy)

        return hy, (hy,cy)

    if initial_state:
        states=initial_state
    else:
        states=[flow.constant(0, dtype=flow.float32, shape=[batch_size,units]),flow.constant(0, dtype=flow.float32, shape=[batch_size,units])]
    
    successive_outputs=[]
    successive_states= []

    for index in range(seq_len):
        # print('time step:',index)
        inp = flow.slice(input, [None, index, 0], [None, 1, input_size])
        # print(inp.shape)
        inp = flow.reshape(inp, [-1, input_size])
        # print(inp.shape)
        output, states = step_function(inp, states)

        output = flow.reshape(output,[-1,1,units])
        successive_outputs.append(output)
        successive_states.append(states)
    last_output = successive_outputs[-1]
    new_states = successive_states[-1]
    outputs = flow.concat(successive_outputs,axis=1)
    


    if return_sequence:
        return outputs
    else:
        return flow.reshape(last_output,[-1,units]) 

def Blstm(input,units,return_sequence=True,initial_state=None,layer_index=0,is_train=True):
    # return_sequence should be True for BLSTM currently
    # default concat method : add

    forward = lstm(input,units,return_sequence=return_sequence,initial_state=initial_state,direction='forward',layer_index=layer_index,is_train=is_train)

    reverse_input = flow.reverse(input,axis=1)
    backward = lstm(reverse_input,units,return_sequence=return_sequence,initial_state=initial_state,direction='backward',layer_index=layer_index,is_train=is_train)
    backward = flow.reverse(backward,axis=1)

    outputs = forward + backward

    return outputs 

def TestLstm():
    func_config = flow.FunctionConfig()
    func_config.default_data_type(flow.float32)

    flow.config.gpu_device_num(1)

    @flow.global_function(func_config)
    def InferenceNet(sentence=flow.FixedTensorDef((32, 128, 312), dtype=flow.float32)):

        output = lstm(sentence,512,return_sequence=False)
        
        return output

    flow.config.enable_debug_mode(True)
    check_point = flow.train.CheckPoint()
    check_point.init()
    sentence_in = np.random.uniform(-10, 10, (32, 128, 312)).astype(np.float32)

    output_of = InferenceNet(sentence_in).get()
    print('output shape',output_of.numpy().shape)
    print('lstm hello world')
    # print('x_o',output_of[0].numpy())


    # print('o',output_of[3].numpy())
    #print('output shape:',output.numpy().shape)
    # print('weight:',test_global_storage.Get("weight_blob_i") )
    # print('weight:',test_global_storage.Get("weight_blob_ih").shape )
    # print('lstm hello world')

    # from tensorflow.keras import layers
    # from tensorflow import keras
    #
    # inputs = keras.Input(shape=(14, 64))
    # x = layers.LSTM(15,return_sequences=True,recurrent_activation ='sigmoid',name="lstm_one")(inputs)
    #
    # weight_blob_i = test_global_storage.Get("weight_blob_i")
    # weight_blob_f = test_global_storage.Get("weight_blob_f")
    # weight_blob_c = test_global_storage.Get("weight_blob_c")
    # weight_blob_o = test_global_storage.Get("weight_blob_o")
    # kernel_1 = np.concatenate( ( weight_blob_i,weight_blob_f,weight_blob_c,weight_blob_o) ,axis=1)
    #
    # weight_blob_ih = test_global_storage.Get("weight_blob_ih")
    # weight_blob_fh = test_global_storage.Get("weight_blob_fh")
    # weight_blob_ch = test_global_storage.Get("weight_blob_ch")
    # weight_blob_oh = test_global_storage.Get("weight_blob_oh")
    # kernel_2 = np.concatenate( ( weight_blob_ih,weight_blob_fh,weight_blob_ch,weight_blob_oh) ,axis=1)
    #
    # bias_blob_i = test_global_storage.Get("bias_blob_i")
    # bias_blob_f = test_global_storage.Get("bias_blob_f")
    # bias_blob_c = test_global_storage.Get("bias_blob_c")
    # bias_blob_o = test_global_storage.Get("bias_blob_o")
    # bias_1 = np.concatenate( ( bias_blob_i,bias_blob_f,bias_blob_c,bias_blob_o) )
    #
    # model = keras.Model(inputs,x)
    # model.get_layer("lstm_one").set_weights([kernel_1,kernel_2,bias_1])
    # output_tf = model.predict(sentence_in)
    #
    # print(output_of.numpy()[:,-1,:])
    # print('-'*100)
    # print(output_tf[:,-1,:])
    # assert(np.allclose(output_of.numpy(),output_tf, rtol=1e-04,atol=1e-04))

def TestBlstm():
    func_config = flow.FunctionConfig()
    func_config.default_data_type(flow.float32)

    flow.config.gpu_device_num(1)

    @flow.global_function(func_config)
    def InferenceNet(sentence=flow.FixedTensorDef((8,15,64), dtype=flow.float32)):

        output = Blstm(sentence,15,return_sequence=True)
        
        return output
    
    flow.config.enable_debug_mode(True)
    check_point = flow.train.CheckPoint()
    check_point.init()
    sentence_in = np.random.uniform(-10, 10, (8, 15, 64)).astype(np.float32)

    output=InferenceNet(sentence_in).get()

    print('output shape',output.numpy().shape)
    print('blstm hello world')

if __name__ == "__main__":
    TestLstm()
    # TestBlstm()