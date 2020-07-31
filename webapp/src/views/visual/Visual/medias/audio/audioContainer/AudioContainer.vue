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
  <div class="audiocontainer">
    <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="8">
      <el-card class="box-card">
        <el-row>
          <el-col :span="6"><el-tag class="top">RUN</el-tag></el-col>
          <el-col :span="16" class="center"><span>{{ content.run }}</span></el-col>
          <el-col :span="2" class="center">
            <div class="leftItem">
              <el-tooltip class="item" effect="dark" content="勾选后，点击定制按钮会跳转到用户定制界面" placement="top">
                <el-checkbox v-if="parentComponent" v-model="checked" @change="ischeckedLocal" />
              </el-tooltip>
              <span v-if="!parentComponent" @click="ischecked"><i class="close-i el-icon-circle-close" /></span>
            </div>
          </el-col>
        </el-row>
        <el-divider />
        <el-row>
          <el-col :span="6"><el-tag class="top">TAG</el-tag></el-col>
          <el-col :span="18" class="center"><span>{{ Object.keys(content.value)[0] }}</span></el-col>
        </el-row>
        <el-divider />
        <el-row>
          <el-col :span="6"><el-tag class="bottom">STEP</el-tag></el-col>
          <el-col :span="18" class="center"><span>{{ audiocontent[scrollvalue].step }}</span></el-col>
        </el-row>
        <el-divider />
        <el-row>
          <el-col :span="6"><el-tag class="bottom">WALL_TIME</el-tag></el-col>
          <el-col :span="18" class="center"><span>{{ normalTime }}</span></el-col>
        </el-row>
        <el-divider />
        <el-row>
          <el-col :span="6"><el-tag class="bottom">LABEL</el-tag></el-col>
          <el-col :span="18" class="center"><span>{{ audiocontent[scrollvalue].label }}</span></el-col>
        </el-row>
      </el-card>

      <div class="audiocontent">
        <customAudio :URL="audiourl" />
        <el-slider
          v-model="scrollvalue"
          :max="audiocontent.length - 1"
          :disabled="audiocontent.length - 1===0"
          class="slider"
        />
      </div>
    </el-col>
  </div>
</template>

<script>
import { createNamespacedHelpers } from 'vuex';
import { getAudioRaw } from '@/api/visual';
import { unixTimestamp2Normal } from '@/utils';
import customAudio from './CustomAudio';

const {
  mapMutations: mapCustomMutations,
  mapGetters: mapCustomGetters,
} = createNamespacedHelpers('Visual/custom');
const { mapGetters: mapLayoutGetters } = createNamespacedHelpers('Visual/layout');
const {
  mapMutations: mapMediaMutations,
} = createNamespacedHelpers('Visual/media');
export default {
  components: {
    customAudio,
  },
  props: {
    content: Object,
    index: Number,
    parentComponent: Boolean,
  },
  data() {
    return {
      scrollvalue: 0,
      audiocontent: [],
      audiourl: '',
      size: 8,
      checked: false,
      normalTime: '',
    };
  },
  computed: {
    ...mapCustomGetters(['getAudio']),
    ...mapLayoutGetters(['getParams']),
  },
  watch: {
    async scrollvalue(val) {
      this.normalTime = unixTimestamp2Normal(this.audiocontent[this.scrollvalue].wall_time);
      const params = {
        step: this.audiocontent[val].step.toString(),
        run: this.content.run,
        tag: Object.keys(this.content.value)[0],
        trainJobName: this.getParams.trainJobName,
      };
      await getAudioRaw(params)
        .then(res => {
          this.audiourl = res;
        });
    },
  },
  mounted() {
    const paramStringIndex = `${this.content.run  }/${  Object.keys(this.content.value)[0]}`;
    for (let i = 0; i < this.getAudio.length; i+=1) {
      if (paramStringIndex === this.getAudio[i].stringIndex) {
        this.checked = true;
        break;
      }
    }
  },
  async created() {
    this.audiocontent = this.content.value[Object.keys(this.content.value)[0]];
    this.normalTime = unixTimestamp2Normal(this.audiocontent[this.scrollvalue].wall_time);
    const params = {
      step: this.audiocontent[0].step.toString(),
      run: this.content.run,
      tag: Object.keys(this.content.value)[0],
      trainJobName: this.getParams.trainJobName,
    };
    await getAudioRaw(params)
      .then(res => {
        this.audiourl = res;
      });
  },
  methods: {
    ...mapCustomMutations(['setAudioData']),
    ...mapMediaMutations(['setErrorMessage']),
    sizebig() {
      this.size = 24;
    },
    sizesmall() {
      this.size = 8;
    },
    ischecked() {
      const param = {};
      param.content = this.content;
      param.index = this.index;
      param.checked = false;
      param.copyToData = true;
      this.setAudioData(param);
    },
    ischeckedLocal() {
      const param = {};
      param.content = this.content;
      param.index = this.index;
      param.checked = this.checked;
      param.copyToData = false;
      this.setAudioData(param);
    },
  },
};
</script>

<style lang="less" scoped>
  .audiocontainer {
    width: 100%;
    height: 100%;
    background-color: rgb(255, 255, 255);
  }

  .audiocontent {
    width: 100%;
    height: 20%;
    margin-top: 20px;
  }

  audio {
    width: 100%;
  }

  .close-i {
    font-size: 19px;
  }

  .el-col {
    margin-bottom: 20px;
  }

  /deep/ .box-card {
    width: 100%;
    height: 100%;
    text-align: left;

    .el-divider--horizontal {
      margin: 6px 0;
    }

    .top {
      font-size: 9px;
      color: #66cbd7;
      background-color: #e5fcff;
      border-color: #e5fcff;
    }

    .bottom {
      font-size: 9px;
      color: #48d994;
      background-color: #def8fb;
      border-color: #def8fb;
    }

    .center {
      align-items: center;
      font-size: 10px;
      color: #333;
      vertical-align: middle;
    }

    span {
      height: 30px;
      font-weight: bold;
      line-height: 30px;
    }

    .el-row {
      margin-bottom: 0;
    }

    .el-col {
      margin-bottom: 0;
    }
  }

  .leftItem {
    margin-right: 1%;
    margin-left: auto;

    /deep/ .checked {
      width: 20px;
      height: 20px;
    }

    /deep/ .el-checkbox__inner {
      font-size: 20px;
    }

    /deep/ .el-checkbox__inner:hover {
      border-color: #8f8bd9;
    }

    /deep/ .el-checkbox {
      font-size: 20px;
    }

    /deep/ .el-checkbox__input.is-checked .el-checkbox__inner {
      background-color: #8f8bd9;
      border-color: #8f8bd9;
    }

    /deep/ .el-checkbox__input.is-checked .el-checkbox__inner::after {
      border-color: #fff;
    }

    /deep/ .el-checkbox__input.is-focus .el-checkbox__inner {
      border-color: gray;
    }

    /deep/ .el-checkbox__input span {
      height: 14px !important;
    }
  }

  .audiotext {
    margin-left: 2%;
    text-align: left;
  }

  .button {
    align-self: flex-start;
  }

  p {
    font-size: 10px;
  }

  /deep/ .el-slider__runway {
    width: 95%;
    margin: 16px auto;
  }

  /deep/ .el-slider__button {
    border-color: #625eb3;
  }

  /deep/ .el-slider__bar {
    background-color: #625eb3;
  }
</style>
