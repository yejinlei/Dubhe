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
  <el-form
    ref="form"
    :model="form"
    :rules="rules"
    :label-width="labelWidth"
    class="model-config-wrapper"
  >
    <el-row>
      <div class="model-config-column">
        <el-form-item
          v-if="!modelDisabled"
          label="模型类型"
          prop="modelResource"
        >
          <el-radio-group
            v-model="form.modelResource"
            @change="onModelResourceChange"
          >
            <el-radio
              border
              :label="0"
              class="mr-0 w-150"
            >我的模型</el-radio>
            <el-radio
              border
              :label="1"
              class="w-150"
            >预训练模型</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          ref="modelAddress"
          prop="modelAddress"
          label="模型"
        >
          <el-select
            v-model="form.modelId"
            class="w-200"
            placeholder="请选择模型"
            :disabled="modelDisabled"
            filterable
            @change="onModelChange"
          >
            <el-option
              v-for="model in modelList"
              :key="model.id"
              :value="model.id"
              :label="`${model.name} (${dict.label.frame_type[model.frameType]})`"
            />
          </el-select>
          <el-select
            v-if="!isPresetModel"
            v-model="form.modelAddress"
            class="w-200"
            placeholder="请选择模型版本"
            @change="onModelVersionChange"
          >
            <el-option
              v-for="version in modelVersionList"
              :key="version.id"
              :value="version.modelAddress"
              :label="version.versionNum"
              :disabled="modelAddressList.indexOf(version.modelAddress) !== -1"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          prop="releaseRate"
          label="灰度发布分流(%)"
          :error="releaseRateErrMsg"
        >
          <el-input-number
            v-model="form.releaseRate"
            :min="0"
            :max="100"
            class="w-200"
            step-strictly
            :disabled="releaseRateDisabled"
            @change="onReleaseRateChange"
          />
        </el-form-item>
        <el-form-item
          v-for="param in deployParamList"
          :key="param.label"
          :label="param.label"
          :prop="param.label"
        >
          <el-input
            v-model="deployParams[param.label]"
            class="w-200"
          />
          <el-tooltip
            effect="dark"
            :content="param.description"
            placement="right"
          >
            <i class="el-icon-warning-outline primary f18 vm" />
          </el-tooltip>
        </el-form-item>
      </div>
      <div class="model-config-column">
        <el-form-item label="节点类型" prop="resourcesPoolType">
          <el-radio-group
            v-model="form.resourcesPoolType"
            @change="onResourcesPoolTypeChange"
          >
            <el-radio
              border
              :label="0"
              class="mr-0 w-150"
            >CPU</el-radio>
            <el-radio
              border
              :label="1"
              class="w-150"
            >GPU</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="节点规格" prop="resourcesPoolSpecs">
          <el-select
            v-model="form.resourcesPoolSpecs"
            placeholder="请选择节点规格"
            class="w-200"
          >
            <el-option
              v-for="specs in specsList"
              :key="specs.id"
              :label="specs.label"
              :value="specs.label"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          prop="resourcesPoolNode"
          label="节点数"
          :error="poolNodeErrMsg"
        >
          <el-input-number
            v-model="form.resourcesPoolNode"
            :min="1"
            :max="10"
            class="w-200"
            step-strictly
            @change="onPoolNodeChange"
          />
        </el-form-item>
      </div>
    </el-row>
    <i
      v-if="deletable"
      class="el-icon-delete config-delete cp"
      @click="$emit('delete', config.id)"
    />
  </el-form>
</template>

<script>
import { getModelByResource } from '@/api/model/model';
import { list as getModelVersions } from '@/api/model/modelVersion';

import { servingConfig } from '@/config';

const SERVING_ADDERSS_REG = /^\/serving\//;

export default {
  name: 'ServingModelConfig',
  dicts: ['cpu_specs', 'gpu_specs', 'frame_type', 'deploy_params'],
  props: {
    config: {
      type: Object,
      required: true,
    },
    labelWidth: {
      type: String,
      default: null,
    },
    deletable: {
      type: Boolean,
      default: true,
    },
    modelDisabled: {
      type: Boolean,
      default: false,
    },
    modelAddressMap: {
      type: Object,
      default: () => ({}),
    },
    releaseRateDisabled: {
      type: Boolean,
      default: true,
    },
    releaseRateSumValid: {
      type: Boolean,
      default: true,
    },
    nodeCountValid: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    return {
      modelList: [],
      modelVersionList: [],
      modelAddressList: [], // 记录其他模型版本选择的模型地址

      dictOrFormReady: false,

      form: {
        modelResource: 0, // 模型类型
        modelId: null,
        modelAddress: null,
        resourcesPoolType: 0, // 节点类型
        resourcesPoolSpecs: null, // 节点规格
        resourcesPoolNode: 1, // 节点数
        releaseRate: 100, // 灰度发布分流
        deployParams: '{}', // 部署动态参数 JSON 字符串
      },
      deployParams: {}, // 本地维护动态参数对象

      rules: {
        modelAddress: [{
          required: true,
          message: '请选择模型及版本',
          trigger: 'manual',
        }],
        modelResource: [{
          required: true,
          message: '请选择模型类型',
          trigger: 'change',
        }],
        resourcesPoolType: [{
          required: true,
          message: '请选择节点类型',
          trigger: 'change',
        }],
        resourcesPoolSpecs: [{
          required: true,
          message: '请选择节点规格',
          trigger: 'change',
        }],
      },
    };
  },
  computed: {
    isPresetModel() {
      return this.form.modelResource === 1;
    },
    specsList() {
      switch (this.form.resourcesPoolType) {
        case 0:
          return this.dict.cpu_specs;
        case 1:
          return this.dict.gpu_specs;
        default:
          return [];
      }
    },
    releaseRateZeroValid() {
      return this.form.releaseRate !== 0;
    },
    releaseRateErrMsg() {
      if (!this.releaseRateSumValid) {
        return '灰度分流总和不为 100%，请检查';
      }
      if (!this.releaseRateZeroValid) {
        return '灰度分流率不能为 0';
      }
      return null;
    },
    poolNodeErrMsg() {
      return this.nodeCountValid ? '' : `模型节点总数不能超过${servingConfig.onlineServingNodeSumMax}个`;
    },
    deployParamList() {
      const { deploy_params } = this.dict;
      if (!deploy_params) return [];
      return deploy_params.map(param => {
        const { label, value } = param;
        return { label, description: value };
      });
    },
    user() {
      return this.$store.getters.user;
    },
  },
  watch: {
    modelAddressMap: {
      handler(map) {
        this.modelAddressList = [];
        for (const key of Object.keys(map)) {
          if (key !== this.form.id) {
            this.modelAddressList.push(map[key]);
          }
        }
      },
      deep: true,
      immediate: true,
    },
  },
  created() {
    // 只在组件被创建时，将 config 同步到内部 form 上
    Object.assign(this.form, this.config);
    this.$on('dictReady', () => {
      if (!this.form.resourcesPoolSpecs) {
        this.onResourcesPoolTypeChange();
      }
      this.setDeployParams();
    });
    this.setDeployParams();
    this.getModels(this.form.modelResource, true);
  },
  methods: {
    // Getters
    async getModels(modelResource, keepValue = false) {
      this.modelList = await getModelByResource(modelResource);

      // 修改时需要保留原服务的模型
      if (!keepValue || !this.form.modelId) {
        this.form.modelAddress = null;
      } else {
        const model = this.modelList.find(model => model.id === this.form.modelId);
        if (!model) {
          this.$message.warning('原有模型不存在，请重新选择');
          this.form.modelId = this.form.modelAddress = null;
          return;
        }
        if (modelResource === 0) {
          this.getModelVersions(model.id, true);
        }
      }
    },
    async getModelVersions(parentId, keepValue = false) {
      const data = await getModelVersions({ parentId, current: 1, size: 500});
      this.modelVersionList = data.result;

      // 修改时需要保留原服务的模型版本
      if (keepValue && this.form.modelAddress) {
        const version = this.modelVersionList.find(version => version.modelAddress === this.form.modelAddress);
        if (!version) {
          if (this.form.modelAddress.match(SERVING_ADDERSS_REG)) {
            this.$message.warning('原模型为回滚备份模型，请选择模型');
          } else {
            this.$message.warning('原有模型版本不存在，请重新选择');
          }
          this.form.modelAddress = null;
        }
      }
    },
    getSpecs() {
      if (!this.form.resourcesPoolSpecs && this.specsList.length) {
        this.form.resourcesPoolSpecs = this.specsList[0].id;
      }
    },

    // Handlers
    onModelResourceChange(modelResource) {
      this.form.modelId = this.form.modelAddress = null;
      this.getModels(modelResource);
      this.$emit('model-resource-change', modelResource);
    },
    onModelChange(modelId, emitEvent = true) {
      this.form.modelId = modelId;
      if (this.isPresetModel) {
        const model = this.modelList.find(model => model.id === modelId);
        this.form.modelAddress = model.url;
      } else {
        this.getModelVersions(modelId);
        this.form.modelAddress = null;
      }
      // 对于模型的更改需要即时同步到外部，用于添加模型版本
      this.$emit('change', this.form);
      emitEvent && this.$emit('model-change', modelId);
    },
    onModelVersionChange(modelAddress) {
      this.form.modelAddress = modelAddress;
      this.$refs.modelAddress.validate('manual');
      // 对于模型版本的更改需要即时同步到外部，用于禁用模型版本选项
      this.$emit('model-version-change', modelAddress);
    },
    onResourcesPoolTypeChange() {
      if (this.specsList.length) {
        // 默认选择第一个节点
        this.form.resourcesPoolSpecs = this.specsList[0].label;
      }
    },
    onPoolNodeChange() {
      this.$emit('change', this.form);
    },
    onReleaseRateChange(rate) {
      this.$emit('release-rate-change', rate);
    },

    updateModel(modelId) {
      this.form.modelId = modelId;
      this.onModelChange(modelId, false);
    },
    updateReleaseRate(rate) {
      this.form.releaseRate = rate;
      this.$emit('change', this.form);
    },
    setDeployParams() {
      // 在 dictReady 和 initForm 两个地方调用，第二次调用时数据准备完毕，可以开始处理 deployParams
      if (!this.dictOrFormReady) {
        this.dictOrFormReady = true;
        return;
      }
      const deployParams = JSON.parse(this.form.deployParams);
      this.deployParams = Object.create(null);
      this.deployParamList.forEach(param => {
        this.$set(this.deployParams, param.label, deployParams[param.label] || null);
      });
    },
    // 外部调用进行表单验证
    validate(resolve, reject) {
      // 表单验证时将 deployParams 更新到 form 上
      this.form.deployParams = JSON.stringify(this.deployParams);
      // 在表单验证时向外传递数据
      this.$emit('change', this.form);
      let valid = true;
      const validCallback = isValid => {
        valid = valid && isValid;
      };
      // el-form 表单校验
      this.$refs.form.validate(validCallback);
      valid = valid && this.releaseRateZeroValid && this.releaseRateSumValid;
      if (valid) {
        // 提交表单时，在表单中增加 节点规格 的 JSON 配置
        const selectedSpecs = this.specsList.find(specs => specs.label === this.form.resourcesPoolSpecs);
        if (selectedSpecs) {
          this.form.poolSpecsInfo = selectedSpecs.value;
        }
        resolve && resolve(this.form);
      } else {
        reject && reject();
      }
    },
    clearValidate(...args) {
      return this.$refs.form.clearValidate.apply(this, args);
    },
  },
};
</script>

<style lang="scss" scoped>
.model-config-wrapper {
  position: relative;
  padding: 10px 0;
  margin: 10px 0;
}

.config-delete {
  position: absolute;
  top: -10px;
  right: 0;
}

@media only screen and (min-width: 1500px) {
  .model-config-column {
    float: left;
    width: 50%;
  }
}
</style>
