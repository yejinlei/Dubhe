/** Copyright 2020 Zhejiang Lab. All Rights Reserved.
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
  <div class="workspace-settings">
    <el-form label-position="top" @submit.native.prevent>
      <el-form-item v-if="state.datasetInfo.value.labelGroupId" label="标签组" style="margin-bottom: 0;">
        <div style="margin-top: -10px;">
          <span class="vm">{{ state.datasetInfo.value.labelGroupName }} &nbsp;</span>
          <el-link
            target="_blank"
            type="primary"
            :underline="false"
            class="vm"
            :href="`/data/labelgroup/detail?id=${state.datasetInfo.value.labelGroupId}`"
          >
            查看详情
          </el-link>
        </div>
      </el-form-item>
      <SelectLabel
        v-if="!isPresetLabel"
        :dataSource="api.systemLabels"
        :handleLabelChange="handleLabelChange"
        @postLabel="postLabel"
      />
      <LabelList 
        :labels="labels"
        :editLabel="edit"
        :annotations="state.annotations.value"
        :currentAnnotationId="api.currentAnnotationId"
        :updateState="updateState"
        :getColorLabel="getColorLabel"
        :findRowIndex="findRowIndex"
      />
      <Annotations
        :annotations="state.annotations.value"
        :currentAnnotationId="state.currentAnnotationId.value"
        :labels="labels"
        :updateState="updateState"
        :getColorLabel="getColorLabel"
        :findRowIndex="findRowIndex"
        :deleteAnnotation="deleteAnnotation"
      />
      <Enhance
        v-if="!isTrack && state.hasEnhanceRecord.value"
        :fileInfo="state.fileInfo.value"
        :fileId="state.fileId.value"
        :datasetId="state.datasetId.value"
      />
      <Footer
        :isTrack="isTrack"
        :updateState="updateState"
        :showScore="state.showScore.value"
        :toggleShowScore="toggleShowScore"
        :showTag="state.showTag.value"
        :toggleShowTag="toggleShowTag"
        :showId="state.showId.value"
        :toggleShowId="toggleShowId"
      />
    </el-form>
  </div>
</template>

<script>
import { Message } from 'element-ui';
import { inject, watch, reactive, onMounted, computed } from '@vue/composition-api';
import { isNil } from 'lodash';

import { getAutoLabels, editLabel } from '@/api/preparation/datalabel';
import { labelsSymbol } from '@/views/dataset/util';

import SelectLabel from './selectLabel';
import LabelList from './labelList';
import Annotations from './annotations';
import Enhance from './enhance';
import Footer from './footer';

export default {
  name: 'SettingContainer',
  components: {
    SelectLabel,
    Annotations,
    Footer,
    LabelList,
    Enhance,
  },
  props: {
    createLabel: Function,
    getColorLabel: Function,
    updateState: Function,
    queryLabels: Function,
    findRowIndex: Function,
    deleteAnnotation: Function,
    state: Object,
    isTrack: Boolean,
  },
  setup(props) {
    const { createLabel, updateState, queryLabels } = props;
    const api = reactive({
      newLabel: undefined,
      currentAnnotationId: undefined,
    });
    // 当前所有标签信息
    const labels = inject(labelsSymbol);

    // 更新标签
    const refreshLabel = async() => {
      const nextLabels = await queryLabels();
      // 更新全局 provide
      updateState({
        labels: nextLabels,
      });
    };

    // 编辑标签
    const edit = (labelId, data) => {
      return editLabel(labelId, data).then(refreshLabel);
    };

    const addLabel = (label) => {
      api.newLabel = label;
    };

    const postLabel = () => {
      if (api.newLabel) {
        if (labels.value.findIndex(d => d.name === api.newLabel) > -1) {
          Message.warning('当前标签已存在');
          return;
        }
        createLabel({ name: api.newLabel }).then(() => {
          Message.success(`标签[${api.newLabel}]创建成功`);
          api.newLabel = undefined;
          refreshLabel();
        });
      } else {
        Message.warning('请选择标签');
      }
    };

    const handleLabelChange = (value, callback) => {
      // 新建标签
      if (!isNil(value)) {
        // 如果不是系统标签，才会选择新建
        if (api.systemLabels.findIndex(d => d.value === value) === -1) {
          addLabel(value);
          // 新建标签直接触发创建
          postLabel();
          typeof callback === 'function' && callback();
        } else {
          const systemLabel = api.systemLabels.find(d => d.value === value) || {};
          systemLabel.label && addLabel(systemLabel.label);
        }
      }
    };

    const getSystemLabel = () => {
      getAutoLabels().then(res => {
        const labelsObj = res.map((item) => ({
          value: item.id,
          label: item.name,
          color: item.color,
          chosen: false,
        }));
        Object.assign(api, {
          systemLabels: labelsObj,
        });
      });
    };

    const toggleShowScore = (val) => {
      updateState({
        showScore: val,
      });
    };

    const toggleShowTag = (val) => {
      const newState = {
        showTag: val,
      };
      // 视频跟踪模式下标签和标注 Id 互斥
      if (!!val && !!props.isTrack) {
        newState.showId = false;
      }
      updateState(newState);
    };

    const toggleShowId = (val) => {
      const newState = {
        showId: val,
      };
      // 视频跟踪模式下标签和标注 Id 互斥
      if (!!val && !!props.isTrack) {
        newState.showTag = false;
      }
      updateState(newState);
    };

    // labelGroupType 标签组类型：0: private 私有标签组,  1:public 公开标签组
    const isPresetLabel = computed(() => props.state.labelGroupType === 1);

    watch(() => props.state, (next) => {
      if ('currentAnnotationId' in next) {
        api.currentAnnotationId = next.currentAnnotationId || [];
      }
    });

    onMounted(() => {
      getSystemLabel();
    });

    return {
      api,
      toggleShowScore,
      toggleShowTag,
      toggleShowId,
      labels,
      postLabel,
      addLabel,
      edit,
      handleLabelChange,
      isPresetLabel,
    };
  },
};
</script>
<style lang="scss">
  .workspace-settings {
    padding: 28px 28px 0;
    margin-bottom: 33px;
    overflow-y: auto;
    background-color: rgb(242, 242, 242);

    .tips-wrapper {
      div {
        width: 50%;
      }
    }

    .setting-container-footer {
      margin-top: 10px;

      .el-form-item {
        display: flex;
        justify-content: space-between;
        margin-bottom: 0;

        &::before,
        &::after {
          content: none;
        }
      }
    }

    .el-icon-edit {
      margin-left: 4px;
    }
  }

  @media (max-width: 1440px) {
    .workspace-settings {
      padding: 10px 15px 0;
    }
  }
</style>
