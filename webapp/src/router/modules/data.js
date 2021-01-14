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
    name: '数据管理',
    path: '/data',
    hidden: false,
    component: () => import('@/layout/index'),
    meta: {
      title: '数据管理',
      icon: 'shujuguanli',
      layout: null,
      noCache: true,
    },
    children: [
      // BaseLayout 基本布局
      {
        name: 'DatasetFork',         // 路由名称
        path: 'datasets',            // 路由地址
        hidden: false,               // 是否隐藏
        component: () => import('@/views/dataset/fork'),   // 路由组件地址
        meta: {
          title: '数据集管理',        // 菜单名称
          icon: 'shujuguanli',       // 菜单图标
          layout: 'BaseLayout',      // 页面布局
          noCache: true,
        },
      },
      {
        name: 'LabelGroup',
        path: 'labelgroup',
        hidden: false,
        component: () => import('@/views/labelGroup/index'),
        meta: {
          title: '标签组管理',
          icon: 'mobanguanli',
          layout: 'BaseLayout',
          noCache: true,
        },
      },
      {
        name: 'Datasets', 
        path: 'datasets/list',
        hidden: true, 
        component: () => import('@/views/dataset/list'),
        meta: {
          title: '视觉/文本数据集',     
          icon: 'shujuguanli',
          layout: 'BaseLayout',
          noCache: true,
        },
      },
      {
        name: 'Entrance',
        path: 'datasets/entrance',
        hidden: true,
        component: () => import('@/views/dataset/entrance'),
        meta: {
          title: '数据集场景选择',
          icon: null,
          layout: 'BaseLayout',
          noCache: true,
        },
      },
      {
        name: 'DatasetMedical',
        path: 'datasets/medical',
        hidden: true,
        component: () => import('@/views/dataset/medical/list'),
        meta: {
          title: '医疗影像数据集',
          icon: null,
          layout: 'BaseLayout',
          noCache: true,
        },
      },
      // DetailLayout 详情页面
      {
        name: 'DatasetClassify',
        path: 'datasets/classify/:datasetId',
        hidden: true,
        component: () => import('@/views/dataset/classify'),
        meta: {
          title: '图像分类',
          icon: null,
          layout: 'DetailLayout',
          noCache: true,
        },
      },
      {
        name: 'TextClassify',
        path: 'datasets/textclassify/:datasetId',
        hidden: true,
        component: () => import('@/views/dataset/nlp/textClassify'),
        meta: {
          title: '文本分类',
          icon: null,
          layout: 'DetailLayout',
          noCache: true,
        },
      },
      {
        name: 'TextAnnotation',
        path: 'datasets/text/annotation/:datasetId',
        hidden: true,
        component: () => import('@/views/dataset/nlp/annotation'),
        meta: {
          title: '文本标注',
          icon: null,
          layout: 'DetailLayout',
          noCache: true,
        },
      },
      // DatasetLayout 数据集页面
      {
        name: 'AnnotateDatasetFile',
        path: 'datasets/annotate/:datasetId/file/:fileId',
        hidden: true,
        component: () => import('@/views/dataset/annotate'),
        meta: {
          title: '目标检测',
          icon: null,
          layout: 'DatasetLayout',
          noCache: true,
        },
      },
      {
        name: 'AnnotateDataset',
        path: 'datasets/annotate/:datasetId',
        hidden: true,
        component: () => import('@/views/dataset/annotate'),
        meta: {
          title: '目标检测',
          icon: null,
          layout: 'DatasetLayout',
          noCache: true,
        },
      },
      {
        name: 'TrackDatasetFile',
        path: 'datasets/track/:datasetId/file/:fileId',
        hidden: true,
        component: () => import('@/views/dataset/annotate'),
        meta: {
          title: '目标跟踪',
          icon: null,
          layout: 'DatasetLayout',
          noCache: true,
        },
      },
      {
        name: 'TrackDataset',
        path: 'datasets/track/:datasetId',
        hidden: true,
        component: () => import('@/views/dataset/annotate'),
        meta: {
          title: '目标跟踪',
          icon: null,
          layout: 'DatasetLayout',
          noCache: true,
        },
      },
      {
        name: 'SegmentationDatasetFile',
        path: 'datasets/segmentation/:datasetId/file/:fileId',
        hidden: true,
        component: () => import('@/views/dataset/annotate'),
        meta: {
          title: '图像分割',
          icon: null,
          layout: 'DatasetLayout',
          noCache: true,
        },
      },
      {
        name: 'SegmentationDataset',
        path: 'datasets/segmentation/:datasetId',
        hidden: true,
        component: () => import('@/views/dataset/annotate'),
        meta: {
          title: '图像分割',
          icon: null,
          layout: 'DatasetLayout',
          noCache: true,
        },
      },
      // SubpageLayout 二级页面
      {
        name: 'LabelGroupCreate',
        path: 'labelgroup/create',
        hidden: true,
        component: () => import('@/views/labelGroup/labelGroupForm'),
        meta: {
          title: '创建标签组',
          icon: null,
          layout: 'SubpageLayout',
          noCache: true,
        },
      },
      {
        name: 'LabelGroupDetail',
        path: 'labelgroup/detail',
        hidden: true,
        component: () => import('@/views/labelGroup/labelGroupForm'),
        meta: {
          title: '标签组详情',
          icon: null,
          layout: 'SubpageLayout',
          noCache: true,
        },
      },
      {
        name: 'LabelGroupEdit',
        path: 'labelgroup/edit',
        hidden: true,
        component: () => import('@/views/labelGroup/labelGroupForm'),
        meta: {
          title: '编辑标签组',
          icon: null,
          layout: 'SubpageLayout',
          noCache: true,
        },
      },
      {
        name: 'DatasetVersion',
        path: 'datasets/:datasetId/version',
        hidden: true,
        component: () => import('@/views/dataset/version'),
        meta: {
          title: '数据集版本管理',
          icon: null,
          layout: 'SubpageLayout',
          noCache: true,
        },
      },
      // FullpageLayout 全屏布局
      {
        name: 'DatasetMedicalViewer',
        path: 'datasets/medical/viewer/:medicalId',
        hidden: true,
        component: () => import('@/views/dataset/medical/viewer'),
        meta: {
          title: '医学影像阅读',
          icon: 'beauty',
          layout: 'FullpageLayout',
          noCache: true,
        },
      },
    ],
  },
];