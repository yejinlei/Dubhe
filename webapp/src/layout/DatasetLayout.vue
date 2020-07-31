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
  <BaseLayout :showBack="true" :showSidebar="false">
    <div slot="left-options" style="margin-left: 10px;">
      <el-tooltip effect="dark" placement="bottom-start">
        <div slot="content">
          <div class="label-tooltip">
            <div class="f14 label-title">快捷键说明：</div>
            <div class="flex tips-wrapper f12">
              <div>上一张：Left</div>
              <div>下一张：Right</div>
              <div>确认提交：Enter</div>
              <div>手动标注: N</div>
              <div>删除标注：Backspace</div>
            </div>
          </div>
        </div>
        <i class="el-icon-question infoColor cp" />
      </el-tooltip>
    </div>
  </BaseLayout>
</template>

<script>
import BaseLayout from './BaseLayout';

export default {
  name: 'DatasetLayout',
  components: {
    BaseLayout,
  },
};
</script>

<style scoped lang='scss'>
@import "~@/assets/styles/variables.scss";

.label-title {
  margin-bottom: 8px;
}

.tips-wrapper {
  flex-wrap: wrap;
  max-width: 272px;
  line-height: 1.5;

  div {
    width: 50%;
  }
}
</style>
