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

// minIO 参数配置
export const minIO = {
  development: {
    config: {
      endPoint: '', // MinIO 服务地址
      port: 9000,
      useSSL: false,
    },
    bucketName: 'dubhe-dev',
  },
  test: {
    config: {
      endPoint: '',
      port: 9000,
      useSSL: false,
    },
    bucketName: 'dubhe-test',
  },
  production: {
    config: {
      endPoint: '',
      port: 9000,
      useSSL: false,
    },
    bucketName: 'dubhe-prod',
  },
};

// 训练管理模块参数配置
export const trainConfig = {
  trainNodeMax: Infinity, // 分布式训练节点上限
  delayCreateTimeMax: 168, // 延时启动时间上限
  delayDeleteTimeMax: 168, // 训练时长上限
};

// 算法管理参数配置
export const algorithmConfig = {
  uploadFileAcceptSize: 1024, // 上传算法文件大小限制，单位为 MB，0 表示不限制大小
};

// 镜像管理参数配置
export const imageConfig = {
  uploadFileAcceptSize: 0, // 上传镜像文件大小限制，单位为 MB，0 表示不限制大小
};

// 模型管理模块参数配置
export const modelConfig = {
  uploadFileAcceptSize: 0, // 上传模型文件大小限制，单位为 MB，0 表示不限制大小
};
