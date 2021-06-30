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
    title="创建数据集"
    width="630px"
    :loading="state.loading"
    :visible="state.visible"
    @change="handleClose"
    @ok="handleOk"
  >
    <el-form ref="formRef" :model="state.form" :rules="rules" label-width="100px">
      <el-form-item label="数据集名称" prop="name">
        <el-input v-model="state.form.name" placeholder="数据集名称不能超过50字" maxlength="50" />
      </el-form-item>
      <el-form-item label="数据类型" prop="dataType">
        <InfoRadio
          v-model="state.form.dataType"
          :dataSource="dataTypeList"
          :transformOptions="transformOptions"
          type="button"
          @change="handleDataTypeChange"
        />
      </el-form-item>
      <el-form-item label="标注类型" prop="annotateType">
        <div class="image-select flex flex-wrap">
          <div
            v-for="item in annotationList"
            :key="item.code"
            :class="getImageKlass(item)"
            @click="selectAnnotationType(item)"
          >
            <div class="image-title">{{ item.name }}</div>
            <img class="pic responsive" :src="getImgUrl(item)" />
            <span>
              <i class="check-icon" />
            </span>
          </div>
        </div>
        <el-input :value="state.form.annotateType" class="dn" />
      </el-form-item>
      <div style="position: relative; top: -10px; margin-left: 100px;">
        更多标注类型说明参考<a
          target="_blank"
          type="primary"
          :underline="false"
          class="primary"
          :href="`${VUE_APP_DOCS_URL}module/dataset/intro`"
          >官方文档</a
        >
      </div>
      <el-form-item v-if="showlabelGroup" label="标签组" style="height: 32px;" prop="labelGroup">
        <el-cascader
          v-model="state.form.labelGroup"
          clearable
          placeholder="标签组"
          :options="labelGroupOptions"
          :props="{ expandTrigger: 'hover' }"
          :show-all-levels="false"
          filterable
          popper-class="group-cascader"
          style="width: 100%; line-height: 32px;"
          @change="handleGroupChange"
        >
          <div slot="empty">
            <span>没有找到标签组？去</span>
            <a
              target="_blank"
              type="primary"
              :underline="false"
              class="primary"
              :href="`/data/labelgroup/create`"
            >
              新建标签组
            </a>
            <span>页面创建</span>
          </div>
        </el-cascader>
        <div style="position: relative; top: -33px; right: 30px; float: right;">
          <el-link
            v-if="state.form.labelGroupId !== null"
            target="_blank"
            type="primary"
            :underline="false"
            class="vm"
            :href="`/data/labelgroup/detail?id=${state.form.labelGroupId}`"
          >
            查看详情
          </el-link>
        </div>
      </el-form-item>
      <div
        v-if="state.form.labelGroupId === null && showlabelGroup"
        style="position: relative; top: -10px; margin-left: 100px;"
      >
        <span>标签组需要在</span>
        <a
          target="_blank"
          type="primary"
          :underline="false"
          class="primary"
          :href="`/data/labelgroup/create`"
        >
          新建标签组
        </a>
        <span>页面创建</span>
      </div>
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
import { onMounted, reactive, watch, ref, computed } from '@vue/composition-api';
import { Message } from 'element-ui';
import cx from 'classnames';
import { set, omit } from 'lodash';

import {
  dataTypeMap,
  transformMapToList,
  annotationBy,
  enableLabelGroup,
  dataTypeCodeMap,
} from '@/views/dataset/util';
import BaseModal from '@/components/BaseModal';
import InfoRadio from '@/components/InfoRadio';
import { isEmptyValue } from '@/utils';
import { validateName } from '@/utils/validate';

import { getLabelGroupList } from '@/api/preparation/labelGroup';
import { add } from '@/api/preparation/dataset';

const annotationByDataType = annotationBy('dataType');

// 数据类型 => 图像
const imageNameMap = {
  imageClassify: 'classify.png',
  targetDetection: 'annotate.png',
  segmentation: 'segmentation.png',
  objectTracking: 'track.png',
  textClassify: 'textclassify.png',
  audioClassify: 'audioClassify.png',
  textSegmentation: 'textsegmentation.png',
  ner: 'ner.png',
  speechRecognition: 'speechRecognition.png',
  custom: 'custom.png',
};

export default {
  name: 'CreateDataset',
  components: {
    BaseModal,
    InfoRadio,
  },
  props: {
    visible: Boolean,
    toggleVisible: Function,
    onResetFresh: Function,
  },
  setup(props) {
    const { toggleVisible, onResetFresh } = props;
    const initialForm = {
      name: '',
      dataType: 0,
      annotateType: 2,
      labelGroup: null,
      labelGroupId: null,
      remark: '',
      type: 0,
    };
    const formRef = ref(null);

    const initialLabelGroupOptions = [
      {
        value: 'custom',
        label: '自定义标签组',
        disabled: false,
        children: [],
      },
      {
        value: 'system',
        label: '预置标签组',
        disabled: false,
        children: [],
      },
    ];

    const state = reactive({
      form: initialForm,
      formKey: 1,
      visible: props.visible,
      loading: false, // 数据集创建进行中
    });

    const labelGroupOptions = ref(initialLabelGroupOptions);

    const rules = {
      name: [
        {
          required: true,
          message: '请输入数据集名称',
          trigger: ['change', 'blur'],
        },
        { validator: validateName, trigger: ['change', 'blur'] },
      ],
      dataType: [{ required: true, message: '请选择数据类型', trigger: 'change' }],
      annotateType: [{ required: true, message: '请选择标注类型', trigger: 'change' }],
      labelGroup: [{ required: true, message: '请选择标签组', trigger: 'change' }],
    };

    // 是否展示标签组
    const showlabelGroup = computed(() => enableLabelGroup(state.form.annotateType));

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
          annotateType: 2,
          labelGroup: null,
          labelGroupId: null,
          remark: '',
          type: 0,
        },
      });

    // 更新标签组信息
    const updateLabelGroupOptions = (index, path, params) => {
      labelGroupOptions.value[index] = set(labelGroupOptions.value[index], path, params);
    };

    const annotationList = computed(() => annotationByDataType(state.form.dataType));

    const transformOptions = (list) => {
      return list.map((d) => ({
        ...d,
        label: d.label,
        value: Number(d.value),
      }));
    };

    // eslint-disable-next-line
    const getImgUrl = item => {
      try {
        // eslint-disable-next-line
        return require(`@/assets/images/dataset/${imageNameMap[item.type]}`);
      } catch (err) {
        console.error(err);
      }
    };

    const getImageKlass = (item) =>
      cx(`image-select-item item-${item.urlPrefix}`, {
        'is-active': item.code === Number(state.form.annotateType),
      });

    // 构建标签组信息
    const buildLabelOptions = (data) =>
      data.map((d) => ({
        value: d.id,
        label: d.name,
        disabled: false,
      }));

    const updateLabelGroup = () => {
      const { dataType, annotateType } = state.form || {};
      if (!isEmptyValue(dataType) && !isEmptyValue(annotateType)) {
        // 遍历两层
        [0, 1].forEach((d) => {
          getLabelGroupList({ type: d, dataType, annotateType }).then((res) => {
            const options = buildLabelOptions(res);
            updateLabelGroupOptions(d, 'children', options);
          });
        });
      }
    };

    const selectAnnotationType = (item) => {
      if (item.code === Number(state.form.annotateType)) return;
      setForm({
        annotateType: item.code,
        labelGroup: null,
        labelGroupId: null,
      });
      updateLabelGroup();
    };

    const handleDataTypeChange = () => {
      // 默认定位到第一个标注场景
      if (annotationList.value.length) {
        setForm({
          annotateType: annotationList.value[0].code,
          labelGroup: null,
          labelGroupId: null,
        });
      }
      updateLabelGroup();
    };

    const handleGroupChange = (val) => {
      if (val.length === 0) {
        setForm({ labelGroup: null, labelGroupId: null });
      } else {
        setForm({ labelGroup: val, labelGroupId: val[1] });
      }
    };

    const handleClose = () => {
      Object.assign(state, {
        formKey: state.formKey + 1,
        // reactive mutate 原始对象
        form: {
          name: '',
          dataType: 0,
          annotateType: 2,
          labelGroup: null,
          labelGroupId: null,
          remark: '',
          type: 0,
        },
        loading: false,
      });
      toggleVisible();
      onResetFresh();
    };

    const handleOk = () => {
      formRef.value.validate((valid) => {
        if (!valid) return;
        const params = omit(state.form, ['labelGroup']);
        params.import = params.dataType === dataTypeCodeMap.CUSTOM;
        setLoading(true);
        add(params)
          .then(() => {
            Message.success('数据集创建成功');
            resetForm();
            toggleVisible();
          })
          .finally(() => {
            setLoading(false);
          });
      });
    };

    watch(
      () => props.visible,
      (next) => {
        next !== state.visible &&
          Object.assign(state, {
            visible: next,
          });
      }
    );

    onMounted(() => {
      annotationList.value.forEach((item) => getImgUrl(item));
      // 初始化标签组
      updateLabelGroup();
    });

    return {
      state,
      formRef,
      VUE_APP_DOCS_URL: process.env.VUE_APP_DOCS_URL,
      dataTypeList: transformMapToList(dataTypeMap),
      selectAnnotationType,
      handleDataTypeChange,
      handleGroupChange,
      transformOptions,
      labelGroupOptions,
      getImageKlass,
      getImgUrl,
      annotationList,
      handleClose,
      handleOk,
      rules,
      showlabelGroup,
    };
  },
};
</script>
<style lang="scss">
@import '../style/common.scss';
</style>
<style lang="scss" scoped>
.image-select-item {
  max-width: 33.3333333%;
}
</style>
