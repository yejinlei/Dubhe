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
import torch
import torchvision.models as models


if __name__ == '__main__':
    model = models.resnext50_32x4d(pretrained=False)
    pre = torch.load("/usr/local/model/pytorch_models/resnext50_32x4d-7cdf4587.pth")
    model.load_state_dict(pre)
    checkpoint = {'model': models.resnext50_32x4d(),
                  'state_dict': model.state_dict()}
    torch.save(checkpoint, '/usr/local/model/pytorch_models/resnext50.pth')
    print("success")
