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
      <SelectLabel
        v-if="!isPresetLabel"
        :dataSource="api.systemLabels"
        :handleLabelChange="handleLabelChange"
        @postLabel="postLabel"
      />
      <LabelList :labels="labels" />
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
import { inject, reactive, onMounted, computed } from '@vue/composition-api';
import { isNil } from 'lodash';

import { getAutoLabels } from '@/api/preparation/datalabel';
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

    const addLabel = (label) => {
      api.newLabel = label;
    };

    const handleLabelChange = value => {
      // 新建标签
      if (!isNil(value)) {
        // 如果不是系统标签，才会选择新建
        if (api.systemLabels.findIndex(d => d.value === value) === -1) {
          addLabel(value);
        } else {
          const systemLabel = api.systemLabels.find(d => d.value === value) || {};
          systemLabel.label && addLabel(systemLabel.label);
        }
      }
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

    // 使用的是预置标签时type大于1，目前自定义标签type为0，自动标注标签为1
    const isPresetLabel = computed(() => labels.value && labels.value[0] && labels.value[0].type > 1);

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
