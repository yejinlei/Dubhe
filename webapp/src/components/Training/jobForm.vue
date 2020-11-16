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

<!--使用场景: add: 任务创建(jobAdd）， edit：任务版本修改（jobDetail），paramAdd:模板创建（jobDetail），paramEdit：模板修改（job）-->
<template>
  <div>
    <el-form
      ref="form"
      :model="form"
      :rules="rules"
      label-width="120px"
      :style="`width: ${widthPercent}%; margin-top: 20px;`"
    >
      <el-form-item v-if="type==='add' || type === 'paramsAdd' || type === 'algoAdd'" label="任务名称" prop="trainName">
        <el-input v-model="form.trainName" />
      </el-form-item>
      <el-form-item v-if="type==='edit'" label="任务名称" prop="jobName">
        <div>{{ form.jobName }}</div>
      </el-form-item>
      <el-form-item v-if="type==='saveParams' || type==='paramEdit'" label="任务模板名称" prop="paramName">
        <el-input id="paramName" v-model="form.paramName" />
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input id="description" v-model="form.description" type="textarea" />
      </el-form-item>
      <el-divider />
      <!--可编辑-->
      <template v-if="type!=='saveParams'">
        <el-form-item label="选用算法类型" prop="algorithmSource">
          <el-radio-group v-model="form.algorithmSource" @change="onAlgorithmSourceChange">
            <el-radio
              id="algorithm_tab_0"
              :label="1"
              border
              class="mr-0"
            >我的算法</el-radio>
            <el-radio
              id="algorithm_tab_1"
              :label="2"
              border
            >预置算法</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          ref="algorithmId"
          label="选用算法"
          prop="algorithmId"
        >
          <el-select
            id="algorithmId"
            v-model="form.algorithmId"
            v-el-select-load-more="getAlgorithmList"
            placeholder="请选择您使用的算法代码"
            class="w270"
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
        <el-form-item
          ref="imageTag"
          label="镜像选择"
          prop="imageTag"
        >
          <el-select
            id="imageName"
            v-model="form.imageName"
            placeholder="请选择镜像"
            style="width: 190px;"
            clearable
            @change="getHarborImages"
          >
            <el-option
              v-for="item in harborProjectList"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
          <el-select
            id="imageTag"
            v-model="form.imageTag"
            placeholder="请选择镜像版本"
            style="width: 305px;"
            clearable
            @change="validateField('imageTag')"
          >
            <el-option
              v-for="(item, index) in harborImageList"
              :key="index"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="加载模型">
          <el-switch v-model="form.modelType" :active-value="1" :inactive-value="0" @change="onModelTypeChange"/>
        </el-form-item>
        <el-form-item v-if="form.modelType" label="选用模型类型">
          <el-radio-group v-model="form.modelResource" @change="onModelResourceChange">
            <el-radio :label="0" border class="mr-0">我的模型</el-radio>
            <el-radio :label="1" border>预训练模型</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.modelType" label="模型选择">
          <el-select
            id="modelId" 
            v-model="form.modelId"
            placeholder="请选择模型"
            style="width: 190px;"
            clearable
            @change="getModelNames"
          >
            <el-option
              v-for="item in modelNameList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
          <el-select
            v-if="!form.modelResource"
            id="modelLoadPathDir"
            v-model="form.modelLoadPathDir"
            placeholder="请选择模型版本"
            style="width: 305px;"
            clearable
          >
            <el-option
              v-for="item in modelLoadPathList"
              :key="item.id"
              :label="item.versionNum"
              :value="item.modelAddress"
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
        <el-form-item
          label="验证数据集"
          prop="valType"
        >
          <el-switch
            v-model="form.valType"
            :active-value="1"
            :inactive-value="0"
            @change="onVerifyTypeChange"
          />
        </el-form-item>
        <el-form-item v-if="form.valType" ref="verifyDataSource" label="验证数据集" prop="verifyDataSourcePath">
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
        <el-form-item
          label="分布式训练"
          prop="trainType"
          class="mt-10"
        >
          <el-switch
            id="trainType"
            v-model="form.trainType"
            :active-value="1"
            :inactive-value="0"
            @change="onTrainTypeChange"
          />
        </el-form-item>
        <el-form-item
          v-if="form.trainType"
          label="节点数"
          prop="resourcesPoolNode"
        >
          <el-input-number
            id="resourcesPoolNode"
            v-model="form.resourcesPoolNode"
            :min="2"
            :max="trainConfig.trainNodeMax"
            :step-strictly="true"
          />
          <el-tooltip effect="dark" content="请确保代码中包含“num_nodes”参数和“node_ips”参数用于接收分布式相关参数" placement="top">
            <i class="el-icon-warning-outline primary f18 v-text-top" />
          </el-tooltip>
        </el-form-item>
        <el-form-item label="节点类型" class="is-required">
          <el-radio-group
            v-model="form.resourcesPoolType"
            @change="onResourcesPoolTypeChange"
          >
            <el-radio
              id="resourcesPoolType_tab_0"
              :label="0"
              border
              class="mr-0"
            >CPU</el-radio>
            <el-radio
              id="resourcesPoolType_tab_1"
              :label="1"
              border
            >GPU</el-radio>
          </el-radio-group>
          <el-tooltip v-if="form.resourcesPoolType" effect="dark" content="后台将自动获取并填充参数 gpu_num_per_node" placement="top">
            <i class="el-icon-warning-outline primary f18 v-text-top" />
          </el-tooltip>
        </el-form-item>
        <el-form-item ref="trainJobSpecs" label="节点规格" prop="trainJobSpecsName" class="w270">
          <el-select id="trainJobSpecsName" v-model="form.trainJobSpecsName">
            <el-option
              v-for="spec in specList"
              :key="spec.id"
              :label="spec.label"
              :value="spec.label"
            />
          </el-select>
          <el-tooltip v-if="form.trainType" effect="dark" content="每个节点的节点规格" placement="top">
            <i class="el-icon-warning-outline primary f18 v-text-top" />
          </el-tooltip>
        </el-form-item>
        <el-form-item
          label="延迟启停"
        >
          <el-switch
            id="delayCreateDelete"
            v-model="delayCreateDelete"
            @change="onDelayChange"
          />
        </el-form-item>
        <el-form-item
          v-if="delayCreateDelete"
          label="延迟启动"
          prop="delayCreateTime"
        >
          <el-input-number
            id="delayCreateTime"
            v-model="form.delayCreateTime"
            :min="0"
            :max="trainConfig.delayCreateTimeMax"
            :step-strictly="true"
          />&nbsp;小时
        </el-form-item>
        <el-form-item
          v-if="delayCreateDelete"
          label="训练时长上限"
          prop="delayDeleteTime"
        >
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
          <div class="param">
            {{ preview }}
          </div>
        </el-form-item>
      </template>
      <!--不可编辑-->
      <template v-if="type==='saveParams'">
        <el-form-item label="选用算法类型">
          {{ form.algorithmSource === 2 ? '预置算法' : '我的算法' }}
        </el-form-item>
        <el-form-item label="选用算法">
          {{ form.algorithmName }}
        </el-form-item>
        <el-form-item label="镜像选择">
          {{ form.imageName }}
        </el-form-item>
        <el-form-item label="模型选择">
          {{ form.modelName }}
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
          <span
            v-for="key of Object.keys(form.runParams || {})"
            :key="key"
          >--{{ key }}={{ form.runParams[key] }} </span>
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
          {{ formSpecs && formSpecs.label }}
        </el-form-item>
        <el-form-item label="运行命令预览">
          <div class="param">
            {{ preview }}
          </div>
        </el-form-item>
      </template>
    </el-form>
  </div>
</template>

<script>
import { validateNameWithHyphen } from '@/utils';
import { list as getAlgorithmList } from '@/api/algorithm/algorithm';
import { harborProjectNames, harborImageNames } from '@/api/system/harbor';
import { list as getModelName } from '@/api/model/model';
import { list as getModelTag } from '@/api/model/modelVersion';
import { trainConfig } from '@/config';
import RunParamForm from './runParamForm';
import DataSourceSelector from './dataSourceSelector';

const defaultForm = {
  id: null, // 用于编辑训练任务时, 表单传递 jobId
  trainName: '',
  jobName: '', // 用于编辑训练任务时, 表单展示 jobName
  paramName: '',
  description: '',
  algorithmSource: 1,
  algorithmId: null,
  algorithmName: null,
  algorithmUsage: null,
  valAlgorithmUsage: null,
  imageTag: null,
  imageName: null,
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
  modelType: 0,
  modelResource: 0,
  modelId: null,
  modelLoadPathDir: null,
  modelName: null,
};

export default {
  name: 'JobForm',
  dicts: ['cpu_specs', 'gpu_specs'],
  components: { RunParamForm, DataSourceSelector },
  props: {
    type: {
      type: String,
      default: 'add', // add: 新增训练任务; paramsAdd: 任务参数创建训练任务; algoAdd: 算法创建训练任务; edit: 修改训练任务; saveParams: 保存训练参数模板; paramEdit: 修改训练参数模板。
    },
    widthPercent: {
      type: Number,
      default: 60,
    },
  },
  data() {
    return {
      algorithmIdList: [],
      harborProjectList: [],
      harborImageList: [],
      modelNameList: [],
      modelLoadPathList: [],
      noMoreLoadAlg: false,
      algLoading: false,
      currentAlgPage: 1,
      algPageSize: 1000,
      runParamObj: {}, // 该对象用于提交训练
      dictReady: false,
      delayCreateDelete: false,
      selectedAlgorithm: null,
      trainConfig,

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
        algorithmSource: [
          { required: true, message: '请选择算法', trigger: 'change' },
        ],
        algorithmId: [
          { required: true, message: '请选择算法', trigger: 'manual' },
        ],
        imageTag: [
          { required: true, message: '请选择镜像', trigger: 'manual' },
        ],
        dataSourcePath: [
          { required: true, message: '请选择数据集', trigger: 'manual' },
        ],
        trainJobSpecsName: [
          { required: true, message: '请选择节点规格', trigger: 'change' },
        ],
        runCommand: [
          { required: true, message: '请输入运行命令', trigger: ['blur', 'change'] },
        ],
      },
    };
  },
  computed: {
    formSpecs() {
      return this.specList.find(spec => spec.label === this.form.trainJobSpecsName);
    },
    specList() {
      switch(this.form.resourcesPoolType) {
        case 0:
          return this.dict.cpu_specs;
        case 1:
          return this.dict.gpu_specs;
        default:
          return [];
      }
    },
    preview() {
      let str = this.form.runCommand;
      for(const key of Object.keys(this.runParamObj)) {
        str += ` --${key}=${this.runParamObj[key]}`;
      }
      if (this.selectedAlgorithm) {
        str += this.selectedAlgorithm.isTrainLog? ' --train_log=/workspace/log' : '';
        str += this.selectedAlgorithm.isTrainOut? ' --train_out=/workspace/out' : '';
        str += this.selectedAlgorithm.isVisualizedLog? ' --train_visualized_log=/workspace/visualizedlog' : '';
        str += ' --data_url=/dataset';
      }
      str += this.form.valDataSourceName && this.form.valDataSourcePath ? ' --val_data_url=/valdataset' : '';
      str += this.form.modelId && this.form.modelLoadPathDir ? ' --model_load_dir=/modeldir' : '';
      if (this.form.resourcesPoolType) {
        // eslint-disable-next-line no-template-curly-in-string
        str += ' --gpu_num_per_node=${gpu_num}';
      }
      return str;
    },
  },
  mounted() {
    this.$on('dictReady', () => { this.dictReady = true; });
  },
  methods: {
    initForm(form) {
      const newForm = form || {};
      Object.keys(this.form).forEach(item => { newForm[item] && (this.form[item] = newForm[item]); });
      setTimeout(() => {
        this.delayCreateDelete = (this.form.delayCreateTime !== 0) && (this.form.delayDeleteTime !== 0);
        this.getAlgorithmList();
        if (this.type !== 'saveParams') {
          this.getHarborProjects().then(() => {
            this.resetProject();
          });
          this.getModelNames(false);
          this.$refs.trainDataSourceSelector.updateAlgorithmUsage(this.form.algorithmUsage, true);
          this.form.valType && this.$refs.verifyDataSourceSelector.updateAlgorithmUsage(this.form.valAlgorithmUsage, true);
        }
        if (this.dictReady) {
          this.onResourcesPoolTypeChange((this.type !== 'add') && (this.type !== 'algoAdd'));
        } else {
          this.$on('dictReady', () => this.onResourcesPoolTypeChange((this.type !== 'add') && (this.type !== 'algoAdd')));
        }
        // runParamObj 初始值为 form.runParams
        this.runParamObj = {...this.form.runParams} || {};
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
      if (this.type !== 'saveParams' && this.$refs.runParamComp.paramsMode === 2) {
        this.$refs.runParamComp.convertArgsToPairs();
      }
      const runParamsValid = this.type === 'saveParams' || this.$refs.runParamComp.validate();
      if (runParamsValid) {
        this.$refs.form.validate(async valid => {
          if (valid) {
            const params = {...this.form};
            params.runParams = {...this.runParamObj};
            params.trainJobSpecsInfo = this.formSpecs.value;
            delete params.modelName; // modelName只用来展示,不作为提交参数
            // 请求交互都不放在组件完成
            this.$emit('getForm', params);
          } else {
            this.$message({
              message: '请仔细检查任务参数',
              type: 'warning',
            });
          }
        });
      } else {
        this.$message({
          message: '运行参数不合法',
          type: 'warning',
        });
      }
    },
    // 镜像项目为空时选择默认项目
    resetProject() {
      if (!this.form.imageName) {
        if (this.harborProjectList.some(project => project === 'oneflow')) {
          this.form.imageName = 'oneflow';
        } else if (this.harborProjectList.length) {
          Object.assign(this.form, { imageName: this.harborProjectList[0] });
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
      this.runParamObj = {};
      this.selectedAlgorithm = null;
      this.delayCreateDelete = false;
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
    async getHarborProjects() {
      this.harborProjectList = await harborProjectNames();
      if (this.form.imageName && !this.harborProjectList.some(project => project === this.form.imageName)) {
        this.$message.warning('该训练原有的运行项目不存在，请重新选择');
        this.form.imageName = null;
        this.form.imageTag = null;
        return;
      }
      this.form.imageName && await this.getHarborImages(true);
      if (this.form.imageTag && !this.harborImageList.some(image => image === this.form.imageTag)) {
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
        return;
      }
      return harborImageNames({ imageName: this.form.imageName })
        .then(res => {
          this.harborImageList = res;
        });
    },
    
    async getModelNames(saveModel = true) {
      this.modelNameList = await getModelName({ modelResource: this.form.modelResource, filter: true });
      if (!this.form.modelId) [this.modelLoadPathList, this.form.modelLoadPathDir] = [[], null];
      (this.form.modelId && !this.form.modelResource) && this.modelLoadPath(saveModel);
    },

    async modelLoadPath(create) {
      if (create) {
        this.form.modelLoadPathDir = null;
      };
      const data = await getModelTag({ parentId: this.form.modelId });
      this.modelLoadPathList = data.result;
    },

    onModelResourceChange() {
      this.form.modelId = this.form.modelLoadPathDir = null;
      this.getModelNames();
    },

    onModelTypeChange() {
      if (this.form.modelType === 0 ) {
        this.form = Object.assign(this.form, {
          modelResource: 0,
          modelId: null,
          modelLoadPathDir: null,
        });
      };
    },

    getAlgorithmList() {
      if (this.noMoreLoadAlg || this.algLoading) {
        return;
      }
      this.algLoading = true;
      const params = {
        algorithmSource: Number(this.form.algorithmSource || 1),
        current: this.currentAlgPage,
        size: this.algPageSize,
      };
      getAlgorithmList(params).then(res => {
        this.algorithmIdList = this.algorithmIdList.concat(res.result);
        this.currentAlgPage += 1;
        this.algLoading = false;
        if (res.result.length < this.algPageSize) {
          this.noMoreLoadAlg = true;
        }
        if (this.form.algorithmId) {
          this.selectedAlgorithm = this.algorithmIdList.find(item => item.id === this.form.algorithmId);
          if (!this.selectedAlgorithm) {
            this.$message.warning('原有算法不存在，请重新选择');
            this.form.algorithmId = null;
          }
        }
      }).finally(() => {
        this.algLoading = false;
      });
    },

    onResourcesPoolTypeChange(keepSpec = false) {
      // 当没有显式指定保留节点规格时，选择规格列表第一个选项
      if (keepSpec !== true && this.specList.length) {
        this.form.trainJobSpecsName = this.specList[0].label;
      }
    },
    onTrainDataSourceChange(dataSourceResult) {
      this.form.dataSourceName = dataSourceResult.dataSourceName;
      this.form.dataSourcePath = dataSourceResult.dataSourcePath;
      dataSourceResult.dataSourcePath && this.$refs.trainDataSource.validate('manual');
      // 如果在运行参数中包含了 image_counts 字段，则自动把数据集图片数量填充至该字段。
      this.$refs.runParamComp.updateParam('image_counts', dataSourceResult.imageCounts);
    },
    onVerifyDataSourceChange(result) {
      this.form.valDataSourceName = result.dataSourceName;
      this.form.valDataSourcePath = result.dataSourcePath;
    },
    async onAlgorithmChange(id) {
      // 选用算法变更时，需要对自动填充的表单项进行验证
      this.validateField('algorithmId');
      // 选用算法变更时，需要同步算法的算法用途、运行项目、运行镜像、运行命令、运行参数
      const algorithm = this.algorithmIdList.find(i => i.id === id);
      this.selectedAlgorithm = algorithm;
      this.form.algorithmUsage = algorithm?.algorithmUsage || null;
      this.form.valAlgorithmUsage = algorithm?.algorithmUsage || null;
      this.$refs.trainDataSourceSelector.updateAlgorithmUsage(this.form.algorithmUsage);
      this.form.valType && this.$refs.verifyDataSourceSelector.updateAlgorithmUsage(this.form.valAlgorithmUsage);
      this.form.runCommand = algorithm?.runCommand || '';
      this.form.runParams = algorithm?.runParams || {};
      this.runParamObj = {...this.form.runParams};
      this.form.imageName = algorithm?.imageName;
      this.$nextTick(() => {
        this.clearFieldValidate('runCommand');
      });
      if (this.form.imageName && !this.harborProjectList.some(project => project === this.form.imageName)) {
        this.$message.warning('算法选择的运行项目不存在，请重新选择');
        this.form.imageName = null;
        this.form.imageTag = null;
        return;
      }
      this.form.imageName && await this.getHarborImages(true);
      this.form.imageTag = algorithm?.imageTag;
      if (this.form.imageTag && !this.harborImageList.some(image => image === this.form.imageTag)) {
        this.$message.warning('算法选择的运行镜像不存在，请重新选择');
        this.form.imageTag = null;
        return;
      }
      if (this.form.imageTag) {
        this.validateField('imageTag');
      }
      this.resetProject();
    },
    onDelayChange(isDelay) {
      if (!isDelay) {
        this.form.delayCreateTime = 0;
        this.form.delayDeleteTime = 0;
      }
    },
    onVerifyTypeChange() {
      if (this.form.valType === 0 ) {
        this.form = Object.assign(this.form, {
          valDataSourceName: null,
          valDataSourcePath: null,
        });
        this.$refs.verifyDataSourceSelector.reset();
      } else {
        // 打开训练数据集时获取相应值
        this.$nextTick(() => {
          this.$refs.verifyDataSourceSelector.updateAlgorithmUsage(this.form.valAlgorithmUsage, false);
        });
      }
    },
    async onAlgorithmSourceChange() {
      // 算法类型更改之后，需要清空下方表单
      this.algorithmIdList = [];
      this.currentAlgPage = 1;
      this.noMoreLoadAlg = false;
      this.selectedAlgorithm = null;
      this.form = Object.assign(this.form, {
        algorithmId: null,
        algorithmUsage: null,
        dataSourceName: null,
        dataSourcePath: null,
        valAlgorithmUsage: null,
        valDataSourceName: null,
        valDataSourcePath: null,
        imageTag: null,
        imageName: null,
        runCommand: '',
        resourcesPoolType: 0,
        valType: 0,
        runParams: {},
        modelType: 0,
        modelResource: 0,
        modelId: null,
        modelLoadPathDir: null,
      });
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
    },
    onTrainTypeChange(trainType) {
      this.form.resourcesPoolNode = trainType === 0 ? 1 : 2;
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

.el-radio.is-bordered {
  width: 130px;
  height: 35px;
  padding: 10px 0;
  text-align: center;
}

.param {
  min-height: 80px;
  padding: 0 10px;
  line-height: 25px;
  color: rgb(204, 204, 204);
  background: rgb(30, 30, 30);
  border-radius: 5px;
}
</style>
