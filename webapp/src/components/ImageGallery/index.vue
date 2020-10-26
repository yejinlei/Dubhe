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
  <div :class="rootClass" class="img-gallery">
    <ul v-if="dataImagesLocal.length" :class="rootClass + '__wrapper'">
      <li
        v-for="(dataImage) in dataImagesLocal"
        :key="dataImage.id"
        :class="rootClass + '__item'"
      >

        <div
          v-if="!isMultiple"
          :class="classThumbnail(singleSelected.id, dataImage.id)"
        >
          <img
            :src="formatUrl(dataImage.url)"
            :alt="dataImage.alt"
            :class="rootClass + '__img'"
          >

          <label
            v-if="useLabel"
            :class="rootClass + '__lbl'"
          >
            {{ dataImage.alt }}
          </label>
        </div>

        <div
          v-if="isMultiple"
          :class="classThumbnailMultiple(dataImage.id)"
          @mouseenter="onMouseEnter(dataImage)"
          @mouseleave="onMouseLeave(dataImage)"
        >
          <img
            :src="formatUrl(dataImage.url)"
            :alt="dataImage.alt"
            :class="rootClass + '__img'"
            @click="onClickImg(dataImage)"
          >
          <el-tag v-if="imageTagVisible && statusCodeMap[dataImage.status] !== 'UNANNOTATED'" :hit="false" class="image-tag" :color="imageLabelTag[dataImage.id]['color']">{{ imageLabelTag[dataImage.id]['text'] }}</el-tag>
          <el-checkbox v-show="showOption(dataImage.id)" :value="selectedMap[dataImage.id]" class="image-checkbox" @change="checked => handleCheck(dataImage, checked)" />
          <div v-show="showOption(dataImage.id)" :title="dataImage.name" class="img-name-row">
            <div class="img-name">{{ basename(dataImage.url) }}</div>
          </div>

          <label
            v-if="useLabel"
            :class="rootClass + '__lbl'"
          >
            {{ dataImage.alt }}
          </label>
        </div>
      </li>
    </ul>
  </div>
</template>

<script>
import Vue from 'vue';
import { bucketHost } from '@/utils/minIO';
import { fileCodeMap, findKey, statusCodeMap } from '@/views/dataset/util';

// eslint-disable-next-line import/no-extraneous-dependencies
const path = require('path');

export default {
  name: 'ImageGallery',
  props: {
    dataImages: {
      type: Array,
      default: () => [],
    },
    selectImgsId: {
      type: Array,
      default: () => [],
    },
    categoryId2Name: {
      type: Object,
      default: () => {},
    },
    isMultiple: {
      type: Boolean,
      default: false,
    },
    useLabel: {
      type: Boolean,
      default: false,
    },
    rootClass: {
      type: String,
      default: 'vue-select-image',
    },
    activeClass: {
      type: String,
      default: '--selected',
    },
  },
  data() {
    return {
      singleSelected: {
        id: '',
      },
      multipleSelected: [],
      imageTagVisible: true,
      imgStatusMap: {
        'UNRECOGNIZED': {'text': '未识别', 'color': '#FFFFFF'},
        'UNANNOTATED': {'text': '未标注', 'color': '#FFFFFF'},
        'AUTO_ANNOTATED': { 'text': '自动', 'color': '#468CFF' },
        'MANUAL_ANNOTATED': { 'text': '人工', 'color': '#FF9943' },
      },
      hoverImg: null,
      statusCodeMap,
    };
  },
  computed: {
    dataImagesLocal() {
      return this.dataImages || [];
    },
    selectedMap() {
      const m = {};
      this.dataImagesLocal.forEach((item) => {
        const isSelected = this.selectImgsId.includes(item.id);
        m[item.id] = isSelected;
      });
      return m;
    },
    imageLabelTag() {
      const labelTag = {};
      this.dataImages.forEach((item) => {
        const statusInfo = this.imgStatusMap[findKey(item.status, fileCodeMap)];
        const annotation = JSON.parse(item.annotation);
        let categoryName = '未识别';
        let tagColor = '#db2a2a';
        if (statusInfo && (annotation instanceof Array) && annotation.length > 0) {
          const categoryId = annotation[0].category_id;
          categoryName = this.categoryId2Name[categoryId];
          tagColor = statusInfo.color;
        }
        labelTag[item.id] = {
          'text': `${statusInfo.text} | ${categoryName}`,
          'color': tagColor,
        };
      });
      return labelTag;
    },
  },
  mounted() {
    // set initial selectedImage
    this.setInitialSelection();
  },
  methods: {
    formatUrl(url) {
      return `${bucketHost}/${url}`;
    },
    basename(imgOrigin) {
      return path.basename(imgOrigin);
    },
    classThumbnail(selectedId, imageId) {
      const baseClass = `${this.rootClass}__thumbnail`;
      if (selectedId === imageId) {
        return `${baseClass} ${baseClass}${this.activeClass}`;
      }
      return `${baseClass}`;
    },
    classThumbnailMultiple(id) {
      const baseClass = `${this.rootClass}__thumbnail`;
      const baseMultipleClass = `${baseClass} is--multiple`;
      if (this.hasSelected(id)) {
        return `${baseMultipleClass} ${baseClass}${this.activeClass}`;
      }
      return `${baseMultipleClass}`;
    },
    onSelectImage(objectImage) {
      this.singleSelected = { ...this.singleSelected, ...objectImage};
      this.$emit('onselectimage', objectImage);
    },
    onClickImg(objectImage) {
      this.$emit('clickImg', objectImage, this.multipleSelected);
      if (this.multipleSelected.length > 0) {
        const checked = this.hasSelected(objectImage.id);
        this.toggleCheck(objectImage, !checked);
      }
    },
    onMouseEnter(objectImage) {
      Vue.set(objectImage, 'isHover', true);
      this.hoverImg = objectImage;
    },
    onMouseLeave(objectImage) {
      if (objectImage.isHover) {
        Vue.set(objectImage, 'isHover', false);
        this.hoverImg = null;
      }
    },
    isHover(id) {
      return this.hoverImg?.id === id;
    },
    hasSelected(id) {
      return this.multipleSelected.find(item => id === item);
    },
    showOption(id) {
      return this.isHover(id) || this.hasSelected(id);
    },
    removeFromSingleSelected() {
      this.singleSelected = {};
      this.$emit('onselectimage', {});
    },
    removeFromMultipleSelected(id, dontFireEmit) {
      this.multipleSelected = this.multipleSelected.filter((item) => id !== item);
      if (!dontFireEmit) {
        this.$emit('onselectmultipleimage', this.multipleSelected);
      }
    },
    resetMultipleSelection() {
      this.multipleSelected = [];
      for (const key in this.selectedMap) {
        this.selectedMap[key] = false;
      }
      this.$emit('onselectmultipleimage', this.multipleSelected);
    },
    selectAll() {
      this.multipleSelected = this.dataImages.map(d => d.id);
      for (const key in this.selectedMap) {
        this.selectedMap[key] = true;
      }
      this.$emit('onselectmultipleimage', this.multipleSelected);
    },
    handleCheck(objectImage, checked) {
      return this.toggleCheck(objectImage, checked);
    },
    toggleCheck(objectImage, checked) {
      if (checked) {
        this.multipleSelected.push(objectImage.id);
        this.selectedMap[objectImage.id] = true;
      } else {
        this.removeFromMultipleSelected(objectImage.id, true);
        this.selectedMap[objectImage.id] = false;
      }

      this.$emit('onselectmultipleimage', this.multipleSelected);
    },
    setInitialSelection() {
      if (this.selectImgsId) {
        if (!this.isMultiple && this.selectImgsId.length === 1) {
          this.singleSelected = { ...this.selectImgsId[0]};
        } else {
          this.multipleSelected = [].concat(this.selectImgsId);
        }
      }
    },
    setImageTagVisible(tagVisible) {
      this.imageTagVisible = tagVisible;
    },
  },
};
</script>

<style lang="scss" scoped>
@import "~@/assets/styles/variables.scss";
@import "~@/assets/styles/mixin.scss";

.img-gallery {
  min-height: 200px;
}

.image-tag {
  position: absolute;
  top: 3px;
  left: 3px;
  max-width: 165px;
  height: unset;
  color: #fff;
  white-space: normal;
  border-width: 0;
  border-radius: 6px 0 8px 0;
}

.img-name-row {
  position: absolute;
  right: 3px;
  bottom: 3px;
  left: 3px;
  padding-left: 4px;
  color: #fff;
  background-color: $black;
  border-radius: 0 0 4px 4px;
}

.img-name {
  @include text-overflow;

  max-width: 90%;
  font-size: 14px;
  line-height: 2;
}

.image-checkbox {
  position: absolute;
  top: 10px;
  right: 10px;
}

.image-checkbox >>> .el-checkbox__inner {
  border-radius: 10px;
}

.vue-select-image__wrapper {
  padding: 0;
  margin: 0;
  overflow: auto;
  list-style-position: outside;
  list-style-type: none;
  list-style-image: none;
}

.vue-select-image__item {
  float: left;
  width: 200px;
  height: 200px;
  margin: 0 12px 12px 0;
}

.vue-select-image__thumbnail {
  position: relative;
  padding: 3px;
  line-height: 20px;
  border-color: transparent;
  border-style: solid;
  border-width: 1px;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.055);
  transition: all 0.2s ease-in-out;

  &:hover {
    border-color: $primaryColor;
  }

  &.vue-select-image__thumbnail--selected {
    border-color: $primaryColor;
  }
}

.vue-select-image__img {
  -webkit-user-drag: none;
  display: block;
  width: 192px;
  height: 192px;
  margin-right: auto;
  margin-left: auto;
  object-fit: cover;
  border-radius: 6px;
}

.vue-select-image__lbl {
  line-height: 3;
}

@media only screen and (min-width: 1200px) {
  .vue-select-image__item {
    margin-left: 30px;
  }
}
</style>
