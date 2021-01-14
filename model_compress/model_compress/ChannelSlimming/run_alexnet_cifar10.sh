export ENABLE_USER_OP=True
export VISIBLE_DEVICES=3
#train base model
python3 of_cnn_train_val.py \
    --model=alexnet \
    --data_type=cifar10 \
	--log_type=base_model \
    --model_update=adam \
    --num_classes=10 \
    --train_data_dir=./ofData/cifar10/train \
    --train_data_part_num=5 \
    --val_data_dir=./ofData/cifar10/test \
    --val_data_part_num=1 \
    --num_nodes=1 \
    --gpu_num_per_node=1 \
    --loss_print_every_n_iter=1 \
    --label_smoothing=0 \
    --warmup_epochs=0 \
    --lr_decay=None \
    --image_shape=3,32,32 \
    --image_size=32 \
    --resize_shorter=32 \
    --rgb_mean=124.95,122.65,114.75 \
    --rgb_std=61.252,60.767,65.852 \
    --num_examples=50000 \
    --num_val_examples=10000 \
    --batch_size_per_device=32 \
    --val_batch_size_per_device=32 \
    --learning_rate=0.001 \
    --bn=True \
    --num_epochs=30 \
    --model_save_every_n_epoch=10 \
    --model_save_dir=./output/snapshots/alexnet/cifar10/model_base

#prune base model
python3 ./prune/pruneAlexnet.py \
    --percent=0.7 \
    --optimizer=adam \
	--prune_method=bn \
	--bn=True \
    --model_load_dir=./output/snapshots/alexnet/cifar10/model_base/snapshot_last \
    --model_save_dir=./output/snapshots/alexnet/cifar10/model_prune

#refine pruned model
python3 of_cnn_train_val.py \
    --model=alexnet \
    --data_type=cifar10 \
    --model_update=adam \
	--log_type=prune_model \
    --num_classes=10 \
    --train_data_dir=./ofData/cifar10/train \
    --train_data_part_num=5 \
    --val_data_dir=./ofData/cifar10/test \
    --val_data_part_num=1 \
    --num_nodes=1 \
    --gpu_num_per_node=1 \
    --loss_print_every_n_iter=1 \
    --label_smoothing=0 \
    --warmup_epochs=0 \
    --lr_decay=None \
    --image_shape=3,32,32 \
    --image_size=32 \
    --resize_shorter=32 \
    --rgb_mean=124.95,122.65,114.75 \
    --rgb_std=61.252,60.767,65.852 \
    --num_examples=50000 \
    --num_val_examples=10000 \
    --batch_size_per_device=32 \
    --val_batch_size_per_device=32 \
    --learning_rate=0.001 \
    --bn=True \
    --num_epochs=100 \
    --model_save_every_n_epoch=10 \
    --model_save_dir=./output/snapshots/alexnet/cifar10/model_refine \
    --model_load_dir=./output/snapshots/alexnet/cifar10/model_prune/model
    
    
    
    
    