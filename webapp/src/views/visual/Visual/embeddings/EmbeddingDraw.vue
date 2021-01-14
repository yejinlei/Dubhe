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

<style lang="less" scoped>
/deep/ .dimension {
  .axis text {
    font-size: 12px;
    stroke: rgb(115, 111, 188);
  }

  .axis path {
    stroke: rgb(115, 111, 188);
    stroke-width: 2px;
  }

  .axis line {
    stroke: rgb(115, 111, 188);
    stroke-width: 2px;
  }
}

/deep/ #subgroup {
  circle {
    cursor: pointer;
  }
}

#background path {
  fill: none;
  stroke: #ccc;
  stroke-opacity: 0.4;
  shape-rendering: crispEdges;
}

/deep/ #foreground {
  path {
    cursor: pointer;
  }
}

.brush .extent {
  fill-opacity: 0.3;
  stroke: #fff;
  shape-rendering: crispEdges;
}

svg {
  padding: 10px;
  border-radius: 10px;
}

.draw {
  display: block;
  width: 100%;
  height: 100%;
}

.container {
  position: relative;
  display: block;
  width: 100%;
  height: 100%;
}

#threeDimension {
  display: block;
  width: 100%;
  height: 100%;
}

#towDimension {
  display: block;
  width: 100%;
  height: 100%;
}

.loading {
  position: fixed;
  top: 50%;
  left: 50%;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  -webkit-transform: translate(-50%, -50%);
  -moz-transform: translate(-50%, -50%);
  -ms-transform: translate(-50%, -50%);
  -o-transform: translate(-50%, -50%);
  transform: translate(-50%, -50%);
}

.noloading {
  position: fixed;
  top: 50%;
  left: 50%;

  /* z-index: -9999; */
  display: none;
  width: 400px;
  height: 400px;
  -webkit-transform: translate(-50%, -50%);
  -moz-transform: translate(-50%, -50%);
  -ms-transform: translate(-50%, -50%);
  -o-transform: translate(-50%, -50%);
  transform: translate(-50%, -50%);
}

#echarts {
  display: block;
  width: 100%;
  height: 100%;
  padding: 10px;
  border: 1px solid #e1e1e1;
  border-radius: 10px;
}

#title {
  position: absolute;
  left: 50%;
  margin-top: 2%;
  font-size: 20px;
  color: #1363a0;
  transform: translateX(-50%);
}

.image {
  display: block;
  width: 100%;
}

.labelBackground {
  background-color: yellowgreen;
}
</style>

<template>
  <div class="container">
    <span id="title">#{{ getCurInfo.curMethod }} - {{ getCurInfo.curDim }} | Current-Step : {{ getCurInfo.curMapStep }}</span>
    <div id="main" ref="draw" class="draw">
      <div v-show="getCurInfo.curDim === '3维'" id="threeDimension" style="width: 100%;" />
    </div>
  </div>
</template>

<script>
import * as d3 from 'd3';
import echarts from 'echarts';
import constant from '@/utils/VisualUtils/constants';
import 'echarts-gl';
import { createNamespacedHelpers } from 'vuex';

const {
  mapGetters: mapEmbeddingGetters,
  mapMutations: mapEmbeddingMutations,
} = createNamespacedHelpers('Visual/embedding');
const { mapState: mapLayoutStates } = createNamespacedHelpers('Visual/layout');
export default {
  data() {
    return {
      width: '100%',
      height: '100%',
      loadFlag: true,
      halfHight: 0,
      halfWidth: 0,
      circleUpdate: '',
      circleEnter: '',
      circleExit: '',
      axisPoint: [
        [100, 0, 0],
        [0, 100, 0],
        [0, 0, 100],
      ],
      axisUpdate: '',
      axisEnter: '',
      axisExit: '',
      position: 0, // 鼠标拖拽功能
      xAxisFloat: 80,
      yAxisFloat: 60,
      xScale: '',
      yScale: '', // 缩放统一
      xAxis: '',
      yAxis: '',
      margin: {
        top: 100,
        right: 120,
        bottom: 100,
        left: 120,
      },
      dataAttr: {
        maxX: 0,
        maxY: 0,
        minX: 0,
        minY: 0,
      },
      ParaCoor: {
        backgroundUpdate: '',
        backgroundExit: '',
        backgroundEnter: '',
        foregroundUpdate: '',
        foregroundExit: '',
        foregroundEnter: '',
        dimensionsUpdate: '',
        dimensionsExit: '',
        dimensionsEnter: '',
        dimensionFlag: false, // 是否已经加入坐标轴
      },
      clickEle: false, // 是否选点
      myChart: '',
      localCurInfo: {},
    };
  },
  computed: {
    ...mapEmbeddingGetters([
      'getCurInfo',
      'getCurData',
      'getCheckLabels',
      'getLegendColor',
      'getLineWidth',
    ]),
    ...mapLayoutStates([
      'userSelectRunFile',
    ]),
    clickEleCumputed: {
      get() {
        return this.clickEle;
      },
      set(val) {
        this.clickEle = val;
      },
    },
  },
  watch: {
    getLineWidth() {
      this.renderUpdate(this.getCurData);
    },
    getCurInfo: {
      handler(val) {
        if (JSON.stringify(this.localCurInfo) === '{}') {
          this.localCurInfo = JSON.parse(JSON.stringify(val));
          this.renderInit();
        } else if (val.curDim !== this.localCurInfo.curDim) {
            this.localCurInfo = JSON.parse(JSON.stringify(val));
            this.renderInit();
          }
      },
      deep: true,
    },
    getCurData: {
      handler(val) {
        this.renderUpdate(val);
      },
      deep: true,
    },
    getCheckLabels() {
      this.renderUpdate(this.getCurData);
    },
    userSelectRunFile() {
      this.renderInit();
    },
  },
  mounted() {
    this.renderInit();
    if (typeof (this.getCurData) !== 'undefined') {
      this.renderUpdate(this.getCurData);
    }
  },
  methods: {
    ...mapEmbeddingMutations(['setMessage']),
    renderInit() {
      if (JSON.stringify(this.localCurInfo) === '{}') {
        // empty
      } else if (parseInt(this.localCurInfo.curDim, 10) === 3) { // 是3维显示
        this.renderThreeInit();
      } else if (parseInt(this.localCurInfo.curDim, 10) === 2) {
        this.renderTwoInit();
      }
    },
    unique(array) {
      return Array.from(new Set(array));
    },
    renderTwoInit() {
      const vm = this;
      this.height = d3.select(this.$refs.draw).node().getBoundingClientRect().height;
      this.width = d3.select(this.$refs.draw).node().getBoundingClientRect().width;
      d3.select(this.$refs.draw).select('svg').remove(); // 先清空svg
      const svg = d3.select(this.$refs.draw).append('svg')
        .attr('width', '100%')
        .attr('height', '100%')
        .attr('preserveAspectRatio', 'xMidYMid meet') // 自适应相关
        .attr('viewBox', `0 0 ${this.width} ${this.height}`);
      vm.margin = {
        top: 100,
        right: 120,
        bottom: 100,
        left: 120,
      };
      const g = svg.append('g')
        .attr('transform', `translate(${vm.margin.left}, ${vm.margin.top})`)
        .attr('id', 'maingroup');
      vm.xScale = d3.scaleLinear()
        .domain([vm.dataAttr.minX, vm.dataAttr.maxX])
        .range([0, this.width - this.margin.left - this.margin.right]);
      vm.yScale = d3.scaleLinear()
        .domain([vm.dataAttr.minY, vm.dataAttr.maxY].reverse())
        .range([0, this.height - this.margin.top - this.margin.bottom]);
      g
        .append('defs')
        .append('clipPath')
        .attr('id', 'clip')
        .append('rect')
        .attr('width', (vm.width - vm.margin.left - vm.margin.right))
        .attr('height', (vm.height - vm.margin.top - vm.margin.bottom))
        .attr('x', 0)
        .attr('y', 0);
      vm.yAxis = d3.axisLeft(vm.yScale)
        .tickSize(-(this.width - this.margin.left - this.margin.right))
        .tickPadding(10);
      vm.xAxis = d3.axisBottom(vm.xScale)
        .tickSize(-(this.height - this.margin.top - this.margin.bottom))
        .tickPadding(10);
      // eslint-disable-next-line
      const xAxisLabel = 'X'
      // eslint-disable-next-line
      const yAxisLabel = 'Y'
      // eslint-disable-next-line
      var yAxisGroup = g
        .append('g')
        .call(vm.yAxis)
        .attr('id', 'yaxis')
        .attr('font-size', '16px')
        .attr('color', '#736FBC');
      // eslint-disable-next-line
      var xAxisGroup = g
        .append('g')
        .call(vm.xAxis)
        .attr('transform', `translate(${0}, ${(vm.height - vm.margin.top - vm.margin.bottom)})`)
        .attr('id', 'xaxis')
        .attr('font-size', '16px')
        .attr('color', '#736FBC');
      // 新增 tooltip
      d3.select(this.$refs.draw).selectAll('.tooltip').remove();
      d3.select(this.$refs.draw).append('div')
        .attr('class', 'tooltip')
        .style('opacity', 0)
        .style('background-color', 'white')
        .style('border', 'solid')
        .style('border-width', '1px')
        .style('border-radius', '20px')
        .style('padding', '10px')
        .style('position', 'absolute')
        .style('z-index', '5')
        .style('left', '0')
        .style('top', '0')
        .style('color', '#7C78C0');
      let idleTimeout = null;
      function idled() {
        idleTimeout = null;
      }
      const idleDelay = 350;
      g.append('g')
        .attr('class', 'brush');
      const gscatter = g
        .append('g')
        .attr('clip-path', 'url(#clip)')
        .attr('id', 'subgroup');
      function zoom() {
        const t = gscatter.transition().duration(750);
        g.select('#xaxis')
          .transition(t)
          .call(vm.xAxis);
        g.select('#yaxis')
          .transition(t)
          .call(vm.yAxis);
        gscatter
          .selectAll('circle')
          .transition(t)
          .attr('cx', datum => {
            return vm.xScale(datum[0]);
          })
          .attr('cy', datum => {
            return vm.yScale(datum[1]);
          });
      }

      const brush = d3
        .brush()
        .extent([
          [0, 0],
          [(vm.width - vm.margin.left - vm.margin.right), (vm.height - vm.margin.top - vm.margin.bottom)],
        ])
        .on('end', () => {
          const s = d3.event.selection;
          if (!s) {
            if (!idleTimeout) {
              idleTimeout = setTimeout(idled, idleDelay);
              return idleTimeout;
            }
            // 还原
            vm.xScale
              .domain(d3.extent(vm.getCurData.data, (d) => d[0]))
              .range([0, (vm.width - vm.margin.left - vm.margin.right)])
              .nice();
            vm.yScale
              .domain(d3.extent(vm.getCurData.data, (d) => d[1]).reverse())
              .range([0, (vm.height - vm.margin.top - vm.margin.bottom)])
              .nice();
          } else {
            // 逻辑上是缩放
            vm.xScale
              .domain([s[0][0], s[1][0]].map(vm.xScale.invert, vm.xScale)) // X1 X2
              .range([0, (vm.width - vm.margin.left - vm.margin.right)])
              .nice();
            vm.yScale
              .domain([s[0][1], s[1][1]].map(vm.yScale.invert, vm.yScale)) // Y1 Y2
              .range([0, (vm.height - vm.margin.top - vm.margin.bottom)])
              .nice();
            g.select('.brush').call(brush.move, null);
          }
          zoom();
        });
      d3.selectAll('.brush')
        .call(brush);
    },
    renderThreeInit() {
      d3.select('#threeDimension').selectAll('div').remove();
      d3.select('#threeDimension')
        .append('div')
        .attr('id', 'threeChild')
        .style('width', '100%')
        .style('height', '100%')
        .style('display', 'block');
      const vm = this;
      this.height = d3.select(this.$refs.draw).node().getBoundingClientRect().height;
      this.width = d3.select(this.$refs.draw).node().getBoundingClientRect().width;

      d3.select(this.$refs.draw).select('svg').remove(); // 先清空svg
      vm.margin = {
        top: 100,
        right: 120,
        bottom: 100,
        left: 120,
      };
    },
    renderTwoUpdate(localData) {
      const vm = this;
      const g = d3.select('#maingroup');
      const gscatter = d3.select('#subgroup');
      this.dataAttr.minX = d3.min(localData.data, (d) => { return d[0]; });
      this.dataAttr.maxX = d3.max(localData.data, (d) => { return d[0]; });
      this.dataAttr.minY = d3.min(localData.data, (d) => { return d[1]; });
      this.dataAttr.maxY = d3.max(localData.data, (d) => { return d[1]; });
      this.height = d3.select(this.$refs.draw).node().getBoundingClientRect().height;
      this.width = d3.select(this.$refs.draw).node().getBoundingClientRect().width;
      vm.halfHight = this.height / 2;
      vm.halfWidth = this.width / 2;
      vm.xScale = d3.scaleLinear()
        .domain([vm.dataAttr.minX, vm.dataAttr.maxX])
        .range([0, this.width - this.margin.left - this.margin.right])
        .nice();
      vm.yScale = d3.scaleLinear()
        .domain([vm.dataAttr.minY, vm.dataAttr.maxY].reverse())
        .range([0, this.height - this.margin.top - this.margin.bottom])
        .nice();
        // Adding axes
      vm.yAxis = d3.axisLeft(vm.yScale)
        .tickSize(-(this.width - this.margin.left - this.margin.right));
      vm.xAxis = d3.axisBottom(vm.xScale)
        .tickSize(-(this.height - this.margin.top - this.margin.bottom))
        .tickPadding(10);
      const t = gscatter.transition().duration(750);
      g.select('#xaxis')
        .transition(t)
        .call(vm.xAxis);
      g.select('#yaxis')
        .transition(t)
        .call(vm.yAxis);

      vm.circleUpdate = gscatter.selectAll('circle').data(localData.data);
      vm.circleExit = vm.circleUpdate.exit();
      vm.circleEnter = vm.circleUpdate.enter().append('circle')
        .attr('cy', (datum) => { return vm.yScale(datum[1]); })
        .attr('cx', (datum) => { return vm.xScale(datum[0]); })
        .attr('r', 3)
        .attr('fill', function myFill(datum, index) {
          if (typeof (vm.getCurData.labelTypeColor[localData.label[index].toString()]) !== 'undefined') {
            return vm.getCurData.labelTypeColor[localData.label[index].toString()];
          } 
            return vm.getLegendColor[9];
        })
        .attr('opacity', 1)
        .on('click', function myClick(datum, i) {
          d3.event.stopPropagation(); // 阻止事件冒泡即可
          vm.clickEleCumputed = true;
          const value = localData.label[i];
          vm.setMessage([`${constant.IMGURl}/api/projector_sample?run=${vm.userSelectRunFile}&tag=${vm.getCurInfo.curTag}&index=${i}`, `第${i + 1}个点,标签为${value}`]);
        })
        .on('mouseover', function myMouseOver(datum, index) {
          const div = d3.selectAll('div.tooltip');
          const value = localData.label[index];
          div
            .style('opacity', 0.9)
            .style('left', `${d3.event.offsetX + 30  }px`)
            .style('top', `${d3.event.offsetY - 10  }px`)
            .style('z-index', 5);
          div.html(`第${index + 1}个点，标签为${value}`);
        })
        .on('mouseout', () => {
          const div = d3.select('div.tooltip');
          div
            .style('opacity', 0)
            .style('z-index', -1);
        });
      vm.circleUpdate.merge(vm.circleEnter).transition().ease(d3.easeLinear).duration(1000)
        .attr('cy', (datum) => { return vm.yScale(datum[1]); })
        .attr('cx', (datum) => { return vm.xScale(datum[0]); })
        .attr('fill', function myFill(datum, index) {
          if (typeof (vm.getCurData.labelTypeColor[localData.label[index].toString()]) !== 'undefined') {
            return vm.getCurData.labelTypeColor[localData.label[index].toString()];
          } 
            return vm.getLegendColor[9];
        });
      vm.circleExit.remove();

      // draw legend 颜色校验等增加鼠标点击的事件吧
      // 删除所有legend
      d3.select('#maingroup').selectAll('.legend').remove();
      const legend = d3.select('#maingroup').selectAll('.legend')
        .data(localData.labelType)
        .enter().append('g')
        .attr('class', 'legend')
        .attr('transform', function myTransform(d, i) { return `translate(${  vm.width - vm.margin.left - vm.margin.right + 30  },${  -(i + 1) * 25 + vm.height - vm.margin.bottom - vm.margin.top  })`; });
      // draw legend colored rectangles
      legend.append('circle')
        .data(localData.labelType)
        .attr('cx', '15')
        .attr('cy', '10')
        .attr('r', 8)
        .style('fill', function myFill(d, i) {
          return vm.getLegendColor[localData.labelType.length - 1 - i];
        });
      // draw legend text
      legend.append('text')
        .data(localData.labelType)
        .attr('class', 'legend_text')
        .attr('x', 40)
        .attr('y', 9)
        .attr('dy', '.5em')
        .attr('fill', 'black')
        .style('text-anchor', 'start')
        .text((d, i) => { return localData.labelType[localData.labelType.length - 1 - i]; });
    },
    renderThreeUpdate(localData) {
      const vm = this;
      // 开始数据投篮栏
      const myData = {};
      for (let i = 0; i < localData.labelType.length; i+=1) {
        myData[localData.labelType[i]] = [];
        myData[localData.labelType[i]].push(['x', 'y', 'z', 'index']);
      }
      for (let i = 0; i < localData.label.length; i+=1) {
        if (localData.labelType.indexOf(localData.label[i].toString()) >= 10 || localData.labelType.indexOf(localData.label[i].toString()) === -1) { // 序列大于10
          myData[localData.labelType[9]].push(localData.data[i].concat(i));
        } else {
          myData[localData.label[i].toString()].push(localData.data[i].concat(i));
        }
      }
      // 数据分栏结束
      const option = {
        tooltip: {
          trigger: 'item',
          backgroundColor: 'rgba(255,255,255,0.8)',
          borderWidth: 1,
          borderColor: 'rgb(124,120,192)',
          padding: [5, 5, 5, 5],
          textStyle: {
            color: 'rgb(124,120,192)',
          },
          formatter(params) {
            const value = localData.label[params.data[3]];
            return `第${params.data[3]}个点，标签为${value}`;
          },
        },
        grid3D: {},
        xAxis3D: {
          color: '#FFFFFF',
        },
        yAxis3D: {
          color: '#FFFFFF',
        },
        zAxis3D: {
          color: '#FFFFFF',
        },
        color: this.getLegendColor,
        legend: {
          right: 50,
          bottom: 50,
          data: localData.labelType,
          orient: 'vertical',
        },
        label: {
          formatter(params) {
            return params.name;
          },
          position: 'right',
          show: false,
          color: '#FFFFFF',
        },
        emphasis: {
          label: {
            show: true,
          },
        },
        series: [],
      };
      for (let i = 0; i < localData.labelType.length; i+=1) {
        option.series.push({
          name: localData.labelType[i],
          type: 'scatter3D',
          symbolSize: 5,
          data: myData[localData.labelType[i]],
          itemStyle: {
          },
        });
      }
      this.myChart = echarts.init(document.getElementById('threeChild'), 'light');
      this.myChart.setOption(option);
      this.myChart.on('click', function myClick(params) {
        const value = localData.label[params.data[3]];
        vm.setMessage([`${constant.IMGURl}/api/projector_sample?run=${vm.userSelectRunFile}&tag=${vm.getCurInfo.curTag}&index=${params.data[3]}`, `点击第${params.data[3]}个点,标签为${value}`]);
      });

      window.addEventListener('resize', () => {
        if (this.myChart) {
          this.myChart.resize();
        }
      });
    },
    renderUpdate(localData) {
      if (JSON.stringify(this.localCurInfo) === '{}') {
        return;
      }
      if (parseInt(this.localCurInfo.curDim, 10) === 3) { // 是3维显示
        this.renderThreeUpdate(localData);
      } else if (parseInt(this.localCurInfo.curDim, 10) === 2) {
        this.renderTwoUpdate(localData);
      }
    },
  },
};
</script>
