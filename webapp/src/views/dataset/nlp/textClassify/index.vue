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
  <div>
    <UploadForm
      action="fakeApi"
      title="导入文本"
      accept=".txt"
      dataType="text"
      :visible="uploadDialogVisible"
      :toggleVisible="handleClose"
      :params="uploadParams"
      @uploadSuccess="uploadSuccess"
      @uploadError="uploadError"
    />
    <div class="classify-container flex">
      <!--文件列表展示-->
      <div class="text-list-container">
        <div class="app-container">
          <!--tabs页和工具栏-->
          <div class="classify-tab">
            <el-tabs :value="lastTabName" @tab-click="handleTabClick">
              <el-tab-pane :label="countInfoTxt.unfinished" name="unfinished" />
              <el-tab-pane :label="countInfoTxt.finished" name="finished" />
            </el-tabs>
            <div class="classify-button flex flex-between flex-vertical-align">
              <div class="row-left">
                <el-button
                  icon="el-icon-right"
                  class="ml-40"
                  type="primary"
                  @click="goDetail"
                >
                  去标注
                </el-button>
                <el-button
                  :disabled="lastTabName==='finished'"
                  icon="el-icon-plus"
                  @click="openUploadDialog"
                >
                  添加文本
                </el-button>
                <el-button
                  type="danger"
                  icon="el-icon-delete"
                  :loading="crud.delAllLoading"
                  :disabled="crud.selections.length === 0"
                  @click="doDelete(crud.selections)"
                >
                  删除
                </el-button>
              </div>
              <div class="row-right">
                <label class="classify-select-tip item__label">已选 {{ crud.selections.length }} 条</label>
              </div>
            </div>
          </div>
          <el-table
            ref="textTable"
            v-loading="crud.loading"
            :data="dataList"
            highlight-current-row
            @selection-change="crud.selectionChangeHandler"
            @sort-change="crud.sortChange"
          >
            <el-table-column fixed type="selection" min-width="5%"/>
            <el-table-column
              class-name="table-text"
              prop="abstract"
              label="文本摘要"
              align="left"
              min-width="50%"
            />
            <TextStatus
              prop="status"
              min-width="10%"
              label="标注状态"
              align="left"
              :statusList="statusList"
              :filterByTextStatus="filterByTextStatus"
              :textStatusFilter="textStatusFilter"
            />
            <el-table-column
              v-if="lastTabName==='finished'"
              show-overflow-tooltip
              prop="labelId"
              align="left"
              min-width="10%"
            >
              <template slot="header">
                <dropdown-header
                  title="标签"
                  :list="labelIdList"
                  :filtered="!isNil(crud.params.labelId)"
                  dropdownStyle="max-height: 400px; overflow-y: auto;"
                  @command="cmd => filter('labelId', cmd)"
                />
              </template>
              <template slot-scope="scope">
                <el-tag 
                  :style="getStyle(scope.row)" 
                  :color="getColor(scope.row)"
                >
                  {{ getName(scope.row) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column
              v-if="lastTabName==='finished'"
              prop="prediction"
              label="预测值"
              min-width="10%"
              sortable="custom"
              align="left"
            /> 
            <Action
              fixed="right"
              min-width="15%"
              align="left"
              :showDetail="showDetail"
              :doDelete="doDelete"
            />
          </el-table>
          <!--分页组件-->
          <el-pagination
            :page-size.sync="crud.page.size"
            :page-sizes="[10, 20, 50]"
            :total="crud.page.total"
            :current-page.sync="crud.page.current"
            :style="`text-align:${crud.props.paginationAlign};`"
            style="margin-top: 8px;"
            layout="total, prev, pager, next, sizes"
            @size-change="crud.sizeChangeHandler($event)"
            @current-change="crud.pageChangeHandler"
          />
        </div>
      </div>
      <!--Label列表展示-->
      <div class="label-list-container">
        <div class="fixed-label-list">
          <SideBar
            :labels="labels"
            :datasetInfo="datasetInfo"
            :createLabel="createLabel"
            :labelClickable="false"
          />
        </div>
      </div>
    </div>
    <TextInfoModal
      :key="modalId"
      :visible="showTextModal"
      :pageInfo="pageInfo"
      :toNext="toNext"
      :toPrev="toPrev"
      :hanleChange="handleTextModalClose"
      :hanleCancel="handleTextModalClose"
      :goDetail="goDetail"
      :deleteFile="deleteFile"
      :crud="crud"
    />
  </div>
</template>

<script>
import { Message } from "element-ui";
import { isNil } from "lodash";

import { getLabels } from "@/api/preparation/datalabel";
import { colorByLuminance } from "@/utils";
import { detail, createLabel as createLabelApi, queryLabels as queryLabelsApi } from "@/api/preparation/dataset";
import { del, submit } from "@/api/preparation/datafile";
import { list, count } from '@/api/preparation/textData';
import { 
  fileCodeMap, 
  getImgFromMinIO, 
  statusCodeMap, 
  textStatusMap, 
  textFinishedMap, 
  textUnfinishedMap, 
  transformFiles,
  readTxt,
} from "@/views/dataset/util";
import CRUD, { presenter, header, crud } from '@crud/crud';
import Action from '@/views/dataset/nlp/textClassify/action';
import TextStatus from '@/views/dataset/nlp/textClassify/textStatus';
import SideBar from '@/views/dataset/nlp/annotation/sidebar';
import UploadForm from '@/components/UploadForm';
import TextInfoModal from '@/views/dataset/components/textInfoModal';
import DropdownHeader from '@/components/DropdownHeader';

const initialPageInfo = {
  current: null,
  size: 1,
  total: 0,
};

export default {
  name: 'TextClassify',
  components: { UploadForm, TextInfoModal, DropdownHeader, Action, TextStatus, SideBar },
  cruds() {
    const id = this.parent.$route.params.datasetId;
    const crudObj = CRUD({
      title: '文本分类',
      crudMethod: { del, list },
    });
    crudObj.params = { 'datasetId': id, 'status': fileCodeMap.UNFINISHED };
    crudObj.page.size = 10;
    return crudObj;
  },
  mixins: [presenter(), header(), crud()],
  data() {
    return {
      dataList: [],
      modalId: 0,
      datasetId: 0,
      datasetInfo: {}, // 数据库信息
      uploadDialogVisible: false,
      showTextModal: false,
      lastTabName: 'unfinished', // 当前选中的tab页
      crudStatusMap: {
        'unfinished': [fileCodeMap.UNFINISHED],
        'finished': [fileCodeMap.FINISHED],
      },
      rawLabelData: [], // 显示的label
      labelData: [], // 所有的label数据
      categoryId2Name : {},
      name2CategoryId : {},
      labels: [], // 可使用的标签
      selections:[], // 选择多的selection
      textStatusFilter: null,
      textStatusMap,
      statusCodeMap,
      pageInfo: initialPageInfo,
      countInfo: {
        unfinished: 0,
        finished: 0,
      },
    };
  },
  computed: {
    uploadParams() {
      return {
        datasetId: this.datasetId,
        objectPath: `dataset/${this.datasetId}/origin`, // 对象存储路径
      };
    },
    isNil() {
      return isNil;
    },
    // 标注状态的可筛选项取决于当前tab，全部对应的值跟所在的tab页有关
    statusList() {
      let rawStatusList;
      let statusList;
      if(this.lastTabName === 'unfinished') {
        rawStatusList = Object.keys(textUnfinishedMap).map(d => ({
          label: textUnfinishedMap[d],
          value: Number(d),
        }));
        statusList = [{ label: '全部', value: fileCodeMap.UNFINISHED }].concat(rawStatusList);
      } else {
        rawStatusList = Object.keys(textFinishedMap).map(d => ({
          label: textFinishedMap[d],
          value: Number(d),
        }));
        statusList = [{ label: '全部', value: fileCodeMap.FINISHED }].concat(rawStatusList);
      }
      return statusList;
    },
    labelIdList() {
      const rawLabelIdList = this.labelData.map(item => ({
        label: item.name,
        value: Number(item.id),
      }));
      return [{ label: '全部', value: null }].concat(rawLabelIdList);
    },
    countInfoTxt() {
      return {
        unfinished: `无标注信息（${this.countInfo.unfinished}）`,
        finished: `有标注信息（${this.countInfo.finished}）`,
      };
    },
  },
  watch: {
    // eslint-disable-next-line
    'crud.data': function(next) {
      const self = this;
      const promises = transformFiles(next).map(d => readTxt(d.url));
      Promise.all(promises).then(res => {
        self.dataList = next.map((d, index) => ({
          ...d,
          abstract: res[index],
        }));
      });

    },
  },
  created() {
    this.datasetId = parseInt(this.$route.params.datasetId, 10);
    this.refreshLabel();
    Promise.all([
      list({ 'datasetId': this.datasetId, 'status': [fileCodeMap.UNFINISHED] }),
      list({ 'datasetId': this.datasetId, 'status': [fileCodeMap.FINISHED] }),
    ]).then(([unfinished, finished]) => {
      if (unfinished.result.length === 0 && finished.result.length !== 0) {
        this.lastTabName = 'finished';
        this.crud.params.status = this.crudStatusMap[this.lastTabName];
        this.crud.toQuery();
      }
    });

    detail(this.datasetId).then(res =>{
      this.datasetInfo = res || {};
    });

    // 更新统计信息
    this.setCountInfo();

    // 获取标签
    this.queryLabels().then(res => {
      this.labels = res;
    });
  },
  methods: {
    textStatusFilter2Type(filter) {
      if (filter === 'all') return null;
      return filter;
    },
    filterByTextStatus(command) {
      if (command === this.textStatusFilter) {
        return;
      }
      this.textStatusFilter = command;
      this.crud.params.status = command;
      this.crud.page.current = 1;
      this.crud.refresh();
    },
    openUploadDialog() {
      this.uploadDialogVisible = true;
    },
    handleClose() {
      this.uploadDialogVisible = false;
    },
    refreshLabel() {
      getLabels(this.datasetId).then((res) => {
        this.rawLabelData = res;
        this.rawLabelData.forEach((item) => {
          this.categoryId2Name[item.id] = {
            'name':item.name,
            'color':item.color,
          };
          this.name2CategoryId[item.name] = item.id;
        });
        // 初始化设置 labelData
        this.labelData = this.rawLabelData;
      });
    },
    parseTextAnnotationType(row, column, cellValue = 0) {
      return textStatusMap[cellValue];
    },
    filter(column, value) {
      this[column] = value;
      this.crud.params[column] = value;
      this.crud.page.current = 1;
      this.crud.toQuery();
    },
    getStyle(item) {
      const sLabel = this.labels.find(d => d.id === item.labelId) || null;
      // 根据亮度来决定颜色
      const color = colorByLuminance(sLabel?.color);
      return {
        color,
        display: 'inline-block',
        width: 'auto',
        minWidth: "60px",
        textAlign: 'center',
        border: 'unset',
      };
    },
    getColor(item) {
      return isNil(item.labelId) ? "" : this.categoryId2Name[item.labelId].color;
    },
    getName(item) {
      return isNil(item.labelId) ? "" : this.categoryId2Name[item.labelId].name;
    },
    
    // 查询标签
    async queryLabels(requestParams = {}) {
      const labels = await queryLabelsApi(this.datasetId, requestParams);
      return labels || [];
    },

    handleTabClick(tab) {
      const tabName = tab.name;
      if (this.lastTabName === tabName) {
        return;
      }
      // 切换tab页清除筛选条件
      this.textStatusFilter = null;
      this.crud.params.labelId = null;
      this.crud.params.status = this.crudStatusMap[tabName];
      this.lastTabName = tabName;
      this.crud.refresh();
      this.checkAll = false;
    },

    // 更新标签
    async updateLabels() {
      this.labels = await this.queryLabels();
    },

    // 新建标签
    createLabel(labelParams = {}) {
      return createLabelApi(this.datasetId, labelParams).then(() => {
        this.updateLabels();
        this.refreshLabel();
        Message.success('标签创建成功');
      });
    },

    async uploadSuccess(res) {
      const files = getImgFromMinIO(res);
      // 提交业务上传
      if (files.length > 0) {
        submit(this.datasetId, files).then(() => {
          this.$message({
            message: '上传文件成功',
            type: 'success',
          });
          this.crud.toQuery();
          this.setCountInfo();
        });
      }
    },
    uploadError(err) {
      this.$message({
        message: err.message || '上传文件失败',
        type: 'error',
      });
    },
    // 删除文件基类
    deleteFile(datas = []){
      this.crud.delAllLoading = true;
      const ids = datas.map(d => ({ id: d }));
      datas = datas.map(d => (d.id));
      const params = {
        fileIds: datas,
        datasetIds: this.datasetId,
      };
      if (ids.length) {
        return del(params).then(() => {
          this.$message({
            message: '删除文件成功',
            type: 'success',
          });
          this.crud.toQuery();
          // 更新统计
          this.setCountInfo();
        }).finally(() => {
          this.crud.delAllLoading = false;
        });
      }
      return Promise.reject(new Error('文件不存在'));
    },
    doDelete(datas = []) {
      const self = this;
      this.$confirm(`确认删除选中的${datas.length}个文件?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => self.deleteFile(datas));
    },
    // 查看标注
    goDetail() {
      const query = this.lastTabName === 'finished' ? {
        tab: 'finished',
      } : {};
      this.$router.push({
        path: `/data/datasets/text/annotation/${this.datasetId}`,
        query,
      });
    },
    async setCountInfo() {
      const countInfo = await count(this.$route.params.datasetId);
      this.countInfo = countInfo;
    },
    // 当size 为1 重新生成页码的位置
    sizeOfPage(){
      return this.crud.page.size * (this.crud.page.current - 1);
    },
    reset(){
      this.pageInfo = initialPageInfo;
    },
    // 显示查看文本详情框
    showDetail(row, index) {
      this.showTextModal = true;
      this.pageInfo = {
        ...this.crud.page,
        current: this.sizeOfPage() + index + 1,
      };
    },
    handleTextModalClose() {
      this.modalId+=1;
      this.showTextModal = false;
      this.reset();
    },
    toNext() {
      this.pageInfo = {
        ...this.pageInfo,
        current: Math.min(this.pageInfo.current + 1, this.pageInfo.total),
      };
    },
    toPrev() {
      this.pageInfo = {
        ...this.pageInfo,
        current: Math.max(this.pageInfo.current - 1, 1),
      };
    },
  },
};
</script>

<style lang="scss" scoped>
@import "~@/assets/styles/variables.scss";

.classify-tab {
  display: flex;
  align-items: center;
  padding: 4px 0;
  margin-bottom: 10px;
}

.sorting-menu-trigger {
  padding: 0;
}

.classify-tab .classify-button {
  flex: 1;
  margin: 13px 80px 20px 100px;
}

.label-switch-tool {
  margin-top: 25px;
}

.label-switch-tool .label-switch {
  float: right;
}

.sidebar {
  padding: 20px;
  border-left: 1px solid $borderColor;
}
</style>

<style lang="scss">
.text-list-container {
  flex: 1;
}

.label-list-container {
  width: 20%;
}

.fixed-label-list {
  position: fixed;
  top: 50px;
  width: 20%;
  height: calc(100vh - 83px);
  padding: 28px 28px 0;
  margin-bottom: 33px;
  overflow-y: auto;
  background-color: #f2f2f2;
}

.table-text div {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

</style>
