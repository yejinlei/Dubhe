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
    <UploadForm
      action="fakeApi"
      :visible="uploadDialogVisible"
      :transformFile="withDimensionFile"
      :toggleVisible="handleClose"
      :params="uploadParams"
      @uploadSuccess="uploadSuccess"
      @uploadError="uploadError"
    />
    <!--主界面-->
    <div class="classify-container flex">
      <!--文件列表展示-->
      <div class="file-list-container">
        <div v-loading="crud.loading" class="app-container">
          <!--tabs页和工具栏-->
          <div class="classify-tab">
            <el-tabs :value="lastTabName" @tab-click="handleTabClick">
              <el-tab-pane label="未完成" name="unannotate" />
              <el-tab-pane label="已完成" name="annotate" />
            </el-tabs>
            <div class="classify-button flex flex-between flex-vertical-align">
              <div class="row-left">
                <el-button
                  :disabled="lastTabName==='annotate'"
                  type="primary"
                  icon="el-icon-plus"
                  @click="openUploadDialog"
                >
                  添加图片
                </el-button>
                <el-button
                  type="danger"
                  icon="el-icon-delete"
                  :loading="crud.delAllLoading"
                  :disabled="crud.selections.length === 0"
                  @click="toDelete(crud.selections)"
                >删除
                </el-button>
                <el-button class="sorting-menu-trigger">
                  <SortingMenu :menuList="menuList" @sort="handleSort" />
                </el-button>
              </div>
              <div class="row-right">
                <el-checkbox v-model="checkAll" :indeterminate="isIndeterminate" :disabled="crud.data.length === 0" @change="handleCheckAllChange">{{ checkAll ? '取消全选' : '选择全部' }}</el-checkbox>
                <label class="classify-select-tip item__label">已选 {{ selectImgsId.length }} 张</label>
              </div>
            </div>
          </div>
          <div
            v-if="lastTabName==='unannotate' && crud.page.total===0 && !crud.loading"
          >
            <InfoCard :handleClick="openUploadDialog">
              <span slot="desc">点击添加图片</span>
            </InfoCard>
          </div>
          <div v-if="lastTabName==='annotate' && crud.page.total===0 && !crud.loading">
            <InfoCard>
              <i slot="image" class="el-icon-receiving" />
              <span slot="desc">
                暂无数据
              </span>
            </InfoCard>
          </div>
          <!--图片列表组件-->
          <image-gallery
            v-if="!crud.loading"
            ref="imgGallery"
            v-loading="crud.loading"
            :data-images="crud.data"
            :is-multiple="true"
            :categoryId2Name="categoryId2Name"
            class="imgs"
            :selectImgsId="selectImgsId"
            @onselectmultipleimage="handleSelectMultipleImg"
            @clickImg="clickImg"
          />
          <!--分页组件-->
          <el-pagination
            v-if="crud.page.total>0"
            page-size.sync="crud.page.size"
            :total="crud.page.total"
            :current-page.sync="crud.page.current"
            :page-size="30"
            :page-sizes="[30, 50, 100]"
            :style="`text-align:${crud.props.paginationAlign};`"
            style="margin-top: 8px;"
            layout="total, prev, pager, next, sizes"
            @size-change="crud.sizeChangeHandler($event)"
            @current-change="crud.pageChangeHandler($event)"
          />
        </div>
      </div>
      <!--Label列表展示-->
      <div class="label-list-container">
        <div class="fixed-label-list">
          <div v-if="datasetInfo.labelGroupId" class='mb-10'>
            <label class="el-form-item__label no-float tl">标签组</label>
            <div class="f14">
              <span class="vm">{{ datasetInfo.labelGroupName }} &nbsp;</span>
              <el-link target="_blank" type="primary" :underline="false" class="vm" :href="`/data/labelgroup/detail?id=${datasetInfo.labelGroupId}`">
                查看详情
              </el-link>
            </div>
          </div>
          <div v-if="datasetInfo.labelGroupType !== 1" class="mb-22">
            <LabelTip />
            <div class="flex flex-between">
              <InfoSelect
                v-model="newLabel"
                style="width: 68%;"
                placeholder="选择已有标签或新建标签"
                :dataSource="systemLabels"
                value-key="value"
                default-first-option
                filterable
                allow-create
                @change="handleLabelChange"
              />
              <el-button size="mini" type="primary" @click="postLabel">确定</el-button>
            </div>
          </div>
          <div v-if="rawLabelData.length">
            <div class="pb-10 flex flex-between flex-wrap flex-vertical-align">
              <label class="el-form-item__label" style="max-width: 39.9%; padding: 0;">全部标签({{ rawLabelData.length }})</label>
              <SearchLabel @change="handleSearch" />
            </div>
            <div style="max-height: 200px; padding: 0 2.5px; overflow: auto;">
              <el-row :gutter="5" style="clear: both;">
                <el-col v-for="data in labelData" :key="data.id" :span="8">
                  <el-tag class="tag-item" :title="data.name" :color="data.color" :style="getStyle(data)" @click="event => chooseLabel(data, event)">
                    <span :title="data.name">{{ data.name }}</span>
                    <EditLabel
                      v-if="!data.labelGroupId"
                      :getStyle="getStyle"
                      :item="data"
                      @handleOk="handleEditLabel"
                    />
                  </el-tag>
                </el-col>
              </el-row>
            </div>
            <div v-if="lastTabName==='annotate'" class="label-switch-tool">
              <label class="label-style">标注类型标签</label>
              <el-switch v-model="typeSwitch" :width="45" class="label-switch" @change="switchLabelTag" />
            </div>
          </div>
        </div>
      </div>
    </div>
    <PicInfoModal
      :key="modalId"
      :initialIndex="initialIndex"
      :visible="showPicModal"
      :file="curFile"
      :fileList="fileList"
      :hanleChange="handlePicModalClose"
      :hanleCancel="handlePicModalClose"
    />
  </div>
</template>

<script>
import { without, isNil } from 'lodash';
import { Message } from 'element-ui';

import { colorByLuminance } from '@/utils';
import { queryDataEnhanceList, detail } from '@/api/preparation/dataset';

import { transformFile, transformFiles, getImgFromMinIO, dataEnhanceMap, withDimensionFile, fileCodeMap } from '@/views/dataset/util';
import crudDataFile, { list, del , submit } from '@/api/preparation/datafile';
import { getAutoLabels, getLabels, createLabel, editLabel } from '@/api/preparation/datalabel';
import { batchFinishAnnotation } from '@/api/preparation/annotation';
import CRUD, { presenter, header, crud } from '@crud/crud';
import ImageGallery from '@/components/ImageGallery';
import UploadForm from '@/components/UploadForm';
import InfoCard from '@/components/Card/info';
import InfoSelect from '@/components/InfoSelect';
import SortingMenu from '@/components/SortingMenu';
import SearchLabel from './components/searchLabel';
import LabelTip from './annotate/settingContainer/labelTip';
import PicInfoModal from './components/picInfoModal';
import EditLabel from './annotate/settingContainer/labelList/edit';

// eslint-disable-next-line import/no-extraneous-dependencies
const path = require('path');

export default {
  name: 'Classify',
  components: { ImageGallery, UploadForm, InfoCard, InfoSelect, SearchLabel, LabelTip, SortingMenu, PicInfoModal, EditLabel },
  cruds() {
    const id = this.parent.$route.params.datasetId;
    const crudObj = CRUD({ title: '数据分类', crudMethod: { ...crudDataFile }});
    crudObj.params = { 'datasetId': id, 'status': fileCodeMap.UNCOMPLETED };
    crudObj.page.size = 30;
    return crudObj;
  },
  mixins: [presenter(), header(), crud()],
  props: {
    categoryId2Name: {
      type: Object,
      default: () => ({}),
    },
  },
  data() {
    return {
      datasetId: 0,
      datasetInfo: {},
      uploadDialogVisible: false,
      lastTabName: 'unannotate',
      crudStatusMap: {
        'unannotate': [fileCodeMap.UNCOMPLETED],
        'annotate': [fileCodeMap.COMPLETED],
      },
      newLabel: undefined,
      checkAll: false,
      isIndeterminate: false,
      typeSwitch: true,
      rawLabelData: [],
      labelData: [],
      name2CategoryId: {},
      // 选中列表
      commit: {
        unannotate: [],
        annotate: [],
      },
      systemLabels: [],
      showPicModal: false,
      curFile: undefined, // 当前文件
      fileList: [], // 所有文件
      modalId: 1,
      initialIndex: 0, // 当前图在轮播图列表中的顺序
      enhanceLabels: [], // 增强标签列表
      menuList: [{ label: '默认排序', value: 0 }, { label: '名称排序', value: 1 }],
    };
  },
  computed: {
    // 文件上传前携带尺寸信息
    withDimensionFile() {
      return withDimensionFile;
    },
    uploadParams() {
      return {
        datasetId: this.datasetId,
        objectPath: `dataset/${this.datasetId}/origin`, // 对象存储路径
      };
    },
    selectImgsId() {
      return this.commit[this.lastTabName] || [];
    },
  },
  created() {
    this.datasetId = parseInt(this.$route.params.datasetId, 10);
    this.refreshLabel();
    Promise.all([
      list({ 'datasetId': this.datasetId, 'status': [fileCodeMap.UNCOMPLETED] }),
      list({ 'datasetId': this.datasetId, 'status': [fileCodeMap.COMPLETED] }),
    ])
      .then(([unannotate, annotate]) => {
        if (unannotate.result.length === 0 && annotate.result.length !== 0) {
          this.lastTabName = 'annotate';
          this.crud.params.status = this.crudStatusMap[this.lastTabName];
          this.crud.toQuery();
        }
      });

    detail(this.datasetId).then(res => {
      this.datasetInfo = res || {};
    });
    // 系统标签
    this.getSystemLabel();
  },
  mounted() {
    (async() => {
      const enhanceListResult = await queryDataEnhanceList();
      const { dictDetails = [] } = enhanceListResult || {};
      const labels = dictDetails.map(d => ({
        label: d.label,
        value: Number(d.value),
      }));
      this.enhanceLabels = labels;
    })();
  },
  methods: {
    handleEditLabel(field, item){
      editLabel(item.id, field).then(this.refreshLabel);
    },
    handleSort(command) {
      this.resetQuery();
      this.crud.params.order = command === 1 ? 'name' : '';
      this.crud.refresh();
    },
    // 根据文件 enhaneType 找到对应的增强标签
    findEnhanceMatch(item) {
      return this.enhanceLabels.find(d => d.value === item.enhanceType);
    },
    // 生成增强标签
    buildEnhanceTag(file) {
      const match = this.findEnhanceMatch(file);
      if (match) {
        const enhanceTag = {
          label: match.label,
          value: match.value,
          tag: dataEnhanceMap[match.value],
        };
        return enhanceTag;
      } 
        return undefined;
      
    },
    // 重置所有查询结果
    resetQuery() {
      this.checkAll = false;
      this.isIndeterminate = false;
      this.$refs.imgGallery.resetMultipleSelection();
      this.crud.page.current = 1;
    },
    getSystemLabel() {
      getAutoLabels().then(res => {
        const labels = res.map((item) => ({
          value: item.id,
          label: item.name,
          color: item.color,
          chosen: false,
        }));
        this.systemLabels = labels;
      });
    },
    // 获取过滤后的标签
    handleSearch(label) {
      if (label) {
        this.labelData = this.rawLabelData.filter(d => d.name.includes(label));
      } else {
        this.labelData = this.rawLabelData;
      }
    },
    toDelete(datas = []) {
      this.$confirm(`确认删除选中的${datas.length}个文件?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        this.crud.delAllLoading = true;
        const ids = datas.map(d => ({ id: d }));
        const params = {
          fileIds: datas,
          datasetIds: this.datasetId,
        };
        if (ids.length) {
          del(params).then(() => {
            this.$message({
              message: '删除文件成功',
              type: 'success',
            });
            this.crud.toQuery();
          }).finally(() => {
            this.crud.delAllLoading = false;
          });
        }
        this.handleCheckAllChange(0);
        // 更新 commit 表
        Object.assign(this.commit, {
          [this.lastTabName]: without(this.commit[this.lastTabName], ...datas),
        });
      });
    },
    handleCheckAllChange(val) {
      const {imgGallery} = this.$refs;
      if(!imgGallery) return false;
      if (val) {
        imgGallery.selectAll();
      } else {
        imgGallery.resetMultipleSelection();
      }
    },
    handleSelectMultipleImg(values) {
      // 选中图片的数量
      const checkedCount = values.length;
      const dataImgLen = this.$refs.imgGallery.dataImages.length;
      this.checkAll = checkedCount === dataImgLen;
      this.isIndeterminate = checkedCount > 0 && checkedCount < dataImgLen;
      this.crud.selectionChangeHandler(values);
      // 更新 commit 表
      Object.assign(this.commit, {
        [this.lastTabName]: values,
      });
    },
    // 点击图片事件
    clickImg(img, selectedImgList) {
      // 文件扩展
      const extendFile = d => ({
        file_name: path.basename(d.url),
        enhanceType: d.enhanceType,
      });

      // 扩展文件增强类型
      const extendFileEnhance = d => ({
        file_name: path.basename(d.url),
        enhanceType: d.enhanceType,
        enhanceTag: this.buildEnhanceTag(d),
      });

      // 如果没有选中图片
      if (selectedImgList.length === 0) {
        this.showPicModal = true;
        this.curFile = transformFile(img, extendFile);
        this.fileList = transformFiles(this.crud.data, extendFileEnhance);
        const curIndex = this.crud.data.findIndex(item => item.id === this.curFile.id);
        if (curIndex > -1) {
          this.initialIndex = curIndex;
        }
      }
    },
    handlePicModalClose() {
      this.modalId+=1;
      this.showPicModal = false;
      this.curFile = undefined;
      this.fileList = [];
    },
    handleTabClick(tab) {
      const tabName = tab.name;
      if (this.lastTabName === tabName) {
        return;
      }
      this.crud.params.status = this.crudStatusMap[tabName];
      this.lastTabName = tabName;
      this.crud.refresh();
      this.checkAll = false;
      this.typeSwitch = true;
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
        });
      }
    },
    uploadError() {
      this.$message({
        message: '上传文件失败',
        type: 'error',
      });
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
          if (item.color === '#000000') {
            item.color = '';
          }
          this.categoryId2Name[item.id] = item.name;
          this.name2CategoryId[item.name] = item.id;
        });
        this.typeSwitch = true;
        this.switchLabelTag(true);
        // 初始化设置 labelData
        this.labelData = this.rawLabelData;
      });
    },
    chooseLabel(row, event) {
      // 过滤编辑入口
      if (event.target.classList.contains('el-icon-edit')) return;
      if (this.selectImgsId.length > 0) {
        const annotations = [];
        this.selectImgsId.forEach((item) => {
          annotations.push({
            annotation: JSON.stringify([{
              category_id: this.name2CategoryId[row.name],
              score: 1.0,
            }]),
            id: item,
          });
        });
        batchFinishAnnotation({ annotations }, this.datasetId).then(() => {
          this.crud.refresh();
          this.handleCheckAllChange(0);
        });
      } else {
        this.$message({
          message: '没有选中任何图片',
          type: 'info',
        });
      }
    },
    handleLabelChange(value) {
      // 新建标签
      if (!isNil(value)) {
        // 如果不是系统标签，才会选择新建
        if (this.systemLabels.findIndex(d => d.value === value) === -1) {
          this.addLabel(value);
          // 新建标签
          this.postLabel();
        } else {
          const systemLabel = this.systemLabels.find(d => d.value === value) || {};
          systemLabel.label && this.addLabel(systemLabel.label);
        }
      }
    },
    addLabel(label) {
      this.newLabel = label;
    },
    postLabel() {
      if (this.newLabel) {
        if (this.rawLabelData.findIndex(d => d.name === this.newLabel) > -1) {
          Message.warning('当前标签已存在');
          return;
        }
        createLabel(this.datasetId, { name: this.newLabel }).then(() => {
          this.$message({
            message: `标签[${this.newLabel}]创建成功`,
            type: 'success',
          });
          this.newLabel = undefined;
          this.refreshLabel();
        });
      } else {
        Message.warning('请选择标签');
      }
    },
    switchLabelTag(newSwitch) {
      this.$refs.imgGallery?.setImageTagVisible(newSwitch);
    },
    getStyle(item) {
      // 根据亮度来决定颜色
      return {
        color: colorByLuminance(item.color),
      };
    },
  },
};
</script>

<style lang="scss" scoped>
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
</style>
<style lang="scss">
.sorting-menu-trigger {
  .sorting-menu {
    padding: 8px 25px;
  }
}

.file-list-container {
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

.label-style {
  font-size: 14px;
  color: #606266;
}

.labelTable {
  min-height: 100px;
  max-height: 300px;
  overflow-y: auto;

  tr {
    float: left;
    width: auto;
    margin: 3px;

    > td {
      padding: 8px 10px;
    }
  }
}

.imgs li {
  cursor: pointer;
}

.row-right {
  .el-checkbox {
    margin-right: 10px;
  }
}

@media (max-width: 1440px) {
  .fixed-label-list {
    padding: 10px 15px 0;
  }
}
</style>
