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
    name: '模型管理',
    path: '/model',
    hidden: false,
    component: () => import('@/layout/index'),
    meta: {
      title: '模型管理',
      icon: 'moxingguanli',
      layout: null,
      noCache: true,
    },
    children: [
      {
        name: 'ModelModel',       // 路由名称
        path: 'model',            // 路由地址
        hidden: false,            // 是否隐藏
        component: () => import('@/views/model/index'),   // 路由组件地址
        meta: {
          title: '模型列表',       // 菜单标题
          icon: 'zongshili',      // 菜单图标
          layout: 'BaseLayout',   // 页面布局
          noCache: true,
        },
      },
      {
        name: 'ModelOptimize',
        path: 'optimize',
        hidden: false,
        component: () => import('@/views/modelOptimize/index'),
        meta: {
          title: '模型优化',
          icon: 'caidanguanli',
          layout: 'BaseLayout',
          noCache: true,
        },
      },
      {
        name: 'ModelVersion',
        path: 'version',
        hidden: true,
        component: () => import('@/views/model/version'),
        meta: {
          title: '模型版本管理',
          icon: null,
          layout: 'SubpageLayout',
          noCache: true,
        },
      },
      {
        name: 'ModelOptRecord',
        path: 'optimize/record',
        hidden: true,
        component: () => import('@/views/modelOptimize/record'),
        meta: {
          title: '模型优化执行记录',
          icon: null,
          layout: 'SubpageLayout',
          noCache: true,
        },
      },
    ],
  },
];