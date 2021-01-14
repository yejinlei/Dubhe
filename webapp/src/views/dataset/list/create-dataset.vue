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
  <el-dialog
    append-to-body
    custom-class="create-dataset"
    center
    :close-on-click-modal="false"
    :visible="visible"
    title="创建数据集"
    width="610px"
    @close="closeDialog"
  >
    <!--步骤条-->
    <el-steps :active="activeStep" finish-status="success">
      <el-step title="新建数据集" />
      <el-step title="导入数据" />
      <el-step title="完成" />
    </el-steps>
    <!--step0新建数据集-->
    <div v-if="activeStep === 0">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
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
            @change="handleAnnotateTypeChange"
          />
        </el-form-item>
        <el-form-item label="标签组" style="height: 32px;">
          <el-cascader
            v-model="chosenGroup"
            clearable
            placeholder="标签组"
            :options="labelGroupOptions"
            :props="{expandTrigger: 'hover'}"
            :show-all-levels="false"
            filterable
            popper-class="group-cascader"
            style="width: 100%; line-height: 32px;"
            @change="handleGroupChange"
            @focus="handleGroupFocus"
          >
            <div slot="empty">
              <span>没有找到标签组？去</span>
              <a
                target="_blank"
                type="primary"
                :underline="false"
                class="primary"
                :href="`/data/labelgroup/create`"
              >
                新建标签组
              </a>
              <span>页面创建</span>
            </div>
          </el-cascader>
          <div style="position: relative; top: -33px; right: 30px; float: right;">
            <el-link
              v-if="chosenGroupId !== null"
              target="_blank"
              type="primary"
              :underline="false"
              class="vm"
              :href="`/data/labelgroup/detail?id=${chosenGroupId}`"
            >
              查看详情
            </el-link>
          </div>
        </el-form-item>
        <div v-if="chosenGroupId === null" style=" position: relative; top: -12px; left: 116px;">
          <span>标签组需要在</span>
          <a
            target="_blank"
            type="primary"
            :underline="false"
            class="primary"
            :href="`/data/labelgroup/create`"
          >
            新建标签组
          </a>
          <span>页面创建</span>
        </div>
        <el-form-item label="数据集描述" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            placeholder="数据集描述长度不能超过100字"
            maxlength="100"
            rows="3"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <div style=" margin-top: 25px; text-align: center;">
        <el-button
          :loading="crud.status.cu === 2"
          type="primary"
          @click="createDataset"
        >
          下一步
        </el-button>
      </div>
    </div>
    <!--TODO: v-if not work，why?-->
    <!--step1上传文件-->
    <div v-show="activeStep === 1">
      <upload-inline
        :key="form.dataType"
        ref="initFileUploadForm"
        action="fakeApi"
        :params="uploadParams"
        v-bind="optionCreateProps"
        @uploadSuccess="uploadSuccess"
        @uploadError="uploadError"
      />
      <!--上传视频时显示帧间隔设置-->
      <el-form
        v-if="form.dataType === dataTypeCodeMap.VIDEO"
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
      <div style=" margin-top: 25px; text-align: center;">
        <el-button @click="skip">跳过</el-button>
        <el-button type="primary" @click="uploadSubmit('initFileUploadForm')">确定上传</el-button>
      </div>
    </div>
    <!--step2上传中-->
    <div v-if="activeStep === 2 && skipUpload !== true">
      <!--上传图片进度条-->
      <el-progress
        v-if="form.dataType !== dataTypeCodeMap.VIDEO"
        type="circle"
        :percentage="uploadPercent"
        :status="uploadStatus"
        :format="formatProgress"
      />
      <!--上传视频进度条-->
      <div v-else class="circleProgressWrapper">
        <div class="circleText">正在上传</div>
        <div class="wrapper right">
          <div class="circleProgress rightCircle"></div>
        </div>
        <div class="wrapper left">
          <div class="circleProgress leftCircle"></div>
        </div>
      </div>
      <div style=" margin-top: 25px; text-align: center;">
        <el-button type="primary" :loading="true">确定</el-button>
      </div>
    </div>
    <!--step3上传完成-->
    <div v-if="activeStep === 3">
      <el-progress v-if="skipUpload !== true" type="circle" :percentage="100" :status="uploadStatus"/>
      <div style=" margin-top: 25px; text-align: center;">
        <el-button type="primary" :loading="!uploadFinished" @click="completeCreate">确定</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import CRUD, { presenter, header, form, crud } from '@crud/crud';
import crudDataset, { detail } from '@/api/preparation/dataset';
import { submit, submitVideo } from '@/api/preparation/datafile';
import { getLabelGroupList } from '@/api/preparation/labelGroup';
import {
  getImgFromMinIO,
  getTextFromMinIO,
  annotationMap,
  annotationCodeMap,
  dataTypeMap,
  dataTypeCodeMap,
  dataTypeAnnotateTypeMap,
  withDimensionFile,
  trackUploadProps,
} from '@/views/dataset/util';
import { validateName } from '@/utils/validate';
import UploadInline from '@/components/UploadForm/inline';
import InfoSelect from '@/components/InfoSelect';
import { toFixed } from '@/utils';
import { isNil } from 'lodash';

// 默认帧间隔
const defaultFrameInterval = 5;
// 默认表单
const defaultForm = {
  id: null,
  name: null,
  dataType: null,
  annotateType: null,
  labelGroupId: null,
  presetLabelType: '',
  remark: '',
  type: 0,
};

export default {
  name: "CreateDataset",
  components: {
    UploadInline,
    InfoSelect,
  },
  cruds() {
    return CRUD({
      title: '数据集管理',
      crudMethod: { ...crudDataset },
      props: { optText: { add: '创建数据集' }},
      queryOnPresenterCreated: false,
    });
  },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    closeCreateDatasetForm: {
      type: Function,
    },
    onResetFresh: {
      type: Function,
    },
  },
  data() {
    return {
      dataTypeCodeMap,
      chosenDatasetId: 0, // 当前数据集id
      activeStep: 0,  // 当前的step
      uploadPercent: 0,
      uploadStatus: undefined,
      skipUpload: false, // 跳过上传
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
      },
      step1Form: {
        frameInterval: defaultFrameInterval, // 默认值
      },
      chosenGroupId: null,
      chosenGroup: null,
      labelGroupOptions: [{
        value: 'custom',
        label: '自定义标签组',
        disabled: false,
        children: [],
      },
      {
        value: 'system',
        label: '预置标签组',
        disabled: false,
        children: [],
      }],
    };
  },
  computed: {
    // 文件上传前携带尺寸信息
    withDimensionFile() {
      return withDimensionFile;
    },
    isText() {
      return this.form.dataType === dataTypeCodeMap.TEXT;
    },
    uploadParams() {
      // 是否为视频数据类类型
      const isVideo =
        this.importRow?.dataType === dataTypeCodeMap.VIDEO || this.form.dataType === dataTypeCodeMap.VIDEO;
      const dir = isVideo ? `video` : `origin`;
      return {
        datasetId: this.chosenDatasetId,
        objectPath: `dataset/${this.chosenDatasetId}/${dir}`, // 对象存储路径
      };
    },
    // 新建数据集上传组件参数拼装
    optionCreateProps() {
      const textProps = {
        accept: ".txt",
        dataType: "text",
      };
      const baseImageProps = {
        transformFile: this.withDimensionFile,
      };
      let props = {};
      switch( this.form.dataType){
        case dataTypeCodeMap.VIDEO:
          props = { ...baseImageProps, ...trackUploadProps };
          break;
        case dataTypeCodeMap.IMAGE:
          props = baseImageProps;
          break;
        case dataTypeCodeMap.TEXT:
          props = textProps;
          break;
        default:
          props = {};
      }
      return props;
    },
    annotationList() {
      // 原始标注列表
      const rawAnnotationList = Object.keys(annotationMap).map(d => ({
        label: annotationMap[d].name,
        value: Number(d),
      }));
      // 图片，可用图像分类和目标检测；视频，可用目标跟踪；文本，可用文本分类
      return rawAnnotationList.filter(d => {
        if (this.form.dataType === dataTypeCodeMap.IMAGE) {
          return [annotationCodeMap.ANNOTATE, annotationCodeMap.CLASSIFY].includes(d.value);
        }
        if (this.form.dataType === dataTypeCodeMap.VIDEO) {
          return d.value === annotationCodeMap.TRACK;
        }
        if (this.form.dataType === dataTypeCodeMap.TEXT) {
          return d.value === annotationCodeMap.TEXTCLASSIFY;
        }
        return true;
      });
    },

    dataTypeList: () =>
      Object.keys(dataTypeMap).map(d => ({
        label: dataTypeMap[d],
        value: Number(d),
      })),

    uploadFinished() {
      if(this.skipUpload) return true;
      return this.uploadStatus && ['success', 'exception'].includes(this.uploadStatus);
    },
  },
  methods: {
    handleGroupFocus() {
      // 在未选择数据类型和标注类型之前，不对可用的标签组进行查询
      if(!isNil(this.form.dataType) && this.form.dataType !== '' && !isNil(this.form.annotateType) && this.form.annotateType !== ''){
        // 在数据类型和标注类型未改动的情况下 避免对可用的标签组列表进行重复查询
        if((this.labelGroupOptions[1].children).length === 0){
          getLabelGroupList({type: 1, dataType: this.form.dataType, annotateType: this.form.annotateType}).then(res => {
            res.forEach((item) => {
              this.labelGroupOptions[1].children.push({
                value: item.id,
                label: item.name,
                disabled: false,
              });
            });
          });
        }
        if((this.labelGroupOptions[0].children).length === 0){
          getLabelGroupList({type: 0, dataType: this.form.dataType, annotateType: this.form.annotateType}).then(res => {
            res.forEach((item) => {
              this.labelGroupOptions[0].children.push({
                value: item.id,
                label: item.name,
                disabled: false,
              });
            });
          });
        }
      }
    },

    handleGroupChange(val){
      if(val.length === 0) {
        this.chosenGroup = null;
        this.chosenGroupId = null;
      } else {
        this.chosenGroup = val;
        // eslint-disable-next-line prefer-destructuring
        this.chosenGroupId = val[1];
      }
    },

    // 重置创建数据集表单
    resetCreateDatasetForm() {
      // 清理第一步表单
      this.$refs.form?.resetFields();
      // 清除标签组
      this.chosenGroup = null;
      this.chosenGroupId = null;
      // 清理上传表单
      this.$refs.initFileUploadForm?.$refs?.formRef.reset();
      this.crud.cancelCU();
      this.crud.status.add = CRUD.STATUS.NORMAL;
      this.chosenDatasetId = 0;
      this.activeStep = 0;
      // 重置帧数
      this.step1Form.frameInterval = defaultFrameInterval;
      this.skipUpload = false;
      this.uploadStatus = undefined;
      this.uploadPercent = 0;
      this.videoUploadProgress = 0;
    },
    // 清空标签组可选列表 清除选中的标签组
    clearLabelGroupItem() {
      this.labelGroupOptions[0].children = [];
      this.labelGroupOptions[1].children = [];
      this.chosenGroupId = null;
      this.chosenGroup = null;
    },
    // step0 改变数据类型
    handleDataTypeChange(dataType) {
      this.clearLabelGroupItem();
      this.form.annotateType = dataTypeAnnotateTypeMap.get(dataType);
    },
    // step0 改变标注类型
    handleAnnotateTypeChange() {
      this.clearLabelGroupItem();
    },
    // step0 创建数据集调用
    createDataset() {
      if (this.activeStep === 0) {
        this.crud.findVM('form').$refs.form.validate(valid => {
          if (!valid) {
            return;
          }
          this.crud.status.add = CRUD.STATUS.PROCESSING;
          this.crud.form.labelGroupId = this.chosenGroupId;
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

    // step1 上传前需要查询数据集详情
    async queryDatasetDetail(datasetId) {
      const res = await detail(datasetId);
      return res;
    },
    // step1 上传包括图片和视频
    async uploader(datasetId, files) {
      const datasetInfo = await this.queryDatasetDetail(datasetId);
      // 点击导入操作
      const { dataType } = datasetInfo || {};
      // 文件上传
      if (dataType === dataTypeCodeMap.IMAGE) {
        return submit(datasetId, files);
      }
      if (dataType === dataTypeCodeMap.VIDEO) {
        return submitVideo(datasetId, {
          frameInterval: this.step1Form.frameInterval,
          url: files[0].url,
        });
      }
      if (dataType === dataTypeCodeMap.TEXT) {
        return submit(datasetId, files);
      }
      return Promise.reject();
    },
    // step1 上传成功
    uploadSuccess(res) {
      if (this.crud.status.cu > 0) {
        this.activeStep+=1;
      }
      // 视频上传完毕
      if (this.form.dataType === dataTypeCodeMap.VIDEO) {
        this.videoUploadProgress = 100;
      }

      let files;
      if (this.form.dataType === dataTypeCodeMap.TEXT) {
        files = getTextFromMinIO(res);
      } else {
        files = getImgFromMinIO(res);
      }

      // 自动标注完成时 导入 提示信息不同
      const successMessage = '上传文件成功';
      if (files.length > 0) {
        this.uploader(this.chosenDatasetId, files).then(() => {
          this.$message({
            message: successMessage,
            duration: 5000,
            type: 'success',
          });
          this.uploadStatus = 'success';
        });
      }
    },
    // step1 上传失败
    uploadError(err) {
      this.$message({
        message: err.message || '上传文件失败',
        type: 'error',
      });
    },
    // step1 跳过上传
    skip() {
      this.skipUpload = true;
      this.activeStep += 2;
    },
    // step1 确定上传
    uploadSubmit(formName) {
      this.$refs[formName].uploadSubmit((resolved, total) => {
         if (this.crud.status.cu > 0) {
            this.activeStep = 2;
          }
        // eslint-disable-next-line func-names
        this.$nextTick(function() {
          this.uploadPercent =
            this.uploadPercent > 100 ? 100 : toFixed(resolved / total);
        });
      });
    },

    // step2 进度格式化
    formatProgress(percentage) {
      let formatTxt = `${percentage}%`;
      if (this.form.dataType === dataTypeCodeMap.VIDEO) {
        formatTxt = this.videoUploadProgress === 100 ? `100%` : `上传中...`;
      }
      return formatTxt;
    },
    // step2 完成时点击确定
    completeCreate() {
      // 发送创建成功消息
      this.$message({
        message: '数据集创建成功',
        type: 'success',
      });
      // 关闭创建数据集对话框
      this.closeCreateDatasetForm();
      this.onResetFresh();
      // 重置创建数据集各个步骤的表单
      this.resetCreateDatasetForm();
    },

    // 关闭显示的创建数据集对话框
    closeDialog() {
      if(this.activeStep === 0){
      // step=0还未创建数据集时不需要刷新列表
        this.closeCreateDatasetForm();
        this.resetCreateDatasetForm();
      } else{
      // step>0数据集创建成功
        this.completeCreate();
      }
    },
  },
};
</script>
