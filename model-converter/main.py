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
 =============================================================
"""

import json
import os
import subprocess
import logging
import web
from subprocess import PIPE

urls = (
    '/hello', 'Hello',
    '/model_convert', 'ModelConvert'
)
logging.basicConfig(filename='onnx.log', level=logging.DEBUG)

class Hello(object):
    def GET(self):
        return 'service alive'

class ModelConvert(object):
    def POST(self):
        data = web.data()
        web.header('Content-Type', 'application/json')
        try:
            json_data = json.loads(data)
            model_path = json_data['model_path']
            output_path = json_data['output_path']
            if not os.path.isdir(model_path):
                msg = 'model_path is not a dir: %s' % model_path
                logging.error(msg)
                return json.dumps({'code': 501, 'msg': msg, 'data': ''})
            if not output_path.endswith('/'):
                msg = 'output_path is not a dir: %s' % output_path
                logging.error(msg)
                return json.dumps({'code': 502, 'msg': msg, 'data': ''})
            exist_flag = exist(model_path)
            if not exist_flag:
                msg = 'SavedModel file does not exist at: %s' % model_path
                logging.error(msg)
                return json.dumps({'code': 503, 'msg': msg, 'data': ''})
            convert_flag, msg = convert(model_path, output_path)
            if not convert_flag:
                return json.dumps({'code': 504, 'msg': msg, 'data': ''})
        except Exception as e:
            logging.error(str(e))
            return json.dumps({'code': 505, 'msg': str(e), 'data': ''})
        return json.dumps({'code': 200, 'msg': 'ok', 'data': msg})

def exist(model_path):
    for file in os.listdir(model_path):
        if file=='saved_model.pbtxt' or file=='saved_model.pb':
            return True
    return False


def convert(model_path, output_path):
    output_path = output_path+'model.onnx'
    try:
        logging.info('model_path=%s, output_path=%s' % (model_path, output_path))
        result = subprocess.run(["python", "-m", "tf2onnx.convert", "--saved-model", model_path, "--output", output_path], stdout=PIPE, stderr=PIPE)
        logging.info(repr(result))
        if result.returncode != 0:
            return False, str(result.stderr)
    except Exception as e:
        logging.error(str(e))
        return False, str(e)
    return True, output_path

if __name__ == '__main__':
    app = web.application(urls, globals())
    app.run()
