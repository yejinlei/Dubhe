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
  <div class="info-table-action-cell">
    <el-button v-if="!isCurrent" type="text" @click="setCurrent">设置为当前版本</el-button>
    <el-popover
      placement="top"
      width="200"
      trigger="click"
    >
      <div>
        <TableTooltip
          :keys="labels"
          :title="title"
          :data="list"
          :keyAccessor="keyAccessor"
          :valueAccessor="valueAccessor"
        />
      </div>
      <el-button slot="reference" type="text">详情</el-button>
    </el-popover>
    <el-button v-if="isCurrent && !publishing" type="text" @click="gotoDetail">查看标注</el-button>
    <el-popconfirm
      v-if="!isCurrent"
      popper-class="reannotate-popconfirm"
      title="确定删除这个版本吗？"
      @onConfirm="deleteItem"
    >
      <el-button slot="reference" type="text">删除</el-button>
    </el-popconfirm>
    <el-button v-if="!publishing" v-click-once type="text" @click="download(row)">
      导出
    </el-button>
    <el-tooltip v-else content="文件生成中，请稍后" placement="top" :open-delay="400">
      <el-button class="disabled-button" type="text">
        导出
      </el-button>
    </el-tooltip>
  </div>
</template>

<script>
import { computed } from '@vue/composition-api';
import { Message } from 'element-ui';

import { toFixed, downloadZipFromObjectPath } from '@/utils';
import { datasetStatusMap, annotationMap, isPublishDataset } from '@/views/dataset/util';
import { toggleVersion, deleteVersion } from '@/api/preparation/dataset';
import { TableTooltip } from '@/hooks/tooltip';

export default {
  name: 'Actions',
  components: {
    TableTooltip,
  },
  props: {
    row: {
      type: Object,
      default: () => ({}),
    },
    actions: Object,
  },
  setup(props, ctx) {
    const { actions } = props;
    const { $router } = ctx.root;
    
    // 发布中
    const publishing = computed(() => isPublishDataset(props.row));
    const isCurrent = computed(() => !!props.row.isCurrent);
    const title = computed(() => `${props.row.name}(${props.row.versionName})`);

    const calculate = (vo = {}) => {
      const { finished, unfinished, autoFinished, finishAutoTrack } = vo;
      const allFinished = finished + autoFinished + finishAutoTrack;
      if (allFinished === 0) return 0;
      const total = allFinished + unfinished;
      return `${toFixed(allFinished / total)}%`;
    };

    const progressCount = props.row.progressVO ? calculate(props.row.progressVO) : '--';

    const list = {
      status: { label: `状\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0态`, value: datasetStatusMap[props.row.status].name },
      fileCount: { label: '文件数量', value: props.row.fileCount },
      progressVO: { label: '标注进度', value: progressCount },
    };

    const gotoDetail = () => {
      const {annotateType} = props.row;
      $router.push({ path: `/data/datasets/${annotationMap[annotateType].urlPrefix}/${props.row.datasetId}` });
    };

    // 设置为当前版本
    const setCurrent = () => {
      toggleVersion({
        datasetId: props.row.datasetId,
        versionName: props.row.versionName,
      }).then(() => {
        actions.refresh();
        Message.success('切换版本成功');
      });
    };

    const deleteItem = () => {
      deleteVersion({
        datasetId: props.row.datasetId,
        versionName: props.row.versionName,
      }).then(() => {
        actions.refresh();
        Message.success('删除版本成功');
      });
    };

    const download = (row) => {
      const prefixUrl = `dataset/${row.datasetId}/versionFile/${row.versionName}`;
      return downloadZipFromObjectPath(
        prefixUrl,
        `${row.datasetId}_${row.versionName}.zip`,
        {
          fileName: file => (file.name.replace(`${prefixUrl}/`, '')),
          filter: result => result.filter(item => {
            return ['annotation', 'origin'].some(str => item.name.startsWith(`${prefixUrl}/${str}`));
          }),
        });
    };

    const valueAccessor = (key, idx, data) => data[key].value || '--';
    const keyAccessor = (key, idx, data) => data[key].label;

    return {
      publishing,
      isCurrent,
      title,
      labels: Object.keys(list),
      list,
      gotoDetail,
      deleteItem,
      setCurrent,
      keyAccessor,
      valueAccessor,
      download,
    };
  },
};
</script>
<style lang="scss">
.tooltip-item-row {
  display: flex;
  margin-bottom: 4px;
  font-size: 14px;
  white-space: nowrap;

  .tooltip-item-label {
    min-width: 88px;
  }

  .tooltip-item-text {
    flex: 1;
  }

  &:last-child {
    margin-bottom: 0;
  }
}
</style>
<style rel="stylesheet/scss" lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.disabled-button {
  color: $disableColor;
  cursor: default !important;
}
</style>
