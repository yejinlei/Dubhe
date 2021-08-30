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
      <el-tab-pane id="tab_0" label="我的镜像" :name="IMAGE_RESOURCE_ENUM.CUSTOM" />
      <el-tab-pane id="tab_1" label="预置镜像" :name="IMAGE_RESOURCE_ENUM.PRESET" />
      <el-tab-pane id="tab_2" label="Notebook 镜像" :name="IMAGE_RESOURCE_ENUM.NOTEBOOK" />
      <el-tab-pane id="tab_4" label="终端镜像" :name="IMAGE_RESOURCE_ENUM.TERMINAL" />
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
      <el-table-column v-if="!isPreset" prop="id" label="ID" sortable="custom" width="80px" />
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
      <el-table-column v-if="isNotebook" prop="imageResource" label="是否为默认" align="center">
        <template slot-scope="scope">
          <i
            :class="resourceObj(scope.row.imageResource).icon"
            :style="{ color: resourceObj(scope.row.imageResource).color, fontSize: '20px' }"
          ></i>
        </template>
      </el-table-column>
      <el-table-column v-if="isAdmin || isCustom" label="操作" width="200px" fixed="right">
        <template slot-scope="scope">
          <el-tooltip
            v-if="isAdmin && isNotebook"
            effect="dark"
            content="设为在线编辑算法时创建nootbook的默认镜像"
            placement="top"
          >
            <el-button
              :id="`doPrecast_` + scope.$index"
              :disabled="Boolean(scope.row.imageResource)"
              type="text"
              @click.stop="doPrecast(scope.row.id)"
            >
              {{ resourceObj(scope.row.imageResource).butText }}
            </el-button>
          </el-tooltip>
          <el-button
            v-if="hasPermission('training:image:edit') && isCustom"
            :id="`doEdit_` + scope.$index"
            type="text"
            @click.stop="doEdit(scope.row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="hasPermission('training:image:delete') && (!isPreset || isAdmin)"
            :id="`doDelete_` + scope.$index"
            type="text"
            @click.stop="doDelete(scope.row.id)"
          >
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
      @open="onDialogOpen"
      @close="onDialogClose"
      @cancel="crud.cancelCU"
      @ok="crud.submitCU"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item v-if="isEdit && isAdmin" label="镜像类型" prop="imageType">
          <el-radio-group v-model="form.projectType" @change="onImageTypeChange">
            <el-radio :label="IMAGE_PROJECT_TYPE.TRAIN" border class="mr-0">训练镜像</el-radio>
            <el-radio :label="IMAGE_PROJECT_TYPE.NOTEBOOK" border>Notebook 镜像</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="isEdit && isAdmin && form.projectType === IMAGE_PROJECT_TYPE.TRAIN"
          label="镜像来源"
          prop="imageResource"
        >
          <el-radio-group v-model="form.imageResource">
            <el-radio :label="Number(IMAGE_RESOURCE_ENUM.CUSTOM)" border class="mr-0"
              >我的镜像</el-radio
            >
            <el-radio :label="Number(IMAGE_RESOURCE_ENUM.PRESET)" border>预置镜像</el-radio>
          </el-radio-group>
        </el-form-item>
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
          >
            <el-option v-for="item in harborProjectList" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isEdit" ref="imagePath" label="镜像文件路径" prop="imagePath">
          <upload-inline
            v-if="crud.status.cu > 0"
            ref="upload"
            action="fakeApi"
            accept=".zip,.tar,.rar,.gz"
            list-type="text"
            :acceptSize="imageConfig.uploadFileAcceptSize"
            :acceptSizeFormat="uploadSizeFomatter"
            :params="uploadParams"
            :show-file-count="false"
            :auto-upload="true"
            :filters="uploadFilters"
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
          <el-input id="imageTag" v-model="form.imageTag" style="width: 400px;" />
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
import { mapGetters } from 'vuex';
// eslint-disable-next-line import/no-extraneous-dependencies
import { debounce } from 'throttle-debounce';

import cdOperation from '@crud/CD.operation';
import rrOperation from '@crud/RR.operation';
import pagination from '@crud/Pagination';
import CRUD, { presenter, header, form, crud } from '@crud/crud';
import trainingImageApi, { getImageNameList, del, setPrecast } from '@/api/trainingImage/index';
import {
  getUniqueId,
  uploadSizeFomatter,
  invalidFileNameChar,
  ADMIN_ROLE_ID,
  hasPermission,
  validateImageName,
  validateImageTag,
} from '@/utils';
import BaseModal from '@/components/BaseModal';
import UploadInline from '@/components/UploadForm/inline';
import DropdownHeader from '@/components/DropdownHeader';
import UploadProgress from '@/components/UploadProgress';
import { imageConfig } from '@/config';

import { IMAGE_RESOURCE_ENUM, IMAGE_PROJECT_TYPE } from '../trainingJob/utils';

const defaultForm = {
  imageName: null,
  imagePath: null,
  imageTag: null,
  remark: null,
  projectType: IMAGE_PROJECT_TYPE.TRAIN,
  imageResource: Number(IMAGE_RESOURCE_ENUM.CUSTOM),
};

const defaultQuery = {
  imageStatus: null,
  imageNameOrId: null,
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
        add: imageConfig.allowUploadImage && hasPermission('training:image:upload'),
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
    return {
      active: IMAGE_RESOURCE_ENUM.CUSTOM,
      localQuery: { ...defaultQuery },
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
        projectType: [{ required: true, message: '请选择镜像类型', trigger: 'change' }],
        imageResource: [{ required: true, message: '请选择镜像来源', trigger: 'change' }],
        imageName: [
          { required: true, message: '请选择项目名称', trigger: 'change' },
          { validator: validateImageName, trigger: ['blur', 'change'] },
        ],
        imagePath: [{ required: true, message: '请输入镜像路径', trigger: ['blur', 'manual'] }],
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
        { color: '#909399', percentage: 40 },
        { color: '#e6a23c', percentage: 80 },
        { color: '#67c23a', percentage: 100 },
      ],
      disableEdit: false,
      loading: false,
      isEdit: false,
      prefabricate: true,
      // 以下为配置参数及常量参数
      imageConfig,
      IMAGE_RESOURCE_ENUM,
      IMAGE_PROJECT_TYPE,
      uploadFilters: [invalidFileNameChar],
    };
  },
  computed: {
    ...mapGetters(['user', 'isAdmin']),
    rolePermissions() {
      const { roles } = this.user;
      return roles && roles.length && roles[0].id === ADMIN_ROLE_ID;
    },
    isCustom() {
      return this.active === IMAGE_RESOURCE_ENUM.CUSTOM;
    },
    isPreset() {
      return this.active === IMAGE_RESOURCE_ENUM.PRESET;
    },
    isNotebook() {
      return this.active === IMAGE_RESOURCE_ENUM.NOTEBOOK;
    },
    isTerminal() {
      return this.active === IMAGE_RESOURCE_ENUM.TERMINAL;
    },
    disableAdd() {
      if (this.isAdmin) return this.isTerminal; // 管理员只有在终端镜像处无法点击上传
      return !this.isCustom; // 其他角色只有在我的镜像处可以点击上传
    },
    operationProps() {
      return {
        disabled: this.disableAdd,
      };
    },
    imageStatusList() {
      const arr = [{ label: '全部', value: null }];
      for (const key in this.statusMap) {
        arr.push({ label: this.statusMap[key], value: key });
      }
      return arr;
    },
    status() {
      return this.progress === 100 ? 'success' : null;
    },
  },
  mounted() {
    this.crud.refresh();
    this.refetch = debounce(3000, this.crud.refresh);
    this.updateImagePath();
  },
  methods: {
    hasPermission,
    // handle
    handleClick() {
      this.localQuery = { ...defaultQuery };
      this.crud.toQuery();
      // 切换tab键时让表格重渲
      this.prefabricate = false;
      this.$nextTick(() => {
        this.prefabricate = true;
      });
    },
    onFileRemove() {
      this.form.imagePath = null;
      this.loading = false;
      this.$refs.imagePath.validate('manual');
    },
    uploadStart(files) {
      this.updateImagePath();
      [this.loading, this.size, this.progress] = [true, files.size, 0];
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
      if (this.isPreset) {
        this.form.imageResource = Number(IMAGE_RESOURCE_ENUM.PRESET);
      } else if (this.isNotebook) {
        this.form.projectType = IMAGE_PROJECT_TYPE.NOTEBOOK;
      }
    },
    [CRUD.HOOK.beforeRefresh]() {
      this.crud.query = { ...this.localQuery };
      switch (this.active) {
        case IMAGE_RESOURCE_ENUM.CUSTOM:
        case IMAGE_RESOURCE_ENUM.PRESET:
          this.crud.query.projectType = IMAGE_PROJECT_TYPE.TRAIN;
          this.crud.query.imageResource = Number(this.active);
          break;
        case IMAGE_RESOURCE_ENUM.NOTEBOOK:
          this.crud.query.projectType = IMAGE_PROJECT_TYPE.NOTEBOOK;
          break;
        case IMAGE_RESOURCE_ENUM.TERMINAL:
          this.crud.query.projectType = IMAGE_PROJECT_TYPE.TERMINAL;
          break;
        // no default
      }
    },
    [CRUD.HOOK.beforeToEdit]() {
      this.isEdit = false;
    },
    async getImageNameList() {
      this.harborProjectList = await getImageNameList({ projectTypes: [this.form.projectType] });
    },
    onImageTypeChange() {
      this.form.imageName = null;
      this.form.imageResource = Number(IMAGE_RESOURCE_ENUM.CUSTOM);
      this.getImageNameList();
    },
    onDialogClose() {
      if (this.isEdit) {
        this.$refs.upload.formRef.reset();
      }
      this.loading = false;
    },
    async onDialogOpen() {
      this.getImageNameList();
    },
    checkStatus() {
      if (this.crud.data.some((item) => [0].includes(item.imageStatus))) {
        this.refetch();
      }
    },
    filterStatus(status) {
      this.localQuery.imageStatus = status;
      this.crud.toQuery();
    },
    resetQuery() {
      this.localQuery = { ...defaultQuery };
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
      this.$confirm('此操作将永久删除该镜像, 是否继续?', '请确认').then(async () => {
        await del({ ids: [id] });
        this.$message({
          message: '删除成功',
          type: 'success',
        });
        this.crud.refresh();
      });
    },
    doPrecast(i) {
      setPrecast({ id: i }).then(() => {
        this.$message({
          message: '设置成功',
          type: 'success',
        });
        this.crud.refresh();
      });
    },
    resourceObj(resource) {
      return resource
        ? { icon: 'el-icon-circle-check', color: '#67C23A', butText: '当前为默认' }
        : { icon: 'el-icon-circle-close', color: '#F56C6C', butText: '设为默认' };
    },
    uploadSizeFomatter,
  },
};
</script>
<style lang="scss" scoped>
.el-radio.is-bordered {
  width: 130px;
  height: 35px;
  padding: 10px 0;
  text-align: center;
}
</style>
