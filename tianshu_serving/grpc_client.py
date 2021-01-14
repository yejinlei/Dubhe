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
import grpc
import base64
import config as configs
from logger import Logger
from proto import inference_pb2_grpc, inference_pb2

log = Logger().logger
parser = configs.get_parser()
args = parser.parse_args()

_HOST = 'kohj2s.serving.dubhe.ai'
_PORT = '31365'
MAX_MESSAGE_LENGTH = 1024 * 1024 * 1024  # 可根据具体需求设置，此处设为1G


def run():
    PROJECT_ROOT = os.path.dirname(os.path.abspath(__file__))
    if args.enable_tls:  # 使用tls加密通信
        with open(PROJECT_ROOT + '\\tls_crt\\server.crt', 'rb') as f:
            trusted_certs = f.read()
        credentials = grpc.ssl_channel_credentials(root_certificates=trusted_certs)
        channel = grpc.secure_channel(_HOST + ':' + _PORT, credentials, options=[
            ('grpc.max_send_message_length', MAX_MESSAGE_LENGTH),
            ('grpc.max_receive_message_length', MAX_MESSAGE_LENGTH), ], )  # 创建连接
    else:
        channel = grpc.insecure_channel(_HOST + ':' + _PORT, options=[
            ('grpc.max_send_message_length', MAX_MESSAGE_LENGTH),
            ('grpc.max_receive_message_length', MAX_MESSAGE_LENGTH), ], )  # 创建连接
    client = inference_pb2_grpc.InferenceServiceStub(channel=channel)  # 创建客户端
    data_request = inference_pb2.DataRequest()
    Image = data_request.images.add()
    Image.image_file = str(base64.b64encode(open("F:\\Files\\pic\\哈士奇.jpg", "rb").read()), encoding='utf-8')
    Image.image_name = "哈士奇.jpg"
    Image = data_request.images.add()
    Image.image_file = str(base64.b64encode(open("F:\\Files\\pic\\fish.jpg", "rb").read()), encoding='utf-8')
    Image.image_name = "fish.jpg"
    response = client.inference(data_request)
    log.info(response.json_result.encode('utf-8').decode('unicode_escape'))


if __name__ == '__main__':
    run()
