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
      <el-form-item label="开发环境" prop="k8sImageName">
        <el-select id="k8sImageName" v-model="form.k8sImageName" placeholder="请选择开发环境" no-data-text="请先选择项目" style="width: 600px;" @change="validateField('k8sImageName')">
          <el-option v-for="(item, index) in imageOptions" :key="index" :label="item.label" :value="item.value" />
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
import BaseModal from '@/components/BaseModal';
import { getModels, add as addNotebook } from '@/api/development/notebook';
import { harborProjects, harborImages } from '@/api/system/harbor';

export default {
  name: 'CreateDialog',
  components: { BaseModal },
  filters: {
    formatModel(item) {
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
        deviceType: null,
        diskMemNum: null,
      },
      rules: {
        noteBookName: [
          { required: true, message: '请输入名称', trigger: 'blur' },
          { pattern: /^[^-](.*[^-])?$/, message: '首尾不能是连字符', trigger: 'blur' },
          { max: 30, message: '长度不超过30个字符', trigger: ['blur', 'change'] },
        ],
        k8sImageName: [{ required: true, message: '请选择镜像', trigger: 'blur' }],
        dataSourcePath: [{ required: true, message: '请选择数据集', trigger: 'blur' }],
        deviceType: [{ required: true, message: '请选择类型', trigger: 'blur' }],
      },
      projectOptions: [],
      imageOptions: [],
      deviceOptions: [],
      modelTypeMap: {},
      defaultSpecOptions: {},
      defaultSpec: {},
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
      this.getHarborProjects();
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
    getHarborProjects() {
      harborProjects(0)
        .then(res => {
          this.projectOptions = res.map(item => ({
            value: item,
            label: item,
          }));
          this.form.harborProject = this.projectOptions && this.projectOptions[0].value;
          this.getHarborImages();
        });
    },
    getHarborImages() {
      const { harborProject } = this.form;
      if (!harborProject) {
        this.imageOptions = [];
        this.form.k8sImageName = null;
        return;
      }
      harborImages({ project: harborProject })
        .then(res => {
          this.imageOptions = res.map(item => ({
            value: item,
            label: item.split(':').reverse()[0],
          }));
          this.form.k8sImageName = null;
        });
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
