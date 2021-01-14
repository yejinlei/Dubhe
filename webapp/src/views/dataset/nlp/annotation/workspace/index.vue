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
    <el-tabs :value="activeTab" class="eltabs-inlineblock" @tab-click="handlePanelClick">
      <el-tab-pane :label="countInfoTxt.unfinished" name="unfinished" />
      <el-tab-pane :label="countInfoTxt.finished" name="finished" />
    </el-tabs>
    <el-card class="box-card" style="margin-top: 20px;" shadow="never">
      <div slot="header" class="clearfix" style="line-height: 24px;">
        <span class="vm">已选择标签：</span>
        <span
          v-if="!labelSelected"
          class="g9 vm"
        >请在右侧选择标签</span>
        <el-tag
          v-else
          :color="labelSelected.color"
          closable
          disable-transitions
          :style="getStyle(labelSelected)"
          @close="closeLabel(labelSelected)"
        >{{ labelSelected.name }}</el-tag>

        <div style="float: right; padding: 3px 0;">
          <el-button v-if="showSave" type="text" @click="save">保存标注</el-button>
          <el-button :disabled="!showPrev" type="text" @click="toPrev">上一篇</el-button>
          <el-button :disabled="!showNext" type="text" @click="toNext">下一篇</el-button>
          <el-popconfirm
            title="确认删除该文本？"
            @onConfirm="deleteFile(file)"
          >
            <el-button slot="reference" :disabled="!showDelete" type="text">删除</el-button>
          </el-popconfirm>
        </div>
      </div>
      <div class="text">
        <Exception v-if="!!showException" />
        <div v-else-if="loading" class="flex flex-center g6" style="min-height: 80px;" >加载中...</div>
        <TextEditor v-else :txt="state.txt" />
      </div>
    </el-card>
  </div>
</template>
<script>
import { Message } from 'element-ui';
import { reactive, watch, computed } from '@vue/composition-api';
import TextEditor from '@/components/textEditor';
import Exception from '@/components/Exception';
import { colorByLuminance } from '@/utils';

export default {
  name: 'TextAnnotationWorkSpace',
  components: {
    TextEditor,
    Exception,
  },
  props: {
    activeTab: String,
    txt: String,
    labelSelected: {
      type: Object,
      default: () => ({}),
    },
    countInfo: {
      type: Object,
      default: () => ({}),
    },
    pageInfo: {
      type: Object,
      default: () => ({}),
    },
    file: {
      type: Object,
      default: () => ({}),
    },
    loading: Boolean,
    deleteFile: Function,
    closeLabel: Function,
    toNext: Function,
    toPrev: Function,
    saveAnnotation: Function,
    changeActiveTab: Function,
  },
  setup(props) {
    const state = reactive({
      activeTab: props.activeTab || 'unfinished',
      txt: props.txt || '',
    });

    const countInfoTxt = computed(() => ({
      unfinished: `无标注信息（${props.countInfo.unfinished}）`,
      finished: `有标注信息（${props.countInfo.finished}）`,
    }));

    const getStyle = (item) => {
      const color = colorByLuminance(item.color);
      return {
        color,
        border: 'none',
      };
    };

    // 上一页，下一页
    const showPrev = computed(() => props.pageInfo.current > 1);
    const showNext = computed(() => props.pageInfo.current < props.pageInfo.total);
    const showDelete = computed(() => props.pageInfo.total > 0);
    const showException = computed(() => 
      props.loading === false && state.txt === '');
    // 下一页不展示，并且有文件存在
    const showSave = computed(() => !(props.pageInfo.current < props.pageInfo.total) && props.pageInfo.total > 0);

    // 保存标注
    const save = () => {
      props.saveAnnotation().then(() => Message.success('保存成功'));
    };

    const handlePanelClick = (tab) => {
      props.changeActiveTab(tab);
    };

    watch(() => props.txt, (next) => {
      state.txt = next;
    });

    watch(() => props.activeTab, (next) => {
      state.activeTab = next;
    });
  
    return {
      state,
      showException,
      showDelete,
      save,
      getStyle,
      showPrev,
      showNext,
      showSave,
      countInfoTxt,
      handlePanelClick,
    };
  },
};
</script>