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
    name: '控制台',
    path: '/system',
    hidden: false,
    component: () => import('@/layout/index'),
    meta: {
      title: '控制台',
      icon: 'kongzhitaixitongguanliyuankejian',
      layout: null,
      noCache: true,
    },
    children: [
      {
        name: 'SystemUser',          // 路由名称
        path: 'user',                // 路由地址
        hidden: false,               // 是否隐藏
        component: () => import('@/views/system/user/index'), // 路由组件地址
        meta: {
          title: '用户管理',          // 菜单标题
          icon: 'yonghuguanli',      // 菜单图标
          layout: 'BaseLayout',      // 页面布局
          noCache: true,
        },
      },
      {
        name: 'SystemRole',
        path: 'role',
        hidden: false,
        component: () => import('@/views/system/role/index'),
        meta: {
          title: '角色管理',
          icon: 'jiaoseguanli',
          layout: 'BaseLayout',
          noCache: true,
        },
      },
      {
        name: 'SystemDict',
        path: 'dict',
        hidden: false,
        component: () => import('@/views/system/dict/index'),
        meta: {
          title: '字典管理',
          icon: 'mobanguanli',
          layout: 'BaseLayout',
          noCache: true,
        },
      },
      {
        name: 'SystemNode',
        path: 'node',
        hidden: false,
        component: () => import('@/views/system/node/index'),
        meta: {
          title: '集群状态',
          icon: 'jiqunguanli',
          layout: 'BaseLayout',
          noCache: true,
        },
      },
    ],
  },
];