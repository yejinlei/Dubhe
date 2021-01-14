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
    url: '/api/data/datasets',
    method: 'get',
    params,
  });
}

// 数据集详情
export function detail(id) {
  return request({
    url: `/api/data/datasets/${id}`,
    method: 'get',
  });
}

// 数据集状态（导入数据集轮询使用）
export function queryDatasetStatus(ids) {
  return request({
    url: `/api/data/datasets/status`,
    method: 'get',
    params: { datasetIds: ids },
  });
}

export function add(data) {
  return request({
    url: 'api/data/datasets',
    method: 'post',
    data,
  });
}

export function del(ids) {
  const delData = { ids };
  return request({
    url: 'api/data/datasets',
    method: 'delete',
    data: delData,
  });
}

export function editDataset(data) {
  return request({
    url: `api/data/datasets/${data.id}`,
    method: 'put',
    data,
  });
}

export function topDataset(data) {
  return request({
    url: `api/data/datasets/${data.id}/top`,
    method: 'get',
  });
}

// 导入自定义数据集
export function addCustomDataset(data) {
  return request({
    url: `api/data/datasets/custom`,
    method: 'post',
    data,
  });
}

// 切换版本
export function toggleVersion({ datasetId, versionName }) {
  return request({
    url: `api/data/datasets/versions/${datasetId}`,
    method: 'put',
    params: { versionName },
  });
}

// 删除版本
export function deleteVersion({ datasetId, versionName }) {
  return request({
    url: `api/data/datasets/versions`,
    method: 'delete',
    data: { datasetId, versionName },
  });
}

export function getDatasetVersions(datasetId) {
  return request({
    url: `/api/data/datasets/versions/${datasetId}/list`,
    method: 'get',
  });
}

// 查询下一个发布版本号
export function queryNextVersion(datasetId) {
  return request({
    url: `/api/data/datasets/versions/${datasetId}/nextVersionName`,
  });
};

// 目标检测文件列表
export function detectFileList(datasetId, params) {
  return request({
    url: `api/data/datasets/${datasetId}/files/detection`,
    method: 'get',
    params,
  });
}

export function getPublishedDatasets(params) {
  return request({
    url: '/api/data/datasets/versions/filter',
    method: 'get',
    params,
  });
}

// 查询文件偏移值
export function queryFileOffset(datasetId, fileId, query = {}) {
  return request({
    url: `api/data/datasets/${datasetId}/files/${fileId}/offset`,
    params: query,
  });
}

// 查询数据集标签
export function queryLabels(datasetId, params) {
  return request({
    url: `api/data/datasets/${datasetId}/labels`,
    method: 'get',
    params,
  });
}

// 创建数据集标签
export function createLabel (datasetId, data) {
  return request({
    url: `api/data/datasets/${datasetId}/labels`,
    method: 'post',
    data,
  });
}

// 查询预置标签
export function queryPresetLabels() {
  return request({
    url: `api/data/datasets/presetLabels`,
    method: 'get',
  });
}

// 查询数据增强字典
export function queryDataEnhanceList() {
  return request({
    baseURL: process.env.VUE_APP_BASE_API,
    url: `api/v1/user/dict/dataset_enhance`,
    method: 'get',
  });
}

// 数据增强
export function postDataEnhance(datasetId, types = []) {
  return request({
    url: `api/data/datasets/enhance`,
    data: {
      datasetId,
      types,
    },
    method: 'post',
  });
}

// 指定原始文件，获取增强文件列表
export function getEnhanceFileList(datasetId, fileId) {
  return request({
    url: `api/data/datasets/${datasetId}/${fileId}/enhanceFileList`,
  });
}

// 根据数据集版本，获取原始文件数量
export function getOriginFileCount(datasetId) {
  return request({
    url: `api/data/datasets/versions/${datasetId}/originFileCount`,
  });
}

// 预置数据集和我的数据集的数量查询
export function queryDatasetsCount() {
  return request({
    url: `/api/data/datasets/count`,
  });
}

// 查询数据集状态
export function queryDatasetsProgress(params) {
  return request({
    url: `/api/data/datasets/progress`,
    method: 'get',
    params,
  });
}

export default { list, add, del };
