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
    <el-popover placement="top" width="200" trigger="click">
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
    <el-button v-if="isCurrent && !publishing" type="text" @click="gotoDetail">
      {{ isCustom ? '查看文件' : '查看标注' }}
    </el-button>
    <el-button v-if="isPreset && !publishing" type="text" @click="convert(row)">
      生成预置数据集
    </el-button>
    <el-dropdown placement="bottom">
      <el-button type="text" style="margin-left: 10px;" @click.stop="() => {}">
        更多<i class="el-icon-arrow-down el-icon--right" />
      </el-button>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item v-if="!isCurrent" @click.native="deleteItem">
          <el-button type="text">删除</el-button>
        </el-dropdown-item>
        <el-dropdown-item v-click-once :disabled="publishing" @click.native="download(row)">
          <el-button v-if="!publishing" type="text">
            导出
          </el-button>
          <el-tooltip v-else content="文件生成中，请稍后" placement="top" :open-delay="400">
            <el-button class="disabled-button" type="text">
              导出
            </el-button>
          </el-tooltip>
        </el-dropdown-item>
        <el-dropdown-item
          v-if="!isOfRecord && showOfRecord(row.annotateType)"
          @click.native="onShiftOfRecord"
        >
          <el-button type="text">转换OFRecord</el-button>
        </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </div>
</template>

<script>
import { computed } from '@vue/composition-api';
import { Message, MessageBox } from 'element-ui';

import { toFixed, downloadZipFromObjectPath } from '@/utils';
import {
  datasetStatusMap,
  isPublishDataset,
  annotationBy,
  dataTypeCodeMap,
  showOfRecord,
  isCustomDataset,
  annotationCodeMap,
} from '@/views/dataset/util';
import { toggleVersion, deleteVersion, shiftOfRecord } from '@/api/preparation/dataset';
import { TableTooltip } from '@/hooks/tooltip';

const annotationByCode = annotationBy('code');

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
    showConvert: Function,
  },
  setup(props, ctx) {
    const { actions, showConvert } = props;
    const { $router } = ctx.root;

    // 发布中
    const publishing = computed(() => isPublishDataset(props.row));
    const isCurrent = computed(() => !!props.row.isCurrent);
    const isPreset = computed(
      () => props.row.presetFlag && props.row.dataType !== dataTypeCodeMap.CUSTOM
    );
    const isOfRecord = computed(() => props.row.isOfRecord);
    const isCustom = computed(() => isCustomDataset(props.row));
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
      status: {
        label: `状\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0态`,
        value: datasetStatusMap[props.row.status].name,
      },
      fileCount: { label: '文件数量', value: props.row.fileCount },
      progressVO: { label: '标注进度', value: progressCount },
    };

    const gotoDetail = () => {
      const { annotateType } = props.row;
      if (isCustomDataset(props.row)) {
        const customUrlPrefix = annotationByCode(annotationCodeMap.CUSTOM, 'urlPrefix');
        $router.push({
          path: `/data/datasets/${customUrlPrefix}/${props.row.datasetId}`,
        });
        return false;
      }
      const urlPrefix = annotationByCode(annotateType, 'urlPrefix');
      $router.push({ path: `/data/datasets/${urlPrefix}/${props.row.datasetId}` });
      return false;
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
      MessageBox.confirm('是否要删除此版本?', '请确认', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        deleteVersion({
          datasetId: props.row.datasetId,
          versionName: props.row.versionName,
        }).then(() => {
          actions.refresh();
          Message.success('删除版本成功');
        });
      });
    };

    const download = (row) => {
      const prefixUrl = `dataset/${row.datasetId}/versionFile/${row.versionName}`;
      return downloadZipFromObjectPath(prefixUrl, `${row.datasetId}_${row.versionName}.zip`, {
        fileName: (file) => file.name.replace(`${prefixUrl}/`, ''),
        filter: (result) => {
          // 自定义数据集没有固定目录结构，直接下载即可
          if (isCustomDataset(row)) return result;
          return result.filter((item) => {
            return ['annotation', 'origin'].some((str) =>
              item.name.startsWith(`${prefixUrl}/${str}`)
            );
          });
        },
      });
    };

    const convert = (row) => {
      return showConvert(row);
    };

    const onShiftOfRecord = () => {
      shiftOfRecord({
        datasetId: props.row.datasetId,
        versionName: props.row.versionName,
      }).then(() => {
        actions.refresh();
        Message.success('开始生成OFRecord');
      });
    };

    const valueAccessor = (key, idx, data) => data[key].value || '--';
    const keyAccessor = (key, idx, data) => data[key].label;

    return {
      publishing,
      isCurrent,
      isPreset,
      isCustom,
      isOfRecord,
      title,
      labels: Object.keys(list),
      list,
      gotoDetail,
      deleteItem,
      setCurrent,
      convert,
      keyAccessor,
      valueAccessor,
      download,
      onShiftOfRecord,
      showOfRecord,
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
