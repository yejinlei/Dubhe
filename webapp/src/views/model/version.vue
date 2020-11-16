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
      <cdOperation />
    </div>
    <!--表格渲染-->
    <el-table
      ref="table"
      v-loading="crud.loading"
      :data="crud.data"
      highlight-current-row
      @selection-change="crud.selectionChangeHandler"
    >
      <el-table-column prop="id" label="ID" />
      <el-table-column prop="versionNum" label="版本" />
      <el-table-column prop="modelSource" label="模型来源">
        <template slot-scope="scope">{{ dict.label.model_source[scope.row.modelSource]||'--' }}</template>
      </el-table-column>
      <el-table-column prop="modelAddress" label="模型地址" width="400px" />
      <el-table-column prop="createTime" label="创建时间" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150px" fixed="right">
        <template slot-scope="scope">
          <el-button
            :id="`doDownload_`+scope.$index"
            type="text"
            @click="doDownload(scope.row.parentId, scope.row.versionNum, scope.row.modelAddress)"
          >下载</el-button>
          <el-button :id="`doDelete`+scope.$index" type="text" @click="doDelete(scope.row.id)">删除</el-button>
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
      :disabled="loading"
      width="800px"
      @close="onDialogClose"
      @cancel="crud.cancelCU"
      @ok="onSubmit"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="模型名称" prop="name">
          <div>{{ modelName }}</div>
        </el-form-item>
        <el-form-item ref="modelAddress" label="模型上传" prop="modelAddress">
          <upload-inline
            v-if="refreshFlag"
            ref="upload"
            action="fakeApi"
            accept=".zip, .pb, .h5, .ckpt, .pkl, .pth, .weight, .caffemodel, .pt"
            :acceptSize="modelConfig.uploadFileAcceptSize"
            :acceptSizeFormat="uploadSizeFomatter"
            list-type="text"
            :limit="1"
            :multiple="false"
            :show-file-count="false"
            :params="uploadParams"
            :auto-upload="true"
            :onRemove="handleRemove"
            @uploadStart="uploadStart"
            @uploadSuccess="uploadSuccess"
            @uploadError="uploadError"
          />
          <upload-progress 
            v-if="loading" 
            :progress="progress" 
            :color="customColors" 
            :status="status" 
            :size="size" 
            @onSetProgress="onSetProgress"
          />
        </el-form-item>
      </el-form>
    </BaseModal>
  </div>
</template>

<script>
import crudModelVersion, { del } from '@/api/model/modelVersion';
import CRUD, { presenter, header, form, crud } from '@crud/crud';
import BaseModal from '@/components/BaseModal';
import cdOperation from '@crud/CD.operation';
import pagination from '@crud/Pagination';
import UploadInline from '@/components/UploadForm/inline';
import UploadProgress from '@/components/UploadProgress';
import { getUniqueId, downloadZipFromObjectPath, uploadSizeFomatter } from '@/utils';
import { modelConfig } from '@/config';

const defaultForm = {
  parentId: null,
  modelAddress: null,
  modelSource: 0,
};
export default {
  name: 'ModelVersion',
  dicts: ['model_source'],
  components: { BaseModal, pagination, cdOperation, UploadInline, UploadProgress },
  cruds() {
    return CRUD({
      title: '模型版本管理',
      crudMethod: { ...crudModelVersion },
      optShow: {
        del: false,
      },
      queryOnPresenterCreated: false, // created 时不请求数据
      props: {
        optText: {
          add: '上传模型版本',
        },
      },
    });
  },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  data() {
    return {
      modelId: null,
      modelName: null,
      actionType: '',
      rules: {
        modelAddress: [
          { required: true, message: '请上传有效的模型', trigger: 'manual' },
        ],
      },
      uploadParams: {
        objectPath: null, // 对象存储路径
      },
      refreshFlag: true,
      loading: false,
      progress: 0,
      size: 0,
      customColors: [
        {color: '#909399', percentage: 40},
        {color: '#e6a23c', percentage: 80},
        {color: '#67c23a', percentage: 100},
      ],
      modelConfig,
    };
  },
  computed: {
    status() {
      return this.progress === 100 ? 'success' : null;
    },
    user() {
      return this.$store.getters.user;
    },
  },
  mounted() {
    this.modelId = this.$route.query.id;
    this.modelName = this.$route.query.name;
    this.actionType = this.$route.query.type;
    this.crud.query.parentId = this.modelId;
    if (this.actionType === 'add') {
      this.crud.toAdd();
    }
    this.crud.refresh();
  },
  methods: {
    // handle
    handleRemove() {
      this.loading = false;
      this.form.modelAddress = null;
      this.$refs.modelAddress.validate('manual');
    },
    uploadStart(files) {
      this.updateImagePath();
      [ this.loading, this.size, this.progress ] = [ true, files.size, 0 ];
    },
    onSetProgress(val) {
      this.progress += val;
    },
    uploadSuccess(res) {
      this.progress = 100;
      setTimeout(() => {
        this.loading = false;
      }, 1000);
      this.form.modelAddress = res[0].data.objectName;
      this.$refs.modelAddress.validate('manual');
    },
    uploadError() {
      this.loading = false;
      this.$message({
        message: '上传文件失败',
        type: 'error',
      });
    },
    onDialogClose() {
      this.$refs.upload.formRef.reset();
      this.loading = false;
    },
    onSubmit() {
      this.form.parentId = this.modelId;
      this.crud.submitCU();
    },
    // hook
    [CRUD.HOOK.beforeToAdd]() {
      this.refreshFlag = false;
      this.$nextTick(() => {
        this.refreshFlag = true;
      });
    },
    updateImagePath() {
      this.uploadParams.objectPath = `upload-temp/${this.user.id}/${getUniqueId()}`;
    },
    // op
    doDelete(id) {
      this.$confirm('此操作将永久删除该模型, 是否继续?', '请确认').then(
        async () => {
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
    doDownload(parentId, versionNum, filepath) {
      const msg = `此操作将下载${this.modelName}模型的${versionNum}版本, 是否继续?`;
      this.$confirm(msg, "请确认").then(
        () => {
          const url = /^\//.test(filepath) ? filepath : `/${filepath}`;
          downloadZipFromObjectPath(url, "model.zip");
          this.$message({
            message: '请查看下载文件',
            type: 'success',
          });
        },
        () => {},
      );
    },
    uploadSizeFomatter,
  },
};
</script>
