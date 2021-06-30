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
import oneflow as flow
import oneflow.core.serving.saved_model_pb2 as saved_model_pb
import numpy as np
import requests
from PIL import Image
from io import BytesIO
import google.protobuf.text_format as text_format
import os
from imagenet1000_clsidx_to_labels import clsidx_2_labels
from logger import Logger
from service.abstract_inference_service import AbstractInferenceService

log = Logger().logger


class OneFlowInferenceService(AbstractInferenceService):
    """
    oneflow 框架推理service
    """
    def __init__(self, args):
        super().__init__()
        self.args = args
        self.model_name = args.model_name
        self.model_path = args.model_path
        flow.clear_default_session()
        self.infer_session = flow.SimpleSession()
        self.load_model()

    def load_image(self, image_path):
        global im
        if image_path.startswith("http"):
            response = requests.get(image_path)
            response = response.content
            BytesIOObj = BytesIO()
            BytesIOObj.write(response)
            im = Image.open(BytesIOObj)
        else:
            im = Image.open(image_path)

        input_names = self.infer_session.list_inputs()
        batch_size, channel, height, width = self.infer_session.input_info(input_names[0])["shape"]
        im = im.resize((height, width))
        im = im.convert('RGB')  # 有的图像是单通道的，不加转换会报错
        im = np.array(im).astype('float32')
        im = (im - [123.68, 116.779, 103.939]) / [58.393, 57.12, 57.375]
        im = np.transpose(im, (2, 0, 1))
        im = np.expand_dims(im, axis=0)
        log.info("===============> load image success <===============")
        im = np.ascontiguousarray(im, 'float32')
        images = np.repeat(im, batch_size, axis=0).astype(np.float32)
        return images

    def load_model(self):
        log.info("===============> start load oneflow model :" + self.model_path + " <===============")
        model_meta_file_path = os.path.join(
            self.model_path, "saved_model.prototxt"
        )
        # load saved model
        saved_model_proto = saved_model_pb.SavedModel()
        with open(model_meta_file_path, "rb") as f:
            text_format.Merge(f.read(), saved_model_proto)

        checkpoint_path = os.path.join(
            self.model_path, saved_model_proto.checkpoint_dir[0]
        )

        self.infer_session.set_checkpoint_path(checkpoint_path)
        for job_name, signature in saved_model_proto.signatures_v2.items():
            self.infer_session.setup_job_signature(job_name, signature)

        for job_name, net in saved_model_proto.graphs.items():
            with self.infer_session.open(job_name) as session:
                session.compile(net.op)
        self.infer_session.launch()
        log.info("===============> load oneflow model success <===============")
        return saved_model_proto

    def inference(self, image):
        data = {"data_name": image['data_name']}
        log.info("===============> start load " + image['data_name'] + " <===============")
        images = self.load_image(image['data_path'])

        predictions = self.infer_session.run('inference', image=images)

        top_k_idx = predictions[0][0]
        top_5_idx = top_k_idx.argsort()[::-1][0:5]
        data['predictions'] = list()
        for label_id in top_5_idx:
            result = {"label": clsidx_2_labels[label_id], "probability": "{:.3f}".format(top_k_idx[label_id])}
            data['predictions'].append(result)
        return data
