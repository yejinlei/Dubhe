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
    name: '模型开发',
    path: '/development',
    hidden: false,
    component: () => import('@/layout/index'),
    meta: {
      title: '模型开发',
      icon: 'xunlianzhunbei',
      layout: null,
      noCache: true,
    },
    children: [
      {
        name: 'Notebook',            // 路由名称
        path: 'notebook',            // 路由地址
        hidden: false,               // 是否隐藏
        component: () => import('@/views/development/notebook'),   // 路由组件地址
        meta: {
          title: 'Notebook',         // 菜单名称
          icon: 'kaifahuanjing',     // 菜单图标
          layout: 'BaseLayout',      // 页面布局
          noCache: true,
        },
      },
      {
        name: 'Algorithm',
        path: 'algorithm',
        hidden: false,
        component: () => import('@/views/algorithm/index'),
        meta: {
          title: '算法管理',
          icon: 'mobanguanli',
          layout: 'BaseLayout',
          noCache: true,
        },
      },
    ],
  },
];