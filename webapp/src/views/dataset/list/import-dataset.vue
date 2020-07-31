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
    :key="formKey"
    title="导入自定义数据集"
    width="600px"
    :visible="visible"
    :disabled="uploading"
    @change="handleCancelUploadDataset"
    @ok="handleUploadDataset('formRef')"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="数据集名称" prop="name">
        <el-input v-model="form.name" placeholder="数据集名称不能超过50字" maxlength="50" />
      </el-form-item>
      <el-form-item label="数据类型" prop="dataType">
        <el-select disabled value="图片" width="100px" />
      </el-form-item>
      <el-form-item ref="datasetFile" v-model="form.datasetFile" label="上传数据集" prop="datasetFile">
        <upload-inline
          ref="uploadForm"
          action="fakeApi"
          accept=".zip"
          list-type="text"
          :acceptSize="0"
          :show-file-count="false"
          :params="uploadParams"
          :auto-upload="true"
          :hash="false"
          :limit="1"
          @uploadStart="uploadStart"
          @uploadSuccess="uploadSuccess"
          @uploadError="uploadError"
        />
        <div v-if="uploading"><i class="el-icon-loading" />数据集上传中...</div>
      </el-form-item>
      <el-form-item label="数据集描述">
        <el-input
          v-model="form.remark"
          type="textarea"
          placeholder="数据集描述长度不能超过100字"
          maxlength="100"
          rows="3"
          show-word-limit
        />
      </el-form-item>
    </el-form>
  </BaseModal>
</template>

<script>
import { bucketName } from '@/utils/minIO';
import UploadInline from '@/components/UploadForm/inline';
import BaseModal from '@/components/BaseModal';
import { addCustomDataset } from '@/api/preparation/dataset';

import { validateName } from "@/utils/validate";

export default {
  name: "UploadDatasetForm",
  components: {
    UploadInline,
    BaseModal,
  },
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    closeUploadDatasetForm: {
      type: Function,
    },
  },
  data() {
    return {
      formKey: 1,
      form: {
        name: "",
        dataType: 0,
        annotateType: 2,
        status: 4,
        datasetFile: undefined,
        remark: "",
      },
      uploading: false,
      rules: {
        name: [
          {
            required: true,
            message: "请输入数据集名称",
            trigger: ["change", "blur"],
          },
          { validator: validateName, trigger: ["change", "blur"] },
        ],
        datasetFile: [
          {
            required: true,
            message: "请选择上传数据集",
            trigger: ["blur", "manual"],
          },
        ],
      },
    };
  },
  computed: {
    uploadParams() {
      return {
        objectPath: `dataset/importdataset`, // 导入自定义数据集存储路径
      };
    },
  },
  methods: {
    handleCancelUploadDataset() {
      this.formKey += 1;
      this.closeUploadDatasetForm();
    },
    handleUploadDataset(formName) {
      this.$refs[formName].validate(valid => {
        if (!valid) {
          return;
        }
        const customForm = { 
          name: this.form.name, 
          desc: this.form.remark,
          archiveUrl: `${bucketName}/${this.form.datasetFile}`,
        };
        return addCustomDataset(customForm).then(() => {
          this.$message({
            message: '导入数据集成功',
            type: 'success',
          });
        }).finally(() => {
          this.resetFormFields();
          this.closeUploadDatasetForm();
        });
      });
    },
    resetFormFields() {
      this.formKey += 1;
      this.form = {};
    },
    uploadStart() {
      this.uploading = true;
    },
    uploadSuccess(res) {
      this.form.datasetFile = res[0].data.objectName;
      this.uploading = false;
      this.$refs.datasetFile.validate('manual');
    },
    uploadError() {
      this.$message({
        message: '上传文件失败',
        type: 'error',
      });
      this.uploading = false;
    },
  },
};
</script>