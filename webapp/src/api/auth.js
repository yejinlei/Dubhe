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

export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data,
  });
}

export function registerUser(data) {
  return request({
    url: 'auth/userRegister',
    method: 'post',
    data,
  });
}

export function resetPassword(data) {
  return request({
    url: 'auth/resetPassword',
    method: 'post',
    data,
  });
}

export function getCodeBySentEmail(data) {
  return request({
    url: 'auth/getCodeBySentEmail',
    method: 'post',
    data,
  });
}

export function getInfo() {
  return request({
    url: 'auth/info',
    method: 'get',
  });
}

export function getCodeImg() {
  return request({
    url: '/auth/code',
    method: 'get',
  });
}

export function getPublicKey() {
  return request({
    url: '/auth/getPublicKey',
    method: 'get',
  });
}

export function logout() {
  return request({
    url: 'auth/logout',
    method: 'delete',
  });
}

// 获取minIO 秘钥
export function getMinIOAuth() {
  return request({
    url: 'api/data/datasets/minio/info',
  });
}
