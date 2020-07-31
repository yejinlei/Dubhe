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
  <div class="ts-drawer">
    <!--配置信息-->
    <div class="title" tabindex="0">配置信息</div>
    <job-detail :item="item" />
    <!--运行信息-->
    <div class="title">运行信息</div>
    <el-row class="row">
      <el-col :span="12">
        <div class="label">运行时长</div>
        <div class="text">{{ item.runtime }}</div>
      </el-col>
      <el-col v-if="item.trainStatus===1" :span="12">
        <div class="label">监控信息</div>
        <el-button
          size="mini"
          @click="getGarafana"
        >进入 Grafana</el-button>
      </el-col>
    </el-row>
    <!--日志信息-->
    <div class="title">
      运行日志
      <el-button class="fr" @click="downloadTrainLog">下载运行日志</el-button>
    </div>
    <div
      v-mouse-wheel="getTrainLog"
      class="iframe"
    >
      <prism-render :code="logTxt" />
    </div>
  </div>
</template>

<script>
import { downloadFile } from '@/utils';
import { getTrainLog, downloadTrainLog, getGarafanaInfo } from '@/api/trainingJob/job';
import PrismRender from '@/components/Prism';
import jobDetail from './jobDetail';

export default {
  name: 'JobDrawer',
  components: { jobDetail, PrismRender },
  props: {
    item: {
      type: Object,
      default: () => { return {}; },
    },
  },
  data() {
    return {
      logList: [],
      noMoreLog: false,
      currentLogLine: 1,
      logLines: 50,
      logLoading: false,
      logMsgInstance: null,
    };
  },
  computed: {
    getLogDisabled() {
      return this.logLoading || this.noMoreLog;
    },
    logTxt() {
      return this.logList.join('\n');
    },
  },
  methods: {
    getTrainLog(isOpen = false) {
      if (this.getLogDisabled) {
        return;
      }
      this.logLoading = true;
      getTrainLog({
        jobId: this.item.id,
        startLine: this.currentLogLine,
        lines: this.logLines,
      }).then(res => {
        this.logList = this.logList.concat(res.content);
        this.currentLogLine = res.endLine + 1;
        if (res.lines < this.logLines) {
          this.noMoreLog = true;
          if (!isOpen && !this.logMsgInstance && res.lines < 3) {
            this.logMsgInstance = this.$message.warning({
              message: '已经到达日志底部了。',
              onClose: this.onLogMsgClose,
            });
          }
          setTimeout(() => {
            this.noMoreLog = false;
          }, 1000);
        }
      }).catch(err => {
        this.noMoreLog = true;
        setTimeout(() => {
          this.noMoreLog = false;
        }, 1000);
        throw err;
      }).finally(() => {
        this.logLoading = false;
      });
    },
    async downloadTrainLog() {
      const data = await downloadTrainLog({ jobId: this.item.id });
      downloadFile(data, `${this.item.jobName  }-log.log`);
    },
    onLogMsgClose() {
      this.logMsgInstance = null;
    },
    reset() {
      this.logList = [];
      this.noMoreLog = false;
      this.currentLogLine = 1;
      this.getTrainLog(true);
    },
    async getGarafana() {
      const res = await getGarafanaInfo(this.item.id);
      if (res.jobMetricsGrafanaUrl) {
        window.open(res.jobMetricsGrafanaUrl, '_blank');
      } else {
        this.$message({
          message: '暂无有效的监控信息',
          type: 'warning',
        });
      }
    },
  },
};
</script>
