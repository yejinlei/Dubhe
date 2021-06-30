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
import time
import socket
import config as configs
from logger import Logger
from service.inference_service_manager import InferenceServiceManager

parser = configs.get_parser()
args = parser.parse_args()
inference_service = InferenceServiceManager(args)
inference_service.init()
log = Logger().logger


def get_host_ip():
    """
    查询本机ip地址
    return
    """
    hostname = socket.gethostname()
    ip = socket.gethostbyname(hostname)
    return ip


def read_directory(data_path):
    """
    读取文件夹并进行拆分文件
    :return:
    """
    files = os.listdir(data_path)
    num_files = len(files)
    index_list = list(range(num_files))
    data_list = list()
    for index in index_list:
        # 是否开启分布式
        if args.enable_distributed:
            ip = get_host_ip()
            log.info("NODE_IPS:{}", os.getenv('NODE_IPS'))
            ip_list = os.getenv('NODE_IPS').split(",")
            num_ips = len(ip_list)
            ip_index = ip_list.index(ip)
            if ip_index == index % num_ips:
                filename = files[index]
                data = {"data_name": filename, "data_path": data_path + filename}
                data_list.append(data)
        else:
            filename = files[index]
            data = {"data_name": filename, "data_path": data_path + filename}
            data_list.append(data)
    return data_list


def main():
    data_list = read_directory(args.input_path)
    inference_service.inference_and_save_json(args.model_name, args.output_path, data_list)
    if args.enable_distributed:
        ip = get_host_ip()
        log.info("NODE_IPS:{}", os.getenv('NODE_IPS'))
        ip_list = os.getenv('NODE_IPS').split(",")
        # 主节点必须等待从节点推理完成
        if ip == ip_list[0]:
            num_files = len(os.listdir(args.input_path))
            num_json = 0
            while num_json < num_files:
                num_json = len(os.listdir(args.output_path))
                time.sleep(5)


if __name__ == '__main__':
    log.info("===============> batch inference start <===============")
    main()
    log.info("===============> batch inference success <===============")
