# Copyright (c) The Tianshu Platform Authors.
# Licensed under the Apache License

# ofrecord dataset dir
DATA_ROOT=./data/glue_ofrecord

# choose dateset `CoLA`, `MRPC` 'SST-2'
dataset=SST-2

# which GPU to use
GPU=0

if [ $dataset = "CoLA" ]; then
  train_example_num=8551
  eval_example_num=1043
  test_example_num=1063
elif [ $dataset = "MRPC" ]; then
  train_example_num=3668
  eval_example_num=408
  test_example_num=1725
elif [ $dataset = "SST-2" ]; then
  train_example_num=67349
  eval_example_num=872
  test_example_num=1821
elif [ $dataset = "QQP" ]; then
  train_example_num=363849
  eval_example_num=40430
  test_example_num=0
elif [ $dataset = "MNLI" ]; then
  train_example_num=392702
  eval_example_num=9815
  test_example_num=0
elif [ $dataset = "WNLI" ]; then
  train_example_num=635
  eval_example_num=71
  test_example_num=0
elif [ $dataset = "RTE" ]; then
  train_example_num=2490
  eval_example_num=277
  test_example_num=0
elif [ $dataset = "QNLI" ]; then
  train_example_num=104743
  eval_example_num=5463
  test_example_num=0
else
  echo "dataset must be GLUE such as 'CoLA','MRPC','SST-2','QQP','MNLI','WNLI','',"
  exit
fi

TEACHER_MODEL_DIR="./models/finetuned_teacher/SST-2_epoch-3_lr-2e-5_wd-0.0001/snapshot_best"
#TEACHER_MODEL_DIR="./models/finetuned_teacher/RTE_epoch-5_lr-3e-5_wd-0.0001/snapshot_best"
#TEACHER_MODEL_DIR="./models/finetuned_teacher/MRPC_epoch-5_lr-1e-5_wd-0.001/snapshot_best"
#TEACHER_MODEL_DIR="./models/finetuned_teacher/CoLA_epoch-5_lr-1e-5_wd-0.01/snapshot_best"
#TEACHER_MODEL_DIR="./models/finetuned_teacher/QQP_epoch-5_lr-2e-5_wd-0.0001/snapshot_best"

RESULT_DIR="./models/finetuned_teacher/SST-2_epoch-3_lr-2e-5_wd-0.0001/snapshot_best"

train_data_dir=$DATA_ROOT/${dataset}/train
eval_data_dir=$DATA_ROOT/${dataset}/eval

CUDA_VISIBLE_DEVICES=$GPU python3 examples/teacher_bert/task_teacher.py \
  --do_train='False' \
  --do_eval='True' \
  --model=Glue_$dataset \
  --task_name=$dataset  \
  --gpu_num_per_node=1 \
  --train_data_dir=$train_data_dir \
  --train_example_num=$train_example_num \
  --eval_data_dir=$eval_data_dir \
  --eval_example_num=$eval_example_num \
  --batch_size_per_device=32 \
  --eval_batch_size_per_device=32 \
  --loss_print_every_n_iter 20 \
  --log_dir=./log \
  --model_save_dir=${TEACHER_MODEL_DIR} \
  --result_dir=${RESULT_DIR} \
  --save_last_snapshot=False \
  --seq_length=128 \
  --num_hidden_layers=12 \
  --num_attention_heads=12 \
  --max_position_embeddings=512 \
  --type_vocab_size=2 \
  --vocab_size=30522 \
  --attention_probs_dropout_prob=0.1 \
  --hidden_dropout_prob=0.1 \
  --hidden_size_per_head=64