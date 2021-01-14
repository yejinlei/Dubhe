# Knowledge Distillation
["Distilling the knowledge in a neural network"](https://arxiv.org/abs/1503.02531)论文的实现

KD的思路是使用教师模型的softmax层输出logits作为“soft target”，使得student模型可以学习teacher模型的输出，达到student模型模仿teacher模型在预测层的表现的目的。

L_KD = αL_CE+(1-α)L_DS
- L_CE 为学生模型的输出logits和label的交叉熵。
- L_DS 为学生模型输出logits和教师模型输出logits的距离，比如可以用软softmax或者KL散度等计算。
- α用来调节两个loss的权重。

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

```bash
bash run_train_student_kd.sh
```

### 4.2 测试
执行以下脚本进行测试：
- DATA_ROOT: GLUE数据集总路径
- dataset: 任务名
- STUDENT_DIR: 学生模型保存路径
- RESULT_DIR: 测试结果json文件保存路径 （如果为RESULT_DIR=""，则默认保存到模型保存路径下，results_eval.json）

蒸馏过的学生模型下载链接如下（SST-2数据集）:

下载链接: https://pan.baidu.com/s/1EgQyQgxAcFAG8Ch3-4VPaw 提取码: 5k9p 
```bash
bash run_eval_student_kd.sh
```
### 4.3 结果
在SST-2 DEV数据集上: 
- 模型精度：教师模型acc 92.2% ->学生模型acc 80.5%
- 模型尺寸：教师模型110M -> 学生模型 14.5M   (↓7.5x)
- 推理耗时：教师模型4.04s -> 0.81s   (↓5.0x)