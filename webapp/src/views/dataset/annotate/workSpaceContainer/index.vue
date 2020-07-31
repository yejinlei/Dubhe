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
  <div class="workspace-container flex flex-col">
    <!-- zoom {{ zoom }}
    <div>brush: {{ brush }}</div>
    <div>api: {{ api }}</div>
    <div>annotation: {{ state.annotations }}</div>
    <div>tooltipData: {{ tooltipData }}</div>
    <div>imgInfo {{ imgInfo }}</div>
    <div>svgDimentsion, {{ svgDimension }}</div> -->
    <div v-if="!state.error.value" v-hotkey.stop="keymap" class="workspace-stage flex flex-col f1">
      <ToolBar
        :zoomIn="zoomIn"
        :zoomOut="zoomOut"
        :zoomReset="resetZoom"
        :clearSelection="clearSelection"
        :api="api"
        :setApi="setApi"
        :confirm="confirm"
        v-on="listeners"
      />
      <div id="stage" ref="imgWrapperRef" v-loading="!imgInfo.loaded" class="f1 rel">
        <ZoomContainer
          ref="zoomRef"
          :controlled="true"
          :filter="filter"
          v-bind="zoom"
          :onZoom="handleZoom"
        >
          <div class="zoom-content">
            <div class="zoom-content-bound rel" :style="dimension.marginStyle">
              <div class="imgWrapper" :style="dimension.imgScaleStyle" :class="dimension.scale < 1 ? 'imgScale' : ''">
                <img ref="imgRef" :src="currentImg.url">
              </div>
              <!-- svg 宽高要根据图片自适应 -->
              <div class="annotation-element-group abs" :style="dimension.annotationGroupStyle">
                <svg
                  ref="svgRef"
                  class="canvas"
                  :class="api.active === 'selection' ? 'crosshair' : ''"
                  :style="dimension.svg"
                  @mousedown="handleMouseDown"
                  @mousemove="handleMouseMove"
                  @mouseup="handleMouseUp"
                >
                  <g class="annotation-group">
                    <Bbox
                      v-for="annotate in api.annotations"
                      :key="annotate.id"
                      :annotate="annotate"
                      :scale="dimension.scale"
                      :imgBoundingLeft="api.imgBoundingLeft"
                      :handleClick="handleBboxClick"
                      :currentAnnotationId="state.currentAnnotationId"
                    />
                  </g>
                  <BasicBrush :brush="brush" />
                </svg>
                <div v-if="state.showScore.value" class="annotation-score-group">
                  <Score
                    v-for="annotate in api.annotations"
                    :key="annotate.id"
                    :annotate="annotate"
                    :scale="dimension.scale"
                    :imgBoundingLeft="api.imgBoundingLeft"
                  />
                </div>
                <div v-if="state.showTag.value" class="annotation-tag-group">
                  <Tag
                    v-for="annotate in api.annotations"
                    :key="annotate.id"
                    :annotate="annotate"
                    :scale="dimension.scale"
                    :getLabelName="getLabelName"
                    :imgBoundingLeft="api.imgBoundingLeft"
                  />
                </div>
                <div v-if="state.showId.value && isTrack" class="annotation-tag-group">
                  <AnnotationId
                    v-for="annotate in api.annotations"
                    :key="annotate.id"
                    :annotate="annotate"
                    :scale="dimension.scale"
                    :getLabelName="getLabelName"
                    :imgBoundingLeft="api.imgBoundingLeft"
                  />
                </div>
              </div>
            </div>
          </div>
        </ZoomContainer>
        <DropDownLabel
          :visible="tooltipData.visible"
          v-bind="tooltipData"
          :hideTooltip="hideTooltip"
          :value="api.label.value"
          :handleChange="handleSelectChange"
          :labels="labels"
        />
      </div>
    </div>
    <Exception v-else>
      <template slot="desc">
        {{ (state.error.value || {}).message || 'error' }}
      </template>
    </Exception>
  </div>
</template>
`
<script>
import { ref, computed, watch, reactive, inject, onMounted, onBeforeUnmount } from '@vue/composition-api';
import { isNil } from 'lodash';
import { event as d3Event } from 'd3-selection';
import { Message } from 'element-ui';

import { labelsSymbol } from '@/views/dataset/util';
import { useBrush, BasicBrush, useZoom, unref, useTooltip, useImage } from '@/hooks';
import { getCursorPosition, getBounding, getZoomPosition, noop } from '@/utils';
import ZoomContainer from '@/components/ZoomContainer';
import Exception from '@/components/Exception';
import ToolBar from './toolbar';
import Bbox from './bbox';
import Score from './score';
import Tag from './tag';
import AnnotationId from './annotationId';
import DropDownLabel from './dropdownLabel';

const addEventListener = require('add-dom-event-listener');

const FooterHeight = 32;

// 侧边栏宽度
export const ThumbWidth = 160;

// msg 实例
let msgInstance = null;

export default {
  name: 'WorkSpaceContainer',
  components: {
    ZoomContainer,
    Exception,
    BasicBrush,
    ToolBar,
    Bbox,
    DropDownLabel,
    Score,
    Tag,
    AnnotationId,
  },
  props: {
    state: Object,
    currentImg: {
      type: Object,
      default: () => null,
    },
    handleBrushEnd: Function,
    createLabel: Function,
    queryLabels: Function,
    getLabelName: Function,
    updateState: Function,
    handleConfirm: Function,
    deleteAnnotation: Function,
    isTrack: Boolean,
  },
  setup(props, ctx) {
    const imgWrapperRef = ref(null);
    const svgRef = ref(null);
    const resizerRef = ref(null); // 事件句柄
    const imgRef = ref(null); // 图片

    // 当前所有标签信息
    const labels = inject(labelsSymbol);

    // 初始化状态
    const api = reactive({
      annotations: props.state.annotations,
      label: {}, // 一个页面当前只能存在一个标签
      bounding: null, // 容器位置信息
      isCenter: false, // 图片是否已居中
      imgBoundingLeft: null, // 图片的位置，给 bbox 位置定位使用
      active: '', // 当前选中
    });

    const { listeners } = ctx;
    const { handleBrushEnd, state, createLabel, queryLabels, updateState, deleteAnnotation, handleConfirm } = props;
    const {
      brush,
      onBrushStart,
      onBrushMove,
      onBrushEnd,
      onBrushReset,
    } = useBrush();

    const initialZoom = {
      zoom: unref(state.zoom),
      zoomX: unref(state.zoomX),
      zoomY: unref(state.zoomY),
    };

    // 初始放大和缩小函数
    const { zoomIn, zoomOut, setZoom, reset: resetZoom, zoom } = useZoom(initialZoom, imgWrapperRef);

    // tooltip
    const { tooltipData, showTooltip, hideTooltip } = useTooltip(imgWrapperRef);

    // 图片尺寸
    const { imgInfo = {}, setImg } = useImage();

    const filter = () => {
      // 如果没有开启选框
      // if (!state.selection.value) return true
      // 不允许通过鼠标拖拽来更改缩放
      return d3Event.type !== 'mousedown';
    };

    const setApi = params => {
      Object.assign(api, params);
    };

    // 监听 currentImage 变化
    watch(() => props.currentImg, (nextImg) => {
      // 每次切换图片重置 zoom
      resetZoom();
      // 重置选中的标签和位置
      Object.assign(api, {
        label: {},
        isCenter: false,
        imgBoundingLeft: null,
      });
      if (nextImg?.url) {
        setImg(nextImg.url);
      }
    }, {
      lazy: true,
    });

    watch(() => props.state, (nextProps) => {
      if ('annotations' in nextProps) {
        api.annotations = nextProps.annotations || [];
      }
    });

    const defaultDimension = {
      svg: {},
      wrapper: {},
      margin: {},
      scale: 1,
    };

    // 根据图片大小重新计算 svg 位置信息
    const dimension = computed(() => {
      // 当 dom 元素加载完毕来计算 svg 尺寸
      if (!isNil(api.bounding) && !!imgInfo.loaded && svgRef.value) {
        // 容器的最大宽度、高度
        const { width: cw, height: ch, left, top } = api.bounding;
        // 图片的宽度，高度
        const iw = imgInfo.width;
        const ih = imgInfo.height;

        const wrapperDimension = {
          width: cw,
          height: ch - FooterHeight,
          left,
          top,
        };

        // 当图片尺寸超过容器尺寸，进行缩放
        const imgScale = Math.min(wrapperDimension.width / imgInfo.width, wrapperDimension.height / imgInfo.height, 1);

        // 如果图片有缩放，直接取容器尺寸即可
        const svgDimension = {
          width: imgScale < 1 ? cw : Math.min(iw, cw),
          height: imgScale < 1 ? ch : Math.min(ih, ch),
        };

        // 标注相关元素的容器
        const annotationGroupStyle = {
          left: imgScale === 1 ? `${(cw - iw) / 2}px` : 0,
          top: imgScale === 1 ? `${(ch - FooterHeight - ih) / 2}px` : 0,
        };

        // 上面已经通过margin: 0 auto 做过宽度处理
        const ml = Math.max((wrapperDimension.width - iw * imgScale), 0) / 2;
        const mt = Math.max((wrapperDimension.height - ih * imgScale), 0) / 2;

        const marginStyle = {};
        if (ml > 0) {
          marginStyle['padding-left'] = `${ml}px`;
          marginStyle['padding-right'] = `${ml}px`;
        }
        if (mt > 0) {
          marginStyle['padding-top'] = `${mt}px`;
          marginStyle['padding-bottom'] = `${mt}px`;
        }
        const margin = {
          left: ml,
          top: mt,
        };

        const imgDimension = {
          width: iw,
          height: ih,
        };

        const imgScaleStyle = imgScale < 1 ? {
          width: `${imgInfo.width * imgScale}px`,
          height: `${imgInfo.height * imgScale}px`,
          transition: 'opacity 0.4s',
          opacity: 1,
        } : {};
        // 居中
        api.isCenter = true;
        return { svg: svgDimension, img: imgDimension, wrapper: wrapperDimension, margin, marginStyle, scale: imgScale, imgScaleStyle, annotationGroupStyle };
      } if (!imgInfo.loaded || imgInfo.width === 0 && imgInfo.height === 0) {
        // 加一个过渡效果，避免太唐突
        return { ...defaultDimension, imgScaleStyle: {
          opacity: 0,
        }};
      }
      return defaultDimension;
    });

    const findImgIndex = () => {
      const { files, currentImgId } = state;
      const currentImgIndex = files.value.findIndex(d => d.id === currentImgId.value);
      return { currentImgIndex, files };
    };

    const onMessageClose = () => {
      // 清理 message 实例
      msgInstance = null;
    };

    const handlePrev = () => {
      const { files, currentImgIndex } = findImgIndex();
      if (currentImgIndex > 0) {
        ctx.emit('changeImg', files.value[currentImgIndex - 1]);
      } else if (!msgInstance) {
          msgInstance = Message.warning({
            message: '当前图片不存在或图片已经到顶了',
            onClose: onMessageClose,
          });
        }
    };
    const handleNext = (callback) => {
      const { files, currentImgIndex } = findImgIndex();
      if (currentImgIndex > -1 && currentImgIndex < files.value.length - 1) {
        ctx.emit('changeImg', files.value[currentImgIndex + 1]);
        // 触发下一页数据
        ctx.emit('nextPage', files.value[currentImgIndex + 1], currentImgIndex + 1, files);
      } else if (typeof callback === 'function') {
          callback();
        } else if (!msgInstance) {
          msgInstance = Message.warning({
            message: '当前图片不存在或图片已经到顶了',
            onClose: onMessageClose,
          });
        }
    };

    // 删除注释
    const removeAnnotation = () => {
      hideTooltip();
      const currentAnnotationId = state.currentAnnotationId.value;
      if (currentAnnotationId) {
        deleteAnnotation(currentAnnotationId);
      }
    };

    // 选中标注，开始画框
    const selection = () => {
      setApi({ active: 'selection' });
      // 关闭已有的 dropdown
      hideTooltip();
      listeners.selection(true);
    };

    watch(() => api.isCenter, (isCenter) => {
      if (isCenter) {
        const { width: boundingWidth } = api.bounding;
        const { width: imgWidth } = getBounding(imgRef.value);
        // todo: 缩放图片取容器尺寸，否则取图片尺寸
        const mw = dimension.value.scale < 1 ? boundingWidth : dimension.value.img.width;

        Object.assign(api, {
          imgBoundingLeft: (mw - imgWidth) / 2,
        });
      }
    }, {
      lazy: true,
    });

    // 快捷键
    const keymap = computed(() => ({
      left: {
        keyup: handlePrev,
      },
      right: {
        keyup: handleNext,
      },
      backspace: removeAnnotation,
      delete: removeAnnotation,
      n: selection,
    }));

    const handleMouseDown = (event) => {
      if (brush.start && brush.end) {
        // 首先清理已有的 brush 状态
        onBrushReset();
      }
      // 关闭已有的 dropdown
      hideTooltip();
      // 判断是否开启选框
      if (!state.selection.value) return;
      const [x, y] = getCursorPosition(svgRef.value, event);
      // 根据绝对路径生成相对于 zoom 之后的位置
      const zoomePos = getZoomPosition(ctx.refs.zoomRef.wrapperRef, [x, y]);
      onBrushStart({ x: zoomePos[0], y: zoomePos[1] });
    };

    const handleMouseMove = (event) => {
      if (!brush.isBrushing) return;
      const [x, y] = getCursorPosition(svgRef.value, event);
      // 根据绝对路径生成相对于 zoom 之后的位置
      const zoomePos = getZoomPosition(ctx.refs.zoomRef.wrapperRef, [x, y]);
      onBrushMove({ x: zoomePos[0], y: zoomePos[1] });
    };

    const handleMouseUp = (event) => {
      if (brush.end) {
        const [x, y] = getCursorPosition(svgRef.value, event);
        // 根据绝对路径生成相对于 zoom 之后的位置
        const zoomePos = getZoomPosition(ctx.refs.zoomRef.wrapperRef, [x, y]);
        onBrushEnd(({ x: zoomePos[0], y: zoomePos[1] }));

        // 展示tooltip
        showTooltip({}, event);

        // 回调
        handleBrushEnd && handleBrushEnd(brush, event);
        onBrushReset();
        return;
      }
      onBrushReset();
    };

    // 清除选择框
    const clearSelection = () => listeners.selection(false);

    // 判断标签是否已存在
    const islabelExists = (value) => {
      return !!(labels.value || []).find(label => label.id === Number(value));
    };

    // 选中注释
    const handleBboxClick = (annotation) => () => {
      updateState({
        currentAnnotationId: annotation.id,
      });
    };

    // 人工确认
    const confirm = () => {
      handleConfirm().then(() => {
        // 切换到下一页
        Message.success({ message: '人工确认成功', duration: 800, onClose: () => handleNext(noop) });
      });
    };

    // 选中selectItem
    const handleSelectChange = async(value) => {
      let labelVal = value;
      // 首先判断标签是否已存在
      // 如果没有，就先创建
      if (!islabelExists(value)) {
        labelVal = await createLabel({ name: value });
        const nextLabels = await queryLabels();
        // 更新全局 provide
        updateState({
          labels: nextLabels,
        });
      }
      const { annotations = [], currentAnnotationId } = state;
      const selectedLabel = {
        ...api.label,
        value: labelVal,
      };
      Object.assign(api, {
        label: selectedLabel,
      });
      const curAnnotation = annotations.value.find(d => d.id === currentAnnotationId.value) || {};
      // 触发标注对应标签变更事件
      ctx.emit('selectLabel', { selectedLabel, curAnnotation });
    };

    const handleZoom = (nextZoomTransform) => {
      if (!nextZoomTransform) return;
      setZoom({
        zoomX: nextZoomTransform.x,
        zoomY: nextZoomTransform.y,
        zoom: nextZoomTransform.k,
      });
    };

    onMounted(() => {
      addEventListener(document.body, 'click', (e) => {
        // 如果不在画布内，直接清空
        // 过滤右侧设置标注 table
        if (!e.target.closest('.annotation-table') && !e.target.closest('#stage')) {
          // 清空选中的注释
          updateState({
            currentAnnotationId: '',
          });
        }
      });

      // resizerRef.value = addEventListener(window, 'resize', () => {
      //   api.bounding = getBounding(imgWrapperRef.value)
      // })
      // 初始化执行一次
      api.bounding = getBounding(imgWrapperRef.value);
    });

    onBeforeUnmount(() => {
      resizerRef.value && resizerRef.value.remove();
      resizerRef.value = null;
    });

    return {
      listeners,
      imgWrapperRef,
      // labels
      labels,
      // brush
      brush,
      handleMouseDown,
      handleMouseMove,
      handleMouseUp,
      clearSelection,
      filter,
      // zoom
      zoom,
      zoomIn,
      zoomOut,
      resetZoom,
      handleZoom,
      // tooltip
      tooltipData,
      hideTooltip,
      // img
      imgInfo,
      dimension,
      svgRef,
      imgRef,
      // annotations
      api,
      setApi,
      // event
      handleSelectChange,
      confirm,
      handleBboxClick,
      keymap,
    };
  },
};
</script>
<style lang='scss'>
  #stage {
    max-height: 100%;
  }

  .workspace-stage {
    flex: 1;
    overflow: hidden;

    .imgWrapper {
      &.imgScale {
        margin: 0 auto;

        img {
          display: inline-block;
          width: 100%;
          height: 100%;
        }
      }
    }

    .canvas {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: calc(100vh - 130px);
    }

    .annotation-score-group {
      .annotation-score-row {
        position: absolute;
        color: #fff;

        .score {
          display: inline-block;
          min-width: 48px;
          font-size: 16px;
          line-height: 24px;
          user-select: none;

          .unit {
            margin-left: 2px;
            font-size: 0.8em;
          }
        }
      }
    }

    .annotation-tag-group {
      .annotation-label {
        position: absolute;
        color: #fff;
        pointer-events: none;
      }
    }

    .bbox-group {
      cursor: pointer;
    }
  }

</style>
