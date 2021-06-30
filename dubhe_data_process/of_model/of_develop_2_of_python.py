# !/usr/bin/env python
# -*- coding:utf-8 -*-

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

import os
import shutil
import numpy as np


old_model_path = '/home/guoran/git-repo/yolo_test_1218/yolov3_model'
new_model_path = "yolov3_model_python/"
os.mkdir(new_model_path)
layers = os.listdir(old_model_path)
print(layers)
for layer in layers:
    models = os.listdir(os.path.join(old_model_path, layer))
    for model in models:
        src_path = old_model_path + "/" + layer + "/" + model
        # print(src_path)
        dst_dir = os.path.join(new_model_path, layer + "-" + model)
        # print(dst_dir)
        os.mkdir(dst_dir)
        os.mkdir(dst_dir + "-momentum")
        # print(dst_dir)
        shutil.copyfile(src_path, dst_dir + "/out")
        momentum = np.fromfile(src_path, dtype=np.float32)
        momentum[:] = 0
        momentum.tofile(dst_dir + "-momentum/out")
        print(
            "cp",
            old_model_path +
            "/" +
            layer +
            "/" +
            model,
            dst_dir +
            "/out")
