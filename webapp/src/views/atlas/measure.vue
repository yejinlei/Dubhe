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
  <div id="measure-container" class="app-container">
    <!--工具栏-->
    <div class="head-container">
      <cdOperation
        linkType="custom"
        @to-add="toAdd"
      >
        <span
          slot="right"
          class="flex flex-end flex-wrap"
        >
          <el-input
            v-model="localQuery.nameOrId"
            clearable
            placeholder="请输入度量名称或 ID"
            class="mr-10 mb-22 w-200"
            @keyup.enter.native="crud.toQuery"
            @clear="crud.toQuery"
          />
          <rrOperation class="fr search-btns" @resetQuery="onResetQuery" />
        </span>
      </cdOperation>
    </div>
    <!--表格渲染-->
    <el-table
      ref="table"
      :data="crud.data"
      highlight-current-row
      @sort-change="crud.sortChange"
    >
      <el-table-column
        prop="id"
        label="ID"
        sortable="custom"
        width="80px"
        fixed
      />
      <el-table-column
        prop="name"
        label="度量名称"
        min-width="120px"
        show-overflow-tooltip
        fixed
      />
      <el-table-column
        prop="description"
        label="度量描述"
        min-width="180px"
        show-overflow-tooltip
      />
      <el-table-column
        prop="createTime"
        label="创建时间"
        sortable="custom"
        min-width="160px"
      >
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        min-width="200"
        fixed="right"
      >
        <template slot-scope="scope">
          <el-button
            type="text"
            @click.stop="doEdit(scope.row)"
          >编辑</el-button>
          <el-button
            type="text"
            @click.stop="doDownload(scope.row)"
          >下载</el-button>
          <el-button
            type="text"
            @click.stop="goVisial(scope.row.name)"
          >可视化</el-button>
          <el-button
            type="text"
            @click.stop="doDelete(scope.row.id)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!--分页组件-->
    <pagination />
    <!-- 表单 -->
    <BaseModal
      :title="formTitle"
      :visible="formVisible"
      :loading="formLoading"
      okText="提交"
      @open="onFormOpen"
      @ok="onFormSubmit"
      @cancel="formVisible = false"
      @close="onFormClose"
    >
      <el-form
        ref="form"
        :rules="formRule"
        :model="form"
        label-width="100px"
      >
        <el-form-item label="度量名称" prop="name">
          <el-input
            ref="nameInput"
            v-model.trim="form.name"
            maxlength="32"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="度量描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item ref="url" label="度量图文件" prop="url">
          <upload-inline
            ref="upload"
            action="fakeApi"
            accept=".json"
            :acceptSize="atlasConfig.uploadFileAcceptSize"
            :acceptSizeFormat="uploadSizeFomatter"
            list-type="text"
            :show-file-count="false"
            :params="uploadParams"
            :auto-upload="true"
            :filters="uploadFilters"
            :hash="false"
            :limit="1"
            :on-remove="onFileRemove"
            @uploadStart="onUploadStart"
            @uploadSuccess="onUploadSuccess"
            @uploadError="onUploadError"
          />
        </el-form-item>
      </el-form>
    </BaseModal>
  </div>
</template>

<script>
import { list, add, edit, del } from '@/api/atlas';
import CRUD, { presenter, header, crud } from '@crud/crud';
import rrOperation from '@crud/RR.operation';
import cdOperation from '@crud/CD.operation';
import pagination from '@crud/Pagination';
import BaseModal from '@/components/BaseModal';
import UploadInline from '@/components/UploadForm/inline';
import { getUniqueId, uploadSizeFomatter, validateNameWithHyphen, Constant, downloadFileAsStream, minioBaseUrl, invalidFileNameChar } from '@/utils';
import { atlasConfig } from '@/config';

const defaultForm = {
  id: null,
  name: '',
  description: '',
  url: undefined,
};

export default {
  name: 'Measure',
  components: {
    pagination,
    rrOperation,
    cdOperation,
    BaseModal,
    UploadInline,
  },
  cruds() {
    return CRUD({
      title: 'Measure',
      crudMethod: { list },
      optShow: {
        del: false,
      },
      props: {
        optText: {
          add: '创建度量',
        },
      },
      time: 0,
    });
  },
  mixins: [presenter(), header(), crud()],
  data() {
    return {
      localQuery: {
        nameOrId: null,
      },
      // 表单数据
      formType: 'add',
      formVisible: false,
      formLoading: false,
      formRule: {
        name: [
          {
            required: true,
            message: '请输入度量名称',
            trigger: ['blur', 'change'],
          },
          {
            max: 32,
            message: '长度在 32 个字符以内',
            trigger: ['blur', 'change'],
          },
          {
            validator: validateNameWithHyphen,
            trigger: ['blur', 'change'],
          },
        ],
        url: [
          {
            required: true,
            message: '请上传度量图文件',
            trigger: 'manual',
          },
        ],
      },
      form: { ...defaultForm },
      uploadParams: {
        objectPath: null,
      },
      uploading: false,
      uploadFilters: [invalidFileNameChar],

      atlasConfig,
    };
  },
  computed: {
    formTitle() {
      return `${Constant.FORM_TYPE_MAP[this.formType]}度量`;
    },
    user() {
      return this.$store.getters.user;
    },
  },
  methods: {
    toAdd() {
      this.formType = 'add';
      this.formVisible = true;
    },
    onResetQuery() {
      this.localQuery = { ...this.defaultQuery};
    },
    onFormOpen() {
      this.updateObjectPath();
    },
    onFormSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          this.formLoading = true;
          const func = this.formType === 'add' ? add : edit;
          await func(this.form).finally(() => { this.formLoading = false; });
          this.formVisible = false;
          this.crud.refresh();
        }
      });
    },
    onFormClose() {
      this.formVisible = false;
      setTimeout(() => {
        this.form = { ...defaultForm };
        this.$refs.upload.formRef.reset();
        this.$nextTick(() => { this.$refs.form.clearValidate(); });
      }, 100); // 延迟清空表单，避免用户在弹窗被关闭时看到清空表单导致的表单验证
    },
    onFileRemove() {
      this.$refs.url.validate('manual');
    },
    onUploadStart() {
      this.uploading = true;
    },
    onUploadSuccess(res) {
      this.uploading = false;
      this.form.url = res[0].data.objectName;
      this.$refs.url.validate('manual');
    },
    onUploadError() {
      this.uploading = false;
      this.$refs.url.validate('manual');
    },

    goVisial(measureName) {
      this.$router.push({
        name: 'AtlasGraphVisual',
        params: { measureName },
      });
    },
    doEdit(measure) {
      Reflect.ownKeys(this.form)
        .forEach(key => { this.form[key] = measure[key]; });
      this.formType = 'edit';
      this.formVisible = true;
    },
    doDownload(measure) {
      const { name, url } = measure;
      downloadFileAsStream(`${minioBaseUrl}/${url}`, `${name}.json`);
    },
    doDelete(id) {
      this.$confirm('是否确认删除度量？').then(async () => {
        await del([id]);
        this.crud.refresh();
      });
    },

    updateObjectPath() {
      this.uploadParams.objectPath = `upload-temp/${this.user.id}/${getUniqueId()}`;
    },

    uploadSizeFomatter,

    [CRUD.HOOK.beforeRefresh]() {
      this.crud.query = { ...this.localQuery };
    },
  },
};
</script>
