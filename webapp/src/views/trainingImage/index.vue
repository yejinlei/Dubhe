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
      <cdOperation :addProps="operationProps" />
    </div>
    <el-tabs v-model="active" class="eltabs-inlineblock" @tab-click="handleClick">
      <el-tab-pane label="我的镜像" name="0" />
      <el-tab-pane label="预置镜像" name="1" />
    </el-tabs>
    <!--表格渲染-->
    <el-table
      ref="table"
      v-loading="crud.loading || disableEdit"
      :data="crud.data"
      highlight-current-row
      @selection-change="crud.selectionChangeHandler"
      @sort-change="crud.sortChange"
    >
      <el-table-column v-if="active == 0" prop="id" label="ID" sortable="custom" width="80px" />
      <el-table-column prop="imageName" label="镜像名称" sortable="custom" />
      <el-table-column prop="imageTag" label="镜像版本号" sortable="custom" />
      <el-table-column prop="imageStatus" width="160px">
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
        <el-form-item label="镜像名称" prop="imageName">
          <el-select
            v-model="form.imageName"
            placeholder="请选择镜像名称"
            style="width: 400px;"
            clearable
            @focus="getHarborProjects"
          >
            <el-option
              v-for="(item, index) in harborProjectList"
              :key="index"
              :label="item.imageName"
              :value="item.imageName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="镜像文件路径" prop="imagePath">
          <upload-inline
            ref="upload"
            action="fakeApi"
            accept=".zip,.tar,.rar,.gz"
            list-type="text"
            :acceptSize="5120"
            :params="uploadParams"
            :show-file-count="false"
            :auto-upload="true"
            :hash="false"
            :limit="1"
            @uploadStart="uploadStart"
            @uploadSuccess="uploadSuccess"
            @uploadError="uploadError"
          />
          <div v-if="loading"><i class="el-icon-loading" />镜像上传中...</div>
        </el-form-item>
        <el-form-item label="镜像版本号" prop="imageTag">
          <el-input
            v-model="form.imageTag"
            style="width: 400px;"
          />
        </el-form-item>
        <el-form-item label="描述" prop="remark">
          <el-input
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
import { nanoid } from 'nanoid';
// eslint-disable-next-line import/no-extraneous-dependencies
import { debounce } from 'throttle-debounce';

import cdOperation from '@crud/CD.operation';
import pagination from '@crud/Pagination';
import CRUD, { presenter, header, form, crud } from '@crud/crud';
import { parseTime } from '@/utils';
import trainingImageApi, {project} from '@/api/trainingImage/index';
import BaseModal from '@/components/BaseModal';
import UploadInline from '@/components/UploadForm/inline';
import DropdownHeader from '@/components/DropdownHeader';

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
    UploadInline,
    DropdownHeader,
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
    return {
      active: '0',
      localQuery: {
        imageStatus: null,
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
        ],
        imagePath: [
          { required: true, message: '请输入镜像路径', trigger: 'blur' },
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
      disableEdit: false,
      loading: false,
    };
  },
  computed: {
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
    getUser() {
      return this.$store.getters.user;
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
    },
    handleClose(done) {
      done();
    },
    uploadStart() {
      this.loading = true;
    },
    updateRunParams(p) {
      this.form.runParams = p;
    },
    uploadSuccess(res) {
      this.loading = false;
      this.form.imagePath = res[0].data.objectName;
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
      this.formType = 'add';
      this.updateImagePath();
    },
    [CRUD.HOOK.beforeRefresh]() {
      this.crud.query = { ...this.localQuery};
      this.crud.query.imageResource = Number(this.active);
    },
    async getHarborProjects() {
      this.harborProjectList = await project();
    },
    onDialogClose() {
      this.$refs.upload.formRef.reset();
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
    updateImagePath() {
      this.uploadParams.objectPath = `upload-image/${this.getUser.id}/${parseTime(new Date(), '{y}{m}{d}{h}{i}{s}{S}') + nanoid(4)}`;
    },
  },
};
</script>
