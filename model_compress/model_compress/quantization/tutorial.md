### 量化步骤

### 迷你示例数据集的训练（非必需，主要是为了获取训练后的模型）

需要下载两个东西：
resnet模型：https://oneflow-public.oss-cn-beijing.aliyuncs.com/model_zoo/resnet_v15_of_best_model_val_top1_77318.tgz
解压好放在 quantization 根目录即可。

mini-imagenet数据集：https://oneflow-public.oss-cn-beijing.aliyuncs.com/online_document/dataset/imagenet/mini-imagenet.zip
解压好放在 quantization/data/ 目录下。


```
rm -rf core.* 
rm -rf ./output/snapshots/*

MODEL_PATH=./resnet_v15_of_best_model_val_top1_77318
DATA_ROOT=./data/mini-imagenet/ofrecord

# training with mini-imagenet
DATA_ROOT=data/mini-imagenet/ofrecord
python3 of_cnn_train_val.py \
   --model_load_dir=${MODEL_PATH} \
   --train_data_dir=$DATA_ROOT/train \
   --num_examples=50 \
   --train_data_part_num=1 \
   --val_data_dir=$DATA_ROOT/validation \
   --num_val_examples=50 \
   --val_data_part_num=1 \
   --num_nodes=1 \
   --gpu_num_per_node=1 \
   --optimizer="sgd" \
   --momentum=0.875 \
   --learning_rate=0.001 \
   --loss_print_every_n_iter=1 \
   --batch_size_per_device=16 \
   --val_batch_size_per_device=10 \
   --num_epoch=10 \
   --model="resnet50" \
   --use_fp16=false \
   --use_xla=false \
   --use_tensorrt=false \
   --use_int8_online=false \
   --use_int8_offline=false
```

### 无 tensorrt 评估

```
DATA_ROOT=./data/mini-imagenet/ofrecord
MODEL_LOAD_DIR="./output/snapshots/model_save-20210112020044/snapshot_epoch_9"

python3  of_cnn_evaluate.py \
    --num_epochs=3 \
    --num_val_examples=50 \
    --model_load_dir=${MODEL_LOAD_DIR}  \
    --val_data_dir=${DATA_ROOT}/validation \
    --val_data_part_num=1 \
    --num_nodes=1 \
    --gpu_num_per_node=1 \
    --val_batch_size_per_device=10 \
    --model="resnet50" \
    --use_fp16=false \
	--use_xla=false \
	--use_tensorrt=false \
	--use_int8_online=false \
	--use_int8_offline=false
```

### 有 tensorrt 评估

```
DATA_ROOT=./data/mini-imagenet/ofrecord
MODEL_LOAD_DIR="./output/snapshots/model_save-20210112020044/snapshot_epoch_9"

python3 of_cnn_evaluate.py \
    --num_epochs=3 \
    --num_val_examples=50 \
    --model_load_dir=${MODEL_LOAD_DIR}  \
    --val_data_dir=${DATA_ROOT}/validation \
    --val_data_part_num=1 \
    --num_nodes=1 \
    --gpu_num_per_node=1 \
    --val_batch_size_per_device=10 \
    --model="resnet50" \
    --use_fp16=false \
	--use_xla=false \
	--use_tensorrt=true \
	--use_int8_online=true \
	--use_int8_offline=false
```


模型评估所需参数：

- num_epochs: 运行 evaluation 的次数
- num_val_examples: 每次运行 evaluate 样本的数量
- model_load_dir: 已训练好的模型路径
- val_data_dir: OFRecord 格式的 ImageNet 数据集路径
- gpu_num_per_node: 每个节点 gpu 的个数
- num_nodes: 节点个数
- val_batch_size_per_device: 每个 gpu 上的 batch size
- model : 模型类型（当前支持 Resnet50, VGG16, Alexnet, InceptionV3等模型）



