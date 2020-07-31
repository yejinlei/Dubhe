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
  <div :class="className" />
</template>
<script>
import * as d3 from 'd3';
import { createNamespacedHelpers } from 'vuex';

const { mapGetters: mapStatisticGatters } = createNamespacedHelpers('Visual/statistic');

export default {
  name: 'Oneoverlook',
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
      id: `dist${  this.itemp}`,
    };
  },
  computed: {
    ...mapStatisticGatters(['getDataSets', 'getDataSetsState']),
  },
  watch: {
    data() {
      document.getElementsByClassName(this.className)[0].innerHTML = '';
      this.drawDistri();
    },
  },
  mounted() {
    this.drawDistri();
  },
  methods: {
    drawDistri() {
      const label = this.id;
      const {data} = this;
      const className = `.${  this.className}`;

      const svgwidth = 290;
      const svgheight = 250;
      const padding = { top: 10, right: 15, bottom: 25, left: 35 };
      const div = d3
        .select(className)
        .append('div')
        .attr('id', `${label  }div`);
      const svg = div
        .append('svg')
        .attr('width', '100%')
        .attr('height', '100%')
        .attr('preserveAspectRatio', 'xMidYMid meet')
        .attr('viewBox', '0 0 290 250')
        .attr('id', label);
      const width = svgwidth - padding.left - padding.right;
      const height = svgheight - padding.top - padding.bottom;
      // 坐标轴
      const dataLen = data.length;
      const stepMin = data[0][0][0];
      const stepMax = data[0][data[0].length - 1][0];
      let valueMin = 10000;
      let valueMax = -10000;
      for (let j = 0; j < data[0].length; j+=1) {
        const pixel = data[0][j][1];
        if (pixel < valueMin) valueMin = pixel;
      }
      for (let j = 0; j < data[dataLen - 1].length; j+=1) {
        const pixel2 = data[dataLen - 1][j][1];
        if (pixel2 > valueMax) {
          valueMax = pixel2;
        }
      }
      const xscale = d3
        .scaleLinear()
        .domain([stepMin, stepMax])
        .range([0, width])
        .nice();
      const yscale = d3
        .scaleLinear()
        .domain([valueMin, valueMax])
        .range([height, 0])
        .nice();
      svg
        .append('g')
        .attr('class', 'axis')
        .attr(
          'transform',
          `translate(${  padding.left  },${  height + padding.top  })`,
        )
        .call(
          d3
            .axisBottom()
            .scale(xscale)
            .tickSize(-height)
            .tickFormat(d => {
              if (d > 10000) {
                const numLen = d.toString().length - 1;
                // eslint-disable-next-line no-restricted-properties
                return `${d / Math.pow(10, numLen)  }e+${  numLen}`;
              }
              return d;
            }),
        );
      svg
        .append('g')
        .attr('class', 'axis')
        .attr(
          'transform',
          `translate(${  padding.left  },${  padding.top  })`,
        )
        .call(
          d3
            .axisLeft()
            .scale(yscale)
            .tickSize(-width),
        );
      svg.append('g').append('text').text('step').attr('fill', 'grey')
        .attr('x', svgwidth / 2).attr('y', svgheight - padding.bottom / 5).attr('font-size', '10px');
      const pathg = svg.append('g');
      const areaFunction = d3
        .area()
        .x(function _nonName(d) {
          return xscale(d[0]);
        })
        .y0(function _nonName(d) {
          return yscale(d[1]);
        })
        .y1(function _nonName(d) {
          return yscale(d[2]);
        });
      const dopa = (1.0 / dataLen) * 2;
      const middle = Math.ceil(dataLen / 2);
      for (let j = 1; j < dataLen; j+=1) {
        let opacity = dopa * j;
        if (j >= middle) opacity = dopa * (dataLen - j);
        pathg
          .append('path')
          .attr(
            'transform',
            `translate(${  padding.left  },${  padding.top  })`,
          )
          .attr('fill', this.runColor)
          .attr('stroke', this.runColor)
          .attr('stroke-width', '0.5')
          .style('opacity', opacity)
          .attr('d', areaFunction(data[j]));
      }
      // 画最中间的一条线
      const lineFunction = d3
        .line()
        .x(function _nonName(d) {
          return xscale(d[0]);
        })
        .y(function _nonName(d) {
          return yscale(d[1]);
        });
      pathg
        .append('path')
        .attr(
          'transform',
          `translate(${  padding.left  },${  padding.top  })`,
        )
        .attr('fill', 'none')
        .style('stroke', 'white')
        .style('stroke-width', '0.5')
        .attr('d', lineFunction(data[middle - 1]));
    },
  },
};
</script>
