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
  <div class="app-container">
    <!--表格渲染-->
    <el-table
      ref="table"
      v-loading="loading"
      :data="nodeList"
      highlight-current-row
    >
      <el-table-column type="expand">
        <template slot-scope="scope">
          <el-table :data="scope.row.pods">
            <el-table-column prop="podName" label="POD" how-overflow-tooltip />
            <el-table-column prop="status" label="状态" align="center">
              <template slot-scope="props">{{ dict.label.pods_status[ props.row.status ] }}</template>
            </el-table-column>
            <el-table-column label="CPU" align="center">
              <template slot-scope="props">{{ props.row.podCpu }}</template>
            </el-table-column>
            <el-table-column label="内存" align="center">
              <template slot-scope="props">{{ props.row.podMemory | parseMemory }}</template>
            </el-table-column>
            <el-table-column prop="podCard" label="GPU" align="center" />
            <el-table-column prop="createTime" label="创建时间" show-overflow-tooltip width="160">
              <template slot-scope="props">
                <span>{{ parseTime(props.row.podCreateTime) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="节点" />
      <el-table-column prop="status" label="状态" align="center">
        <template slot-scope="scope">
          <span v-if="scope.row.warning"> {{ dict.label.node_warning[ scope.row.warning ] || scope.row.warning }} </span>
          <span v-else> {{ dict.label.node_status[ scope.row.status ] || scope.row.status }} </span>
        </template>
      </el-table-column>
      <el-table-column label="CPU (使用中 / 总数)" align="center">
        <template slot-scope="scope">{{ scope.row.nodeCpu }} / {{ scope.row.totalNodeCpu }}核</template>
      </el-table-column>
      <el-table-column label="内存 (使用中 / 总数)" align="center">
        <template slot-scope="scope">{{ scope.row.nodeMemory | parseMemory }} / {{ scope.row.totalNodeMemory | parseMemory }}</template>
      </el-table-column>
      <el-table-column label="GPU (使用中 / 总数)" align="center">
        <template slot-scope="scope">{{ scope.row.gpuUsed }} / {{ scope.row.gpuCapacity }}</template>
      </el-table-column>
      <el-table-column prop="ip" label="IP" width="210" />
    </el-table>
  </div>
</template>

<script>
import { parseTime } from '@/utils';
import { getNodes } from '@/api/system/node';

export default {
  name: 'Node',
  dicts: ['node_status', 'node_warning', 'pods_status'],
  filters: {
    parseMemory(value) {
      return value.substring(0, value.indexOf('Mi')) > 1024 ? `${(value.substring(0, value.indexOf('Mi')) / 1024).toFixed(2)  }Gi` : value;
    },
  },
  data() {
    return {
      loading: false,
      nodeList: [],
    };
  },
  created() {
    this.getNodes();
  },
  methods: {
    parseTime,
    async getNodes() {
      this.loading = true;
      getNodes().then(res => {
        this.nodeList = res;
      }).finally(() => {
        this.loading = false;
      });
    },
  },
};
</script>
