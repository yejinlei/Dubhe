# !/usr/bin/env python
# -*- coding:utf-8 -*-

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
=============================================================
"""
import json
import re
import six
import numpy as np
from typing import Tuple
# import requests # 在 nfs 没有挂载 时使用 url 访问
import sys
import oneflow as flow
import oneflow.typing as tp

BATCH_SIZE = 16


class TextCNN:
    def __init__(self, emb_sz, emb_dim, ksize_list, n_filters_list, n_classes, dropout):
        self.initializer = flow.random_normal_initializer(stddev=0.1)
        self.emb_sz = emb_sz
        self.emb_dim = emb_dim
        self.ksize_list = ksize_list
        self.n_filters_list = n_filters_list
        self.n_classes = n_classes
        self.dropout = dropout
        self.total_n_filters = sum(self.n_filters_list)
    
    def get_logits(self, inputs, is_train):
        emb_weight = flow.get_variable(
            'embedding-weight',
            shape=(self.emb_sz, self.emb_dim),
            dtype=flow.float32,
            trainable=is_train,
            reuse=False,
            initializer=self.initializer,
        )
        data = flow.gather(emb_weight, inputs, axis=0)
        data = flow.transpose(data, [0, 2, 1])  # BLH -> BHL
        data = flow.reshape(data, list(data.shape) + [1])
        seq_length = data.shape[2]
        pooled_list = []
        for i in range(len(self.n_filters_list)):
            ksz = self.ksize_list[i]
            n_filters = self.n_filters_list[i]
            conv = flow.layers.conv2d(data, n_filters, [ksz, 1], data_format="NCHW",
                                      kernel_initializer=self.initializer, name='conv-{}'.format(i))  # NCHW
            # conv = flow.layers.layer_norm(conv, name='ln-{}'.format(i))
            conv = flow.nn.relu(conv)
            pooled = flow.nn.max_pool2d(conv, [seq_length - ksz + 1, 1], strides=1, padding='VALID', data_format="NCHW")
            pooled_list.append(pooled)
        pooled = flow.concat(pooled_list, 3)
        pooled = flow.reshape(pooled, [-1, self.total_n_filters])
        
        if is_train:
            pooled = flow.nn.dropout(pooled, rate=self.dropout)
        
        pooled = flow.layers.dense(pooled, self.total_n_filters, use_bias=True,
                                   kernel_initializer=self.initializer, name='dense-1')
        pooled = flow.nn.relu(pooled)
        logits = flow.layers.dense(pooled, self.n_classes, use_bias=True,
                                   kernel_initializer=self.initializer, name='dense-2')
        return logits


def get_eval_config():
    config = flow.function_config()
    config.default_data_type(flow.float)
    return config


def pad_sequences(sequences, maxlen=None, dtype='int32',
                  padding='pre', truncating='pre', value=0.):
    """Pads sequences to the same length.

    This function transforms a list of
    `num_samples` sequences (lists of integers)
    into a 2D Numpy array of shape `(num_samples, num_timesteps)`.
    `num_timesteps` is either the `maxlen` argument if provided,
    or the length of the longest sequence otherwise.

    Sequences that are shorter than `num_timesteps`
    are padded with `value` at the beginning or the end
    if padding='post.

    Sequences longer than `num_timesteps` are truncated
    so that they fit the desired length.
    The position where padding or truncation happens is determined by
    the arguments `padding` and `truncating`, respectively.

    Pre-padding is the default.

    # Arguments
        sequences: List of lists, where each element is a sequence.
        maxlen: Int, maximum length of all sequences.
        dtype: Type of the output sequences.
            To pad sequences with variable length strings, you can use `object`.
        padding: String, 'pre' or 'post':
            pad either before or after each sequence.
        truncating: String, 'pre' or 'post':
            remove values from sequences larger than
            `maxlen`, either at the beginning or at the end of the sequences.
        value: Float or String, padding value.

    # Returns
        x: Numpy array with shape `(len(sequences), maxlen)`

    # Raises
        ValueError: In case of invalid values for `truncating` or `padding`,
            or in case of invalid shape for a `sequences` entry.
    """
    if not hasattr(sequences, '__len__'):
        raise ValueError('`sequences` must be iterable.')
    num_samples = len(sequences)
    
    lengths = []
    sample_shape = ()
    flag = True
    
    # take the sample shape from the first non empty sequence
    # checking for consistency in the main loop below.
    
    for x in sequences:
        try:
            lengths.append(len(x))
            if flag and len(x):
                sample_shape = np.asarray(x).shape[1:]
                flag = False
        except TypeError:
            raise ValueError('`sequences` must be a list of iterables. '
                             'Found non-iterable: ' + str(x))
    
    if maxlen is None:
        maxlen = np.max(lengths)
    
    is_dtype_str = np.issubdtype(dtype, np.str_) or np.issubdtype(dtype, np.unicode_)
    if isinstance(value, six.string_types) and dtype != object and not is_dtype_str:
        raise ValueError("`dtype` {} is not compatible with `value`'s type: {}\n"
                         "You should set `dtype=object` for variable length strings."
                         .format(dtype, type(value)))
    
    x = np.full((num_samples, maxlen) + sample_shape, value, dtype=dtype)
    for idx, s in enumerate(sequences):
        if not len(s):
            continue  # empty list/array was found
        if truncating == 'pre':
            trunc = s[-maxlen:]
        elif truncating == 'post':
            trunc = s[:maxlen]
        else:
            raise ValueError('Truncating type "%s" '
                             'not understood' % truncating)
        
        # check `trunc` has expected shape
        trunc = np.asarray(trunc, dtype=dtype)
        if trunc.shape[1:] != sample_shape:
            raise ValueError('Shape of sample %s of sequence at position %s '
                             'is different from expected shape %s' %
                             (trunc.shape[1:], idx, sample_shape))
        
        if padding == 'post':
            x[idx, :len(trunc)] = trunc
        elif padding == 'pre':
            x[idx, -len(trunc):] = trunc
        else:
            raise ValueError('Padding type "%s" not understood' % padding)
    return x


@flow.global_function('predict', get_eval_config())
def predict_job(text: tp.Numpy.Placeholder((BATCH_SIZE, 150), dtype=flow.int32),
                ) -> Tuple[tp.Numpy, tp.Numpy]:
    with flow.scope.placement("gpu", "0:0"):
        model = TextCNN(50000, 100, ksize_list=[2, 3, 4, 5], n_filters_list=[100] * 4, n_classes=2, dropout=0.5)
        logits = model.get_logits(text, is_train=False)
        logits = flow.nn.softmax(logits)
        label = flow.math.argmax(logits)
        return label, logits


class TextCNNClassifier:
    
    def __init__(self):
        model_load_dir = "of_model/textcnn_imdb_of_best_model/"
        word_index_dir = "of_model/imdb_word_index/imdb_word_index.json"
        
        checkpoint = flow.train.CheckPoint()
        checkpoint.init()
        checkpoint.load(model_load_dir)
        
        with open(word_index_dir) as f:
            word_index = json.load(f)
        word_index = {k: (v + 2) for k, v in word_index.items()}
        word_index["<PAD>"] = 0
        word_index["<START>"] = 1
        word_index["<UNK>"] = 2
        self.word_index = word_index
    
    def inference(self, text_path_list, id_list, label_list):
        print("infer")
        classifications = []
        batch_text = []
        for i, text_path in enumerate(text_path_list):
            text = open('/nfs/' + text_path, "r").read()
            """
            # 在 nfs 没有挂载 时使用 url 访问 MinIO 进行测试
            url = "http://10.5.29.100:9000/" + text_path
            print(url)
            text = requests.get(url).text  # .encode('utf-8').decode('utf-8')
            """
            text = re.sub("[^a-zA-Z']", " ", text)
            text = list(map(lambda x: x.lower(), text.split()))
            text.insert(0, "<START>")
            batch_text.append(
                list(map(lambda x: self.word_index[x] if x in self.word_index else self.word_index["<UNK>"], text))
            )
            
            if i % BATCH_SIZE == BATCH_SIZE - 1:
                text = pad_sequences(batch_text, value=self.word_index["<PAD>"], padding='post', maxlen=150)
                text = np.array(text, dtype=np.int32)
                label, logits = predict_job(text)
                label = label.tolist()
                logits = logits.tolist()
                for k in range(BATCH_SIZE):
                    classifications.append({
                        'id': id_list[i - BATCH_SIZE + 1 + k],
                        'annotation': json.dumps(
                            [{'category_id': label_list[label[k]], 'score': round(logits[k][label[k]], 4)}])
                    })
                batch_text = []
        return classifications
