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
    <!--表格渲染-->
    <el-table
      ref="table"
      v-loading="crud.loading"
      :data="crud.data"
      highlight-current-row
      @selection-change="crud.selectionChangeHandler"
      @sort-change="crud.sortChange"
      @row-click="onRowClick"
    >
      <el-table-column prop="id" label="ID" width="80" sortable="custom" fixed />
      <el-table-column prop="paramName" label="任务模板名称" fixed />
      <el-table-column prop="algorithmName" label="算法名称" />
      <el-table-column prop="dataSourceName" label="数据集来源" />
      <el-table-column prop="resourcesPoolType" label="节点类型">
        <template #header>
          <dropdown-header
            title="节点类型"
            :list="resourcesPoolTypeList"
            :filtered="Boolean(filterParams.resourcesPoolType)"
            @command="cmd => filter('resourcesPoolType', cmd)"
          />
        </template>
        <template slot-scope="scope">
          {{ resourcesPoolTypeMap[scope.row.resourcesPoolType] }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" @click.stop="goTraining(scope.row)">创建训练任务</el-button>
          <el-button type="text" @click.stop="doEdit(scope.row)">编辑</el-button>
          <el-button type="text" @click.stop="doDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!--分页组件-->
    <pagination />
    <!--表单组件-->
    <BaseModal
      class="training-params-dialog"
      :before-close="crud.cancelCU"
      :visible="crud.status.cu > 0"
      :title="crud.status.title"
      :loading="crud.status.cu === 2"
      width="50%"
      @cancel="crud.cancelCU"
      @ok="crud.submitCU"
    >
      <job-form v-if="reFresh" ref="form" :form="form" :widthPercent="100" :showFooterBtns="false" type="paramEdit" />
    </BaseModal>
    <!--右边侧边栏-->
    <el-drawer
      :visible.sync="drawer"
      :with-header="false"
      :direction="'rtl'"
      :size="'40%'"
      :before-close="handleClose"
    >
      <div class="ts-drawer">
        <div class="title">参数信息</div>
        <job-detail :item="selectItemObj" type="param" />
      </div>
    </el-drawer>
  </div>
</template>

<script>
import CRUD, { presenter, header, form, crud } from '@crud/crud';
import pagination from '@crud/Pagination';
import crudParams, { del as deleteParams } from '@/api/trainingJob/params';
import BaseModal from '@/components/BaseModal';
import DropdownHeader from '@/components/DropdownHeader';
import JobForm from '@/components/Training/jobForm';
import jobDetail from './components/jobDetail';

const defaultForm = {
  $_id: 0,
  paramName: null,
  description: null,
  algorithmId: null,
  dataSourceName: null,
  dataSourcePath: null,
  algorithmSource: 1,
  outPath: null,
  runParams: {},
  logPath: null,
  resourcesPoolType: 0,
  trainJobSpecsId: null,
};
export default {
  name: 'JobParam',
  components: { BaseModal, DropdownHeader, pagination, jobDetail, JobForm },
  cruds() {
    return CRUD({
      title: '任务模板',
      crudMethod: { ...crudParams },
      optShow: {
        del: false,
      },
      queryOnPresenterCreated: false, // created 时不请求数据
      props: {
        optText: {
          add: '创建任务模板',
        },
        optTitle: {
          add: '创建',
        },
      },
    });
  },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  data() {
    return {
      currentRow: null,
      resourcesPoolTypeMap: {
        0: 'CPU',
        1: 'GPU',
      },
      selectItemObj: null,
      drawer: false,
      reFresh: true,
      filterParams: {
        resourcesPoolType: undefined,
      },
    };
  },
  computed: {
    resourcesPoolTypeList() {
      const arr = [{ label: '全部', value: null }];
      for (const key in this.resourcesPoolTypeMap) {
        arr.push({ label: this.resourcesPoolTypeMap[key], value: key });
      }
      return arr;
    },
  },
  methods: {
    toQuery(params) {
      this.crud.query = { ...params};
      this.crud.toQuery();
    },
    filter(column, value) {
      this.filterParams[column] = value || undefined;
      this.crud.query[column] = value;
      this.crud.refresh();
    },
    // handle 操作
    onRowClick(itemObj) {
      this.selectItemObj = itemObj;
      this.drawer = true;
    },
    handleClose(done) {
      done();
    },
    [CRUD.HOOK.beforeToCU](crud, form) {
      setTimeout(() => {
        this.$refs.form.clearValidate();
      }, 0);
      delete form.trainName;
      delete form.$_id;
    },
    [CRUD.HOOK.beforeSubmit](crud) {
      const {form} = crud;
      delete form.trainName;
      delete form.$_id;
    },
    // link
    goTraining(paramsDataObj) {
      this.$router.push({
        path: '/training/jobAdd',
        name: 'jobAdd',
        params: {
          from: 'param',
          paramsInfo: paramsDataObj,
        },
      });
    },
    // op
    async doEdit(paramsDataObj) {
      this.reFresh = false;
      this.$nextTick(async() => {
        await this.crud.toEdit(paramsDataObj);
        this.reFresh = true;
      });
    },
    async doDelete(id) {
      this.$confirm('此操作将永久删除该任务模板配置, 是否继续?', '请确认').then(async() => {
        await deleteParams({ ids: [id] });
        this.$message({
          message: '删除成功',
          type: 'success',
        });
        await this.crud.refresh();
      });
    },
  },
};
</script>
