/** Copyright 2020 Tianshu AI Platform. All Rights Reserved. * * Licensed under the Apache License,
Version 2.0 (the "License"); * you may not use state file except in compliance with the License. *
You may obtain a copy of the License at * * http://www.apache.org/licenses/LICENSE-2.0 * * Unless
required by applicable law or agreed to in writing, software * distributed under the License is
distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. * See the License for the specific language governing permissions and * limitations under
the License. * ============================================================= */

<template>
  <el-popover v-if="show" placement="right" width="400" trigger="hover" :offset="20">
    <el-table :data="msgList" :show-header="false" :empty-text="emptyText">
      <el-table-column prop="name" show-overflow-tooltip />
      <el-table-column prop="message" show-overflow-tooltip />
    </el-table>
    <i slot="reference" class="el-icon-warning-outline primary f16 v-text-top" />
  </el-popover>
</template>

<script>
import { computed } from '@vue/composition-api';

export default {
  props: {
    show: Boolean,
    statusDetail: {
      type: String,
      require: true,
    },
    emptyText: {
      type: String,
      default: '暂无提示信息',
    },
  },
  setup(props) {
    const msgList = computed(() => {
      try {
        const msg = JSON.parse(props.statusDetail);
        const list = Object.keys(msg).map((m) => ({ name: m, message: msg[m] }));
        return list;
      } catch (e) {
        return [];
      }
    });

    return {
      msgList,
    };
  },
};
</script>
