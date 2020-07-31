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
    url: 'api/v1/roles',
    method: 'get',
    params,
  });
}

// 获取所有的Role
export function getAll() {
  return request({
    url: 'api/v1/roles/all',
    method: 'get',
  });
}

export function add(data) {
  return request({
    url: 'api/v1/roles',
    method: 'post',
    data,
  });
}

export function get(id) {
  return request({
    url: `api/v1/roles/${  id}`,
    method: 'get',
  });
}

export function del(ids) {
  return request({
    url: 'api/v1/roles',
    method: 'delete',
    data: { ids },
  });
}

export function edit(data) {
  return request({
    url: 'api/v1/roles',
    method: 'put',
    data,
  });
}

export function editMenu(data) {
  return request({
    url: 'api/v1/roles/menu',
    method: 'put',
    data,
  });
}

export default { list, add, edit, del, get, editMenu };
