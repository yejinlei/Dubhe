<template>
  <div style="width: 600px;">
    <el-form-item label="文件类型" prop="fileType">
      <div class="image-select flex flex-wrap">
        <div
          v-for="item in fileTypeList"
          :key="item"
          :class="getImageKlass(item)"
          @click="selectFileType(item)"
        >
          <div class="image-title">{{ item }}</div>
          <IconFont :type="item" class="fileIcon" />
          <span>
            <i class="check-icon" />
          </span>
        </div>
      </div>
      <el-input :value="state.form.fileType" class="dn" />
      <div class="el-form-item__tip"><el-link>没有数据？查看并下载预置模板</el-link></div>
    </el-form-item>
    <el-form-item label="上传文件" prop="file">
      <upload-inline
        :key="state.form.fileType"
        ref="fileUploadForm"
        action="fakeApi"
        v-bind="uploadOptions"
        :on-remove="fileRemove"
        @fileChange="fileChange"
      />
    </el-form-item>
    <div style="margin: 25px 0 0 100px;">
      <el-button type="primary" :loading="loading" @click="preview">
        下一步
      </el-button>
    </div>
  </div>
</template>
<script>
import Vue from 'vue';
import { computed, ref } from '@vue/composition-api';
import cx from 'classnames';

import UploadInline from '@/components/UploadForm/inline';
import { tableUploadProps } from '@/views/dataset/util';

export default {
  name: 'UploaderTable',
  components: {
    UploadInline,
  },
  props: {
    fileTypeList: Array,
    state: Object,
    setState: Function,
    setForm: Function,
    tableForm: Object,
    validateField: Function,
    loading: Boolean,
  },
  setup(props, ctx) {
    const { setForm, validateField } = props;
    const fileUploadForm = ref(null);

    const getImageKlass = (item) =>
      cx(`image-select-item item-${item}`, {
        'is-active': item === props.state.form.fileType,
      });

    const selectFileType = (item) => {
      if (item === props.state.form.fileType) return;
      setForm({ fileType: item, file: null });
    };

    const preview = () => {
      props.tableForm.validate((isValid) => {
        if (!isValid) return;
        ctx.emit('preview', props.state.form.file.raw, props.state.form.fileType);
      });
    };

    const fileChange = (file) => {
      setForm({ file });
      Vue.nextTick(() => {
        validateField('file');
      });
    };

    const fileRemove = () => {
      setForm({ file: null });
    };

    const uploadOptions = computed(() => {
      const accept = props.state.form.fileType === 'csv' ? '.csv' : '.xls,.xlsx';
      return {
        ...tableUploadProps,
        accept,
        hash: true,
      };
    });

    return {
      uploadOptions,
      getImageKlass,
      selectFileType,
      preview,
      fileChange,
      fileRemove,
      fileUploadForm,
    };
  },
};
</script>
