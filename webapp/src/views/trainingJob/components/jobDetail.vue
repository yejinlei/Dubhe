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
      <el-col :xl="isJob ? 12 : 24" :span="24">
        <div class="label">名称</div>
        <div class="text">{{ isJob ? item.trainName : item.paramName }}</div>
      </el-col>
      <el-col v-if="isJob" :xl="12" :span="24">
        <div class="label">版本</div>
        <div class="text">{{ item.trainVersion }}</div>
      </el-col>
      <el-col v-if="isJob && item.parentTrainVersion" :xl="12" :span="24">
        <div class="label">父版本</div>
        <div class="text">{{ item.parentTrainVersion }}</div>
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
        <div class="label">验证数据集</div>
        <div class="text">{{ item.valDataSourceName }}</div>
      </el-col>
      <el-col :xl="12" :span="24">
        <div class="label">算法</div>
        <div class="text">
          <div class="dib">{{ item.algorithmName }}</div>
          <el-tooltip
            v-if="!isParam"
            :content="algorithmEditTooltip"
            placement="top"
          >
            <div class="dib">
              <i
                v-if="item.algorithmCodeDir"
                class="el-icon-edit primary"
                :class="{'cp': !editLoading}"
                @click="goEditAlgorithm(item.algorithmId, item.algorithmCodeDir)"
              />
              <i v-else class="el-icon-question primary" />
            </div>
          </el-tooltip>
          <i v-if="editLoading" class="el-icon-loading" />
        </div>
      </el-col>
      <el-col :xl="12" :span="24">
        <div class="label">镜像名称</div>
        <div class="text">{{ item.imageName }}</div>
      </el-col>
      <el-col :xl="12" :span="24">
        <div class="label">镜像版本</div>
        <div class="text">{{ item.imageTag }}</div>
      </el-col>
      <el-col :xl="12" :span="24">
        <div class="label">模型名称</div>
        <div class="text">{{ item.modelName }}</div>
      </el-col>
      <el-col v-if="!isParam" :span="12">
        <div class="label">日志下载</div>
        <div class="text">
          <el-tooltip
            effect="dark"
            :content="logDisableTooltip"
            :disabled="!logDownloadDisable"
            placement="top"
          >
            <div>
              <el-button
                size="mini"
                :disabled="logDownloadDisable"
                @click="() => download(item.logPath, item.jobName + '_log.zip')"
              >下载</el-button>
            </div>
          </el-tooltip>
        </div>
      </el-col>
      <el-col v-if="!isParam" :span="12">
        <div class="label">模型下载</div>
        <div class="text">
          <el-tooltip
            effect="dark"
            content="训练选择的算法没有包含模型输出参数，无法下载模型"
            :disabled="Boolean(item.outPath)"
            placement="top"
          >
            <div>
              <el-button
                size="mini"
                :disabled="!item.outPath"
                @click="() => choosePath(item)"
              >下载</el-button>
            </div>
          </el-tooltip>
        </div>
      </el-col>
      <el-col :span="24">
        <div class="label">运行命令</div>
        <div class="text">{{ item.runCommand }}</div>
      </el-col>
      <el-col :span="24" class="dynamic">
        <div class="label">运行参数</div>
        <div class="text">
          <span
            v-for="(p, index) in runParamsList"
            :key="p.key"
          >{{ p.key }} = {{ p.value }} {{ index === runParamsList.length - 1 ? '' : ', ' }}</span>
        </div>
      </el-col>
      <el-col :xl="12" :span="24">
        <div class="label">训练类型</div>
        <div class="text">{{ trainTypeMap[item.trainType] }}</div>
      </el-col>
      <el-col :xl="12" :span="24">
        <div class="label">节点数</div>
        <div class="text">{{ item.resourcesPoolNode }}</div>
      </el-col>
      <el-col :span="24">
        <div class="label">节点规格</div>
        <div class="text">
          {{ spec && spec.label }}
        </div>
      </el-col>
    </el-row>
    <!--模型下载Dialog-->
    <path-select-dialog
      ref="pathSelect"
      type="modelDownload"
      @chooseDone="chooseDone"
    />
  </div>
</template>

<script>
import { convertMapToList, downloadZipFromObjectPath } from '@/utils';
import { createNotebook, getNotebookAddress } from '@/api/development/notebook';
import pathSelectDialog from './pathSelectDialog';

export default {
  name: 'JobDetail',
  dicts: ['cpu_specs', 'gpu_specs'],
  components: { pathSelectDialog },
  props: {
    item: {
      type: Object,
      default: () => ({}),
    },
    type: {
      type: String,
      default: 'job',
    },
  },
  data() {
    return {
      runParamsList: [],
      editLoading: false,
      trainTypeMap: {
        0: '普通训练',
        1: '分布式训练',
      },
    };
  },
  computed: {
    logDisableTooltip() {
      switch(this.item.trainStatus) {
        case 0:
        case 1:
          return '训练尚未完成，无法下载训练日志';
        case 7:
          return '训练创建失败，无法下载训练日志';
          // no default
      };
      if (!this.item.logPath) {
        return '算法没有指定日志输入路径，无法下载训练日志';
      }
      return null;
    },
    algorithmEditTooltip() {
      if (this.item.algorithmCodeDir) {
        return '算法在线编辑';
      }
      return '算法不存在或不可编辑';
    },
    logDownloadDisable() {
      return [0, 1, 7].indexOf(this.item.trainStatus) >= 0 || !this.item.logPath;
    },
    isJob() {
      return this.type === 'job';
    },
    isParam() {
      return this.type === 'param';
    },
    spec() {
      return this.specList.find(spec => spec.label === this.item.trainJobSpecsName);
    },
    specList() {
      switch(this.item.resourcesPoolType) {
        case 0:
          return this.dict.cpu_specs;
        case 1:
          return this.dict.gpu_specs;
        default:
          return [];
      }
    },
  },
  watch: {
    item: {
      handler(newItem) {
        this.runParamsList = convertMapToList(newItem.runParams);
      },
      immediate: true,
    },
  },
  beforeDestroy() {
    this.editLoading = false;
  },
  methods: {
    choosePath(item) {
      this.$refs.pathSelect.show({
        resumePath: `${item.outPath}/`,
        fileName: `${item.jobName}_model.zip`,
      });
    },
    chooseDone(params, afterPathList){
      this.download(params.path, params.fileName, afterPathList);
      // 如果是文件，zip包会有完整的层级结构，文件目录则会优化，tofix
    },
    download(filePath, fileName, afterPathList = []) {
      downloadZipFromObjectPath(filePath, fileName, { 
        flat: true,
        filter: afterPathList.length ? result => result.filter(item => {
          return afterPathList.some(path => item.name.startsWith(`${filePath}/${path}/`));
        }) : null, 
      });
      this.$message({
        message: '请查看下载文件',
        type: 'success',
      });
    },
    async goEditAlgorithm(id, codeDir) {
      if (this.editLoading) {
        return;
      }
      this.editLoading = true;
      const notebookInfo = await createNotebook(1, {
        sourceId: id,
        sourceFilePath: codeDir,
      }).finally(() => {
        this.editLoading = false;
      });
      if (notebookInfo.status === 0 && notebookInfo.url) {
        this.openNotebook(notebookInfo.url, notebookInfo.noteBookName);
      } else {
        this.editLoading = true;
        this.getNotebookAddress(notebookInfo.id, notebookInfo.noteBookName);
      }
    },
    getNotebookAddress(id, noteBookName) {
      if (!this.editLoading) {
        return;
      }
      getNotebookAddress(id).then(url => {
        if (url) {
          this.openNotebook(url, noteBookName);
        } else {
          setTimeout(() => {
            this.getNotebookAddress(id, noteBookName);
          }, 1000);
        }
      }).catch(err => {
        this.editLoading = false;
        throw new Error(err);
      });
    },
    openNotebook(url, noteBookName) {
      window.open(url);
      this.$message.success('Notebook已启动.');
      this.editLoading = false;
      this.$router.push({ name: 'Notebook', params: {
        noteBookName,
      }});
    },
  },
};
</script>
