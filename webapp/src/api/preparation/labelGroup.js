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

// 创建标签组
export function add(data) {
  return request({
    url: `/api/data/labelGroup`,
    method: 'post',
    data,
  });
}
  
// 编辑标签组
export function edit(data) {
  return request({
    url: `/api/data/labelGroup/${data.id}`,
    method: 'put',
    data,
  });
}
  
// 删除标签组
export function del(ids) {
  return request({
    url: `/api/data/labelGroup`,
    method: 'delete',
    data: {ids},
  });
}

// 复制标签组
export function copy(data) {
  return request({
    url: `/api/data/labelGroup/copy`,
    method: 'post',
    data,
  });
}

// 标签组列表分页查询
export function list(params) {
  return request({
    url: `/api/data/labelGroup/query`,
    method: 'get',
    params,
  });
}

// 标签组列表的简况查询 用于详情页选择标签组列举
export function getLabelGroupList(params) {
  return request({
    url: `/api/data/labelGroup/getList/${params}`,
    method: 'get',
  });
}

// 获取标签组详情
export function getLabelGroupDetail(id) {
  return request({
    url: `/api/data/labelGroup/${id}`,
    method: 'get',
  });
}

export function importLabelGroup(form) {
  return request.post(`api/data/labelGroup/import`, form, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}

export default { list, add, del, edit };

