#
FT_BERT_BASE_DIR="./models/finetuned_teacher/SST-2_epoch-3_lr-2e-5_wd-0.0001/snapshot_last_snapshot"
TMP_STUDENT_DIR='./models/student_model/bert_pkd_3/SST-2'
LAYER_LIST="2,6,10"
python3 bert-pkd_generate_student_model.py \
  --teacher_model=${FT_BERT_BASE_DIR} \
  --student_model=${TMP_STUDENT_DIR} \
  --layer_list=${LAYER_LIST}