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
  <el-dialog
    :key="state.uploadKey"
    :closeOnClickModal="false"
    append-to-body
    width="610px"
    :visible="visible"
    :title="state.title"
    @close="handleClose"
  >
    <!--选择上传的文件-->
    <div v-show="state.uploadStep === 0">
      <upload-inline
        ref="fileUploadForm"
        action="fakeApi"
        :accept="state.accept"
        :params="state.uploadParams"
        :transformFile="withDimensionFile"
        v-bind="state.optionUploadProps"
        @uploadSuccess="uploadSuccess"
        @uploadError="uploadError"
      />
      <!--上传视频时显示帧间隔设置-->
      <el-form
        v-if="!state.isImage"
        ref="formStep"
        :model="state.form"
        label-width="100px"
        style="margin-top: 10px;"
      >
        <el-form-item
          label="视频帧间隔"
          prop="frameInterval"
          :rules="[{required: true, message: '请输入有效的帧间隔', trigger: 'blur'}]"
        >
          <el-input-number v-model="state.form.frameInterval" :min="1" />
        </el-form-item>
      </el-form>
    </div>
    <!--上传文件进度展示-->
    <div v-show="state.uploadStep === 1">
      <el-progress
        v-if="state.isImage"
        type="circle"
        :percentage="state.percentage"
        :status="state.uploadStatus"
        :format="format"
      />
      <div v-else class="circleProgressWrapper">
        <div class="circleText">正在上传</div>
        <div class="wrapper right">
          <div class="circleProgress rightCircle"></div>
        </div>
        <div class="wrapper left">
          <div class="circleProgress leftCircle"></div>
        </div>
      </div>
    </div>
    <!--上传成功-->
    <div v-show="state.uploadStep === 2">
      <el-progress type="circle" :percentage="100" status="success" />
    </div>
    <div slot="footer">
      <div v-show="state.uploadStep === 0">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="uploadSubmit('fileUploadForm')">开始上传</el-button>
      </div>
      <div v-show="state.uploadStep === 1">
        <el-button @click="handleClose">取消</el-button>
      </div>
      <div v-show="state.uploadStep === 2">
        <el-button type="primary" @click="handleClose">完成</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script>

import Vue from 'vue';
import { reactive, watch } from '@vue/composition-api';
import { toFixed } from '@/utils';
import UploadInline from '@/components/UploadForm/inline';
import { getImgFromMinIO, withDimensionFile, trackUploadProps } from '@/views/dataset/util';
import { submit, submitVideo } from '@/api/preparation/datafile';
import { Message } from 'element-ui';

export default {
  name: 'UploadDataFile',
  components: {
    UploadInline,
  },
  props: {
    row: {
      type: Object,
      default: () => {},
    },
    visible: {
      type: Boolean,
      default: false,
    },
    loading: {
      type: Boolean,
      default: false,
    },
    closeUploadDataFile: {
      type: Function,
    },
  },
  setup(props, context) {
    const defaultFrameInterval = 5;
    const { closeUploadDataFile } = props;
    const state = reactive({
      uploadKey: 1,
      row: {},
      uploadStep: 0,
      isImage: undefined,
      accept: "",
      title: "",
      uploadParams: {},
      optionUploadProps: {},
      percentage: 0,
      uploadStatus: undefined,
      form: {
        frameInterval: defaultFrameInterval,
      },
    });

    // 监测选中导入的列数据变化
    watch(() => props.row, (next) => {
      Object.assign(state, {
        row: { ...state.row, ...next },
      });
      const { id } = state.row;
      if (state.row.dataType === 0) {
        Object.assign(state, {
          isImage: true,
          title: "导入图片",
          accept: ".jpg,.png,.bmp,.jpeg",
          uploadParams: {
            datasetId: id,
            objectPath: `dataset/${id}/origin`, // 图片对象存储路径
          },
          optionUploadProps: {},
        });
      } else {
        Object.assign(state, {
          isImage: false,
          title: "导入视频",
          uploadParams: {
            datasetId: id,
            objectPath: `dataset/${id}/video`, // 图片对象存储路径
          },
          accept: ".mp4,.avi,.mkv,.mov,.webm,.wmv",
          optionUploadProps: trackUploadProps,
        });
      }
    });

    // 上传包括图片和视频
    const uploader = async (datasetId, files) => {
      // 文件上传
      if (state.isImage) {
        return submit(datasetId, files);
      }
      return submitVideo(datasetId, {
        frameInterval: state.form.frameInterval,
        url: files[0].url,
      });
    };

    // 上传视频时不显示实时进度
    const format = (percentage) => {
      return percentage < 100 ? `${percentage}%` : ``;
    };

    // 上传成功
    const uploadSuccess = (res) => {
      // 视频上传完毕
      if (!state.isImage) {
        state.percentage = 100;
      }
      const files = getImgFromMinIO(res);
      // 自动标注完成时 导入 提示信息不同
      const successMessage = "上传文件成功";
      if (files.length > 0) {
        uploader(state.row.id, files).then(() => {
          Message.success({ message: successMessage, duration: 1000 });
        });
      }
      Object.assign(state, {
        loading: false,
        uploadStatus: "success",
        uploadStep: 2,
        title: "上传成功",
      });
    };

    // 上传失败
    const uploadError = () => {
      state.loading = false;
      state.uploadStatus = "exception";
      Message.error({ message: "上传失败", duration: 1000 });
    };

    // 确定上传
    const uploadSubmit = formName => {
      context.refs[formName].uploadSubmit((resolved, total) => {
        // eslint-disable-next-line func-names
        Vue.nextTick(function() {
          state.percentage =
            state.percentage > 100 ? 100 : toFixed(resolved / total);
        });
      });
      Object.assign(state, {
        loading: true,
        uploadStep: 1,
        title: "上传中",
      });
    };

    const handleClose = () => {
      closeUploadDataFile();
      Object.assign(state, {
        uploadStep: 0,
        uploadKey: state.uploadKey + 1,
        percentage: 0,
        uploadStatus: undefined,
      });
    };

    return {
      state,
      uploadSubmit,
      format,
      handleClose,
      withDimensionFile,
      uploadSuccess,
      uploadError,
    };
  },
};
</script>