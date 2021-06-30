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
  <el-dropdown trigger="click" placement="bottom-start" @command="onCommand">
    <div>
      <span>切换场景</span>
      <i class="el-icon-arrow-down el-icon--right" />
    </div>
    <el-dropdown-menu slot="dropdown">
      <el-dropdown-item command="0" :disabled="state.datasetListType === '0'" :style="choosed('0')"
        >视觉/语音/文本</el-dropdown-item
      >
      <el-dropdown-item command="1" :disabled="state.datasetListType === '1'" :style="choosed('1')"
        >医学影像</el-dropdown-item
      >
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>
import { reactive } from '@vue/composition-api';

export default {
  name: 'TenantSelector',
  props: {
    datasetListType: String,
  },
  setup(props, ctx) {
    const { $router } = ctx.root;
    const state = reactive({
      datasetListType: props.datasetListType,
    });
    const onCommand = (value) => {
      state.datasetListType = value;
      localStorage.setItem('datasetListType', state.datasetListType);
      if (value === '0') {
        $router.push({ path: '/data/datasets' });
      } else if (value === '1') {
        $router.push({ path: '/data/datasets/medical' });
      }
    };

    const choosed = (val) => (state.datasetListType === val ? 'color: #0000ff' : '');

    return {
      onCommand,
      state,
      choosed,
    };
  },
};
</script>
