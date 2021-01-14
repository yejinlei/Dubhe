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
from abc import ABC, abstractmethod


class AbstractInferenceService(ABC):

    def __init__(self):
        self.model_name = None
        self.model_path = None
        self.platform = None

    @abstractmethod
    def load_model(self):
        """
        加载预训练模型
        """
        pass

    @abstractmethod
    def load_image(self, image_path):
        """
        加载图片以供推理使用
        """
        pass

    @abstractmethod
    def inference(self, image_path):
        """
        使用图片进行推理
        """
        pass
