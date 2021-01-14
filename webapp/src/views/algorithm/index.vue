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
            id="algorithmName"
            v-model="localQuery.algorithmName"
            clearable
            placeholder="请输入算法名称或 ID"
            style="width: 200px;"
            class="filter-item"
            @keyup.enter.native="crud.toQuery"
            @clear="crud.toQuery"
          />
          <el-input
            id="algorithmUsage"
            v-model="localQuery.algorithmUsage"
            clearable
            placeholder="请输入算法用途"
            style="width: 200px;"
            class="filter-item"
            @keyup.enter.native="crud.toQuery"
            @clear="crud.toQuery"
          />
          <rrOperation class="fr search-btns" @resetQuery="onResetQuery" />
        </span>
      </cdOperation>
      <div>
        <el-tabs v-model="active" class="eltabs-inlineblock" @tab-click="handleClick">
          <el-tab-pane id="tab_0" label="我的算法" name="1" />
          <el-tab-pane id="tab_1" label="预置算法" name="2" />
        </el-tabs>
      </div>
    </div>
    <!--表格渲染-->
    <el-table
      ref="table"
      v-loading="crud.loading || disableEdit"
      :data="crud.data"
      highlight-current-row
      @selection-change="crud.selectionChangeHandler"
      @sort-change="crud.sortChange"
      @row-click="goDetail"
    >
      <el-table-column v-if="isCustom" prop="id" label="ID" width="80" sortable="custom" fixed />
      <el-table-column prop="algorithmName" label="名称" fixed />
      <el-table-column prop="algorithmUsage" label="算法用途" min-width="100px">
        <template slot-scope="scope">
          {{ scope.row.algorithmUsage || '--' }}
        </template>
      </el-table-column>
      <el-table-column
        prop="description"
        label="描述"
        min-width="200px"
        show-overflow-tooltip
      />
      <el-table-column
        prop="createTime"
        label="创建时间"
        min-width="160px"
      >
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="370px" fixed="right">
        <template slot-scope="scope">
          <el-button v-if="isCustom" :id="`goEdit_`+scope.$index" type="text" @click.stop="goEdit(scope.row)">在线编辑</el-button>
          <el-button :id="`goTraining_`+scope.$index" type="text" @click.stop="goTraining(scope.row)">创建训练任务</el-button>
          <el-button :id="`goDownload_`+scope.$index" type="text" @click.stop="goDownload(scope.row)">下载</el-button>
          <el-button v-if="isPreset" :id="`doFork_`+scope.$index" type="text" @click.stop="doFork(scope.row)">fork</el-button>
          <el-dropdown v-if="isCustom">
            <el-button type="text" style="margin-left: 10px;" @click.stop>
              更多<i class="el-icon-arrow-down el-icon--right" />
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item :id="`doFork_`+scope.$index" @click.native="doFork(scope.row)">
                <el-button type="text">fork</el-button>
              </el-dropdown-item>
              <el-dropdown-item v-if="isCustom" :id="`doDelete_`+scope.$index" @click.native="doDelete(scope.row.id)">
                <el-button type="text">删除</el-button>
              </el-dropdown-item>
            </el-dropdown-menu></el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <!--分页组件-->
    <pagination />
    <!--表单组件-->
    <BaseModal
      :before-close="crud.cancelCU"
      :visible="crud.status.cu > 0"
      :title="formType === 'add' ? '上传算法' : 'fork生成算法'"
      :loading="crud.status.cu === 2"
      :disabled="uploading"
      width="800px"
      @close="onDialogClose"
      @cancel="crud.cancelCU"
      @ok="crud.submitCU"
    >
      <div v-if="formType === 'fork'">以下操作将按照预置算法的配置生成一份您的算法，进而可以做后续配置。</div>
      <el-form
        ref="form"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="名称" prop="algorithmName">
          <el-input
            id="algorithmName"
            v-model.trim="form.algorithmName"
            placeholder
            maxlength="32"
            show-word-limit
            style="width: 300px;"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            id="description"
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder
            style="width: 600px;"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="算法用途" prop="algorithmUsage">
          <el-select
            id="algorithmUsage"
            v-model="form.algorithmUsage"
            placeholder="请选择或输入算法用途"
            filterable
            clearable
            allow-create
            @change="onAlgorithmUsageChange"
          >
            <el-option
              v-for="item in algorithmUsageList"
              :key="item.id"
              :label="item.auxInfo"
              :value="item.auxInfo"
            >
              <span style="float: left;">{{ item.auxInfo }}</span>
              <el-button
                v-if="!item.isDefault"
                class="select-del-btn"
                type="text"
                @click.stop="delAlgorithmUsage(item)"
              ><i class="el-icon-close" /></el-button>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-show="formType !== 'fork'" ref="codeDir" label="上传代码包" prop="codeDir">
          <div v-if="formType === 'fork' && form.codeDir">源代码包：
            <el-button id="goDownload" type="text" @click="goDownload(form)">下载</el-button>
          </div>
          <upload-inline
            v-if="crud.status.cu > 0"
            ref="upload"
            action="fakeApi"
            accept=".zip"
            :acceptSize="algorithmConfig.uploadFileAcceptSize"
            :acceptSizeFormat="uploadSizeFomatter"
            list-type="text"
            :show-file-count="false"
            :params="uploadParams"
            :auto-upload="true"
            :hash="false"
            :filters="uploadFilters"
            :limit="1"
            :on-remove="onFileRemove"
            @uploadStart="uploadStart"
            @uploadSuccess="uploadSuccess"
            @uploadError="uploadError"
          />
          <upload-progress
            v-if="uploading" 
            :progress="progress" 
            :color="customColors" 
            :status="status" 
            :size="size" 
            @onSetProgress="onSetProgress"
          />    
        </el-form-item>
        <el-form-item label="训练输出" prop="isTrainOut" class="is-required">
          <el-tooltip
            class="item"
            effect="dark"
            content="请确保代码中包含“train_out”参数用于接收训练的模型输出路径"
            placement="right"
          >
            <i class="el-icon-warning-outline primary f18 vm" />
          </el-tooltip>
        </el-form-item>
        <el-form-item label="断点续训">
          <el-tooltip
            class="item"
            effect="dark"
            content="请确保代码中包含“model_load_dir”参数用于接收训练的断点路径"
            placement="right"
          >
            <i class="el-icon-warning-outline primary f18 vm" />
          </el-tooltip>
        </el-form-item>
        <el-form-item label="日志输出" prop="isTrainLog">
          <el-checkbox id="isTrainLog" v-model="form.isTrainLog" />
          <el-tooltip
            v-show="form.isTrainLog"
            class="item"
            effect="dark"
            content="请确保代码中包含“train_log”参数用于接收训练的日志输出路径"
            placement="right"
          >
            <i class="el-icon-warning-outline primary f18 vm" />
          </el-tooltip>
        </el-form-item>
        <el-form-item label="可视化日志" prop="isVisualizedLog">
          <el-checkbox id="isVisualizedLog" v-model="form.isVisualizedLog" />
          <el-tooltip
            v-show="form.isVisualizedLog"
            class="item"
            effect="dark"
            content="请确保代码中包含“train_visualized_log”参数用于接收训练的可视化日志路径，仅支持在训练时使用 oneflow 镜像"
            placement="right"
          >
            <i class="el-icon-warning-outline primary f18 vm" />
          </el-tooltip>
        </el-form-item>
      </el-form>
    </BaseModal>
    <!--右边侧边栏-->
    <el-drawer
      :visible.sync="drawer"
      :with-header="false"
      :direction="'rtl'"
      size="36%"
      :before-close="handleClose"
    >
      <algorithm-detail :item="selectedItemObj" />
    </el-drawer>
  </div>
</template>

<script>
import { downloadZipFromObjectPath, validateNameWithHyphen, getUniqueId, uploadSizeFomatter, invalidFileNameChar } from '@/utils';
import CRUD, { presenter, header, form, crud } from '@crud/crud';
import cdOperation from '@crud/CD.operation';
import rrOperation from '@crud/RR.operation';
import pagination from '@crud/Pagination';
import crudAlgorithm, { del as deleteAlgorithm } from '@/api/algorithm/algorithm';
import { list as getAlgorithmUsages, add as addAlgorithmUsage, del as deleteAlgorithmUsage } from '@/api/algorithm/algorithmUsage';
import { createNotebook, getNotebookAddress } from '@/api/development/notebook';
import BaseModal from '@/components/BaseModal';
import AlgorithmDetail from '@/components/Training/algorithmDetail';
import UploadInline from '@/components/UploadForm/inline';
import UploadProgress from '@/components/UploadProgress';
import { algorithmConfig } from '@/config';

const defaultForm = {
  id: null,
  algorithmName: null,
  algorithmSource: null,
  algorithmUsage: null,
  description: null,
  codeDir: null,
  accuracy: null,
  p4InferenceSpeed: null,
  isTrainLog: true,
  isTrainOut: true,
  isVisualizedLog: true,
  fork: false,
};
export default {
  name: 'Algorithm',
  components: {
    BaseModal,
    pagination,
    cdOperation,
    AlgorithmDetail,
    UploadInline,
    rrOperation,
    UploadProgress,
  },
  cruds() {
    return CRUD({
      title: '算法管理',
      crudMethod: { ...crudAlgorithm },
      optShow: {
        del: false,
      },
      queryOnPresenterCreated: false, // created 时不请求数据
      props: {
        optText: {
          add: '上传算法',
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
      active: '1',
      localQuery: {
        algorithmName: null,
      },
      defaultQuery: {
        algorithmName: null,
      },
      rules: {
        algorithmName: [
          {
            required: true,
            message: '请输入算法名称',
            trigger: 'blur',
          },
          {
            max: 32,
            message: '长度不超过32个字符',
            trigger: ['blur', 'change'],
          },
          {
            validator: validateNameWithHyphen,
            trigger: ['blur', 'change'],
          },
        ],
        codeDir: [
          {
            required: true,
            message: '请选择上传代码',
            trigger: ['blur', 'manual'],
          },
        ],
      },
      algorithmUsageList: [],
      formType: '', // 'add' or 'fork'
      // drawer var
      selectedItemObj: null,
      drawer: false,
      uploadParams: {
        objectPath: null, // 对象存储路径
      },
      disableEdit: false,
      uploading: false,
      progress: 0,
      size: 0,
      algorithmConfig,
      customColors: [
        {color: '#909399', percentage: 40},
        {color: '#e6a23c', percentage: 80},
        {color: '#67c23a', percentage: 100},
      ],
      uploadFilters: [invalidFileNameChar],
    };
  },
  computed: {
    allAlgorithmUsageList() {
      return [{ label: '全部', value: null }].concat(this.algorithmUsageList.map(item => {
        return { label: item.auxInfo, value: item.auxInfo };
      }));
    },
    operationProps() {
      return {
        disabled: this.isPreset,
      };
    },
    isCustom() {
      return this.active === '1';
    },
    isPreset() {
      return this.active === '2';
    },
    user() {
      return this.$store.getters.user;
    },
    status() {
      return this.progress === 100 ? 'success' : null;
    },
  },
  mounted() {
    this.getAlgorithmUsages();
    this.crud.query.algorithmSource = Number(this.active);
    this.crud.refresh();
    this.updateObjectPath();
  },
  beforeDestroy() {
    this.disableEdit = false;
  },
  methods: {
    // handle
    handleClick() {
      this.crud.query.algorithmSource = Number(this.active);
      this.crud.refresh();
    },
    handleClose(done) {
      done();
    },
    onResetQuery() {
      this.localQuery = { ...this.defaultQuery};
    },
    onDialogClose() {
      this.$refs.upload.formRef.reset();
      this.uploading = false;
    },
    onAlgorithmUsageChange(value) {
      const usageRes = this.algorithmUsageList.find(usage => usage.auxInfo === value);
      if (value && !usageRes) {
        this.createAlgorithmUsage(value);
      }
    },
    onFileRemove() {
      this.form.codeDir = null;
      this.uploading = false;
      this.$refs.codeDir.validate('manual');
    },
    uploadStart(files) {
      this.updateObjectPath();
      [ this.uploading, this.size, this.progress ] = [ true, files.size, 0 ];
    },
    onSetProgress(val) {
      this.progress += val;
    },
    uploadSuccess(res) {
      this.progress = 100;
      setTimeout(() => {
        this.uploading = false;
      }, 1000); 
      if (this.uploading) {
        this.form.codeDir = res[0].data.objectName;
        this.$refs.codeDir.validate('manual');
      }
    },
    uploadError() {
      this.$message({
        message: '上传文件失败',
        type: 'error',
      });
      this.uploading = false;
    },
    // link
    goDetail(itemObj) {
      this.selectedItemObj = itemObj;
      this.drawer = true;
    },
    goTraining(item) {
      this.$router.push({
        path: '/training/jobadd',
        name: 'jobAdd',
        params: {
          from: 'algorithm',
          params: {
            algorithmId: item.id,
            algorithmSource: Number(this.active),
            algorithmUsage: item.algorithmUsage,
            runParams: item.runParams,
            imageNameProject: item.imageNameProject,
            imageName: item.imageName,
            runCommand: item.runCommand,
          },
        },
      });
    },
    goDownload(algorithm) {
      downloadZipFromObjectPath(algorithm.codeDir, `${algorithm.algorithmName}.zip`, { flat: true });
      this.$message({
        message: '请查看下载文件',
        type: 'success',
      });
    },
    async goEdit(algorithm) {
      if (this.disableEdit) {
        return;
      }
      this.disableEdit = true;
      const notebookInfo = await createNotebook(1, {
        sourceId: algorithm.id,
        sourceFilePath: algorithm.codeDir,
      }).finally(() => {
        this.disableEdit = false;
      });
      if (notebookInfo.status === 0 && notebookInfo.url) {
        this.openNoteBook(notebookInfo.url, notebookInfo.noteBookName);
      } else {
        this.disableEdit = true;
        this.getNotebookAddress(notebookInfo.id, notebookInfo.noteBookName);
      }
    },
    // op
    async doDelete(id) {
      this.$confirm('此操作将永久删除该算法, 是否继续?', '请确认').then(async() => {
        await deleteAlgorithm({ ids: [id] });
        this.$message({
          message: '删除成功',
          type: 'success',
        });
        await this.crud.refresh();
      });
    },
    doFork(item) {
      this.formType = 'fork';
      item.fork = true;
      this.crud.toFork(item);
    },
    // hook
    [CRUD.HOOK.beforeToAdd]() {
      this.formType = 'add';
    },
    [CRUD.HOOK.beforeRefresh]() {
      this.crud.query = { ...this.localQuery};
      this.crud.query.algorithmSource = Number(this.active);
    },
    getNotebookAddress(id, noteBookName) {
      if (!this.disableEdit) {
        return;
      }
      getNotebookAddress(id).then(url => {
        if (url) {
          this.openNoteBook(url, noteBookName);
        } else {
          setTimeout(() => {
            this.getNotebookAddress(id, noteBookName);
          }, 1000);
        }
      }).catch(err => {
        this.disableEdit = false;
        throw new Error(err);
      });
    },
    async getAlgorithmUsages() {
      const params = {
        isContainDefault: true,
        current: 1,
        size: 1000,
      };
      const data = await getAlgorithmUsages(params);
      this.algorithmUsageList = data.result;
    },
    async createAlgorithmUsage(auxInfo) {
      await addAlgorithmUsage({ auxInfo });
      this.getAlgorithmUsages();
    },
    async delAlgorithmUsage(usage) {
      await deleteAlgorithmUsage({ ids: [usage.id] });
      if (this.form.algorithmUsage === usage.auxInfo) {
        this.form.algorithmUsage = null;
      }
      this.getAlgorithmUsages();
    },
    updateObjectPath() {
      this.uploadParams.objectPath = `upload-temp/${this.user.id}/${getUniqueId()}`;
    },
    openNoteBook(url, noteBookName) {
      window.open(url);
      this.$message.success('Notebook已启动.');
      this.disableEdit = false;
      this.$router.push({ name: 'Notebook', params: {
        noteBookName,
      }});
    },
    uploadSizeFomatter,
  },
};
</script>
