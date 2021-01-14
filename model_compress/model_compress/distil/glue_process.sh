# Copyright (c) The Tianshu Platform Authors.
# Licensed under the Apache License

# --aug_train True
TASK=SST-2
python ./src/glue_ofrecord/glue_process.py --data_dir ./data/glue_data/${TASK} --output_dir ./data/glue_ofrecord_test/${TASK} \
    --vocab_file ./glue_ofrecord/vocab.txt --do_lower_case True --max_seq_length 128 \
    --do_train True --do_eval True --do_predict True  --task=${TASK}