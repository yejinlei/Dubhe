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
  <BaseModal
    :visible.sync="showDialog"
    :disabled="btnDisabled"
    title="创建Notebook"
    width="800px"
    @open="onDialogOpen"
    @close="onDialogClose"
    @cancel="showDialog=false"
    @ok="onSubmit"
  >
    <el-form ref="form" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="名称" prop="noteBookName">
        <el-input id="noteBookName" v-model="form.noteBookName" class="input" maxlength="30" style="width: 600px;" show-word-limit placeholder="请输入notebook名称" />
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input id="description" v-model="form.description" type="textarea" maxlength="255" show-word-limit style="width: 600px;" />
      </el-form-item>
      <el-form-item
        label="开发环境"
        prop="k8sImageName"
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
          id="k8sImageName"
          v-model="form.k8sImageName"
          placeholder="请选择镜像版本"
          style="width: 305px;"
          clearable
          @change="validateField('k8sImageName')"
        >
          <el-option
            v-for="(item, index) in harborImageList"
            :key="index"
            :label="item.imageTag"
            :value="item.imageUrl"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="类型" prop="deviceType">
        <el-radio-group id="deviceType" v-model="form.deviceType" @change="onDeviceChange">
          <el-radio-button v-for="(item,index) in deviceOptions" :key="index" :label="item">{{ item==='GPU'?'CPU + GPU':item }}</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="规格">
        {{ defaultSpec | formatModel }}
      </el-form-item>
    </el-form>
  </BaseModal>
</template>

<script>
import { validateNameWithHyphen } from '@/utils';
import BaseModal from '@/components/BaseModal';
import { getModels, add as addNotebook } from '@/api/development/notebook';
import { getImageNameList, getImageTagList } from '@/api/trainingImage/index';
import { IMAGE_PROJECT_TYPE } from '@/views/trainingJob/utils';

export default {
  name: 'CreateDialog',
  components: { BaseModal },
  filters: {
    formatModel(item) {
      if (!item) {
        return '没有节点配置信息';
      }
      const gpuStr = item.gpuNum && item.spec ? ` GPU: ${item.gpuNum}*${item.spec}` : '';
      return `${item.cpuNum}Cores ${item.memNum}GB${gpuStr}`;
    },
  },
  data() {
    return {
      showDialog: false,
      form: {
        noteBookName: null,
        description: null,
        harborProject: null,
        k8sImageName: null,
        imageName: null,
        deviceType: null,
        diskMemNum: null,
      },
      rules: {
        noteBookName: [
          { required: true, message: '请输入名称', trigger: 'blur' },
          { pattern: /^[^-](.*[^-])?$/, message: '首尾不能是连字符', trigger: 'blur' },
          { validator: validateNameWithHyphen, trigger: ['blur', 'change'] },
          { max: 30, message: '长度不超过30个字符', trigger: ['blur', 'change'] },
        ],
        k8sImageName: [{ required: true, message: '请选择镜像', trigger: 'blur' }],
        dataSourcePath: [{ required: true, message: '请选择数据集', trigger: 'blur' }],
        deviceType: [{ required: true, message: '请选择类型', trigger: 'blur' }],
      },
      projectOptions: [],
      harborProjectList: [],
      harborImageList: [],
      deviceOptions: [],
      modelTypeMap: {},
      defaultSpecOptions: {},
      defaultSpec: null,
      btnDisabled: false,
    };
  },
  computed: {
    modelOptions() {
      return this.modelTypeMap[this.form.deviceType] || [];
    },
  },
  methods: {
    showThis() {
      this.showDialog = true;
    },
    onDialogOpen() {
      this.getHarborProjects().then(() => {
        this.resetProject();
      });
      this.getModels();
    },
    onDialogClose() {
      this.imageOptions = [];
      this.form.harborProject = null;
      this.form.k8sImageName = null;
      this.$refs.form.resetFields();
    },
    onDeviceChange() {
      this.defaultSpec = this.defaultSpecOptions[this.form.deviceType];
    },
    async getHarborProjects() {
      this.harborProjectList = await getImageNameList({ projectType: IMAGE_PROJECT_TYPE.NOTEBOOK });
      if (this.form.imageName && !this.harborProjectList.some(project => project === this.form.imageName)) {
        this.$message.warning('原有的镜像名称不存在，请重新选择');
        this.form.imageName = null;
        this.form.k8sImageName = null;
        return;
      }
      this.form.imageName && await this.getHarborImages(true);
      if (this.form.imageTag && !this.harborImageList.some(image => image.imageTag === this.form.imageTag)) {
        this.$message.warning('原有的镜像版本不存在，请重新选择');
        this.form.k8sImageName = null;
      }
    },
    getHarborImages(saveImageName = false) {
      if (saveImageName !== true) {
        this.form.k8sImageName = null;
      }
      if (!this.form.imageName) {
        this.harborImageList = [];
        return;
      }
      return getImageTagList({ imageName: this.form.imageName, projectType: IMAGE_PROJECT_TYPE.NOTEBOOK })
        .then(res => {
          this.harborImageList = res;
        });
    },
    // 镜像项目为空时选择默认项目
    resetProject() {
      if (!this.form.imageName) {
        if (this.harborProjectList.length) {
          Object.assign(this.form, { imageName: this.harborProjectList[0] });
        } else {
          this.$message.warning('镜像项目列表为空');
          return;
        }
        this.getHarborImages();
      }
    },
    getModels() {
      getModels()
        .then(res => {
          this.deviceOptions = [];
          this.modelTypeMap = res;
          for (const key in res) {
            if (Object.prototype.hasOwnProperty.call(res, key)) {
              this.deviceOptions.push(key);
            }
            for (const item of res[key]) {
              if (item.defaultStatus) {
                this.defaultSpecOptions[key] = item;
              }
            }
          }
          this.form.deviceType = this.deviceOptions[0] || null;
          this.form.diskMemNum = this.modelOptions[0]?.diskMemNum || 1;
          this.defaultSpec = this.defaultSpecOptions[this.form.deviceType];
        });
    },
    validateField(field) {
      this.$refs.form.validateField(field);
    },
    onSubmit() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          const { cpuNum, gpuNum, memNum } = this.defaultSpec;
          this.btnDisabled = true;
          addNotebook({
            noteBookName: this.form.noteBookName,
            description: this.form.description,
            k8sImageName: this.form.k8sImageName,
            imageName: this.form.imageName,
            dataSourceName: this.form.dataSourceName,
            dataSourcePath: this.form.dataSourcePath,
            cpuNum,
            gpuNum,
            memNum,
            diskMemNum: this.form.diskMemNum,
          })
            .then(() => {
              this.$emit('on-add');
              this.$message({
                message: '创建成功',
                type: 'success',
              });
               this.btnDisabled = false;
              this.showDialog = false;
              this.form.imageName = this.form.k8sImageName = null;
            })
            .catch(err => {
              this.btnDisabled = false;
              this.$message({
                message: err.message,
                type: 'error',
              });
            });
        } else {
          return false;
        }
      });
    },
  },
};

</script>
