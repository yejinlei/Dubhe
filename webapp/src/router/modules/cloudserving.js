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

export default [
  {
    name: '云端Serving',
    path: '/cloudserving',
    hidden: false,
    component: () => import('@/layout/index'),
    meta: {
      title: '云端Serving',
      icon: 'shujumoxing',
      layout: null,
      noCache: true,
    },
    children: [
      {
        name: 'CloudServing',       // 路由名称
        path: 'onlineserving',      // 路由地址
        hidden: false,              // 是否隐藏
        component: () => import('@/views/cloudServing'),  // 路由组件地址
        meta: {
          title: '在线服务',         // 菜单标题
          icon: 'shujumoxing',      // 菜单图标
          layout: 'BaseLayout',     // 页面布局
          noCache: true,
        },
      },
      {
        name: 'BatchServing',
        path: 'batchserving',
        hidden: false,
        component: () => import('@/views/cloudServing/batch'),
        meta: {
          title: '批量服务',
          icon: 'shujumoxing',
          layout: 'BaseLayout',
          noCache: true,
        },
      },
      {
        name: 'CloudServingDetail',
        path: 'onlineserving/detail',
        hidden: true,
        component: () => import('@/views/cloudServing/detail'),
        meta: {
          title: '部署详情',
          icon: null,
          layout: 'SubpageLayout',
          noCache: true,
        },
      },
      {
        name: 'BatchServingDetail',
        path: 'batchserving/detail',
        hidden: true,
        component: () => import('@/views/cloudServing/batchDetail'),
        meta: {
          title: '部署详情',
          icon: null,
          layout: 'SubpageLayout',
          noCache: true,
        },
      },
      {
        name: 'CloudServingForm',
        path: 'onlineserving/form',
        hidden: true,
        component: () => import('@/views/cloudServing/formPage'),
        meta: {
          title: '创建在线服务',
          icon: null,
          layout: 'SubpageLayout',
          noCache: true,
        },
      },
    ],
  },
];