# OneFlow 量化推理

## OneFlow 中的 XRT

XRT 是一个同时支持多个计算引擎的运行时加速库，目前已经集成了 TensorFlow XLA 和 NVIDIA
TensorRT 两个后端引擎。其中 XLA 全面支持训练和预测，TensorRT 支持预测以及部分算子支持训练。对于同一个计算图，XRT 允许多个计算引擎联合使用，以获得更好的加速效果，其中 TensorRT 具有 Int8 量化功能。

由于 TensorRT 中官方支持的 op 并没有那么全面，其余自定义 op 有可能受接口限制，因此 OneFlow 后续会采用 plug-in 形式添加，支持更多算子。

## 在 OneFlow 中使用 TensorRT

* 前期准备
	* 数据集：以测试 ResNet50 为例，需要提前准备 ImageNet 的 OFRecord 格式数据集。
	* 下载 TensorRT：编译时需要链接 TensorRT 的头文件和动态库，因此用户需要根据自己系统和已安装的 CUDA 版本选择相应版本的 TensorRT，同时满足 TensorRT 的其他依赖。
	* 下载 OneFlow-Benchmark：OneFlow-Benchmark 是 OneFlow 的模型基准仓库，提供了一系列完备实现的网络模型，本次测试选择的是其中的ResNet50。
* 编译：编译时开启 -DWITH_TENSORRT 选项，并指定 TensorRT 源码解压后的所在路径

```
cmake .. -DWITH_TENSORRT=ON -DTENSORRT_ROOT=/home/${user}/TensorRT-6.0.1.8 && make -j 24
```

或者可以在 cmake 前使用环境变量指定

```
export TENSORRT_ROOT=/home/${user}/TensorRT-6.0.1.8
```
编译成功后即可安装支持 TensoRT 的 OneFlow。

* 运行
目前 OneFlow 中的 TensorRT 仅支持单卡推理。编译成功后切换到 dev_trt_infer 分支，在 config.py 中
* 添加 --use\_tensorrt，可使用 TenosrRT 推理。
* 添加 --use\_tensorrt 和 use\_int8，可开启 TenosrRT 的 int8 量化。

## 环境

硬件环境

* CPU：Intel(R) Xeon(R) CPU E5-2650 v4 @ 2.20GHz x 6
* GPU：[GeForce GTX 1080] x 4

软件环境

* 系统：Ubuntu 18.04.4 LTS
* NVIDIA Driver Version：440.44
* CUDA：10.2
* GCC：7.5
* Cmake：3.14.4
* Make：4.1

测试结果

测试模型为 ResNet 50（以下称 rn50），使用在线量化，分别进行单机单卡和单机多卡推理，batch_size 取 64 和可运行的最大 batch_size。
若正常运行，log 打印如下：

```
==================================================================
Running resnet50: num_gpu_per_node = 1, num_nodes = 1.
==================================================================
dtype = float32
gpu_num_per_node = 1
num_nodes = 1
node_ips = ['127.0.0.1']
ctrl_port = 50051
model = resnet50
use_fp16 = None
use_xla = None
channel_last = None
pad_output = None
num_epochs = 1
model_load_dir = resnet_v15_of_best_model_val_top1_77318
batch_size_per_device = 64
val_batch_size_per_device = 256
nccl_fusion_threshold_mb = 0
nccl_fusion_max_ops = 0
fuse_bn_relu = False
fuse_bn_add_relu = False
gpu_image_decoder = False
image_path = test_img/tiger.jpg
num_classes = 1000
num_examples = 1281167
num_val_examples = 50000
rgb_mean = [123.68, 116.779, 103.939]
rgb_std = [58.393, 57.12, 57.375]
image_shape = [3, 224, 224]
label_smoothing = 0.1
model_save_dir = ./output/snapshots/model_save-20201123172206
log_dir = ./output
loss_print_every_n_iter = 1
image_size = 224
resize_shorter = 256
train_data_dir = None
train_data_part_num = 256
val_data_dir = /dataset/ImageNet/ofrecord/validation
val_data_part_num = 256
optimizer = sgd
learning_rate = 0.256
wd = 3.0517578125e-05
momentum = 0.875
lr_decay = cosine
lr_decay_rate = 0.94
lr_decay_epochs = 2
warmup_epochs = 5
decay_rate = 0.9
epsilon = 1.0
gradient_clipping = 0.0
------------------------------------------------------------------
Time stamp: 2020-11-23-17:22:06
Restoring model from resnet_v15_of_best_model_val_top1_77318.
Loading data from /dataset/ImageNet/ofrecord/validation


W1123 17:23:41.120939 31217 trt_executable.cpp:146] Rebuild engine since the maximum batch size 1 is less than the input batch size 256
W1123 17:24:25.756124 33076 trt_logger.cpp:35] TensorRT Logging: Explicit batch network detected and batch size specified, use execute without batch size instead.
W1123 17:24:31.005220 33076 trt_logger.cpp:35] TensorRT Logging: Explicit batch network detected and batch size specified, use execute without batch size instead.
W1123 17:24:36.085610 33076 trt_logger.cpp:35] TensorRT Logging: Explicit batch network detected and batch size specified, use execute without batch size instead.
W1123 17:24:41.073289 33076 trt_logger.cpp:35] TensorRT Logging: Explicit batch network detected and batch size specified, use execute without batch size instead.
W1123 17:24:45.920917 33076 trt_logger.cpp:35] TensorRT Logging: Explicit batch network detected and batch size specified, use execute without batch size instead.
W1123 17:24:50.633805 33076 trt_logger.cpp:35] TensorRT Logging: Explicit batch network detected and batch size specified, use execute without batch size instead.
W1123 17:24:55.354147 33076 trt_logger.cpp:35] TensorRT Logging: Explicit batch network detected and batch size specified, use execute without batch size instead.
W1123 17:24:59.904863 33076 trt_logger.cpp:35] TensorRT Logging: Explicit batch network detected and batch size specified, use execute without batch size instead.
validation: epoch 0, iter 195, top_1: 0.772155, top_k: 0.934856, samples/s: 181.038 1606123666.3968866
```

### 单机单卡


