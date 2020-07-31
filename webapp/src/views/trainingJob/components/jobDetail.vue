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
  <div>
    <el-row class="row">
      <el-col :xl="type==='job' ? 12 : 24" :span="24">
        <div class="label">名称</div>
        <div class="text">{{ type==='job' ? item.trainName : item.paramName }}</div>
      </el-col>
      <el-col v-if="type==='job'" :xl="12" :span="24">
        <div class="label">版本</div>
        <div class="text">{{ item.trainVersion }}</div>
      </el-col>
      <el-col :span="24">
        <div class="label">描述</div>
        <div class="text">{{ item.description }}</div>
      </el-col>
      <el-col :xl="12" :span="24">
        <div class="label">训练数据集</div>
        <div class="text">{{ item.dataSourceName }}</div>
      </el-col>
      <el-col :xl="12" :span="24">
        <div class="label">算法</div>
        <div class="text">{{ item.algorithmName }}</div>
      </el-col>
      <el-col :xl="12" :span="24">
        <div class="label">镜像名称</div>
        <div class="text">{{ item.imageName }}</div>
      </el-col>
      <el-col :xl="12" :span="24">
        <div class="label">镜像版本</div>
        <div class="text">{{ item.imageTag }}</div>
      </el-col>
      <el-col v-if="type !== 'param'" :span="12">
        <div class="label">日志下载</div>
        <div class="text">
          <el-button
            size="mini"
            :disabled="item.trainStatus === 0 || item.trainStatus === 1"
            @click="() => download(item.logPath, item.jobName + '_log.zip')"
          >下载</el-button>
        </div>
      </el-col>
      <el-col v-if="type !== 'param'" :span="12">
        <div class="label">模型下载</div>
        <div class="text">
          <el-button
            size="mini"
            :disabled="item.trainStatus !== 2 || !item.outPath"
            @click="() => download(item.outPath, item.jobName + '_model.zip')"
          >下载</el-button>
        </div>
        <el-tooltip
          v-show="item.trainStatus !== 2 || !item.outPath"
          class="item"
          effect="dark"
          :content="modelTooltip"
          placement="top"
        >
          <i class="el-icon-question primary f18 vm" />
        </el-tooltip>
      </el-col>
      <el-col :span="24">
        <div class="label">运行命令</div>
        <div class="text">{{ item.runCommand }}</div>
      </el-col>
      <el-col :span="24" class="dynamic">
        <div class="label">运行参数</div>
        <div class="text">
          <span v-for="(p, index) in runParamsList" :key="p.key">{{ p.key }} = {{ p.value }} {{ index === runParamsList.length - 1 ? '' : ', ' }}</span>
        </div>
      </el-col>
      <el-col :span="24">
        <div class="label">节点规格</div>
        <div class="text">
          {{ spec && spec.specsName }}
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { convertMapToList, downloadZipFromObjectPath } from '@/utils';
import { getTrainJobSpecs } from '@/api/trainingJob/job';

export default {
  name: 'JobDetail',
  props: {
    item: {
      type: Object,
      default: () => { return {}; },
    },
    type: {
      type: String,
      default: 'job',
    },
  },
  data() {
    return {
      runParamsList: [],
      specList: [],
      spec: null,
    };
  },
  computed: {
    modelTooltip() {
      if (this.item.trainStatus !== 2) {
        return '训练尚未完成，无法下载模型';
      }
      if (!this.item.outPath) {
        return '训练选择的算法没有包含模型输出参数，无法下载模型';
      }
      return null;
    },
  },
  watch: {
    item: {
      handler(newItem) {
        this.runParamsList = convertMapToList(newItem.runParams);
        this.getTrainJobSpecs(newItem.resourcesPoolType);
      },
      immediate: true,
    },
  },
  methods: {
    download(filePath, fileName) {
      downloadZipFromObjectPath(filePath, fileName, { flat: true });
      this.$message({
        message: '请查看下载文件',
        type: 'success',
      });
    },
    async getTrainJobSpecs(resourcesPoolType) {
      this.specList = await getTrainJobSpecs({ resourcesPoolType });
      if (this.specList.length === 0) {
        this.$message.warning('所选节点类型没有现存规格');
      } else {
        this.spec = this.specList.find(spec => spec.id === this.item.trainJobSpecsId);
      }
    },
  },
};
</script>
