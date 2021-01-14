/** Copyright 2020 Tianshu AI Platform. All Rights Reserved.
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

const Constant = {
  // map
  tableSortMap: {
    ascending: 'asc',
    descending: 'desc',
  },

  tableSortMap2Element: {
    asc: 'ascending',
    desc: 'descending',
  },
  
  // 表单标题映射
  FORM_TYPE_MAP: {
    add: '创建',
    fork: 'Fork ',
    edit: '编辑',
  },
};

export const MODEL_RESOURCE = {
  CUSTOM: {
    label: '我的模型',
    value: 0,
  },
  PRESET: {
    label: '预训练模型',
    value: 1,
  },
};

export const MODEL_RESOURCE_MAP = {
  0: '我的模型',
  1: '预训练模型',
};

export const RESOURCES_POOL_TYPE = {
  CPU: {
    label: 'CPU',
    value: 0,
  },
  GPU: {
    label: 'GPU',
    value: 1,
  },
};

export const RESOURCES_POOL_TYPE_MAP = {
  0: 'CPU',
  1: 'GPU',
};

export { Constant };
