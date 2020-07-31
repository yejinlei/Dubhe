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
  <div class="app-container">
    <!--任务版本新增-->
    <job-form
      ref="jobForm"
      :form="form"
      :loading="loading"
      :type="formType"
      @getForm="getForm"
      @resetForm="resetForm"
    />
  </div>
</template>

<script>
import { add as addJob } from '@/api/trainingJob/job';
import JobForm from '@/components/Training/jobForm';

const defaultJobForm = {
  $_id: 0,
  trainName: '',
  description: '',
  algorithmSource: 1,
  algorithmId: null,
  dataSourceName: null,
  dataSourcePath: null,
  imageTag: null,
  imageName: null,
  runCommand: null,
  outPath: '/home/result/',
  logPath: '/home/log/',
  resourcesPoolType: 0,
  trainJobSpecsId: null,
  runParams: {},
};

export default {
  name: 'JobAdd',
  components: { JobForm },
  data() {
    return {
      formType: 'add',
      form: { ...defaultJobForm}, // 【训练任务版本】新增和编辑使用
      loading: false,
    };
  },
  mounted() {
    const from = this.$route.params.from || 'job';
    if (from === 'algorithm') {
      const {params} = this.$route.params;
      this.form = Object.assign(this.form, params);
      this.$nextTick(() => {
        this.$refs.jobForm.clearValidate();
      });
    } else if (from === 'param') {
      const {paramsInfo} = this.$route.params;
      this.form = Object.assign(this.form, paramsInfo);
      this.form.$_id = new Date().getTime();
      this.form.trainName = paramsInfo.paramName;
      this.formType = 'paramsAdd';
    }
  },
  methods: {
    // 任务新增
    async getForm(form) {
      const params = { ...form};
      delete params.$_id;
      delete params.algorithmSource;
      this.loading = true;
      const res = await addJob(params).finally(() => {
        this.loading = false;
      });
      this.$message({
        message: '任务提交成功',
        type: 'success',
      });
      this.$router.push({ path: `/training/jobDetail?type=detail&id=${res[0]}` });
    },
    resetForm(reset) {
      if (reset) {
        this.form = { ...defaultJobForm};
        this.$message({
          message: '数据已重置',
          type: 'success',
        });
      }
    },
  },
};
</script>
