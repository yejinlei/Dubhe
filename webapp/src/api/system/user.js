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
    url: 'api/v1/users',
    method: 'get',
    params,
  });
}

export function add(data) {
  return request({
    url: 'api/v1/users',
    method: 'post',
    data,
  });
}

export function del(ids) {
  return request({
    url: 'api/v1/users',
    method: 'delete',
    data: { ids },
  });
}

export function edit(data) {
  return request({
    url: 'api/v1/users',
    method: 'put',
    data,
  });
}

export default { list, add, edit, del };

