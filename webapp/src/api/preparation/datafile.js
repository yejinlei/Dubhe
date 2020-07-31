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
  const {datasetId} = params;
  delete params.datasetId;
  return request({
    url: `api/data/datasets/${datasetId}/files`,
    method: 'get',
    params,
  });
}

export function del(params) {
  const datasetId = params.datasetIds;
  delete params.datasetIds;
  params.datasetIds = [datasetId];
  return request({
    url: 'api/data/datasets/files',
    method: 'delete',
    data: params,
  });
}

export function submit(id, files) {
  return request({
    url: `api/data/datasets/${id}/files`,
    method: 'post',
    data: { files },
  });
}

export function submitVideo(id, data) {
  return request({
    url: `api/data/datasets/${id}/video`,
    method: 'post',
    data,
  });
}

export default { list, del };
