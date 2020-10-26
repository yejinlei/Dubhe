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
  <!--训练管理页面-断点续训Dialog-->
  <BaseModal
    :visible.sync="visible"
    :title="title"
    width="600px"
    @open="onDialogOpen"
    @opened="onDialogOpened"
    @cancel="visible=false"
    @ok="ok"
  >
    <div v-if="fetchDone && treeList.length" class="fontBold">{{desc}}</div>
    <div class="tree-container">
      <el-tree
        v-if="fetchDone"
        ref="tree"
        highlight-current
        :data="treeList"
        :empty-text="emptyText"
        node-key="id"
        :props="defaultProps"
        :show-checkbox="type === 'modelDownload'"
        :default-expanded-keys="defaultExpandedKeys.slice(0, 1)"
        @node-click="handleNodeClick"
      ></el-tree>
    </div>
  </BaseModal>
</template>

<script>
import { Loading } from 'element-ui';
import BaseModal from '@/components/BaseModal';
import { getTreeListFromFilepath } from '@/utils';
import { resumeTrain } from '@/api/trainingJob/job';

export default {
  name: 'JobResumeDialog',
  components: { BaseModal },
  props: {
    type: {
      type: String,
      default: 'jobResume',
    },
  },
  data() {
    return {
      title: '断点续训',
      desc:'',
      visible: false,
      path: '',
      id: '',
      fileName:'',
      params: {},
      treeList: [],
      defaultExpandedKeys: [],
      emptyText: '暂无数据',
      selectNode: null,
      defaultProps: {
        children: 'children',
        label: 'name',
      },
      fetchDone: false,
    };
  },
  methods: {
    async show(item) {
      this.path = item.resumePath;
      this.id = item.id;
      this.fileName = item.fileName;
      this.params = item.params;
      this.title = this.getCentext(this.type, 0);
      this.desc = this.getCentext(this.type, 1);
      this.emptyText = this.getCentext(this.type, 2);
      this.visible = true;
    },
    getCentext(type='', num) {
      const ctxArr = [
        {
          'jobResume':'断点续训',
          'modelDownload':'模型下载',
          'modelSelect':'模型选择',
        },
        {
          'jobResume':'请选择从哪里开始继续训练',
          'modelDownload': '请选择需要下载的模型文件目录',
          'modelSelect': '请选择要保存的模型',
        },
        {
          'jobResume':'暂无数据，无法断点续训',
          'modelDownload': '暂无数据',
          'modelSelect': '暂无模型数据',
        },
      ];
      if(ctxArr[num][type]){
        return ctxArr[num][type];
      }
    },
    // handle
    async onDialogOpen() {
      this.fetchDone = false;
      this.treeList = [];
    },
    async onDialogOpened() {
      const loadingInstance = Loading.service({ target: '.el-dialog__body' });
      [this.treeList, this.defaultExpandedKeys] = await getTreeListFromFilepath(
        this.path,
      );
      setTimeout(() => {
        loadingInstance.close();
        this.fetchDone = true;
      }, 500);
    },
    handleNodeClick(data) {
      this.selectNode = data;
    },
    ok(){
      if (this.type==='jobResume') {
        this.chooseToResume();
      } else if (this.type==='modelDownload') {
        const nodes = this.$refs.tree.getCheckedNodes();
        const downList = [];
        const pidList = [];
        for(const node of nodes) {
          if (pidList.indexOf(node.pid) === -1) {
            downList.push(node.originPath);
          }
          pidList.push(node.id);
        }
        this.chooseToDownload(downList);
      } else {
        this.chooseModel();
      }
    },
    async chooseToResume() {
      if (!this.selectNode) {
        this.$message({
          message: '请选中断点续训的文件路径',
          type: 'error',
        });
        return;
      }
      const loadingInstance = Loading.service({target:'.el-dialog__body'});
      const params = {
        id: this.id,
        path: this.selectNode.originPath,
      };
      await resumeTrain(params).finally(() => {
        this.resumeLoading = false;
        this.visible = false;
        this.$emit('chooseDone', true);
        loadingInstance.close();
      });
    },
    chooseToDownload(list) {
      if (list.length === 0) {
        this.$message({
          message: '请选中下载模型的文件路径',
          type: 'error',
        });
        return;
      }
      this.visible = false; 
      const index = list[0].indexOf('/out/');
      if (index !== -1) {
        const beforePath = list[0].substring(0, index);
        let afterPathList = [];
        afterPathList = list.map(item => item.substring(index + 1, item.length));
        const params = {
          fileName: this.fileName,
          path: beforePath,
        };
        this.$emit('chooseDone', params, afterPathList);
      } else {
        this.$message.warning('根目录不存在!');
      }
    },
    chooseModel() {
      if (!this.selectNode) {
        this.$message({
          message: '请选择要保存的模型文件',
          type: 'error',
        });
        return;
      }
      this.visible = false;
      this.$emit('chooseModel', this.selectNode.originPath, this.params);
    },
  },
};
</script>
