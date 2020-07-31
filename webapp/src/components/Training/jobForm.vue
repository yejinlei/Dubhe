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
      class="demo-ruleForm"
      :style="`width: ${widthPercent}%;`"
    >
      <el-form-item v-if="type==='add' || type === 'paramsAdd'" label="任务名称" prop="trainName">
        <el-input v-model="form.trainName" />
      </el-form-item>
      <el-form-item v-if="type==='edit'" label="任务名称" prop="jobName">
        <div>{{ form.jobName }}</div>
      </el-form-item>
      <el-form-item v-if="type==='saveParams' || type==='paramEdit'" label="任务模板名称" prop="paramName">
        <el-input v-model="form.paramName" />
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input v-model="form.description" type="textarea" />
      </el-form-item>
      <hr>
      <!--可编辑-->
      <template v-if="type!=='saveParams'">
        <el-form-item label="选用算法类型" prop="algorithmSource">
          <el-radio-group v-model="form.algorithmSource" @change="onAlgorithmSourceChange">
            <el-radio-button :label="1">我的算法</el-radio-button>
            <el-radio-button :label="2">预置算法</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          ref="algorithmId"
          label="选用算法"
          prop="algorithmId"
        >
          <el-select
            v-model="form.algorithmId"
            v-el-select-load-more="getAlgorithmList"
            placeholder="请选择您使用的算法代码"
            class="w250"
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
            v-model="form.imageName"
            placeholder="请选择镜像"
            style="width: 120px;"
            clearable
            @change="getHarborImages"
          >
            <el-option
              v-for="(item, index) in harborProjectList"
              :key="index"
              :label="item.imageName"
              :value="item.imageName"
            />
          </el-select>
          <el-select
            v-model="form.imageTag"
            placeholder="请选择镜像版本"
            style="width: 336px;"
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
        <el-form-item ref="dataset" label="选用数据集" prop="dataSourcePath">
          <el-select
            v-model="algorithmUsage"
            placeholder="请选择数据集用途"
            @change="getDataSetList"
          >
            <el-option :value="null" label="全部" />
            <el-option
              v-for="item in algorithmUsageList"
              :key="item.id"
              :value="item.auxInfo"
              :label="item.auxInfo"
            />
          </el-select>
          <el-select
            v-model="selectedDataSource"
            placeholder="请选择您挂载的数据集"
            filterable
            value-key="id"
            @change="onDataSourceChange"
          >
            <el-option
              v-for="item in datasetIdList"
              :key="item.id"
              :value="item"
              :label="item.name"
            />
          </el-select>
          <el-select
            v-model="selectedDataSourceVersion"
            placeholder="请选择您挂载的数据集版本"
            value-key="versionUrl"
            @change="onDataSourceVersionChange"
          >
            <el-option
              v-for="(item, index) in datasetVersionList"
              :key="index"
              :value="item"
              :label="item.versionName"
            />
          </el-select>
          <el-tooltip effect="dark" content="请确保代码中包含“data_url”参数用于传输数据集路径" placement="top">
            <i class="el-icon-warning-outline primary f18 v-text-top" />
          </el-tooltip>
          <el-tooltip effect="dark" :disabled="!selectedDataSourceVersion" :content="ofRecordTooltip" placement="top">
            <el-checkbox
              v-model="versionOfRecordUrlChecked"
              :disabled="!versionOfRecordUrlShow"
              @change="onOfRecordUrlChange"
            >使用 OfRecord</el-checkbox>
          </el-tooltip>
        </el-form-item>
        <el-form-item ref="runCommand" label="运行命令" prop="runCommand">
          <el-input
            v-model="form.runCommand"
            placeholder="例如：python mnist.py"
            style="max-width: 500px;"
          />
        </el-form-item>
        <!--运行参数-->
        <run-param-form
          :id="form.$_id"
          ref="runParamComp"
          :runParamObj="form.runParams || {}"
          prop="runParams"
          paramLabelWidth="120px"
          :input1Width="runParamWidth"
          :input2Width="runParamWidth"
          @updateRunParams="updateRunParams"
        />
        <el-form-item label="节点类型" class="is-required">
          <el-radio-group v-model="form.resourcesPoolType" @change="getTrainJobSpecs">
            <el-radio-button :label="0">CPU</el-radio-button>
            <el-radio-button :label="1">GPU</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item ref="trainJobSpecs" label="节点规格" prop="trainJobSpecsId">
          <el-radio-group v-model="form.trainJobSpecsId">
            <el-radio-button
              v-for="spec in specList"
              :key="spec.id"
              :label="spec.id"
              class="mb-10 spec-btn"
            >{{ spec.specsName }}</el-radio-button>
          </el-radio-group>
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
        <el-form-item label="选用数据集">
          {{ form.dataSourceName }}
        </el-form-item>
        <el-form-item label="运行命令">
          {{ form.runCommand }}
        </el-form-item>
        <el-form-item label="运行参数">
          <span v-for="key of Object.keys(form.runParams || {})" :key="key">--{{ key }}={{ form.runParams[key] }} </span>
        </el-form-item>
        <el-form-item label="节点类型">
          {{ form.resourcesPoolType ? 'GPU' : 'CPU' }}
        </el-form-item>
        <el-form-item label="节点规格">
          {{ formSpecs && formSpecs.specsName }}
        </el-form-item>
      </template>
      <el-form-item v-if="showFooterBtns">
        <el-button type="primary" :loading="loading" @click="save">开始训练</el-button>
        <el-button @click="reset">清空</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { validateNameWithHyphen } from '@/utils';
import { list as getAlgorithmList } from '@/api/algorithm/algorithm';
import { getTrainJobSpecs } from '@/api/trainingJob/job';
import { getPublishedDatasets, getDatasetVersions } from '@/api/preparation/dataset';
import { harborProjectNames, harborImageNames } from '@/api/system/harbor';
import { list as getAlgorithmUsages } from '@/api/algorithm/algorithmUsage';
import RunParamForm from '@/components/Training/runParamForm';

export default {
  name: 'JobForm',
  components: { RunParamForm },
  props: {
    form: {
      type: Object,
    },
    type: {
      type: String,
      default: 'add', // add: 新增训练任务; paramsAdd: 任务参数创建训练任务; edit: 修改训练任务; saveParams: 保存训练参数。
    },
    widthPercent: {
      type: Number,
      default: 60,
    },
    runParamWidth: {
      type: Number,
      default: 150,
    },
    showFooterBtns: {
      type: Boolean,
      default: true,
    },
    loading: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
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
        trainJobSpecsId: [
          { required: true, message: '请选择节点规格', trigger: 'change' },
        ],
        runCommand: [
          { required: true, message: '请输入运行命令', trigger: ['blur', 'change'] },
        ],
      },
      selectedDataSource: null,
      selectedDataSourceVersion: null,
      algorithmUsage: null,
      algorithmUsageList: [],
      datasetIdList: [],
      datasetVersionList: [],
      algorithmIdList: [],
      harborProjectList: [],
      harborImageList: [],
      noMoreLoadAlg: false,
      algLoading: false,
      currentAlgPage: 1,
      algPageSize: 1000,
      versionOfRecordUrlShow: false,
      versionOfRecordUrlChecked: false,
      versionOfRecordUrlOptions: {},
      specList: [],
    };
  },
  computed: {
    formSpecs() {
      return this.specList.find(spec => spec.id === this.form.trainJobSpecsId);
    },
    ofRecordTooltip() {
      const content = this.selectedDataSourceVersion?.versionOfRecordUrl
        ? '选中 OfRecord 将使用二进制数据集文件'
        : '二进制数据集文件不可用或正在生成中';
      return content;
    },
  },
  mounted() {
    setTimeout(() => {
      if (this.type !== 'saveParams') {
        this.getAlgorithmList();
        this.getAlgorithmUsages();
        this.getDataSetList(this.form.algorithmUsage, true);
        this.getHarborProjects().then(() => {
          this.resetProject();
        });
      }
      this.getTrainJobSpecs(this.form.resourcesPoolType, this.type !== 'add');
      this.form.runParams = this.form.runParams || {};
    }, 0);
  },
  methods: {
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
      this.form.runParams = params;
    },
    save() {
      if (this.loading) {
        return;
      }
      // 先将字符串模式转换为键值对模式
      if (this.type !== 'saveParams' && this.$refs.runParamComp.paramsMode === 2) {
        this.$refs.runParamComp.convertArgsToPairs();
      }
      const runParamsValid = this.type === 'saveParams' || this.$refs.runParamComp.goValid();
      if (runParamsValid) {
        this.$refs.form.validate(async valid => {
          if (valid) {
            const params = { ...this.form};
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
          message: '请仔细检查任务参数',
          type: 'warning',
        });
      }
    },
    // 镜像项目为空时选择默认项目
    resetProject() {
      if (!this.form.imageName) {
        if (this.harborProjectList.some(project => project.imageName === 'oneflow')) {
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
      this.selectedDataSource = this.selectedDataSourceVersion = this.algorithmUsage = null;
      this.$emit('resetForm', true);
      setTimeout(() => {
        this.getTrainJobSpecs(this.form.resourcesPoolType);
        this.resetProject();
        this.$refs.form.clearValidate();
        this.$refs.runParamComp.reset();
      }, 0);
    },
    async getHarborProjects() {
      this.harborProjectList = await harborProjectNames();
      if (this.form.imageName && !this.harborProjectList.some(project => project.imageName === this.form.imageName)) {
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
    getAlgorithmUsages() {
      const params = {
        isContainDefault: true,
        current: 1,
        size: 1000,
      };
      getAlgorithmUsages(params).then(res => {
        this.algorithmUsageList = res.result;
      });
    },
    async getTrainJobSpecs(resourcesPoolType, keepSpec = false) {
      this.specList = await getTrainJobSpecs({ resourcesPoolType });
      // 接口没有返回规格列表, 则清空所选规格; 规格列表没有当前选项, 则选择规格列表第一个选项
      if (this.specList.length === 0) {
        this.$message.warning('所选节点类型没有现存规格，请重新选择');
        this.form.trainJobSpecsId = null;
      } else if (!keepSpec) {
          this.form.trainJobSpecsId = this.specList[0].id;
        }
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
        if (this.form.algorithmId && !this.algorithmIdList.find(item => item.id === this.form.algorithmId)) {
          this.$message.warning('原有算法不存在，请重新选择');
          this.form.algorithmId = null;
        }
      }).finally(() => {
        this.algLoading = false;
      });
    },
    /**
     * 用于获取数据集列表，init 用于表示是否为修改训练任务初始化
     * @param {String} annotateType
     * @param {Boolean} init
     */
    async getDataSetList(annotateType, init) {
      const params = {
        size: 1000,
        annotateType: annotateType || undefined,
      };
      const data = await getPublishedDatasets(params);
      this.datasetIdList = data.result;
      this.datasetVersionList = [];
      if (init && this.form.dataSourceName) {
        this.selectedDataSource = this.datasetIdList.find(dataset => dataset.name === this.form.dataSourceName.split(':')[0]);
        if (!this.selectedDataSource) {
          this.$message.warning('原有数据集不存在，请重新选择');
          this.form.dataSourceName = this.form.dataSourcePath = null;
          return;
        }
        this.datasetVersionList = await getDatasetVersions(this.selectedDataSource.id);
        this.selectedDataSourceVersion = this.datasetVersionList.find(dataset => dataset.versionUrl === this.form.dataSourcePath);
        // 依次使用 versionUrl 和 versionOfRecordUrl 对带入数据集路径进行匹配
        if (!this.selectedDataSourceVersion) {
          this.selectedDataSourceVersion = this.datasetVersionList.find(dataset => dataset.versionOfRecordUrl === this.form.dataSourcePath);
          if (this.selectedDataSourceVersion) {
            this.versionOfRecordUrlShow = this.versionOfRecordUrlChecked = true;
          }
        }
        if (!this.selectedDataSourceVersion) {
          this.$message.warning('原有数据集版本不存在，请重新选择');
          this.form.dataSourcePath = null;
        }
      } else {
        this.selectedDataSource = this.selectedDataSourceVersion = this.form.dataSourceName = this.form.dataSourcePath = null;
      }
    },
    async onDataSourceChange(dataSource) {
      // 数据集选项发生变化时，清空数据集版本、路径、OfRecord 相关信息，同时获取版本列表
      this.form.dataSourceName = dataSource.name;
      this.form.dataSourcePath = null;
      this.selectedDataSourceVersion = null;
      this.versionOfRecordUrlOptions = null;
      this.versionOfRecordUrlShow = false;
      this.versionOfRecordUrlChecked = false;
      this.datasetVersionList = await getDatasetVersions(dataSource.id);
    },
    onDataSourceVersionChange(version) {
      // 选择数据集版本后，如果存在 OfRecordUrl，则默认勾选使用，否则禁用选择
      this.form.dataSourceName = `${this.selectedDataSource.name  }:${  version.versionName}`;
      const { versionUrl, versionOfRecordUrl } = version;
      this.versionOfRecordUrlShow = Boolean(version.versionOfRecordUrl);
      this.versionOfRecordUrlChecked = Boolean(version.versionOfRecordUrl);
      this.versionOfRecordUrlOptions = {
        versionUrl, versionOfRecordUrl,
      };
      this.form.dataSourcePath = this.versionOfRecordUrlChecked ? versionOfRecordUrl : versionUrl;
      this.$refs.dataset.validate('manual');
      // 如果在运行参数中包含了 image_counts 字段，则自动把数据集图片数量填充至该字段。
      if (this.form.runParams?.image_counts !== undefined) {
        this.form.runParams.image_counts = version.imageCounts;
        this.$refs.runParamComp.syncListData();
      }
    },
    onOfRecordUrlChange(ofRecord) {
      const { versionUrl, versionOfRecordUrl } = this.versionOfRecordUrlOptions;
      this.form.dataSourcePath = ofRecord ? versionOfRecordUrl : versionUrl;
    },
    async onAlgorithmChange(id) {
      // 选用算法变更时，需要对自动填充的表单项进行验证
      this.validateField('algorithmId');
      // 选用算法变更时，需要同步算法的算法用途、运行项目、运行镜像、运行命令、运行参数
      const algorithm = this.algorithmIdList.find(i => i.id === id);
      this.algorithmUsage = algorithm?.algorithmUsage || null;
      this.getDataSetList(this.algorithmUsage);
      this.form.runCommand = algorithm?.runCommand || null;
      this.form.runParams = algorithm?.runParams || {};
      this.form.imageName = algorithm?.imageName;
      if (this.form.imageName && !this.harborProjectList.some(project => project.imageName === this.form.imageName)) {
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
    async onAlgorithmSourceChange() {
      // 算法类型更改之后，需要清空下方表单
      this.algorithmIdList = [];
      this.currentAlgPage = 1;
      this.noMoreLoadAlg = false;
      this.getAlgorithmList();
      this.form = Object.assign(this.form, {
        algorithmId: null,
        dataSourceName: null,
        dataSourcePath: null,
        imageTag: null,
        imageName: null,
        runCommand: null,
        resourcesPoolType: 0,
        runParams: {},
      });
      this.$nextTick(() => {
        this.clearFieldValidate('runCommand');
        this.clearFieldValidate('trainJobSpecs');
      });
      this.algorithmUsage = this.selectedDataSource = this.selectedDataSourceVersion = null;
      this.$refs.runParamComp.reset();
      this.harborImageList = this.datasetVersionList = [];
      this.resetProject();
      this.getAlgorithmUsages();
      this.getTrainJobSpecs(this.form.resourcesPoolType);
    },
  },
};
</script>

<style lang="scss" scoped>
::v-deep.w250 {
  .el-input {
    width: 250px;
  }
}
</style>
<style lang="scss"> // 若使用 scoped，带有属性的样式权重过高会影响正常样式
@import '@/assets/styles/variables.scss';
// el-radio-button 被换行时，第二行左侧没有边框，需要添加样式
.spec-btn {
  .el-radio-button__inner {
    border-left: solid 1px $borderColorBase;
  }

  .el-radio-button__orig-radio:hover + .el-radio-button__inner,
  .el-radio-button__orig-radio:checked + .el-radio-button__inner {
    border-left-color: transparent;
  }

  &:first-child {
    .el-radio-button__orig-radio:hover + .el-radio-button__inner,
    .el-radio-button__orig-radio:checked + .el-radio-button__inner {
      border-left-color: $primaryBorderColor;
    }
  }
}
</style>
