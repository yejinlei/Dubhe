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
    :key="formKey"
    :title="importStep===0 ? '导入数据集' : '创建数据集'"
    width="600px"
    center
    :visible="visible"
    @change="handleCancelUploadDataset"
    @ok="handleUploadDataset('formRef')"
  >
    <div v-if="importStep===0" class="placeholder">
      <div class="has-tip">
        <div class="tip">
          请参考下方说明文档，创建数据集完毕后，按照数据集模板格式上传数据集文件，否则标注文件可能无法正确解析。
        </div>
        <div class="requirement">
          <p>1. 数据集脚本工具（<a href="http://docs.dubhe.ai/docs/module/dataset/import-dataset" target="_blank">文档</a>）
          <p>2. 图片数据集（<a href="http://docs.dubhe.ai/docs/module/dataset/import-dataset/#%E5%AF%BC%E5%85%A5%E5%B7%B2%E6%9C%89%E6%95%B0%E6%8D%AE%E9%9B%86" target="_blank">文档</a>）</p>
          <p>3. 文本数据集（<a href="http://docs.dubhe.ai/docs/module/dataset/import-dataset/#%E5%AF%BC%E5%85%A5%E6%96%87%E6%9C%AC%E6%95%B0%E6%8D%AE%E9%9B%86" target="_blank">文档</a></p>
          <p>4. 图片数据集模板（<a href="http://tianshu.org.cn/static/upload/file/dubhe-dataset-template.zip" target="_blank">下载</a>）</p>
          <p>5. 文本数据集模板（<a href="http://tianshu.org.cn/static/upload/file/dubhe-dataset-nlp-template.zip" target="_blank">下载</a>）</p>
          <p>7. 更多参考（<a href="http://docs.dubhe.ai/docs/" target="_blank">官网文档</a>）</p>
        </div>
      </div>
    </div>
    <el-form v-else ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-alert class="info-alert" type="warning" show-icon :closable="false">
        <div slot='title' class='slot-content'>
          <div>数据集创建完毕后，需要使用脚本工具上传本地已有数据集</div>
          <a href="http://docs.dubhe.ai/docs/module/dataset/import-dataset" target="_blank">使用文档</a>
        </div>
      </el-alert>
      <el-form-item label="数据集名称" prop="name">
        <el-input v-model="form.name" placeholder="数据集名称不能超过50字" maxlength="50" />
      </el-form-item>
      <el-form-item label="数据类型" prop="dataType">
        <InfoSelect
          v-model="form.dataType"
          placeholder="数据类型"
          :dataSource="dataTypeList"
          width="200px"
          @change="handleDataTypeChange" />
      </el-form-item>
      <el-form-item label="标注类型" prop="annotateType">
        <InfoSelect
          v-model="form.annotateType"
          placeholder="标注类型"
          :dataSource="annotationList"
          width="200px"
        />
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
    <el-button v-if="importStep===0" slot="footer" class="tc" type="primary" @click="nextImportStep">已阅读，确定创建</el-button>
  </BaseModal>
</template>

<script>
import BaseModal from '@/components/BaseModal';

import InfoSelect from '@/components/InfoSelect';
import { validateName } from "@/utils/validate";
import { annotationCodeMap, annotationMap, dataTypeCodeMap, dataTypeMap, dataTypeAnnotateTypeMap } from '@/views/dataset/util';

import { add } from '@/api/preparation/dataset';

export default {
  name: "ImportDataset",
  components: {
    BaseModal,
    InfoSelect,
  },
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    toggleImportDataset: {
      type: Function,
    },
    onResetFresh: {
      type: Function,
    },
  },
  data() {
    return {
      importStep: 0,
      formKey: 1,
      form: {
        name: "",
        dataType: "",
        annotateType: "",
        status: 4,
        remark: "",
      },
      rules: {
        name: [
          {
            required: true,
            message: "请输入数据集名称",
            trigger: ["change", "blur"],
          },
          { validator: validateName, trigger: ["change", "blur"] },
        ],
        dataType: [
          {
            required: true,
            message: "请选择数据类型",
            trigger: ["change", "blur"],
          },
        ],
        annotateType: [
          {
            required: true,
            message: "请选择标注类型",
            trigger: ["change", "blur"],
          },
        ],
      },
    };
  },
  computed: {
    annotationList() {
      // 原始标注列表
      const rawAnnotationList = Object.keys(annotationMap).map(d => ({
        label: annotationMap[d].name,
        value: Number(d),
      }));
      // 图片，可用图像分类和目标检测；视频，可用目标跟踪；文本，可用文本分类
      return rawAnnotationList.filter(d => {
        if (this.form.dataType === dataTypeCodeMap.IMAGE) {
          return [annotationCodeMap.ANNOTATE, annotationCodeMap.CLASSIFY].includes(d.value);
        }
        if (this.form.dataType === dataTypeCodeMap.VIDEO) {
          return d.value === annotationCodeMap.TRACK;
        }
        if (this.form.dataType === dataTypeCodeMap.TEXT) {
          return d.value === annotationCodeMap.TEXTCLASSIFY;
        }
        return true;
      });
    },

    dataTypeList: () =>
      Object.keys(dataTypeMap)
        .filter(type => [dataTypeCodeMap.IMAGE, dataTypeCodeMap.TEXT].includes(Number(type)))
        .map(d => ({
          label: dataTypeMap[d],
          value: Number(d),
        }),
      ),
  },
  methods: {
    nextImportStep() {
      this.importStep += 1;
    },
    handleCancelUploadDataset() {
      this.formKey += 1;
      this.importStep = 0;
      this.toggleImportDataset();
      this.onResetFresh();
    },
    handleUploadDataset(formName) {
      this.$refs[formName].validate(valid => {
        if (!valid) {
          return;
        }
        const customForm = {
          name: this.form.name,
          remark: this.form.remark,
          annotateType: this.form.annotateType,
          dataType: this.form.dataType,
          type: 0,
          import: true,
        };

        return add(customForm).then(() => {
          this.$message({
            message: '创建数据集成功',
            type: 'success',
          });
        }).finally(() => {
          this.resetFormFields();
          this.toggleImportDataset();
          this.onResetFresh();
        });
      });
    },
    resetFormFields() {
      this.formKey += 1;
      this.importStep = 0;
      this.form = {
        name: "",
        dataType: "",
        annotateType: "",
        status: 4,
        remark: "",
      };
    },
    handleDataTypeChange(dataType) {
      this.form.annotateType = dataTypeAnnotateTypeMap.get(dataType);
    },
  },
};
</script>
