"""
/**
* Copyright 2020 Zhejiang Lab. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* =============================================================
*/
"""

# !/usr/bin/env python3
# -*- coding: utf-8 -*-
import _thread
import argparse
import codecs
import json
import os
import shutil
import sys
import time
import urllib
from queue import Queue
import web
from upload_config import Upload_cfg, MyApplication
import gen_ofrecord as ofrecord
from log_config import setup_log

urls = ('/gen_ofrecord', 'Ofrecord')
sys.stdout = codecs.getwriter("utf-8")(sys.stdout.detach())

parser = argparse.ArgumentParser(description="config for label server")
parser.add_argument("-p", "--port", type=int, required=True)
parser.add_argument("-m", "--mode", type=str, default="test", required=False)
args = parser.parse_args()

base_path = "/nfs/"
record_url = 'api/data/datasets/versions/'
url_json = './config/url.json'

with open(url_json) as f:
    url_dict = json.loads(f.read())
record_url = url_dict[args.mode] + record_url
port = args.port
of_que = Queue()
of_cond = []

des_folder = os.path.join('./log', args.mode)
if not os.path.exists(des_folder):
    os.makedirs(des_folder)

of_log = setup_log(args.mode, 'ofrecord-' + args.mode + '.log')


class Ofrecord(Upload_cfg):
    """Recieve and analyze the post request"""
    def POST(self):
        try:
            super().POST()
            x = web.data()
            x = json.loads(x.decode())
            print(x)
            dataset_version_id = x['id']
            label_map = x['datasetLabels']
            if dataset_version_id not in web.of_cond:
                web.of_cond.append(dataset_version_id)
                src_path = base_path + x['datasetPath']
                save_path = base_path + x['datasetPath'] + '/ofrecord'
                # transform the windows path to linux path
                src_path = '/'.join(src_path.split('\\'))
                save_path = '/'.join(save_path.split('\\'))
                of_config = [dataset_version_id, src_path, save_path,label_map]
                of_log.info('Recv of_config:%s' % of_config)
                web.t_queue1.put(of_config)
            else:
                pass
            return {"code": 200, "msg": "", "data": dataset_version_id}
        except Exception as e:
            of_log.error("Error post")
            of_log.error(e)
            return 'post error'


def gen_ofrecord_thread():
    """The implementation of ofRecord generating thread"""
    global record_url
    global of_que
    of_log.info('ofrecord server start'.center(66, '-'))
    of_log.info(record_url)
    while True:
        try:
            of_task = of_que.get()
            debug_msg = '-------- OfRecord gen start: %s --------' % of_task[0] if of_task else ''
            of_log.info(debug_msg)
            if not of_task:
                continue
            dataset_version_id = of_task[0]
            src_path = of_task[1]
            save_path = of_task[2]
            label_map = of_task[3]
            of_log.info('[%s] not in of_cond' % dataset_version_id)
            if os.path.exists(save_path):
                shutil.rmtree(save_path)
            os.makedirs(save_path)
            task_url = record_url + str(dataset_version_id) + '/convert/finish'
            of_log.info('key: label, type: int32')
            of_log.info('key: img_raw, type: bytes')
            desc = os.path.join(save_path, 'train')
            of_log.info('desc: %s' % desc)
            try:
                con, num_images, num_part = ofrecord.read_data_sets(
                    src_path, desc,label_map)
            except Exception as e:
                error_msg = 'Error happened in ofrecord.read_data_sets'
                of_log.error(error_msg)
                if of_task[0] in web.of_cond:
                    web.of_cond.remove(of_task[0])
                # send messages to DataManage
                url_dbg = 'Request to [%s]' % task_url
                of_log.info(url_dbg)
                headers = {'Content-Type': 'application/json'}
                req_body = bytes(json.dumps({'msg': str(e)}), 'utf8')

                req = urllib.request.Request(
                    task_url, data=req_body, headers=headers)
                response = urllib.request.urlopen(req, timeout=5)
                debug_msg = "response.read(): %s; ret_code: %s" % (
                    response.read(), response.getcode())
                of_log.info(debug_msg)
                raise e
            if not con:
                error_msg = 'No annotated images, No ofrecord will be created'
                of_log.warning(error_msg)
            of_log.info(
                'train: {} images in {} part files.\n'.format(
                    num_images, num_part))
            of_log.info('generate ofrecord file done')

            url_dbg = 'Request to [%s]' % task_url
            of_log.info(url_dbg)
            headers = {'Content-Type': 'application/json'}
            req_body = {'msg': 'ok'}
            req = urllib.request.Request(
                task_url, data=json.dumps(req_body).encode(), headers=headers)
            response = urllib.request.urlopen(req, timeout=5)
            debug_msg = "response.read(): %s; ret_code: %s" % (
                response.read(), response.getcode())
            of_log.info(debug_msg)
            web.of_cond.remove(of_task[0])


        except Exception as e:
            of_log.error("Error ofProcess")
            of_log.error(e)
            of_log.info(record_url)
        debug_msg = '-------- OfRecord gen end --------'
        of_log.info(debug_msg)

        time.sleep(0.01)


def of_thread(no, interval):
    """Running the ofRecord generating thread"""
    gen_ofrecord_thread()


if __name__ == "__main__":
    _thread.start_new_thread(of_thread, (5, 5))
    app = MyApplication(urls, globals())
    web.of_cond = of_cond
    web.t_queue1 = of_que
    app.run(port=port)
