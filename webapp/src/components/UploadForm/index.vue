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
  <BaseModal
    class="upload-dialog"
    :title="title"
    :visible.sync="state.visible"
    :loading="state.uploading"
    width="600px"
    @close="handleCancel"
    @cancel="handleCancel"
    @ok="uploadSubmit"
  >
    <Form
      ref="formRef"
      v-bind="$attrs"
    />
    <slot v-bind="state" />
  </BaseModal>
</template>
<script>
import { ref, reactive, watch } from '@vue/composition-api';

import BaseModal from '@/components/BaseModal';
import Form from './form';
import { minIOUpload, hashify, getFileOutputPath } from './util';

export default {
  name: 'UploadDialog',
  components: {
    BaseModal,
    Form,
  },
  inheritAttrs: false,
  props: {
    title: {
      type: String,
      default: '导入文件',
    },
    visible: {
      type: Boolean,
      default: false,
    },
    transformFile: Function,
    hash: {
      type: Boolean,
      default: true,
    },
    toggleVisible: Function,
    request: Function,
    params: {
      type: Object,
      default: () => ({}),
    },
  },
  setup(props, ctx) {
    const { toggleVisible, request, transformFile } = props;
    const formRef = ref(null);
    const state = reactive({
      visible: props.visible,
      uploading: false,
      progress: 0, // 上传进度
    });

    // 上传进度
    const handleUploadProgress = () => {
      // console.log('resolved', resolved, total)
    };

    // 提交结果
    const uploadSubmit = () => {
      const fileList = (formRef.value?.$refs.uploader || {}).uploadFiles;

      // 重命名
      const renameFileList = fileList.map(file => ({
        ...file,
        name: hashify(file.name, props.hash),
      }));

      if (!fileList || !fileList.length) {
        throw new Error('文件不能为空');
      }

      const uploadReqeust = request || minIOUpload;

      state.uploading = true;
      // 开始调用上传接口
      uploadReqeust && uploadReqeust({ ...props.params, fileList: renameFileList, transformFile }, handleUploadProgress)
        .then(res => {
          const outputPath = getFileOutputPath(renameFileList, props.params);
          state.uploading = false;
          toggleVisible();
          // 清空已有的记录
          formRef.value.reset();
          ctx.emit('uploadSuccess', res, outputPath);
        })
        .catch(err => {
          state.uploading = false;
          toggleVisible();
          ctx.emit('uploadError', err);
        });
    };

    const handleCancel = () => {
      // 清空已有的记录
      formRef.value.reset();
      toggleVisible();
      ctx.emit('close');
    };

    watch(() => props.visible, (next) => {
      next !== state.visible && Object.assign(state, {
        visible: next,
      });
    });

    return {
      state,
      formRef,
      uploadSubmit,
      handleCancel,
    };
  },
};
</script>
