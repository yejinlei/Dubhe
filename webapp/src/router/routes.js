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

import Layout from '@/layout/index';
import { Scalars, ScalarsPanel } from '@/views/visual/Visual/scalars';
import { Medias, MediasPanel } from '@/views/visual/Visual/medias';
import { Graphs, GraphsPanel } from '@/views/visual/Visual/graphs';
import { Hyperparms, HyperparmsPanel } from '@/views/visual/Visual/hyperparms';
import { Features, FeaturesPanel } from '@/views/visual/Visual/features';
import { Customs, CustomsPanel } from '@/views/visual/Visual/customs';
import { Statistics, StatisticsPanel } from '@/views/visual/Visual/statistics';
import { ROCs, ROCsPanel } from '@/views/visual/Visual/rocs';
import { Embeddings, EmbeddingsPanel } from '@/views/visual/Visual/embeddings';
import { Exception, ExceptionPanel } from '@/views/visual/Visual/exception';

const constantRoutes = [
  {
    name: 'Login',
    path: '/login',
    meta: { title: '登录' },
    component: () => import('@/views/login'),
    hidden: true,
  },
  {
    name: 'Register',
    path: '/register',
    meta: { title: '注册' },
    component: () => import('@/views/register'),
    hidden: true,
  },
  {
    name: 'Resetpassword',
    path: '/resetpassword',
    meta: { title: '找回密码' },
    component: () => import('@/views/resetpassword'),
    hidden: true,
  },
  {
    name: 'Home',
    path: '/home',
    component: () => import('@/views/home'),
    hidden: true,
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
  },
  // 个人中心
  {
    path: '/user',
    component: Layout,
    hidden: true,
    children: [
      {
        path: 'center',
        component: () => import('@/views/user/center'),
        name: 'UserCenter',
        meta: { title: '个人中心' },
      },
    ],
  },
  // 可视化
  {
    name: 'VISUAL',
    path: '/visual',
    component: () => import('@/views/visual/Layout'),
    meta: { title: '可视分析' },
    hidden: true,
    children: [
      {
        path: 'graph',
        name: 'Graph',
        meta: { title: '可视分析-模型结构' },
        components: {
          default: Graphs,
          right: GraphsPanel,
        },
      },
      {
        path: 'scalar',
        name: 'Scalar',
        meta: { title: '可视分析-标量数据' },
        components: {
          default: Scalars,
          right: ScalarsPanel,
        },
      },
      {
        path: 'media',
        name: 'Media',
        meta: { title: '可视分析-媒体数据' },
        components: {
          default: Medias,
          right: MediasPanel,
        },
      },
      {
        path: 'statistic',
        name: 'Statistic',
        meta: { title: '可视分析-统计分析' },
        components: {
          default: Statistics,
          right: StatisticsPanel,
        },
      },
      {
        path: 'embedding',
        name: 'Embedding',
        meta: { title: '可视分析-降维分析' },
        components: {
          default: Embeddings,
          right: EmbeddingsPanel,
        },
      },
      {
        path: 'feature',
        name: 'Feature',
        meta: { title: '可视分析-特征分析' },
        components: {
          default: Features,
          right: FeaturesPanel,
        },
      },
      {
        path: 'roc',
        name: 'Roc',
        meta: { title: '可视分析-评测曲线' },
        components: {
          default: ROCs,
          right: ROCsPanel,
        },
      },
      {
        path: 'hyperparm',
        name: 'Hyperparm',
        meta: { title: '可视分析-超参分析' },
        components: {
          default: Hyperparms,
          right: HyperparmsPanel,
        },
      },
      {
        path: 'exception',
        name: 'Exception',
        meta: { title: '可视分析-异常检测' },
        components: {
          default: Exception,
          right: ExceptionPanel,
        },
      },
      {
        path: 'custom',
        name: 'Custom',
        meta: { title: '可视分析-用户定制' },
        components: {
          default: Customs,
          right: CustomsPanel,
        },
      },
    ],
  },
];

export default constantRoutes;
