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

<template>
  <div class="app-container">
    <h2>数据集版本管理</h2>
    <InfoTable
      :columns="columns"
      :request="request"
      :params="queryParams"
      :actionRef="actionRefFn"
    />
  </div>
</template>

<script>
import { createElement, ref } from '@vue/composition-api';
import InfoTable from '@/components/InfoTable';

import api from '@/utils/request';
import { parseTime } from '@/utils';
import Actions from './actions';
import { dataTypeMap, annotationMap } from '../util';

export default {
  name: 'DatasetVersion',
  components: {
    InfoTable,
  },
  setup(props, ctx) {
    const { $route } = ctx.root;
    const { params = {}} = $route;
    const actionRef = ref(null);

    const columns = [
      { prop: 'datasetId', label: 'ID', minWidth: 100, sortable: true },
      { prop: 'name', label: '名称', minWidth: 160 },
      { prop: 'dataType', label: '数据类型', minWidth: 100, render: ({ row }) => {
        return dataTypeMap[row.dataType];
      } },
      { prop: 'annotateType', label: '标注类型', minWidth: 100, render: ({ row }) => {
        return annotationMap[row.annotateType].name;
      } },
      { prop: 'isCurrent', label: '是否为当前版本', minWidth: 120, render: ({ row }) => {
        return row.isCurrent ? '是' : '否';
      } },
      { prop: 'versionName', label: '版本号', minWidth: 100, sortable: true },
      { prop: 'createTime', label: '创建时间', minWidth: 150, sortable: true, render: ({ row }) => parseTime(row.createTime) },
      { prop: 'versionNote', label: '版本描述' },
      { label: '操作', minWidth: 240, align: 'left', render: ({ row }, actions) => {
        return [
          createElement(Actions, {
            props: {
              row,
              actions,
            },
          }),
        ];
      } },
    ];

    const queryParams = {
      datasetId: params.datasetId,
    };

    const request = (options) => {
      return api(`api/data/datasets/versions/`, {
        params: options,
      });
    };

    return {
      parseTime,
      request,
      queryParams,
      columns,
      actionRefFn: () => actionRef,
    };
  },
};
</script>
