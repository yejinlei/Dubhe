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
import os
import math
import oneflow as flow
import oneflow.typing as tp
from typing import Tuple,Any

import bert as bert_util

def BertForSequenceClassification(
    input_ids_blob,
    input_mask_blob,
    token_type_ids_blob,
    label_blob,
    vocab_size,
    seq_length=512,
    hidden_size=768,
    num_hidden_layers=12,
    num_attention_heads=12,
    intermediate_size=3072,
    hidden_act="gelu",
    hidden_dropout_prob=0.1,
    attention_probs_dropout_prob=0.1,
    max_position_embeddings=512,
    type_vocab_size=16,
    initializer_range=0.02,
    label_num=2,
    is_student=False,
    fit_size=768,
    is_train=False
):
    # with flow.scope.namespace('teacher'):
    backbone = bert_util.BertBackbone(
        input_ids_blob=input_ids_blob,
        input_mask_blob=input_mask_blob,
        token_type_ids_blob=token_type_ids_blob,
        vocab_size=vocab_size,
        seq_length=seq_length,
        hidden_size=hidden_size,
        num_hidden_layers=num_hidden_layers,
        num_attention_heads=num_attention_heads,
        intermediate_size=intermediate_size,
        hidden_act=hidden_act,
        hidden_dropout_prob=hidden_dropout_prob,
        attention_probs_dropout_prob=attention_probs_dropout_prob,
        max_position_embeddings=max_position_embeddings,
        type_vocab_size=type_vocab_size,
        initializer_range=initializer_range,
        is_train=is_train
    )
    pooled_output = PooledOutput(
        sequence_output=backbone.sequence_output(),
        hidden_size=hidden_size,
        initializer_range=initializer_range,
        is_train=is_train
    )
    logit_blob = _AddClassfication(
        input_blob=pooled_output,
        label_blob=label_blob,
        hidden_size=hidden_size,
        label_num=label_num,
        initializer_range=initializer_range,
        scope_name='classification',
        is_train=is_train
    )
    sequence_output = backbone.all_encoder_layers()
    att_output = backbone.all_attention_probs()
    embed_output = backbone.embedding_output()
    sequence_output.insert(0,embed_output)
    # print(logit_blob.shape)
    # print(len(sequence_output))

    # print(sequence_output.shape)

    tmp = []
    if is_student:
        for s_id, sequence_layer in enumerate(sequence_output):
            tmp.append(
            fit_dense(
                input_blob=sequence_layer,
                hidden_size=hidden_size,
                label_num=fit_size,
                initializer_range=initializer_range,
                scope_name='fit_dense',
                is_train=is_train
            ))
        sequence_output = tmp

    return logit_blob, sequence_output, att_output

def BertStudentForSequenceClassification(
    input_ids_blob,
    input_mask_blob,
    token_type_ids_blob,
    label_blob,
    vocab_size,
    seq_length=512,
    hidden_size=768,
    num_hidden_layers=12,
    num_attention_heads=12,
    intermediate_size=3072,
    hidden_act="gelu",
    hidden_dropout_prob=0.1,
    attention_probs_dropout_prob=0.1,
    max_position_embeddings=512,
    type_vocab_size=16,
    initializer_range=0.02,
    label_num=2,
    is_student=False,
    fit_size=768,
    is_train=True
):
    with flow.scope.namespace('student'):
        backbone = bert_util.BertBackbone(
            input_ids_blob=input_ids_blob,
            input_mask_blob=input_mask_blob,
            token_type_ids_blob=token_type_ids_blob,
            vocab_size=vocab_size,
            seq_length=seq_length,
            hidden_size=hidden_size,
            num_hidden_layers=num_hidden_layers,
            num_attention_heads=num_attention_heads,
            intermediate_size=intermediate_size,
            hidden_act=hidden_act,
            hidden_dropout_prob=hidden_dropout_prob,
            attention_probs_dropout_prob=attention_probs_dropout_prob,
            max_position_embeddings=max_position_embeddings,
            type_vocab_size=type_vocab_size,
            initializer_range=initializer_range,
            is_train=is_train
        )
        pooled_output = PooledOutput(
            sequence_output=backbone.sequence_output(),
            hidden_size=hidden_size,
            initializer_range=initializer_range,
            is_train=is_train
        )
        logit_blob = _AddClassfication(
            input_blob=pooled_output,
            label_blob=label_blob,
            hidden_size=hidden_size,
            label_num=label_num,
            initializer_range=initializer_range,
            scope_name='classification',
            is_train=is_train
        )
        sequence_output = backbone.all_encoder_layers()
        att_output = backbone.all_attention_probs()
        embed_output = backbone.embedding_output()
        sequence_output.insert(0, embed_output)
        # print(logit_blob.shape)
        # print(len(sequence_output))
        # print(sequence_output.shape)

        tmp = []
        if is_student:
            for s_id, sequence_layer in enumerate(sequence_output):
                tmp.append(
                fit_dense(
                    input_blob=sequence_layer,
                    hidden_size=hidden_size,
                    label_num=fit_size,
                    initializer_range=initializer_range,
                    scope_name='fit_dense',
                    is_train=is_train
                ))
            sequence_output = tmp

    return logit_blob, sequence_output, att_output

def CreateInitializer(std):
  return flow.truncated_normal(std)


def _EmbeddingLookup(input_ids_blob,
                     vocab_size,
                     embedding_size=128,
                     initializer_range=0.02,
                     word_embedding_name="word_embeddings",
                     is_train=True):
  embedding_table = flow.get_variable(name=word_embedding_name, shape=[vocab_size, embedding_size],
                                      dtype=flow.float,
                                      trainable=is_train,
                                      initializer=CreateInitializer(initializer_range))
  output = flow.gather(params=embedding_table, indices=input_ids_blob, axis=0)
  return output, embedding_table

def watch_diff_handler(blob: tp.Numpy):
    print("watch_diff_handler:", blob, blob.shape, blob.dtype)

def watch_handler(y: tp.Numpy):
    print("out:",y)

from lstm import lstm,Blstm
def LSTMStudentForSequenceClassification(
    input_ids_blob,
    input_mask_blob,
    token_type_ids_blob,
    label_blob,
    vocab_size,
    seq_length=512,
    hidden_size=300,
    intermediate_size=400,
    num_hidden_layers=1,
    hidden_dropout_prob=0.5,
    initializer_range=0.25,
    label_num=2,
    is_student=True,
    is_train=True
):
    with flow.scope.namespace('student'):
        with flow.scope.namespace("embeddings"):
            (embedding_output_, embedding_table_) = _EmbeddingLookup(
                input_ids_blob=input_ids_blob,
                vocab_size=vocab_size+1,
                embedding_size=hidden_size,
                word_embedding_name="word_embeddings",
                is_train=is_train)

        with flow.scope.namespace('lstm'):
            output = lstm(embedding_output_, hidden_size, return_sequence=False, is_train=is_train)
            output = flow.layers.dense(inputs=output,units=intermediate_size,activation=flow.nn.relu,kernel_initializer=CreateInitializer(initializer_range),trainable=is_train,name='FC1')
            output = _Dropout(output, hidden_dropout_prob)
            logit_blob = flow.layers.dense(inputs=output,units=label_num,kernel_initializer=CreateInitializer(initializer_range),trainable=is_train,name='FC2')
    return logit_blob

def PooledOutput(sequence_output, hidden_size, initializer_range, is_train):
    with flow.scope.namespace("bert-pooler"):
        first_token_tensor = flow.slice(
            sequence_output, [None, 0, 0], [None, 1, -1])
        first_token_tensor = flow.reshape(
            first_token_tensor, [-1, hidden_size])
        pooled_output = bert_util._FullyConnected(
            first_token_tensor,
            input_size=hidden_size,
            units=hidden_size,
            weight_initializer=bert_util.CreateInitializer(initializer_range),
            name="dense",
            is_train=is_train
        )
        pooled_output = flow.math.tanh(pooled_output)
    return pooled_output


def _AddClassfication(input_blob, label_blob, hidden_size, label_num, initializer_range,
                          scope_name='classification',is_train=True):
    with flow.scope.namespace(scope_name):
        output_weight_blob = flow.get_variable(
            name="output_weights",
            shape=[label_num, hidden_size],
            dtype=input_blob.dtype,
            # initializer=bert_util.CreateInitializer(initializer_range),
            initializer=flow.random_normal_initializer(
                mean=0.0, stddev=initializer_range, seed=None, dtype=None),
            trainable=is_train
        )
        output_bias_blob = flow.get_variable(
            name="output_bias",
            shape=[label_num],
            dtype=input_blob.dtype,
            initializer=flow.constant_initializer(0.0),
            trainable=is_train
        )
        logit_blob = flow.matmul(
            input_blob, output_weight_blob, transpose_b=True)
        logit_blob = flow.nn.bias_add(logit_blob, output_bias_blob)
        # pre_example_loss = flow.nn.sparse_softmax_cross_entropy_with_logits(
        #     logits=logit_blob, labels=label_blob
        # )
        # loss = pre_example_loss
        # return loss, pre_example_loss, logit_blob
        return logit_blob

def _Dropout(input_blob, dropout_prob):
  if dropout_prob == 0.0:
    return input_blob
  return flow.nn.dropout(input_blob, rate=dropout_prob)


def fit_dense(input_blob, hidden_size, label_num, initializer_range,
                          scope_name='fit_dense',is_train=True):
    with flow.scope.namespace(scope_name):
        in_shape = input_blob.shape
        in_num_axes = len(in_shape)
        assert in_num_axes >= 2

        input_blob = (
            flow.reshape(input_blob, (-1, in_shape[-1])) if in_num_axes > 2 else input_blob
        )

        output_weight_blob = flow.get_variable(
            name="weight",
            shape=[label_num, hidden_size],
            dtype=input_blob.dtype,
            # initializer=bert_util.CreateInitializer(initializer_range),
            initializer=flow.random_normal_initializer(
                mean=0.0, stddev=initializer_range, seed=None, dtype=None),
            trainable=is_train
        )
        output_bias_blob = flow.get_variable(
            name="bias",
            shape=[label_num],
            dtype=input_blob.dtype,
            initializer=flow.constant_initializer(0.0),
            trainable=is_train
        )
        logit_blob = flow.matmul(
            input_blob, output_weight_blob, transpose_b=True)
        logit_blob = flow.nn.bias_add(logit_blob, output_bias_blob)
        logit_blob = (
            flow.reshape(logit_blob, in_shape[:-1] + (label_num,)) if in_num_axes > 2 else logit_blob
        )
        # pre_example_loss = flow.nn.sparse_softmax_cross_entropy_with_logits(
        #     logits=logit_blob, labels=label_blob
        # )
        # loss = pre_example_loss
        # return loss, pre_example_loss, logit_blob
        return logit_blob



def soft_cross_entropy(predicts, targets):
    student_likelihood = flow.math.log(flow.nn.softmax(predicts, axis=-1))
    targets_prob = flow.nn.softmax(targets, axis=-1)
    tmp = flow.math.multiply(flow.math.negative(targets_prob), student_likelihood)
    res = flow.math.reduce_mean(tmp)
    return res

def mseloss(rep1, rep2):
    return flow.math.reduce_mean(flow.math.square(rep1-rep2))

def layer_distill(args, student_reps, teacher_reps):
    rep_loss = 0.
    teacher_layer_num = len(teacher_reps) - 1
    student_layer_num = len(student_reps) - 1

    assert teacher_layer_num % student_layer_num == 0
    layers_per_block = int(teacher_layer_num / student_layer_num)

    new_teacher_reps = [teacher_reps[i * layers_per_block] for i in range(student_layer_num + 1)]
    new_student_reps = student_reps

    for student_rep, teacher_rep in zip(new_student_reps, new_teacher_reps):
        tmp_loss = mseloss(student_rep, teacher_rep)
        rep_loss += tmp_loss
    return rep_loss


def att_distill(args, student_atts, teacher_atts):
    att_loss = 0.
    teacher_layer_num = len(teacher_atts)
    student_layer_num = len(student_atts)

    assert teacher_layer_num % student_layer_num == 0
    layers_per_block = int(teacher_layer_num / student_layer_num)
    new_teacher_atts = [teacher_atts[i * layers_per_block + layers_per_block - 1] for i in range(student_layer_num)]

    for student_att, teacher_att in zip(student_atts, new_teacher_atts):
        student_att = flow.where(student_att <= flow.constant(-1e2,dtype=flow.float), flow.zeros_like(student_att), student_att)
        teacher_att = flow.where(teacher_att <= flow.constant(-1e2,dtype=flow.float), flow.zeros_like(teacher_att), teacher_att)

        tmp_loss = mseloss(student_att, teacher_att)
        att_loss += tmp_loss

    return att_loss

def pred_distill(args, student_logits, teacher_logits):
    soft_loss = soft_cross_entropy(student_logits / args.temperature,
                                  teacher_logits / args.temperature)
    return soft_loss

