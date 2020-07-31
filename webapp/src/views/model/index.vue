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
  <div class="app-container">
    <!--工具栏-->
    <div class="head-container">
      <div class="cd-opts">
        <span class="cd-opts-left">
          <el-button
            class="filter-item"
            type="primary"
            icon="el-icon-plus"
            round
            @click="toAdd"
          >
            创建模型
          </el-button>
        </span>
        <span class="cd-opts-right">
          <el-input
            v-model="query.name"
            clearable
            placeholder="请输入模型名称或ID"
            class="filter-item"
            style="width: 200px;"
            @keyup.enter.native="crud.toQuery"
          />
          <rrOperation />
        </span>
      </div>
      <div>
        <el-tabs v-model="active" class="eltabs-inlineblock" @tab-click="crud.toQuery">
          <el-tab-pane label="我的模型" name="0" />
          <el-tab-pane label="预训练模型" name="1" />
        </el-tabs>
      </div>
    </div>
    <!--表格渲染-->
    <el-table
      ref="table"
      v-loading="crud.loading"
      :data="crud.data"
      highlight-current-row
      @selection-change="crud.selectionChangeHandler"
      @sort-change="crud.sortChange"
    >
      <el-table-column prop="id" label="ID" width="80" sortable="custom" />
      <el-table-column prop="name" label="模型名称" min-width="180px" />
      <el-table-column prop="frameType" label="框架名称" min-width="150px">
        <template slot-scope="scope">{{ dict.label.frame_type[scope.row.frameType]||'--' }}</template>
      </el-table-column>
      <el-table-column prop="modelType" label="模型格式" min-width="150px">
        <template slot-scope="scope">{{ dict.label.model_type[scope.row.modelType]||'--' }}</template>
      </el-table-column>
      <el-table-column prop="modelClassName" label="模型类别" min-width="150px">
        <template slot-scope="scope">{{ scope.row.modelClassName ||'--' }}</template>
      </el-table-column>
      <el-table-column prop="modelDescription" label="模型描述" min-width="300px" show-overflow-tooltip />
      <el-table-column v-if="isCustom" prop="versionNum" label="版本" width="80">
        <template slot-scope="scope">
          <a
            v-if="scope.row.versionNum"
            @click="goVersion(scope.row.id, scope.row.name)"
          >{{ scope.row.versionNum }}</a>
          <span v-if="!scope.row.versionNum">--</span>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="160" sortable="custom">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260px" fixed="right">
        <template slot-scope="scope">
          <el-button
            v-if="isCustom"
            type="text"
            @click="goVersion(scope.row.id, scope.row.name)"
          >历史版本</el-button>
          <el-tooltip
            :disabled="!!scope.row.modelAddress"
            effect="dark"
            content="暂无版本"
            placement="top"
          >
            <span :class="{'ml-10 mr-10': isCustom}">
              <el-button
                :disabled="!scope.row.modelAddress"
                type="text"
                @click="doDownload(scope.row)"
              >下载</el-button>
            </span>
          </el-tooltip>
          <el-button
            v-if="isCustom"
            type="text"
            @click="doEdit(scope.row)"
          >编辑</el-button>
          <el-button
            v-if="isCustom"
            type="text"
            @click="doDelete(scope.row.id)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!--分页组件-->
    <pagination />
    <!--表单组件-->
    <BaseModal
      :before-close="crud.cancelCU"
      :visible="crud.status.cu > 0"
      :title="crud.status.title"
      :loading="crud.status.cu === 2"
      width="600px"
      @open="onDialogOpen"
      @cancel="crud.cancelCU"
      @ok="crud.submitCU"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="模型名称" prop="name">
          <el-input
            v-model.trim="form.name"
            style="width: 300px;"
            maxlength="15"
            placeholder="请输入模型名称"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="框架" prop="frameType">
          <el-select v-model="form.frameType" placeholder="请选择框架" style="width: 300px;">
            <el-option
              v-for="item in dict.frame_type"
              :key="item.value"
              :value="item.value"
              :label="item.label"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="模型格式" prop="modelType">
          <el-select v-model="form.modelType" placeholder="请选择模型格式" style="width: 300px;">
            <el-option
              v-for="item in dict.model_type"
              :key="item.value"
              :value="item.value"
              :label="item.label"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="模型类别" prop="modelClassName">
          <el-select
            v-model="form.modelClassName"
            placeholder="请选择或输入模型类别"
            filterable
            allow-create
            style="width: 300px;"
            @change="onAlgorithmUsageChange"
          >
            <el-option
              v-for="item in algorithmUsageList"
              :key="item.id"
              :label="item.auxInfo"
              :value="item.auxInfo"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="模型描述" prop="modelDescription">
          <el-input v-model="form.modelDescription" type="textarea" placeholder="请输入模型描述" maxlength="255" show-word-limit style="width: 400px;" />
        </el-form-item>
      </el-form>
    </BaseModal>
    <!--多步骤新增dialog-->
    <add-model-dialog
      ref="addModel"
      @addDone="addDone"
    />
  </div>
</template>

<script>
import crudModel, { del } from '@/api/model/model';
import { list as getAlgorithmUsages, add as addAlgorithmUsage } from '@/api/algorithm/algorithmUsage';
import CRUD, { presenter, header, form, crud } from '@crud/crud';
import BaseModal from '@/components/BaseModal';
import rrOperation from '@crud/RR.operation';
import pagination from '@crud/Pagination';
import { downloadZipFromObjectPath, validateNameWithHyphen } from '@/utils';
import AddModelDialog from './components/addModelDialog';

const defaultForm = {
  name: null,
  frameType: null,
  modelType: null,
  modelClassName: null,
  modelDescription: null,
};
export default {
  name: 'Model',
  dicts: ['model_type', 'frame_type'],
  components: { BaseModal, pagination, rrOperation, AddModelDialog },
  cruds() {
    return CRUD({
      title: '模型',
      crudMethod: { ...crudModel },
      props: {
        optText: {
          add: '创建模型',
        },
      },
      optShow: {
        del: false,
      },
    });
  },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  data() {
    return {
      rules: {
        name: [
          { required: true, message: '请输入模型名称', trigger: 'blur' },
          { max: 20, message: '长度在 20 个字符以内', trigger: 'blur' },
          {
            validator: validateNameWithHyphen,
            trigger: ['blur', 'change'],
          },
        ],
        frameType: [
          { required: true, message: '请选择模型框架', trigger: 'blur' },
        ],
        modelType: [
          { required: true, message: '请选择模型格式', trigger: 'blur' },
        ],
        modelClassName: [
          { required: true, message: '请输入模型类别', trigger: ['blur', 'change'] },
        ],
        modelDescription: [
          { required: true, message: '请输入模型描述', trigger: 'blur' },
          { max: 255, message: '长度在255个字符以内', trigger: 'blur' },
        ],
      },
      algorithmUsageList: [],
      active: '0',
    };
  },
  computed: {
    isCustom() {
      return this.active === '0';
    },
    isPreset() {
      return this.active === '1';
    },
  },
  mounted() {
    if (this.$route.params?.type === 'add') {
      setTimeout(() => {
        this.crud.toAdd();
      }, 500);
    }
  },
  methods: {
    getAlgorithmUsages() {
      const params = {
        isContainDefault: true,
        current: 1,
        size: 1000,
      };
      getAlgorithmUsages(params).then(res => {
        this.algorithmUsageList = res.result;
      });
    },
    async createAlgorithmUsage(auxInfo) {
      await addAlgorithmUsage({ auxInfo });
      this.getAlgorithmUsages();
    },
    // handle
    onDialogOpen() {
      this.getAlgorithmUsages();
    },
    onAlgorithmUsageChange(value) {
      const usageRes = this.algorithmUsageList.find(usage => usage.auxInfo === value);
      if (!usageRes) {
        this.createAlgorithmUsage(value);
      }
    },
    toAdd() {
      this.$refs.addModel.show();
    },
    addDone() {
      this.crud.refresh();
    },
    // link
    goVersion(id, name, type = 'detail') {
      this.$router.push({ path: '/model/version', query: { id, name, type }});
    },
    // op
    async doEdit(item) {
      // temp handle
      item.frameType = String(item.frameType);
      item.modelType = String(item.modelType);
      this.crud.toEdit(item);
    },
    doDelete(id) {
      this.$confirm('此操作将永久删除该模型, 是否继续?', '请确认').then(
        async() => {
          const params = {
            ids: [id],
          };
          await del(params);
          this.$message({
            message: '删除成功',
            type: 'success',
          });
          this.crud.refresh();
        },
      );
    },
    doDownload(row) {
      const { name, versionNum, modelAddress } = row;
      const msg = this.isCustom
        ? `此操作将下载 ${name} 模型的 ${versionNum} 版本, 是否继续?`
        : `此操作将下载预训练模型 ${name}, 是否继续?`;
      this.$confirm(msg, '请确认').then(
        () => {
          const url = /^\//.test(modelAddress) ? modelAddress : `/${  modelAddress}`;
          downloadZipFromObjectPath(url, 'model.zip');
          this.$message({
            message: '请查看下载文件',
            type: 'success',
          });
        },
        () => {},
      );
    },
    [CRUD.HOOK.beforeRefresh]() {
      this.crud.query.modelResource = Number(this.active);
    },
  },
};
</script>
