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

<!--使用场景: add: 任务创建(jobAdd), edit: 任务版本修改(jobDetail), paramsAdd: 模板创建(jobDetail), paramsEdit: 模板修改(job)-->
<template>
  <div>
    <el-form
      ref="form"
      :model="form"
      :rules="rules"
      label-width="150px"
      :style="`width: ${widthPercent}%; margin-top: 20px;`"
    >
      <el-form-item v-if="type === 'add' || type === 'paramsAdd'" label="任务名称" prop="trainName">
        <el-input v-model="form.trainName" />
      </el-form-item>
      <el-form-item v-if="type === 'edit'" label="任务名称" prop="jobName">
        <div>{{ form.jobName }}</div>
      </el-form-item>
      <el-form-item
        v-if="type === 'saveParams' || type === 'paramsEdit'"
        label="任务模板名称"
        prop="paramName"
      >
        <el-input id="paramName" v-model="form.paramName" />
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input id="description" v-model="form.description" type="textarea" />
      </el-form-item>
      <el-divider />
      <!--可编辑-->
      <template v-if="type !== 'saveParams'">
        <el-form-item label="创建方式">
          <el-radio-group v-model="notebookCreate" @change="onNotebookCreateChange">
            <el-radio :label="false" border>常规创建</el-radio>
            <el-radio :label="true" border class="w-200">启动 Notebook 保存环境</el-radio>
          </el-radio-group>
          <el-tooltip
            effect="dark"
            content="训练任务的创建方式可以是常规创建，即依次选择算法、镜像或数据集后开始训练，亦可直接启动用户自己创建的Notebook进行训练。"
            placement="top"
          >
            <i class="el-icon-warning-outline primary f18 v-text-top" />
          </el-tooltip>
        </el-form-item>
        <el-divider />
        <el-form-item v-if="!notebookCreate" label="选用算法类型" prop="algorithmSource">
          <el-radio-group v-model="form.algorithmSource" @change="onAlgorithmSourceChange">
            <el-radio id="algorithm_tab_0" :label="ALGORITHM_RESOURCE_ENUM.CUSTOM" border
              >我的算法</el-radio
            >
            <el-radio id="algorithm_tab_1" :label="ALGORITHM_RESOURCE_ENUM.PRESET" border
              >预置算法</el-radio
            >
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="!notebookCreate" ref="algorithmId" label="选用算法" prop="algorithmId">
          <el-select
            id="algorithmId"
            v-model="form.algorithmId"
            v-el-select-load-more="getAlgorithmList"
            placeholder="请选择您使用的算法代码"
            class="w270"
            filterable
            @change="onAlgorithmChange"
          >
            <el-option
              v-for="item in algorithmIdList"
              :key="item.id"
              :value="item.id"
              :label="item.algorithmName"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!notebookCreate" ref="imageTag" label="镜像选择" prop="imageTag">
          <el-select
            id="imageName"
            v-model="form.imageName"
            placeholder="请选择镜像"
            style="width: 190px;"
            clearable
            filterable
            @change="getHarborImages"
          >
            <el-option v-for="item in harborProjectList" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select
            id="imageTag"
            v-model="form.imageTag"
            placeholder="请选择镜像版本"
            style="width: 305px;"
            clearable
            filterable
            @change="validateField('imageTag')"
          >
            <el-option
              v-for="(item, index) in harborImageList"
              :key="index"
              :label="item.imageTag"
              :value="item.imageTag"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="notebookCreate" label="选择 Notebook 环境" prop="notebookId">
          <el-select
            v-model="form.notebookId"
            placeholder="请选择 Notebook 任务"
            class="w270"
            filterable
          >
            <el-option
              v-for="item in notebookList"
              :key="item.id"
              :value="item.id"
              :label="item.noteBookName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="加载模型">
          <el-switch v-model="useModel" @change="onUseModelChange" />
        </el-form-item>
        <el-form-item v-if="useModel" label="选用模型类型">
          <el-radio-group v-model="form.modelResource" @change="onModelResourceChange">
            <el-radio :label="MODEL_RESOURCE_ENUM.CUSTOM" border>我的模型</el-radio>
            <el-radio :label="MODEL_RESOURCE_ENUM.PRESET" border>预训练模型</el-radio>
            <el-radio :label="MODEL_RESOURCE_ENUM.ATLAS" border>炼知模型</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="
            [MODEL_RESOURCE_ENUM.CUSTOM, MODEL_RESOURCE_ENUM.PRESET].includes(form.modelResource)
          "
          key="modelSelect"
          label="模型选择"
          class="is-required"
          :error="modelSelectionErrorMsg"
        >
          <el-select
            id="modelId"
            v-model="form.modelId"
            placeholder="请选择模型"
            style="width: 190px;"
            clearable
            filterable
            @change="onModelChange"
          >
            <el-option
              v-for="item in modelList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
          <el-select
            v-if="useCustomModel"
            id="modelBranchId"
            v-model="form.modelBranchId"
            placeholder="请选择模型版本"
            style="width: 305px;"
            clearable
            filterable
            @change="onModelBranchChange"
          >
            <el-option
              v-for="item in modelBranchList"
              :key="item.id"
              :label="item.version"
              :value="item.id"
            />
          </el-select>
          <el-tooltip
            effect="dark"
            content="模型路径通过“model_load_dir”传到算法内部"
            placement="top"
          >
            <i class="el-icon-warning-outline primary f18 v-text-top" />
          </el-tooltip>
        </el-form-item>
        <el-form-item
          v-if="useAtlasModel"
          key="teacherModel"
          label="教师模型"
          class="is-required"
          :error="teacherModelErrorMsg"
        >
          <el-select
            v-model="teacherModelIds"
            multiple
            clearable
            filterable
            placeholder="请选择教师模型"
            @change="onTeacherModelChange"
          >
            <el-option
              v-for="model in modelList"
              :key="model.id"
              :label="model.name"
              :value="model.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="useAtlasModel" label="学生模型">
          <el-select
            v-model="studentModelIds"
            multiple
            clearable
            filterable
            placeholder="请选择学生模型"
          >
            <el-option
              v-for="model in modelList"
              :key="model.id"
              :label="model.name"
              :value="model.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item ref="trainDataSource" label="训练数据集" prop="dataSourcePath">
          <DataSourceSelector
            ref="trainDataSourceSelector"
            type="train"
            :algorithm-usage="form.algorithmUsage"
            :data-source-name="form.dataSourceName"
            :data-source-path="form.dataSourcePath"
            @change="onTrainDataSourceChange"
          />
        </el-form-item>
        <el-form-item label="验证数据集" prop="valType">
          <el-switch
            v-model="form.valType"
            :active-value="1"
            :inactive-value="0"
            @change="onVerifyTypeChange"
          />
        </el-form-item>
        <el-form-item
          v-if="form.valType"
          ref="verifyDataSource"
          label="验证数据集"
          prop="verifyDataSourcePath"
        >
          <DataSourceSelector
            ref="verifyDataSourceSelector"
            type="verify"
            :algorithm-usage="form.valAlgorithmUsage"
            :data-source-name="form.valDataSourceName"
            :data-source-path="form.valDataSourcePath"
            @change="onVerifyDataSourceChange"
          />
        </el-form-item>
        <el-form-item ref="runCommand" label="运行命令" prop="runCommand">
          <el-input
            id="runCommand"
            v-model="form.runCommand"
            placeholder="例如：python mnist.py"
            style="max-width: 500px;"
          />
        </el-form-item>
        <!--运行参数-->
        <run-param-form
          ref="runParamComp"
          :run-param-obj="form.runParams || {}"
          prop="runParams"
          param-label-width="120px"
          class="w120"
          @updateRunParams="updateRunParams"
        />
        <el-divider />
        <el-form-item label="节点数" prop="resourcesPoolNode">
          <el-input-number
            id="resourcesPoolNode"
            v-model="form.resourcesPoolNode"
            :min="1"
            :max="trainConfig.trainNodeMax"
            :step-strictly="true"
            @change="onResourcesPoolNodeChange"
          />
          <el-tooltip
            v-show="form.resourcesPoolNode > 1"
            effect="dark"
            content="请确保代码中包含“num_nodes”参数和“node_ips”参数用于接收分布式相关参数"
            placement="top"
          >
            <i class="el-icon-warning-outline primary f18 v-text-top" />
          </el-tooltip>
        </el-form-item>
        <el-form-item label="节点类型" class="is-required">
          <el-radio-group v-model="form.resourcesPoolType" @change="onResourcesPoolTypeChange">
            <el-radio id="resourcesPoolType_tab_0" :label="0" border>CPU</el-radio>
            <el-radio id="resourcesPoolType_tab_1" :label="1" border>GPU</el-radio>
          </el-radio-group>
          <el-tooltip
            v-if="form.resourcesPoolType"
            effect="dark"
            content="后台将自动获取并填充参数 gpu_num_per_node"
            placement="top"
          >
            <i class="el-icon-warning-outline primary f18 v-text-top" />
          </el-tooltip>
        </el-form-item>
        <el-form-item ref="trainJobSpecs" label="节点规格" prop="trainJobSpecsName" class="w270">
          <el-select id="trainJobSpecsName" v-model="form.trainJobSpecsName" filterable>
            <el-option
              v-for="spec in specsList"
              :key="spec.id"
              :label="spec.specsName"
              :value="spec.specsName"
            />
          </el-select>
          <el-tooltip
            v-if="form.trainType"
            effect="dark"
            content="每个节点的节点规格"
            placement="top"
          >
            <i class="el-icon-warning-outline primary f18 v-text-top" />
          </el-tooltip>
        </el-form-item>
        <el-form-item label="延迟启停">
          <el-switch id="delayCreateDelete" v-model="delayCreateDelete" @change="onDelayChange" />
        </el-form-item>
        <el-form-item v-if="delayCreateDelete" label="延迟启动" prop="delayCreateTime">
          <el-input-number
            id="delayCreateTime"
            v-model="form.delayCreateTime"
            :min="0"
            :max="trainConfig.delayCreateTimeMax"
            :step-strictly="true"
          />&nbsp;小时
        </el-form-item>
        <el-form-item v-if="delayCreateDelete" label="训练时长上限" prop="delayDeleteTime">
          <el-input-number
            id="delayDeleteTime"
            v-model="form.delayDeleteTime"
            :min="0"
            :max="trainConfig.delayDeleteTimeMax"
            :step-strictly="true"
          />&nbsp;小时
          <el-tooltip effect="dark" content="选择 0 表示不限制训练时长" placement="top">
            <i class="el-icon-warning-outline primary f18 v-text-top" />
          </el-tooltip>
        </el-form-item>
        <el-form-item label="运行命令预览" prop="preview">
          <div class="command-preview">
            {{ preview }}
          </div>
        </el-form-item>
      </template>
      <!--不可编辑-->
      <template v-if="type === 'saveParams'">
        <el-form-item label="选用算法类型">
          {{ form.algorithmSource === ALGORITHM_RESOURCE_ENUM.PRESET ? '预置算法' : '我的算法' }}
        </el-form-item>
        <el-form-item label="选用算法">
          {{ form.algorithmName }}
        </el-form-item>
        <el-form-item label="镜像选择">
          {{ form.imageName }}
        </el-form-item>
        <el-form-item
          v-if="
            [MODEL_RESOURCE_ENUM.CUSTOM, MODEL_RESOURCE_ENUM.PRESET].includes(form.modelResource)
          "
          label="模型选择"
        >
          {{ trainModel.name }}
        </el-form-item>
        <el-form-item v-if="useAtlasModel" label="教师模型">
          {{ teacherModelNames }}
        </el-form-item>
        <el-form-item v-if="useAtlasModel" label="学生模型">
          {{ studentModelNames }}
        </el-form-item>
        <el-form-item label="训练数据集">
          {{ form.dataSourceName }}
        </el-form-item>
        <el-form-item label="验证数据集">
          {{ form.valDataSourceName }}
        </el-form-item>
        <el-form-item label="运行命令">
          {{ form.runCommand }}
        </el-form-item>
        <el-form-item label="运行参数">
          <span v-for="key of Object.keys(form.runParams || {})" :key="key"
            >--{{ key }}={{ form.runParams[key] }}
          </span>
        </el-form-item>
        <el-form-item label="分布式训练">
          {{ form.trainType === 1 ? '是' : '否' }}
        </el-form-item>
        <el-form-item v-if="form.trainType" label="节点数">
          {{ form.resourcesPoolNode }}
        </el-form-item>
        <el-form-item label="延迟启停">
          {{ delayCreateDelete ? '是' : '否' }}
        </el-form-item>
        <el-form-item v-if="delayCreateDelete" label="延迟启动">
          {{ form.delayCreateTime }}&nbsp;小时
        </el-form-item>
        <el-form-item v-if="delayCreateDelete" label="延迟停止">
          {{ form.delayDeleteTime }}&nbsp;小时
        </el-form-item>
        <el-form-item label="节点类型">
          {{ form.resourcesPoolNode }}
        </el-form-item>
        <el-form-item label="节点规格">
          {{ formSpecs && formSpecs.specsName }}
        </el-form-item>
        <el-form-item label="运行命令预览">
          <div class="command-preview">
            {{ preview }}
          </div>
        </el-form-item>
      </template>
    </el-form>
  </div>
</template>

<script>
import { isNil, isObjectLike } from 'lodash';

import {
  validateNameWithHyphen,
  getQueueMessage,
  ALGORITHM_RESOURCE_ENUM,
  MODEL_RESOURCE_ENUM,
  RESOURCES_MODULE_ENUM,
} from '@/utils';
import { list as getAlgorithmList } from '@/api/algorithm/algorithm';
import { getModelByResource } from '@/api/model/model';
import { list as getModelBranchs } from '@/api/model/modelVersion';
import { getTrainModel } from '@/api/trainingJob/job';
import { getImageNameList, getImageTagList } from '@/api/trainingImage';
import { list as getSpecsNames } from '@/api/system/resources';
import { list as getNotebooks } from '@/api/development/notebook';
import { trainConfig } from '@/config';
import { IMAGE_PROJECT_TYPE } from '@/views/trainingJob/utils';
import { NOTEBOOK_STATUS_ENUM } from '@/views/development/utils';

import RunParamForm from './runParamForm';
import DataSourceSelector from './dataSourceSelector';

/**
 * 添加一个新的字段时，需要考虑修改如下代码：
 * defaultForm: 默认表单
 * initForm(): 表单初始化方法
 * save(): 表单验证及提交方法
 * reset(): 重置表单方法
 */

const defaultForm = {
  id: null, // 用于编辑训练任务时, 表单传递 jobId
  trainName: '',
  jobName: '', // 用于编辑训练任务时, 表单展示 jobName
  paramName: '',
  description: '',
  algorithmSource: ALGORITHM_RESOURCE_ENUM.CUSTOM,
  algorithmId: null,
  algorithmName: null,
  algorithmUsage: null,
  valAlgorithmUsage: null,
  imageTag: null,
  imageName: null,
  notebookId: null,
  dataSourceName: null,
  dataSourcePath: null,
  valDataSourceName: null,
  valDataSourcePath: null,
  runCommand: '',
  runParams: {},
  trainType: 0,
  valType: 0,
  resourcesPoolNode: 1,
  resourcesPoolType: 0,
  trainJobSpecsName: null,
  outPath: '/home/result/',
  logPath: '/home/log/',
  // 延迟启停相关参数
  delayCreateTime: 0,
  delayDeleteTime: 0,
  // 模型相关参数
  modelResource: null,
  teacherModelIds: null,
  studentModelIds: null,
  modelId: null,
  modelBranchId: null,
};

export default {
  name: 'JobForm',
  components: { RunParamForm, DataSourceSelector },
  props: {
    type: {
      type: String,
      default: 'add', // add: 新增训练任务; paramsAdd: 任务参数创建训练任务; edit: 修改训练任务; saveParams: 保存训练参数模板; paramsEdit: 修改训练参数模板。
    },
    widthPercent: {
      type: Number,
      default: 60,
    },
  },
  data() {
    return {
      ALGORITHM_RESOURCE_ENUM,
      MODEL_RESOURCE_ENUM,

      algorithmIdList: [],
      harborProjectList: [],
      harborImageList: [],
      modelList: [],
      modelBranchList: [],
      noMoreLoadAlg: false,
      algLoading: false,
      currentAlgPage: 1,
      algPageSize: 1000,
      runParamObj: {}, // 该对象用于提交训练
      dictReady: false,
      delayCreateDelete: false,
      selectedAlgorithm: null,
      trainConfig,

      useModel: false, // 本地判断是否使用模型
      teacherModelIds: [],
      studentModelIds: [],
      modelSelectionErrorMsg: '', // 模型选择错误信息
      teacherModelErrorMsg: '', // 教师模型错误信息

      trainModelList: [],
      teacherModelList: [],
      studentModelList: [],

      specsList: [],
      notebookCreate: false,
      notebookList: [],

      form: { ...defaultForm },
      rules: {
        trainName: [
          { required: true, message: '请输入任务名称', trigger: 'blur' },
          { max: 32, message: '长度控制在32个字符', trigger: 'blur' },
          { validator: validateNameWithHyphen, trigger: ['blur', 'change'] },
        ],
        paramName: [
          { required: true, message: '请输入任务参数名称', trigger: 'blur' },
          { max: 32, message: '长度控制在32个字符', trigger: 'blur' },
          { validator: validateNameWithHyphen, trigger: ['blur', 'change'] },
        ],
        algorithmSource: [{ required: true, message: '请选择算法', trigger: 'change' }],
        algorithmId: [{ required: true, message: '请选择算法', trigger: 'manual' }],
        notebookId: [{ required: true, message: '请选择 Notebook 任务', trigger: 'change' }],
        imageTag: [{ required: true, message: '请选择镜像', trigger: 'manual' }],
        trainJobSpecsName: [{ required: true, message: '请选择节点规格', trigger: 'change' }],
        runCommand: [{ required: true, message: '请输入运行命令', trigger: ['blur', 'change'] }],
      },
    };
  },
  computed: {
    formSpecs() {
      return this.specsList.find((spec) => spec.specsName === this.form.trainJobSpecsName);
    },
    isSaveParams() {
      return this.type === 'saveParams';
    },
    preview() {
      let str = this.form.runCommand;
      for (const key of Object.keys(this.runParamObj)) {
        str += ` --${key}=${this.runParamObj[key]}`;
      }
      if (this.selectedAlgorithm) {
        str += this.selectedAlgorithm.isTrainOut ? ' --train_out=/workspace/log' : '';
        str += this.selectedAlgorithm.isTrainModelOut ? ' --train_model_out=/workspace/out' : '';
        str += this.selectedAlgorithm.isVisualizedLog
          ? ' --train_visualized_log=/workspace/visualizedlog'
          : '';
      }
      str += this.form.dataSourceName && this.form.dataSourcePath ? ' --data_url=/dataset' : '';
      str +=
        this.form.valDataSourceName && this.form.valDataSourcePath
          ? ' --val_data_url=/valdataset'
          : '';
      str += this.form.modelId && this.form.modelBranchId ? ' --model_load_dir=/modeldir' : '';
      if (this.form.resourcesPoolType) {
        // eslint-disable-next-line no-template-curly-in-string
        str += ' --gpu_num_per_node=${gpu_num}';
      }
      if (this.form.resourcesPoolNode > 1) {
        str += ` --num_nodes=${this.form.resourcesPoolNode} --node_ips=\${node_ips}`;
      }
      return str;
    },
    useCustomModel() {
      return this.form.modelResource === MODEL_RESOURCE_ENUM.CUSTOM;
    },
    useAtlasModel() {
      return this.form.modelResource === MODEL_RESOURCE_ENUM.ATLAS;
    },
    trainModel() {
      return this.trainModelList.length ? this.trainModelList[0] : {};
    },
    teacherModelNames() {
      return this.teacherModelList.map((model) => model.name).join(', ');
    },
    studentModelNames() {
      return this.studentModelList.map((model) => model.name).join(', ');
    },
    usePresetAlgorithm() {
      return this.form.algorithmSource === ALGORITHM_RESOURCE_ENUM.PRESET;
    },
  },
  created() {
    this.callMsg = getQueueMessage();
  },
  methods: {
    initForm(form) {
      const newForm = form || {};
      Object.keys(this.form).forEach((item) => {
        if (!isNil(newForm[item])) {
          this.form[item] = newForm[item];
        }
      });
      this.notebookCreate = Boolean(this.form.notebookId);
      setTimeout(async () => {
        this.delayCreateDelete = this.form.delayCreateTime !== 0 && this.form.delayDeleteTime !== 0;
        this.getAlgorithmList();
        this.getNotebookList(true);
        if (!this.isSaveParams) {
          this.getHarborProjects().then(() => {
            this.resetProject();
          });
          this.getModels(true);
          this.$refs.trainDataSourceSelector.updateAlgorithmUsage(this.form.algorithmUsage, true);
          this.form.valType &&
            this.$refs.verifyDataSourceSelector.updateAlgorithmUsage(
              this.form.valAlgorithmUsage,
              true
            );
        } else if (this.form.modelResource !== null) {
          const { modelList, teacherModelList, studentModelList } = await getTrainModel({
            modelResource: this.form.modelResource,
            modelId: this.form.modelId || undefined,
            modelBranchId: this.form.modelBranchId || undefined,
            teacherModelIds: this.form.teacherModelIds || undefined,
            studentModelIds: this.form.studentModelIds || undefined,
          });
          this.trainModelList = modelList;
          this.teacherModelList = teacherModelList;
          this.studentModelList = studentModelList;
        }
        this.onResourcesPoolTypeChange(!['add'].includes(this.type));

        // 根据 modelResource 的值来判断是否使用了模型
        this.useModel = this.form.modelResource !== null;
        // runParamObj 初始值为 form.runParams
        this.runParamObj = { ...this.form.runParams };
        this.clearValidate();
      }, 0);
    },
    validate(...args) {
      this.$refs.form.validate.apply(this, args);
    },
    clearValidate(...args) {
      this.$refs.form.clearValidate.apply(this, args);
    },
    validateField(field) {
      this.$refs[field].validate('manual');
    },
    clearFieldValidate(field) {
      this.$refs[field].clearValidate();
    },
    updateRunParams(params) {
      this.runParamObj = params;
    },
    save() {
      if (this.loading) {
        return;
      }
      // 先将字符串模式转换为键值对模式
      if (!this.isSaveParams && this.$refs.runParamComp.paramsMode === 2) {
        this.$refs.runParamComp.convertArgsToPairs();
      }
      // 保存训练参数时，不对 runParams 进行校验
      const runParamsValid = this.isSaveParams || this.$refs.runParamComp.validate();
      if (!runParamsValid) {
        this.$message({
          message: '运行参数不合法',
          type: 'warning',
        });
        return;
      }

      if (!this.isSaveParams && !this.checkModelValid()) {
        return;
      }

      // 清除模型部分多余字段
      if (!this.useAtlasModel) {
        Object.assign(this.form, {
          teacherModelIds: null,
          studentModelIds: null,
        });
      }

      this.$refs.form.validate(async (valid) => {
        if (valid) {
          const params = { ...this.form };
          params.runParams = { ...this.runParamObj }; // 提交时由 runParamObj 确定对象值
          if (this.formSpecs) {
            const { cpuNum, gpuNum, memNum, workspaceRequest } = this.formSpecs;
            Object.assign(params, { cpuNum, gpuNum, memNum, workspaceRequest });
          }
          // 请求交互都不放在组件完成
          this.$emit('getForm', params);
        } else {
          this.$message({
            message: '请仔细检查任务参数',
            type: 'warning',
          });
        }
      });
    },
    // 镜像项目为空时选择默认项目
    resetProject() {
      if (!this.form.imageName) {
        if (this.harborProjectList.some((project) => project === 'oneflow')) {
          this.form.imageName = 'oneflow';
        } else if (this.harborProjectList.length) {
          this.form.imageName = this.harborProjectList[0].imageName;
        } else {
          this.$message.warning('镜像项目列表为空');
          return;
        }
        this.getHarborImages();
      }
    },
    reset() {
      this.$refs.trainDataSourceSelector.reset();
      this.form = { ...defaultForm };
      this.form.runParams = {};
      this.runParamObj = {};
      this.selectedAlgorithm = null;
      this.delayCreateDelete = false;
      this.useModel = false;
      this.getModels();

      this.modelSelectionErrorMsg = '';
      // 清空模型炼知数据
      this.teacherModelIds = [];
      this.studentModelIds = [];
      this.teacherModelErrorMsg = '';

      this.$message({
        message: '数据已重置',
        type: 'success',
      });
      setTimeout(() => {
        this.onResourcesPoolTypeChange();
        this.resetProject();
        this.$refs.form.clearValidate();
        this.$refs.runParamComp.reset();
      }, 0);
    },
    // 用于恢复指定字段的表单值为默认值
    partialReset(keys) {
      keys.forEach((key) => {
        if (defaultForm[key] !== undefined) {
          if (isObjectLike(defaultForm[key])) {
            this.form[key] = { ...defaultForm[key] };
          } else {
            this.form[key] = defaultForm[key];
          }
        }
      });
    },
    async getHarborProjects() {
      this.harborProjectList = await getImageNameList({
        projectTypes: [
          IMAGE_PROJECT_TYPE.TRAIN,
          IMAGE_PROJECT_TYPE.NOTEBOOK,
          IMAGE_PROJECT_TYPE.TERMINAL,
        ],
      });
      if (
        this.form.imageName &&
        !this.harborProjectList.some((project) => project === this.form.imageName)
      ) {
        this.$message.warning('该训练原有的运行项目不存在，请重新选择');
        this.form.imageName = null;
        this.form.imageTag = null;
        return;
      }
      this.form.imageName && (await this.getHarborImages(true));
      if (
        this.form.imageTag &&
        !this.harborImageList.some((image) => image.imageTag === this.form.imageTag)
      ) {
        this.$message.warning('该训练原有的运行镜像不存在，请重新选择');
        this.form.imageTag = null;
      }
    },
    getHarborImages(saveImageName = false) {
      if (saveImageName !== true) {
        this.form.imageTag = null;
      }
      if (!this.form.imageName) {
        this.harborImageList = [];
        return Promise.reject();
      }
      return getImageTagList({
        imageName: this.form.imageName,
      }).then((res) => {
        this.harborImageList = res;
      });
    },

    // saveModel 用于表示是否需要根据模型列表匹配模型/版本/教师模型/学生模型
    async getModels(saveModel = false) {
      // modelResource 不存在时，获取 我的模型 的模型列表
      this.modelList = await getModelByResource(
        this.form.modelResource || MODEL_RESOURCE_ENUM.CUSTOM
      );

      // 如果不保留则不进行其余任何操作
      if (!saveModel) {
        return;
      }

      switch (this.form.modelResource) {
        // 我的模型
        case MODEL_RESOURCE_ENUM.CUSTOM:
          if (!this.form.modelId) {
            return;
          }
          if (!this.modelList.find((model) => model.id === this.form.modelId)) {
            this.$message.warning('选择的模型不存在，请重新选择');
            this.form.modelId = this.form.modelBranchId = null;
            return;
          }
          this.getModelBranchs(this.form.modelId, saveModel);
          break;
        // 预训练模型
        case MODEL_RESOURCE_ENUM.PRESET:
          if (!this.form.modelId) {
            return;
          }
          if (!this.modelList.find((model) => model.id === this.form.modelId)) {
            this.$message.warning('选择的模型不存在，请重新选择');
            this.form.modelId = null;
          }
          break;
        // 炼知模型
        case MODEL_RESOURCE_ENUM.ATLAS:
          this.pushModel(this.teacherModelIds, this.form.teacherModelIds, '教师');
          this.pushModel(this.studentModelIds, this.form.studentModelIds, '学生');
          break;
        // no default
      }
    },
    async getModelBranchs(parentId, saveBranchId = false) {
      if (!this.useCustomModel) {
        return;
      } // 只有使用 我的模型 时，才获取版本列表
      this.modelBranchList = (await getModelBranchs({ parentId })).result;

      // 如果不保留则清空模型版本选项
      if (!saveBranchId) {
        this.form.modelBranchId = null;
        return;
      }

      if (!this.form.modelBranchId) {
        return;
      }
      if (!this.modelBranchList.find((model) => model.id === this.form.modelBranchId)) {
        this.$message.warning('选择的模型版本不存在，请重新选择');
        this.form.modelBranchId = null;
      }
    },
    pushModel(modelList, modelIdString, modelType = '') {
      if (!modelIdString) {
        return;
      }
      const modelIdList = modelIdString.split(',');
      const existSet = new Set();
      const notExistSet = new Set();

      // 教师、学生模型 在修改时，如果部分模型不存在，则只显示剩余模型
      modelIdList.forEach((id) => {
        if (this.modelList.find((model) => model.id === Number(id))) {
          existSet.add(Number(id));
        } else {
          notExistSet.add(id);
        }
      });

      Array.from(existSet).forEach((id) => modelList.push(id));
      if (notExistSet.size > 0) {
        this.callMsg({
          message: `以下 id 的${modelType}模型不存在: ${Array.from(notExistSet).join('、')}`,
          type: 'warning',
        });
      }
    },

    onModelResourceChange() {
      // 模型类型修改时，清空 模型/模型版本/模型版本列表/教师模型/学生模型
      this.form.modelId = this.form.modelBranchId = null;
      this.modelBranchList = [];
      this.teacherModelIds = [];
      this.studentModelIds = [];
      this.modelSelectionErrorMsg = '';
      this.teacherModelErrorMsg = '';
      this.getModels();
    },
    onUseModelChange(useModel) {
      if (useModel) {
        this.form.modelResource = MODEL_RESOURCE_ENUM.CUSTOM;
      } else {
        Object.assign(this.form, {
          modelResource: null,
          modelId: null,
          modelBranchId: null,
        });
        this.teacherModelIds = [];
        this.studentModelIds = [];
        this.modelSelectionErrorMsg = '';
        this.teacherModelErrorMsg = '';
        // 取消加载模型时，重新获取 我的模型 模型列表，以备再次启用
        this.getModels();
      }
    },
    onModelChange(id) {
      if (this.useCustomModel) {
        if (id) {
          this.getModelBranchs(id);
        } else {
          this.modelBranchList = [];
        }
      } else {
        this.checkModelValid();
      }
    },
    onModelBranchChange() {
      this.checkModelValid();
    },
    onTeacherModelChange() {
      this.checkModelValid();
    },
    checkModelValid() {
      // 模型信息校验
      let errorMsg = null;
      switch (this.form.modelResource) {
        // 我的模型
        case MODEL_RESOURCE_ENUM.CUSTOM:
          if (!this.form.modelId) {
            errorMsg = '模型不能为空';
          } else if (!this.form.modelBranchId) {
            errorMsg = '模型版本不能为空';
          }
          this.modelSelectionErrorMsg = errorMsg;
          if (errorMsg) {
            this.$message.warning(errorMsg);
            return false;
          }
          break;

        // 预训练模型
        case MODEL_RESOURCE_ENUM.PRESET:
          if (!this.form.modelId) {
            errorMsg = '模型不能为空';
          }
          this.modelSelectionErrorMsg = errorMsg;
          if (errorMsg) {
            this.$message.warning(errorMsg);
            return false;
          }
          break;

        // 炼知模型
        case MODEL_RESOURCE_ENUM.ATLAS:
          if (!this.teacherModelIds.length) {
            errorMsg = '教师模型不能为空';
          }
          this.teacherModelErrorMsg = errorMsg;
          if (errorMsg) {
            this.$message.warning(errorMsg);
            return false;
          }
          this.form.teacherModelIds = this.teacherModelIds.join(',');
          this.form.studentModelIds = this.studentModelIds.length
            ? this.studentModelIds.join(',')
            : null;
          break;
        // no default
      }
      return true;
    },

    getAlgorithmList() {
      if (this.noMoreLoadAlg || this.algLoading) {
        return;
      }
      this.algLoading = true;
      const params = {
        algorithmSource: this.form.algorithmSource || ALGORITHM_RESOURCE_ENUM.CUSTOM,
        current: this.currentAlgPage,
        size: this.algPageSize,
      };
      getAlgorithmList(params)
        .then((res) => {
          this.algorithmIdList = this.algorithmIdList.concat(res.result);
          this.currentAlgPage += 1;
          this.algLoading = false;
          if (res.result.length < this.algPageSize) {
            this.noMoreLoadAlg = true;
          }
          if (this.form.algorithmId) {
            this.selectedAlgorithm = this.algorithmIdList.find(
              (item) => item.id === this.form.algorithmId
            );
            if (!this.selectedAlgorithm) {
              this.$message.warning('原有算法不存在，请重新选择');
              this.form.algorithmId = null;
            }
          }
        })
        .finally(() => {
          this.algLoading = false;
        });
    },

    async onResourcesPoolTypeChange(keepSpec = false) {
      this.specsList = (
        await getSpecsNames({
          module: RESOURCES_MODULE_ENUM.TRAIN,
          resourcesPoolType: this.form.resourcesPoolType,
          current: 1,
          size: 500,
        })
      ).result;
      // 当没有显式指定保留节点规格时，选择规格列表第一个选项
      if (keepSpec !== true && this.specsList.length) {
        this.form.trainJobSpecsName = this.specsList[0].specsName;
      }
    },
    onTrainDataSourceChange(dataSourceResult) {
      this.form.dataSourceName = dataSourceResult.dataSourceName;
      this.form.dataSourcePath = dataSourceResult.dataSourcePath;
      this.form.algorithmUsage = dataSourceResult.algorithmUsage;
      // 如果在运行参数中包含了 image_counts 字段，则自动把数据集图片数量填充至该字段。
      this.$refs.runParamComp.updateParam('image_counts', dataSourceResult.imageCounts);
    },
    onVerifyDataSourceChange(result) {
      this.form.valDataSourceName = result.dataSourceName;
      this.form.valDataSourcePath = result.dataSourcePath;
      this.form.valAlgorithmUsage = result.algorithmUsage;
    },
    async onAlgorithmChange(id) {
      // 选用算法变更时，需要对自动填充的表单项进行验证
      this.validateField('algorithmId');
      // 选用算法变更时，需要同步算法的模型类别、运行项目、运行镜像、运行命令、运行参数
      const algorithm = this.algorithmIdList.find((i) => i.id === id);
      this.selectedAlgorithm = algorithm;
      this.form.algorithmUsage = algorithm?.algorithmUsage || null; // 同步算法用途
      this.form.valAlgorithmUsage = algorithm?.algorithmUsage || null; // 同步算法用途到验证数据集
      this.$refs.trainDataSourceSelector.updateAlgorithmUsage(this.form.algorithmUsage); // 根据算法用途更新数据集列表
      this.form.valType &&
        this.$refs.verifyDataSourceSelector.updateAlgorithmUsage(this.form.valAlgorithmUsage); // 根据验证数据集算法用途更新数据集列表
      if (this.usePresetAlgorithm) {
        this.form.runCommand = algorithm?.runCommand || ''; // 同步运行命令
        this.form.runParams = algorithm?.runParams || {}; // 同步运行参数
        this.runParamObj = { ...this.form.runParams }; // 同步运行参数
        this.form.imageName = algorithm?.imageName; // 同步镜像名称
        this.$nextTick(() => {
          this.clearFieldValidate('runCommand'); // 清空运行命令的表单校验
        });
        // 镜像名校验
        if (
          this.form.imageName &&
          !this.harborProjectList.some((project) => project === this.form.imageName)
        ) {
          this.$message.warning('算法选择的运行项目不存在，请重新选择');
          this.form.imageName = null;
          this.form.imageTag = null;
          return;
        }
        this.form.imageName && (await this.getHarborImages(true)); // 获取镜像版本列表
        this.form.imageTag = algorithm?.imageTag; // 同步镜像版本
        // 镜像版本校验
        if (
          this.form.imageTag &&
          !this.harborImageList.some((image) => image.imageTag === this.form.imageTag)
        ) {
          this.$message.warning('算法选择的运行镜像不存在，请重新选择');
          this.form.imageTag = null;
          return;
        }
        if (this.form.imageTag) {
          this.validateField('imageTag');
        }
        // 镜像项目为空时选择默认项目
        this.resetProject();
      }
    },
    onDelayChange(isDelay) {
      if (!isDelay) {
        this.form.delayCreateTime = 0;
        this.form.delayDeleteTime = 0;
      }
    },
    onVerifyTypeChange() {
      if (this.form.valType === 0) {
        this.form = Object.assign(this.form, {
          valDataSourceName: null,
          valDataSourcePath: null,
        });
        this.$refs.verifyDataSourceSelector.reset();
      } else {
        // 打开训练数据集时获取相应值
        this.$nextTick(() => {
          this.$refs.verifyDataSourceSelector.onAlgorithmUsageChange(this.form.valAlgorithmUsage);
        });
      }
    },
    async onAlgorithmSourceChange() {
      // 算法类型更改之后，需要清空下方表单
      this.algorithmIdList = [];
      this.currentAlgPage = 1;
      this.noMoreLoadAlg = false;
      this.selectedAlgorithm = null;
      this.partialReset([
        'algorithmId',
        'algorithmUsage',
        'dataSourceName',
        'dataSourcePath',
        'valAlgorithmUsage',
        'valDataSourceName',
        'valDataSourcePath',
        'imageTag',
        'imageName',
        'runCommand',
        'resourcesPoolType',
        'valType',
        'modelResource',
        'modelId',
        'modelBranchId',
        'runParams',
      ]);
      this.getAlgorithmList();
      this.$refs.trainDataSourceSelector.reset();
      // 切换算法时去获取相应内容
      this.$refs.trainDataSourceSelector.updateAlgorithmUsage(null);
      this.runParamObj = {};
      this.$nextTick(() => {
        this.clearFieldValidate('runCommand');
        this.clearFieldValidate('trainJobSpecs');
      });
      this.$refs.runParamComp.reset();
      this.harborImageList = [];
      this.resetProject();
      this.onResourcesPoolTypeChange();

      // 模型数据重置
      this.useModel = false;
      this.teacherModelIds = [];
      this.studentModelIds = [];
      this.teacherModelErrorMsg = '';
      this.getModels();
    },
    onResourcesPoolNodeChange(node) {
      this.form.trainType = node > 1 ? 1 : 0;
    },

    // 选择 Notebook 创建训练
    onNotebookCreateChange() {
      this.partialReset([
        'algorithmSource',
        'algorithmId',
        'algorithmUsage',
        'dataSourceName',
        'dataSourcePath',
        'valAlgorithmUsage',
        'valDataSourceName',
        'valDataSourcePath',
        'imageTag',
        'imageName',
        'runCommand',
        'resourcesPoolType',
        'valType',
        'modelResource',
        'modelId',
        'modelBranchId',
        'runParams',
      ]);
    },
    async getNotebookList(keepValue = false) {
      this.notebookList = (
        await getNotebooks({
          status: NOTEBOOK_STATUS_ENUM.STOPPED,
          current: 1,
          size: 500,
        })
      ).result;
      if (keepValue && this.form.notebookId) {
        const notebook = this.notebookList.find((n) => n.id === this.form.notebookId);
        if (!notebook) {
          this.$message.warning('原 Notebook 环境不存在，请重新选择');
          this.form.notebookId = null;
        }
      }
    },
  },
};
</script>

<style lang="scss" scoped>
::v-deep.w270 {
  .el-input {
    width: 270px;
  }
}

::v-deep.el-input-number {
  width: 270px;

  .el-input-number__increase,
  .el-input-number__decrease {
    width: 70px;
  }
}

.el-radio-group > .el-radio {
  margin-right: 0;
}

.el-radio.is-bordered {
  width: 130px;
  height: 35px;
  padding: 10px 0;
  text-align: center;

  &.w-200 {
    width: 200px;
  }
}
</style>
