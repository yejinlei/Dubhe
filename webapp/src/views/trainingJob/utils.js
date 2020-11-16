/** Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

// 训练状态
export const trainingStatusMap = {
  0: { statusMap: 'running' },
  1: { statusMap: 'running' },
  2: { tagMap: 'success', statusMap: 'done' },
  3: { tagMap: 'danger', statusMap: 'done' },
  4: { tagMap: 'info', statusMap: 'done' },
  5: { statusMap: 'done' },
  7: { tagMap: 'danger', statusMap: 'done' },
};

// 目录树弹窗文案
export const modelOfficial = [
  {
    'jobResume':'断点续训',
    'modelDownload':'模型下载',
    'modelSelect':'模型选择',
  },
  {
    'jobResume':'请选择从哪里开始继续训练',
    'modelDownload': '请选择需要下载的模型文件目录',
    'modelSelect': '请选择要保存的模型',
  },
  {
    'jobResume':'暂无数据，无法断点续训',
    'modelDownload': '暂无数据',
    'modelSelect': '暂无模型数据',
  },
];