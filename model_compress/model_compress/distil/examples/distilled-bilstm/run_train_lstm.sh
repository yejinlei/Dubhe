"""
Copyright 2020 The OneFlow Authors. All rights reserved.

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

nvidia-smi

dataset=SST-2

# ofrecord dataset dir
DATA_ROOT=./data/glue_ofrecord

# choose dateset `CoLA`, `MRPC` 'SST-2'

if [ $DATA_ROOT = "CoLA" ]; then
  train_example_num=8551
  eval_example_num=1043
  test_example_num=1063
  learning_rate=2e-5
  wd=0.0001
  epoch=70
elif [ $DATA_ROOT = "MRPC" ]; then
  train_example_num=3668
  eval_example_num=408
  test_example_num=1725
  learning_rate=5e-6
  epoch=20
  wd=0.001
elif [ $DATA_ROOT = "SST-2" ]; then
  train_example_num=67349
  eval_example_num=872
  test_example_num=1821
  learning_rate=3e-5
  epoch=4
  wd=0.0001
elif [ $DATA_ROOT = "QQP" ]; then
  train_example_num=363849
  eval_example_num=40430
  test_example_num=0
  learning_rate=2e-5
  epoch=5
  wd=0.0001
elif [ $DATA_ROOT = "MNLI" ]; then
  train_example_num=392702
  eval_example_num=9815
  test_example_num=0
  learning_rate=2e-5
  wd=0.0001
elif [ $DATA_ROOT = "WNLI" ]; then
  train_example_num=635
  eval_example_num=71
  test_example_num=0
  learning_rate=2e-5
  wd=0.0001
elif [ $DATA_ROOT = "RTE" ]; then
  train_example_num=2490
  eval_example_num=277
  test_example_num=0
  learning_rate=2e-5
  wd=0.0001
else
  echo "dataset must be GLUE such as 'CoLA','MRPC','SST-2','QQP','MNLI','WNLI','STS-B',"
  exit
fi

STUDENT_DIR="./models/student_model/${dataset}/lstm_32_epoch-${epoch}_lr-${learning_rate}_wd-${wd}"

train_data_dir=$DATA_ROOT/${dataset}/train
train_data_dir_lstm=$DATA_ROOT/${dataset}_lstm_32/train

eval_data_dir=$DATA_ROOT/${dataset}/eval
eval_data_dir_lstm=$DATA_ROOT/${dataset}_lstm_32/eval

#EPOCH=10
#learning_rate=2e-5 # 3e-5
GPU=0
CUDA_VISIBLE_DEVICES=$GPU python3 task_lstm.py \
  --do_train='True' \
  --do_eval='True' \
  --model=Glue_${TASK_NAME} \
  --task_name=${TASK_NAME}  \
  --gpu_num_per_node=1 \
  --num_epochs=${epoch} \
  --train_data_dir=$train_data_dir \
  --train_data_dir_lstm=${train_data_dir_lstm} \
  --train_example_num=$train_example_num \
  --eval_data_dir=$eval_data_dir \
  --eval_data_dir_lstm=$eval_data_dir_lstm \
  --eval_example_num=$eval_example_num \
  --batch_size_per_device=32 \
  --eval_batch_size_per_device=32 \
  --loss_print_every_n_iter 1 \
  --log_dir=./log \
  --model_save_dir=${STUDENT_DIR} \
  --seq_length=32 \
  --student_num_hidden_layers=4 \
  --student_num_attention_heads=12 \
  --student_max_position_embeddings=512 \
  --student_type_vocab_size=2 \
  --student_vocab_size=10002 \
  --student_attention_probs_dropout_prob=0.1 \
  --student_hidden_dropout_prob=0.1 \
  --student_hidden_size_per_head=26 \
  --student_hidden_size=300 \
  --learning_rate=$learning_rate \
  --model_save_every_n_iter=50000 \
  --weight_decay_rate=$wd