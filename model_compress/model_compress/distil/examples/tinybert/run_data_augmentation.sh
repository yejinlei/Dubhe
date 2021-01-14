# Data augmentation aims to expand the task-specific training set.

nvidia-smi

PRETRAINED_MODEL='../../models/uncased_L-12_H-768_A-12_oneflow' # the BERT-base teacher model
VOCAB_FILE='../../src/glue_ofrecord/vocab.txt'

GLOVE_EMB='../../glove/glove.840B.300d.txt'
GLUE_DIR='../../data/glue_data'
TASK_NAME=SST-2

GPU=0
CUDA_VISIBLE_DEVICES=$GPU python3 data_augmentation.py \
  --model_load_dir=${PRETRAINED_MODEL} \
  --model_save_dir=./snapshots \
  --vocab_file $VOCAB_FILE \
  --do_lower_case \
  --glove_embs $GLOVE_EMB \
  --glue_dir $GLUE_DIR \
  --task_name $TASK_NAME \
  --log_dir=./log \
  --save_last_snapshot=True \
  --gpu_num_per_node=1 \
  --seq_length=512 \
  --num_hidden_layers=12 \
  --num_attention_heads=12 \
  --max_position_embeddings=512 \
  --type_vocab_size=2 \
  --vocab_size=30522 \
  --attention_probs_dropout_prob=0.1 \
  --hidden_dropout_prob=0.1 \
  --hidden_size_per_head=64