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
    :visible="visible"
    :loading="loading"
    title="修改数据集"
    @change="handleCancel"
    @ok="handleEditDataset"
  >
    <el-form ref="form" :model="state.model" :rules="rules" label-width="100px">
      <el-form-item label="数据集名称" prop="name">
        <el-input v-model="state.model.name" placeholder="数据集名称不能超过50字" maxlength="50" />
      </el-form-item>
      <el-form-item label="数据类型" prop="dataType">
        <InfoSelect
          v-model="state.model.dataType"
          placeholder="数据类型"
          :dataSource="dataTypeList"
          disabled
        />
      </el-form-item>
      <el-form-item v-if="!state.model.import" label="标注类型" prop="annotateType">
        <InfoSelect
          v-model="state.model.annotateType"
          placeholder="标注类型"
          :dataSource="annotationList"
          disabled
        />
      </el-form-item>
      <el-form-item v-if="!state.model.import" label="标签组" prop="labelGroupId">
        <div v-if="editable" class="label-input">
          <el-popover
            ref="popoverRef"
            v-model="state.popoverVisible"
            placement="top"
            trigger="click"
            popper-class="label-group-popover"
          >
            <div class="add-label-tag">
              <el-tabs v-model="state.labelGroupTab" type="border-card">
                <el-tab-pane label="自定义标签组" name="custom">
                  <el-select 
                    v-model="state.customLabelGroupId" 
                    filterable 
                    placeholder="请选择" 
                    popper-class="label-group-select"
                    @change="handleCustomId"
                  >
                    <el-option
                      v-for="item in customLabelGroups"
                      :key="item.labelGroupId"
                      :label="item.name"
                      :value="item.labelGroupId"
                    >
                    </el-option>
                  </el-select>
                </el-tab-pane>
                <el-tab-pane label="预置标签组" name="system" :disabled="!systemLabelEnabled">
                  <el-select 
                    v-model="state.systemLabelGroupId" 
                    filterable 
                    placeholder="请选择" 
                    @change="handleSystemId"
                  >
                    <el-option
                      v-for="item in systemLabelGroups"
                      :key="item.labelGroupId"
                      :label="item.name"
                      :value="item.labelGroupId"
                      :disabled="!optionEnabled(item.labelGroupId, state.model.annotateType)"
                    >
                    </el-option>
                  </el-select>
                </el-tab-pane>
              </el-tabs>
            </div>
            <el-button slot="reference" type="text">
              &nbsp;
              <span v-if="state.model.labelGroupId === null">&nbsp;&nbsp;标签组</span>
              <el-tag v-else :closable="deletable" @close="handleRemoveLabelGroup()">
                {{state.model.labelGroupName}}                
              </el-tag>
            </el-button>
          </el-popover>
          <el-link 
            v-if="state.model.labelGroupId !== null" 
            target="_blank" 
            type="primary" 
            :underline="false" 
            class="vm" 
            :href="`/data/labelgroup/detail?id=${state.model.labelGroupId}`"
            style="float: right; margin-right: 8px;"  
          >
            查看详情
          </el-link>
        </div>
        <div v-else class="label-input" style="color: #c0c4cc; background-color: #f5f7fa;">
          &nbsp;&nbsp;&nbsp;&nbsp;{{state.model.labelGroupName}}
          <el-link 
            v-if="state.model.labelGroupId !== null" 
            target="_blank" 
            type="primary" 
            :underline="false" 
            class="vm" 
            :href="`/data/labelgroup/detail?id=${state.model.labelGroupId}`"
            style="float: right; margin-right: 8px;"  
          >
            查看详情
          </el-link>
        </div>
      </el-form-item>
      <el-form-item label="数据集描述" prop="remark">
        <el-input
          v-model="state.model.remark"
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
import {isNil} from 'lodash'; 
import { watch, reactive, computed, ref, onMounted } from '@vue/composition-api';

import BaseModal from '@/components/BaseModal';
import InfoSelect from '@/components/InfoSelect';
import { validateName } from '@/utils/validate';
import { annotationMap, dataTypeMap, statusCodeMap  } from '@/views/dataset/util';
import { getLabelGroupList } from '@/api/preparation/labelGroup';

export default {
  name: 'EditDataset',
  components: {
    BaseModal,
    InfoSelect,
  },
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    loading: {
      type: Boolean,
      default: false,
    },
    handleCancel: Function,
    handleOk: Function,
    goLabelGroupDetail: Function,
    row: {
      type: Object,
      default: () => {},
    },
  },
  setup(props, { refs }) {
    const { handleOk } = props;
    const popoverRef = ref(null);
    const systemLabelGroups = [];
    const customLabelGroups = [];

    const rules= {
      name: [
        { required: true, message: '请输入数据集名称', trigger: ['change', 'blur'] },
        { validator: validateName, trigger: ['change', 'blur'] },
      ],
      dataType: [
        { required: true, message: '请选择数据类型', trigger: 'change' },
      ],
      annotateType: [
        { required: true, message: '请选择标注类型', trigger: 'change' },
      ],
      remark: [
        { required: false, message: '请输入数据集描述信息', trigger: 'blur' },
      ],
    };

    const buildModel = (record, options) => {
      return { ...record, ...options};
    };

    const state = reactive({
      model: buildModel(props.row),
      popoverVisible: false,
      labelGroupTab: "custom",
      customLabelGroupId: null,
      systeomLabelGroupId: null,
    });

    const systemLabelEnabled = computed(() => {
      return props.row.annotateType !== 5;
    });

    const deletable = computed(() => {
      return isNil(props.row.labelGroupId);
    });

    const dataTypeList = computed(() => {
      return Object.keys(dataTypeMap).map(d => ({
        label: dataTypeMap[d],
        value: Number(d),
      }));
    });

    const annotationList = computed(() => {
      // 原始标注列表
      const rawAnnotationList = Object.keys(annotationMap).map(d => ({
        label: annotationMap[d].name,
        value: Number(d),
      }));
      // 如果是图片，目标跟踪不可用
      // 如果是视频，只能用目标跟踪
      return rawAnnotationList.map(d => {
        let disabled = false;
        if (state.model.dataType === 0) {
          disabled = d.value === 5;
        } else if (state.model.dataType === 1) {
          disabled = d.value !== 5;
        }
        return {
          ...d,
          disabled,
        };
      });
    });

    const editable = computed(() => {
      return ['UNANNOTATED', 'UNSAMPLED'].includes(statusCodeMap[state.model.status]);
    });
        
    const handleEditDataset = () => {
      refs.form.validate(valid => {
        if (!valid) {
          return false;
        }
        handleOk(state.model, props.row);
        return null;
      });
    };

    const handleCustomId = () => {
      Object.assign(state, {
        popoverVisible: false,
        systemLabelGroupId: null,
        model: {
          ...state.model,
          labelGroupId: state.customLabelGroupId,
          labelGroupName: customLabelGroups.find(d => d.labelGroupId === state.customLabelGroupId).name,
        },
      });
    };

    const handleSystemId = () => {
      Object.assign(state, {
        popoverVisible: false,
        customLabelGroupId: null,
        model: {
          ...state.model,
          labelGroupId: state.systemLabelGroupId,
          labelGroupName: systemLabelGroups.find(d => d.labelGroupId === state.systemLabelGroupId).name,
        },
      });
    };
        
    const handleRemoveLabelGroup = () => {
      Object.assign(state, {
        customLabelGroupId: null,
        systemLabelGroupId: null,
        model: {
          ...state.model,
          labelGroupId: null,
        },
      });
      popoverRef.value.doClose();
    };

    const optionEnabled = (labelGroupId, annotateType) => {
      if(annotateType === 1) {
        return labelGroupId === 1;
      } 
      if(annotateType === 5) {
        return false;
      }
      return true;
    };

    onMounted(() => {
      getLabelGroupList(1).then(res => res.forEach((item) => {
        systemLabelGroups.push({
          labelGroupId: item.id,
          name: item.name,
        });
      }));
      getLabelGroupList(0).then(res => res.forEach((item) => {
        customLabelGroups.push({
          labelGroupId: item.id,
          name: item.name,
        });
      }));
    });

    watch(() => props.row, (next) => {
      Object.assign(state, {
        model: { ...state.model, ...next },
      });
    });

    return {
      rules,
      state,
      deletable,
      editable,
      systemLabelEnabled,
      optionEnabled,
      systemLabelGroups,
      customLabelGroups,
      handleCustomId,
      handleSystemId,
      handleRemoveLabelGroup,
      handleEditDataset,
      dataTypeList,
      annotationList,
      popoverRef,
    };
  },
};
</script>
