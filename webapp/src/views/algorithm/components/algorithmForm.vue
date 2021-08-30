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
  <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
    <div v-if="isFork" class="ts-tip fork-tip">
      以下操作将按照预置算法的配置生成一份您的算法，进而可以做后续配置。
    </div>
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
    <el-form-item v-if="isAdmin && !isFork && !isServing" label="算法来源" prop="algorithmSource">
      <el-radio-group v-model="form.algorithmSource" @change="onAlgorithmSourceChange">
        <el-radio :label="ALGORITHM_RESOURCE_ENUM.CUSTOM" border class="mr-0">我的算法</el-radio>
        <el-radio :label="ALGORITHM_RESOURCE_ENUM.PRESET" border>预置算法</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item v-show="!isFork && !isServing" label="支持推理" prop="inference">
      <el-switch id="inference" v-model="form.inference" @change="onInferenceChange" />
    </el-form-item>
    <el-form-item v-show="!form.inference" label="模型类别" prop="algorithmUsage">
      <el-select
        id="algorithmUsage"
        v-model="form.algorithmUsage"
        placeholder="请选择或输入模型类别"
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
            ><i class="el-icon-close"
          /></el-button>
        </el-option>
      </el-select>
    </el-form-item>
    <el-form-item v-show="formType !== 'fork'" ref="codeDirRef" label="上传代码包" prop="codeDir">
      <upload-inline
        ref="uploadRef"
        action="fakeApi"
        :accept="form.inference ? '.py' : '.zip'"
        :acceptSize="algorithmConfig.uploadFileAcceptSize"
        :acceptSizeFormat="uploadSizeFomatter"
        list-type="text"
        :show-file-count="false"
        :params="uploadParams"
        :auto-upload="true"
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
        :color="defaultProcessColors"
        :status="status"
        :size="size"
        @onSetProgress="onSetProgress"
      />
    </el-form-item>
    <template v-if="form.algorithmSource === ALGORITHM_RESOURCE_ENUM.PRESET && !form.inference">
      <el-form-item ref="imageTag" label="镜像选择" prop="imageTag">
        <el-select
          id="imageName"
          v-model="form.imageName"
          placeholder="请选择镜像"
          class="w-200"
          clearable
          filterable
          @change="onImageNameChange"
        >
          <el-option v-for="item in imageNameList" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select
          id="imageTag"
          v-model="form.imageTag"
          placeholder="请选择镜像版本"
          class="w-200"
          clearable
          filterable
        >
          <el-option
            v-for="(item, index) in imageTagList"
            :key="index"
            :label="item.imageTag"
            :value="item.imageTag"
          />
        </el-select>
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
      <RunParamForm
        ref="runParamCompRef"
        :run-param-obj="form.runParams || {}"
        prop="runParams"
        param-label-width="120px"
        class="w120"
        @updateRunParams="updateRunParams"
      />
    </template>
    <el-form-item v-show="!form.inference" label="文件输出" prop="isTrainOut">
      <el-switch id="isTrainOut" v-model="form.isTrainOut" />
    </el-form-item>
    <el-form-item v-show="!form.inference" label="可视化日志" prop="isVisualizedLog">
      <el-switch id="isVisualizedLog" v-model="form.isVisualizedLog" />
    </el-form-item>
    <div class="ts-tip">
      <ol v-show="!form.inference">
        <li>请确保代码中包含“train_model_out”参数用于接收训练的模型输出路径</li>
        <li>如需传入训练数据集，请确保代码中包含“data_url”参数用于传输训练数据集路径</li>
        <li>如需传入验证数据集，请确保代码中包含“val_data_url”参数用于传输验证数据集路径</li>
        <li>
          如需断点续训或加载已有模型，请确保代码中包含“model_load_dir”参数用于接收训练模型路径
        </li>
        <li>如需文件输出，请确保代码中包含“train_out”参数用于接收文件输出路径</li>
        <li>
          如需训练过程可视化，请确保代码中包含“train_visualized_log”参数用于接收训练的可视化日志路径，目前仅支持
          OneFlow 框架
        </li>
        <li>
          如需使用分布式训练，请确保代码中包含“num_nodes”参数和“node_ips”参数用于接收分布式相关参数
        </li>
        <li>
          如需使用 GPU
          训练，请确保代码中包含“gpu_num_per_node”参数接收相关参数，后台将自动获取并填充
        </li>
      </ol>
      <span v-show="form.inference">
        推理脚本模板详见<el-link type="primary" target="_blank" :href="docUrl">文档</el-link>
      </span>
    </div>
  </el-form>
</template>

<script>
import { computed, reactive, ref, toRefs } from '@vue/composition-api';
import { Message } from 'element-ui';

import {
  validateNameWithHyphen,
  uploadSizeFomatter,
  getUniqueId,
  invalidFileNameChar,
  defaultProcessColors,
  ALGORITHM_RESOURCE_ENUM,
} from '@/utils';
import UploadInline from '@/components/UploadForm/inline';
import UploadProgress from '@/components/UploadProgress';
import RunParamForm from '@/components/Training/runParamForm';
import { useMapGetters } from '@/hooks';
import { algorithmConfig } from '@/config';
import {
  list as getAlgoUsages,
  add as addAlgoUsage,
  del as deleteAlgoUsage,
} from '@/api/algorithm/algorithmUsage';
import { getImageNameList, getImageTagList } from '@/api/trainingImage';
import { IMAGE_PROJECT_TYPE } from '@/views/trainingJob/utils';

const defaultForm = {
  id: null,
  algorithmName: null,
  algorithmSource: ALGORITHM_RESOURCE_ENUM.CUSTOM,
  algorithmUsage: null,
  imageName: null, // 预置镜像名
  imageTag: null, // 预置镜像版本
  description: null,
  codeDir: null,
  runCommand: null,
  runParams: {},
  accuracy: null,
  p4InferenceSpeed: null,
  isTrainOut: true,
  isTrainModelOut: true,
  isVisualizedLog: true,
  inference: false,
  fork: false,
};

export default {
  name: 'AlgorithmForm',
  components: {
    UploadInline,
    UploadProgress,
    RunParamForm,
  },
  props: {
    formType: {
      type: String,
      default: 'add', // 创建-add / Fork-fork / Serving 上传推理脚本-serving
    },
  },
  setup(props) {
    // 状态
    const state = reactive({
      algorithmUsageList: [], // 算法用途列表
      imageNameList: [], // 镜像名列表
      imageTagList: [], // 镜像版本列表
      uploading: false, // 上传算法 Loading
      size: 0, // 文件大小
      progress: 0, // 虚拟进度
      uploadParams: {
        objectPath: null, // 对象存储路径
      },
    });
    // store
    const { user, isAdmin } = useMapGetters(['user', 'isAdmin']);
    // computed
    const isFork = computed(() => props.formType === 'fork');
    const isServing = computed(() => props.formType === 'serving');
    const status = computed(() => (state.progress === 100 ? 'success' : null));
    const docUrl = computed(() => {
      return `${process.env.VUE_APP_DOCS_URL}module/cloud-serving/online-deployment/online-create`;
    });

    // 表单引用
    const formRef = ref(null);
    const uploadRef = ref(null);
    const codeDirRef = ref(null);
    const runParamCompRef = ref(null);
    // 表单
    const form = reactive({
      ...defaultForm,
      runParams: {},
    });
    const assignForm = (newForm = {}) => {
      Object.assign(form, newForm);
    };
    let runParamObj = {};
    // rules
    const rules = {
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
      algorithmSource: [{ required: true, message: '请选择算法来源', trigger: 'change' }],
      codeDir: [
        {
          required: true,
          message: '请选择上传代码',
          trigger: ['blur', 'manual'],
        },
      ],
    };

    // 表单列表数据
    // 算法用途
    const getAlgorithmUsages = () => {
      getAlgoUsages({
        isContainDefault: true,
        current: 1,
        size: 1000,
      }).then((res) => {
        state.algorithmUsageList = res.result;
      });
    };
    const createAlgorithmUsage = async (auxInfo) => {
      await addAlgoUsage({ auxInfo });
      getAlgorithmUsages();
    };
    const delAlgorithmUsage = async (usage) => {
      await deleteAlgoUsage({ ids: [usage.id] });
      if (form.algorithmUsage === usage.auxInfo) {
        form.algorithmUsage = null;
      }
      getAlgorithmUsages();
    };
    // 镜像选择
    // 获取镜像版本列表
    const getImageTags = async (imageName, keepValue = false) => {
      state.imageTagList = await getImageTagList({
        projectType: IMAGE_PROJECT_TYPE.TRAIN,
        imageName,
      });
      if (keepValue && form.imageTag && !state.imageTagList.includes(form.imageTag)) {
        Message.warning('原有镜像版本不存在，请重新选择');
        form.imageTag = null;
      }
    };
    // 获取镜像名列表
    const getImageNames = async (keepValue = false) => {
      state.imageNameList = await getImageNameList({ projectTypes: [IMAGE_PROJECT_TYPE.TRAIN] });
      if (!keepValue || !form.imageName) {
        form.imageTag = null;
      } else if (!state.imageNameList.includes(form.imageName)) {
        Message.warning('原有镜像不存在，请重新选择');
        form.imageName = form.imageTag = null;
      } else {
        getImageTags(form.imageName, true);
      }
    };

    // Handler
    const onAlgorithmSourceChange = () => {
      form.runCommand = null;
      runParamObj = {};
    };
    const onInferenceChange = () => {
      uploadRef.value.formRef.reset();
      form.codeDir = null;
    };
    const onAlgorithmUsageChange = (value) => {
      const usageRes = state.algorithmUsageList.find((usage) => usage.auxInfo === value);
      if (value && !usageRes) {
        createAlgorithmUsage(value);
      }
    };
    const onImageNameChange = (imageName) => {
      form.imageTag = null;
      if (imageName) {
        getImageTags(imageName);
        return;
      }
      state.imageTagList = [];
    };
    const updateRunParams = (params) => {
      runParamObj = params;
    };
    // 上传事件处理
    const updateObjectPath = () => {
      state.uploadParams.objectPath = `upload-temp/${user.id}/${getUniqueId()}`;
    };
    const onFileRemove = () => {
      form.codeDir = null;
      state.uploading = false;
      codeDirRef.value.validate('manual');
    };
    const uploadStart = (files) => {
      updateObjectPath();
      [state.uploading, state.size, state.progress] = [true, files.size, 0];
    };
    const onSetProgress = (val) => {
      state.progress += val;
    };
    const uploadSuccess = (res) => {
      state.progress = 100;
      setTimeout(() => {
        state.uploading = false;
      }, 1000);
      if (state.uploading) {
        form.codeDir = res[0].data.objectName;
        codeDirRef.value.validate('manual');
      }
    };
    const uploadError = () => {
      Message.error('上传文件失败');
      state.uploading = false;
    };

    // 入口方法
    const initForm = (newForm = {}) => {
      assignForm(newForm);
      getAlgorithmUsages();
      updateObjectPath();
      // 只能 Fork 为我的算法，因此不需要获取镜像列表
      if (!isFork.value) {
        getImageNames(true);
      }
    };
    // 重置表单
    const resetForm = () => {
      assignForm({
        ...defaultForm,
        runParams: {},
      });
      uploadRef.value.formRef.reset();
    };
    // 表单验证
    const validate = (resolve, reject) => {
      let valid = true;
      formRef.value.validate((isValid) => {
        valid = valid && isValid;
      });
      if (form.algorithmSource === ALGORITHM_RESOURCE_ENUM.PRESET && !form.inference) {
        const runParamValid = runParamCompRef.value.validate();
        if (!runParamValid) {
          Message.warning('运行参数不合法');
        } else {
          form.runParams = { ...runParamObj };
        }
        valid = valid && runParamValid;
      }
      if (valid) {
        if (typeof resolve === 'function') {
          return resolve(form);
        }
        return true;
      }
      if (typeof reject === 'function') {
        return reject(form);
      }
      return false;
    };

    return {
      ALGORITHM_RESOURCE_ENUM,
      algorithmConfig,
      uploadSizeFomatter,
      uploadFilters: [invalidFileNameChar],
      defaultProcessColors,

      ...toRefs(state),
      isAdmin,
      isFork,
      isServing,
      status,
      docUrl,

      formRef,
      uploadRef,
      codeDirRef,
      runParamCompRef,
      form,
      rules,

      delAlgorithmUsage,

      onAlgorithmSourceChange,
      onInferenceChange,
      onAlgorithmUsageChange,
      onImageNameChange,
      updateRunParams,
      onFileRemove,
      uploadStart,
      onSetProgress,
      uploadSuccess,
      uploadError,

      initForm,
      resetForm,
      validate,
    };
  },
};
</script>

<style lang="scss" scoped>
.ts-tip {
  ol {
    padding-left: 20px;
    margin: 14px 0;

    li {
      line-height: 24px;
      list-style-type: decimal;
    }
  }
}

.fork-tip {
  margin: -10px 0 10px;
}
</style>
