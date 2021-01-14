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
  <div class="app-container">
    <!--顶栏-->
    <div class="head-container">
      <div class="cd-opts">
        <span class="cd-opts-left">
          <el-button
            id="toAdd"
            :disabled="isParams"
            class="filter-item"
            type="primary"
            icon="el-icon-plus"
            round
            @click="toAdd"
          >创建训练任务</el-button>
        </span>
        <span class="cd-opts-right">
          <span slot="right">
            <template v-if="isAllTrain || isRunningTrain">
              <el-input
                id="trainName"
                v-model="jobQuery.trainName"
                clearable
                placeholder="请输入任务名称或 ID"
                style="width: 200px;"
                class="filter-item"
                @clear="toQuery"
                @keyup.enter.native="toQuery"
              />
            </template>
            <template v-if="isParams">
              <el-input
                id="paramName"
                v-model="paramQuery.paramName"
                clearable
                placeholder="请输入任务模板名称"
                class="filter-item"
                style="width: 200px;"
                @clear="toQuery"
                @keyup.enter.native="toQuery"
              />
            </template>
            <span>
              <el-button id="resetQuery" class="filter-item" @click="resetQuery">重置</el-button>
              <el-button id="toQuery" class="filter-item" type="primary" @click="toQuery">搜索</el-button>
            </span>
          </span>
        </span>
      </div>
    </div>
    <el-tabs v-model="active" class="eltabs-inlineblock" @tab-click="handleClick">
      <el-tab-pane id="tab_0" label="全部任务" name="0" />
      <el-tab-pane id="tab_1" label="运行中任务" name="1" />
      <el-tab-pane id="tab_2" label="任务模板" name="2" />
    </el-tabs>
    <!--表格内容-->
    <job-list v-if="isAllTrain || isRunningTrain" ref="jobList" :isAllTrain="isAllTrain" />
    <job-param v-if="isParams" ref="jobParam" />
  </div>
</template>

<script>
import jobList from "./jobList";
import jobParam from "./jobParam";

export default {
  name: "Job",
  dicts: ["job_status"],
  components: { jobList, jobParam },
  data() {
    return {
      active: "0",
      id: null,
      currentPage: 1,
      jobQuery: {
        trainName: null,
        trainStatus: 1,
      },
      paramQuery: {
        paramName: null,
      },
    };
  },
  computed: {
    isAllTrain() {
      return this.active === "0";
    },
    isRunningTrain() {
      return this.active === "1";
    },
    isParams() {
      return this.active === "2";
    },
  },
  mounted() {
    this.$nextTick(() => {
      this.jobQuery.trainStatus = this.isRunningTrain ? 1 : undefined;
      this.toQuery();
    });
  },
  beforeRouteEnter(to, from, next) {
    if (from.name === "JobDetail" && from.params.currentPage) {
      next(vm => {
        vm.currentPage = from.params.currentPage;
      });
      return;
    }
    next();
  },
  methods: {
    // tab change
    handleClick() {
      this.jobQuery.trainStatus = this.isRunningTrain ? 1 : undefined;
      this.currentPage = 1;
      this.toQuery();
    },
    // ACTION
    toQuery() {
      if (this.isParams) {
        this.$nextTick(() => {
          this.$refs.jobParam.toQuery(this.paramQuery);
        });
      } else {
        this.$nextTick(() => {
          this.$refs.jobList.crud.page.current = this.currentPage;
          this.$refs.jobList.toQuery(this.jobQuery);
        });
      }
    },
    toAdd() {
      this.$router.push({ path: "/training/jobadd" });
    },
    resetQuery() {
      if (this.isParams) {
        this.paramQuery = {
          trainName: null,
          trainStatus: null,
        };
      } else if (this.isRunningTrain) {
        this.jobQuery = {
          paramName: null,
          trainStatus: 1,
        };
      } else {
        this.jobQuery = {
          paramName: null,
        };
      }
      this.toQuery();
    },
  },
};
</script>
