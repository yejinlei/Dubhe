# 通道剪枝快速上手

## 1. 简介

通道剪枝：剪去DNN模型或者CNN模型的一些冗余的参数通道，来获得更小的模型和更快的结果

炼知技术平台提供了7个通道剪枝相关算子，以及众多基于Oneflow算子复现的通道剪枝模型和使用示例。

| 类型    | 通道剪枝算子         | 算子介绍                                                     |
| ------- | -------------------- | ------------------------------------------------------------ |
| DNN剪枝 | 神经元权重剪枝       | 以DNN神经网络的神经元训练参数的平均值作为剪枝权重，根据用户设置的剪枝率，减去权重较小的神经元 |
| CNN剪枝 | BN层剪枝             | 以CNN神经网络的BN层gamma参数作为剪枝权重，根据用户设置的剪枝率，减去权重较小的卷积通道（卷积核） |
| CNN剪枝 | 卷积层权重平均剪枝   | 以CNN神经网络的卷积层参数的平均值作为剪枝权重，根据用户设置的剪枝率，减去权重较小的卷积通道（卷积核） |
| CNN剪枝 | 卷积层权重总和剪枝   | 以CNN神经网络的卷积层参数的总和作为剪枝权重，根据用户设置的剪枝率，减去权重较小的神卷积通道（卷积核） |
| CNN剪枝 | 卷积层权重最大值剪枝 | 以CNN神经网络的卷积层参数的最大值作为剪枝权重，根据用户设置的剪枝率，减去权重较小的卷积通道（卷积核） |
| CNN剪枝 | 随机剪枝             | 根据用户设置的剪枝率，随机选取卷积通道（卷积核）进行剪枝     |
| CNN剪枝 | 卷积层阈值剪枝       | 计算CNN神经网络的卷积层参数中大于阈值的个数，将此个数作为剪枝的权重，根据用户设置的剪枝率，减去权重较小的卷积通道（卷积核） |

## 2. 使用

### 2.1 依赖及安装

- CUDA Version 10.1.243

- CUDA Driver Version: 418.56

- oneflow_cu101
- numpy > 1.17.0
- 可通过以下命令安装

```
python3 -m pip install --find-links https://oneflow-inc.github.io/nightly oneflow_cu101 --user
# 若找不到对应版本，升级pip
python3 -m pip install --upgrade --user pip
# 运行关于numpy的报错，例如module 'numpy.random' has no attribute 'default_rng'
# 由于numpy版本低于1.17.0，升级numpy
python3 -m pip install --upgrade --user numpy
```

### 2.2 数据获取

- 数据集需转换成oneflow格式，存放在**ofData文件夹**下
- 通道剪枝主要针对CV相关的任务，数据集需处理成oneflow格式，此任务提供了默认的数据集：Cifar10、Cifar100、mnist分类数据集。可从以下链接直接下载of格式数据集，并放至ofData数据集中：https://pan.baidu.com/s/1fj0DuQM6342CWx2DrMJGhQ（提取码：r8qx）
- 若要使用使用自定义数据集，使用方法见**2.3 运行**下的**使用自己数据集**

### 2.3 运行

- **默认运行（训练基模型、剪枝、微调）**

  ```
  # cifar10数据集、alexnet模型
  python run.py
  ```

- 运行结果见**output文件夹**，文件夹结构说明见**2.4 文件说明**

- 运行过程的日志见**log文件夹**，文件夹结构说明见**2.4 文件说明**

- **改变默认数据集（可选mnist、cifar10、cifar100）**

  ```
  python run.py --data_type=mnist
  ```

- **改变默认模型（可选dnn_2、dnn_4、lenet、alexnet、vgg、resnet）**

  ```
  python run.py --model=lenet
  ```

- **改变剪枝率**
  
  ```
  python run.py --percent=0.5
  ```
  
- **改变剪枝算子**
  
  ```
  # dnn剪枝不需要此参数，默认权重剪枝
  # cnn剪枝算子有bn、conv_avg、conv_all、conv_max、random、conv_threshold
  python run.py --prune_method=bn
  ```
  
- **改变更多参数**
  
  - 见下面**2.4 文件说明**中**train_val.py文件**
  
- **使用自己数据集（以randomData255为例）**
  - 数据集示例见myData下的randomData255，里面train.json包含了2张3\*32\*32大小的图片，test.json包含了2张3\*32\*32大小的图片

  - 创建自己的数据集文件夹在**myData**文件夹下，文件夹名为数据集的名字**randomData255**

  - randomData255文件夹中有两个文件：train.json和test.json，介绍如下

    - **train.json**
      - 存储为一个字典，字段有data、label、shape
      - data为二维数组，如randomData255数据集为2张3\*32\*32大小的图片，则data维度为2\*3027，3027是3\*32\*32的展开，图片的像素值范围为 [0, 255]
      - label为一维数组，代表每张图片的类别，randomData255数据集中长度为2，是一个2维度的向量
      - shape为一维数组，长度为3，第一个是通道数，第二三个为像素（需相等），如 [3, 32, 32]
    - **test.json**
      - 存储为一个字典，和train.json相似，字段有data、label，没有shape字段

  - 在ofrecordMake.py文件夹下运行：

    ```
    # randomData255换成自己的数据集名称，制作完成的数据集见ofData文件夹
    python ofrecordMake.py --dataName=randomData255
    ```

  - 基模型训练、剪枝、微调，运行：

    ```
    # randomData255换成自己的数据集名称
    python run.py --data_type=randomData255
    ```

- **自定义步骤step**

  - 1代表训练基模型；2代表剪枝、3代表微调，默认step=123

  - 只运行训练基模型

    ```
    python run.py --step=1
    ```

  - 只运行剪枝：

    ```
    # 在output/snapshots中对应位置需要有model_base，位置介绍见下面output文件夹介绍
    python run.py --step=2
    ```

  - 只运行微调：

    ```
    # 在./output/snapshots中对应位置需要有model_prune
    python run.py --step=3
    ```

  - 运行训练基模型、剪枝

    ```
    python run.py --step=12
    ```

    

### 2.4 文件说明

- **py文件说明**
  - **run.py**

    - 自动化调用train.py来进行训练和微调，以及prune剪枝

    - 大部分参数设置为默认值，可以自动调整部分参数

    - 部分参数包括：model、data_type、prune_method、percent

    - 示例

      ```
      python run.py --model alexnet --data_type=cifar10 --prune_method=bn --step=123
      ```

- **train_val.py**
  - 训练train和微调refine模型的主函数
  - 可以自己调整所有参数，参数列表见**2.5 config参数**
  - 示例见run_dnn2_cifar10.sh、run_alexnet_cifar10.sh（bn剪枝算法）
- **ofrecordMake.py**
  
- 制作自定义数据集
  
- **文件夹说明**

  - **log文件夹**
    - 日志文件夹，存储不同模型和数据的日志log文件，记录每个epoch在test数据集上的top1准确率、topk准确率、运行速度。
    - 如"log_vgg_cifar10_base_model.txt"：vgg模型-cifar10数据集-baseline模型训练的log记录。

  - **model文件夹**
    - **cnn文件夹**
      - lenet_model.py：LeNet模型
      - alexnet_model.py：AlexNet模型
      - vgg_model.py：VggNet模型
      - resnet_model.py：ResNet模型
    - **dnn文件夹**
      - dnn_model：Dnn模型，包括两层Dnn模型dnn_2、四层Dnn模型dnn_4

  - **util文件夹**
    -  config.py：命令行参数配置文件
    - job_function_util.py：job function相关config
    - model_weight.py：模型加载、保存等相关函数
    - ofrecord_util.py：数据集加载函数
    - optimizer_util.py：model相关config
    - util.py：加载cfg,data,snapshot,summary等函数

  - **prune文件夹**
    - util文件夹
      - 存放model_weight.py文件，模型加载、保存等相关函数
      - 存放prune_algorithm.py文件，剪枝的不同算法
    - 不同模型下的剪枝算法

  - **ofData文件夹**
    - 存放of格式的数据

  - **output文件夹**
    - 模型输出文件夹，模型文件存储在snapshots文件夹下
    - 按照模型分别存放，各自模型下按照数据集存放，各自数据集下分为基模型model_base、剪枝模型model_prune、微调模型model_refine。


### 2.5 config参数

- --dtype=float32：训练过程中参数的类型
- --gpu_num_per_node=1：每个训练节点的GPU数量
- --num_nodes = 1：训练节点个数
- --model=vgg：训练中的模型（vgg、lenet、alexnet、dnn_2、dnn_4）
- --data_type='imageNet'：加载的数据集（imageNet / cifar10）
- --log_type==base_model：写log日志的类型（base_model / prune_model）
- --default_dir=train：使用默认地址来加载和保存模型（推荐）'train'或者'refine'
- --model_load_dir='xxxxxx'：自己指定模型加载地址（使用default_dir则不需要此项）
- --model_save_dir='xxxxxx'：自己指定模型保存地址（使用default_dir则不需要此项）
- --batch_size_per_device=32：train中每个设备的batch_size（乘上gpu_num_per_node和num_nodes就是train_batch_size）
- --val_batch_size_per_device=32：test中每个设备的batch_size（乘上gpu_num_per_node和num_nodes就是test_batch_size）
- --num_classes=1000：分类数据集的类别个数
- --num_epochs=1：epoch的个数
- --num_examples=64000：决定train中的iter个数（除以train_batch_size就是iter个数）
- --num_val_examples=50000：决定test中的iter个数（除以test_batch_size就是iter个数）
- --rgb_mean=[123.68, 116.779, 103.939]：图片归一预处理时的均值
- --rgb_std=[58.393, 57.12, 57.375]：图片归一预处理时的方差
- --image_shape=[3, 224, 224]：图片的channel、height、width
- --log_dir='./output'：log信息的保存地址
- --result_dir='./output': results json保存地址。results json文件名格式为：args.result_dir, "results_"+args.model+'_'+args.data_type+'_'+args.log_type+"_{}.json".format(self.desc))
- --loss_print_every_n_iter=1：每n个iter输出一次loss、accuracy、speed信息
- --model_save_every_n_epoch=10：每n个epoch保存一次模型
- --image_size=224：图片大小
- --train_data_dir='./ofData/imageNet/train'：训练数据集的目录
- --train_data_part_num=30：训练数据集的part数（part0000-part00xx）
- --val_data_dir='./ofData/imageNet/test'：测试数据集的目录
- --val_data_part_num=2：测试数据集的part数（part0000-part00xx）
- --model_update='momentum'：训练的优化器（'momentum' / 'adam' / 'sgd'）
- --learning_rate=0.01：学习率
- --prune_method=bn：剪枝算法(bn、conv_avg、conv_all、conv_max、random、conv_similarity、bn_similarity、conv_threshold、以及不需要此参数的dnn剪枝)
- --step=123：剪枝步骤，1代表训练基模型；2代表剪枝、3代表微调，默认step=123







