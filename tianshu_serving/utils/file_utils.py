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
from PIL import Image
from io import BytesIO
import os
import time
import requests
import shutil
import base64
import json
from pathlib import Path
from tempfile import NamedTemporaryFile
from logger import Logger

log = Logger().logger
MAX_TIME_LENGTH = 1000000


def download_image(images_path):
    """
    根据网络图片下载到本地
    """
    save_image_dir = "/usr/local/images/"
    if not os.path.exists(save_image_dir):
        os.mkdir(save_image_dir)
    for image_path in images_path:
        response = requests.get(image_path)
        response = response.content
        BytesIOObj = BytesIO()
        BytesIOObj.write(response)
        im = Image.open(BytesIOObj)
        im.save(
            save_image_dir + str(int(round(time.time() * MAX_TIME_LENGTH))) + "." + image_path.split("/")[-1].split(".")[-1])


def upload_data(files):
    """
    前端上传图片保存到本地
    """
    save_data_dir = "/usr/local/data/"
    if not os.path.exists(save_data_dir):
        os.mkdir(save_data_dir)
    data_list = list()
    for file in files:
        try:
            suffix = Path(file.filename).suffix
            with NamedTemporaryFile(delete=False, suffix=suffix, dir=save_data_dir) as tmp:
                shutil.copyfileobj(file.file, tmp)
                tmp_file_name = Path(tmp.name).name
            data = {"data_name": file.filename, "data_path": save_data_dir + tmp_file_name}
            data_list.append(data)
        finally:
            file.file.close
    return data_list


def upload_image_by_base64(data_list):
    """
    base64图片信息保存到本地
    """
    save_data_dir = "/usr/local/data/"
    if not os.path.exists(save_data_dir):
        os.mkdir(save_data_dir)
    data_list_b64 = list()
    for data in data_list:
        file_path = save_data_dir + str(int(round(time.time() * MAX_TIME_LENGTH))) + "." + data.data_name.split(".")[-1]
        file_b64 = base64.b64decode(data.data_file)
        file = open(file_path, 'wb')
        file.write(file_b64)
        file.close()
        data_b64 = {"data_name": data.data_name, "data_path": file_path}
        data_list_b64.append(data_b64)
    return data_list_b64


def writer_json_file(json_path, data_name, data):
    """
    保存为json文件
    """
    if not os.path.exists(json_path):
        os.mkdir(json_path)
    filename = json_path + data_name + '.json'
    with open(filename, 'w', encoding='utf-8') as file_obj:
        file_obj.write(json.dumps(data, ensure_ascii=False))
