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
    <el-row class="row">
      <el-col v-if="item.delayCreateCountDown > 0" :span="12">
        <div class="label">延迟启动</div>
        <div class="text">剩余 {{ item.delayCreateCountDown | minute2Time }}</div>
      </el-col>
      <el-col v-if="item.delayDeleteCountDown > 0" :span="12">
        <div class="label">训练停止</div>
        <div class="text">剩余 {{ item.delayDeleteCountDown | minute2Time }}</div>
      </el-col>
    </el-row>
    <!--日志信息-->
    <div v-if="prepared" id="log-wrapper">
      <div class="title">
        运行日志
        <el-button
          class="fr log-download-btn"
          :disabled="!podList.length"
          :loading="logDownloading"
          @click="downloadTrainLog"
        >下载{{ isDistributed ? '全部' : '' }}运行日志</el-button>
      </div>
      <log-container
        v-if="!isDistributed && item.trainStatus !== 7"
        ref="logContainer"
        :log-getter="getTrainLog"
        :options="logOptions"
        class="log single-log"
      />
      <div v-else-if="podList.length" id="distributed-log-wrapper">
        <el-tabs
          v-model="activeLogTab"
          class="log-tabs"
          @tab-click="onLogTabClick"
        >
          <el-tab-pane
            v-for="pod in podList"
            :key="pod.podName"
            :label="pod.displayName"
            :name="pod.podName"
          />
        </el-tabs>
        <el-button class="fr log-download-btn mb-20" @click="() => doDownloadPodLog()">下载节点运行日志</el-button>
        <keep-alive>
          <log-container
            :key="activePod.podName"
            ref="podLogContainer"
            :log-getter="getPodLog"
            :options="podLogOption"
            class="log distributed-log"
          />
        </keep-alive>
      </div>
      <div v-else class="log">
        <p class="log-error-msg">{{ logErrorMsg }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import { downloadFile } from '@/utils';
import {
  getTrainLog,
  getJobDetail,
  getGarafanaInfo,
  getPods,
} from '@/api/trainingJob/job';
import {
  getPodLog,
  downloadPodLog,
  batchDownloadPodLog,
  countPodLogs,
} from '@/api/system/pod';
import LogContainer from '@/components/LogContainer';
import JobDetail from './jobDetail';

const LOG_DOWNLOAD_LINES_THRESHOLD = 100000; // 暂定阈值 100K 条

export default {
  name: 'JobDrawer',
  components: { JobDetail, LogContainer },
  filters: {
    minute2Time(totalMinutes) {
      let remainMinutes = totalMinutes || 0;

      const days = Math.floor(totalMinutes / 1440);
      remainMinutes %= 1440;

      const hours = Math.floor(remainMinutes / 60);
      remainMinutes %= 60;

      // eslint-disable-next-line prefer-template
      return `${days ? days + '天' : ''}${hours ? hours + '小时' : ''}${remainMinutes}分钟`;
    },
  },
  data() {
    return {
      item: {},
      prepared: false,

      keepCountDown: false,
      logDownloading: false,
      podList: [],
      activeLogTab: null,
      podLogLoadTags: {}, // 用于记录分布式训练中，节点的运行日志是否已加载过
    };
  },
  computed: {
    isDistributed() {
      return this.item.trainType === 1;
    },
    activePod() {
      return this.podList.find(pod => pod.podName === this.activeLogTab) || {};
    },
    podLogOption() {
      return { podName: this.activePod.podName };
    },
    logOptions() {
      return { jobId: this.item.id };
    },
    logErrorMsg() {
      if (this.item.trainStatus === 7) {
        return this.item.trainMsg;
      }
      return null;
    },
  },
  methods: {
    getTrainLog,
    getPodLog,
    async downloadTrainLog() {
      this.logDownloading = true;
      const logLines = await countPodLogs(this.podList);
      if (!this.isDistributed) {
        const pod = this.podList[0];
        await this.doDownloadPodLog(pod, logLines[pod.podName], true);
        this.logDownloading = false;
      } else {
        let count = 0;
        this.podList.forEach(pod => {
          count += logLines[pod.podName];
        });
        // 日志总行数小于阈值，则打包下载，否则每节点单独下载
        if (count < LOG_DOWNLOAD_LINES_THRESHOLD) {
          const data = await batchDownloadPodLog({ podVOList: this.podList }).finally(() => { this.logDownloading = false; });
          downloadFile(data, `${this.item.jobName}-log.zip`);
        } else {
          this.$message.warning('节点日志总行数过多，将以单个节点为单位分别下载日志，请允许多个文件下载');
          const jobList = [];
          this.podList.forEach(pod => {
            jobList.push(this.doDownloadPodLog(pod, logLines[pod.podName], false));
          });
          Promise.all(jobList).then(() => {
            this.logDownloading = false;
          });
        }
      }
    },
    /**
     * 下载节点日志
     * @param {*} pod 下载的 pod，如果为空则为 this.activePod
     * @param {*} podLogLines 下载的 pod 对应的日志行数，如果为空则调用接口单独查询
     * @param {*} divisionWarning 是否在超过阈值需要分片下载时进行提示
     */
    async doDownloadPodLog(pod, podLogLines, divisionWarning = true) {
      pod = pod || this.activePod;
      const { podName } = pod;
      const displayName = this.item.jobName + (this.isDistributed ? `-${pod.displayName}` : '');
      if (!podLogLines) {
        const count = await countPodLogs([{ podName }]);
        podLogLines = count[podName];
      }
      // 如果节点日志行数超过阈值，则以阈值行数为单位分片下载
      if (podLogLines < LOG_DOWNLOAD_LINES_THRESHOLD) {
        this.downloadPodLog({ podName }, `${displayName}-log.log`);
      } else {
        if(divisionWarning) {
          this.$message.warning(`目标 ${displayName} 日志总行数过多，将按行数进行日志分片下载，请允许多个文件下载`);
        }
        let startLine = 1;
        let lines = LOG_DOWNLOAD_LINES_THRESHOLD;
        while (startLine < podLogLines) {
          this.downloadPodLog({ podName, startLine, lines }, `${displayName}-log-${startLine}-${startLine + lines - 1}.log`);
          startLine += lines;
          lines = podLogLines - startLine > LOG_DOWNLOAD_LINES_THRESHOLD ? lines : podLogLines - startLine;
        }
      }
    },
    async downloadPodLog(query, name) {
      const data = await downloadPodLog(query);
      downloadFile(data, name);
    },
    countDown() {
      if (this.keepCountDown) {
        setTimeout(() => {
          this.getJobDetail(this.item.id);
          this.countDown();
        }, 60000);
      }
    },
    async onOpen(jobId) {
      await this.getJobDetail(jobId);
      this.prepared =  true;
      if (this.item.delayCreateCountDown > 0 || this.item.delayDeleteCountDown > 0) {
        this.keepCountDown = true;
        this.countDown();
      }
      this.podList = await getPods(this.item.id);
      if (this.isDistributed) {
        if (this.podList.length) {
          this.activeLogTab = this.podList[0].podName;
          this.podLogLoadTags = {};
          this.$nextTick(() => {
            this.$refs.podLogContainer.reset(true);
            this.podLogLoadTags[this.activeLogTab] = true;
          });
        }
        return;
      }
      if (this.item.trainStatus !== 7) {
        this.$nextTick(() => {
          this.$refs.logContainer.reset(true);
        });
      }
    },
    onClose() {
      this.keepCountDown = false;
      this.prepared = false;
    },
    onLogTabClick() {
      if (!this.podLogLoadTags[this.activeLogTab]) {
        this.$nextTick(() => {
          this.$refs.podLogContainer.reset(true);
          this.podLogLoadTags[this.activeLogTab] = true;
        });
      }
    },
    async getJobDetail(jobId) {
      this.item = await getJobDetail(jobId);
    },
    async getGarafana() {
      const res = await getGarafanaInfo(this.item.id);
      if (res.length) {
        const newWindow = window.open('', '_blank');
        res.forEach(item => {
          newWindow.document.write(
            `<p>${item.jobPodName}</p>
             <iframe src="${item.jobMetricsGrafanaUrl}" style = 'width: 100%; height: 100%;' scrolling="yes"></iframe>`,
          );
        });
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

<style lang="scss" scoped>
#distributed-log-wrapper {
  margin: 20px 0;
}

.log {
  width: 90%;
  height: 500px;
  margin: 40px 5%;
  overflow: auto;
  border: #ccc solid 1px;
}

.log-tabs {
  margin: 0 5%;
}

.distributed-log {
  margin-top: 0;
}

.log-download-btn {
  margin-right: 5%;
}

.log-error-msg {
  padding: 16px;
  margin: 6px 0 0;
}
</style>
