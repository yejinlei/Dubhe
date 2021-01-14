nvidia-smi

# tqdm
# wikitext-103-raw, wikitext-2-raw
CORPUS_RAW='./data/wikitext-103-raw/wiki.train.raw'
VOCAB_FILE='./glue_ofrecord/vocab.txt'
OUTPUT_DIR='./data/pretrain_data_json'
GPU=0

CUDA_VISIBLE_DEVICES=$GPU python pregenerate_training_data.py \
  --train_corpus $CORPUS_RAW \
  --vocab_file $VOCAB_FILE \
  --do_lower_case \
  --epochs_to_generate 3 \
  --output_dir $OUTPUT_DIR

