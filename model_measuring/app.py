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

dependencies = ['torch', 'kamal']  # 依赖项
import json
import traceback
import os
import torch
import web
import kamal
from PIL import Image
from kamal.transferability import TransferabilityGraph
from kamal.transferability.trans_metric import AttrMapMetric
from torchvision.models import *

urls = (
    '/model_measure/measure', 'Measure',
    '/model_measure/package', 'Package',
)
app = web.application(urls, globals())
class Package:
    def POST(self):
        req = web.data()
        save_path_list = []
        for json_data in  json.loads(req):
            try:
                metadata = json_data['metadata']
            except Exception as e:
                traceback.print_exc()
                return json.dumps(Response(506, 'Failed to package model, Error: %s'% (traceback.format_exc(limit=1)), save_path_list).__dict__)
            entry_name = json_data['entry_name']
            for name, fn in kamal.hub.list_entry(__file__):
                if entry_name in name:
                    try:
                        dataset = metadata['dataset']
                        model = fn(pretrained=False, num_classes=metadata['entry_args']['num_classes'])
                        num_params = sum( [ torch.numel(p) for p in model.parameters() ] )
                        save_path_for_measure = '%sfinegraind_%s/' % (json_data['ckpt'], entry_name)
                        save_path = save_path_for_measure+'%s' % (metadata['name'])
                        ckpt = self.file_name(json_data['ckpt'])
                        if ckpt == '':
                            return json.dumps(
                                Response(506, 'Failed to package model [%s]: No .pth file was found in directory ckpt' % (entry_name), save_path_list).__dict__)
                        model.load_state_dict(torch.load(ckpt), False)
                        kamal.hub.save(  # 该调用将用户的pytorch模型打包成上述格式，并存储至指定位置
                            model,  # 需要保存的模型 nn.Module
                            save_path=save_path,
                            # 导出文件夹名称
                            entry_name=entry_name,  # 入口函数名，需要与上边的入口函数一致
                            spec_name=None,  # 具体的参数版本名，为空则自动用md5替代
                            code_path=__file__,  # 模型依赖的代码，可以是文件夹(必须包含hubconf.py文件)，
                            # 或者是当前hubconf.py, 例子中直接使用了依赖中的模型实现，故只需指定为本文件即可
                            metadata=metadata,
                            tags=dict(
                                      num_params=num_params,
                                      metadata=metadata,
                                      name=metadata['name'],
                                      url=metadata['url'],
                                      dataset=dataset,
                                      img_size=metadata['input']['size'],
                                      readme=json_data['readme'])

                        )
                        save_path_list.append(save_path_for_measure)
                        return json.dumps(Response(200, 'Success', save_path_list).__dict__)
                    except Exception:
                        traceback.print_exc()
                        return json.dumps(Response(506,'Failed to package model [%s], Error: %s' % (entry_name, traceback.format_exc(limit=1)), save_path_list).__dict__)
            return json.dumps(Response(506, 'Failed to package model [%s], Error: %s' % (entry_name, traceback.format_exc(limit=1)), save_path_list).__dict__)


    def file_name(self, file_dir):
        for root, dirs, files in os.walk(file_dir):
            for file in files:
                if file.endswith('pth'):
                    return root + file
        return ''


class Measure:
    def POST(self):
        req = web.data()
        json_data = json.loads(req)
        print(json_data)
        try:
            measure_name = 'measure'
            zoo_set = json_data['zoo_set']
            probe_set_root = json_data['probe_set_root']
            export_path = json_data['export_path']
            output_filename_list = []
            TG = TransferabilityGraph(zoo_set)
            print("Add %s" % (probe_set_root))
            imgs_set = list(os.listdir(os.path.join(probe_set_root)))
            images = [Image.open(os.path.join(probe_set_root, img)) for img in imgs_set]
            metric = AttrMapMetric(images, device=torch.device('cuda'))
            TG.add_metric(probe_set_root, metric)
            isExists = os.path.exists(export_path)
            if not isExists:
                # 如果不存在则创建目录
                os.makedirs(export_path)
            output_filename = export_path+'%s.json' % (measure_name)
            TG.export_to_json(probe_set_root, output_filename, topk=3, normalize=True)
            output_filename_list.append(output_filename)
        except Exception:
            traceback.print_exc()
            return json.dumps(Response(506, 'Failed to generate measurement file of [%s], Error: %s' % (probe_set_root, traceback.format_exc(limit=1)), output_filename_list).__dict__)
        return json.dumps(Response(200, 'Success', output_filename_list).__dict__)

class Response:
    def __init__(self, code, msg, data):
        self.code = code
        self.msg = msg
        self.data = data

if __name__ == "__main__":
    app.run()