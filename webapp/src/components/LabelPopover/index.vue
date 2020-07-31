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
  <el-popover
    v-model="addLabelTagVisible"
    placement="bottom"
    trigger="click"
    :visible-arrow="false"
    width="370"
    height="370"
    @hide="handleHide"
  >
    <div slot="default" class="add-label-tag">
      <el-tabs v-model="activeLabel" tab-position="left" @tab-click="handleTabClick">
        <el-tab-pane label="自动标注标签" name="systemLabel">
          <el-table :data="systemLabel" :show-header="false" height="290" row-class-name="tag-table-row">
            <el-table-column prop="chosen" class-name="no-ellipsis" width="30">
              <template slot-scope="scope">
                <el-checkbox v-model="scope.row.chosen" />
              </template>
            </el-table-column>
            <el-table-column prop="name" width="80" class-name="pl-0" />
            <el-table-column prop="color" align="right">
              <template slot-scope="scope">
                <el-color-picker v-model="scope.row.color" />
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="自定义标签" name="customLabel">
          <div style="height: 290px;">
            <el-input v-model="newCustomLabel" placeholder="字符长度不能超过30" maxlength="30" @keyup.enter.native="addCustomLabel">
              <el-button slot="append" style="padding: 12px;" type="text" class="el-icon-check" @click="addCustomLabel" />
            </el-input>
            <el-table :data="customLabel" :show-header="false" height="260" row-class-name="tag-table-row">
              <div slot="empty">暂无标签</div>
              <el-table-column prop="chosen" class-name="no-ellipsis" width="30">
                <template slot-scope="scope">
                  <el-checkbox v-model="scope.row.chosen" />
                </template>
              </el-table-column>
              <el-table-column prop="name" width="120" class-name="pl-0 ellipsis">
                <template slot-scope="scope">
                  <span :title="scope.row.name">{{ scope.row.name }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="color" align="right" width="60">
                <template slot-scope="scope">
                  <el-color-picker v-model="scope.row.color" />
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        <el-tab-pane :disabled="![1, 2].includes(annotateType)" label="预置标签" name="presetLabel">
          <div style="height: 290px; padding: 40px 0 0 16px;">
            <el-radio-group v-model="chosenRadioId" class="block-label-group">
              <el-radio v-for="(value, key) in presetLabelList" :key="key" :label="key" :disabled="key==2 && annotateType==1">
                {{ value }}
              </el-radio>
            </el-radio-group>
          </div>
        </el-tab-pane>
      </el-tabs>
      <div class="add-label-foot" style=" padding-top: 10px; margin-bottom: 0; text-align: center;">
        <el-button type="text" @click="addLabelTagVisible = false">取消</el-button>
        <el-button type="primary" @click="addLabelTag">确定</el-button>
      </div>
    </div>
    <el-button slot="reference" type="text">&nbsp;+ {{ labelButtonText }}</el-button>
  </el-popover>
</template>

<script>
import { find } from 'lodash';

export default {
  name: 'LabelPopover',
  props: {
    customLabel: {
      type: Array,
      default: () => [],
    },
    systemLabel: {
      type: Array,
      default: () => [],
    },
    presetLabelList: {
      type: Object,
      default: () => {},
    },
    chosenPresetLabelId: {
      type: String,
    },
    annotateType: {
      type: Number,
      default: 2,
    },
    setPresetLabel: {
      type: Function,
    },
    setNoPresetLabel: {
      type: Function,
    },
  },
  data() {
    return {
      addLabelTagVisible: false,
      activeLabel: 'systemLabel', // 默认为自动标注标签
      newCustomLabel: '',
      chosenRadioId: undefined,
      defaultLabelColor: '#6973FF',
    };
  },
  computed: {
    labelButtonText() {
      return this.chosenPresetLabelId ? '修改标签' : '添加标签';
    },
  },
  watch: {
    // 因为外部修改标注类型，本组件key不变，需监听外部变化来改变popover的标签页
    // eslint-disable-next-line func-names
    'chosenPresetLabelId': function(next) {
      if (next) {
        this.activeLabel = 'presetLabel';
        this.chosenRadioId = this.chosenPresetLabelId;
      } else {
        this.activeLabel = 'systemLabel';
      }
    },
    // eslint-disable-next-line func-names
    'annotateType': function(next) {
      if ([1, 5].includes(next)) {
        this.activeLabel = 'systemLabel';
      }
    },
  },
  created() {
    // 修改预置标签时弹出popover为预置标签tab
    if (this.chosenPresetLabelId) {
      this.activeLabel = 'presetLabel';
      this.chosenRadioId = this.chosenPresetLabelId;
    }
  },
  methods: {
    handleTabClick() {
      // 切换tab清除了选中的预置标签
      this.chosenRadioId = undefined;
    },
    findItem(list, name) {
      return find(list, d => d.name === name);
    },
    addCustomLabel() {
      if (this.newCustomLabel.trim() !== '' && !this.findItem(this.customLabel, this.newCustomLabel)) {
        this.customLabel.push({
          name: this.newCustomLabel,
          color: this.defaultLabelColor,
          chosen: true,
        });
        this.newCustomLabel = '';
      }
    },
    addLabelTag() {
      if (this.activeLabel === 'presetLabel') {
        if (!this.chosenRadioId === undefined) {
          this.setNoPresetLabel(); // 若未选择预置标签，则不添加标签
        } else {
          this.setPresetLabel(this.chosenRadioId);
        }
      } else {
        this.setNoPresetLabel();
      }
      this.addLabelTagVisible = false;
    },
    handleHide() {
      this.$emit('hide');
    },
  },
};
</script>

<style lang='scss'>
.tag-table-row {
  td {
    padding: 6px 0 0 0;
  }

  .cell {
    white-space: nowrap;
  }
}

.el-table {
  .no-ellipsis .cell {
    padding-right: 0;
    text-overflow: unset;
  }

  .pl-0 .cell {
    padding-left: 0;
  }
}
</style>
