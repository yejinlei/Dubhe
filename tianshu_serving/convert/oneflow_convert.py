"""
Copyright 2020 Tianshu AI Platform. All Rights Reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""
# oneflow checkpoint 到savedmodel 转换脚本
# 转换出来job_name是'inference'，也就是 make_infer_func 中定义的同名函数

import oneflow as flow
import os
import shutil

os.environ['CUDA_VISIBLE_DEVICES'] = '3'

# 输入参数(需要根据模型修改)
image_size = (3, 299, 299)
model_name = "inceptionv3"
saved_model_path = "inceptionv3"
batch_size = 8

# 导入模型网络结构
# from of_models.resnet_model import resnet50
# from of_models.alexnet_model import alexnet
# from of_models.vgg_model import vgg16bn
from models.of_models import inceptionv3

model = inceptionv3

# 模型checkpoint文件路径
# DEFAULT_CHECKPOINT_DIR = "./checkpoint_of/resnet_v15_of_best_model_val_top1_77318"
# DEFAULT_CHECKPOINT_DIR = "./checkpoint_of/alexnet_of_best_model_val_top1_54762"
# DEFAULT_CHECKPOINT_DIR = "./checkpoint_of/vgg16_of_best_model_val_top1_721"
DEFAULT_CHECKPOINT_DIR = "./checkpoint_of/snapshot_epoch_75"


def init_env():
    flow.env.init()
    flow.config.machine_num(1)
    flow.config.cpu_device_num(1)
    flow.config.gpu_device_num(1)
    flow.config.enable_debug_mode(True)


def make_infer_func(batch_size, image_size):
    input_lbns = {}
    output_lbns = {}
    image_shape = (batch_size,) + tuple(image_size)

    @flow.global_function(type="predict")
    def inference(
            image: flow.typing.Numpy.Placeholder(image_shape, dtype=flow.float32)
    ) -> flow.typing.Numpy:
        input_lbns["image"] = image.logical_blob_name
        output = model(image)
        output = flow.nn.softmax(output)
        output_lbns["output"] = output.logical_blob_name
        return output

    return inference, input_lbns, output_lbns


def ckpt_to_savedmodel():
    init_env()
    resnet_infer, input_lbns, output_lbns = make_infer_func(
        batch_size, image_size
    )
    # origin resnet inference model
    checkpoint = flow.train.CheckPoint()
    checkpoint.load(DEFAULT_CHECKPOINT_DIR)

    # save model
    # 修改路径
    if os.path.exists(saved_model_path) and os.path.isdir(saved_model_path):
        shutil.rmtree(saved_model_path)

    model_version = 1
    saved_model_builder = flow.SavedModelBuilderV2(saved_model_path)
    job_builder = (
        saved_model_builder.ModelName(model_name)
            .Version(model_version)
            .Job(resnet_infer)
    )
    for input_name, lbn in input_lbns.items():
        job_builder.Input(input_name, lbn)
    for output_name, lbn in output_lbns.items():
        job_builder.Output(output_name, lbn)
    job_builder.Complete().Save()


if __name__ == "__main__":
    ckpt_to_savedmodel()
