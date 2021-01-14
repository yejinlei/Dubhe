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
  <div class="flex flex-center flex-col entry-wrapper">
    <div class="flex box-wrapper">
      <div :class="['radio-label', state.entrance === 0 ? 'border-chosen': 'border']" @click="changeRadio(0)">
        <img src="@/assets/images/dataset/normalDataset.png" width="50%"/>
        <div class="mb-20 mt-20 bold">
          视觉/文本
        </div>
        <div class="tl">
          针对一般计算机视觉、文本的数据标注，目前支持图像分类、目标检测、目标跟踪和文本分类场景
        </div>
      </div>
      <div :class="['radio-label', state.entrance === 1 ? 'border-chosen': 'border']" @click="changeRadio(1)">
        <img src="@/assets/images/dataset/medicalDataset.png" width="50%"/>
        <div class="mb-20 mt-20 bold">
          医学影像
        </div>
        <div class="tl">
          针对医学影像 dcm 格式文件的数据标注，目前支持器官分割和病灶识别场景
        </div>
      </div>
    </div>
    <div class="tc">
      <el-button type="primary" @click="handleNext">
        下一步
      </el-button>
    </div>
  </div>
</template>
<script>
import { reactive } from '@vue/composition-api';
import { cacheDatasetType } from './util';

export default {
  name: "Entrance",
  setup(props, ctx) {
    const { $router } = ctx.root;

    const redirect = (val) => {
      if(val === 0) {
        $router.push({ path: '/data/datasets/list'});
      } else if(val === 1){
        $router.push({ path: '/data/datasets/medical'});
      };
    };

    const state = reactive({
      entrance: 0,
    });

    const changeRadio = (val) => {
      state.entrance = val;
    };

    const handleNext = () => {
      // 缓存用户选择类型
      cacheDatasetType(state.entrance);
      redirect(state.entrance);
    };

    return {
      state,
      changeRadio,
      handleNext,
    };
  },
};
</script>

<style lang="scss" scoped>
  @import '@/assets/styles/variables.scss';

  .entry-wrapper {
    height: calc(100vh - 50px - 32px);
  }

  .box-wrapper {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 1000px;
    padding-right: 125px;
    padding-left: 125px;
    margin: 0 auto 64px;
  }

  .radio-label {
    width: 250px;
    min-height: 320px;
    padding: 30px 16px;
    line-height: 24px;
    text-align: center;
    cursor: pointer;
    border-radius: 12px;
  }

  .border {
    border: 2px solid $borderColor;
  }

  .border-chosen {
    background-color: $subMenuBg;
    border: 2px solid $primaryColor;
  }
</style>
