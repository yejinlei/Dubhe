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
import tensorflow as tf
import requests
import numpy as np
import config as configs
from imagenet1000_clsidx_to_labels import clsidx_2_labels
from service.abstract_inference_service import AbstractInferenceService
from utils.imagenet_preprocessing_utils import preprocess_input
from logger import Logger
from PIL import Image
from io import BytesIO

parser = configs.get_parser()
args = parser.parse_args()
log = Logger().logger


class TensorflowInferenceService(AbstractInferenceService):
    """
    tensorflow 框架推理service
    """
    def __init__(self, model_name, model_path):
        super().__init__()
        self.session = tf.compat.v1.Session(graph=tf.Graph())
        self.model_name = model_name
        self.model_path = model_path
        self.signature_input_keys = []
        self.signature_input_tensor_names = []
        self.signature_output_keys = []
        self.signature_output_tensor_names = []
        self.input_info_from_signature = {}
        self.output_info_from_signature = {}
        self.load_model()

    def load_image(self, image_path):
        if image_path.startswith("http"):
            response = requests.get(image_path)
            response = response.content
            BytesIOObj = BytesIO()
            BytesIOObj.write(response)
            im = Image.open(BytesIOObj)
        else:
            im = Image.open(image_path)

        # signature中读取图片大小做resize
        image_shape_from_signature = list(self.input_info_from_signature.values())[0]["shape"]
        height = image_shape_from_signature[1]
        width = image_shape_from_signature[2]

        im = im.resize((height, width))
        im = im.convert('RGB')  # 有的图像是单通道的，不加转换会报错
        im = np.array(im).astype('float32')
        return np.ascontiguousarray(im, 'float32')

    def load_model(self):
        log.info("===============> start load tensorflow model :" + self.model_path + " <===============")
        meta_graph = tf.compat.v1.saved_model.load(
            self.session, [tf.compat.v1.saved_model.tag_constants.SERVING], self.model_path)

        # 加载模型之前先校验用户传入signature name
        if args.signature_name not in meta_graph.signature_def:
            log.error("==============> Invalid signature name <==================")

        # 从signature中获取meta graph中输入和输出的节点信息
        signature = meta_graph.signature_def[args.signature_name]
        input_keys, input_tensor_names = get_tensors(signature.inputs)
        output_keys, output_tensor_names = get_tensors(signature.outputs)

        self.signature_input_keys = input_keys
        self.signature_output_keys = output_keys
        self.signature_input_tensor_names = input_tensor_names
        self.signature_output_tensor_names = output_tensor_names

        self.input_info_from_signature = get_tensor_info_from_signature(signature.inputs)
        self.output_info_from_signature = get_tensor_info_from_signature(signature.outputs)
        log.info("===============> load tensorflow model success <===============")

    def inference(self, image):
        data = {"image_name": image['image_name']}
        # 获得用户输入的图片
        log.info("===============> start load " + image['image_name'] + " <===============")
        # 推理所需的输入,目前的分类预置模型都只有一个输入
        input_dict = {}
        input_keys = self.signature_input_keys
        input_data = {}
        im = preprocess_input(self.load_image(image['image_path']), mode=args.prepare_mode)
        if len(list(im.shape)) == 3:
            input_data[input_keys[0]] = np.expand_dims(im, axis=0)

        for i in range(len(input_keys)):
            input_key = input_keys[i]
            input_tensor_name = self.signature_input_tensor_names[i]
            input_dict[input_tensor_name] = input_data[input_key]
        # 推理所需的输出tensor名
        output_tensor_names = self.signature_output_tensor_names

        # 进行推理，返回推理结果
        inference_result = self.session.run(output_tensor_names, feed_dict=input_dict)

        # 推理结果后处理
        data['predictions'] = list()
        for i in range(len(self.signature_output_keys)):
            output_key = self.signature_output_keys[i]
            if self.output_info_from_signature[output_key]['shape'][-1] >= 1000:
                # 返回Top 5 类
                top5 = np.argsort(inference_result[i][0])[::-1][0:5]
                for index in top5:
                    if len(inference_result[i][0]) == 1001:
                        result = {"label": clsidx_2_labels[index - 1], output_key: str(inference_result[i][0][index])}
                    else:
                        result = {"label": clsidx_2_labels[index], output_key: str(inference_result[i][0][index])}
                    data['predictions'].append(result)
        return data


def get_tensor_info_from_signature(data):
    tensor_info_dict = {}
    for name, tensor_info in data.items():
        tensor_shape = list(map(lambda dim: dim.size, tensor_info.tensor_shape.dim))
        tf_dtype = tf.dtypes.as_dtype(tensor_info.dtype)
        tensor_info_dict[name] = ({"shape": tensor_shape, "dtype": tf_dtype})
    return tensor_info_dict


def get_tensors(data):
    keys = []
    tensor_names = []
    for name, tensor_info in data.items():
        keys.append(name)
        tensor_names.append(tensor_info.name)
    return keys, tensor_names
