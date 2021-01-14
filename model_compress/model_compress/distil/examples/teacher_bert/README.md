# BERT教师模型
使用BERT在GLUE文本分类任务数据集上进行微调，作为知识蒸馏的教师模型。

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
预训练BERT模型下载地址：
链接: https://pan.baidu.com/s/1jfTUY7ygcZZOJzjfrgUL8Q 提取码: 6b87 

下载后放置在`model_compress/models/uncased_L-12_H-768_A-12_oneflow`
#### 3.1 训练
- 执行以下脚本进行微调教师模型：
    - DATA_ROOT: GLUE数据集总路径
    - dataset: 任务名
    - MODEL_SAVE_DIR: 模型保存路径
    - RESULT_DIR: 测试结果json文件保存路径 （如果为RESULT_DIR=""，则默认保存到模型保存路径下，results_eval.json）
    - SERVE_FOR_ONLINE: 模型是否用于上线 （默认SERVE_FOR_ONLINE='False'，如果SERVE_FOR_ONLINE='True'，则删除清理模型保存路径中的无关变量，如优化器参数等）

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
    
#### 3.2 测试
- 微调后，可以执行以下脚本对教师模型进行测试：
    - DATA_ROOT: GLUE数据集总路径
    - dataset: 任务名
    - TEACHER_MODEL_DIR: 教师模型路径
    - RESULT_DIR: 测试结果json文件保存路径 （如果为RESULT_DIR=""，则默认保存到模型保存路径下，results_eval.json）

    ```bash
    bash run_eval_teacher.sh
    ```