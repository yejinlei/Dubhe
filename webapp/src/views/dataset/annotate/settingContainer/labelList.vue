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
  <div v-if="labels.length" class="mb-10">
    <div class="flex flex-between flex-wrap flex-vertical-align">
      <el-form-item :label="labelsTitle" style=" max-width: 39.9%; padding: 0; margin-bottom: 0;" />
      <SearchLabel style="padding-bottom: 10px;" @change="handleSearch" />
    </div>
    <div style="max-height: 200px; padding: 0 2.5px; overflow: auto;">
      <el-row :gutter="5" style="clear: both;">
        <el-col v-for="item in state.labelData" :key="item.id" :span="8">
          <el-tag class="tag-item" :title="item.name" :color="item.color" :style="getStyle(item)">{{ item.name }}</el-tag>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { reactive, watch, computed } from '@vue/composition-api';
import SearchLabel from '@/views/dataset/components/searchLabel';

const chroma = require('chroma-js');

export default {
  name: 'LabelList',
  components: {
    SearchLabel,
  },
  props: {
    labels: {
      type: Array,
      default: () => ([]),
    },
  },
  setup(props) {
    const state = reactive({
      labelData: props.labels,
    });
    // 根据亮度来决定颜色
    const getStyle = (item) => {
      if (item.color && chroma(item.color).luminance() < 0.5) {
        return {
          color: '#fff',
        };
      }
      return {
        color: '#000',
      };
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

    watch(() => props.labels, (next) => {
      state.labelData = next;
    });

    return {
      state,
      labelsTitle,
      getStyle,
      handleSearch,
    };
  },
};
</script>
