# Copyright (c) The OneFlow Authors.
# Licensed under the Apache License
# Fine-tune Teacher model.
# ofrecord dataset dir
DATA_ROOT=$1

# pretrained model dir
PRETRAINED_MODEL=$2

TRAIN_LOG_DIR=$3

RESULT_DIR=$4

# choose dateset
dataset=$5

train_data_dir=$DATA_ROOT/${dataset}/train
eval_data_dir=$DATA_ROOT/${dataset}/eval

MODEL_SAVE_DIR="/usr/local/output/model/before"

# which GPU to use
GPU=0

if [ $dataset = "CoLA" ]; then
  train_example_num=8551
  eval_example_num=1043
  test_example_num=1063
  learning_rate=1e-5
  EPOCH=5
  wd=0.01
elif [ $dataset = "MRPC" ]; then
  train_example_num=3668
  eval_example_num=408
  test_example_num=1725
  learning_rate=3e-5
  EPOCH=5
  wd=0.001
elif [ $dataset = "SST-2" ]; then
  train_example_num=67349
  eval_example_num=872
  test_example_num=1821
  learning_rate=2e-5
  EPOCH=3
  wd=0.0001
elif [ $dataset = "QQP" ]; then
  train_example_num=363849
  eval_example_num=40430
  test_example_num=0
  learning_rate=2e-5
  EPOCH=5
  wd=0.0001
elif [ $dataset = "MNLI" ]; then
  train_example_num=392702
  eval_example_num=9815
  test_example_num=0
  learning_rate=2e-5
  EPOCH=5
  wd=0.0001
elif [ $dataset = "WNLI" ]; then
  train_example_num=635
  eval_example_num=71
  test_example_num=0
  learning_rate=2e-5
  EPOCH=5
  wd=0.0001
elif [ $dataset = "RTE" ]; then
  train_example_num=2490
  eval_example_num=277
  test_example_num=0
  learning_rate=3e-5
  EPOCH=5
  wd=0.0001
elif [ $dataset == "QNLI" ]; then
  train_example_num=104743
  eval_example_num=5463
  test_example_num=0
  learning_rate=2e-5
  EPOCH=5
  wd=0.0001
else
  echo "dataset must be GLUE such as 'CoLA','MRPC','SST-2','QQP','MNLI','WNLI'"
  exit
fi

CUDA_VISIBLE_DEVICES=$GPU python3 examples/teacher_bert/task_teacher.py \
  --do_train='True' \
  --do_eval='True' \
  --serve_for_online='True' \
  --model=Glue_${dataset} \
  --task_name=${dataset}  \
  --gpu_num_per_node=1 \
  --num_epochs=${EPOCH} \
  --train_data_dir=$train_data_dir \
  --train_example_num=$train_example_num \
  --eval_data_dir=$eval_data_dir \
  --eval_example_num=$eval_example_num \
  --model_load_dir=${PRETRAINED_MODEL} \
  --batch_size_per_device=32 \
  --eval_batch_size_per_device=32 \
  --loss_print_every_n_iter 20 \
  --log_dir=${TRAIN_LOG_DIR} \
  --result_dir=${RESULT_DIR} \
  --model_save_dir=${MODEL_SAVE_DIR} \
  --save_last_snapshot=True \
  --seq_length=128 \
  --num_hidden_layers=12 \
  --num_attention_heads=12 \
  --max_position_embeddings=512 \
  --type_vocab_size=2 \
  --vocab_size=30522 \
  --attention_probs_dropout_prob=0.1 \
  --hidden_dropout_prob=0.1 \
  --hidden_size_per_head=64 \
  --learning_rate $learning_rate \
  --weight_decay_rate $wd
