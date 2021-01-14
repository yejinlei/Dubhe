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
  <div class="mb-10">
    <div class="flex flex-between flex-wrap flex-vertical-align">
      <el-form-item v-show="showLabel" style="padding: 0; margin-bottom: 0;">
        <label class="el-form-item__label">
          <span class="vm">{{ labelsTitle }}</span>
          <Add @handleOk="handleCreateLabel" />
        </label>
      </el-form-item>
      <SearchLabel ref="searchRef" style="padding-bottom: 10px;" @change="handleSearch" />
    </div>
    <div style="max-height: 200px; padding: 0 2.5px; overflow: auto;">
      <div v-if="state.labelData.length">
        <div v-for="item in state.labelData" :key="item.id">
          <el-tag
            class="tag-item"
            :title="item.name"
            :color="item.color"
            :style="getStyle(item)"
            @click="handleClick(item)"
          >
            {{ item.name }}
          </el-tag>
        </div>
      </div>
      <div v-else class="g6 f14">
        暂无标签，请添加
      </div>
    </div>
  </div>
</template>

<script>
import { Message } from 'element-ui';
import { reactive, watch, computed, ref } from '@vue/composition-api';
import { colorByLuminance } from '@/utils';
import SearchLabel from '@/views/dataset/components/searchLabel';
import Add from './add';

export default {
  name: 'LabelList',
  components: {
    SearchLabel,
    Add,
  },
  props: {
    labels: {
      type: Array,
      default: () => ([]),
    },
    currentAnnotationId: {
      type: String,
      default: undefined,
    },
    handleLabel: Function,
    annotations: Array,
    createLabel: Function,
    labelClickable: {
      type: Boolean,
      default: true,
    },
  },
  setup(props) {
    const { annotations: rawAnnotations, labelClickable, handleLabel } = props;

    const searchRef = ref(null);

    const state = reactive({
      annotations: rawAnnotations,
      labelData: props.labels,
      currentAnnotationId: props.currentAnnotationId,
    });
    // 根据亮度来决定颜色
    const getStyle = (item) => {
      const color = colorByLuminance(item.color);
      return {
        color,
        display: 'inline-block',
        width: 'auto',
        minWidth: "120px",
        cursor: labelClickable ? "pointer" : "unset",
      };
    };
    // label点击事件处理
    const handleClick = (item) => {
      labelClickable ? handleLabel(item, event) : null;
    };
    // 查询分类标签
    const handleSearch = (label) => {
      if (label) {
        state.labelData = props.labels.filter(d => d.name.includes(label));
      } else {
        state.labelData = props.labels;
      }
    };

    const labelsTitle = computed(() => {
      return `全部标签(${props.labels.length})`;
    });

    const showLabel = computed(() => {
      if (!searchRef.value) return true;
      return !searchRef.value.state.open;
    });

    const handleCreateLabel = (form) => {
      // TODO: 判断是否重复
      if (state.labelData.findIndex(d => d.name === form.name) > -1) {
          Message.warning('当前标签已存在');
          return;
        }
      props.createLabel(form);
    };

    watch(() => props.labels, (next) => {
      state.labelData = next;
    });

    watch(() => props.currentAnnotationId, (next) => {
      state.currentAnnotationId = next;
    });

    return {
      state,
      searchRef,
      labelsTitle,
      getStyle,
      handleClick,
      showLabel,
      handleCreateLabel,
      handleSearch,
    };
  },
};
</script>
<style lang="scss" scoped>
  .el-icon-edit {
    padding: 0 4px;
    margin-left: 4px;
  }
</style>
