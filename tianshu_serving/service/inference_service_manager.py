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
import json
import time
from service.oneflow_inference_service import OneFlowInferenceService
from service.tensorflow_inference_service import TensorflowInferenceService
from service.pytorch_inference_service import PytorchInferenceService
from logger import Logger
from utils import file_utils

log = Logger().logger


class InferenceServiceManager:

    def __init__(self, args):
        self.inference_service = None
        self.args = args
        self.model_name_service_map = {}

    def init(self):
        if self.args.model_config_file != "":
            with open(self.args.model_config_file) as data_file:
                model_config_file_dict = json.load(data_file)
                model_config_list = model_config_file_dict["model_config_list"]
                for model_config in model_config_list:
                    model_name = model_config["model_name"]
                    model_path = model_config["model_path"]
                    model_platform = model_config.get("platform")

                    if model_platform == "oneflow":
                        self.inference_service = OneFlowInferenceService(model_name, model_path)
                    elif model_platform == "tensorflow" or model_platform == "keras":
                        self.inference_service = TensorflowInferenceService(model_name, model_path)
                    elif model_platform == "pytorch":
                        self.inference_service = PytorchInferenceService(model_name, model_path)

                    self.model_name_service_map[model_name] = self.inference_service
        else:
            # Read from command-line parameter
            if self.args.platform == "oneflow":
                self.inference_service = OneFlowInferenceService(self.args.model_name, self.args.model_path)
            elif self.args.platform == "tensorflow" or self.args.platform == "keras":
                self.inference_service = TensorflowInferenceService(self.args.model_name, self.args.model_path)
            elif self.args.platform == "pytorch":
                self.inference_service = PytorchInferenceService(self.args.model_name, self.args.model_path)

            self.model_name_service_map[self.args.model_name] = self.inference_service

    def inference(self, model_name, images):
        """
        在线服务推理方法
        """
        inferenceService = self.model_name_service_map[model_name]
        result = list()
        for image in images:
            data = inferenceService.inference(image)
            if len(images) == 1:
                return data
            else:
                result.append(data)
        return result

    def inference_and_save_json(self, model_name, json_path, images):
        """
        批量服务推理方法
        """
        inferenceService = self.model_name_service_map[model_name]
        for image in images:
            data = inferenceService.inference(image)
            file_utils.writer_json_file(json_path, image['image_name'], data)
            time.sleep(1)
