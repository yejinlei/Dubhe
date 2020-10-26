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

import request from '@/utils/request';

export function list(params) {
  return request({
    url: 'api/v1/trainJob',
    method: 'get',
    params,
  });
}

export function add(data) {
  return request({
    url: 'api/v1/trainJob',
    method: 'post',
    data,
  });
}

export function edit(data) {
  return request({
    url: 'api/v1/trainJob',
    method: 'put',
    data,
  });
}

export function del(ids) {
  return request({
    url: 'api/v1/trainJob',
    method: 'delete',
    data: ids,
  });
}

export function resumeTrain(data) {
  return request({
    url: 'api/v1/trainJob/resume',
    method: 'post',
    data,
  });
}

export function stop(data) {
  return request({
    url: 'api/v1/trainJob/stop',
    method: 'post',
    data,
  });
}

export function getJobList(params) {
  return request({
    url: `api/v1/trainJob/trainJobVersionDetail`,
    method: 'get',
    params,
  });
}

export function getJobDetail(jobId) {
  return request({
    url: `api/v1/trainJob/jobDetail`,
    method: 'get',
    params: { id: jobId },
  });
}

export function getTrainLog(params) {
  return request({
    url: `api/v1/trainLog`,
    method: 'get',
    params,
  });
}

export function myTrainJobCount() {
  return request({
    url: `api/v1/trainJob/mine`,
    method: 'get',
  });
}

export function getTrainJobSpecs(params) {
  return request({
    url: `api/v1/trainJob/trainJobSpecs`,
    method: 'get',
    params,
  });
}

export function getGarafanaInfo(jobId) {
  return request({
    url: `api/v1/trainJob/grafanaUrl/${jobId}`,
  });
}

export function getPods(jobId) {
  return request({
    url: `api/v1/trainLog/pod/${jobId}`,
    method: 'get',
  });
}

export default { list, add, edit, del };
