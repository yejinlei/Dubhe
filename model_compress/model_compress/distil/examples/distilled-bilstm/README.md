# Distilled-BiLSTM
["Distilling task-specific knowledge from bert into simple neural networks"](https://arxiv.org/abs/1903.12136)论文的实现

Distilled BiLSTM的教师模型采用微调过的BERT，学生模型采用简单神经网络LSTM。
蒸馏的目标是KD loss，即仅使用软标签进行蒸馏，将BERT中的知识蒸馏到LSTM中。

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

## 2. 数据获取
如何获取数据请查阅[这里](../../README.md#22-数据获取)

## 3. 微调教师模型
如何微调教师模型请查阅[这里](../../README.md#23-微调教师模型)
  
## 4. 蒸馏到学生模型
### 4.1 训练
执行以下脚本将教师模型蒸馏到学生模型：
- DATA_ROOT: GLUE数据集总路径
- dataset: 任务名
- FT_BERT_BASE_DIR: 在特定任务上微调过的教师模型路径
- STUDENT_DIR: 学生模型保存路径
- RESULT_DIR: 测试结果json文件保存路径 （如果为RESULT_DIR=""，则默认保存到模型保存路径下，results_eval.json）
- SERVE_FOR_ONLINE: 模型是否用于上线 （默认SERVE_FOR_ONLINE='False'，如果SERVE_FOR_ONLINE='True'，则删除清理模型保存路径中的无关变量，如教师模型参数和优化器参数等等）

> 最大序列长度为32，词表大小为10000

```bash
bash run_train_student_distilled_lstm.sh
```

### 4.2 测试
蒸馏过的学生模型下载链接如下（SST-2数据集）:

下载链接: https://pan.baidu.com/s/1M4XzB2DnLikglxVFvhnYpw  提取码: hqhj

执行以下脚本进行测试：
- DATA_ROOT: GLUE数据集总路径
- dataset: 任务名
- STUDENT_DIR: 学生模型保存路径
- RESULT_DIR: 测试结果json文件保存路径 （如果为RESULT_DIR=""，则默认保存到模型保存路径下，results_eval.json）

```bash
bash run_eval_student_distilled_lstm.sh
```
### 4.3 结果
在SST-2 DEV数据集上: 
- 模型精度：教师模型acc 92.2% ->学生模型acc 82.9%
- 模型尺寸：教师模型110M -> 学生模型 15.3M   (↓7.5x)
- 推理耗时：教师模型4.04s -> 0.83s   (↓4.8x)