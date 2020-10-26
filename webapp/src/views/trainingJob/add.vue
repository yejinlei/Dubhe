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
      :type="formType"
      @getForm="getForm"
    />
    <el-button type="primary" :loading="loading" style="margin-left: 120px;" @click="save">开始训练</el-button>
    <el-button @click="reset">清空</el-button>
  </div>
</template>

<script>
import { add as addJob } from '@/api/trainingJob/job';
import JobForm from '@/components/Training/jobForm';

export default {
  name: 'JobAdd',
  components: { JobForm },
  data() {
    return {
      formType: 'add',
      loading: false,
    };
  },
  created() {
    const from = this.$route.params.from || 'job';
    if (from === 'algorithm') {
      const {params} = this.$route.params;
      this.formType = 'algoAdd';
      this.$nextTick(() => {
        this.$refs.jobForm.initForm(params);
      });
    } else if (from === 'param') {
      const {paramsInfo} = this.$route.params;
      paramsInfo.trainName = paramsInfo.paramName;
      this.formType = 'paramsAdd';
      this.$nextTick(() => {
        this.$refs.jobForm.initForm(paramsInfo);
      });
    }
    this.$nextTick(() => {
      this.$refs.jobForm.initForm();
    });
  },
  methods: {
    save() {
      this.$refs.jobForm.save();
    },
    reset() {
      this.$refs.jobForm.reset();
    },
    // 任务新增
    async getForm(form) {
      const params = { ...form};
      delete params.algorithmSource;
      this.loading = true;
      const res = await addJob(params).finally(() => {
        this.loading = false;
      });
      this.$message({
        message: '任务提交成功',
        type: 'success',
      });
      this.$router.push({ path: `/training/jobdetail?type=detail&id=${res[0]}` });
    },
  },
};
</script>
