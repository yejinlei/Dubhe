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
  <div :class="className" style="width: 100%; height: 100%;" />
</template>

<script>
import * as d3 from 'd3';
import { createNamespacedHelpers } from 'vuex';

const { mapGetters: mapStatisticGetters, mapMutations: mapStatisticMutations } = createNamespacedHelpers('Visual/statistic');

export default {
  name: 'Onethreed',
  props: {
    data: Array,
    ttlabel: String,
    tag: String,
    itemp: Number,
    className: String,
    runColor: String,
  },
  data() {
    return {
      id: `offset${  this.itemp}`,
    };
  },
  computed: {
    ...mapStatisticGetters(['getShowNumber', 'getFeatchDataFinished']),
  },
  watch: {
    data() {
      // 清空原先所有图形，用清空dom的方式
      document.getElementsByClassName(this.className)[0].innerHTML = '';
      // 重绘
      this.drawOffset();
    },
  },
  mounted() {
    this.drawOffset();
    if (this.getFeatchDataFinished) {
      this.setDrawAllSvgFinished(true);
    }
  },
  methods: {
    ...mapStatisticMutations(['setStatisticInfo', 'setDrawAllSvgFinished']),
    // 显示比率
    drawOffset() {
      const label = this.id;
      const {data} = this;
      const className = `.${  this.className}`;
      const that = this;
      // 先找到value和number的最大值和最小值
      let numberMin = 10000;
      let numberMax = 0;
      for (let i = 0; i < data.length; i+=1) {
        for (let j = 0; j < data[i].length; j+=1) {
          const pixel = data[i][j][1];
          if (numberMin > pixel) numberMin = pixel;
          if (numberMax < pixel) numberMax = pixel;
        }
      }
      // 左右上下增加一个宽度，防止顶格
      const valueMin = data[0][0][0];
      const valueMax = data[0][data[0].length - 1][0];
      // 画svg
      const areaHeight = 50;
      const heightTop = 10;
      const padding = { top: areaHeight + heightTop, right: 45, bottom: 20, left: 10 };
      const svgWidth = 290;
      const svgHeight = 250;
      const width = svgWidth - padding.left - padding.right;
      const height = svgHeight - padding.top - padding.bottom;
      const div = d3
        .select(className)
        .append('div')
        .attr('id', `${label  }div`)
        .attr('width', '100%')
        .attr('height', '100%');
      const outersvg = div
        .append('svg')
        .attr('id', label) // 在放大缩小时有用
        .attr('width', '100%')
        .attr('height', '100%')
        .attr('preserveAspectRatio', 'xMidYMid meet')
        .attr('viewBox', '0 0 290 250');
      const svg = outersvg.append('g');

      // 画坐标轴
      const xscale = d3
        .scaleLinear()
        .domain([valueMin, valueMax])
        .rangeRound([0, width])
        .nice();
      svg
        .append('g')
        .attr('class', 'axis')
        .attr(
          'transform',
          `translate(${  padding.left  },${  padding.top + height  })`,
        )
        .call(d3.axisBottom()
          .scale(xscale)
          .ticks(5),
        );
      const stepscale = d3
        .scaleLinear()
        .domain([data[0][0][2], data[data.length - 1][0][2]])
        .rangeRound([0, height])
        .nice();
      svg
        .append('g')
        .attr('class', 'axis stepaxis')
        .attr(
          'transform',
          `translate(${  padding.left + width  },${  padding.top  })`,
        )
        .call(d3.axisRight()
          .scale(stepscale)
          .tickFormat(d => {
            if (d > 10000) {
              const numLen = d.toString().length - 1;
              // eslint-disable-next-line no-restricted-properties
              return `${d / Math.pow(10, numLen)  }e+${  numLen}`;
            } if (d < 0.001) {
              if (d === 0) return d;
              const dString = d.toString();
              let i = 3;
              for (; i < dString.length; i+=1) {
                if (dString[i] !== '0') {
                  break;
                }
              }
              // eslint-disable-next-line no-restricted-properties
              return `${(d * Math.pow(10, i - 1)).toFixed(1)  }e-${  i - 1}`;
            }
            return d;
          }),
        );
      const yscale = d3
        .scaleLinear()
        .domain([numberMin, numberMax])
        .rangeRound([areaHeight, 0]);
      // 显示信息
      svg.append('g').append('text')
        .attr('transform', 'rotate(90)')
        .attr('y', -(svgWidth - 13))
        .attr('x', svgHeight / 2)
        .attr('fill', 'grey')
        .attr('font-size', '10px')
        .text('step');
      const pathg = svg.append('g').attr('class', 'areapathg');
      svg
        .append('g')
        .append('text')
        .attr('class', 'textbox')
        .attr('visibility', 'hidden')
        .attr('fill', 'black')
        .attr('x', padding.left - 20)
        .attr('y', padding.top - 30)
        .style('font-size', '9px');
      svg
        .append('g')
        .selectAll('circle')
        .data(data)
        .enter()
        .append('circle')
        .attr('fill', '#fff45a');
      svg
        .append('g')
        .append('line')
        .attr('id', 'xaxisline')
        .attr('stroke-dasharray', '10')
        .attr('visibility', 'hidden');
      svg
        .append('g')
        .append('rect')
        .attr('y', padding.top + height + 7)
        .attr('width', 32)
        .attr('height', 15)
        .attr('fill', 'white')
        .attr('class', 'xrect')
        .attr('visibility', 'hidden');
      svg
        .append('g')
        .append('text')
        .attr('class', 'xcoord')
        .attr('visibility', 'hidden')
        .attr('fill', 'black')
        .attr('x', padding.left)
        .attr('y', padding.top + height + 15)
        .style('font-size', '9px');
      svg
        .append('g')
        .append('rect')
        .attr('width', 24)
        .attr('height', 15)
        .attr('fill', 'white')
        .attr('class', 'steprect')
        .attr('visibility', 'hidden')
        .attr('x', padding.left + width + 7);
      svg
        .append('g')
        .append('text')
        .attr('class', 'ycoord')
        .attr('visibility', 'hidden')
        .attr('fill', 'black')
        .attr('x', padding.left + width + 8)
        .style('font-size', '9px');
      // 画图
      const lineFunction = d3
        .line()
        .x(function _nonName(d) {
          return xscale(d[0]);
        })
        .y(function _nonName(d) {
          return yscale(d[1]);
        });
      pathg
        .selectAll('path')
        .data(data)
        .enter()
        .append('g')
        .append('path')
        .attr('stroke', 'gainsboro')
        .attr('stroke-width', '0.5')
        .attr('d', function _nonName(d) {
          return lineFunction(d);
        })
        .attr('transform', function _nonName(d) {
          const translateHeight = stepscale(d[0][2]) + heightTop;
          return `translate(${  padding.left  },${  translateHeight  })`;
        })
        .attr('fill', this.runColor)
        .attr('class', function _nonName(d, i) {
          return `step${  i}`;
        })
        .attr('id', function _nonName(d, i) {
          return `${label  }step${  i}`;
        })
        // 添加鼠标操作:珠子+数值
        .on('mousemove', function _nonName(d, i) {
          const curX = d3.mouse(svg.node())[0];
          const curY = d3.mouse(svg.node())[1];
          // 计算珠子
          // 遮挡住的珠子怎么显示
          // 找x轴最近的点，然后就找到相应的y
          const curXValue = xscale.invert(curX - padding.left);
          let minDistIndex = 0;
          for (let j = 0; j < d.length; j += 1) {
            if (curXValue < d[j][0]) {
              minDistIndex = j;
              break;
            }
          }
          if ((curXValue - d[minDistIndex - 1]) < (d[minDistIndex] - curXValue)) {
            minDistIndex -= 1;
          }
          const minDistX = padding.left + xscale(d[minDistIndex][0]);
          const points = [];
          for (let j = 0; j < data.length; j += 1) {
            points.push(heightTop + stepscale(data[j][0][2]) + yscale(data[j][minDistIndex][1]));
          }
          svg
            .selectAll('circle')
            .data(points)
            .attr('r', '1.5')
            .attr('cx', minDistX)
            .attr('cy', function no_name(d) {
              return d;
            });
          // 当前选中的直方图边界高亮
          svg.select('.areapathg')
            .selectAll('path')
            .style('opacity', 1.0)
            .attr('stroke', 'gainsboro')
            .attr('stroke-width', '0.5');
          svg.select(`.step${  i}`).attr('stroke', '#fff45a').attr('stroke-width', '1');
          // 控制面板需要显示数据
          // 当前step，是对多少个数据进行统计的，统计个数的最小值和最大值，和最大值对应的区间
          // 这个数据不准确，把所有相加
          const curDataCountSum = d3.sum(data[i], function _nonName(d) {
            return d[1];
          });
          const curCountMin = d3.min(data[i], function _nonName(d) {
            return d[1];
          });
          let curCountMax = 0;
          for (let it = 0; it < data[i].length; it+=1) {
            const pixel = data[i][it][1];
            if (curCountMax < pixel) {
              curCountMax = pixel;
            }
          }
          that.setStatisticInfo([data[i][0][2], Math.ceil(curDataCountSum), curCountMin, curCountMax.toFixed(2)]);
          svg
            .select('.xrect')
            .attr('visibility', 'visible')
            .attr('x', curX - 15);
          svg
            .select('.xcoord')
            .attr('visibility', 'visible')
            .attr('x', curX - 13)
            .text(Math.floor(xscale.invert(curX - padding.left) * 1000) / 1000);

          svg
            .select('.steprect')
            .attr('visibility', 'visible')
            .attr('y', padding.top + stepscale(d[0][2]) - 8)
            .attr('width', d[0][2].toString().length * 7);
          svg
            .select('.ycoord')
            .attr('visibility', 'visible')
            .attr('y', padding.top + stepscale(d[0][2]) + 3)
            .text(d[0][2]);
          svg
            .select('.textbox')
            .attr('visibility', 'visible')
            .text(`count:${  d[minDistIndex][1].toFixed(2)}`)
            .attr('x', curX + 5)
            .attr('y', curY - 10);

          // 透明效果
          // 遍历整个数据比较高度
          const dh = height / (data[data.length - 1][0][2] - 1);
          const count = Math.ceil(areaHeight / dh);
          const curHeight = [];
          // yscale，值越大高度越小
          for (let j = 0; j < d.length; j+=1) {
            curHeight.push(stepscale(d[j][2]) - (areaHeight - yscale(d[j][1])));
          }
          for (let j = i + 1; j <= i + count && j < data.length; j+=1) {
            const onedata = data[j];
            for (let k = 0; k < onedata.length; k+=1) {
              const heightk = stepscale(onedata[k][2]) - (areaHeight - yscale(onedata[k][1]));
              if (heightk <= curHeight[k]) {
                svg.select(`.step${  j}`).style('opacity', 0.5);
                break;
              }
            }
          }
        });
      svg.on('mouseleave', function _nonName() {
        // 用mouseout光标会闪烁
        svg
          .select('.areapathg')
          .selectAll('path')
          .style('opacity', 1.0)
          .attr('stroke', 'gainsboro')
          .attr('stroke-width', '0.5');
        svg.select('.textbox').attr('visibility', 'hidden');
        svg.selectAll('circle').attr('r', '0');
        svg
          .select('.xaxisline')
          .attr('x1', '0')
          .attr('y1', '0')
          .attr('x2', '0')
          .attr('y2', '0');
        svg.select('.xrect').attr('visibility', 'hidden');
        svg.select('.xcoord').attr('visibility', 'hidden');
        svg.select('.steprect').attr('visibility', 'hidden');
        svg.select('.ycoord').attr('visibility', 'hidden');
        // 控制面板为空
        that.setStatisticInfo([]);
      });
      // 在只显示若干条数据时，在纵轴上移动显示中间步骤
      svg.select('.stepaxis').on('mousemove', function _nonName() {
        const curY = d3.mouse(svg.node())[1];
        const step = parseInt(stepscale.invert(curY - padding.top), 0);
        let k = 0;
        for (let i = 0; i < data.length; i+=1) {
          if (data[i][0][2] > step) {
            k = i;
            if (i !== 0 && data[i][0][2] - step > step - data[i - 1][0][2]) {
              k -= 1;
            }
            break;
          }
        }
        svg
          .select('.areapathg')
          .selectAll('path')
          .attr('stroke', 'gainsboro')
          .attr('stroke-width', '0.5');
        d3.select(`#${  label  }step${  k}`)
          .attr('stroke', '#fff45a')
          .attr('visibility', 'visible')
          .attr('stroke-width', '1');
        svg
          .select('.steprect')
          .attr('visibility', 'visible')
          .attr('y', curY - 7);
        svg
          .select('.ycoord')
          .attr('visibility', 'visible')
          .attr('y', curY + 5)
          .text(data[k][0][2]);
      });
    },
  },
};
</script>
