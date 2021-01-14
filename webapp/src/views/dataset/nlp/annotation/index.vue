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
  <div class="main-content">
    <div v-loading="state.pageLoading" class="text-container">
      <div class="navbar">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: datasetUrl }">{{ state.datasetInfo.name || '-' }}</el-breadcrumb-item>
          <el-breadcrumb-item>标注详情</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class='workstage flex'>
        <div class="main f1">
          <WorkSpace
            :loading="state.loading"
            :activeTab="state.activeTab"
            :countInfo="state.countInfo"
            :changeActiveTab="changeActiveTab"
            :txt="state.txt"
            :file="state.file"
            :labelSelected="state.labelSelected"
            :closeLabel="closeLabel"
            :pageInfo="state.pageInfo"
            :toNext="toNext"
            :toPrev="toPrev"
            :saveAnnotation="saveAnnotation"
            :deleteFile="deleteFile"
          />
        </div>
        <div class="sidebar" style="width: 25%;">
          <SideBar
            :labels="state.labels"
            :datasetInfo="state.datasetInfo"
            :createLabel="createLabel"
            :handleLabel="handleLabel"
          />
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import { Message } from 'element-ui';
import { omit, isNil } from 'lodash';
import { onMounted, reactive, watch, computed } from '@vue/composition-api';
import { detail, queryLabels as queryLabelsApi, createLabel as createLabelApi } from '@/api/preparation/dataset';
import { queryFiles, deleteFile as deleteFileApi, save as saveApi, count } from '@/api/preparation/textData';
import { transformFiles, fileCodeMap, dataTypeCodeMap, readTxt } from '../../util';
import WorkSpace from './workspace';
import SideBar from './sidebar';

const pMap = require('p-map');

export default {
  name: "TextAnnotation",
  components: {
    WorkSpace,
    SideBar,
  },
  setup(props, ctx){
    const { $route, $router } = ctx.root;
    const { params = {}, query = {}} = $route;

    const state = reactive({
      labels: [],
      datasetInfo: {},
      pageInfo: {
        current: 1,
        size: 1,
      },
      countInfo: {
        finished: 0,
        unfinished: 0,
      },
      loading: false, // 加载内容
      detail: null, // 标注详情
      timestamp: Date.now(),
      txt: '',
      file: null,
      labelSelected: null,
      activeTab: 'unfinished',
      pageLoading: false, // 初始化页面加载
    });

    // 重置
    const reset = () => {
      Object.assign(state, {
        detail: null,
        labelSelected: null,
        txt: '',
        file: null,
      });
    };

    // 查询标签
    const queryLabels = async(requestParams = {}) => {
      const labels = await queryLabelsApi(params.datasetId, requestParams);
      return labels || [];
    };

    // 更新标签
    const updateLabels = async () => {
      const labels = await queryLabels();
      Object.assign(state, {
        labels,
      });
    };

    // 移除标签
    const closeLabel = () => {
      Object.assign(state, {
        labelSelected: null,
      });
    };

    // 新建标签
    const createLabel = (labelParams = {}) => {
      return createLabelApi(params.datasetId, labelParams).then(() => {
        updateLabels();
        Message.success('标签创建成功');
      });
    };

    // 选中标签
    const handleLabel = (label) => {
      if (isNil(state.detail)) {
        Message.warning('当前无文件选中');
        return;
      }
      Object.assign(state, {
        labelSelected: label,
      });
    };

    // 根据当前文件状态获取 status 映射值
    const getStatusMap = (tab) => {
      const fileStatusKey = {
        finished: 'FINISHED',
        unfinished: 'UNFINISHED',
      };

      // 默认为 unfinished
      const fileStatus = fileStatusKey[tab === 'finished' ? 'finished' : 'unfinished'];

      return fileCodeMap[fileStatus];
    };

    // 获取文件工具方法
    const queryFileUtil = (cfg) => {
      const requestParams = omit({
        ...state.pageInfo,
        status: getStatusMap(state.activeTab),
        ...cfg,
      }, ['total']);
      return queryFiles(params.datasetId, requestParams);
    };

    const setLoadingStatus = loading => {
      Object.assign(state, {
        loading,
      });
    };

    const setPageLoading = loading => {
      Object.assign(state, {
        pageLoading: loading,
      });
    };

    const forceUpdate = () => {
      Object.assign(state, {
        timestamp: Date.now(),
      });
    };

    // 更新文件信息，cfg 参数：
    // status: 文件状态，current: 当前页，size: 每页数量
    const queryFileInfo = async cfg => {
      // 开始加载
      setLoadingStatus(true);
      const filesInfo = await queryFileUtil(cfg);
      // 获取 minIO 文件路径
      const datasetFiles = transformFiles(filesInfo.result);

      const textRes = await pMap(
        datasetFiles,
        async file => {
          const text = await readTxt(file.url);
          return text;
        },
        {concurrency: 1},
      );

      const detail = filesInfo.result[0] || null;
      const sLabel = state.labels.find(d => d.id === detail?.labelId) || null;

      Object.assign(state, {
        pageInfo: filesInfo.page,
        file: datasetFiles[0],
        detail,
        labelSelected: sLabel,
        txt: textRes[0] || '',
        loading: false,
      });
      return { datasetFiles, textRes, pageInfo: filesInfo.page };
    };

    const setCountInfo = async () => {
      const countInfo = await count(params.datasetId);
      Object.assign(state, {
        countInfo,
      });
    };

    // 保存标注工具方法
    const saveAnnotationUtil = () => {
      const annotation = state.labelSelected 
        ? JSON.stringify([{
            category_id: state.labelSelected.id,
            score: 1,
          }])
        : null;
      return saveApi(params.datasetId, state.file.id, { annotation }).then(setCountInfo);
    };

    const saveAnnotation = () => {
      return saveAnnotationUtil().then(forceUpdate);
    };

    // 相对于原有记录是否发生过变更
    const checkChanged = () => {
      let changed = false;
      if(state.labelSelected) {
        // 如果 id 已修改
        changed = (state.detail.labelId !== state.labelSelected.id);
      } else {
        // 未选择标签，需要判断初始是否存在标签
        changed = !!state.detail.labelId;
      }
      return changed;
    };

    // 保存标注结果
    const saveAction = () => {
      return saveAnnotationUtil().then(() => Message.success('自动保存成功'));
    };

    // 下一页
    const toNext = async () => {
      if (state.pageInfo.current + 1 > state.pageInfo.total) return;
      // 只有发生过变更的数据才需要保存
      const changed = checkChanged();
      if(changed) {
        // 阻塞请求，保证先写入，后更新
        await saveAction();
      }
      // 区分内容是否有变更 
      if (!changed) {
        Object.assign(state, {
          pageInfo: {
            ...state.pageInfo,
            current: state.pageInfo.current + 1,
          },
        });
      } else {
        // 强制更新，获取最新 current 对应内容
        forceUpdate();
      }
    };

    // 上一页
    const toPrev = async () => {
      if (state.pageInfo.current < 2) return;
      // 只有发生过变更的数据才需要保存
      const changed = checkChanged();
      if(changed) {
        // 阻塞请求，保证先写入，后更新
        await saveAction();
      }
      Object.assign(state, {
        pageInfo: {
          ...state.pageInfo,
          current: state.pageInfo.current - 1,
        },
      });
    };

    // 删除文本
    const deleteFile = (file) => {
      if(!file.id) return;
      deleteFileApi(params.datasetId, file.id).then(() => {
        // 切换到上一页
        const { current } = state.pageInfo;
        Object.assign(state, {
          pageInfo: {
            ...state.pageInfo,
            current: Math.max(current - 1, 1),
          },
        });
        // 当前第一页强制更新
        if(current === 1) {
          forceUpdate();
        }
        // 更新统计信息
        setCountInfo();
      });
    };

    // 切换 tab 需要更新分页信息
    const changeActiveTab = (tab) => {
      reset();
      Object.assign(state, {
        activeTab: tab.name,
      });
      // 根据文件类型，切换到第一页
      queryFileInfo({status: getStatusMap(tab.name), current: 1});
    };

    const datasetUrl = computed(() => `/data/datasets/textclassify/${params.datasetId}`);

    // 监听页面变更
    watch(() => state.pageInfo.current, next => {
      reset();
      queryFileInfo({current: next});
    }, {
      lazy: true,
    });

    // 强制更新
    watch(() => state.timestamp, () => {
      reset();
      queryFileInfo({current: state.pageInfo.current});
    }, {
      lazy: true,
    });

    onMounted(async () => {
      // 判断当前数据集不存在
      let datasetInfo = {};
      setPageLoading(true);
      try {
        // 获取数据集信息
        datasetInfo = await detail(params.datasetId);
      } catch (err) {
        Object.assign(state, {
          error: new Error('当前数据集不存在，请重新输入'),
          pageLoading: false,
        });
        return;
      }
      // 校验数据类型是否为文本
      if (datasetInfo.dataType !== dataTypeCodeMap.TEXT) {
        $router.push({ path: '/data/datasets' });
        throw new Error('不支持该标注类型');
      }

      const newState = {
        datasetInfo,
      };

      if (query.tab === 'finished') {
        newState.activeTab = 'finished';
      }

      // 获取数据集标签，分页结果
      const [labels, countInfo] = await Promise.all([
        queryLabels(), count(params.datasetId)]);
      newState.labels = labels;
      newState.countInfo = countInfo;
      Object.assign(state, newState);
      setPageLoading(false);
      // 文件级更新
      queryFileInfo({ status: getStatusMap(query.tab) });
    });

    return {
      state,
      toNext,
      toPrev,
      datasetUrl,
      deleteFile,
      closeLabel,
      createLabel,
      handleLabel,
      saveAnnotation,
      changeActiveTab,
    };
  },
};
</script>
<style lang="scss" scoped>
@import "~@/assets/styles/variables.scss";

.main-content {
  height: calc(100vh - 50px - 32px);
  padding-top: 20px;
  background-color: #f4f6f7;
}

.text-container {
  width: 1080px;
  min-width: 60vw;
  max-width: calc(100vw - 80px);
  max-height: 100%;
  padding: 20px 0 0;
  margin: 0 auto;
  background-color: #fff;

  .navbar {
    padding: 0 20px 12px;
    border-bottom: 1px solid $borderColor;
  }

  .workstage {
    .main {
      padding: 10px 20px 40px;
    }

    .sidebar {
      padding: 20px;
      border-left: 1px solid $borderColor;
    }
  }
}
</style>