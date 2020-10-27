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
            :disabled="form.dataType === 1"
            @change="handleAnnotateTypeChange"
          />
        </el-form-item>
        <el-form-item label="标签组" prop="labelGroupId">
          <div class="label-input">
            <el-popover
              ref="popover"
              v-model="popoverVisible"
              placement="top"
              trigger="click"
              popper-class="label-group-popover"
            >
              <div class="add-label-tag">
                <el-tabs v-model="labelGroupTab" type="border-card">
                  <el-tab-pane label="自定义标签组" name="custom">
                    <el-select 
                      v-model="customLabelGroupId" 
                      filterable 
                      placeholder="请选择" 
                      popper-class="label-group-select"
                      @change="handleCustomId"
                    >
                      <el-option
                        v-for="item in customLabelGroups"
                        :key="item.labelGroupId"
                        :label="item.name"
                        :value="item.labelGroupId"
                      >
                      </el-option>
                    </el-select>
                  </el-tab-pane>
                  <el-tab-pane label="预置标签组" name="system" :disabled="!systemLabelEnabled">
                    <el-select 
                      v-model="systemLabelGroupId" 
                      filterable 
                      placeholder="请选择" 
                      @change="handleSystemId"
                    >
                      <el-option
                        v-for="item in systemLabelGroups"
                        :key="item.labelGroupId"
                        :label="item.name"
                        :value="item.labelGroupId"
                        :disabled="!optionEnabled(item.labelGroupId, form.annotateType)"
                      >
                      </el-option>
                    </el-select>
                  </el-tab-pane>
                </el-tabs>
              </div>
              <el-button slot="reference" type="text">
                &nbsp;
                <span v-if="labelGroupId === null">&nbsp;&nbsp;标签组</span>
                <el-tag v-else closable @close="handleRemoveLabelGroup()">
                  {{labelGroupName}}                
                </el-tag>
              </el-button>
            </el-popover>
            <el-link 
              v-if="labelGroupId !== null" 
              target="_blank" 
              type="primary" 
              :underline="false" 
              class="vm" 
              :href="`/data/labelgroup/detail?id=${labelGroupId}`"
              style="float: right; margin-right: 8px;"  
            >
              查看详情
            </el-link>
          </div>
        </el-form-item>
        <div v-if="labelGroupId === null" style=" position: relative; top: -12px; left: 118px;">
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
    <!--step1上传文件-->
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
      <!--上传视频时显示帧间隔设置-->
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
      <div style=" margin-top: 25px; text-align: center;">
        <el-button @click="skip">跳过</el-button>
        <el-button type="primary" @click="uploadSubmit('initFileUploadForm')">确定上传</el-button>
      </div>
    </div>
    <!--step2上传中-->
    <div v-if="activeStep === 2 && skipUpload !== true">
      <!--上传图片进度条-->
      <el-progress
        v-if="form.dataType !== 1"
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
  annotationMap,
  dataTypeMap,
  withDimensionFile,
  trackUploadProps,
} from '@/views/dataset/util';
import { validateName } from '@/utils/validate';
import UploadInline from '@/components/UploadForm/inline';
import InfoSelect from '@/components/InfoSelect';
import { toFixed } from '@/utils';

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
      chosenDatasetId: 0, // 当前数据集id
      activeStep: 0,  // 当前的step
      actionKey: 1,
      // customLabelEnabled: true, // 自定义标签组可用性
      systemLabelEnabled: true, // 预置标签组可用性
      uploadPercent: 0,
      uploadStatus: undefined,
      skipUpload: false, // 跳过上传
      popoverVisible: false,
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
      labelGroupTab: "custom",
      labelGroupName: null,
      labelGroupId: null,
      customLabelGroupId: null,
      systemLabelGroupId: null,
      customLabelGroups: [],
      systemLabelGroups: [],
    };
  },
  computed: {
    // 文件上传前携带尺寸信息
    withDimensionFile() {
      return withDimensionFile;
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
    // 新建数据集（视频）上传组件参数
    optionCreateProps() {
      const props = this.form.dataType === 1 ? trackUploadProps : {};
      return props;
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
  created() {
    this.crud.toQuery();
    getLabelGroupList(1).then(res => {
      res.forEach((item) => {
        this.systemLabelGroups.push({
          labelGroupId: item.id,
          name: item.name,
        });
      });
    });    
    getLabelGroupList(0).then(res => {
      res.forEach((item) => {
        this.customLabelGroups.push({
          labelGroupId: item.id,
          name: item.name,
        });
      });
    });
  },
  methods: {
    handleCustomId() {
      this.popoverVisible = false;
      this.labelGroupId = this.customLabelGroupId;
      this.systemLabelGroupId = null;
      this.labelGroupName = this.customLabelGroups.find(d => d.labelGroupId === this.labelGroupId).name;
    },
    handleSystemId() {
      this.popoverVisible = false;
      this.labelGroupId = this.systemLabelGroupId;
      this.customLabelGroupId = null;
      this.labelGroupName = this.systemLabelGroups.find(d => d.labelGroupId === this.labelGroupId).name;
    },
    handleRemoveLabelGroup() {
      this.labelGroupId = null;
      this.customLabelGroupId = null;
      this.systemLabelGroupId = null;
      this.$refs.popover.doClose();
    },

    optionEnabled(labelGroupId, annotateType) {
      // 目标检测(1)目标跟踪(5)可以使用预置标签组COCO
      if([1, 5].includes(annotateType)) {
        return labelGroupId === 1;
      } 
      return true;
    },

    // 重置创建数据集表单
    resetCreateDatasetForm() {
      // 清理第一步表单
      this.$refs.form?.resetFields();
      // 清除标签组
      this.labelGroupId = null;
      this.systemLabelEnabled = true;
      this.systemLabelGroupId = null;
      this.customLabelGroupId = null;
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

    // step0 标签选择框刷新
    handleLabelHide() {
      this.actionKey += 1;
    },
    // step0 改变数据类型
    handleDataTypeChange(dataType) {
      // 数据类型选中为视频时,标注类型自动切换为目标跟踪,同时清除不符合类型的标签组
      if (dataType === 1) {
        this.form.annotateType = 5;
        this.handleAnnotateTypeChange(5);
      } else {
        this.form.annotateType = undefined;
        this.systemLabelEnabled = true;
      }
    },
    // step0 改变标注类型
    handleAnnotateTypeChange(annotateType) {
      // 更改标注类型会清除不符合条件的标签组
      // 目标检测(1) 目标跟踪(5) 可以选中预置标签组中的Coco(id=1)
      if ([1, 5].includes(annotateType)) {
        if(this.labelGroupId !== 1 && this.labelGroupId === this.systemLabelGroupId) {
          this.systemLabelEnabled = true;
          this.labelGroupId = null;
          this.systemLabelGroupId = null; 
        }
      }
      // 图像分类(2)可以选中预置标签组Coco(id=1)和ImageNet(id=2)
      if (annotateType === 2) {
        if(![1, 2].includes(this.labelGroupId) && this.labelGroupId === this.systemLabelGroupId) {
          this.systemLabelEnabled = true;
          this.labelGroupId = null;
          this.systemLabelGroupId = null;
        }
      }
      // 其余不可以使用预置标签组
      if (![1, 2, 5].includes(annotateType)) {
        if( this.labelGroupId === this.systemLabelGroupId) {
          this.systemLabelGroupId = null;
          this.labelGroupId = null;
          this.labelGroupName = null;
          this.systemLabelEnabled = false;
          this.labelGroupTab = "custom";
        }
      } 
    },
    // step0 创建数据集调用
    createDataset() {
      if (this.activeStep === 0) {
        this.crud.findVM('form').$refs.form.validate(valid => {
          if (!valid) {
            return;
          }
          this.crud.status.add = CRUD.STATUS.PROCESSING;
          this.crud.form.labelGroupId = this.labelGroupId;
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
      if (dataType === 0) {
        return submit(datasetId, files);
      } 
      if (dataType === 1) {
        return submitVideo(datasetId, {
          frameInterval: this.step1Form.frameInterval,
          url: files[0].url,
        });
      }
      return Promise.reject();
    },
    // step1 上传成功
    uploadSuccess(res) {
      if (this.crud.status.cu > 0) {
        this.activeStep+=1;
      }
      // 视频上传完毕
      if (this.form.dataType === 1) {
        this.videoUploadProgress = 100;
      }
      const files = getImgFromMinIO(res);
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
    uploadError() {
      this.uploadStatus = 'exception';
      this.$message({
        message: '上传文件失败',
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
        // eslint-disable-next-line func-names
        this.$nextTick(function() {
          this.uploadPercent =
            this.uploadPercent > 100 ? 100 : toFixed(resolved / total);
        });
      });

      if (this.crud.status.cu > 0) {
        this.activeStep = 2;
      }
    },

    // step2 进度格式化
    formatProgress(percentage) {
      let formatTxt = `${percentage}%`;
      if (this.form.dataType === 1) {
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
