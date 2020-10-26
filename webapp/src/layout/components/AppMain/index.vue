/*
* Copyright 2019-2020 Zheng Jie
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

<template>
  <section class="app-main">
    <router-view :key="$route.path" />
    <div v-if="$store.state.settings.showFooter" id="el-main-footer">
      <span> {{ $store.state.settings.footerTxt }} </span>
      <template v-if="$store.state.settings.caseNumber">
        <span>⋅</span>
        <a href="/" target="_blank">{{ $store.state.settings.caseNumber }}</a>
      </template>
    </div>
  </section>
</template>

<script>
export default {
  name: 'AppMain',
};
</script>

<style lang="scss" scoped>
.app-main {
  position: relative;
  width: 100%;

  /* 50= navbar  50  */
  min-height: calc(100vh - 50px);
  overflow: hidden;
}

.fixed-header + .app-main {
  padding-top: 50px;
}

#el-main-footer {
  position: fixed;
  bottom: 0;
  z-index: 99;
  width: 100%;
  height: 33px;
  padding: 10px 6px 0 6px;
  overflow: hidden;
  font-family: Arial, sans-serif !important;
  font-size: 0.7rem !important;
  color: #7a8b9a;
  letter-spacing: 0.8px;
  pointer-events: none;
  background: none repeat scroll 0 0 white;
  border-top: 1px solid #e7eaec;
}
</style>

<style lang="scss">
// fix css style bug in open el-dialog
.el-popup-parent--hidden {
  .fixed-header {
    padding-right: 15px;
  }
}
</style>
