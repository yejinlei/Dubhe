# TinyBERT
["TinyBERT: Distilling BERT for Natural Language Understanding"](https://arxiv.org/abs/1909.10351)论文的实现

## 1. 依赖
- Python 3.6
- oneflow-cu101 0.1.10

完整的环境可以通过以下命令安装：
```bash
conda create -n tinybert python=3.6
```

```bash
python3 -m pip install --find-links https://oneflow-inc.github.io/nightly oneflow_cu101 --user
```
> 注：以下操作时，根目录为`model_compress/distil`      
## 2. 通用蒸馏 (General Distillation，可选)
通用蒸馏阶段使用预训练得到的 BERT-base 为教师模型，在大规模文本语料上进行知识蒸馏得到通用的TinyBERT。
这个操作可以让TinyBERT学习到通用的语义表示，提高了模型的泛化能力，也为随后针对特定任务的蒸馏提供了一个很好的初始化。

通用蒸馏包含两步：
（1）语料预处理 （2）通用蒸馏

### 2.1 语料预处理
准备大规模语料，比如[WikiText-2 dataset](https://blog.einstein.ai/the-wikitext-long-term-dependency-language-modeling-dataset/)。可以用过如下命令下载：
```
cd data
wget https://s3.amazonaws.com/research.metamind.io/wikitext/wikitext-103-raw-v1.zip
unzip wikitext-103-raw-v1.zip
rm wikitext-103-raw-v1.zip
```
执行以下命令，进行训练数据预处理
- CORPUS_RAW：大规模语料，比如说Wikipedia
- BERT_BASE_DIR：教师模型类型
- OUTPUT_DIR: 处理过的语料保存路径
直接执行
```bash
bash run_pregenerate_training_data.sh
```
或者 执行
```bash
CORPUS_RAW='./data/wikitext-103-raw/wiki.train.raw'
BERT_BASE_DIR='bert-base-uncased'
OUTPUT_DIR='./data/pretrain_data_json'

python pregenerate_training_data.py \
  --train_corpus $CORPUS_RAW \
  --bert_model $BERT_BASE_DIR \
  --do_lower_case \
  --epochs_to_generate 3 \
  --output_dir $OUTPUT_DIR
```

### 2.2 通用蒸馏
将Pytorch的通用TinyBERT模型转为OneFlow的模型格式：
Pytorch版通用tinybert -> tensorflow版通用tinybert -> OneFlow版通用tinybert

#### Step1:
- 从[TinyBERT页面](https://github.com/huawei-noah/Pretrained-Language-Model/tree/master/TinyBERT)下载已经训练好的通用TinyBERT模型:

- 利用我们提供的convert_bert_pytorch_checkpoint_to_original_tf.py脚本，将其转为tensorflow模型格式。转换过程如下：

```
python convert_bert_pytorch_checkpoint_to_original_tf.py --model_name='./models/2nd_General_TinyBERT_4L_312D' --pytorch_model_path='./models/2nd_General_TinyBERT_4L_312D/pytorch_model.bin' --tf_cache_dir='./models/2nd_General_TinyBERT_4L_312D_tf'
```
- 再利用我们提供的convert_tf_ckpt_to_of.py脚本，将其转为oneflow模型格式。转换过程如下：

```
cd ./models/2nd_General_TinyBERT_4L_312D_tf/
cat > checkpoint <<ONEFLOW
model_checkpoint_path: "bert_model.ckpt" 
all_model_checkpoint_paths: "bert_model.ckpt" 
ONEFLOW
```
该命令将在解压目录下创建一个checkpoint文件，并写入以下内容：

model_checkpoint_path: "bert_model.ckpt" 
all_model_checkpoint_paths: "bert_model.ckpt" 

此时，已经准备好待转化的tensorflow模型目录，整个模型目录的结构如下：

```2nd_General_TinyBERT_4L_312D_tf
├── bert_config.json
├── bert_model.ckpt.data-00000-of-00001
├── bert_model.ckpt.index
├── checkpoint
└── vocab.txt
```
#### Step2:
我们接着使用convert_tf_ckpt_to_of.py将tensorflow模型转为OneFlow模型：

```
python convert_tf_ckpt_to_of.py \
  --tf_checkpoint_path ./models/2nd_General_TinyBERT_4L_312D_tf \
  --of_dump_path ./models/2nd_General_TinyBERT_4L_312D_oneflow
```

以上命令，将转化好的OneFlow格式的模型保存在`./2nd_General_TinyBERT_4L_312D_oneflow`目录下，供后续微调训练使用。


**你也可以直接下载我们提供的两种规模的通用TinyBERT： General_TinyBERT(4layer-312dim)和General_TinyBERT(6layer-768dim)**

下载地址如下：

链接: https://pan.baidu.com/s/1vZDILxXi-uxo2v3zFlWL3A 提取码: kpia 

将他们下载下来，放置在`'./models'`路径下，如`'./models/2nd_General_TinyBERT_4L_312D_oneflow'`和`'./models/2nd_General_TinyBERT_6L_768D_oneflow'`



## 3. 数据增强 (可选)
数据增强是TinyBERT中重要的一步个步骤，通过数据增强步骤，TinyBERT可以学习更多的任务相关的例子，可以进一步提高学生模型的泛化能力。可以帮助TinyBERT获得和BERT-base相匹配的性能，甚至在部分任务上超过BERT-base的表现。

### 3.1 GLUE数据集下载
可以通过执行以下脚本下载GLUE任务的所有数据集，将会自动下载并解压到'--data_dir=data'目录下。

```bash
python ../../src/download_glue_data.py --data_dir data/glue_data --tasks all
```

TASKS = ["CoLA", "SST", "MRPC", "QQP", "STS", "MNLI", "SNLI", "QNLI", "RTE", "WNLI", "diagnostic"]

以上脚本将会默认下载所有BLUE任务数据集，也可以通过'--tasks=TASKS'，指定下载某些数据集

参考[加载与准备OneFlow数据集](https://github.com/Oneflow-Inc/oneflow-documentation/blob/master/cn/docs/extended_topics/how_to_make_ofdataset.md)，制作OFRecords数据集。或者执行，生成OFRecords数据集:
```
bash glue_process.sh
```

**或者直接下载转换后的OFRecords GLUE数据集：**
链接: https://pan.baidu.com/s/1TuDJpJ8z9zJvvhqjjXiGDg 提取码: phyf 

### 3.2 下载GloVe嵌入
TinyBERT所采用的数据增强方法，结合了预训练BERT和GloVe嵌入来做词级别的替换。
可以同以下脚本下载GloVe嵌入，放置到'model_compress/distil/glove'目录下
```
cd glove
wget http://nlp.stanford.edu/data/glove.840B.300d.zip
unzip glove.840B.300d.zip 
rm glove.840B.300d.zip 
```

### 3.3 进行数据增强
通过执行以下脚本进行数据增强
``` bash
bash run_data_augmentation.sh
```
增强后的数据集 train_aug.tsv 会自动保存到相应的GLUE任务数据集下。


## 4. 任务特定蒸馏 (Task-specific Distillation)
在任务特定蒸馏中，将重新对得到的通用TinyBERT进行微调。通过在特定任务上进行微调，来进一步改进TinyBERT。任务特定化蒸馏包括三个步骤:
（1）微调教师BERT，随后（2）微调学生TinyBERT，包含层与层蒸馏、注意力蒸馏和软标签蒸馏。

### 4.1 微调教师模型BERT
预训练BERT模型下载地址：
- 链接: https://pan.baidu.com/s/1jfTUY7ygcZZOJzjfrgUL8Q 提取码: 6b87 
- 下载后放置在`./models/uncased_L-12_H-768_A-12_oneflow`

如何微调教师模型请查阅[这里](../../README.md#23-微调教师模型)
- 我们微调过的教师模型可以在这里下载： 链接: https://pan.baidu.com/s/1jiOTSPBmmBoij0UwPO6UKw 提取码: 9xkp
    - 已在SST-2,QQP,MRPC,RTE,CoLA数据集上微调
- 并放置到`"model_compress/distil/models/finetuned_teacher/"`。
- 在上述数据集的dev集上性能为SST-2: 92.2%, QQP: 91.1%, MRPC: 89.2%, RTE: 69.8%, CoLA: 58.5%
- 评价指标：
    - Accuracy: SST-2, MRPC, QQP, RTE
    - MCC (Matthews correlation coefficient): CoLA

### 4.2 微调学生模型TinyBERT
执行以下脚本将教师模型蒸馏到学生模型：
- DATA_ROOT: GLUE数据集总路径
- dataset: 任务名
- FT_BERT_BASE_DIR: 在特定任务上微调过的教师模型路径
- TMP_STUDENT_DIR: 临时学生模型路径
- STUDENT_DIR: 学生模型保存路径
- RESULT_DIR: 测试结果json文件保存路径 （如果为RESULT_DIR=""，则默认保存到模型保存路径下，results_eval.json）
- SERVE_FOR_ONLINE: 模型是否用于上线 （默认SERVE_FOR_ONLINE='False'，如果SERVE_FOR_ONLINE='True'，则删除清理模型保存路径中的无关变量，如教师模型参数和优化器参数等等）

直接执行
```bash
bash run_train_student_tinybert.sh
```

最终得到学生TinyBERT，可以从这里下载：
- 下载链接: https://pan.baidu.com/s/1nOAZHd3wLmyVw2vTJB7KfQ 提取码: ma65
- 并放置到`./models/student_model/SST-2/tinybert_epoch-4_lr-2e-5_wd-0.0001`

### 4.3 性能测试
通过执行以下脚本，在GLUE任务上进行性能测试：
- DATA_ROOT: GLUE数据集总路径
- dataset: 任务名
- STUDENT_DIR: 学生模型保存路径，蒸馏过的学生模型下载链接如下（SST-2数据集）
- RESULT_DIR: 测试结果json文件保存路径 （如果为RESULT_DIR=""，则默认保存到模型保存路径下，results_eval.json）

```bash
bash run_eval_student_tinybert.sh
```

### 4.4 结果:
在SST-2 DEV数据集上: 
- 模型精度：教师模型acc 92.2% ->学生模型acc 91.3%
- 模型尺寸：教师模型110M -> 学生模型 14.5M  (↓7.5x)
- 推理耗时：教师模型4.04s -> 0.65s   (↓6.2×)