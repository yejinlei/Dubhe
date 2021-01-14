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
  <el-form-item>
    <slot name="label">
      <LabelTip />
    </slot>
    <div class="flex flex-between">
      <InfoSelect
        v-model="state.label"
        :innerRef="innerRef"
        style="width: 68%;"
        placeholder="选择已有标签或新建标签"
        :dataSource="dataSource"
        value-key="value"
        default-first-option
        filterable
        allow-create
        @change="handleChange"
      />
      <el-button size="mini" type="primary" @click="postLabel">确定</el-button>
    </div>
  </el-form-item>
</template>

<script>
import { reactive, ref } from '@vue/composition-api';

import InfoSelect from '@/components/InfoSelect';
import LabelTip from './labelTip';

export default {
  name: 'SelectLabel',
  components: {
    LabelTip,
    InfoSelect,
  },
  props: {
    dataSource: {
      type: Array,
      default: () => ([]),
    },
    handleLabelChange: Function,
  },
  setup(props, ctx) {
    const { handleLabelChange } = props;
    const selectRef = ref(null);

    const state = reactive({
      label: undefined,
    });

    const postLabel = () => {
      ctx.emit('postLabel');
      state.label = undefined;
    };

    const handleChange = (params) => {
      handleLabelChange(params, () => {
        state.label = undefined;
      });
    };

    return {
      state,
      postLabel,
      handleChange,
      innerRef: () => selectRef,
    };
  },
};
</script>
