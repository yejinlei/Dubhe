# coding: utf-8
import os
import numpy as np
import pickle as pkl
from tqdm import tqdm
import time
from datetime import timedelta
import csv
import sys
import codecs
import logging_setup
import struct
from parse_args import parse_args
import oneflow.core.record.record_pb2 as of_record

MAX_VOCAB_SIZE = 10000  # 词表长度限制
UNK, PAD = '<UNK>', '<PAD>'  # 未知字，padding符号

if __name__ == '__main__':
    logger = logging_setup.setup_logger(__name__)
else:
    logger = logging_setup.setup_multiprocessing_logger()

def _truncate_seq_pair(tokens_a, tokens_b, max_length):
    """Truncates a sequence pair in place to the maximum length."""
    # This is a simple heuristic which will always truncate the longer sequence
    # one token at a time. This makes more sense than truncating an equal percent
    # of tokens from each, since if one sequence is very short then each token
    # that's truncated likely contains more information than a longer sequence.
    while True:
        total_length = len(tokens_a) + len(tokens_b)
        if total_length <= max_length:
            break
        if len(tokens_a) > len(tokens_b):
            tokens_a.pop()
        else:
            tokens_b.pop()

def SST2_Processor(path):
    examples = []
    with open(path, 'r', encoding='UTF-8') as f:
        i=0
        for line in tqdm(f):
            if i==0:
                i += 1
                continue
            try:
                lin = line.strip()
                if not lin:
                    continue
                text_a, label = lin.split('\t')
                text_b = None
                examples.append([text_a, text_b, label])

            except Exception as e:
                print(e)
    return examples

def CoLA_Processor(path):
    examples = []
    with open(path, 'r', encoding='UTF-8') as f:
        i=0
        for line in tqdm(f):
            try:
                lin = line.strip().split('\t')
                if not lin:
                    continue
                text_a = lin[3]
                text_b = None
                label = lin[1]

                examples.append([text_a, text_b, label])
            except Exception as e:
                print(e)
    return examples

def QQP_Processor(path):
    examples = []
    with open(path, 'r', encoding='UTF-8') as f:
        i=0
        for line in tqdm(f):
            if i==0:
                i += 1
                continue
            try:
                lin = line.strip().split('\t')
                if not lin:
                    continue
                text_a = lin[3]
                text_b = lin[4]
                label = lin[5]

                examples.append([text_a,text_b,label])
            except Exception as e:
                print(e)
    return examples

def RTE_Processor(path):
    examples = []
    with open(path, 'r', encoding='UTF-8') as f:
        i=0
        for line in tqdm(f):
            if i==0:
                i += 1
                continue
            try:
                lin = line.strip().split('\t')
                if not lin:
                    continue
                text_a = lin[1]
                text_b = lin[2]
                label = lin[-1]

                examples.append([text_a,text_b,label])
            except Exception as e:
                print(e)
    return examples

def MRPC_Processor(path):
    examples = []
    with open(path, 'r', encoding='UTF-8') as f:
        i=0
        for line in tqdm(f):
            if i==0:
                i += 1
                continue
            try:
                lin = line.strip().split('\t')
                if not lin:
                    continue
                text_a = lin[3]
                text_b = lin[4]
                label = lin[0]
                examples.append([text_a,text_b,label])
            except Exception as e:
                print(e)
    return examples

def convert_single_example(examples,tokenizer, pad_size, vocab):
    contents = []
    for example in examples:
        text_a = example[0]
        text_b = example[1]
        label = example[2]

        words_line = []
        tokens_a = tokenizer(text_a)

        if text_b:
            tokens_b = tokenizer(text_b)
            _truncate_seq_pair(tokens_a, tokens_b, pad_size - 1)
            token = tokens_a + [PAD] + tokens_b
        else:
            token = tokens_a

        seq_len = len(token)
        if pad_size:
            if len(token) < pad_size:
                token.extend([PAD] * (pad_size - len(token)))
            else:
                token = token[:pad_size]
                seq_len = pad_size
        # word to id
        for word in token:
            words_line.append(vocab.get(word, vocab.get(UNK)))
        contents.append((words_line, label, seq_len))
    return contents

def build_vocab(dataset, file_path, tokenizer, max_size, min_freq):
    vocab_dic = {}
    if dataset == 'SST-2':
        examples = SST2_Processor(file_path)
    elif dataset == 'CoLA':
        examples = CoLA_Processor(file_path)
    elif dataset == 'MRPC':
        examples = MRPC_Processor(file_path)
    elif dataset == 'QQP':
        examples = QQP_Processor(file_path)
    elif dataset == 'RTE':
        examples = RTE_Processor(file_path)
    else:
        print('Error: the dataset does not support')

    print('Building vocab ...')
    for example in tqdm(examples):
        text_a = example[0]
        text_b = example[1]
        if text_b:
            text = text_a + text_b
        else:
            text = text_a
        for word in tokenizer(text):
            vocab_dic[word] = vocab_dic.get(word, 0) + 1
        vocab_list = sorted([_ for _ in vocab_dic.items() if _[1] >= min_freq], key=lambda x: x[1], reverse=True)[:max_size]
        vocab_dic = {word_count[0]: idx for idx, word_count in enumerate(vocab_list)}
        vocab_dic.update({UNK: len(vocab_dic), PAD: len(vocab_dic) + 1})
    return vocab_dic


def build_dataset(dataset, config, ues_word):
    if ues_word:
        tokenizer = lambda x: x.split(' ')  # 以空格隔开，word-level
    else:
        tokenizer = lambda x: [y for y in x]  # char-level
    if os.path.exists(config.vocab_path):
        vocab = pkl.load(open(config.vocab_path, 'rb'))
    else:
        vocab = build_vocab(dataset, config.train_path, tokenizer=tokenizer, max_size=MAX_VOCAB_SIZE, min_freq=1)
        pkl.dump(vocab, open(config.vocab_path, 'wb'))
    print(f"Vocab size: {len(vocab)}")

    def load_dataset(dataset, tokenizer, path, pad_size=32):
        if dataset=='SST-2':
            examples = SST2_Processor(path)
        elif dataset=='CoLA':
            examples = CoLA_Processor(path)
        elif dataset=='MRPC':
            examples = MRPC_Processor(path)
        elif dataset=='QQP':
            examples = QQP_Processor(path)
        elif dataset == 'RTE':
            examples = RTE_Processor(path)
        else:
            print('error dataset not support')
        contents = convert_single_example(examples,tokenizer,pad_size,vocab)
        return contents

    train = load_dataset(dataset,tokenizer, config.train_path, config.pad_size)
    dev = load_dataset(dataset,tokenizer, config.dev_path, config.pad_size)
    # test = load_dataset(config.test_path, config.pad_size)
    return vocab, train, dev


def get_time_dif(start_time):
    """获取已使用时间"""
    end_time = time.time()
    time_dif = end_time - start_time
    return timedelta(seconds=int(round(time_dif)))

def file_based_convert_examples_to_features(
        examples, label_list, max_seq_length, output_file):
    """Convert a set of `InputExample`s to a TFRecord file."""

    # writer = tf.python_io.TFRecordWriter(output_file)
    writer = open(output_file, 'ab')

    total_written = 0
    for (ex_index, example) in enumerate(examples):
        if ex_index % 10000 == 0:
            logger.info("Writing example %d of %d" % (ex_index, len(examples)))

        label_map = {}
        for (i, label) in enumerate(label_list):
            label_map[label] = i

        input_mask = [1] * example[2] + [0] * (max_seq_length - example[2])
        segment_ids = [1] * max_seq_length
        assert len(input_mask)==max_seq_length

        label_id = label_map[example[1]]
        def create_int32_feature(values):
            return of_record.Feature(int32_list=of_record.Int32List(value=values)),

        sample = of_record.OFRecord(
            feature={
                "input_ids": create_int32_feature(example[0]),
                "input_mask": create_int32_feature(input_mask),
                "segment_ids": create_int32_feature(segment_ids),
                "label_ids": create_int32_feature([label_id]),
                "is_real_example": create_int32_feature([int(True)])
            }
        )

        writer.write(struct.pack("q", sample.ByteSize()))
        writer.write(sample.SerializeToString())
        if ex_index % 10000 == (len(examples) - 1) % 10000:
            logger.info('Wrote intances %d/%d to "%s"', ex_index, len(examples), output_file)

        total_written += 1

    writer.close()
    logger.info('Wrote total %d instances to output files "%s"', total_written, output_file)

class Config(object):
    vocab_path = ''
    train_path = ''
    dev_path = ''
    pad_size = 32

if __name__ == "__main__":
    '''提取预训练词向量'''
    # 下面的目录、文件名按需更改。
    config =Config
    dataset = "MRPC"
    train_dir = "../../data/glue_data/{}/train.tsv".format(dataset)
    dev_dir = "../../data/glue_data/{}/dev.tsv".format(dataset)

    vocab_dir = "../../data/glue_ofrecord/{}_lstm_32".format(dataset)
    pretrain_dir = ""
    emb_dim = 300
    if os.path.exists(os.path.join(vocab_dir,'vocab.pkl')):
        word_to_id = pkl.load(open(os.path.join(vocab_dir,'vocab.pkl'), 'rb'))
    else:
        tokenizer = lambda x: x.split(' ')  # 以词为单位构建词表(数据集中词之间以空格隔开)
        # tokenizer = lambda x: [y for y in x]  # 以字为单位构建词表
        word_to_id = build_vocab(dataset, train_dir, tokenizer=tokenizer, max_size=MAX_VOCAB_SIZE, min_freq=1)
        os.makedirs(vocab_dir, exist_ok=True)
        pkl.dump(word_to_id, open(os.path.join(vocab_dir,'vocab.pkl'), 'wb'))

    # print(word_to_id)
    # print(len(word_to_id))

    output_dir = '../../data/glue_ofrecord/{}_lstm_32'.format(dataset)
    total_examples = {}
    max_seq_length= 32
    config.vocab_path = os.path.join(vocab_dir,'vocab.pkl')
    config.train_path = train_dir
    config.dev_path = dev_dir
    config.pad_size = max_seq_length
    if dataset == 'RTE':
        label_list = ["entailment", "not_entailment"]
    elif dataset in ['SST-2', 'MRPC', 'QQP', 'CoLA']:
        label_list = ["0", "1"]
    elif dataset == 'MNLI':
        label_list = ["contradiction", "entailment", "neutral"]
    else:
        print('Error: the dataset not supports')

    # print(config.vocab_path)
    _,train_dataset,dev_dataset = build_dataset(dataset=dataset, config=config,ues_word='True')
    # print(dev_dataset[0])

    os.makedirs(os.path.join(output_dir, 'eval'), exist_ok=True)
    dev_file = os.path.join(output_dir, 'eval', "eval.of_record-0")
    file_based_convert_examples_to_features(dev_dataset,label_list,config.pad_size,dev_file)

    os.makedirs(os.path.join(output_dir, 'train'), exist_ok=True)
    train_file = os.path.join(output_dir, 'train', "train.of_record-0")
    file_based_convert_examples_to_features(train_dataset,label_list,config.pad_size,train_file)
