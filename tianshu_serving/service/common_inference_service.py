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
from imagenet1000_clsidx_to_labels import clsidx_2_labels
from logger import Logger

log = Logger().logger

# 只能定义一个class
class CommonInferenceService:
    # 请在__init__初始化方法中接收args参数，并加载模型（其中模型路径参数为args.model_path，是否使用gpu参数为args.use_gpu，模型加载方法用户可自定义）
    def __init__(self, args):
        self.args = args
        self.model = self.load_model()


    def load_data(self, data_path):
        image = open(data_path, 'rb').read()
        image = Image.open(io.BytesIO(image))
        if image.mode != 'RGB':
            image = image.convert("RGB")
        image = transforms.Resize((self.args.reshape_size[0], self.args.reshape_size[1]))(image)
        image = transforms.ToTensor()(image)
        image = transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])(image)
        image = image[None]
        if self.args.use_gpu:
            image = image.cuda()
        return image

    def load_model(self):
        if os.path.isfile(self.args.model_path):
            self.checkpoint = torch.load(self.args.model_path)
        else:
            for file in os.listdir(self.args.model_path):
                self.checkpoint = torch.load(self.args.model_path + file)
        model = self.checkpoint["model"]
        model.load_state_dict(self.checkpoint['state_dict'])
        for parameter in model.parameters():
            parameter.requires_grad = False
        if self.args.use_gpu:
            model.cuda()
        model.eval()
        return model

    # inference方法名称固定
    def inference(self, data):
        result = {"data_name": data['data_name']}
        log.info("===============> start load " + data['data_name'] + " <===============")
        data = self.load_data(data['data_path'])
        preds = functional.softmax(self.model(data), dim=1)
        predictions = torch.topk(preds.data, k=5, dim=1)
        result['predictions'] = list()
        for prob, label in zip(predictions[0][0], predictions[1][0]):
            predictions = {"label": clsidx_2_labels[int(label)], "probability": "{:.3f}".format(float(prob))}
            result['predictions'].append(predictions)
        return result

# 非必须，可用于本地调试
if __name__=="__main__":
    import argparse
    parser = argparse.ArgumentParser(description='dubhe serving')
    parser.add_argument('--model_path', type=str, default='./res4serving.pth', help="model path")
    parser.add_argument('--use_gpu', type=bool, default=True, help="use gpu or not")
    parser.add_argument('--reshape_size', type=list, default=[224,224], help="use gpu or not")
    args = parser.parse_args()
    server = CommonInferenceService(args)

    image_path = "./cat.jpg"
    image = {"data_name": "cat.jpg", "data_path": image_path}
    re = server.inference(image)
    print(re)