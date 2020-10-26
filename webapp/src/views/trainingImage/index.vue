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
      <cdOperation :addProps="operationProps">
        <span slot="right">
          <el-input
            v-model="localQuery.imageNameOrId"
            clearable
            placeholder="请输入镜像名称或ID"
            class="filter-item"
            style="width: 200px;"
            @keyup.enter.native="crud.toQuery"
            @clear="crud.toQuery"
          />
          <rrOperation @resetQuery="resetQuery" />
        </span>
      </cdOperation>
    </div>
    <el-tabs v-model="active" class="eltabs-inlineblock" @tab-click="handleClick">
      <el-tab-pane id="tab_0" label="我的镜像" name="0" />
      <el-tab-pane id="tab_1" label="预置镜像" name="1" />
    </el-tabs>
    <!--表格渲染-->
    <el-table
      v-if="prefabricate"
      ref="table"
      v-loading="crud.loading || disableEdit"
      :data="crud.data"
      highlight-current-row
      @selection-change="crud.selectionChangeHandler"
      @sort-change="crud.sortChange"
    >
      <el-table-column v-if="isShow" prop="id" label="ID" sortable="custom" width="80px" />
      <el-table-column prop="imageName" label="镜像名称" sortable="custom" />
      <el-table-column prop="imageTag" label="镜像版本号" sortable="custom" />
      <el-table-column prop="imageStatus" label="状态" width="160px">
        <template #header>
          <dropdown-header
            title="状态"
            :list="imageStatusList"
            :filtered="Boolean(localQuery.imageStatus)"
            @command="filterStatus"
          />
        </template>
        <template slot-scope="scope">
          <el-tag effect="plain" :type="map[scope.row.imageStatus]">
            {{ statusMap[scope.row.imageStatus] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="镜像描述" show-overflow-tooltip />
      <el-table-column prop="createTime" label="上传时间" sortable="custom" width="200px">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column v-if="isShow" label="操作" width="200px" fixed="right">
        <template slot-scope="scope">
          <el-button :id="`doEdit_`+scope.$index" type="text" @click.stop="doEdit(scope.row)">
            修改
          </el-button>
          <el-button :id="`doDelete_`+scope.$index" type="text" @click.stop="doDelete(scope.row.id)">
            删除
          </el-button>
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
      width="600px"
      @close="onDialogClose"
      @cancel="crud.cancelCU"
      @ok="crud.submitCU"
    >
      <el-form
        ref="form"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item v-if="isEdit" label="镜像名称" prop="imageName">
          <el-select
            id="imageName"
            v-model="form.imageName"
            placeholder="请选择或输入镜像名称"
            style="width: 400px;"
            clearable
            filterable
            allow-create
            default-first-option
            @focus="getHarborProjects"
          >
            <el-option
              v-for="item in harborProjectList"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isEdit" ref="imagePath" label="镜像文件路径" prop="imagePath">
          <upload-inline
            v-if="crud.status.cu > 0"
            ref="upload"
            action="fakeApi"
            accept=".zip,.tar,.rar,.gz"
            list-type="text"
            :acceptSize="0"
            :params="uploadParams"
            :show-file-count="false"
            :auto-upload="true"
            :hash="false"
            :limit="1"
            :on-remove="onFileRemove"
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
        <el-form-item v-if="isEdit" label="镜像版本号" prop="imageTag">
          <el-input
            id="imageTag"
            v-model="form.imageTag"
            style="width: 400px;"
          />
        </el-form-item>
        <el-form-item label="描述" prop="remark">
          <el-input
            id="remark"
            v-model="form.remark"
            type="textarea"
            :rows="4"
            maxlength="1024"
            show-word-limit
            placeholder
            style="width: 400px;"
          />
        </el-form-item>
      </el-form>
    </BaseModal>
  </div>
</template>

<script>
// eslint-disable-next-line import/no-extraneous-dependencies
import { debounce } from 'throttle-debounce';

import cdOperation from '@crud/CD.operation';
import rrOperation from '@crud/RR.operation';
import pagination from '@crud/Pagination';
import CRUD, { presenter, header, form, crud } from '@crud/crud';
import trainingImageApi, { imageNameList, del } from '@/api/trainingImage/index';
import { getUniqueId } from '@/utils';
import BaseModal from '@/components/BaseModal';
import UploadInline from '@/components/UploadForm/inline';
import DropdownHeader from '@/components/DropdownHeader';
import UploadProgress from '@/components/UploadProgress';

const defaultForm = {
  imageName: null,
  imagePath: null,
  imageTag: null,
  remark: null,
};
export default {
  name: 'TrainingImage',
  components: {
    BaseModal,
    pagination,
    cdOperation,
    rrOperation,
    UploadInline,
    DropdownHeader,
    UploadProgress,
  },
  cruds() {
    return CRUD({
      title: '镜像',
      crudMethod: { ...trainingImageApi },
      optShow: {
        del: false,
      },
      queryOnPresenterCreated: false,
      props: {
        optText: {
          add: '上传镜像',
        },
        optTitle: {
          add: '上传',
        },
      },
    });
  },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  data() {
    const validateImageTag = (rule, value, callback) => {
      if (value === '' || value == null) {
        callback();
      } else if (value.length > 32) {
        callback(new Error('长度不超过 32 个字符'));
      } else if (!/^[A-Za-z0-9_\-.]+$/.test(value)) {
        callback(new Error('只支持英文、数字、下划线、英文横杠和英文.号'));
      } else {
        callback();
      }
    };
    const validateImageName = (rule, value, callback) => {
      if (value === '' || value == null) {
        callback();
      } else if (value.length > 64) {
        callback(new Error('长度不超过 64 个字符'));
      } else if (!/^[a-z0-9_-]+$/.test(value)) {
        callback(new Error('只支持小写英文、数字、下划线和横杠'));
      } else {
        callback();
      }
    };
    return {
      active: '0',
      localQuery: {
        imageStatus: null,
        imageNameOrId: null,
      },
      map: {
        0: 'info',
        1: 'success',
        2: 'danger',
      },
      statusMap: {
        0: '制作中',
        1: '制作成功',
        2: '制作失败',
      },
      rules: {
        imageName: [
          { required: true, message: '请选择项目名称', trigger: 'change' },
          { validator: validateImageName, trigger: ['blur', 'change'] },
        ],
        imagePath: [
          { required: true, message: '请输入镜像路径', trigger: ['blur', 'manual'] },
        ],
        imageTag: [
          { required: true, message: '请输入镜像版本号', trigger: 'blur' },
          { validator: validateImageTag, trigger: ['blur', 'change'] },
        ],
      },
      harborProjectList: [],
      drawer: false,
      uploadParams: {
        objectPath: null, // 对象存储路径
      },
      progress: 0,
      size: 0,
      customColors: [
        {color: '#909399', percentage: 40},
        {color: '#e6a23c', percentage: 80},
        {color: '#67c23a', percentage: 100},
      ],
      disableEdit: false,
      loading: false,
      isEdit: false,
      prefabricate: true,
    };
  },
  computed: {
    isShow() {
      return this.active === '0';
    },
    operationProps() {
      return {
        disabled: Number(this.active) === 1,
      };
    },
    imageStatusList() {
      const arr = [{ label: '全部', value: null }];
      for (const key in this.statusMap) {
        arr.push({ label: this.statusMap[key], value: key });
      }
      return arr;
    },
    user() {
      return this.$store.getters.user;
    },
    status() {
      return this.progress === 100 ? 'success' : null;
    },
  },
  mounted() {
    this.crud.query.imageResource = Number(this.active);
    this.crud.refresh();
    this.refetch = debounce(3000, this.crud.refresh);
    this.updateImagePath();
  },
  methods: {
    // handle
    handleClick() {
      this.crud.query.imageResource = Number(this.active);
      this.crud.refresh();
      // 切换tab键时让表格重渲
      this.prefabricate = false;
      this.$nextTick(() => { this.prefabricate = true; });
    },
    onFileRemove() {
      this.form.imagePath = null;
      this.loading = false;
      this.$refs.imagePath.validate('manual');
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
      if (this.loading) {
        this.form.imagePath = res[0].data.objectName;
        this.$refs.imagePath.validate('manual');
      }
    },
    uploadError() {
      this.$message({
        message: '上传文件失败',
        type: 'error',
      });
      this.loading = false;
    },
    // hook
    [CRUD.HOOK.afterRefresh]() {
      this.checkStatus();
    },
    [CRUD.HOOK.beforeToAdd]() {
      this.isEdit = true;
      this.formType = 'add';
    },
    [CRUD.HOOK.beforeRefresh]() {
      this.crud.query = { ...this.localQuery};
      this.crud.query.imageResource = Number(this.active);
    },
    [CRUD.HOOK.beforeToEdit]() {
      this.isEdit = false;
    },
    async getHarborProjects() {
      this.harborProjectList = await imageNameList();
    },
    onDialogClose() {
      if (this.isEdit) {
        this.$refs.upload.formRef.reset();
      }
      this.loading = false;
    },
    checkStatus() {
      if (this.crud.data.some(item => [0].includes(item.imageStatus))) {
        this.refetch();
      }
    },
    filterStatus(status) {
      this.localQuery.imageStatus = status;
      this.crud.toQuery();
    },
    resetQuery() {
      this.localQuery = {
        imageStatus: null,
        imageNameOrId: null,
      };
    },
    updateImagePath() {
      this.uploadParams.objectPath = `upload-temp/${this.user.id}/${getUniqueId()}`;
    },
    async doEdit(imageObj) {
      const dataObj = {
        ids: [imageObj.id],
        ...imageObj,
      };
      await this.crud.toEdit(dataObj);
    },
    doDelete(id) {
      this.$confirm('此操作将永久删除该镜像, 是否继续?', '请确认').then(
        async() => {
          await del({ ids: [id] });
          this.$message({
            message: '删除成功',
            type: 'success',
          });
          this.crud.refresh();
        },
      );
    },
  },
};
</script>
