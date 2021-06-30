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
  <el-form ref="form" :model="form" :rules="rules" label-width="100px">
    <el-form-item label="模型名称" prop="name">
      <el-input
        v-model.trim="form.name"
        style="width: 300px;"
        maxlength="32"
        placeholder="请输入模型名称"
        show-word-limit
      />
    </el-form-item>
    <el-form-item label="框架" prop="frameType">
      <el-select
        v-model="form.frameType"
        placeholder="请选择框架"
        style="width: 400px;"
        :disabled="isAtlas"
        filterable
        @change="onFrameTypeChange"
      >
        <el-option
          v-for="item in dict.frame_type"
          :key="item.value"
          :value="item.value"
          :label="item.label"
        />
      </el-select>
      <el-tooltip
        v-if="isAtlas"
        content="模型炼知暂只支持 Pytorch 框架的模型"
        effect="dark"
        placement="top"
        class="model-uploader-tooltip"
      >
        <i class="el-icon-warning-outline primary f18 v-text-top" />
      </el-tooltip>
    </el-form-item>
    <el-form-item label="模型格式" prop="modelType">
      <el-select
        v-model="form.modelType"
        placeholder="请选择模型格式"
        style="width: 400px;"
        :disabled="isAtlas"
        filterable
      >
        <el-option
          v-for="item in modelTypeList"
          :key="item.value"
          :value="item.value"
          :label="item.label"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="模型类别" prop="modelClassName">
      <el-select
        v-model="form.modelClassName"
        placeholder="请选择或输入模型类别"
        filterable
        allow-create
        style="width: 400px;"
        @change="onAlgorithmUsageChange"
      >
        <el-option
          v-for="item in algorithmUsageList"
          :key="item.id"
          :label="item.auxInfo"
          :value="item.auxInfo"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="模型描述" prop="modelDescription">
      <el-input
        v-model="form.modelDescription"
        type="textarea"
        placeholder="请输入模型描述"
        maxlength="255"
        show-word-limit
        style="width: 500px;"
      />
    </el-form-item>
    <!-- 上传模型炼知表单时，直接在表单中进行模型上传 -->
    <template v-if="isAtlas">
      <el-form-item ref="modelAddress" label="模型上传" prop="modelAddress">
        <ModelUploader :type="type" v-on="$listeners" @modelAddressChange="onModelAddressChange" />
      </el-form-item>
    </template>
  </el-form>
</template>

<script>
import {
  list as getAlgorithmUsages,
  add as addAlgorithmUsage,
} from '@/api/algorithm/algorithmUsage';
import { validateNameWithHyphen, MODEL_RESOURCE_ENUM } from '@/utils';
import { getModelTypeMap } from '@/api/model/model';

import ModelUploader from './modelUploader';
import { atlasFrameTypeList, atlasModelTypeList } from '../util';

const defaultForm = {
  name: null,
  frameType: null,
  modelType: null, // 模型格式
  modelClassName: null, // 模型类别（用途）
  modelDescription: null,
  modelAddress: null, // 创建炼知模型时需要传递
  modelResource: null, // 创建炼知模型时需要传递
};

export default {
  name: 'CreateModelForm',
  dicts: ['model_type', 'frame_type'],
  components: {
    ModelUploader,
  },
  props: {
    type: {
      type: String,
      default: 'Custom',
    },
  },
  data() {
    return {
      algorithmUsageList: [],

      form: { ...defaultForm },
      rules: {
        name: [
          { required: true, message: '请输入模型名称', trigger: 'blur' },
          { max: 32, message: '长度在32个字符以内', trigger: 'blur' },
          {
            validator: validateNameWithHyphen,
            trigger: ['blur', 'change'],
          },
        ],
        frameType: [{ required: true, message: '请选择模型框架', trigger: 'blur' }],
        modelType: [{ required: true, message: '请选择模型格式', trigger: 'blur' }],
        modelClassName: [
          { required: true, message: '请输入模型类别', trigger: ['blur', 'change'] },
        ],
        modelDescription: [{ max: 255, message: '长度在255个字符以内', trigger: 'blur' }],
        modelAddress: [
          { required: true, message: '请上传有效的模型', trigger: ['blur', 'manual'] },
        ],
      },
      modelTypeMap: {},
    };
  },
  computed: {
    isAtlas() {
      return this.type === 'Atlas';
    },
    modelTypeList() {
      if (!this.form.frameType || !this.modelTypeMap[this.form.frameType]) {
        return this.dict.model_type;
      }
      return this.dict.model_type.filter((type) =>
        this.modelTypeMap[this.form.frameType].includes(+type.value)
      );
    },
  },
  created() {
    this.getAlgorithmUsages();
    this.getModelTypeMap();

    if (this.isAtlas) {
      // 炼知模型使用默认值，目前只支持 Pytorch
      this.form.frameType = atlasFrameTypeList[0].value;
      this.form.modelType = atlasModelTypeList[0].value;
    }
  },
  methods: {
    // form functions
    validate(resolve, reject) {
      let valid = true;
      this.$refs.form.validate((isValid) => {
        valid = valid && isValid;
      });
      if (valid) {
        this.form.modelResource = this.isAtlas
          ? MODEL_RESOURCE_ENUM.ATLAS
          : MODEL_RESOURCE_ENUM.CUSTOM;
        if (typeof resolve === 'function') {
          return resolve(this.form);
        }
        return true;
      }
      if (typeof reject === 'function') {
        return reject(this.form);
      }
      return false;
    },
    reset() {
      this.form = { ...defaultForm };
      this.$nextTick(() => {
        this.$refs.form.clearValidate();
      });
    },

    getAlgorithmUsages() {
      const params = {
        isContainDefault: true,
        current: 1,
        size: 1000,
      };
      getAlgorithmUsages(params).then((res) => {
        this.algorithmUsageList = res.result;
      });
    },
    async createAlgorithmUsage(auxInfo) {
      await addAlgorithmUsage({ auxInfo });
      this.getAlgorithmUsages();
    },
    onAlgorithmUsageChange(value) {
      const usage = this.algorithmUsageList.find((usage) => usage.auxInfo === value);
      if (!usage) {
        this.createAlgorithmUsage(value);
      }
    },
    onModelAddressChange(modelAddress) {
      this.form.modelAddress = modelAddress;
      this.$refs.modelAddress.validate('manual');
    },

    // 获取模型框架 —— 模型格式匹配关系
    async getModelTypeMap() {
      this.modelTypeMap = await getModelTypeMap();
    },

    // 模型框架
    onFrameTypeChange() {
      this.form.modelType = null;
    },
  },
};
</script>
