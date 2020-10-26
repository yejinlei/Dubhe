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
    <el-select
      v-model="algoUsage"
      placeholder="请选择数据集用途"
      @change="onAlgorithmUsageChange"
    >
      <el-option :value="null" label="全部" />
      <el-option
        v-for="item in algorithmUsageList"
        :key="item.id"
        :value="item.auxInfo"
        :label="item.auxInfo"
      />
    </el-select>
    <el-select
      v-model="dataSource"
      placeholder="请选择您挂载的数据集"
      filterable
      value-key="id"
      @change="onDataSourceChange"
    >
      <el-option
        v-for="item in datasetIdList"
        :key="item.id"
        :value="item"
        :label="item.name"
      />
    </el-select>
    <el-select
      v-model="dataSourceVersion"
      placeholder="请选择您挂载的数据集版本"
      value-key="versionUrl"
      @change="onDataSourceVersionChange"
    >
      <el-option
        v-for="(item, index) in datasetVersionList"
        :key="index"
        :value="item"
        :label="item.versionName"
      />
    </el-select>
    <el-tooltip effect="dark" :content="urlTooltip" placement="top">
      <i class="el-icon-warning-outline primary f18 v-text-top" />
    </el-tooltip>
    <el-tooltip effect="dark" :disabled="!dataSourceVersion" :content="ofRecordTooltip" placement="top">
      <el-checkbox
        v-model="useOfRecord"
        :disabled="!ofRecordDisabled"
        @change="onUseOfRecordChange"
      >使用 OfRecord</el-checkbox>
    </el-tooltip>
  </div>
</template>

<script>
import { list as getAlgorithmUsages } from '@/api/algorithm/algorithmUsage';
import { getPublishedDatasets, getDatasetVersions } from '@/api/preparation/dataset';

export default {
  name: 'DataSourceSelector',
  props: {
    type: {
      type: String,
      default: 'train',
    },
    algorithmUsage: {
      type: String,
      default: null,
    },
    dataSourceName: {
      type: String,
      default: null,
    },
    dataSourcePath: {
      type: String,
      default: null,
    },
  },
  data() {
    return {
      algorithmUsageList: [],
      datasetIdList: [],
      datasetVersionList: [],

      algoUsage: null,
      dataSource: null,
      dataSourceVersion: null,
      useOfRecord: false,

      result: {
        dataSourceType: null,
        dataSourceName: null,
        dataSourcePath: null,
        imageCounts: null,
      },
    };
  },
  computed: {
    ofRecordTooltip() {
      const content = this.dataSourceVersion?.versionOfRecordUrl
        ? '选中 OfRecord 将使用二进制数据集文件'
        : '二进制数据集文件不可用或正在生成中';
      return content;
    },
    ofRecordDisabled() {
      return this.dataSourceVersion && this.dataSourceVersion.versionOfRecordUrl;
    },
    urlTooltip() {
      return this.type === 'verify'
        ? '请确保代码中包含“val_data_url”参数用于传输数据集路径'
        : '请确保代码中包含“data_url”参数用于传输数据集路径';
    },
  },
  watch: {
    result: {
      deep: true,
      handler(result) {
        this.$emit('change', result);
      },
    },
  },
  mounted() {
    this.algoUsage = this.algoUsage || null;
    this.getAlgorithmUsages();
  },
  methods: {
    // handlers
    onAlgorithmUsageChange(annotateType, datasetInit = false) {
      // 算法用途修改之后，重新获取数据集列表，清空数据集结果
      this.getDataSetList(annotateType, datasetInit);
    },
    async onDataSourceChange(dataSource) {
      // 数据集选项发生变化时，获取版本列表，同时清空数据集版本、路径、OfRecord 相关信息
      this.datasetVersionList = await getDatasetVersions(dataSource.id);
      this.result.dataSourceName = null;
      this.result.dataSourcePath = null;
      this.dataSourceVersion = null;
      this.useOfRecord = false;
    },
    onDataSourceVersionChange(version) {
      // 选择数据集版本后，如果存在 OfRecordUrl，则默认勾选使用，否则禁用选择
      this.result.dataSourceName = `${this.dataSource.name}:${version.versionName}`;
      this.result.imageCounts = version.imageCounts;
      if (version.versionOfRecordUrl) {
        this.useOfRecord = true;
        this.result.dataSourcePath = version.versionOfRecordUrl;
      } else {
        this.useOfRecord = false;
        this.result.dataSourcePath = version.versionUrl;
      }
    },
    onUseOfRecordChange(useOfRecord) {
      this.result.dataSourcePath = useOfRecord
        ? this.dataSourceVersion.versionOfRecordUrl
        : this.dataSourceVersion.versionUrl;
    },
    // getters
    getAlgorithmUsages() {
      const params = {
        isContainDefault: true,
        current: 1,
        size: 1000,
      };
      getAlgorithmUsages(params).then(res => {
        this.algorithmUsageList = res.result;
      });
    },
    /**
     * 用于获取数据集列表
     * @param {String} annotateType
     * @param {Boolean} init 表示是否根据传入的数据集信息进行初始化
     */
    async getDataSetList(annotateType, init) {
      const params = {
        size: 1000,
        annotateType: annotateType || undefined,
      };
      const data = await getPublishedDatasets(params);
      this.datasetIdList = data.result;
      this.datasetVersionList = [];
      if (!init || !this.dataSourceName) {
        this.dataSource = this.dataSourceVersion = this.result.dataSourceName = this.result.dataSourcePath = null;
      } else {
        // 根据传入的数据集信息进行初始化
        this.dataSource = this.datasetIdList.find(dataset => dataset.name === this.dataSourceName.split(':')[0]);
        if (!this.dataSource) {
          // 无法在数据集列表中找到同名的数据集
          this.$message.warning('原有数据集不存在，请重新选择');
          this.result.dataSourceName = this.result.dataSourcePath = null;
          return;
        }
        this.datasetVersionList = await getDatasetVersions(this.dataSource.id);
        // 首先尝试使用 versionUrl 进行数据集路径匹配
        this.dataSourceVersion = this.datasetVersionList.find(dataset => dataset.versionUrl === this.dataSourcePath);
        if (!this.dataSourceVersion) {
          // 无法匹配上时使用 versionOfRecordUrl 进行数据集路径匹配
          this.dataSourceVersion = this.datasetVersionList.find(dataset => dataset.versionOfRecordUrl === this.dataSourcePath);
          this.dataSourceVersion && (this.useOfRecord = true);
        }
        // 如果二者都不能匹配上，说明原有的数据集版本目前不存在
        if (!this.dataSourceVersion) {
          this.$message.warning('原有数据集版本不存在，请重新选择');
          this.result.dataSourcePath = null;
        }
      }
    },
    // 外部调用接口方法
    updateAlgorithmUsage(usage, init = false) {
      this.algoUsage = usage || null;
      this.onAlgorithmUsageChange(usage, init);
    },
    reset() {
      Object.assign(this.result, {
        dataSourceType: null,
        dataSourceName: null,
        dataSourcePath: null,
      });
      this.algoUsage = null;
      this.dataSource = null;
      this.dataSourceVersion = null;
      this.useOfRecord = false;
      this.datasetVersionList = [];
    },
  },
};
</script>
