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
  <div>
    <el-form-item
      v-for="(key, index) in keys"
      :key="key"
      class="mb-10"
      :label="'自定义标签' + (key + 1)"
      :prop="'labels.' + index"
      :rules="rules"
    >
      <div class="flex">
        <InfoSelect
          :value="list[index].id || list[index].name"
          style="width: 200px; margin-right: 10px;"
          placeholder="选择或新建标签"
          :dataSource="activeLabels"
          valueKey="id"
          labelKey='name'
          default-first-option
          filterable
          allow-create
          :disabled="!editAble && isOriginList(list[index])"
          @change="params => handleChange(key, params)"
        />
        <el-input v-model="list[index].name" :disabled="!editAble && isOriginList(list[index])" class='dn'></el-input>
        <el-color-picker v-model="list[index].color" :disabled="!editAble && isOriginList(list[index])" size="small" />
        <span style="width: 50px; margin-left: 10px; line-height: 32px;">
          <i 
            v-if="keys.length > 1 && addAble"
            class="el-icon-remove-outline vm cp"
            :class="!editAble && isOriginList(list[index]) ? 'disabled' : ''"
            style="font-size: 20px;"
            @click.prevent="remove(key)"
          />
          <i 
            v-if="index === (keys.length - 1) && addAble" 
            class="el-icon-circle-plus-outline vm cp"
            :class="!addAble ? 'disabled' : ''"
            style="font-size: 20px;" 
            @click="add"
          />
        </span>
      </div>
    </el-form-item>
  </div>
</template>
<script>
import InfoSelect from '@/components/InfoSelect';
import { validateLabel } from '@/utils/validate';

  export default {
    name: 'DynamicField',
    components: {
      InfoSelect,
    },
    props: {
      actionType: String,
      list: {
        type: Array,
        deafault: () => ([]),
      },
      activeLabels: {
        type: Array,
        deafault: () => ([]),
      },
      originList: {
        type: Array,
        deafault: () => ([]),
      },
      keys: {
        type: Array,
        deafault: () => ([]),
      },
      remove: Function,
      add: Function,
      handleChange: Function,
      validateDuplicate: Function,
    },
    setup(props) {
      const rules = [
        { validator: validateLabel, trigger: ['change', 'blur'] },
        { validator: props.validateDuplicate, trigger: ['change', 'blur'] },
      ];
      // 可以添加
      const addAble = ['create', 'edit'].includes(props.actionType);
      const editAble = props.actionType === 'create';

      const isOriginList = item => {
        const isOrigin = props.originList.findIndex(d => d.id === item.id) > -1;
        return isOrigin;
      };

      return {
        rules,
        editAble,
        addAble,
        isOriginList,
      };
    },
  };
</script>