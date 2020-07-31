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
  <div>
    <el-form-item label="运行参数模式">
      <el-radio-group v-model="paramsMode" @change="onParamsModeChange">
        <el-radio-button :label="1">key-value</el-radio-button>
        <el-radio-button :label="2">arguments</el-radio-button>
      </el-radio-group>
    </el-form-item>
    <el-form-item
      v-show="paramsMode === 1"
      label=""
      label-width="0px"
      :prop="prop"
      style="margin-bottom: 0;"
    >
      <el-form ref="runParamForm" :label-width="paramLabelWidth">
        <div v-for="(item, index) in runParamsList" :key="index">
          <el-form-item
            :ref="itemKeyId(index)"
            style="display: inline-block; margin-bottom: 18px;"
            :label="'运行参数' + (index+1)"
            :prop="itemKeyId(index)"
            :rules="{
              validator: (rule, value, callback) => {validateKey(callback, item, index)}, trigger: 'blur'
            }"
            :error="errMsg[index]"
          >
            <el-input v-model="item.key" :style="`width:${input1Width}px;`" clearable :disabled="disabled" @change="handleChange" />
          </el-form-item>
          <el-form-item
            :ref="itemValueId(index)"
            style="display: inline-block;"
            label="="
            label-width="30px"
            :prop="itemValueId(index)"
            :rules="{
              validator: (rule, value, callback) => {validateValue(callback, item, index)}, trigger: 'blur'
            }"
          >
            <el-input v-model="item.value" type="text" :style="`width:${input2Width}px;`" :disabled="disabled" @change="handleChange" />
          </el-form-item>
          <template v-if="!disabled">
            <el-button v-if="index==runParamsList.length-1" type="primary" size="mini" icon="el-icon-plus" circle @click="() => { addP(index) }" />
            <el-button v-if="runParamsList.length>1" type="danger" size="mini" icon="el-icon-minus" circle @click="() => { removeP(index) }" />
          </template>
        </div>
      </el-form>
    </el-form-item>
    <el-form-item v-show="paramsMode === 2" label="运行参数" :error="argErrorMsg">
      <el-input
        v-model="paramsArguments"
        type="textarea"
        autosize
        placeholder="例如：--param1=15 --param2=string"
        :rows="1"
      />
    </el-form-item>
  </div>
</template>

<script>
import { stringIsValidPythonVariable } from '@/utils';

export default {
  name: 'RunParamForm',
  props: {
    id: {
      type: [Number, String],
      default: null,
    },
    runParamObj: {
      type: Object,
      default: () => {},
    },
    prop: {
      type: String,
      default: null,
    },
    input1Width: {
      type: Number,
      default: 150,
    },
    input2Width: {
      type: Number,
      default: 150,
    },
    paramLabelWidth: {
      type: String,
      default: '100px',
    },
    disabled: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      runParamsList: [],
      errMsg: [],
      paramsMode: 1,
      paramsArguments: '',
      argErrorMsg: null,
      validateKey: (callback, item, index) => {
        // 先校验是不是都为空，若都为空则通过
        const isEmptyKey = this.isInputEmpty(item.key);
        const isEmptyValue = this.isInputEmpty(item.value);
        if (isEmptyKey && isEmptyValue) {
          // 可能之前value有校验错误信息
          this.$refs[this.itemValueId(index)][0].form.clearValidate(this.itemValueId(index));
          callback();
          return;
        }
        // 再校验自己是不是合法的变量名
        if (!stringIsValidPythonVariable(item.key)) {
          callback(new Error('参数key必须是合法变量名'));
          return;
        }
        // 然后和value联合校验
        if (isEmptyKey) {
          callback(new Error('请输入参数key'));
          return;
        } if (isEmptyValue) {
          this.$refs.runParamForm.validateField(this.itemValueId(index));
        } else {
          callback();
          
        }
      },
      validateValue: (callback, item, index) => {
        // 先校验是不是都为空，若都为空则通过
        const isEmptyKey = this.isInputEmpty(item.key);
        const isEmptyValue = this.isInputEmpty(item.value);
        if (isEmptyKey && isEmptyValue) {
          // 可能之前key有校验错误信息
          this.$refs[this.itemKeyId(index)][0].form.clearValidate(this.itemKeyId(index));
          callback();
          return;
        }
        // 输入框格式保证了其类似一定是字符串，只需不传空即可
        // 然后和key联合校验
        if (isEmptyValue) {
          callback();
          return;
        } if (isEmptyKey) {
          this.$refs.runParamForm.validateField(this.itemKeyId(index));
        } else {
          callback();
          
        }
      },
    };
  },
  watch: {
    id(newValue) {
      if (newValue === null || isNaN(newValue)) {
        /**
         * newValue为null时的一种情况是与el-form组合使用的
         * crud组件触发了cancelCU方法，此时不需更新
         */
        return;
      }
      this.syncListData();
    },
    runParamObj() {
      this.syncListData();
    },
  },
  async mounted() {
    this.syncListData();
  },
  methods: {
    isInputEmpty(value) {
      return value === '' || value === null;
    },
    itemKeyId(index) {
      return `runParamsList.${  index  }.key`;
    },
    itemValueId(index) {
      return `runParamsList.${  index  }.value`;
    },
    addP() {
      this.runParamsList.push({
        key: '',
        value: '',
      });
    },
    removeP(i) {
      this.runParamsList.splice(i, 1);
      this.updateRunParamObj();
    },
    syncListData() {
      const rpObj = { ...this.runParamObj};
      const list = [];
      for (const formKey in rpObj) {
        list.push({
          key: formKey,
          value: typeof (rpObj[formKey]) === 'object' ? JSON.stringify(rpObj[formKey]) : rpObj[formKey],
        });
      }
      this.runParamsList = list;
      if (this.runParamsList.length === 0) {
        this.addP();
      }
      if (this.paramsMode === 2) {
        this.convertPairsToArgs();
      }
    },
    handleChange() {
      this.updateRunParamObj();
    },
    updateRunParamObj() {
      const obj = {};
      this.runParamsList.forEach(d => {
        if (d.key === '') return;
        obj[d.key] = d.value;
      });
      this.$emit('updateRunParams', obj);
    },
    goValid() {
      // 单独校验
      let valid = true;
      this.errMsg = [];
      this.runParamsList.forEach((item, index) => {
        if (this.isInputEmpty(item.key)) {
          if (!this.isInputEmpty(item.value)) {
            valid = false;
          }
        } else if (!stringIsValidPythonVariable(item.key)) {
          valid = false;
          this.$nextTick(() => {
            this.errMsg[index] = '参数key必须是合法变量名';
          });
        }
      });
      return valid;
    },
    onParamsModeChange(value) {
      switch (value) {
      case 1:
        this.convertArgsToPairs();
        break;
      case 2:
        this.convertPairsToArgs();
        break;
        // no default
      }
    },
    convertArgsToPairs() {
      const paramsList = this.paramsArguments.split(' ');
      const pairList = [];
      const re = /^--(.+)=(.*)$/;
      paramsList.forEach(arg => {
        const group = re.exec(arg);
        if (group) {
          pairList.push({
            key: group[1],
            value: group[2],
          });
        } else if (arg) {
          this.$nextTick(() => {
            this.argErrorMsg = `参数'${arg}'不合法，请检查运行参数`;
          });
          this.paramsMode = 2;
          
        }
      });
      pairList.forEach(pair => {
        if (!stringIsValidPythonVariable(pair.key)) {
          this.$nextTick(() => {
            this.argErrorMsg = `参数名'${pair.key}'不是合法参数，请检查运行参数`;
          });
          this.paramsMode = 2;
          
        }
      });
      // 参数为空时增加一个空参数
      if (!pairList.length) {
        pairList.push({ key: '', value: '' });
      }
      this.runParamsList = pairList;
      this.updateRunParamObj();
      this.argErrorMsg = null;
    },
    convertPairsToArgs() {
      let args = '';
      this.runParamsList.forEach(pair => {
        // 跳过空参数
        if (!pair.key) return;
        args += args ? ' ' : '';
        args += `--${pair.key}=${pair.value}`;
      });
      this.paramsArguments = args;
    },
    reset() {
      this.errMsg = [];
      this.argErrorMsg = null;
      this.paramsMode = 1;
      this.paramsArguments = '';
      this.runParamsList = [{ key: '', value: '' }];
      this.$refs.runParamForm.clearValidate();
    },
  },
};
</script>
