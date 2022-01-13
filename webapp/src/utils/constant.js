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

export const Constant = {
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

// 算法来源枚举值
export const ALGORITHM_RESOURCE_ENUM = {
  CUSTOM: 1, // 我的算法
  PRESET: 2, // 预置算法
};

// 模型分类枚举值
export const MODEL_RESOURCE_ENUM = {
  CUSTOM: 0, // 我的模型
  PRESET: 1, // 预训练模型
  ATLAS: 2, // 炼知模型
};

// 模型分类名称
export const MODEL_RESOURCE_MAP = {
  [MODEL_RESOURCE_ENUM.CUSTOM]: '我的模型',
  [MODEL_RESOURCE_ENUM.PRESET]: '预训练模型',
  [MODEL_RESOURCE_ENUM.ATLAS]: '炼知模型',
};

// 炼知模型打包状态枚举
export const ALTAS_MODEL_PACKAGE_ENUM = {
  UNPACKAGED: 0,
  PACKAGED: 1,
};

// 资源类型枚举
export const RESOURCES_POOL_TYPE_ENUM = {
  CPU: 0,
  GPU: 1,
};

// 资源业务场景枚举
export const RESOURCES_MODULE_ENUM = {
  NOTEBOOK: 1,
  TRAIN: 2,
  SERVING: 3,
  TADL: 4,
};

// 资源类型名称
export const RESOURCES_POOL_TYPE_MAP = {
  [RESOURCES_POOL_TYPE_ENUM.CPU]: 'CPU',
  [RESOURCES_POOL_TYPE_ENUM.GPU]: 'GPU',
};

// webSocket topic 枚举值
export const WEB_SOCKET_TOPIC_ENUM = {
  RESOURCE_MONITOR: 'resourceMonitor', // 用户资源监控
};

// K8S pod 业务标签枚举值
export const K8S_BUSINESS_LABEL_ENUM = {
  NOTEBOOK: 'notebook',
  TRAINING: 'algorithm',
  MODEL_OPTIMIZE: 'modelopt',
  SERVING: 'serving',
  BATCH_SERVING: 'batchserving',
  TADL: 'tadl',
  TERMINAL: 'terminal',
};

// K8S pod 业务标签与业务模块对应关系匹配
export const K8S_BUSINESS_LABEL_MAP = {
  [K8S_BUSINESS_LABEL_ENUM.NOTEBOOK]: '算法开发',
  [K8S_BUSINESS_LABEL_ENUM.TRAINING]: '训练管理',
  [K8S_BUSINESS_LABEL_ENUM.MODEL_OPTIMIZE]: '模型优化',
  [K8S_BUSINESS_LABEL_ENUM.SERVING]: '云端 Serving 在线服务',
  [K8S_BUSINESS_LABEL_ENUM.BATCH_SERVING]: '云端 Serving 批量服务',
  [K8S_BUSINESS_LABEL_ENUM.TADL]: 'TADL',
  [K8S_BUSINESS_LABEL_ENUM.TERMINAL]: '天枢专业版',
};

// 默认进度条颜色
export const defaultProcessColors = [
  { color: '#909399', percentage: 40 },
  { color: '#e6a23c', percentage: 80 },
  { color: '#67c23a', percentage: 100 },
];

// 系统管理员ID
export const ADMIN_ROLE_ID = 1;

// 时间常量（毫秒）
export const ONE_MINUTE = 1000 * 60;

export const ONE_HOUR = ONE_MINUTE * 60;

export const ONE_DAY = ONE_HOUR * 24;

export const ONE_WEEK = ONE_DAY * 7;
