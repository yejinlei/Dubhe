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
    <!--任务详情，版本列表-->
    <el-table
      ref="table"
      v-loading="resumeLoading"
      :data="tableList"
      highlight-current-row
      @sort-change="handleSortChange"
      @row-click="onRowClick"
    >
      <el-table-column prop="id" label="版本ID" sortable="custom" />
      <el-table-column prop="trainVersion" label="版本" sortable="custom" />
      <el-table-column prop="runtime" label="训练时长" sortable="custom" />
      <el-table-column prop="trainStatus" label="状态">
        <template #header>
          <el-dropdown trigger="click" placement="bottom-start" @command="cmd => filter('trainStatus', cmd)">
            <span>
              状态
              <i class="el-icon-arrow-down el-icon--right" />
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item
                v-for="item in jobStatusList"
                :key="item.id"
                :command="item.value"
              >{{ item.label }}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
        <template slot-scope="scope">
          <el-tag
            :type="map[scope.row.trainStatus].tagMap"
            effect="plain"
          >{{ dict.label.job_status[scope.row.trainStatus] || '--' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" sortable="custom">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300px" fixed="right">
        <template slot-scope="scope">
          <!--状态：0为待处理，1为运行中 -->
          <el-button
            v-if="map[scope.row.trainStatus].statusMap==='running'"
            :id="`doStop_`+scope.$index"
            type="text"
            @click.stop="doStop(scope.row.id)"
          >停止</el-button>
          <!--状态：2为运行完成，3为失败，4为停止，5为未知 -->
          <el-button
            v-if="map[scope.row.trainStatus].statusMap==='done'"
            :id="`goEdit_`+scope.$index"
            type="text"
            @click.stop="goEdit(scope.row, 'edit')"
          >修改</el-button>
          <el-button
            :id="`goVisual_`+scope.$index"
            :disabled="!scope.row.visualizedLogPath"
            type="text"
            @click.stop="goVisual(scope.row.jobName)"
          >可视化</el-button>
          <!-- 状态为 4 (停止) 时，在下拉菜单中展示保存模型 -->
          <el-button
            v-show="scope.row.trainStatus !== 4"
            :id="`goSaveModel_`+scope.$index"
            :disabled="!scope.row.outPath"
            type="text"
            @click.stop="goSaveModel(scope.row)"
          >保存模型</el-button>
          <el-button
            v-show="scope.row.trainStatus === 4"
            :id="`doResume_`+scope.$index"
            :disabled="!scope.row.outPath"
            type="text"
            @click.stop="doResume(scope.row)"
          >断点续训</el-button>
          <el-dropdown>
            <el-button type="text" style="margin-left: 10px;" @click.stop="()=>{}">
              更多<i class="el-icon-arrow-down el-icon--right" />
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <!-- 状态为 4 (停止) 时，在下拉菜单中展示保存模型 -->
              <el-dropdown-item
                v-show="scope.row.trainStatus === 4"
                :id="`goSaveModel_inside_`+scope.$index"
                @click.native="goSaveModel(scope.row)"
              >
                <el-button
                  :disabled="!scope.row.outPath"
                  type="text"
                >保存模型</el-button>
              </el-dropdown-item>
              <el-dropdown-item
                :id="`goEditParams_`+scope.$index"
                :disabled="map[scope.row.trainStatus].statusMap!=='done'"
                @click.native="goEdit(scope.row, 'saveParams')"
              >
                <!--状态：2为运行完成，3为失败，4为停止，5为未知 -->
                <el-button
                  :disabled="map[scope.row.trainStatus].statusMap!=='done'"
                  type="text"
                >保存任务模板</el-button>
              </el-dropdown-item>
              <el-dropdown-item
                :id="`doDelete_`+scope.$index"
                :disabled="map[scope.row.trainStatus].statusMap!=='done'"
                @click.native="doDelete(scope.row.id)"
              >
                <!--状态：2为运行完成，3为失败，4为停止，5为未知 -->
                <el-button
                  :disabled="map[scope.row.trainStatus].statusMap!=='done'"
                  type="text"
                >删除</el-button>
              </el-dropdown-item>
            </el-dropdown-menu></el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <!--修改Dialog-->
    <BaseModal
      :visible.sync="showDialog"
      :title="dialogTitle"
      :okText="dialogType === 'edit' ? '开始训练' : '保存'"
      :loading="submitLoading"
      width="50%"
      @cancel="showDialog=false"
      @ok="doEdit"
    >
      <job-form
        v-if="reFresh"
        ref="jobFormEdit"
        :width-percent="100"
        :type="dialogType"
        @getForm="getForm"
      />
    </BaseModal>
    <!--保存模型Dialog-->
    <save-model-dialog
      ref="saveModel"
      type="training"
    />
    <!--断点续训Dialog-->
    <path-select-dialog
      ref="pathSelect"
      class-key="keepTrainDialog"
      :type="pathType"
      @chooseDone="chooseDone"
      @chooseModel="chooseModel"
    />
    <!--右边侧边栏-->
    <el-drawer
      :visible.sync="drawerVisible"
      :with-header="false"
      :direction="'rtl'"
      :size="'50%'"
      @close="handleDrawerClose"
    >
      <job-drawer ref="jobDrawer" />
    </el-drawer>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';

// eslint-disable-next-line import/no-extraneous-dependencies
import { debounce } from 'throttle-debounce';

import CRUD, { presenter, header, crud } from '@crud/crud';
import { Constant } from '@/utils';
import crudJob, { getJobList, stop as stopJob, del as deleteJob, edit as editJob } from '@/api/trainingJob/job';
import { add as addParams } from '@/api/trainingJob/params';
import BaseModal from '@/components/BaseModal';
import JobForm from '@/components/Training/jobForm';
import SaveModelDialog from '@/components/Training/saveModelDialog';
import pathSelectDialog from './components/pathSelectDialog';
import jobDrawer from './components/jobDrawer';
import { trainingStatusMap as map } from './utils';

export default {
  name: 'JobDetail',
  dicts: ['job_status'],
  components: { BaseModal, JobForm, jobDrawer, SaveModelDialog, pathSelectDialog },
  cruds() {
    return CRUD({
      crudMethod: { ...crudJob },
      optShow: {
        add: false,
        edit: false,
        del: false,
      },
      queryOnPresenterCreated: false,
    });
  },
  mixins: [presenter(), header(), crud()],
  data() {
    return {
      id: null,
      tableList: [], // 任务版本列表
      params: {},
      map,
      pathType: '', // 目录树选择类型
      keepPool: true,
      showDialog: false,
      dialogTitle: '',
      dialogType: 'edit', // edit: 修改训练任务; saveParams: 保存任务参数
      drawerVisible: false,
      reFresh: true, // job_form_id 没法重新渲染，先用refresh
      modelList: [],
      submitLoading: false,
      resumeLoading: false,
      rules: {
        parentId: [
          { required: true, message: '请选择模型', trigger: 'blur' },
        ],
      },
    };
  },
  beforeRouteEnter(to, from, next) {
    if (!to.query.id) {
      next('/training/job');
    } else {
      next();
    }
  },
  computed: {
    ...mapGetters([
      'user',
    ]),
    jobStatusList() {
      return [{ label: '全部', value: null }].concat(this.dict.job_status);
    },
  },
  mounted() {
    this.id = this.$route.query.id;
    this.refetch = debounce(1000, this.getJobList);
    this.getJobList();
  },
  beforeDestroy() {
    this.keepPool = false;
  },
  methods: {
    // handle 操作
    onRowClick(row) {
      this.drawerVisible = true;
      this.$nextTick(() => {
        this.$refs.jobDrawer.onOpen(row.id);
      });
    },
    handleSortChange({ prop, order }) {
      const sortParams = {
        sort: order ? prop : undefined,
        order: order ? Constant.tableSortMap[order] : undefined,
      };
      this.params = Object.assign(this.params, sortParams);
      this.getJobList();
    },
    handleDrawerClose() {
      this.$refs.jobDrawer.onClose();
    },
    // 页面逻辑
    async getJobList() {
      const params = {trainId: this.id, ...this.params};
      const data = await getJobList(params);
      this.tableList = data;
      if (this.keepPool && this.tableList.some(item => item.trainStatus === 0 || item.trainStatus === 1)) {
        setTimeout(() => {
          this.refetch(); // 如果有中间状态就进入轮询
        }, 1000);
      }
    },
    refreshList() {
      this.params = {};
      this.getJobList();
    },
    async getForm(form) {
      this.submitLoading = true;
      if (this.dialogType === 'edit') {
        await editJob(form).finally(() => {
          this.submitLoading = false;
        });
        this.refreshList();
        this.$message({
          message: '任务修改成功',
          type: 'success',
        });
      } else {
        await addParams(form).finally(() => {
          this.submitLoading = false;
        });
        this.$message({
          message: '任务模板保存成功',
          type: 'success',
        });
      }
      this.showDialog = false;
    },
    // 表头筛选
    filter(column, value) {
      this.params[column] = value || undefined;
      this.getJobList();
    },
    // link
    goEdit(item, dialogType) {
      this.reFresh = false;
      this.$nextTick(() => {
        if (dialogType === 'saveParams') {
          item.paramName = item.jobName;
        }
        item.valAlgorithmUsage = item.algorithmUsage;
        this.showDialog = true;
        this.$nextTick(() => {
          this.$refs.jobFormEdit.initForm(item);
        });
        this.dialogTitle = dialogType === 'edit' ? '修改任务' : '保存任务模板';
        this.dialogType = dialogType;
        this.reFresh = true;
      });
    },
    goVisual(jobName) {
      const params = {
        id: this.user.id,
        trainJobName: jobName,
      };
      const { href } = this.$router.resolve({ name: 'VISUAL', params });
      const url = `${href}?id=${params.id}&trainJobName=${params.trainJobName}`;
      window.open(url, '_blank');
    },
    async goSaveModel(model) {
      this.pathType = 'modelSelect';
      const modelParams = {
        algorithmId: model.algorithmId,
        algorithmName: model.algorithmName,
        algorithmSource: model.algorithmSource,
        algorithmUsage: model.algorithmUsage,
        modelAddress: model.outPath,
      };
      this.$nextTick(() => {
        this.$refs.pathSelect.show({
          resumePath: `${model.outPath}/`,
          id: model.algorithmId,
          params: modelParams,
        });
      });
    },
    // op
    doEdit() {
      this.$refs.jobFormEdit.save();
    },
    doStop(id) {
      this.$confirm('此操作将停止该任务版本, 是否继续?', '请确认')
        .then(async() => {
          const params = {
            trainId: this.id,
            id,
          };
          await stopJob(params);
          this.$message({
            message: '停止成功',
            type: 'success',
          });
          this.refreshList();
        });
    },
    doDelete(id) {
      const alertText = this.tableList.length === 1
        ? '此版本为该训练唯一的版本，如果删除该版本，对应的训练任务将一并删除, 是否继续?'
        : '此操作将删除该任务版本, 是否继续?';
      this.$confirm(alertText, '请确认')
        .then(async() => {
          const params = {
            trainId: this.id,
            id,
          };
          await deleteJob(params);
          this.$message({
            message: '删除成功',
            type: 'success',
          });
          if (this.tableList.length === 1) {
            this.$router.push({ path: '/training/job' });
          } else {
            this.refreshList();
          }
        });
    },
    async doResume(item) {
      this.pathType = 'jobResume';
      this.$nextTick(() => {
        this.$refs.pathSelect.show({
          resumePath: `${item.outPath}/`,
          id: item.id,
        });
      });
    },
    chooseDone() {
      this.refreshList();
    },
    chooseModel(selectPath, params) {
      Object.assign(params, {modelAddress: selectPath});
      this.$refs.saveModel.show(params);
    },
  },
};
</script>
