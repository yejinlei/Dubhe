# 知识蒸馏快速上手

## 1. 简介
知识蒸馏：通过一些优化目标从大型、知识丰富的teacher模型学习一个小型的student模型

炼知技术平台提供了4个知识蒸馏相关算子，以及众多基于Oneflow算子复现的知识蒸馏模型和使用示例。
<table>
<thead>
  <tr>
    <th>类型</th>
    <th>知识蒸馏模型</th>
    <th><a href="../../docs/API_knowledge_distill.md" target="_blank">主要算子</a></th>
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
    <td><a href="theseus/README.md" target="_blank">链接</a></td>
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

以上脚本将会默认下载所有GLUE任务数据集，也可以通过'--tasks=TASKS'，指定下载某些数据集

也可以在这里下载GLUE任务数据集，并放置到相关目录(data/glue_data)下
链接: https://pan.baidu.com/s/1Im0uQM_V_zXkmQuHsFae0A 提取码: u64u

参考[加载与准备OneFlow数据集](https://github.com/Oneflow-Inc/oneflow-documentation/blob/master/cn/docs/extended_topics/how_to_make_ofdataset.md)，制作OFRecords数据集。或者执行以下命令，生成OFRecords数据集:
```
bash glue_process.sh
```

**或者直接下载转换后的OFRecords GLUE数据集，并放置到相关目录(data/glue_ofrecord)下：**
链接: https://pan.baidu.com/s/1CY2BfCGBZEeo1EgY5JQcuA 提取码: v2h4 

### 2.3 微调教师模型
预训练BERT模型下载地址：
链接: https://pan.baidu.com/s/1jfTUY7ygcZZOJzjfrgUL8Q 提取码: 6b87 

下载后放置在`./models/uncased_L-12_H-768_A-12_oneflow`
#### 2.3.1 训练
- 执行以下脚本进行微调教师模型：
    - DATA_ROOT: GLUE数据集总路径
    - dataset: 任务名
    - MODEL_SAVE_DIR: 模型保存路径
    - RESULT_DIR: 测试结果json文件保存路径 （如果为RESULT_DIR=""，则默认保存到模型保存路径下，results_eval.json）
    - SERVE_FOR_ONLINE: 模型是否用于上线 （默认SERVE_FOR_ONLINE='False'，如果SERVE_FOR_ONLINE='True'，则删除清理模型保存路径中的无关变量，如教师模型参数和优化器参数等等）

    ```bash
    bash run_train_teacher.sh
    ```
- 我们微调过的教师模型可以在这里下载： 链接: https://pan.baidu.com/s/1jiOTSPBmmBoij0UwPO6UKw 提取码: 9xkp
    - 已在SST-2,QQP,MRPC,RTE,CoLA数据集上微调
- 并放置到`"model_compress/distil/models/finetuned_teacher/"`。
- 在上述数据集的dev集上性能为SST-2: 92.2%, QQP: 91.1%, MRPC: 89.2%, RTE: 69.8%, CoLA: 58.5%
- 评价指标：
    - Accuracy: SST-2, MRPC, QQP, RTE
    - MCC (Matthews correlation coefficient): CoLA

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
- TMP_STUDENT_DIR: 临时学生模型路径（如果需要的话，不需要则设为TMP_STUDENT_DIR=""）
- STUDENT_DIR: 学生模型保存路径
- RESULT_DIR: 测试结果json文件保存路径 （如果RESULT_DIR=""，则默认保存到模型保存路径下，results_eval.json）
- SERVE_FOR_ONLINE: 模型是否用于上线 （默认SERVE_FOR_ONLINE='False'，如果SERVE_FOR_ONLINE='True'，则删除清理模型保存路径中的无关变量，如教师模型参数和优化器参数等等）

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
      > 临时学生模型下载链接（SST-2, RTE, MRPC, CoLA, QQP数据集） 链接: https://pan.baidu.com/s/17F8KVsLd_lMODLaVLc7yrQ 提取码: 95ir 
      > 下载并解压，将相应的模型放置到`"./models/student_model/bert_pkd_3"`路径下
    - TinyBERT
        ```bash
        bash run_train_student_tinybert.sh
        ```
      > 临时学生模型(通用TinyBERT)下载链接 链接: https://pan.baidu.com/s/1vZDILxXi-uxo2v3zFlWL3A 提取码: kpia 

> BERT类模型最大序列长度设为128; LSTM类模型最大序列长度设为32，词表大小为10000

#### 2.4.2 测试
执行以下脚本进行测试：
- DATA_ROOT: GLUE数据集总路径
- dataset: 任务名
- STUDENT_DIR: 学生模型保存路径，蒸馏过的学生模型下载链接如下（SST-2数据集）
- RESULT_DIR: 测试结果json文件保存路径 （如果RESULT_DIR=""，则默认保存到模型保存路径下，results_eval.json）

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
      


