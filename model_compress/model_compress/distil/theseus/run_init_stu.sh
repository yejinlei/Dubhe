# Data augmentation aims to expand the task-specific training set.
BERT_BASE_DIR="/remote-home/my/Projects/bert_theseus/BERT/log/MRPC_uncased_L-12_H-768_A-12_oneflow_v1/snapshot_last_snapshot"
TMP_STUDENT_DIR='/remote-home/my/Projects/bert_theseus/BERT-theseus/BERT-theseus/log/MRPC_init_student'
LAYER_LIST="0,1,2,3,4,5"
python3 init_stu.py \
  --teacher_model=${BERT_BASE_DIR} \
  --student_model=${TMP_STUDENT_DIR} \
  --layer_list=${LAYER_LIST}