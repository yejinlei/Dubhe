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
import grpc
import time
import json
import config as configs
from logger import Logger
from utils import file_utils
from concurrent import futures
from proto import inference_pb2_grpc, inference_pb2
from service.inference_service_manager import InferenceServiceManager
from response import Response

log = Logger().logger
parser = configs.get_parser()
args = parser.parse_args()
inference_service = InferenceServiceManager(args)
inference_service.init()

MAX_MESSAGE_LENGTH = 1024 * 1024 * 1024  # 可根据具体需求设置，此处设为1G


def response_convert(response):
    """
    返回值封装字典
    """
    return {
        'success': response.success,
        'data': response.data,
        'error': response.error
    }


class InferenceService(inference_pb2_grpc.InferenceServiceServicer):
    """
    调用grpc方法进行推理
    """
    def inference(self, request, context):
        data_list = request.data_list
        log.info("===============> grpc inference start <===============")
        try:
            data_list_b64 = file_utils.upload_image_by_base64(data_list)  # 上传图片到本地
        except Exception as e:
            log.error("upload data failed", e)
            return inference_pb2.DataResponse(json_result=json.dumps(
                response_convert(Response(success=False, data=str(e), error="upload data failed"))))
        try:
            result = inference_service.inference(args.model_name, data_list_b64)
            log.info("===============> grpc inference success <===============")
            return inference_pb2.DataResponse(json_result=json.dumps(
                response_convert(Response(success=True, data=result))))
        except Exception as e:
            log.error("inference fail", e)
            return inference_pb2.DataResponse(json_result=json.dumps(
                response_convert(Response(success=False, data=str(e), error="inference fail"))))


def main():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10), options=[
        ('grpc.max_send_message_length', MAX_MESSAGE_LENGTH),
        ('grpc.max_receive_message_length', MAX_MESSAGE_LENGTH),
    ])
    inference_pb2_grpc.add_InferenceServiceServicer_to_server(InferenceService(), server)
    if args.enable_tls:  # 使用tls加密通信
        with open(args.secret_key, 'rb') as f:
            private_key = f.read()
        with open(args.secret_crt, 'rb') as f:
            certificate_chain = f.read()
        # create server credentials
        server_credentials = grpc.ssl_server_credentials(((private_key, certificate_chain,),))
        server.add_secure_port(args.host + ':' + str(args.port), server_credentials)  # 添加监听端口
    else:  # 不使用tls加密通信
        server.add_insecure_port(args.host + ':' + str(args.port))  # 添加监听端口
    server.start()  # 启动服务器
    try:
        while True:
            log.info('===============> grpc server start <===============')
            time.sleep(60 * 60 * 24)
    except KeyboardInterrupt:
        server.stop(0)  # 关闭服务器


if __name__ == '__main__':
    main()
