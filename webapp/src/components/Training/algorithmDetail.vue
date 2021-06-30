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
  <div class="ts-drawer">
    <!--基本信息-->
    <div class="title">基本信息</div>
    <el-row class="row">
      <el-col :xl="5" :lg="8" :span="24">
        <div class="label">算法名称</div>
      </el-col>
      <el-col :xl="19" :lg="16" :span="24">
        <div class="text">{{ item.algorithmName }}</div>
        <edit
          v-if="displayType === 1"
          :row="item"
          rules="required|validNameWithHyphen"
          valueBy="algorithmName"
          title="修改算法名称"
          @handleOk="editName"
        />
      </el-col>
    </el-row>
    <el-row class="row">
      <el-col :xl="5" :lg="8" :span="24">
        <div class="label">描述信息</div>
      </el-col>
      <el-col :xl="19" :lg="16" :span="24">
        <div class="text">{{ item.description }}</div>
        <edit
          v-if="displayType === 1"
          :row="item"
          rules=""
          valueBy="description"
          title="修改算法描述"
          :width="400"
          inputType="textarea"
          @handleOk="editDescription"
        />
      </el-col>
    </el-row>
    <el-row class="row">
      <el-col :xl="5" :lg="8" :span="24">
        <div class="label">模型类别</div>
      </el-col>
      <el-col :xl="19" :lg="16" :span="24">
        <div class="text">{{ item.algorithmUsage }}</div>
        <el-popover
          v-if="displayType === 1"
          v-model="usageEditVisible"
          placement="bottom"
          title="修改模型类别"
          @show="onUsageEditShow"
        >
          <el-select
            v-model="localUsage"
            placeholder="请选择或输入模型类别"
            filterable
            clearable
            allow-create
            @change="onAlgorithmUsageChange"
          >
            <el-option
              v-for="usage in algorithmUsageList"
              :key="usage.id"
              :label="usage.auxInfo"
              :value="usage.auxInfo"
            >
              <span style="float: left;">{{ usage.auxInfo }}</span>
              <el-button
                v-if="!usage.isDefault"
                class="select-del-btn"
                type="text"
                @click.stop="delAlgorithmUsage(usage)"
                ><i class="el-icon-close"
              /></el-button>
            </el-option>
          </el-select>
          <div class="tc" style="margin-top: 8px;">
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" @click="handleOk">确定</el-button>
          </div>
          <i slot="reference" class="el-icon-edit primary cp dib" />
        </el-popover>
      </el-col>
    </el-row>
    <el-row class="row">
      <el-col :xl="5" :lg="8" :span="24">
        <div class="label">文件输出</div>
      </el-col>
      <el-col :xl="19" :lg="16" :span="24">
        <div class="text">
          <div v-if="displayType === 1">
            <el-switch
              v-model="item.isTrainOut"
              @change="(value) => editAlgorithm('isTrainOut', value)"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="请确保代码中包含“train_out”参数用于接收训练的文件输出路径"
              placement="top"
            >
              <i class="el-icon-warning-outline primary f18 v-bottom" />
            </el-tooltip>
          </div>
          <span v-else>{{ item.isTrainOut ? '是' : '否' }}</span>
        </div>
      </el-col>
    </el-row>
    <el-row class="row">
      <el-col :xl="5" :lg="8" :span="24">
        <div class="label">可视化日志</div>
      </el-col>
      <el-col :xl="19" :lg="16" :span="24">
        <div class="text">
          <div v-if="displayType === 1">
            <el-switch
              v-model="item.isVisualizedLog"
              @change="(value) => editAlgorithm('isVisualizedLog', value)"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="请确保代码中包含“train_visualized_log”参数用于接收训练的可视化日志路径，仅支持在训练时使用 oneflow 镜像"
              placement="top"
            >
              <i class="el-icon-warning-outline primary f18 v-bottom" />
            </el-tooltip>
          </div>
          <span v-else>{{ item.isVisualizedLog ? '是' : '否' }}</span>
        </div>
      </el-col>
    </el-row>
    <el-row v-if="displayType === 2" class="row">
      <el-col :xl="5" :lg="8" :span="24">
        <div class="label">镜像名称</div>
      </el-col>
      <el-col :xl="19" :lg="16" :span="24">
        <div class="text">{{ item.imageName }}</div>
      </el-col>
    </el-row>
    <el-row v-if="displayType === 2" class="row">
      <el-col :xl="5" :lg="8" :span="24">
        <div class="label">镜像版本</div>
      </el-col>
      <el-col :xl="19" :lg="16" :span="24">
        <div class="text">{{ item.imageTag }}</div>
      </el-col>
    </el-row>
    <!--参数信息-->
    <div v-if="displayType === 2">
      <div class="title">参数信息</div>
      <el-row class="row">
        <el-col :xl="5" :lg="8" :span="24">
          <div class="label">运行命令</div>
        </el-col>
        <el-col :xl="19" :lg="16" :span="24">
          <div class="text">{{ item.runCommand }}</div>
        </el-col>
      </el-row>
      <el-row class="row">
        <el-col :xl="5" :lg="8" :span="24">
          <div class="label">运行参数</div>
        </el-col>
        <el-col :xl="19" :lg="16" :span="24">
          <div class="text">
            <div v-for="(p, index) in runParamsList" :key="p.key">
              {{ p.key }} = {{ p.value }}{{ index === runParamsList.length - 1 ? '' : ', ' }}
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { convertMapToList } from '@/utils';
import { edit as editAlgorithm } from '@/api/algorithm/algorithm';
import {
  list as getUsages,
  add as addUsage,
  del as deleteUsage,
} from '@/api/algorithm/algorithmUsage';
import Edit from '@/components/InlineTableEdit';

export default {
  name: 'AlgorithmDetail',
  components: { Edit },
  props: {
    item: {
      type: Object,
      default: null,
    },
    type: {
      type: String,
      default: 'algorithm',
    },
  },
  data() {
    return {
      algorithmUsageList: [],
      localUsage: null,
      usageEditVisible: false,
    };
  },
  computed: {
    displayType() {
      let displayType;
      // 1 为我的算法，2 为预置算法，3 为其他页面进入
      if (this.type === 'algorithm' && this.item.algorithmSource === 1) {
        displayType = 1;
      } else if (this.type === 'algorithm' && this.item.algorithmSource === 2) {
        displayType = 2;
      } else {
        displayType = 3;
      }
      return displayType;
    },
    runParamsList() {
      return convertMapToList(this.item.runParams);
    },
  },
  mounted() {
    if (this.displayType === 1) {
      this.getAlgorithmUsages();
      this.localUsage = this.item.algorithmUsage;
    }
  },
  methods: {
    async editName(algorithmName, algorithm) {
      await editAlgorithm({
        id: algorithm.id,
        algorithmName,
      });
      this.item.algorithmName = algorithmName;
    },
    async editDescription(description, algorithm) {
      await editAlgorithm({
        id: algorithm.id,
        description,
      });
      this.item.description = description;
    },
    editAlgorithm(type, value) {
      const param = { id: this.item.id };
      param[type] = value;
      return editAlgorithm(param);
    },
    async getAlgorithmUsages() {
      const params = {
        isContainDefault: true,
        current: 1,
        size: 1000,
      };
      const data = await getUsages(params);
      this.algorithmUsageList = data.result;
    },
    async createAlgorithmUsage(auxInfo) {
      await addUsage({ auxInfo });
      this.getAlgorithmUsages();
    },
    async delAlgorithmUsage(usage) {
      await deleteUsage({ ids: [usage.id] });
      if (this.localUsage === usage.auxInfo) {
        this.localUsage = null;
      }
      this.getAlgorithmUsages();
    },
    onAlgorithmUsageChange(value) {
      const usage = this.algorithmUsageList.find((usage) => usage.auxInfo === value);
      if (value && !usage) {
        this.createAlgorithmUsage(value);
      }
    },
    onUsageEditShow() {
      this.localUsage = this.item.algorithmUsage;
    },
    handleCancel() {
      this.usageEditVisible = false;
    },
    async handleOk() {
      await this.editAlgorithm('algorithmUsage', this.localUsage);
      this.item.algorithmUsage = this.localUsage;
      this.usageEditVisible = false;
    },
  },
};
</script>
