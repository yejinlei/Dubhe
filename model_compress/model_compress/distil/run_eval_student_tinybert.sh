# Copyright (c) The Tianshu Platform Authors.
# Licensed under the Apache License

dataset=SST-2

# ofrecord dataset dir
DATA_ROOT=./data/glue_ofrecord

# which GPU to use
GPU=0

# saved student model dir
STUDENT_DIR="./models/student_model/SST-2/tinybert_epoch-4_lr-2e-5_wd-0.0001"
RESULT_DIR=""

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
else
  echo "dataset must be GLUE such as 'CoLA','MRPC','SST-2','QQP','MNLI','WNLI','STS-B',"
  exit
fi

train_data_dir=$DATA_ROOT/${dataset}/train
eval_data_dir=$DATA_ROOT/${dataset}/eval

CUDA_VISIBLE_DEVICES=$GPU  python3 ./examples/tinybert/task_student_tinybert.py \
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
  --loss_print_every_n_iter 10 \
  --log_dir=./log \
  --model_save_dir=${STUDENT_DIR} \
  --result_dir=${RESULT_DIR} \
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
  --model_save_every_n_iter=50000 \
  --intermediate_distill='True' \
  --pred_distill='True'