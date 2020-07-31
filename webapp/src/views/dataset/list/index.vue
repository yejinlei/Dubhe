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
      <cdOperation :addProps="operationProps" :delProps="operationProps">
        <el-button slot="left" icon="el-icon-upload" round class="filter-item" @click="showUploadHint">导入自定义数据集</el-button>
        <span slot="right">
          <!-- 搜索 -->
          <el-input
            v-model="query.name"
            clearable
            placeholder="输入名称或ID查询"
            style="width: 200px;"
            class="filter-item"
            @keyup.enter.native="crud.toQuery"
          />
          <el-date-picker
            v-model="query.createTime"
            :default-time="['00:00:00','23:59:59']"
            type="daterange"
            value-format="timestamp"
            range-separator=":"
            class="date-item"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :picker-options="pickerOptions"
          />
          <rrOperation @resetQuery="onResetQuery" />
        </span>
      </cdOperation>
    </div>
    <!--表单组件-->
    <el-dialog
      :key="dialogKey"
      append-to-body
      custom-class="create-dataset"
      center
      :close-on-click-modal="false"
      :visible="crud.status.cu > 0"
      title="创建数据集"
      width="610px"
      @close="closeDatasetDialog"
    >
      <el-steps :active="activeStep" finish-status="success">
        <el-step title="新建数据集" />
        <el-step title="导入数据" />
        <el-step title="完成" />
      </el-steps>
      <el-form
        v-if="activeStep === 0"
        ref="form"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="数据集名称" prop="name">
          <el-input v-model="form.name" placeholder="数据集名称不能超过50字" maxlength="50" />
        </el-form-item>
        <el-form-item label="数据类型" prop="dataType">
          <InfoSelect
            v-model="form.dataType"
            placeholder="数据类型"
            :dataSource="dataTypeList"
            @change="handleDataTypeChange"
          />
        </el-form-item>
        <el-form-item label="标注类型" prop="annotateType">
          <InfoSelect
            v-model="form.annotateType"
            placeholder="标注类型"
            :dataSource="annotationList"
            :disabled="form.dataType === 1"
            @change="handleAnnotateTypeChange"
          />
        </el-form-item>
        <el-form-item label="标签" prop="labels">
          <div class="label-input">
            <span v-if="chosenPresetLabelId" class="pl-16">
              {{ presetLabelList[chosenPresetLabelId] }}
            </span>
            <span v-else>
              <el-tag
                v-for="tag in form.labels"
                :key="tag.name"
                closable
                :disable-transitions="false"
                @close="removeLabelTag(tag.name)"
              >{{ tag.name }}</el-tag>
            </span>
            <label-popover
              :key="actionKey"
              :customLabel="customLabel"
              :systemLabel="systemLabel"
              :annotateType="form.annotateType"
              :presetLabelList="presetLabelList"
              :chosenPresetLabelId="chosenPresetLabelId"
              :setPresetLabel="setPresetLabel"
              :setNoPresetLabel="setNoPresetLabel"
              @hide="handleLabelHide"
            />
          </div>
        </el-form-item>
        <el-form-item label="数据集描述" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="数据集描述长度不能超过100字" maxlength="100" rows="3" show-word-limit />
        </el-form-item>
      </el-form>
      <div v-show="activeStep === 1">
        <upload-inline
          ref="initFileUploadForm"
          action="fakeApi"
          :params="uploadParams"
          :transformFile="withDimensionFile"
          v-bind="optionCreateProps"
          @uploadSuccess="uploadSuccess"
          @uploadError="uploadError"
        />
        <el-form
          v-if="form.dataType === 1"
          ref="formStep1"
          :model="step1Form"
          label-width="100px"
          style="margin-top: 10px;"
        >
          <el-form-item
            label="视频帧间隔"
            prop="frameInterval"
            :rules="[{required: true, message: '请输入有效的帧间隔', trigger: 'blur'}]"
          >
            <el-input-number v-model="step1Form.frameInterval" :min="1" />
          </el-form-item>
        </el-form>
      </div>
      <el-progress
        v-if="activeStep === 2 && skipUpload !== true"
        :type="form.dataType === 1 ? 'line' : 'circle'"
        :percentage="form.dataType === 1 ? 100 : uploadPercent"
        :status="uploadStatus"
        :class="form.dataType === 1 ? 'upload-progress' : ''"
        :format="formatProgress"
      />
      <div slot="footer" class="dialog-footer">
        <div v-if="activeStep === 0">
          <el-button :loading="crud.status.cu === 2" type="primary" @click="createDataset">下一步</el-button>
        </div>
        <div v-if="activeStep === 1">
          <el-button @click="datasetNextStep">跳过</el-button>
          <el-button type="primary" @click="uploadSubmit('initFileUploadForm')">确定上传</el-button>
        </div>
        <div v-if="activeStep >= 2">
          <el-button
            v-if="createDatasetStatus === '上传中'"
            @click="cancelUpload('initFileUploadForm')"
          >取消</el-button>
          <el-button
            :loading="createDatasetStatus !== '完成'"
            type="primary"
            @click="completeCreateDataset"
          >确定</el-button>
        </div>
      </div>
    </el-dialog>
    <!--导入提示窗-->
    <el-dialog
      title="导入自定义数据集"
      width="600px"
      :visible="uploadHintVisible"
      @close="closeUploadHint"
    >
      <div style="background: #ffe9cc; padding: 10px; color: #f38900;">
        <p>1. 用户自定义导入的数据集，须在模型训练中根据格式自行解析</p>
        <p>2. 请确保数据集图片和标注信息的完整性</p>
        <p>3. 用户上传的数据集必须为 zip 压缩包</p>
      </div>
        
        <div slot="footer" class="tc">
          <el-button type="primary" @click="showUploadDatasetForm">已阅读，确定上传</el-button>
        </div>
    </el-dialog>
    <!--导入自定义数据集表单组件-->
    <ImportDataset 
      ref="uploadDatasetForm" 
      :visible="uploadDatasetFormVisible" 
      :closeUploadDatasetForm="closeUploadDatasetForm"
    />
    <!--上传表单-->
    <UploadForm
      action="fakeApi"
      :visible="uploadDialogVisible"
      :toggleVisible="toggleUploadFormClose"
      :params="uploadParams"
      :transformFile="withDimensionFile"
      v-bind="optionImportProps"
      @uploadSuccess="uploadSuccess"
      @uploadError="uploadError"
      @close="handleUploadFormClose"
    >
      <template v-slot:default="slotProps">
        <el-progress
          v-if="slotProps.uploading === true && importRow && importRow.dataType === 1"
          type="line"
          :percentage="100"
          class="upload-progress"
          :format="formatImportProgress(slotProps)"
        />
      </template>
      <el-form
        v-if="isTrackRow"
        ref="importForm"
        :model="importForm"
        label-width="100px"
        style="margin-top: 10px;"
      >
        <el-form-item
          label="视频帧间隔"
          prop="frameInterval"
          :rules="[{required: true, message: '请输入有效的帧间隔', trigger: 'blur'}]"
        >
          <el-input-number v-model="importForm.frameInterval" :min="1" />
        </el-form-item>
      </el-form>
    </UploadForm>
    <div class="mb-10">
      <el-tabs :value="activePanel" class="eltabs-inlineblock" @tab-click="handlePanelClick">
        <el-tab-pane label="我的数据集" name="0" />
        <el-tab-pane label="预置数据集" name="2" />
      </el-tabs>
    </div>
    <!--表格渲染-->
    <el-table
      ref="table"
      v-loading="crud.loading"
      :data="crud.data"
      highlight-current-row
      @selection-change="crud.selectionChangeHandler"
      @sort-change="crud.sortChange"
      @current-change="handleCurrentChange"
    >
      <el-table-column fixed type="selection" min-width="40" :selectable="canSelect" />
      <el-table-column fixed prop="id" width="70" label="ID" sortable="custom" align="left" />
      <el-table-column
        fixed
        show-overflow-tooltip
        prop="name"
        label="名称"
        min-width="160"
        align="left"
        class-name="dataset-name-col"
      >
        <template slot-scope="scope">
          <el-link class="mr-10 name-col" :underline="!isImport(scope.row)" @click="goDetail(scope.row)">{{ scope.row.name }}</el-link>
          <Edit v-if="!isImport(scope.row)" class="edit-icon" :row="scope.row" valueBy="name" title="修改数据集名称" @handleOk="handleEditDatasetName" />
        </template>
      </el-table-column>
      <el-table-column
        show-overflow-tooltip
        prop="dataType"
        :formatter="parseDataType"
        width="100"
        align="left"
      >
        <template slot="header">
          <el-dropdown trigger="click" @command="filterByDataType">
            <span>
              <span :class="dataTypeFilter === 'all' ? '' : 'primary'">数据类型</span>
              <i class="el-icon-arrow-down el-icon--right"/>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item
                v-for="item in withAllDataTypeList"
                :key="item.value"
                :command="item.value"
              >{{ item.label }}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
      <el-table-column prop="progress" min-width="200" label="进度" align="left">
        <template v-if="scope.row.progress !== null" slot-scope="scope">
          <div class="flex progress-wrap">
            <i v-show="scope.row.pollIng && !isImport(scope.row)" class="el-icon-loading" />
            <div v-if="showUnZip(scope.row)" class="lh-1 flex" style="width: 100%">
              <!-- 只展示导入中进度条 -->
              <el-progress
                v-if="isUnzip(scope.row)"
                type="line"
                :show-text="false"
                :percentage="100"
                class="decompress-progress"
              />
              <span class="f14" style="width: 60px">{{decompressStateMap[scope.row.decompressState]}}</span>
            </div>
            <el-popover 
              v-else
              placement="top-start" 
              width="120" 
              trigger="hover" 
              popper-class="f1"
            >
              <TableTooltip
                class="progress-tip"
                :keys="progressKeys"
                :data="scope.row.progress"
                :annotateType="scope.row.annotateType"
                :keyAccessor="keyAccessor"
                :valueAccessor="valueAccessor"
              />
              <el-progress
                slot="reference"
                :percentage="getProgress(scope.row) || 0"
                :color="progressFill(scope.row.status)"
              />
            </el-popover>
          </div>
        </template>
      </el-table-column>
      <el-table-column
        show-overflow-tooltip
        prop="annotateType"
        :formatter="parseAnnotateType"
        align="left"
        min-width="100"
      >
        <template slot="header">
          <el-dropdown trigger="click" @command="filterByAnnotateType">
            <span>
              <span :class="annotateFilter === 'all' ? '' : 'primary'">标注类型</span>
              <i class="el-icon-arrow-down el-icon--right"/>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item
                v-for="item in withAllAnnotationList"
                :key="item.value"
                :command="item.value"
              >{{ item.label }}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
      <Status
        prop="status"
        min-width="115"
        label="状态"
        align="left"
        :withAllDatasetStatusList="withAllDatasetStatusList"
        :filterByDatasetStatus="filterByDatasetStatus"
        :datasetStatusFilter="datasetStatusFilter"
      />
      <el-table-column prop="currentVersionName" min-width="80" label="当前版本" align="left" />
      <el-table-column
        prop="updateTime"
        min-width="160"
        label="更新时间"
        :formatter="formatDate"
        sortable="custom"
        align="left"
      />
      <el-table-column
        prop="remark"
        min-width="200"
        label="数据集描述"
        align="left"
        show-overflow-tooltip
      />
      <!--  <el-table-column prop="updateUser.username" min-width="100" show-overflow-tooltip label="最近更新人" align="left" />
      <el-table-column prop="createUser.username" min-width="100" show-overflow-tooltip label="创建人" align="left" /> -->
      <Action
        fixed="right"
        min-width="330"
        align="left"
        :showPublish="showPublish"
        :openUploadDialog="openUploadDialog"
        :goDetail="goDetail"
        :autoAnnotate="autoAnnotate"
        :gotoVersion="gotoVersion"
        :download="download"
        :reAnnotation="reAnnotation"
        :dataEnhance="showDataEnhance"
      />
    </el-table>
    <!--分页组件-->
    <el-pagination
      :page-size.sync="crud.page.size"
      :page-sizes="[10, 20, 50]"
      :total="crud.page.total"
      :current-page.sync="crud.page.current"
      :style="`text-align:${crud.props.paginationAlign};`"
      style="margin-top: 8px;"
      layout="total, prev, pager, next, sizes"
      @size-change="crud.sizeChangeHandler($event)"
      @current-change="crud.pageChangeHandler"
    />
    <BaseModal
      :visible="actionModal.show && actionModal.type === 'publish'"
      :loading="actionModal.showOkLoading"
      title="发布数据集"
      @change="handleCancel"
      @ok="handlePublish(actionModal.row)"
    >
      <Publish v-if="actionModal.row" ref="publishForm" :row="actionModal.row" />
    </BaseModal>
    <DataEnhance
      :key="enhanceKey"
      :visible="actionModal.show && actionModal.type === 'dataEnhance'"
      :loading="actionModal.showOkLoading"
      :row="actionModal.row"
      :handleCancel="handleCancel"
      :handleOk="handleDataEnhance"
    />
  </div>
</template>

<script>
import { Message } from 'element-ui';
import { isNil, invert } from 'lodash';
import { mapState } from 'vuex';

import crudDataset, { editDataset, detail, postDataEnhance, queryPresetLabels } from '@/api/preparation/dataset';
import {
  publish,
  autoAnnotate,
  annotateStatus,
  delAnnotation,
} from '@/api/preparation/annotation';
import CRUD, { presenter, header, form, crud } from '@crud/crud';
import rrOperation from '@crud/RR.operation';
import cdOperation from '@crud/CD.operation';
import datePickerMixin from '@/mixins/datePickerMixin';
import UploadForm from '@/components/UploadForm';
import UploadInline from '@/components/UploadForm/inline';
import LabelPopover from '@/components/LabelPopover';
import InfoSelect from '@/components/InfoSelect';
import { getAutoLabels } from '@/api/preparation/datalabel';

import { submit, submitVideo } from '@/api/preparation/datafile';
import { getImgFromMinIO, annotationMap, dataTypeMap, annotationProgressMap, decompressProgressMap, datasetStatusMap, withDimensionFile } from '@/views/dataset/util';
import Edit from '@/components/InlineTableEdit';
import BaseModal from '@/components/BaseModal';
import { toFixed, isEqualByProp, formatDateTime, downloadZipFromObjectPath } from '@/utils';
import { TableTooltip } from '@/hooks/tooltip';
import { validateName } from '@/utils/validate';
import store from '@/store';

import Status from './status';
import Action from './action';
import Publish from './publish';
import ImportDataset from './import-dataset';
import DataEnhance from './data-enhance';

// 默认帧间隔
const defaultFrameInterval = 5;

const defaultForm = {
  id: null,
  name: null,
  dataType: null,
  annotateType: null,
  labels: [],
  presetLabelType: '',
  remark: '',
  type: 0,
};

// 轮询类型 map
const pollMap = {
  7: 'sample',
  8: 'enhance',
  2: 'autoAnnotate',
  'unzip': 'unzip',
};

// 自定义数据集
const isImport = d => d.import;
// 解压中
const isUnzip = d => isImport(d) && [0, 1].includes(d.decompressState);

export default {
  name: 'DataSet',
  components: {
    cdOperation,
    rrOperation,
    UploadForm,
    UploadInline,
    LabelPopover,
    Edit,
    BaseModal,
    Publish,
    ImportDataset,
    Status,
    Action,
    InfoSelect,
    TableTooltip,
    DataEnhance,
  },
  cruds() {
    return CRUD({
      title: '数据集管理',
      crudMethod: { ...crudDataset },
      props: { optText: { add: '创建数据集' }},
      queryOnPresenterCreated: false,
    });
  },
  mixins: [presenter(), header(), form(defaultForm), crud(), datePickerMixin],
  data() {
    return {
      chosenDatasetId: 0,
      chosenDatasetStatus: 0,
      uploadDialogVisible: false,
      uploadDatasetFormVisible: false,
      uploadHintVisible: false,
      activeStep: 0,
      createDatasetStatus: '未完成',
      uploadPercent: 0,
      skipUpload: false, // 跳过上传
      videoUploadProgress: 0, // 视频上传进度计算
      uploadStatus: undefined,
      dialogKey: 1,
      actionKey: 1,
      enhanceKey: 1000,
      currentRow: null,
      customLabel: [],
      systemLabel: [],
      presetLabelList: {}, // 预置标签列表
      chosenPresetLabelId: null, // 已选中的预置标签id
      annotateFilter: 'all',
      dataTypeFilter: 'all',
      datasetStatusFilter: 'all',
      rules: {
        name: [
          { required: true, message: '请输入数据集名称', trigger: ['change', 'blur'] },
          { validator: validateName, trigger: ['change', 'blur'] },
        ],
        dataType: [
          { required: true, message: '请选择数据类型', trigger: 'change' },
        ],
        annotateType: [
          { required: true, message: '请选择标注类型', trigger: 'change' },
        ],
        remark: [
          { required: false, message: '请输入数据集描述信息', trigger: 'blur' },
        ],
        labels: [{ required: false, message: '请添加标签', trigger: 'blur' }],
      },
      actionModal: {
        show: false,
        row: undefined,
        showOkLoading: false,
        type: null,
      },
      importRow: null,
      trackUploadProps: {
        acceptSize: 1024,
        accept: '.mp4,.avi,.mkv,.mov,.webm,.wmv',
        listType: 'text',
        limit: 1,
        multiple: false,
        showFileCount: false,
      },
      step1Form: {
        frameInterval: defaultFrameInterval, // 默认值
      },
      importForm: {
        frameInterval: defaultFrameInterval, // 默认值
      },
      autoTimer: {}, // 自动标注定时器
      progressKeys: Object.keys(annotationProgressMap),
      decompressStateMap: decompressProgressMap,
    };
  },
  computed: {
    ...mapState({
      activePanel: state => {
        return String(state.dataset.activePanel);
      },
    }),
    // 文件上传前携带尺寸信息
    withDimensionFile() {
      return withDimensionFile;
    },
    // 自定义上传数据集
    isImport() {
      return isImport;
    },
    // 解压缩中的自定义数据集
    isUnzip() {
      return isUnzip;
    },
    localQuery() {
      return {
        type: this.activePanel || 0,
      };
    },
    uploadParams() {
      // 是否为视频数据类类型
      const isVideo =
        this.importRow?.dataType === 1 || this.form.dataType === 1;
      const dir = isVideo ? `video` : `origin`;
      return {
        datasetId: this.chosenDatasetId,
        objectPath: `dataset/${this.chosenDatasetId}/${dir}`, // 对象存储路径
      };
    },
    annotationList() {
      // 原始标注列表
      const rawAnnotationList = Object.keys(annotationMap).map(d => ({
        label: annotationMap[d].name,
        value: Number(d),
      }));
      // 如果是图片，目标跟踪不可用
      // 如果是视频，只能用目标跟踪
      return rawAnnotationList.map(d => {
        let disabled = false;
        if (this.form.dataType === 0) {
          disabled = d.value === 5;
        } else if (this.form.dataType === 1) {
          disabled = d.value !== 5;
        }
        return {
          ...d,
          disabled,
        };
      });
    },
    withAllAnnotationList() {
      const rawAnnotationList = Object.keys(annotationMap).map(d => ({
        label: annotationMap[d].name,
        value: Number(d),
      }));
      return [{ label: '全部', value: 'all' }].concat(rawAnnotationList);
    },
    dataTypeList: () =>
      Object.keys(dataTypeMap).map(d => ({
        label: dataTypeMap[d],
        value: Number(d),
      })),
    withAllDataTypeList() {
      const rawDataTypeList = Object.keys(dataTypeMap).map(d => ({
        label: dataTypeMap[d],
        value: Number(d),
      }));
      return [{ label: '全部', value: 'all' }].concat(rawDataTypeList);
    },
    datasetStatusList: () =>
      Object.keys(datasetStatusMap).map(d => ({
        label: datasetStatusMap[d].name,
        value: Number(d),
      })),
    withAllDatasetStatusList() {
      const rawDatasetStatusList = Object.keys(datasetStatusMap).map(d => ({
        label: datasetStatusMap[d].name,
        value: Number(d),
      }));
      return [{ label: '全部', value: 'all' }].concat(rawDatasetStatusList);
    },
    // 新建数据集（视频）上传组件参数
    optionCreateProps() {
      const props = this.form.dataType === 1 ? this.trackUploadProps : {};
      return props;
    },
    // 数据集（视频）导入上传组件参数
    optionImportProps() {
      const props = this.importRow?.dataType === 1 ? this.trackUploadProps : {};
      return props;
    },
    isTrackRow() {
      return this.importRow && this.importRow.dataType === 1;
    },
    // 区分预置数据集和普通数据集操作权限
    operationProps() {
      return Number(this.activePanel) === 2 ? { disabled: true } : undefined;
    },
  },
  watch: {
    // eslint-disable-next-line func-names
    'crud.data': function(next, prev = []) {
      // 首先匹配size
      // 其次匹配 id 是否一致
      // 再次检查此前是否存在轮询的数据集
      if (next.length > 0 && (prev.length !== next.length || !isEqualByProp(prev, next, 'id') || prev.some(d => d.pollIng))) {
        // 获取自动标注状态列表的结果
        const autoList = next.filter(d => d.status === 2);
        // 获取正在采用中的数据
        const sampleList = next.filter(d => d.status === 7);
        // 获取数据增强中的数据
        const enhanceList = next.filter(d => d.status === 8);
        // 获取导入后未解压0正在解压1的数据集
        const unZipList = next.filter(isUnzip);
        // 需要轮询的列表
        const pollList = autoList.concat(sampleList, enhanceList, unZipList);

        Promise.all(
          pollList.map(row => {
            // vue hack, 初始化状态
            this.$set(row, 'pollIng', true);
            // 采样、增强给一个默认起始数字
            if ([7, 8].includes(row.status)) {
              this.$set(row, 'sample_progress', 10);
            }
            // 导入自定义数据集
            if (isUnzip(row)) {
              this.$set(row, 'pollType', 'unzip');
            }
            // 轮询类型，兼容自定义数据集
            return this.poll(row, pollMap[row.pollType || row.status]);
          }),
        );
      }
    },
  },
  created() {
    this.crud.toQuery();
    getAutoLabels().then(res => {
      res.forEach((item) => {
        this.systemLabel.push({
          id: item.id,
          name: item.name,
          color: item.color,
          chosen: false,
        });
      });
    });
    // 获取接口返回的预置标签列表
    queryPresetLabels().then(res => {
      this.presetLabelList = res;
    });
  },
  mounted() {
    if (this.$route.params.type === 'add') {
      setTimeout(() => {
        this.crud.toAdd();
      }, 500);
    }
  },
  beforeDestroy() {
    Object.keys(this.autoTimer).forEach(key => clearTimeout(this.autoTimer[key]));
  },
  methods: {
    keyAccessor: (key) => annotationProgressMap[key],
    valueAccessor: (key, idx, data) => data[key],
    [CRUD.HOOK.beforeRefresh]() {
      this.crud.query = { ...this.query, ...this.localQuery};
    },
    onResetQuery() {
      // 重置查询条件
      this.query = {};
      this.crud.order = null;
      this.crud.sort = null;
      this.crud.params = {};
      // 重置表格的排序和筛选条件
      this.dataTypeFilter = 'all';
      this.datasetStatusFilter = 'all';
      this.annotateFilter = 'all';
      this.$refs.table.clearSort();
    },
    handlePanelClick(tab) {
      this.onResetQuery();
      store.dispatch('dataset/togglePanel', Number(tab.name));
      Object.assign(this.localQuery, {
        type: Number(tab.name),
      });
      this.crud.refresh();
    },
    // 进度条颜色
    progressFill(status) {
      const fillMap = {
        2: '#52C41A', // 自动标注中
        7: '#606266', // 采样中
      };
      if (fillMap[status]) return fillMap[status];
      return '#52C41A';
    },
    // 新建数据集进度格式化
    formatProgress(percentage) {
      let formatTxt = `${percentage}%`;
      if (this.form.dataType === 1) {
        formatTxt = this.videoUploadProgress === 100 ? `100%` : `上传中...`;
      }
      return formatTxt;
    },
    // 导入数据进度格式化
    formatImportProgress(slotProps) {
      const _this = this;
      return (percentage) => {
        let formatTxt = `${percentage}%`;
        if (_this.importRow?.dataType === 1) {
          formatTxt =
          slotProps.uploading === false && slotProps.progress === 100
            ? `100%` : `上传中...`;
        }
        return formatTxt;
      };
    },
    handleUploadFormClose() {
      // 重置上传状态
      this.resetUpload();
    },
    handleDataTypeChange(value) {
      // 数据类型选中为视频时,标注类型自动切换为目标跟踪,同时清除不符合类型的标签
      if (value === 1) {
        this.form.annotateType = 5;
        this.handleAnnotateTypeChange(5);
      } else {
        this.form.annotateType = undefined;
      }
    },
    handleAnnotateTypeChange(value) {
      // 图像分类(2)可以选中预置标签Coco和ImageNet, 目标检测(1)可以选中预置标签中的Coco
      // 更改标注类型会清除不符合条件的标签
      if (![1, 2].includes(value)) {
        this.form.labels = [];
        this.chosenPresetLabelId = null;
      } else if (value === 1 && this.chosenPresetLabelId === '2') {
        this.chosenPresetLabelId = null;
      }
    },
    // 查询数据集详情
    async queryDatasetDetail(datasetId) {
      const res = await detail(datasetId);
      return res;
    },
    download(row) {
      return downloadZipFromObjectPath(`dataset/${row.id}/origin`);
    },
    formatDate(row, column, cellValue) {
      return formatDateTime(cellValue);
    },
    copy() {
      for (const key in this.currentRow) {
        this.form[key] = this.currentRow[key];
      }
      this.form.id = null;
      this.form.createTime = null;
      this.crud.toAdd();
    },
    handleCurrentChange(row) {
      this.currentRow = JSON.parse(JSON.stringify(row));
    },
    removeLabelTag(tag) {
      const array = this.form.labels;
      this.form.labels = array.filter((value) => {
        return value.name !== tag;
      });
      this.customLabel = this.customLabel.filter((value) => {
        return value.name !== tag;
      });
      this.systemLabel.forEach((item) => {
        if (item.name === tag) {
          item.chosen = false;
        }
      });
    },
    handleLabelHide() {
      this.actionKey += 1;
    },
    refreshLabel() {
      const labels = [];
      this.customLabel.forEach((item) => {
        if (item.chosen) {
          labels.push({
            name: item.name,
            color: item.color,
          });
        }
      });
      this.systemLabel.forEach((item) => {
        if (item.chosen) {
          labels.push({
            id: item.id,
            name: item.name,
            color: item.color,
          });
        }
      });
      this.form.labels = labels;
    },

    // 添加预置标签
    setPresetLabel(labelId) {
      this.chosenPresetLabelId = labelId;
    },
    // 添加其他标签
    setNoPresetLabel() {
      this.chosenPresetLabelId = null;
      this.refreshLabel();
    },
    datasetPrevStep() {
      this.activeStep-=1;
      if (this.activeStep < 0) {
        this.activeStep = 0;
      }
    },
    datasetNextStep() {
      // 跳过上传
      this.skipUpload = true;
      this.activeStep += 2;
      this.createDatasetStatus = '完成';
      if (this.activeStep > 3) {
        this.activeStep = 0;
      }
    },
    resetChosenLabel() {
      const resetSystemLabels = this.systemLabel.map(d => ({
        ...d,
        chosen: false,
      }));

      const resetCustomLabels = this.customLabel.map(d => ({
        ...d,
        chosen: false,
      }));

      this.systemLabel = resetSystemLabels;
      this.customLabel = resetCustomLabels;
    },
    closeDatasetDialog() {
      // 关闭窗口时,若当前处于第1步以后,重置查询条件刷新,避免限制条件下看不到新建的数据集
      if (this.activeStep > 0) {
        this.onResetQuery();
        this.crud.toQuery();
      }
      // 清理第一步表单
      this.$refs.form?.resetFields();
      this.setNoPresetLabel();
      // 清理上传表单
      this.$refs.initFileUploadForm?.$refs?.formRef.reset();
      this.crud.cancelCU();
      this.crud.status.add = CRUD.STATUS.NORMAL;
      this.chosenDatasetId = 0;
      this.activeStep = 0;
      // 清除标签
      this.resetChosenLabel();
      // 重置帧数
      this.step1Form.frameInterval = defaultFrameInterval;
      this.resetUpload();
      this.dialogKey += 1;
    },
    resetUpload() {
      // 重置上传状态
      this.uploadStatus = undefined;
      this.videoUploadProgress = 0;
    },
    createDataset() {
      if (this.activeStep === 0) {
        this.crud.findVM('form').$refs.form.validate(valid => {
          if (!valid) {
            return;
          }
          this.crud.status.add = CRUD.STATUS.PROCESSING;
          // 最终发送请求前，判断使用预置标签时,才在此处调整请求参数
          if (this.chosenPresetLabelId) {
            this.crud.form.presetLabelType = this.chosenPresetLabelId;
            this.crud.form.labels = [];
          }
          this.crud.crudMethod
            .add(this.crud.form)
            .then(res => {
              this.chosenDatasetId = res;
              this.activeStep = 1;
            })
            .catch(err => {
              this.$message({
                message: err.message || '数据集创建失败',
                type: 'exception',
              });
              this.crud.status.add = CRUD.STATUS.PREPARED;
            });
        });
      }
    },
    completeCreateDataset() {
      this.chosenDatasetId = 0;
      this.crud.status.add = CRUD.STATUS.NORMAL;
      this.$message({
        message: '数据集创建成功',
        type: 'success',
      });
      this.crud.resetForm();
      this.crud.resetQuery();
    },
    showUploadDatasetForm() {
      this.closeUploadHint();
      this.uploadDatasetFormVisible = true;
    },
    closeUploadDatasetForm() {
      this.uploadDatasetFormVisible = false;
      this.crud.toQuery();
    },
    showUploadHint() {
      this.uploadHintVisible = true;
    },
    closeUploadHint() {
      this.uploadHintVisible = false;
    },
    handleEditDatasetName(name, row) {
      const editForm = {
        id: row.id,
        name,
      };
      editDataset(editForm).then(() => {
        this.crud.status.edit = CRUD.STATUS.NORMAL;
        this.$set(row, 'name', editForm.name);
        if (row.pollIng) {
          this.poll(row, pollMap[row.status]);
        }
      });
    },
    isValidDetail(row) {
      // 未采样(5) 采样中(7) 自动标注中(2)不能进行查看标注 此外，类型为视频时，自动标注完成(3)也不可查看(此时下游会进行目标跟踪)
      if (row.dataType === 1 && row.status === 3) {
        return false;
      }
      return ![2, 5, 7, 8].includes(row.status);
    },
    // 开始标注
    async goDetail(row) {
      // 自定义数据集无法查看详情
      if(isImport(row)) return false;
      const datasetInfo = await this.queryDatasetDetail(row.id);
      if (!this.isValidDetail(datasetInfo)) {
        return Message.error('数据集当前状态不能进行查看');
      }
      this.$router.push({
        path: `/data/datasets/${
          annotationMap[row.annotateType].urlPrefix
        }/${row.id}`,
      });
      return null;
    },

    // 历史版本
    gotoVersion(row) {
      this.$router.push({
        path: `/data/datasets/${row.id}/version`,
      });
    },
    autoAnnotate(row) {
      this.$set(row, 'pollIng', true); // 新增响应式变量，并设置禁用操作台按钮
      return autoAnnotate([row.id])
        .then(() => {
          this.$message({
            message: '自动标注任务开始',
            type: 'success',
          });
          // 启动自动标注轮询
          this.poll(row, 'autoAnnotate');
        })
        .catch(e => {
          row.pollIng = false;
          this.$message({
            message: e.message || '自动标注任务失败',
            type: 'error',
          });
        });
    },
    annotateFilter2Type(filter) {
      if (filter === 'all') return null;
      return filter;
    },
    filterByAnnotateType(command) {
      if (command === this.annotateFilter) {
        return;
      }
      this.annotateFilter = command;
      this.crud.params.annotateType = this.annotateFilter2Type(command);
      this.crud.page.current = 1;
      this.crud.refresh();
    },
    dataFilter2Type(filter) {
      if (filter === 'all') return null;
      return filter;
    },
    filterByDataType(command) {
      if (command === this.dataTypeFilter) {
        return;
      }
      this.dataTypeFilter = command;
      this.crud.params.dataType = this.dataFilter2Type(command);
      this.crud.page.current = 1;
      this.crud.refresh();
    },
    datasetStatusFilter2Type(filter) {
      if (filter === 'all') return null;
      return filter;
    },
    filterByDatasetStatus(command) {
      if (command === this.datasetStatusFilter) {
        return;
      }
      this.datasetStatusFilter = command;
      this.crud.params.status = this.datasetStatusFilter2Type(command);
      this.crud.page.current = 1;
      this.crud.refresh();
    },
    async openUploadDialog(row) {
      await this.queryDatasetDetail(row.id);
      // todo: 当数据集为视频，只允许传递一个文件
      // 当前选中 row
      this.importRow = row;
      this.chosenDatasetId = row.id;
      this.chosenDatasetStatus = row.status;
      this.uploadDialogVisible = true;
    },
    toggleUploadFormClose() {
      // 取消当前选中 row
      this.importRow = null;
      // 重置帧数
      this.importForm.frameInterval = defaultFrameInterval;
      this.uploadDialogVisible = false;
    },

    uploadSubmit(formName) {
      this.$refs[formName].uploadSubmit((resolved, total) => {
        // eslint-disable-next-line func-names
        this.$nextTick(function() {
          this.uploadPercent =
            this.uploadPercent > 100 ? 100 : toFixed(resolved / total);
        });
      });

      if (this.crud.status.cu > 0) {
        this.createDatasetStatus = '上传中';
        this.activeStep = 2;
      }
    },

    // 将文件上传和视频上传统一
    async uploader(datasetId, files) {
      const datasetInfo = await this.queryDatasetDetail(datasetId);
      // 点击导入操作
      const { dataType } = datasetInfo || {};
      // 文件上传
      if (dataType === 0) {
        return submit(datasetId, files);
      } if (dataType === 1) {
        // 根据是否通过点击导入按钮来区分 frameInterval 来源
        const frameInterval = this.importRow
          ? this.importForm.frameInterval
          : this.step1Form.frameInterval;
        return submitVideo(datasetId, {
          frameInterval,
          url: files[0].url,
        });
      }
      return Promise.reject();
    },

    uploadSuccess(res) {
      if (this.crud.status.cu > 0) {
        this.createDatasetStatus = '完成';
        this.activeStep+=1;
      }
      // 视频上传完毕
      if (this.form.dataType === 1) {
        this.videoUploadProgress = 100;
      }
      const files = getImgFromMinIO(res);
      // 自动标注完成时 导入 提示信息不同
      const successMessage = [0, 1].includes(this.chosenDatasetStatus)
        ? '上传文件成功' : '上传文件成功，若数据集状态未及时更新，请手动刷新页面';
      if (files.length > 0) {
        this.uploader(this.chosenDatasetId, files).then(() => {
          this.$message({
            message: successMessage,
            duration: 5000,
            type: 'success',
          });
        });
      }
      this.uploadStatus = 'success';
    },
    uploadError() {
      if (this.crud.status.cu > 0) {
        this.createDatasetStatus = '完成';
      }
      this.uploadStatus = 'exception';
      this.$message({
        message: '上传文件失败',
        type: 'error',
      });
    },
    cancelUpload(formName) {
      this.$refs[formName]?.$refs?.formRef?.cancelUpload();
      this.uploadStatus = 'warning';
      this.chosenDatasetId = 0;
      if (formName === 'initFileUploadForm') {
        this.crud.status.add = CRUD.STATUS.NORMAL;
        this.crud.resetForm();
        this.crud.toQuery();
      } else {
        this.uploadDialogVisible = false;
      }
    },
    getProgress(row) {
      const { progress } = row;
      // 采样中模拟一个假的进度条
      if ([7, 8].includes(row.status)) {
        return Math.min(Math.floor(row.sample_progress), 99);
      }
      if (row.status === 0 && row.sample_progress > 0) {
        return 100;
      }
      const allFinished =
        progress.finished + progress.autoFinished + progress.finishAutoTrack;
      // 兼容 0
      if (allFinished === 0) return 0;
      return toFixed(allFinished / (allFinished + progress.unfinished), 2, 0);
    },
    parseDataType(row, column, cellValue = 0) {
      if(row.import) return "自定义";
      return dataTypeMap[cellValue];
    },
    parseAnnotateType(row, column, cellValue) {
      if(row.import) return "自定义";
      return (annotationMap[cellValue] || {}).name || '';
    },
    parseStatus(row, column, cellValue = 0) {
      return datasetStatusMap[cellValue].name;
    },
    // 轮询状态
    poll(row, type) {
      // 给采样一个递增值
      if ([7, 8].includes(row.status)) {
        row.sample_increment = 9;
      }
      return this.setTime(row, 0, { type });
    },
    setTime(row, times, { type }) {
      // 轮询事件超过 5 min，失败
      if (times > 100) {
        this.autoTimer[row.id] && clearTimeout(this.autoTimer[row.id]);
        row.pollIng = false;
        return Promise.reject(new Error('更新数据集状态超时'));
      }

      return this.datasetPoll(row, type).then(() => {
        if (row.pollIng) {
          this.autoTimer[row.id] && clearTimeout(this.autoTimer[row.id]);
          this.autoTimer[row.id] = setTimeout(() => {
            this.setTime(row, times + 1, { type });
          }, 3000);
        }
      });
    },
    // 展示解压状态
    showUnZip(d) {
      return d.import && [0, 1, 3].includes(d.decompressState);
    },
    // 数据集轮询进度
    datasetPoll(row, type) {
      // 解压缩
      if (type === 'unzip'){
        return this.queryDatasetDetail(row.id).then(res => {
          // 解压完成
          if (!isUnzip(res)) {
          // 采样视频进入下一个阶段
            Object.assign(row, res, {
              pollIng: false,
            });
            delete row.pollType;
          }
          return Promise.resolve(row);
        });
      }
      if (type === 'sample' || type === 'enhance') {
        const __originStatus = Number(invert(pollMap)[type]);
        // 采样中
        return this.queryDatasetDetail(row.id).then(res => {
          if (res.status !== __originStatus) {
          // 采样视频进入下一个阶段
            row.pollIng = false;
            row.progress = res.progress; // 更新最新自动标注进度
            row.status = res.status; // 更新标注状态
            delete row.sample_progress;
          } else {
            row.sample_progress = (row.sample_progress || 10) + row.sample_increment;
            row.sample_increment *= 0.9;
          }
          return Promise.resolve(row);
        });
      } 
      if (type === 'autoAnnotate') {
        // 自动标注中
        return annotateStatus(row.id).then(res => {
          if (!isNil(res.progress)) {
            row.pollIng = true;
            row.progress = res.progress; // 更新最新自动标注进度
            row.status = res.status; // 更新标注状态
            // 自动标注完成
            if (res.status !== 2) {
              row.pollIng = false;
            }
            return Promise.resolve(row);
          }
          return null;
        });
      }
      // 异常兼容
      return Promise.reject(new Error(`数据集 ${row.id} 查询错误`));
    },
    // 数据集状态为自动标注中(2)采样中(7)数据增强中(8)不能被选中, 视频会自动进行目标跟踪,在自动标注完成(3)时也不能选中删除
    canSelect(row) {
      return !(row.pollIng || [2, 7, 8].includes(row.status) || ((row.status === 3) && (row.dataType === 1)));
    },
    showActionModal(row, type) {
      this.actionModal = {
        show: true,
        row,
        showOkLoading: false,
        type,
      };
    },
    showPublish(row) {
      this.showActionModal(row, 'publish');
    },
    showDataEnhance(row) {
      this.showActionModal(row, 'dataEnhance');
    },
    resetActionModal() {
      this.actionModal = {
        show: false,
        row: undefined,
        showOkLoading: false,
        type: null,
      };
      this.enhanceKey += 1;
    },
    handleCancel() {
      this.resetActionModal();
    },
    handlePublish() {
      const publishForm = this.$refs.publishForm.$refs.form;
      publishForm.validate(async valid => {
        if (valid) {
          const { model } = this.$refs.publishForm.state;
          Object.assign(this.actionModal, {
            showOkLoading: true,
          });
          await publish({
            datasetId: model.id,
            versionNote: model.versionNote || '',
          });
          // 重置 actionModal
          this.resetActionModal();
          this.crud.toQuery();
          return false;
        }
        return null;
      });
    },
    reAnnotation(row) {
      // 标注文件量大时，清除标注耗时较久，置灰操作防止多次点击
      this.$set(row, 'disabledAction', true);
      return delAnnotation(row.id)
        .then(() => {
          this.autoAnnotate(row);
          this.$set(row, 'disabledAction', false);
        });
    },
    handleDataEnhance(model, row) {
      Object.assign(this.actionModal, {
        showOkLoading: true,
      });
      return postDataEnhance(model.id, model.types).then(() => {
        // 启动数据集增强
        this.$set(row, 'status', 8);
        this.$set(row, 'pollIng', true);
        this.poll(row, 'enhance');
      }).finally(() => {
        // 重置 actionModal
        this.resetActionModal();
      });
    },
  },
};
</script>

<style lang='scss'>
.create-dataset {
  .label-input .el-tag {
    margin-left: 4px;
  }
}

.tt-wrapper.progress-tip {
  .tooltip-item-label {
    min-width: 100px;
  }
}

.dataset-name-col {
  .cell {
    text-overflow: unset;
  }

  .name-col {
    max-width: 90%;

    span {
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

.el-progress {
  display: block;
}

.el-progress-circle {
  margin: 0 auto;
}

.progress-wrap {
  .el-icon-loading + span {
    display: block;
    flex: 1;
    margin-left: 4px;
  }
}

.upload-progress {
  width: 200px;
  margin: 40px auto 0;

  .el-progress-bar {
    padding-right: 70px;
    margin-right: -75px;
  }

  .el-progress-bar__inner {
    background:
      -webkit-repeating-linear-gradient(
        -30deg,
        #83a7cf 0,
        #83a7cf 10px,
        #93b3d6 10px,
        #93b3d6 20px
      );
    animation: process 5s linear infinite;
  }

  @keyframes process {
    0% {
      background-position: 0 0;
    }

    100% {
      background-position: 180px 0;
    }
  }
}

.decompress-progress {
  width: 100%;
  margin: auto 0;
  display: inline-block;

  .el-progress-bar__inner {
    background:
      -webkit-repeating-linear-gradient(
        -30deg,
        #83a7cf 0,
        #83a7cf 10px,
        #93b3d6 10px,
        #93b3d6 20px
      );
    animation: process 5s linear infinite;
  }

  @keyframes process {
    0% {
      background-position: 0 0;
    }

    100% {
      background-position: 180px 0;
    }
  }
}

.reannotate-popconfirm {
  .el-popconfirm__main {
    align-items: baseline;
  }
}
</style>

<style scoped lang='scss'>
.annotate-li::before {
  color: #ff9943;
  content: "• ";
}

.auto-annotate-li::before {
  color: #5693f5;
  content: "• ";
}

.label-input {
  height: max-content;
  border-color: #b4bccc;
  border-style: solid;
  border-width: 1px;
  border-radius: 5px;
}

.upload-dialog {
  border-radius: 4px;
}
</style>
