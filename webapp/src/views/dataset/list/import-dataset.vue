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
          请认真阅读下方说明，创建数据集完毕后，按照系统格式要求上传数据集文件，否则标注文件可能无法正确解析。
          <a class="db" href="http://tianshu.org.cn/static/upload/file/dubhe-dataset-template.zip" target="_blank">下载示例数据集模板</a>
        </div>
        <div class="requirement">
          <p>1. 系统提供了一站式脚本服务用以快速导入本地已有数据集（<a href="http://docs.dubhe.ai/docs/module/dataset/import-dataset" target="_blank">使用文档</a>），推荐使用
          <p>2. 本地数据集需要包括图片（origin 目录）、标注文件（annotation 目录）和标签文件三部分</p>
          <p>3. 注意区分「图像分类」和「目标检测」类型数据集</p>
          <p>4. 图片格式支持 jpg/png/bmp/jpeg，不大于 5M，位于 origin 目录下，不支持目录嵌套</p>
          <p>5. 标注文件为 json 格式，位于 annotation 目录下，必须和文件同名（如果不存在标注，可不上传），不支持目录嵌套</p>
          <p>6. 标签文件为 json 格式，命名要求为 label_{name}.json，其中 name 为标签组名称，不能与系统已有标签组重名</p>
          <p>7. 更多参考示例数据集模板</p>
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
        <el-select disabled value="图片" style="width: 200px;" />
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
import { annotationMap } from '@/views/dataset/util';

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
        dataType: 0,
        annotateType: 2,
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
      const activeList = Object.keys(annotationMap).filter(type => ["1", "2"].includes(type)).map(d => ({
        label: annotationMap[d].name,
        value: Number(d),
      }));
      return activeList;
    },
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
        dataType: 0,
        annotateType: 2,
        status: 4,
        remark: "",
      };
    },
  },
};
</script>