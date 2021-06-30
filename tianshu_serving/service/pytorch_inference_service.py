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
import os
import io
import torch
import torch.nn.functional as functional
from PIL import Image
from torchvision import transforms
import requests
from imagenet1000_clsidx_to_labels import clsidx_2_labels
from io import BytesIO
from logger import Logger
from service.abstract_inference_service import AbstractInferenceService

log = Logger().logger


class PytorchInferenceService(AbstractInferenceService):
    """
    pytorch 框架推理service
    """

    def __init__(self, args):
        super().__init__()
        self.args = args
        self.model_name = args.model_name
        self.model_path = args.model_path
        self.model = self.load_model()
        self.checkpoint = None

    def load_image(self, image_path):
        if image_path.startswith("http"):
            response = requests.get(image_path)
            response = response.content
            BytesIOObj = BytesIO()
            BytesIOObj.write(response)
            image = Image.open(BytesIOObj)
        else:
            image = open(image_path, 'rb').read()
            image = Image.open(io.BytesIO(image))
        if image.mode != 'RGB':
            image = image.convert("RGB")
        image = transforms.Resize((self.args.reshape_size[0], self.args.reshape_size[1]))(image)
        image = transforms.ToTensor()(image)
        image = transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])(image)
        image = image[None]
        if self.args.use_gpu:
            image = image.cuda()
        log.info("===============> load image success <===============")
        return image

    def load_model(self):
        log.info("===============> start load pytorch model :" + self.args.model_path + " <===============")
        if os.path.isfile(self.args.model_path):
            self.checkpoint = torch.load(self.model_path)
        else:
            for file in os.listdir(self.args.model_path):
                self.checkpoint = torch.load(self.model_path + file)
        model = self.checkpoint[self.args.model_structure]
        model.load_state_dict(self.checkpoint['state_dict'])
        for parameter in model.parameters():
            parameter.requires_grad = False
        if self.args.use_gpu:
            model.cuda()
        model.eval()
        log.info("===============> load pytorch model success <===============")
        return model

    def inference(self, image):
        data = {"data_name": image['data_name']}
        log.info("===============> start load " + image['data_name'] + " <===============")
        image = self.load_image(image['data_path'])
        preds = functional.softmax(self.model(image), dim=1)
        results = torch.topk(preds.data, k=5, dim=1)
        data['predictions'] = list()
        for prob, label in zip(results[0][0], results[1][0]):
            result = {"label": clsidx_2_labels[int(label)], "probability": "{:.3f}".format(float(prob))}
            data['predictions'].append(result)
        return data
