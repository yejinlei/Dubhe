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
      <span :class="{primary: filtered}">{{ title }}</span>
      <i class="el-icon-arrow-down el-icon--right" />
    </div>
    <el-dropdown-menu slot="dropdown" :style="dropdownStyle">
      <el-dropdown-item
        v-for="(item, index) in list"
        :key="index"
        :command="item[valueTag]"
      >{{ item[labelTag] }}</el-dropdown-item>
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>
export default {
  props: {
    title: {
      type: String,
      required: true,
    },
    list: {
      type: Array,
      default: () => [],
    },
    // labelTag 用于标识列表中用于表示 label 的字段名, 默认为 label
    labelTag: {
      type: String,
      default: 'label',
    },
    // labelTag 用于标识列表中用于表示 value 的字段名, 默认为 value
    valueTag: {
      type: String,
      default: 'value',
    },
    // filtered 用于标识是否处于筛选状态
    filtered: {
      type: Boolean,
      default: false,
    },
    // 下拉框样式可配置
    dropdownStyle: {
      type: String,
      default: "",
    },
  },
  setup(props, ctx) {
    const onCommand = (value) => {
      ctx.emit('command', value);
    };
    return { onCommand };
  },
};
</script>
