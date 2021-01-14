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
    name: '训练管理',
    path: '/training',
    hidden: false,
    component: () => import('@/layout/index'),
    meta: {
      title: '训练管理',
      icon: 'xunlianguocheng',
      layout: null,
      noCache: true,
    },
    children: [
      {
        name: 'TrainingImage',       // 路由名称
        path: 'image',               // 路由地址
        hidden: false,               // 是否隐藏
        component: () => import('@/views/trainingImage/index'),  // 路由组件地址
        meta: {
          title: '镜像管理',          // 菜单标题
          icon: 'jingxiangguanli',   // 菜单图标
          layout: 'BaseLayout',      // 页面布局
          noCache: true,
        },
      },
      {
        name: 'TrainingJob',
        path: 'job',
        hidden: false,
        component: () => import('@/views/trainingJob/index'),
        meta: {
          title: '训练任务',
          icon: 'renwuguanli',
          layout: 'BaseLayout',
          noCache: true,
        },
      },
      {
        name: 'JobDetail',
        path: 'jobdetail',
        hidden: true,
        component: () => import('@/views/trainingJob/detail'),
        meta: {
          title: '任务详情',
          icon: null,
          layout: 'SubpageLayout',
          noCache: true,
        },
      },
      {
        name: 'jobAdd',
        path: 'jobadd',
        hidden: true,
        component: () => import('@/views/trainingJob/add'),
        meta: {
          title: '添加任务',
          icon: null,
          layout: 'SubpageLayout',
          noCache: true,
        },
      },
    ],
  },
];