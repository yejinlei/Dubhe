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
  <BaseModal
    :key="state.formKey"
    title="导入本地数据集"
    width="600px"
    center
    :loading="state.loading"
    :visible="state.visible"
    @change="handleClose"
    @ok="handleOk"
  >
    <el-form ref="formRef" :model="state.form" :rules="rules" label-width="100px">
      <el-alert class="info-alert" type="warning" show-icon :closable="false">
        <div slot="title" class="slot-content">
          <div>数据集创建完毕后，需要使用脚本工具上传本地已有数据集</div>
          <a :href="`${VUE_APP_DOCS_URL}module/dataset/dataset-util`" target="_blank">使用文档</a>
        </div>
      </el-alert>
      <el-form-item label="数据集名称" prop="name">
        <el-input v-model="state.form.name" placeholder="数据集名称不能超过50字" maxlength="50" />
      </el-form-item>
      <el-form-item label="数据集来源" prop="sourceType">
        <InfoRadio v-model="state.form.sourceType" :dataSource="sourceTypeList" />
        <div>
          标准数据集是指天枢平台预置支持的部分数据集类型，
          <a
            target="_blank"
            type="primary"
            :underline="false"
            class="primary"
            :href="`${VUE_APP_DOCS_URL}module/dataset/intro`"
            >详细参考</a
          >
        </div>
      </el-form-item>
      <el-form-item v-if="!sourceByCustom" label="数据类型" prop="dataType">
        <InfoRadio
          v-model="state.form.dataType"
          :dataSource="dataTypeList"
          :transformOptions="transformOptions"
          type="button"
          @change="handleDataTypeChange"
        />
      </el-form-item>
      <el-form-item v-if="!sourceByCustom" label="标注类型" prop="annotateType">
        <InfoSelect
          v-model="state.form.annotateType"
          placeholder="标注类型"
          :dataSource="annotationList"
          width="200px"
        />
      </el-form-item>
      <el-form-item label="数据集描述">
        <el-input
          v-model="state.form.remark"
          type="textarea"
          placeholder="数据集描述长度不能超过100字"
          maxlength="100"
          rows="3"
          show-word-limit
        />
      </el-form-item>
    </el-form>
  </BaseModal>
</template>

<script>
import { reactive, watch, ref, computed } from '@vue/composition-api';
import { Message } from 'element-ui';
import { omit } from 'lodash';

import BaseModal from '@/components/BaseModal';
import InfoRadio from '@/components/InfoRadio';
import InfoSelect from '@/components/InfoSelect';

import { validateName } from '@/utils/validate';
import {
  annotationBy,
  dataTypeMap,
  dataTypeCodeMap,
  annotationCodeMap,
  transformMapToList,
  extDataAnnotationByCode,
} from '@/views/dataset/util';

import { add } from '@/api/preparation/dataset';

const annotationByDataType = annotationBy('dataType');

export default {
  name: 'ImportDataset',
  components: {
    BaseModal,
    InfoRadio,
    InfoSelect,
  },
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    toggleVisible: {
      type: Function,
    },
    onResetFresh: {
      type: Function,
    },
  },
  setup(props) {
    const { toggleVisible, onResetFresh } = props;
    const initialForm = {
      name: '',
      dataType: 0,
      annotateType: 2,
      remark: '',
      loading: false,
      sourceType: 0,
    };

    const formRef = ref(null);

    // 标准数据集白名单：图像分类、目标检测、语义分割
    // 文本分类
    // 音频分类
    const stdAnnotateType = [
      annotationCodeMap.ANNOTATE,
      annotationCodeMap.CLASSIFY,
      annotationCodeMap.SEGMENTATION,
      annotationCodeMap.TEXTCLASSIFY,
      annotationCodeMap.AUDIOCLASSIFY,
    ];

    const rules = {
      name: [
        {
          required: true,
          message: '请输入数据集名称',
          trigger: ['change', 'blur'],
        },
        { validator: validateName, trigger: ['change', 'blur'] },
      ],
      sourceType: [{ required: true, message: '请选择数据集来源', trigger: 'change' }],
      dataType: [{ required: true, message: '请选择数据类型', trigger: 'change' }],
      annotateType: [{ required: true, message: '请选择标注类型', trigger: ['change', 'blur'] }],
    };

    const state = reactive({
      form: initialForm,
      formKey: 1,
      visible: props.visible,
      loading: false, // 数据集创建进行中
    });

    const sourceTypeList = [
      {
        label: '自定义数据集',
        value: 0,
      },
      {
        label: '标准数据集',
        value: 1,
      },
    ];

    // 是否为自定义来源
    const sourceByCustom = computed(() => state.form.sourceType === 0);

    const dataTypeList = computed(() => {
      const transformed = transformMapToList(
        omit(dataTypeMap, [dataTypeCodeMap.TABLE, dataTypeCodeMap.CUSTOM, dataTypeCodeMap.VIDEO])
      );
      return transformed.map((d) => ({
        ...d,
        value: Number(d.value),
      }));
    });

    const annotationList = computed(() =>
      annotationByDataType(state.form.dataType)
        .filter((d) => stdAnnotateType.includes(d.code))
        .map((d) => ({
          value: d.code,
          label: d.name,
        }))
    );

    const setForm = (params) =>
      Object.assign(state, {
        form: {
          ...state.form,
          ...params,
        },
      });

    // 更新加载状态
    const setLoading = (loading) => Object.assign(state, { loading });

    // 重置状态（reactive mutate 原始对象）
    const resetForm = () =>
      Object.assign(state, {
        form: {
          name: '',
          dataType: 0,
          sourceType: 0,
          annotateType: 2,
          remark: '',
        },
      });

    const handleDataTypeChange = () => {
      // 默认定位到第一个标注场景
      if (annotationList.value.length) {
        setForm({
          annotateType: annotationList.value[0].value,
        });
      }
    };

    const selectAnnotationType = (item) => {
      if (item.code === Number(state.form.annotateType)) return;
      setForm({
        annotateType: item.code,
      });
    };

    const handleClose = () => {
      Object.assign(state, {
        formKey: state.formKey + 1,
        // reactive mutate 原始对象
        form: {
          name: '',
          dataType: 0,
          sourceType: 0,
          annotateType: 2,
          remark: '',
        },
        loading: false,
      });
      toggleVisible(false);
      onResetFresh();
    };

    const handleOk = () => {
      formRef.value.validate((valid) => {
        if (!valid) return;
        const params = { type: 0, import: true, name: state.form.name, remark: state.form.remark };
        // 区分自定义数据集、标注数据集
        state.form.sourceType === 0
          ? Object.assign(params, {
              dataType: dataTypeCodeMap.CUSTOM,
              annotateType: extDataAnnotationByCode(dataTypeCodeMap.CUSTOM)[0],
            })
          : Object.assign(params, {
              dataType: state.form.dataType,
              annotateType: state.form.annotateType,
            });
        setLoading(true);
        add(params)
          .then(() => {
            Message.success('数据集创建成功，请下载数据集脚本工具进行下一步操作');
            resetForm();
            toggleVisible(false);
          })
          .finally(() => {
            setLoading(false);
          });
      });
    };

    const transformOptions = (list) => {
      return list.map((d) => ({
        ...d,
        label: d.label,
        value: Number(d.value),
      }));
    };

    watch(
      () => props.visible,
      (next) => {
        Object.assign(state, {
          visible: next,
        });
      }
    );

    return {
      VUE_APP_DOCS_URL: process.env.VUE_APP_DOCS_URL,
      rules,
      state,
      formRef,
      sourceTypeList,
      sourceByCustom,
      dataTypeList,
      annotationList,
      transformOptions,
      handleDataTypeChange,
      selectAnnotationType,
      handleClose,
      handleOk,
    };
  },
};
</script>
