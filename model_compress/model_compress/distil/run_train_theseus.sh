# pretrained model dir
# ofrecord dataset dir
DATA_ROOT=$1

# saved student model dir
STUDENT_DIR="$2/student_v2"

# tran log out
TRAIN_LOG_DIR=$3

# inference json result out
RESULT_DIR=$4

BERT_BASE_DIR="/usr/local/output/model/before/snapshot_best"
#BERT_BASE_DIR="/usr/local/Oneflow-Model-Compression/model_compress/distil/models/SST-2_epoch-3_lr-2e-5_wd-0.0001/snapshot_best"
INIT_STUDENT_DIR="$2/student_init"
ONE_TRAIN_MODEL="$2/student_v1"

dataset=$5

train_data_dir=$DATA_ROOT/${dataset}/train
eval_data_dir=$DATA_ROOT/${dataset}/eval

# which GPU to use
GPU=0

#dataset=MRPC
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
  learning_rate=1e-5
  EPOCH=5
  wd=0.001
elif [ $dataset = "SST-2" ]; then
  train_example_num=67349
  eval_example_num=872
  test_example_num=1821
  learning_rate=1e-5
  EPOCH=5
  wd=0.0001
elif [ $dataset = "QQP" ]; then
  train_example_num=363849
  eval_example_num=40430
  test_example_num=0
  learning_rate=1e-5
  EPOCH=5
  wd=0.0001
elif [ $dataset = "MNLI" ]; then
  train_example_num=392702
  eval_example_num=9815
  test_example_num=0
  learning_rate=1e-5
  EPOCH=5
  wd=0.0001
elif [ $dataset = "WNLI" ]; then
  train_example_num=635
  eval_example_num=71
  test_example_num=0
  learning_rate=1e-5
  EPOCH=5
  wd=0.0001
elif [ $dataset = "RTE" ]; then
  train_example_num=2490
  eval_example_num=277
  test_example_num=0
  learning_rate=1e-5
  EPOCH=5
  wd=0.0001
else
  echo "dataset must be GLUE such as 'CoLA','MRPC','SST-2','QQP','MNLI','WNLI','STS-B',"
  exit
fi

mkdir -p ${INIT_STUDENT_DIR}

# LAYER_LIST="0,1,2,3,4,5"
python3 ./theseus/init_stu.py \
  --teacher_model=${BERT_BASE_DIR} \
  --student_model=${INIT_STUDENT_DIR} \
  --layer_list="0,1,2"

mkdir -p ${ONE_TRAIN_MODEL}

CUDA_VISIBLE_DEVICES=$GPU python3 ./theseus/run_classifier.py \
  --do_train=True \
  --model=Glue_$dataset \
  --task_name=$dataset  \
  --gpu_num_per_node=1 \
  --num_epochs=${EPOCH} \
  --train_data_dir=$train_data_dir \
  --train_example_num=$train_example_num \
  --eval_data_dir=$eval_data_dir \
  --eval_example_num=$eval_example_num \
  --model_load_dir=${INIT_STUDENT_DIR} \
  --batch_size_per_device=32 \
  --eval_batch_size_per_device=4 \
  --loss_print_every_n_iter 20 \
  --log_dir=${TRAIN_LOG_DIR} \
  --result_dir=${RESULT_DIR} \
  --model_save_dir=${ONE_TRAIN_MODEL} \
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
  --weight_decay_rate $wd \
  --compress_ratio=4 \
  --replace_prob=0.5 \
| tee -a ${ONE_TRAIN_MODEL}/train_log.txt

mkdir -p ${STUDENT_DIR}

CUDA_VISIBLE_DEVICES=$GPU python3 ./theseus/run_classifier.py \
  --do_train=True \
  --model=Glue_$dataset \
  --task_name=$dataset  \
  --gpu_num_per_node=1 \
  --num_epochs=${EPOCH} \
  --train_data_dir=$train_data_dir \
  --train_example_num=$train_example_num \
  --eval_data_dir=$eval_data_dir \
  --eval_example_num=$eval_example_num \
  --model_load_dir=${ONE_TRAIN_MODEL}/snapshot_last_snapshot \
  --batch_size_per_device=32 \
  --eval_batch_size_per_device=4 \
  --loss_print_every_n_iter 200 \
  --log_dir=${TRAIN_LOG_DIR} \
  --result_dir=${RESULT_DIR} \
  --model_save_dir=${STUDENT_DIR} \
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
  --learning_rate=1e-5 \
  --weight_decay_rate $wd \
  --compress_ratio=4 \
  --replace_prob=1.0 \
 | tee -a ${STUDENT_DIR}/train_log.txt
