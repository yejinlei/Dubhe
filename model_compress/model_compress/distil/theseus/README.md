# 知识蒸馏快速上手

## 1. 简介
知识蒸馏：通过一些优化目标从大型、知识丰富的teacher模型学习一个小型的student模型

炼知技术平台提供了4个知识蒸馏相关算子，以及众多基于Oneflow算子复现的知识蒸馏模型和使用示例。
<table>
<thead>
  <tr>
    <th>类型</th>
    <th>知识蒸馏模型</th>
    <th><a href="../../../docs/API_knowledge_distill.md" target="_blank">主要算子</a></th>
    <th>使用文档</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td rowspan="2">软标签蒸馏</td>
    <td>KD</td>
    <td>软标签蒸馏</td>
    <td><a href="./examples/knowledge_distillation/README.md" target="_blank">链接</a></td>
  </tr>
  <tr>
    <td>Distilled-BiLSTM</td>
    <td>软标签蒸馏，将BERT蒸馏到BiLSTM</td>
    <td><a href="./examples/distilled-bilstm/README.md" target="_blank">链接</a></td>
  </tr>
  <tr>
    <td rowspan="2">从其他知识蒸馏</td>
    <td>BERT-PKD</td>
    <td>软标签蒸馏+层与层蒸馏</td>
    <td><a href="./examples/bert-pkd/README.md" target="_blank">链接</a></td>
  </tr>
  <tr>
    <td>TinyBERT</td>
    <td>软标签蒸馏+层与层蒸馏+注意力蒸馏</td>
    <td><a href="./examples/tinybert/README.md" target="_blank">链接</a></td>
  </tr>
  <tr>
    <td>模块替换</td>
    <td>BERT-Theseus</td>
    <td>依照概率替换原有的BERT模块和Theseus的模块组成新的模型来训练</td>
    <td><a href="./examples/xxx/README.md" target="_blank">链接</a></td>
  </tr>
</tbody>
</table>

## 2. 使用
### 2.1 依赖
- Python 3.6
- oneflow-cu101 0.1.10
- numpy 1.19.2

完整的环境可以通过以下命令安装：
  ```bash
conda create -n distil python=3.6
  ```

  ```
python3 -m pip install --find-links https://oneflow-inc.github.io/nightly oneflow_cu101 --user
  ```
    
### 2.2 数据获取
知识蒸馏主要针对NLP相关的任务，炼知平台在GLUE任务的数据集上对不同算法进行了测试。

可以通过执行以下脚本下载GLUE任务的所有数据集，将会自动下载并解压到'--data_dir=data'目录下。

```
bash run_download_glue_data.sh
```
或者
```bash
python ../src/download_glue_data.py --data_dir data/glue_data --tasks all
```

TASKS = ["CoLA", "SST", "MRPC", "QQP", "STS", "MNLI", "SNLI", "QNLI", "RTE", "WNLI", "diagnostic"]

以上脚本将会默认下载所有BLUE任务数据集，也可以通过'--tasks=TASKS'，指定下载某些数据集

参考[加载与准备OneFlow数据集](https://github.com/Oneflow-Inc/oneflow-documentation/blob/master/cn/docs/extended_topics/how_to_make_ofdataset.md)，制作OFRecords数据集。或者执行以下命令，生成OFRecords数据集:
```
bash glue_process.sh
```

**或者直接下载转换后的OFRecords GLUE数据集，并放置到相关目录(data/glue_ofrecord)下：**
链接: https://pan.baidu.com/s/1TuDJpJ8z9zJvvhqjjXiGDg 提取码: phyf 


### 2.3 微调教师模型
预训练BERT模型下载地址：
链接: https://pan.baidu.com/s/1jfTUY7ygcZZOJzjfrgUL8Q 提取码: 6b87 

下载后放置在`./models/uncased_L-12_H-768_A-12_oneflow`
#### 2.3.1 训练
- 执行以下脚本进行微调教师模型：
    - DATA_ROOT: GLUE数据集总路径
    - dataset: 任务名
    - MODEL_SAVE_DIR: 模型保存路径

    ```bash
    bash run_train_teacher.sh
    ```
#### 2.3.2 测试
- 微调后，可以执行以下脚本对教师模型进行测试：
    - DATA_ROOT: GLUE数据集总路径
    - dataset: 任务名
    - TEACHER_MODEL_DIR: 教师模型路径

    ```bash
    bash run_eval_teacher.sh
    ```
  
### 2.4 蒸馏到学生模型
#### 2.4.1 训练
执行以下脚本将教师模型蒸馏到学生模型：
- DATA_ROOT: GLUE数据集总路径
- dataset: 任务名
- FT_BERT_BASE_DIR: 在特定任务上微调过的教师模型路径
- TMP_STUDENT_DIR: 临时学生模型路径（如果需要的话）
- STUDENT_DIR: 学生模型保存路径

- 不同知识蒸馏算法：
    - KD
        ```bash
        bash run_train_student_kd.sh
        ```
    - Distilled-BiLSTM
        ```bash
        bash run_train_student_distilled_lstm.sh
        ```
    - BERT-PKD
        ```bash
        bash run_train_student_bert_pkd.sh
        ```
      >注：BERT-PKD可以随机初始化，也可以选择根据教师BERT中间层进行初始化，详细步骤请查阅[这里](./examples/bert-pkd/README.md#41-教师模型中间层保存与转换)
    - TinyBERT
        ```bash
        bash run_train_student_tinybert.sh
        ```
      
    - BERT-of-Theseus
        ```bash
        bash run_bert_theseus.sh ${GPU_ID} ${SAVE_TAG} {PHRASE1_LR} ${PHRASE2_LR} ${PHRASE1_REPLACE_RATE} ${COMPRESS_RATIO}
        example: bash run_bert_theseus.sh 0 1 1e-5 1e-5 0.5 4
        ```
        - GPU_ID: 指定进行训练的 GPU 的 id
        - SAVE_TAG: 指定模型保存文件的特定标识符
        - PHRASE1_LR: BERT-of-Theseus第一阶段的学习率
        - PHRASE1_LR: BERT-of-Theseus第二阶段的学习率
        - PHRASE1_REPLACE_RATE: 第一阶段当中，BERT的模块替换为Theseus模块的概率
        - COMPRESS_RATIO: 压缩的比率，例如 COMPRESS_RATIO=4，会将12层的BERT-Base压缩为3层
        
        - BERT-of-Theses 需要在特定数据集上微调好的模型作为输入 
        - 修改 run_bert_theseus.sh 里 line 25 的 dataset=你需要的数据集，现在默认是 SST-2
        - 将特定数据集现象下面的 PRETRAINED_MODEL 和 BERT_BASE_DIR 都改成上面你微调好的模型所在的文件夹。
        
        - 默认的保存路径为: 
            - 第一阶段./log/${dataset}_bert_theseus_uncased_L-12_H-768_A-12_oneflow_v${SAVE_TAG}s1
            - 第一阶段./log/${dataset}_bert_theseus_uncased_L-12_H-768_A-12_oneflow_v${SAVE_TAG}s2
   
        
> BERT类模型最大序列长度设为128; LSTM类模型最大序列长度设为32，词表大小为10000

#### 2.4.2 测试
执行以下脚本进行测试：
- DATA_ROOT: GLUE数据集总路径
- dataset: 任务名
- STUDENT_DIR: 学生模型保存路径，蒸馏过的学生模型下载链接如下（SST-2数据集）

- 不同知识蒸馏算法：
    - KD
    
        下载链接: https://pan.baidu.com/s/1EgQyQgxAcFAG8Ch3-4VPaw 提取码: 5k9p 
        ```bash
        bash run_eval_student_kd.sh
        ```
    - Distilled-BiLSTM
    
        下载链接: https://pan.baidu.com/s/1M4XzB2DnLikglxVFvhnYpw  提取码: hqhj
        ```bash
        bash run_eval_student_distilled_lstm.sh
        ```
    - BERT-PKD
      - 从教师模型中间层初始化，下载链接: https://pan.baidu.com/s/1l7vXn-3U05Hzl0RXCJPiLg 提取码: 33dk
      - 随机初始化，下载链接: https://pan.baidu.com/s/1m46j57Tova_yaGLabAqUIw 提取码: pdx4
        ```bash
        bash run_eval_student_bert_pkd.sh
        ```

    - TinyBERT
    
        下载链接: https://pan.baidu.com/s/1nOAZHd3wLmyVw2vTJB7KfQ 提取码: ma65
        ```bash
        bash run_eval_student_tinybert.sh
        ```
    
    - BERT-of-Theseus
    
        ```bash
        bash eval_bert_theseus.sh ${GPU_ID} ${VERSION}
        example: bash eval_bert_theseus.sh 0 1s1
        ```


