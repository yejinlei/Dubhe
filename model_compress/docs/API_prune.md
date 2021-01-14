1. 通道剪枝算子
=========

## 1.1 "bn"剪枝算子

- `get_pruneThre_bn():`卷积层对应的BN层的gamma参数作为缩放因子，获得剪枝对应阈值
  - [源代码](../model_compress/ChannelSlimming/prune/util/prune_algorithm.py#L120)
  - **返回**：剪枝对应的阈值

- `get_removeIndex_bn(a, thre):`根据阈值获得当前卷积层需要剪枝的通道index
  - [源代码](../model_compress/ChannelSlimming/prune/util/prune_algorithm.py#L182)
  - **参数**：
    - **a**：当前卷积层的参数
    - **thre**：`get_pruneThre_bn()`返回的阈值

1.2 "conv_avg"剪枝算子
---------

- `get_pruneThre_conv_avg():`卷积层参数的平均值作为缩放因子，获得剪枝对应阈值
  - [源代码](../model_compress/ChannelSlimming/prune/util/prune_algorithm.py#L54)
  - **返回**：剪枝对应的阈值

- `get_removeIndex_conv_avg(a, shape, thre):`根据阈值获得当前卷积层需要剪枝的通道index
  - [源代码](../model_compress/ChannelSlimming/prune/util/prune_algorithm.py#L187)
  - **参数**：
    - **a**：当前卷积层的参数
    - **shape**：当前卷积层的shape信息
    - **thre**：`get_pruneThre_conv_avg()`返回的阈值

## 1.3 "conv_max"剪枝算子

- 同"conv_avg"剪枝算子

## 1.4 "conv_all"剪枝算子

- 同"conv_avg"剪枝算子

1.5 "random"剪枝算子
---------

- `get_removeIndex_conv_avg(shape):`随机选择需要剪枝的通道index
  - [源代码](../model_compress/ChannelSlimming/prune/util/prune_algorithm.py#L220)
  - **参数**：
    - **shape**：当前卷积层的shape信息

1.6 "dnn"剪枝算子
---------

- `get_pruneThre_fc():`全连接层的神经元的参数的平均值作为缩放因子，获得剪枝对应阈值
  - [源代码](../model_compress/ChannelSlimming/prune/util/prune_algorithm.py#137)
  - **返回**：剪枝对应的阈值

- `get_removeIndex_fc(a, shape, thre):`根据阈值获得当前全连接层需要剪枝的神经元index
  - [源代码](../model_compress/ChannelSlimming/prune/util/prune_algorithm.py#L171)
  - **参数**：
    - **a**：当前全连接层的参数
    - **shape**：当前全连接层的shape信息
    - **thre**：`get_pruneThre_fc()`返回的阈值

2. 模型调用算子
=========

## 2.1 pruneDnn.py

- DNN模型剪枝，可调用1.6剪枝算子
- [文件](../model_compress/ChannelSlimming/prune/pruneDnn.py)

## 2.2 pruneLenet.py

- CNN模型的lenet模型剪枝，可调用1.1-1.5剪枝算子
- [文件](../model_compress/ChannelSlimming/prune/pruneLenet.py)

## 2.3 pruneAlexnet.py

- CNN模型的lenet模型剪枝，可调用1.1-1.5剪枝算子
- [文件](../model_compress/ChannelSlimming/prune/pruneAlexnet.py)

## 2.4 pruneVggnet.py

- CNN模型的lenet模型剪枝，可调用1.1-1.5剪枝算子
- [文件](../model_compress/ChannelSlimming/prune/pruneVggnet.py)

## 2.5 pruneResnet.py

- CNN模型的lenet模型剪枝，可调用1.1-1.5剪枝算子
- [文件](../model_compress/ChannelSlimming/prune/pruneResnet.py)