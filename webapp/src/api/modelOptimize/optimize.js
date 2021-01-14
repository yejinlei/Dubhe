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

import request from '@/utils/request';

export function list(params) {
  return request({
    url: 'api/modelOpt/task',
    method: 'get',
    params,
  });
}

export function add(data) {
  return request({
    url: 'api/modelOpt/task',
    method: 'post',
    data,
  });
}

export function edit(data) {
  return request({
    url: 'api/modelOpt/task',
    method: 'put',
    data,
  });
}

export function del(data) {
  return request({
    url: `api/modelOpt/task`,
    method: 'delete',
    data,
  });
}

export function getOptimizeAlgorithms(params) {
  return request({
    url: 'api/modelOpt/task/getAlgorithm',
    method: 'get',
    params,
  });
}

export function getBuiltInModel(params) {
  return request({
    url: 'api/modelOpt/task/getBuiltInModel',
    method: 'get',
    params,
  });
}

export function getOptimizeDatasets(params) {
  return request({
    url: 'api/modelOpt/task/getDataset',
    method: 'get',
    params,
  });
}

export function getCustomizeDatasets(params) {
  return request({
    url: 'api/modelOpt/task/MyDataset',
    method: 'get',
    params,
  });
}

export function addCustomizeDatasets(data) {
  return request({
    url: 'api/modelOpt/task/MyDataset',
    method: 'post',
    data,
  });
}

export function addCustomizeModel(data) {
  return request({
    url: 'api/ptModelInfo/uploadModel',
    method: 'post',
    data,
  });
}

export function submit(data) {
  return request({
    url: `api/modelOpt/task/submit`,
    method: 'post',
    data,
  });
}

export default { list, add, edit, del };
