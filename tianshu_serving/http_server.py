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
import config as configs
from fastapi import FastAPI, File, UploadFile
from utils import file_utils
import uvicorn
import threading
from logger import Logger
from typing import List
from service.inference_service_manager import InferenceServiceManager
from response import Response

app = FastAPI(version='1.0', title='Zhejiang Lab TS_Serving inference Automation',
              description="<b>API for performing oneflow、tensorflow、pytorch inference</b></br></br>")

# 独立部署可在该处解决跨域问题，或在nginx和网关下解决
# app.add_middleware(
#     CORSMiddleware,
#     allow_origins=["*"],  # 设置允许的origins来源
#     allow_credentials=True,
#     allow_methods=["*"],  # 设置允许跨域的http方法，比如 get、post、put等。
#     allow_headers=["*"])  # 允许跨域的headers，可以用来鉴别来源等作用。

parser = configs.get_parser()
args = parser.parse_args()
configs.print_args(args)
inference_service = InferenceServiceManager(args)
inference_service.init()
log = Logger().logger


@app.get("/")
def read_root():
    return {"message": "ok"}


@app.post("/image_inference")
async def inference(images_path: List[str] = None):
    threading.Thread(target=file_utils.download_image(images_path))  # 开启异步线程下载图片到本地
    images = list()
    for image in images_path:
        data = {"data_name": image.split("/")[-1], "data_path": image}
        images.append(data)
    try:
        data = inference_service.inference(args.model_name, images)
        return Response(success=True, data=data)
    except Exception as e:
        return Response(success=False, data=str(e), error="inference fail")


@app.post("/inference")
async def inference(files: List[UploadFile] = File(...)):
    """
    上传本地文件推理
    """
    log.info("===============> http inference start <===============")
    try:
        data_list = file_utils.upload_data(files)  # 上传图片到本地
    except Exception as e:
        log.error("upload data failed", e)
        return Response(success=False, data=str(e), error="upload data failed")
    try:
        result = inference_service.inference(args.model_name, data_list)
        log.info("===============> http inference success <===============")
        return Response(success=True, data=result)
    except Exception as e:
        log.error("inference fail", e)
        return Response(success=False, data=str(e), error="inference fail")


if __name__ == '__main__':
    uvicorn.run(app, host=args.host, port=args.port)
