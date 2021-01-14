# Copyright (c) The OneFlow Authors.
# Licensed under the Apache License

# Script for knowledge distillation with KD algorithm.

# ofrecord dataset dir
DATA_ROOT=$1

# saved student model dir
STUDENT_DIR="$2/student_model"

# tran log out
TRAIN_LOG_DIR=$3

# inference json result out
RESULT_DIR=$4

dataset=$5

# fine-tuned teacher model dir
FT_BERT_BASE_DIR="/usr/local/output/model/before/snapshot_best"

# temp student model dir
TMP_STUDENT_DIR="./models/bert_pkd_3/${dataset}"

train_data_dir=$DATA_ROOT/${dataset}/train
eval_data_dir=$DATA_ROOT/${dataset}/eval

# which GPU to use
GPU=0

if [ $dataset = "CoLA" ]; then
  train_example_num=8551
  eval_example_num=1043
  test_example_num=1063
  learning_rate=5e-5
  wd=0.0001
  epoch=70
elif [ $dataset = "MRPC" ]; then
  train_example_num=3668
  eval_example_num=408
  test_example_num=1725
  learning_rate=2e-5
  epoch=5
  wd=0.001
elif [ $dataset = "SST-2" ]; then
  train_example_num=67349
  eval_example_num=872
  test_example_num=1821
  learning_rate=2e-5
  epoch=4
  wd=0.0001
elif [ $dataset = "QQP" ]; then
  train_example_num=363849
  eval_example_num=40430
  test_example_num=0
  learning_rate=5e-5
  epoch=5
  wd=0.0001
elif [ $dataset = "MNLI" ]; then
  train_example_num=392702
  eval_example_num=9815
  test_example_num=0
  learning_rate=2e-5
  epoch=5
  wd=0.0001
elif [ $dataset = "WNLI" ]; then
  train_example_num=635
  eval_example_num=71
  test_example_num=0
  learning_rate=2e-5
  epoch=5
  wd=0.0001
elif [ $dataset = "RTE" ]; then
  train_example_num=2490
  eval_example_num=277
  test_example_num=0
  learning_rate=2e-5
  epoch=5
  wd=0.0001
else
  echo "dataset must be GLUE such as 'CoLA','MRPC','SST-2','QQP','MNLI','WNLI','STS-B',"
  exit
fi

CUDA_VISIBLE_DEVICES=$GPU python3 ./examples/knowledge_distillation/task_student_kd.py \
  --do_train='True' \
  --do_eval='True' \
  --serve_for_online='True' \
  --model=Glue_${dataset} \
  --task_name=${dataset}  \
  --gpu_num_per_node=1 \
  --num_epochs=${epoch} \
  --train_data_dir=$train_data_dir \
  --train_example_num=$train_example_num \
  --eval_data_dir=$eval_data_dir \
  --eval_example_num=$eval_example_num \
  --teacher_model=${FT_BERT_BASE_DIR} \
  --student_model=${TMP_STUDENT_DIR} \
  --batch_size_per_device=32 \
  --eval_batch_size_per_device=32 \
  --loss_print_every_n_iter 10 \
  --log_dir=${TRAIN_LOG_DIR} \
  --result_dir=${RESULT_DIR} \
  --model_save_dir=${STUDENT_DIR} \
  --seq_length=128 \
  --student_num_hidden_layers=4 \
  --student_num_attention_heads=12 \
  --student_max_position_embeddings=512 \
  --student_type_vocab_size=2 \
  --student_vocab_size=30522 \
  --student_attention_probs_dropout_prob=0.1 \
  --student_hidden_dropout_prob=0.1 \
  --student_hidden_size_per_head=26 \
  --student_hidden_size=312 \
  --teacher_num_hidden_layers=12 \
  --teacher_num_attention_heads=12 \
  --teacher_max_position_embeddings=512 \
  --teacher_type_vocab_size=2 \
  --teacher_vocab_size=30522 \
  --teacher_attention_probs_dropout_prob=0.1 \
  --teacher_hidden_dropout_prob=0.1 \
  --teacher_hidden_size_per_head=64 \
  --teacher_hidden_size=768 \
  --learning_rate=$learning_rate \
  --model_save_every_n_iter=50000 \
  --weight_decay_rate=$wd \
  --kd_alpha=0.8