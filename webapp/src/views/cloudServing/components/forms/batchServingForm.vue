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
    label-width="120px"
    class="model-config-wrapper"
  >
    <BatchUploadDialog
      ref="imgUploadDialog"
      action="fackApi"
      :params="uploadParams"
      :limit="5000"
      :hash="false"
      :filters="uploadFilters"
      @upload-success="onUploadSuccess"
      @close="onUploadClose"
    />
    <el-form-item label="服务名称" prop="name">
      <el-input
        ref="nameInput"
        v-model.trim="form.name"
        maxlength="32"
        show-word-limit
      />
    </el-form-item>
    <el-form-item label="服务描述" prop="description">
      <el-input
        v-model="form.description"
        type="textarea"
        :rows="4"
        maxlength="200"
        show-word-limit
      />
    </el-form-item>
    <el-divider />
    <el-form-item
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
        placeholder="请选择模型"
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
        placeholder="请选择模型版本"
        @change="onModelVersionChange"
      >
        <el-option
          v-for="version in modelVersionList"
          :key="version.id"
          :value="version.modelAddress"
          :label="version.versionNum"
        />
      </el-select>
    </el-form-item>
    <el-form-item
      prop="inputPath"
      label="上传预测图片"
      class="is-required"
      :error="inputPathErrorMsg"
    >
      <el-button @click="onUploadDialogClick">上传图片</el-button>
      <el-tooltip
        v-if="isFork"
        class="item"
        effect="dark"
        content="Fork 服务时，无需重新上传图片"
        placement="right"
      >
        <i class="el-icon-warning-outline primary f18 vm" />
      </el-tooltip>
    </el-form-item>
    <el-form-item label="节点类型" prop="resourcesPoolType">
      <el-radio-group
        v-model="form.resourcesPoolType"
        @change="onNodeTypeChange"
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
      label="计算节点个数"
    >
      <el-input-number
        v-model="form.resourcesPoolNode"
        :min="1"
        :max="10"
        class="w-200"
        step-strictly
      />
    </el-form-item>
    <el-form-item
      v-for="param in deployParamList"
      :key="param.label"
      :label="param.label"
      :prop="param.label"
    >
      <el-input
        v-model="form.deployParams[param.label]"
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
  </el-form>
</template>

<script>
import { getUniqueId, invalidFileNameChar } from '@/utils';
import { getModelByResource } from '@/api/model/model';
import { list as getModelVersions } from '@/api/model/modelVersion';
import { validateNameWithHyphen } from '@/utils/validate';

import BatchUploadDialog from './batchUploadDialog';

const defaultForm = {
  id: null,
  name: null,
  description: null,
  modelResource: 0, // 模型类型
  modelId: null,
  modelAddress: null,
  inputPath: null, // 图片目录
  resourcesPoolType: 0, // 节点类型
  resourcesPoolSpecs: null, // 节点规格
  resourcesPoolNode: 1, // 节点数
  deployParams: {}, // 部署动态参数
};

export default {
  name: 'BatchServingForm',
  dicts: ['cpu_specs', 'gpu_specs', 'frame_type', 'deploy_params'],
  components: { BatchUploadDialog },
  data() {
    return {
      formType: 'add',
      modelList: [],
      modelVersionList: [],
      
      uploadVisiable: false,
      uploaded: false,
      inputPathValid: true,

      dictOrFormReady: false,

      form: { ...defaultForm },

      rules: {
        name: [
          {
            required: true,
            message: '请输入服务名称',
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
      uploadFilters: [invalidFileNameChar],
    };
  },
  computed: {
    isAdd() {
      return this.formType === 'add';
    },
    isFork() {
      return this.formType === 'fork';
    },
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
    inputPathErrorMsg() {
      return this.inputPathValid ? null : '请先上传图片';
    },
    uploadParams() {
      return {
        objectPath: this.form.inputPath,
      };
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
  created() {
    this.$on('dictReady', () => {
      if (!this.form.resourcesPoolSpecs) {
        this.onNodeTypeChange();
      }
      this.setDeployParams();
    });
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
          this.form.modelAddress = null;
          this.$message.warning('原有模型版本不存在，请重新选择');
        }
      }
    },

    // Handlers
    onModelResourceChange(modelResource) {
      this.form.modelId = this.form.modelAddress = null;
      this.getModels(modelResource);
    },
    onModelChange(modelId) {
      this.form.modelId = modelId;
      if (this.isPresetModel) {
        const model = this.modelList.find(model => model.id === modelId);
        this.form.modelAddress = model.url;
        this.$refs.modelAddress.validate('manual');
      } else {
        this.getModelVersions(modelId);
        this.form.modelAddress = null;
      }
    },
    onModelVersionChange(modelAddress) {
      this.form.modelAddress = modelAddress;
      this.$refs.modelAddress.validate('manual');
    },
    onUploadSuccess() {
      this.uploaded = true;
      this.inputPathValid = true;
    },
    onUploadClose() {
      this.inputPathValid = !this.isAdd || this.uploaded;
    },
    onNodeTypeChange() {
      if (this.specsList.length) {
        // 默认选择第一个节点
        this.form.resourcesPoolSpecs = this.specsList[0].label;
      }
    },
    async validate(resolve, reject) {
      let valid = true;
      const validCallback = isValid => {
        valid = valid && isValid;
      };
      // el-form 表单校验
      this.$refs.form.validate(validCallback);
      // 自定义校验
      // 创建批量服务时，必须要求先上传图片
      this.inputPathValid = !this.isAdd || this.uploaded;
      valid = valid && this.inputPathValid;
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

    initForm(originForm, formType = 'add') {
      // 获取初始表单对象或空对象作为初始表单
      const form = originForm || Object.create(null);
      // formType 赋值
      this.formType = formType;

      // 根据表单的字段，将初始表单的对应字段赋值到表单上，若字段不存在则使用默认值
      Object.keys(this.form).forEach(key => { form[key] && (this.form[key] = form[key]); });
      // deployParam 变量单独赋值，如果初始表单不包含这个字段，则赋值空对象，避免对象引用问题
      this.form.deployParams = form.deployParams || Object.create(null);
      this.setDeployParams();

      // 如果是 fork 表单则 name 增加后缀
      formType === 'fork' && (this.form.name += '-Fork');
      this.getModels(this.form.modelResource, true);
      // 如果 inputPath 不存在，则更新 inputPath
      if (!this.form.inputPath) {
        this.updateObjectPath();
      }
      
      // 渲染完成后清空表单验证，避免初始值导致表单错误提示
      this.$nextTick(() => {
        this.$refs.form.clearValidate();
      });
    },
    resetForm() {
      this.form = { ...defaultForm };
      // 清空 deployParams
      this.form.deployParams = Object.create(null);
      this.$nextTick(() => {
        this.clearValidate();
      });
    },
    setDeployParams() {
      // 在 dictReady 和 initForm 两个地方调用，第二次调用时数据准备完毕，可以开始处理 deployParams
      if (!this.dictOrFormReady) {
        this.dictOrFormReady = true;
        return;
      }
      const deployParam = Object.create(null);
      // 根据 deployParamList 中的参数列表，取得对应字段的值或初始值
      // 用于避免修改、Fork 服务时，详情返回的对象字段与最新的字典接口字段不一致，以字段字段为准
      this.deployParamList.forEach(param => {
        deployParam[param.label] = this.form.deployParams[param.label] || null;
      });
      this.form.deployParams = deployParam;
    },
    clearValidate(...args) {
      return this.$refs.form.clearValidate.apply(this, args);
    },
    updateObjectPath() {
      this.form.inputPath = `serving/input/${this.user.id}/${getUniqueId()}`;
    },
    onUploadDialogClick() {
      this.$refs.imgUploadDialog.show();
    },
  },
};
</script>
