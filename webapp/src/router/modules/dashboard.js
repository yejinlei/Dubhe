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
    path: '/dashboard',   
    component: () => import('@/layout/index'),  // 路由组件路径
    hidden: false,        // 是否隐藏菜单
    children: [
      {
        name: 'Dashboard',   // 路由名称
        path: '/dashboard',  // 路由地址
        meta: {
          title: '概览',     // 菜单名称
          icon: 'yibiaopan', // 菜单图标
          // 页面布局: 'BaseLayout'基本布局, 'SubpageLayout'二级页面, 'DetailLayout'详情页面, 'DatasetLayout'数据集页面, 'FullpageLayout'数据集页面
          layout: 'BaseLayout',
          noCache: true,
        },
        component: () => import('@/views/dashboard/dashboard'),
      },
    ],
  },
];
