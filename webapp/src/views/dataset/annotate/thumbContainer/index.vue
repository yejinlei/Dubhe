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
  <div class="thumb-wrapper">
    <div>
      <el-button
        v-if="state.datasetInfo.value.dataType === 0"
        class="mb-20"
        type="primary"
        icon="el-icon-plus"
        round
        @click="handleUpload"
      >
        添加图片
      </el-button>
    </div>
    <div class="file-infobar flex">
      <el-dropdown trigger="click" @command="handleDropdown">
        <span class="el-dropdown-link primary" :title="activeAnnotationName">
          {{ activeAnnotationName }}<i class="el-icon-arrow-down el-icon--right" />
        </span>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item
            v-for="item in dropdownList"
            :key="item.command"
            :command="item.command"
          >
            {{ item.label }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <div>
        {{ state.total.value }}&nbsp;<span class="f14">张</span>
      </div>
    </div>
    <List
      ref="listRef"
      v-bind="$attrs"
      :updateState="updateState"
      :list="state.files.value"
      :addList="state.addFiles.value"
      :hasMore="state.hasMore.value"
      :total="state.total.value"
      :offset="state.offset.value"
      :type="thumbState.type"
      :history="state.history.value"
      v-on="$listeners"
    />
    <div v-if="state.total.value > 0" class="el-pagination annotate-pagination">
      <span class="mb-10 el-pagination__jump">
        前往
        <el-input
          v-model="thumbState.gotoNumber"
          class="el-pagination__editor"
          type="number"
          :min="1"
          :max="state.total.value"
          @keyup.native.enter="handleKeyup"
        />
        张</span>
      <el-tooltip content="跳转位置为图片在数据集列表中的位置" placement="top">
        <i class="el-icon-question" />
      </el-tooltip>
    </div>
    <UploadForm
      ref="uploaderRef"
      action="fakeApi"
      :visible="thumbState.showDialog"
      :transformFile="withDimensionFile"
      :toggleVisible="handleClose"
      :params="uploadParams"
      @uploadSuccess="uploadSuccess"
      @uploadError="uploadError"
    />
  </div>
</template>

<script>
import { reactive, computed, ref, watch } from '@vue/composition-api';
import { Message } from 'element-ui';
import { pick } from 'lodash';

import UploadForm from '@/components/UploadForm';
import { fileTypeEnum, fileCodeMap, getImgFromMinIO, withDimensionFile } from '@/views/dataset/util';
import { submit } from '@/api/preparation/datafile';
import { detectFileList, queryFileOffset } from '@/api/preparation/dataset';
import List from './list';

export default {
  name: 'ThumbContainer',
  components: {
    List,
    UploadForm,
  },
  inheritAttrs: false,
  props: {
    state: Object,
    updateList: Function,
    updateState: Function,
    isTrack: Boolean,
  },
  setup(props, ctx) {
    const { $route } = ctx.root;

    const uploaderRef = ref(null);
    const listRef = ref(null);

    const { updateList, state, updateState, isTrack } = props;
    const { datasetId } = state;
    const thumbState = reactive({
      type: props.state.fileFilterType.value, // 文件筛选状态
      showDialog: false,
      gotoNumber: 1,
    });

    // 名称
    const activeAnnotationName = computed(() => {
      const selectedType = thumbState.type;
      return fileTypeEnum[selectedType].abbr;
    });
    // 下拉列表
    const dropdownList = computed(() => {
      let filter = [];
      if (isTrack) {
        // 目标跟踪：全部 未标注 未识别 手动标注中 手动标注完成 自动标注完成 目标跟踪完成
        filter = pick(fileTypeEnum, [fileCodeMap.ALL, fileCodeMap.UNANNOTATED, fileCodeMap.UNRECOGNIZED, fileCodeMap.MANUAL_ANNOTATING, fileCodeMap.MANUAL_ANNOTATED, fileCodeMap.AUTO_ANNOTATED, fileCodeMap.TRACK_SUCCEED]);
      } else {
        // 目标检测：全部 未标注 未识别 手动标注中 自动标注完成 手动标注完成
        filter = pick(fileTypeEnum, [fileCodeMap.ALL, fileCodeMap.UNANNOTATED, fileCodeMap.UNRECOGNIZED, fileCodeMap.MANUAL_ANNOTATING, fileCodeMap.AUTO_ANNOTATED, fileCodeMap.MANUAL_ANNOTATED]);
      }
      const statusList = Object.keys(filter).map(k => ({
        command: k,
        label: fileTypeEnum[k].label,
      }));
      return statusList;
    });

    const handleDropdown = (command) => {
      Object.assign(thumbState, {
        type: command,
        gotoNumber: 1,
      });
      updateState({ annotations: [], fileFilterType: command });
      // 重新请求文件
      updateList({ type: command, offset: 0 });
      // 获取滚动列表容器
      const listWrapper = listRef.value.$refs?.listWrapper;
      listWrapper.scrollTo({
        top: 0,
      });
    };

    const handleClose = () => {
      thumbState.showDialog = false;
    };

    const uploadSuccess = async(res) => {
      const files = getImgFromMinIO(res);
      // 提交业务上传
      submit(datasetId.value, files).then(() => {
        Message.success('上传成功');
        updateList({ type: thumbState.type });
      });
    };

    const uploadError = (err) => {
      Message.error('上传失败', err);
      console.error(err);
    };

    const handleUpload = () => {
      thumbState.showDialog = true;
    };

    const uploadParams = {
      datasetId: datasetId.value,
      objectPath: `dataset/${datasetId.value}/origin`, // 对象存储路径
    };

    const handleKeyup = async({ target, keyCode }) => {
      let {value} = target;
      if (keyCode === 13) {
        if (parseInt(target.value, 10) < parseInt(target.min, 10)) {
          thumbState.gotoNumber = target.min;
          value = target.min;
        }
        if (parseInt(target.value, 10) > parseInt(target.max, 10)) {
          thumbState.gotoNumber = target.max;
          value = target.max;
        }

        // 获取图片id，跳转对应图片详情页
        const res = await detectFileList(datasetId.value, {
          offset: value - 1,
          limit: 1,
        });
        const endStrReg = /(\/file\/)(\d+)$/;
        if (res.result.length > 0) {
          if (state.currentImgId.value === res.result[0].id) {
            return;
          }
          const nextPath = $route.path.replace(endStrReg, `$1${res.result[0].id}`);
          window.location.replace(nextPath);
        }
      }
    };

    watch(() => state.currentImgId.value, async(next) => {
      // 根据筛选类型来过滤
      const query = {
        type: thumbState.type,
      };
      if (!next) return;
      const currentImgIndex = await queryFileOffset(datasetId.value, next, query);
      thumbState.gotoNumber = currentImgIndex + 1;
    }, {
      lazy: false,
    });

    return {
      listRef,
      thumbState,
      withDimensionFile,
      uploadParams,
      dropdownList,
      handleUpload,
      handleClose,
      uploadSuccess,
      uploadError,
      activeAnnotationName,
      handleDropdown,
      uploaderRef,
      handleKeyup,
    };
  },
};
</script>
<style lang="scss">
@import "~@/assets/styles/variables.scss";
@import "~@/assets/styles/mixin.scss";

.thumb-wrapper {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  width: 160px;
  padding-top: 20px;
  margin-bottom: 34px;
  text-align: center;
  background: #fff;
  box-shadow: 2px 0 6px 0 rgba(0, 0, 0, 0.15);

  .file-infobar {
    padding: 0 8px;
    font-size: 16px;
    line-height: 32px;
    border-top: 1px solid $borderColor;
    border-bottom: 1px solid $borderColor;

    .el-dropdown {
      display: inline-block;
      width: 66%;
      white-space: nowrap;
    }
  }

  .thumb-list-item {
    @include responsive-backgroud;

    width: 80px;
    height: 64px;
    padding: 4px;
    margin: 12px auto 0;
    cursor: pointer;

    &.active {
      border-radius: 2px;
      box-shadow: 0 2px 4px 0 rgba(64, 64, 64, 0.5);
    }
  }

  .infinite-list-wrapper {
    flex: 1;
  }

  .annotate-pagination {
    span {
      display: inline-block;
      margin-left: 0;
    }

    .el-icon-question {
      line-height: 28px;
    }
  }
}
</style>
