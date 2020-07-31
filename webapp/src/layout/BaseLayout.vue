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
  <div :class="classObj" class="app-wrapper">
    <div v-if="device==='mobile' && sidebar.opened" class="drawer-bg" @click="handleClickOutside" />
    <sidebar v-if="showSidebar" class="sidebar-container" />
    <div class="main-container">
      <div :class="{'fixed-header': fixedHeader}">
        <navbar :showBack="showBack" :showSidebar="showSidebar" :showTitle="showTitle" @go-back="onClickBack">
          <template v-slot:left>
            <slot name="left-options" />
          </template>
          <template v-slot:right>
            <slot name="right-options" />
            <Feedback />
          </template>
        </navbar>
      </div>
      <app-main />
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex';
import ResizeMixin from './mixin/ResizeHandler';
import { AppMain, Navbar, Sidebar, Feedback } from './components';

export default {
  name: 'BaseLayout',
  components: {
    AppMain,
    Navbar,
    Sidebar,
    Feedback,
  },
  mixins: [ResizeMixin],
  props: {
    showBack: {
      type: Boolean,
      default: false,
    },
    showTitle: {
      type: Boolean,
      default: true,
    },
    showSidebar: {
      type: Boolean,
      default: true,
    },
  },
  computed: {
    ...mapState({
      sidebar: state => state.app.sidebar,
      device: state => state.app.device,
      fixedHeader: state => state.settings.fixedHeader,
    }),
    classObj() {
      return {
        hideSidebar: this.showSidebar && !this.sidebar.opened,
        openSidebar: this.showSidebar && this.sidebar.opened,
        noSidebar: !this.showSidebar,
        withoutAnimation: this.sidebar.withoutAnimation,
        mobile: this.device === 'mobile',
      };
    },
  },
  methods: {
    handleClickOutside() {
      this.$store.dispatch('app/closeSideBar', { withoutAnimation: false });
    },
    onClickBack() {
      const backTo = this.$route?.meta?.backTo;
      // 指定跳转路由 name
      if (backTo) {
        this.$router.push({ name: backTo });
      } else {
        // 不存在历史记录
        // 或者新开 Tab
        if (!window.history.length || window.history.length === 1) {
          this.$router.push('/');
          return;
        }
        this.$router ? this.$router.back() : window.history.back();
      }
    },
  },
};
</script>

<style lang="scss" scoped>
  @import "~@/assets/styles/mixin.scss";
  @import "~@/assets/styles/variables.scss";

  .app-wrapper {
    @include clearfix;

    position: relative;
    width: 100%;
    height: 100%;

    .drawer-bg {
      position: absolute;
      top: 0;
      z-index: 999;
      width: 100%;
      height: 100%;
      background: #000;
      opacity: 0.3;
    }

    &.mobile.openSidebar {
      position: fixed;
      top: 0;
    }
  }

  .fixed-header {
    position: fixed;
    top: 0;
    right: 0;
    z-index: 9;
    width: calc(100% - #{$sideBarWidth});
    padding: 0;
    transition: width 0.28s;
  }

  .noSidebar .fixed-header {
    width: 100%;
  }

  .hideSidebar .fixed-header {
    width: calc(100% - #{$iconBarWidth});
  }

  .mobile .fixed-header {
    width: 100%;
  }
</style>
