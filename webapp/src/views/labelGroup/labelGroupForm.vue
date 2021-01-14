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
  <div v-loading="state.loading" class="app-container" style="width: 600px; margin-top: 28px;">
    <el-form ref="formRef" :model="state.createForm" :rules="rules" label-width="100px">
      <el-form-item label="名称" prop="name">
        <el-input 
          v-model="state.createForm.name" 
          placeholder="标签组名称不能超过50字" 
          maxlength="50" 
          show-word-limit
          :disabled="state.actionType === 'detail'" 
        />
      </el-form-item>
      <el-form-item label="类型" prop="labelGroupType" >
        <InfoSelect
          v-model="state.createForm.labelGroupType"
          placeholder="类型"
          :dataSource="labelGroupTypeList"
          :disabled="['detail','edit'].includes(state.actionType)"
          @change="handleLabelGroupTypeChange"
        />
      </el-form-item>
      <el-form-item label="描述" prop="remark">
        <el-input
          v-model="state.createForm.remark"
          type="textarea"
          placeholder="标签组描述长度不能超过100字"
          maxlength="100"
          rows="3"
          show-word-limit
          :disabled="state.actionType === 'detail'"
        />
      </el-form-item>   
      <el-form-item label="创建方式">
        <el-tabs :value="state.addWay" class='labels-edit-wrapper' type="border-card" :before-leave="beforeLeave" @tab-click="handleClick">
          <el-tab-pane label="自定义标签组" name="custom" class="dynamic-field">
            <Exception v-if="state.createForm.labels.length === 0" />
            <div v-else>
              <div v-if="state.groupType === 1">
                <el-tag v-for="label in state.originList" :key="label.id" class="mr-10">{{ label.name }}</el-tag>
              </div>
              <el-form
                v-else
                ref="customFormRef"
                :model="state.createForm" 
                label-width="100px"
              >
                <DynamicField
                  :list="state.createForm.labels"
                  :labelGroupType="state.createForm.labelGroupType"
                  :originList="state.originList"
                  :keys="state.keys"
                  :activeLabels="state.activeLabels"
                  :add="addRow"
                  :remove="removeLabel"
                  :handleChange="handleLabelChange"
                  :actionType="state.actionType"
                  :validateDuplicate="validateDuplicate"
                />
              </el-form>
            </div>
          </el-tab-pane>
          <el-tab-pane label="编辑标签组" name="edit" class='labelgroup-editor'>
            <prism-editor
              ref="editorRef"
              v-model="state.codeContent" 
              :readonly="state.actionType === 'detail'"
              class="min-height-100 max-height-400" 
              :highlight="highlighter"
            />
            <span class='icon-wrapper' @click="beautify">
              <IconFont type="beauty" class="format" />
            </span>
          </el-tab-pane>
          <el-tab-pane label="导入标签组" name="upload" :disabled="state.actionType !== 'create'">
            <div class="min-height-100 flex flex-center upload-tab">
              <UploadInline
                ref="uploadFormRef"
                action="fakeApi"
                accept=".json"
                listType="text"
                :limit="1"
                :acceptSize="0"
                :multiple="false"
                :showFileCount="false"
                :hash="false"
                @uploadError="uploadError"
              />
            </div>
          </el-tab-pane>
        </el-tabs>
        <div class="field-extra mt-10">
          <div v-if="state.addWay === 'custom'">
            <div>「自定义标签组」由用户自己创建，标签名长度不能超过 30</div>
          </div>
          <div v-else-if="state.addWay === 'edit'">
            <div>1.「编辑标签组」提供用户自由编写标签方式</div>
            <div>2. 请不要随意删除已有标签</div>
            <div>3. 请不要随意修改已有标签 id</div>
            <div>4. 请按照标准格式提供颜色色值</div>
          </div>
          <div v-else-if="state.addWay === 'upload'">
            <div>1. 请按照格式要求提交 json 格式标签文件</div>
          </div>
        </div>
      </el-form-item>
    </el-form>
    <div style="margin-left: 100px;">
      <el-button type="primary" @click="handleSubmit">{{ submitTxt }}</el-button>
      <!-- <el-button @click="goBack">{{state.cancelText}}</el-button> -->
    </div>
  </div>
</template>

<script>
import { reactive, ref, onMounted, computed } from '@vue/composition-api';
import { Message, MessageBox } from 'element-ui';
import { pick, uniqBy } from 'lodash';

import Beautify from 'js-beautify';
import { PrismEditor } from 'vue-prism-editor';
import 'vue-prism-editor/dist/prismeditor.min.css'; 
import { highlight, languages } from 'prismjs/components/prism-core';
import 'prismjs/components/prism-clike';
import 'prismjs/components/prism-javascript';

import Exception from '@/components/Exception';
import UploadInline from "@/components/UploadForm/inline";
import { remove, replace, duplicate } from '@/utils';
import { validateName, validateLabelsUtil } from '@/utils/validate';
import { getAutoLabels } from '@/api/preparation/datalabel';
import { add, edit, getLabelGroupDetail, importLabelGroup } from "@/api/preparation/labelGroup";
import InfoSelect from '@/components/InfoSelect';
import DynamicField from './dynamicField';
import { labelGroupTypeMap } from './util';

import 'prismjs/themes/prism-tomorrow.css';

const defaultColor = '#FFFFFF';

const initialLabels = [{"name":"","color": defaultColor}, {"name":"","color":"#000000"}];

export default {
  name: 'LabelGroupForm',
  components: {
    PrismEditor,
    DynamicField,
    UploadInline,
    Exception,
    InfoSelect,
  },
  setup(props, ctx) {
    const editorRef = ref(null);
    const formRef = ref(null);
    const uploadFormRef = ref(null);
    const customFormRef = ref(null);

    const { $route, $router } = ctx.root;
    const routeMap = {
      LabelGroupCreate: 'create',
      LabelGroupDetail: 'detail',
      LabelGroupEdit: 'edit',
    };

    const txtMap = {
      create: "确认创建",
      edit: "确认编辑",
      detail: "返回",
    };

    const operateTypeMap = {
      1: 'custom',
      2: 'edit',
      3: 'upload',
    };

    // 表单规则
    const rules = {
      name: [
        { required: true, message: '请输入标签组名称', trigger: ['change', 'blur'] },
        { validator: validateName, trigger: ['change', 'blur'] },
      ],
      labelGroupType: [
        {required: true, message: '请选择标签组类型', trigger: ['change', 'blur'] },
      ],
    };

    const buildModel = (record, options) => {
      return { ...record, ...options};
    };

    // 生成 keys
    const setKeys = labels => labels.map((label, index) => index);

    // 页面类型
    const actionType = routeMap[$route.name] || 'create';

    const state = reactive({
      id: actionType !== 'create' ? $route.query.id : null,
      actionType,
      groupType: null, // 查询标签组详情类型
      model: buildModel(props.row),
      systemLabels: [], // 系统自动标注标签列表
      originList: [], // 记录原始返回列表
      activeLabels: [], // 当前可用标签列表
      fileCount: undefined,
      // counter: 动态表单项数量，keys: 每次生成唯一的表单项
      counter: initialLabels.length - 1,
      keys: setKeys(initialLabels),
      createForm: {
        labels: initialLabels,
        name: '',
        labelGroupType: undefined,
        remark: "",
        type: 0,
      },
      codeContent: JSON.stringify(initialLabels),
      customForm: {
        labels: [{
          name: '',
          color: defaultColor,
        }],
      },
      addWay: "custom", // 默认创建类型为自定义
      cancelText: "取消",
      errmsg: '',
      loading: false, // 加载详情
    });

    const submitTxt = txtMap[state.actionType];

    // 获取 key 值索引
    const getIndex = (index) => state.keys.findIndex(key => key === index);

    const setCode = (code) => {
      Object.assign(state, {
        codeContent: code,
      });
    };

    const beautify = () => {
      // 编辑器内容
      const code = editorRef.value.value;
      const formated = Beautify(code);
      setCode(formated);
    };

    const uploadError = () => {

    };

    const goBack = () => {
      $router.push({path: "/data/labelgroup"});
    };

    // 更新
    const updateCreateForm = (next) => {
      Object.assign(state, {
        createForm: {
          ...state.createForm,
          ...next,
        },
      });
    };

    const labelGroupTypeList = computed(() => {
      return Object.keys(labelGroupTypeMap).map(d => ({
        label: labelGroupTypeMap[d],
        value: Number(d),
      }));
    });

    const handleLabelGroupTypeChange = () => {
      Object.assign(state, {
        createForm: {
          ...state.createForm,
          labels: initialLabels,
        },
      });
      getAutoLabels(state.createForm.labelGroupType).then(res => {
        Object.assign(state, {
          activeLabels: res,
          systemLabels: res,
        });
      });
    };

    const handleLabelGroupRequest = (params) => {
      const nextParams = {
        ...params,
        labels: JSON.stringify(params.labels),
      };

      const requestResource = params.id ? edit : add;
      const message =  params.id ? '标签组编辑成功' : '标签组创建成功';

      requestResource(nextParams).then(() => {
        Message.success({
          message,
          duration: 1500,
          onClose: goBack,
        });
      });
    };

    const handleSubmit = () => {
      if(actionType === 'detail') {
        goBack();
        return;
      }

      formRef.value.validate(validWrapper => {
        if (validWrapper) {
          switch(state.addWay) {
            // 自定标签组
            case 'custom':
              customFormRef.value.validate(isValid => {
                if (isValid) {
                  const params = {
                    ...state.createForm,
                    operateType: 1,
                  };
                  handleLabelGroupRequest(params);
                }
              });
              break;
            // 编辑标签组
            case 'edit':
              try {
                let errMsg = '';
                const code = JSON.parse(editorRef.value.value);
                if(Array.isArray(code) && code.length) {
                  for(const d of code) {
                    if(validateLabelsUtil(d) !== '') {
                      errMsg = validateLabelsUtil(d);
                      break;
                    }
                  }
                }
                if(errMsg) {
                  Message.error(errMsg);
                  return;
                }
                const editParams = {
                  ...state.createForm,
                  labels: code,
                  operateType: 2,
                };
                handleLabelGroupRequest(editParams);
              } catch(err) {
                console.error(err);
                throw err;
              }
              break;
            case 'upload': {
              const { uploadFiles } = uploadFormRef.value.formRef?.$refs.uploader || {};
              const { name, remark, labelGroupType } = state.createForm;
              
              const formData = new FormData();
              formData.append('name', name);
              formData.append('remark', remark);
              formData.append('file', uploadFiles[0].raw);
              formData.append('operateType', 3);
              formData.append('labelGroupType', labelGroupType);

              importLabelGroup(formData).then(() => {
                Message.success({
                  message: '标签组导入成功',
                  duration: 1500,
                  onClose: goBack,
                });
              });
              break;
            }
            default:
              break;
          }
        }
      });
    };

    const beforeLeave = (activeName, oldActiveName) => {
      if(activeName === oldActiveName) return false;
      if(oldActiveName === 'upload') {
        const { uploadFiles } = uploadFormRef.value.formRef?.$refs.uploader || {};
        if(uploadFiles.length) {
          return MessageBox.confirm('标注文件已提交，确认切换？')
            .catch(() => {
              state.addWay = 'upload';
              return Promise.reject();
            });
        }
        return true;
      }
      return true;
    };

    // 
    const handleClick = (tab) => {
      if(state.addWay === tab.name) return;
      // 切换到编辑模式
      if (tab.name === 'edit') {
        // 从自定义编辑切换过去
        if(state.addWay === 'custom') {
          state.codeContent = JSON.stringify(state.createForm.labels);
        }
      } else if (tab.name === 'custom'){
        if(state.addWay === 'edit') {
          try {
            const nextLabels = JSON.parse(editorRef.value.value);
            Object.assign(state, {
              createForm: {
                ...state.createForm,
                labels: nextLabels,
              },
              keys: setKeys(nextLabels),
              counter: Math.max(state.counter, nextLabels.length - 1),
            });
          } catch(err) {
            Message.error('编辑格式不合法');
            return;
          }
        }
      }
      state.addWay = tab.name;
    };

    const highlighter = (code) => {
      return highlight(code, languages.js);
    };

    const addLabel = (row) => {
      state.createForm.labels.push(row);
      const nextKeys = state.keys.concat(state.counter + 1);
      Object.assign(state, {
        keys: nextKeys,
        counter: state.counter + 1,
      });
    };

    // 添加一行标签
    const addRow = () => {
      addLabel({
        name: '',
        color: defaultColor,
      });
    };

    // 用户自定义创建标签
    const createCustomLabel = (name, index) => {
      const updateLabel = {name, color: defaultColor};
      updateCreateForm({
        labels: replace(state.createForm.labels, index, updateLabel),
      });
    };

    const validateDuplicate = (rule, value, callback) => {
      const isDuplicate = duplicate(state.createForm.labels, d => {
        if(!value.id) return false;
        return d.id === value.id;
      });
      if (isDuplicate) {
        callback(new Error('标签不能重复'));
        return;
      }
      callback();
    };

    const handleLabelChange = (key, value) => {
      const index = getIndex(key);
      
      // 每次触发错误表单项验证
      const errorFields = customFormRef.value.fields.filter(d => d.validateState === 'error').map(d => d.prop);
      customFormRef.value.validateField(errorFields);
      // 判断是新建还是选择标签
      const editLabel = state.systemLabels.find(d => d.id === value);
      // 选择已有标签
      if(editLabel) {
        const updateLabel = pick(editLabel, ['name', 'id', 'color']);
        Object.assign(state, {
          createForm: {
            ...state.createForm,
            labels: replace(state.createForm.labels, index, updateLabel),
          },
        });
      } else {
        // 创建用户自定义标签
        createCustomLabel(value, index);
      }
    };

    // 移除标签
    const removeLabel = (k) => {
      // 至少保留一条记录
      if (state.keys.length === 1) return;
      const index = getIndex(k);

      Object.assign(state, {
        keys: state.keys.filter(key => key !== k),
        createForm: {
          ...state.createForm,
          labels: remove(state.createForm.labels, index),
        },
      });
    };

    const setLoading = (loading) => {
      Object.assign(state, {
          loading,
        });
    };

    const labelGroupType = computed(() => labelGroupTypeMap[state.groupType]) || undefined;

    onMounted(() => {
      // 异常判断
      if(actionType !== 'create') {
        if(!state.id) {
          $router.push({ path: '/data/labelgroup' });
          throw new Error('当前标签组 id 不存在');
        }
        setLoading(true);
        // 查询数据集详情
        getLabelGroupDetail(state.id).then(async (res) => {
          // 当编辑模式，且数据为空时需要提供默认数据
          const labels = res.labels.length === 0 && actionType === 'edit' ? initialLabels : res.labels;
          const restProps = state.actionType === 'detail' ? {
            groupType: res.type || 0,
          } : {};
          const autoLabels = await getAutoLabels(res.labelGroupType);
          Object.assign(state, {
            activeLabels: autoLabels,
            systemLabels: autoLabels,
          });
          Object.assign(state, {
            createForm: {
              ...state.createForm,
              ...res,
              labels,
            },
            addWay: operateTypeMap[res.operateType] || 'custom',
            activeLabels: uniqBy(state.activeLabels.concat(res.labels), 'id'),
            originList: res.labels.slice(),
            keys: setKeys(labels),
            counter: Math.max(state.counter, labels.length - 1),
            codeContent: JSON.stringify(res.labels),
            ...restProps,
          });
        }).finally(() => {
          setLoading(false);
        });
      }
    });

    return {
      rules,
      state,
      submitTxt,
      beautify,
      editorRef,
      formRef,
      customFormRef,
      validateDuplicate,
      goBack,
      handleClick,
      handleSubmit,
      highlighter,
      removeLabel,
      addRow,
      handleLabelChange,
      uploadError,
      uploadFormRef,
      beforeLeave,
      labelGroupType,
      labelGroupTypeList,
      handleLabelGroupTypeChange,
    };
  },
};
</script>

<style lang="scss">
  @import '@/assets/styles/variables.scss';

  .min-height-100 {
    min-height: 100px;
  }

  .height-400 {
    height: 400px;
  }

  .max-height-400 {
    max-height: 400px;
  }

  .field-extra {
    font-size: 14px;
    line-height: 1.5;
    color: $infoColor;
  }

  .labelgroup-editor {
    position: relative;
    padding: 5px;
    font-family: Fira code, Fira Mono, Consolas, Menlo, Courier, monospace;
    font-size: 18px;
    line-height: 1.5;
    color: black;
    background: white;
  }

  .prism-editor__textarea:focus {
    outline: none;
  }

  .labels-edit-wrapper {
    .icon-wrapper {
      position: absolute;
      top: -10px;
      right: 10px;
      width: 32px;
      height: 32px;
      line-height: 32px;
      color: $commonTextColor;
      text-align: center;
      cursor: pointer;
      border: 1px solid $borderColor;
      border-radius: 50%;
      transition: 200ms ease;

      &:hover {
        color: #333;
      }
    }

    .format {
      font-size: 20px;
    }

    .disabled {
      color: $infoColor;
      pointer-events: none;
      cursor: not-allowed;
    }

    .el-tabs__content {
      padding-right: 0;
    }

    .dynamic-field {
      min-height: 100px;
      max-height: 400px;
      overflow: auto;

      .exception {
        min-height: 100px;
      }

      .el-form-item {
        margin-bottom: 20px;
      }
    }

    .upload-tab {
      max-width: 80%;
    }
  }
</style>