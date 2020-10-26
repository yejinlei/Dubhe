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
  <!--模型管理页面-保存模型Dialog-->
  <el-dialog append-to-body :close-on-click-modal="false" :visible.sync="visible" title="创建模型" width="800px" @open="onDialogOpen" @close="onDialogClose">
    <!--step-->
    <el-steps :active="step" finish-status="success" style="width: 300px; margin-right: auto; margin-left: auto;">
      <el-step title="创建模型" />
      <el-step title="上传版本" />
    </el-steps>
    <!--step==1-->
    <template v-if="step===0">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" style="margin-top: 20px;">
        <el-form-item label="模型名称" prop="name">
          <el-input
            v-model.trim="form.name"
            style="width: 300px;"
            maxlength="15"
            placeholder="请输入模型名称"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="框架" prop="frameType">
          <el-select v-model="form.frameType" placeholder="请选择框架" style="width: 400px;">
            <el-option
              v-for="item in dict.frame_type"
              :key="item.value"
              :value="item.value"
              :label="item.label"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="模型格式" prop="modelType">
          <el-select v-model="form.modelType" placeholder="请选择模型格式" style="width: 400px;">
            <el-option
              v-for="item in dict.model_type"
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
          <el-input v-model="form.modelDescription" type="textarea" placeholder="请输入模型描述" maxlength="255" show-word-limit style="width: 500px;" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="doAddModel">下一步</el-button>
      </div>
    </template>
    <!--step==2-->
    <template v-if="step==1">
      <el-form v-if="visible" ref="form2" :model="form2" :rules="rules" label-width="100px">
        <el-form-item label="模型名称">
          <div>{{ form.name }}</div>
        </el-form-item>
        <el-form-item ref="modelAddress" label="模型上传" prop="modelAddress">
          <upload-inline
            v-if="refreshFlag"
            action="fakeApi"
            accept=".zip,.pb,.h5,.ckpt,.pkl,.pth,.weight,.caffemodel,.pt"
            :acceptSize="0"
            list-type="text"
            :limit="1"
            :multiple="false"
            :show-file-count="false"
            :params="uploadParams"
            :auto-upload="true"
            :onRemove="handleRemove"
            @uploadStart="uploadStart"
            @uploadSuccess="uploadSuccess"
            @uploadError="uploadError"
          />
          <upload-progress 
            v-if="loading" 
            :progress="progress" 
            :color="customColors" 
            :status="status" 
            :size="size" 
            @onSetProgress="onSetProgress"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="visible = false;step=0;">下次再传</el-button>
        <el-button type="primary" :disabled="loading" @click="doAddVersion">确定上传</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import { add as addVersion } from '@/api/model/modelVersion';
import { add as addModel } from '@/api/model/model';
import { list as getAlgorithmUsages, add as addAlgorithmUsage } from '@/api/algorithm/algorithmUsage';
import UploadInline from '@/components/UploadForm/inline';
import UploadProgress from '@/components/UploadProgress';
import { getUniqueId, validateNameWithHyphen } from '@/utils';

const defaultForm = {
  name: null,
  frameType: null,
  modelType: null,
  modelClassName: null,
  modelDescription: null,
};

const defaultForm2 = {
  parentId: null,
  modelAddress: null,
  modelSource: 0,
};

export default {
  name: 'AddModelDialog',
  dicts: ['model_type', 'frame_type'],
  components: { UploadInline, UploadProgress },
  data() {
    return {
      visible: false,
      form: { ...defaultForm},
      form2: { ...defaultForm2},
      rules: {
        parentId: [
          { required: true, message: '请选择模型', trigger: 'blur' },
        ],
        name: [
          { required: true, message: '请输入模型名称', trigger: 'blur' },
          { max: 20, message: '长度在20个字符以内', trigger: 'blur' },
          {
            validator: validateNameWithHyphen,
            trigger: ['blur', 'change'],
          },
        ],
        frameType: [
          { required: true, message: '请选择模型框架', trigger: 'blur' },
        ],
        modelType: [
          { required: true, message: '请选择模型格式', trigger: 'blur' },
        ],
        modelClassName: [
          { required: true, message: '请输入模型类别', trigger: ['blur', 'change'] },
        ],
        modelDescription: [
          { required: true, message: '请输入模型描述', trigger: 'blur' },
          { max: 255, message: '长度在255个字符以内', trigger: 'blur' },
        ],
        modelAddress: [
          { required: true, message: '请上传有效的模型', trigger: ['blur', 'manual'] },
        ],
      },
      step: 0,
      uploadParams: {
        objectPath: null, // 对象存储路径
      },
      algorithmUsageList: [],
      refreshFlag: true,
      loading: false,
      size: 0,
      progress: 0,
      customColors: [
        {color: '#909399', percentage: 40},
        {color: '#e6a23c', percentage: 80},
        {color: '#67c23a', percentage: 100},
      ],
    };
  },
  computed: {
    status() {
      return this.progress === 100 ? 'success' : null;
    },
    user() {
      return this.$store.getters.user;
    },
  },
  methods: {
    show() {
      this.refreshFlag = false;
      this.visible = true;
      this.$nextTick(() => {
        this.refreshFlag = true;
      });
    },
    onDialogOpen() {
      this.getAlgorithmUsages();
    },
    onDialogClose() {
      this.reset();
      this.loading = false;
      this.$emit('addDone', true);
    },
    reset() {
      this.step = 0;
      this.form = { ...defaultForm};
      this.form2 = { ...defaultForm2};
      setTimeout(() => {
        this.$refs.form && this.$refs.form.clearValidate();
        this.$refs.form2 && this.$refs.form2.clearValidate();
      }, 0);
    },
    // handle
    onAlgorithmUsageChange(value) {
      const usage = this.algorithmUsageList.find(usage => usage.auxInfo === value);
      if (!usage) {
        this.createAlgorithmUsage(value);
      }
    },
    handleRemove() {
      this.loading = false;
      this.form2.modelAddress = null;
      this.$refs.modelAddress.validate('manual');
    },
    uploadStart(files) {
      this.updateImagePath();
      [ this.loading, this.size, this.progress ] = [ true, files.size, 0 ];
    },
    onSetProgress(val) {
      this.progress += val;
    },
    uploadSuccess(res) {
      this.progress = 100;
      setTimeout(() => {
        this.loading = false;
      }, 1000);
      this.form2.modelAddress = res[0].data.objectName;
      this.$refs.modelAddress.validate('manual');
    },
    uploadError() {
      this.loading = false;
      this.$message({
        message: '上传文件失败',
        type: 'error',
      });
    },
    // op
    doAddModel() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          const params = { ...this.form};
          const res = await addModel(params);
          this.form2.parentId = res.id;
          this.step = 1;
          this.$message({
            message: '模型新建成功',
            type: 'success',
          });
        }
      });
    },
    doAddVersion() {
      this.$refs.form2.validate(async valid => {
        if (valid) {
          const params = { ...this.form2};
          await addVersion(params);
          this.visible = false;
          this.$message({
            message: '模型版本上传成功',
            type: 'success',
          });
        }
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
    async createAlgorithmUsage(auxInfo) {
      await addAlgorithmUsage({ auxInfo });
      this.getAlgorithmUsages();
    },
    updateImagePath() {
      this.uploadParams.objectPath = `upload-temp/${this.user.id}/${getUniqueId()}`;
    },
  },
};
</script>
