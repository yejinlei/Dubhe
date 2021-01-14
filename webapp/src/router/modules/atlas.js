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
    name: '模型炼知',
    path: '/atlas',
    hidden: false,
    component: () => import('@/layout/index'),
    meta: {
      title: '模型炼知',
      icon: 'icon_huabanfuben1',
      layout: null,
      noCache: true,
    },
    children: [
      {
        name: 'Measure',            // 路由名称
        path: 'measure',            // 路由地址
        hidden: false,              // 是否隐藏
        component: () => import('@/views/atlas/measure'),  // 路由组件地址
        meta: {
          title: '度量管理',         // 菜单标题
          icon: 'icon_huabanfuben1',// 菜单图标
          layout: 'BaseLayout',     // 页面布局
          noCache: true,
        },
      },
      {
        name: 'AtlasGraphVisual',
        path: 'graphvisual',
        hidden: false,
        component: () => import('@/views/atlas/graphVisual'),
        meta: {
          title: '图谱可视化',
          icon: 'icon_huabanfuben1',
          layout: 'BaseLayout',
          noCache: true,
        },
      },
      {
        name: 'AtlasGraph',
        path: 'graph',
        hidden: false,
        component: () => import('@/views/atlas/graphList'),
        meta: {
          title: '图谱列表',
          icon: 'icon_huabanfuben1',
          layout: 'BaseLayout',
          noCache: true,
        },
      },
    ],
  },
];