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
  <div :id="classname" class="chart">
    <div :class="['tooltip', scaleLargeSmall ? 'font1' : 'font2']" :scale="scaleLargeSmall">
      <table>
        <tr>
          <td class="td1">wall_time</td>
          <td class="td2">step</td>
          <td class="td1">value</td>
        </tr>
        <tr>
          <td :id="'td1' + classname" class="td1" />
          <td :id="'td2' + classname" class="td2" />
          <td :id="'td3' + classname" class="td1" />
        </tr>
      </table>
    </div>
  </div>
</template>
<script>
import * as d3 from 'd3';
import { createNamespacedHelpers } from 'vuex';
import { unixTimestamp2Normal, scientificNotation } from '@/utils';

const { mapMutations: mapScalarMutations, mapGetters: mapScalarGetters } = createNamespacedHelpers(
  'Visual/scalar'
);
const { mapMutations: mapCustomMutations } = createNamespacedHelpers('Visual/custom');

export default {
  props: {
    chartdata: Object,
    start: Boolean,
    end: Boolean,
    ytext: String,
    scaleLargeSmall: Boolean,
    classname: String,
    isaddmain: Boolean,
    title: String,
  },
  data() {
    return {
      data: [],
      yname: [],
      mergeddata: [],
      mergeddata0: [],
      mergeddata1: [],
      mergetype: '',
      yname0: [],
      yname1: [],
      legendnumber: 0,
      drawnumber: 0,
      thisid: '',
      customcontent: {},
    };
  },
  computed: {
    ...mapScalarGetters([
      'smoothvalue',
      'yaxis',
      'mergeditem',
      'checkedorder',
      'freshnumber',
      'grade',
    ]),
  },
  watch: {
    freshnumber() {
      if (this.mergetype === '') {
        d3.select(`#svg${this.classname}`).remove();
        this.SvgDraw();
      }
    },
    isaddmain(val) {
      if (val) {
        if (this.mergetype === 'single') {
          this.thisid = '';
          for (let i = 0; i < this.mergeddata.length; i += 1) {
            this.thisid = this.thisid + this.mergeddata[i].run + this.mergeddata[i].tag;
          }
          this.customcontent = {
            title: this.title,
            mergetype: this.mergetype,
            legendnumber: this.legendnumber,
            value: this.mergeddata,
          };
          this.setScalar([this.thisid, this.customcontent]);
        } else if (this.mergetype === 'double') {
          this.thisid = '';
          for (let i = 0; i < this.mergeddata0.length; i += 1) {
            this.thisid = this.thisid + this.mergeddata0[i].run + this.mergeddata0[i].tag;
          }
          for (let i = 0; i < this.mergeddata1.length; i += 1) {
            this.thisid = this.thisid + this.mergeddata1[i].run + this.mergeddata1[i].tag;
          }
          this.customcontent = {
            title: this.title,
            mergetype: this.mergetype,
            legendnumber: this.legendnumber,
            value0: this.mergeddata0,
            value1: this.mergeddata1,
          };
          this.setScalar([this.thisid, this.customcontent]);
        }
      } else {
        this.deleteScalar(this.thisid);
      }
    },
    smoothvalue() {
      if (this.grade[this.classname] === 'main') {
        if (this.mergetype === 'single') {
          d3.select(`#svg${this.classname}`).remove();
          this.MergeSvgDraw();
        } else if (this.mergetype === 'double') {
          d3.select(`#svg${this.classname}`).remove();
          this.DYMergeSvgDraw();
        }
      } else {
        d3.select(`#svg${this.classname}`).remove();
        this.SvgDraw();
      }
    },
    yaxis() {
      if (this.grade[this.classname] === 'main') {
        if (this.mergetype === 'single') {
          d3.select(`#svg${this.classname}`).remove();
          this.MergeSvgDraw();
        } else if (this.mergetype === 'double') {
          d3.select(`#svg${this.classname}`).remove();
          this.DYMergeSvgDraw();
        }
      } else {
        d3.select(`#svg${this.classname}`).remove();
        this.SvgDraw();
      }
    },
    start(val) {
      if (val) {
        if (Object.keys(this.mergeditem[this.classname]).length === 1) {
          this.mergetype = 'single';
          this.legendnumber = this.checkedorder.length;
          this.mergeddata = [].concat(
            this.mergeditem[this.classname][Object.keys(this.mergeditem[this.classname])[0]]
          );
          d3.select(`#svg${this.classname}`).remove();
          this.MergeSvgDraw();
        } else if (Object.keys(this.mergeditem[this.classname]).length === 2) {
          this.mergetype = 'double';
          this.legendnumber = this.checkedorder.length;
          this.mergeddata0 = [].concat(
            this.mergeditem[this.classname][Object.keys(this.mergeditem[this.classname])[0]]
          );
          this.mergeddata1 = [].concat(
            this.mergeditem[this.classname][Object.keys(this.mergeditem[this.classname])[1]]
          );
          d3.select(`#svg${this.classname}`).remove();
          const foo0 = Object.keys(this.mergeditem[this.classname])[0];
          this.yname0[0] = foo0;
          this.yname0[1] = `log(${this.yname0[0]})`;
          const foo1 = Object.keys(this.mergeditem[this.classname])[1];
          this.yname1[0] = foo1;
          this.yname1[1] = `log(${this.yname1[0]})`;
          this.DYMergeSvgDraw();
        }
        this.setmergestep();
      }
    },
    end(val) {
      if (val) {
        this.deleteScalar(this.thisid);
        this.mergeddata = [];
        this.mergeddata0 = [];
        this.mergeddata1 = [];
        this.mergetype = '';
        this.yname0 = [];
        this.yname1 = [];
        this.data = this.chartdata.value[Object.keys(this.chartdata.value)[0]];
        this.yname[0] = this.ytext;
        this.yname[1] = `log(${this.ytext})`;
        d3.select(`#svg${this.classname}`).remove();
        this.SvgDraw();
      }
    },
  },
  created() {
    if (this.grade[this.classname] === 'main') {
      if (Object.keys(this.mergeditem[this.classname]).length === 1) {
        this.mergetype = 'single';
        this.mergeddata = [].concat(
          this.mergeditem[this.classname][Object.keys(this.mergeditem[this.classname])[0]]
        );
        this.legendnumber = this.mergeddata.length;
        this.MergeSvgDraw();
      } else if (Object.keys(this.mergeditem[this.classname]).length === 2) {
        this.mergetype = 'double';
        this.mergeddata0 = [].concat(
          this.mergeditem[this.classname][Object.keys(this.mergeditem[this.classname])[0]]
        );
        this.mergeddata1 = [].concat(
          this.mergeditem[this.classname][Object.keys(this.mergeditem[this.classname])[1]]
        );
        this.legendnumber = this.mergeddata0.length + this.mergeddata1.length;
        const foo0 = Object.keys(this.mergeditem[this.classname])[0];
        this.yname0[0] = foo0;
        this.yname0[1] = `log(${this.yname0[0]})`;
        const foo1 = Object.keys(this.mergeditem[this.classname])[1];
        this.yname1[0] = foo1;
        this.yname1[1] = `log(${this.yname1[0]})`;
        this.DYMergeSvgDraw();
      }
    } else if (Object.keys(this.chartdata).length === 2) {
      this.data = this.chartdata.value[Object.keys(this.chartdata.value)[0]];
      this.yname[0] = this.ytext;
      this.yname[1] = `log(${this.ytext})`;
    } else if (Object.keys(this.chartdata).length === 4) {
      this.mergetype = 'single';
      this.legendnumber = this.chartdata.legendnumber;
      this.mergeddata = this.chartdata.value;
    } else if (Object.keys(this.chartdata).length === 5) {
      this.mergetype = 'double';
      this.legendnumber = this.chartdata.legendnumber;
      this.mergeddata0 = this.chartdata.value0;
      this.mergeddata1 = this.chartdata.value1;
      const arr0 = this.mergeddata0[0].tag.split('/');
      const arr1 = this.mergeddata1[0].tag.split('/');
      this.yname0[0] = arr0[arr0.length - 1];
      this.yname0[1] = `log(${this.yname0[0]})`;
      this.yname1[0] = arr1[arr1.length - 1];
      this.yname1[1] = `log(${this.yname1[0]})`;
    }
  },
  mounted() {
    if (this.mergetype === 'double') {
      this.DYMergeSvgDraw();
    } else if (this.mergetype === 'single') {
      this.MergeSvgDraw();
    } else {
      this.SvgDraw();
    }
  },
  methods: {
    ...mapScalarMutations(['setmergestep']),
    ...mapCustomMutations(['setScalar', 'deleteScalar']),
    SvgDraw() {
      let data = [].concat(JSON.parse(JSON.stringify(this.data)));
      const datamid = [].concat(JSON.parse(JSON.stringify(this.data)));
      const smooth = this.smoothvalue * 1;
      let yaxis = this.yname[0];
      if (this.yaxis === 'log-linear') {
        let flag = 0;
        for (let i = 0; i < data.length; i += 1) {
          if (datamid[i].value > 0) {
            datamid[i].value = Math.log(datamid[i].value);
          } else {
            flag = 1;
            break;
          }
        }
        if (flag === 0) {
          const foo = this.yname[1];
          yaxis = foo;
          data = datamid;
        }
      }
      const smoothdata = [].concat(JSON.parse(JSON.stringify(data)));
      let last = smoothdata[0].value;
      for (let i = 1; i < smoothdata.length; i += 1) {
        smoothdata[i].value = last * smooth + (1 - smooth) * smoothdata[i].value;
        last = smoothdata[i].value;
      }
      // set the dimensions and margins of the graph
      const margin = { top: 20, right: 20, bottom: 70, left: 70 };
      const width = 350 - margin.left - margin.right;
      const height = 270 - margin.top - margin.bottom;

      // append the svg object to the body of the page
      const svg = d3
        .select(`#${this.classname}`)
        .append('svg')
        .attr('id', `svg${this.classname}`)
        .attr('width', '100%')
        .attr('height', '100%')
        .attr('preserveAspectRatio', 'xMidYMid meet')
        .attr('viewBox', '0 0 350 270')
        .append('g')
        .attr('transform', `translate(${margin.left},${margin.top})`);

      // add arrow
      svg
        .append('defs')
        .append('marker')
        .attr('id', `xarrowhead${this.classname}`)
        .attr('markerUnits', 'strokeWidth')
        .attr('markerWidth', '15')
        .attr('markerHeight', '15')
        .attr('viewBox', '0 0 12 12')
        .attr('refX', '5')
        .attr('refY', '6')
        .append('path')
        .attr('d', 'M2,2 L10,6 L2,10 L6,6 L2,2');

      svg
        .append('defs')
        .append('marker')
        .attr('id', `yarrowhead${this.classname}`)
        .attr('markerUnits', 'strokeWidth')
        .attr('markerWidth', '15')
        .attr('markerHeight', '15')
        .attr('viewBox', '0 0 12 12')
        .attr('refX', '5')
        .attr('refY', '6')
        .attr('orient', '-90deg')
        .append('path')
        .attr('d', 'M2,2 L10,6 L2,10 L6,6 L2,2');

      // Add X axis
      const xdomain = d3.extent(data, function my1(d) {
        return d.step;
      });
      const xgap = xdomain[1] - xdomain[0];
      // xdomain[0] = xdomain[0] - xgap / 4
      xdomain[1] += xgap / 4;
      const x = d3
        .scaleLinear()
        .domain(xdomain)
        .rangeRound([0, width]);
      const xAxis = svg
        .append('g')
        .attr('transform', `translate(0,${height})`)
        .call(
          d3
            .axisBottom(x)
            .tickSizeOuter(0)
            .ticks(5)
            .tickFormat((d) => {
              return scientificNotation(d, 1);
            })
        );
      xAxis.select('path').attr('marker-end', `url(#xarrowhead${this.classname})`);
      svg
        .append('text')
        .attr('text-anchor', 'middle')
        .attr('x', width / 2)
        .attr('y', height + margin.top + 20)
        .text('step');

      // Add Y axis
      const ydomain = d3.extent(data, function my2(d) {
        return d.value;
      });
      const ygap = ydomain[1] - ydomain[0];
      ydomain[0] -= ygap / 4;
      ydomain[1] += ygap / 4;
      const y = d3
        .scaleLinear()
        .domain(ydomain)
        .rangeRound([height, 0]);
      const yAxis = svg.append('g').call(
        d3
          .axisLeft(y)
          .tickSizeOuter(0)
          .ticks(5)
          .tickFormat((d) => {
            return scientificNotation(d, 1);
          })
      );
      yAxis.select('path').attr('marker-end', `url(#yarrowhead${this.classname})`);
      svg
        .append('text')
        .attr('text-anchor', 'middle')
        .attr('transform', 'rotate(-90)')
        .attr('y', -margin.left + 20)
        .attr('x', -height / 2)
        .text(yaxis);

      // Add a clipPath: everything out of this area won't be drawn.
      svg
        .append('defs')
        .append('svg:clipPath')
        .attr('id', `clip${this.classname}`)
        .append('svg:rect')
        .attr('width', width)
        .attr('height', height)
        .attr('x', 0)
        .attr('y', 0);

      // Add brushing
      const brush = d3.brush().extent([
        [0, 0],
        [width, height],
      ]);

      // Create the line variable: where both the line and the brush take place
      const line = svg.append('g').attr('clip-path', `url(#clip${this.classname})`);
      // Add the original line
      line
        .append('path')
        .datum(data)
        .attr('class', 'originalline')
        .attr('fill', 'none')
        .attr('stroke', 'blue')
        .attr('stroke-width', 1.5)
        .attr('stroke-opacity', 0.2)
        .attr(
          'd',
          d3
            .line()
            .x(function my3(d) {
              return x(d.step);
            })
            .y(function my4(d) {
              return y(d.value);
            })
        );
      // Add the smooth line
      line
        .append('path')
        .datum(smoothdata)
        .attr('class', 'smoothline')
        .attr('fill', 'none')
        .attr('stroke', 'blue')
        .attr('stroke-width', 1.5)
        .attr(
          'd',
          d3
            .line()
            .x(function my5(d) {
              return x(d.step);
            })
            .y(function my6(d) {
              return y(d.value);
            })
        );
      // Add the brushing
      line
        .append('g')
        .attr('class', 'brush')
        .call(brush);
      // create a tooltip
      const Tooltip = d3.select(`#${this.classname}`).select('.tooltip');
      const td1 = d3.select(`#td1${this.classname}`);
      const td2 = d3.select(`#td2${this.classname}`);
      const td3 = d3.select(`#td3${this.classname}`);

      line
        .selectAll('.myCircle')
        .data(smoothdata)
        .enter()
        .append('circle')
        .attr('class', 'myCircle')
        .attr('cx', function my7(d) {
          return x(d.step);
        })
        .attr('cy', function my8(d) {
          return y(d.value);
        })
        .attr('r', 3)
        .attr('stroke-width', 10)
        .attr('stroke', 'black')
        .attr('fill', 'black')
        .attr('fill-opacity', 0)
        .attr('stroke-opacity', 0)
        .on('mouseover', function my9() {
          d3.select(this).attr('fill-opacity', 1);
          Tooltip.style('visibility', 'visible');
        })
        .on('mousemove', function my10(d) {
          const walltime = unixTimestamp2Normal(d.wall_time);
          const vv = scientificNotation(d.value, 7);
          td1.html(walltime);
          td2.html(d.step);
          td3.html(vv);
        })
        .on('mouseout', function my11() {
          d3.select(this).attr('fill-opacity', 0);
          Tooltip.style('visibility', 'hidden');
        });
      // A function that set idleTimeOut to null
      let idleTimeout;
      function idled() {
        idleTimeout = null;
      }

      // A function that update the chart for given boundaries
      function updateChart() {
        const extent = d3.event.selection;
        if (!extent) {
          if (!idleTimeout) {
            idleTimeout = setTimeout(idled, 350);
            return idleTimeout;
          }
          x.domain(xdomain);
          y.domain(ydomain);
        } else {
          x.domain([x.invert(extent[0][0]), x.invert(extent[1][0])]);
          y.domain([y.invert(extent[1][1]), y.invert(extent[0][1])]);
          line.select('.brush').call(brush.move, null);
        }
        xAxis
          .transition()
          .duration(1000)
          .call(
            d3
              .axisBottom(x)
              .tickSizeOuter(0)
              .ticks(5)
              .tickFormat((d) => {
                return scientificNotation(d, 1);
              })
          );
        yAxis
          .transition()
          .duration(1000)
          .call(
            d3
              .axisLeft(y)
              .tickSizeOuter(0)
              .ticks(5)
              .tickFormat((d) => {
                return scientificNotation(d, 1);
              })
          );
        line
          .select('.smoothline')
          .transition()
          .duration(1000)
          .attr(
            'd',
            d3
              .line()
              .x(function my81(d) {
                return x(d.step);
              })
              .y(function my82(d) {
                return y(d.value);
              })
          );
        line
          .select('.originalline')
          .transition()
          .duration(1000)
          .attr(
            'd',
            d3
              .line()
              .x(function my83(d) {
                return x(d.step);
              })
              .y(function my84(d) {
                return y(d.value);
              })
          );
        line
          .selectAll('.myCircle')
          .transition()
          .duration(1000)
          .attr('cx', function my85(d) {
            return x(d.step);
          })
          .attr('cy', function my86(d) {
            return y(d.value);
          });
      }
      brush.on('end', updateChart);
      // If user double click, reinitialize the chart
      svg.on('dblclick', function my12() {
        x.domain(xdomain);
        xAxis.transition().call(
          d3
            .axisBottom(x)
            .tickSizeOuter(0)
            .ticks(5)
            .tickFormat((d) => {
              return scientificNotation(d, 1);
            })
        );
        y.domain(ydomain);
        yAxis.transition().call(
          d3
            .axisLeft(y)
            .tickSizeOuter(0)
            .ticks(5)
            .tickFormat((d) => {
              return scientificNotation(d, 1);
            })
        );
        line
          .selectAll('.smoothline')
          .transition()
          .duration(1000)
          .attr(
            'd',
            d3
              .line()
              .x(function my13(d) {
                return x(d.step);
              })
              .y(function my14(d) {
                return y(d.value);
              })
          );
        line
          .selectAll('.originalline')
          .transition()
          .duration(1000)
          .attr(
            'd',
            d3
              .line()
              .x(function my15(d) {
                return x(d.step);
              })
              .y(function my16(d) {
                return y(d.value);
              })
          );
        line
          .selectAll('.myCircle')
          .transition()
          .duration(1000)
          .attr('cx', function my17(d) {
            return x(d.step);
          })
          .attr('cy', function my18(d) {
            return y(d.value);
          });
      });
    },
    MergeSvgDraw() {
      const { legendnumber } = this;
      let data = [].concat(JSON.parse(JSON.stringify(this.mergeddata)));
      const datamid = [].concat(JSON.parse(JSON.stringify(this.mergeddata)));
      const smooth = this.smoothvalue * 1;
      let yaxis = this.yname[0];
      if (this.yaxis === 'log-linear') {
        let flag = 0;
        for (let i = 0; i < data.length; i += 1) {
          for (let j = 0; j < data[i].value.length; j += 1) {
            if (datamid[i].value[j].value > 0) {
              datamid[i].value[j].value = Math.log(datamid[i].value[j].value);
            } else {
              flag = 1;
              break;
            }
          }
          if (flag === 1) break;
        }
        if (flag === 0) {
          const foo = this.yname[1];
          yaxis = foo;
          data = datamid;
        }
      }
      const smoothdata = [].concat(JSON.parse(JSON.stringify(data)));
      for (let i = 0; i < smoothdata.length; i += 1) {
        let last = smoothdata[i].value[0].value;
        for (let j = 1; j < smoothdata[i].value.length; j += 1) {
          smoothdata[i].value[j].value =
            last * smooth + (1 - smooth) * smoothdata[i].value[j].value;
          last = smoothdata[i].value[j].value;
        }
      }
      let dataset0 = [];
      let dataset = [];
      for (let i = 0; i < data.length; i += 1) {
        data[i].order = i;
        smoothdata[i].order = i;
        dataset0 = dataset0.concat(data[i].value);
        dataset = dataset.concat(smoothdata[i].value);
      }
      // color palette
      const res = data.map(function my19(d) {
        return d.order;
      }); // list of group names
      const color = d3
        .scaleOrdinal()
        .domain(res)
        .range(['#ed357b', '#1d276e', '#6ec6d0', '#0c9257', '#ffdf1e', '#fe8325']);
      // set the dimensions and margins of the graph
      const margin = { top: 20, right: 20, bottom: 50 + 20 * legendnumber, left: 70 };
      const width = 350 - margin.left - margin.right;
      const height = 250 + 20 * legendnumber - margin.top - margin.bottom;

      // append the svg object to the body of the page
      const svg = d3
        .select(`#${this.classname}`)
        .append('svg')
        .attr('id', `svg${this.classname}`)
        .attr('width', '100%')
        .attr('height', '100%')
        .attr('preserveAspectRatio', 'xMidYMid meet')
        .attr('viewBox', `0 0 350 ${(250 + 20 * legendnumber).toString()}`)
        .append('g')
        .attr('transform', `translate(${margin.left},${margin.top})`);

      // add arrow
      svg
        .append('defs')
        .append('marker')
        .attr('id', `xarrowhead${this.classname}`)
        .attr('markerUnits', 'strokeWidth')
        .attr('markerWidth', '15')
        .attr('markerHeight', '15')
        .attr('viewBox', '0 0 12 12')
        .attr('refX', '5')
        .attr('refY', '6')
        .append('path')
        .attr('d', 'M2,2 L10,6 L2,10 L6,6 L2,2');

      svg
        .append('defs')
        .append('marker')
        .attr('id', `yarrowhead${this.classname}`)
        .attr('markerUnits', 'strokeWidth')
        .attr('markerWidth', '15')
        .attr('markerHeight', '15')
        .attr('viewBox', '0 0 12 12')
        .attr('refX', '5')
        .attr('refY', '6')
        .attr('orient', '-90deg')
        .append('path')
        .attr('d', 'M2,2 L10,6 L2,10 L6,6 L2,2');

      // Add X axis
      const xdomain = d3.extent(dataset0, function my20(d) {
        return d.step;
      });
      const xgap = xdomain[1] - xdomain[0];
      xdomain[0] -= xgap / 4;
      xdomain[1] += xgap / 4;
      const x = d3
        .scaleLinear()
        .domain(xdomain)
        .rangeRound([0, width]);
      const xAxis = svg
        .append('g')
        .attr('transform', `translate(0,${height})`)
        .call(
          d3
            .axisBottom(x)
            .tickSizeOuter(0)
            .ticks(5)
            .tickFormat((d) => {
              return scientificNotation(d, 1);
            })
        );
      xAxis.select('path').attr('marker-end', `url(#xarrowhead${this.classname})`);
      svg
        .append('text')
        .attr('text-anchor', 'middle')
        .attr('x', width / 2)
        .attr('y', height + margin.top + 20)
        .text('step');

      // Add Y axis
      const ydomain = d3.extent(dataset0, function my21(d) {
        return d.value;
      });
      const ygap = ydomain[1] - ydomain[0];
      ydomain[0] -= ygap / 4;
      ydomain[1] += ygap / 4;
      const y = d3
        .scaleLinear()
        .domain(ydomain)
        .rangeRound([height, 0]);
      const yAxis = svg.append('g').call(
        d3
          .axisLeft(y)
          .tickSizeOuter(0)
          .ticks(5)
          .tickFormat((d) => {
            return scientificNotation(d, 1);
          })
      );
      yAxis.select('path').attr('marker-end', `url(#yarrowhead${this.classname})`);
      svg
        .append('text')
        .attr('text-anchor', 'middle')
        .attr('transform', 'rotate(-90)')
        .attr('y', -margin.left + 20)
        .attr('x', -height / 2)
        .text(yaxis);

      // Add a clipPath: everything out of this area won't be drawn.
      svg
        .append('defs')
        .append('svg:clipPath')
        .attr('id', `clip${this.classname}`)
        .append('svg:rect')
        .attr('width', width)
        .attr('height', height)
        .attr('x', 0)
        .attr('y', 0);

      // Add brushing
      const brush = d3.brush().extent([
        [0, 0],
        [width, height],
      ]);

      const line = svg.append('g').attr('clip-path', `url(#clip${this.classname})`);
      // Add the smooth line
      line
        .selectAll('.smoothline')
        .data(smoothdata)
        .enter()
        .append('path')
        .attr('class', 'smoothline')
        .attr('fill', 'none')
        .attr('stroke', function my22(d) {
          return color(d.order);
        })
        .attr('stroke-width', 1.5)
        .attr('d', function my23(d) {
          return d3
            .line()
            .x(function my24(d) {
              return x(d.step);
            })
            .y(function my25(d) {
              return y(d.value);
            })(d.value);
        });
      // Add the brushing
      line
        .append('g')
        .attr('class', 'brush')
        .call(brush);
      // create a tooltip

      const Tooltip = d3.select(`#${this.classname}`).select('.tooltip');
      const td1 = d3.select(`#td1${this.classname}`);
      const td2 = d3.select(`#td2${this.classname}`);
      const td3 = d3.select(`#td3${this.classname}`);

      line
        .selectAll('.myCircle')
        .data(dataset)
        .enter()
        .append('circle')
        .attr('class', 'myCircle')
        .attr('cx', function my26(d) {
          return x(d.step);
        })
        .attr('cy', function my27(d) {
          return y(d.value);
        })
        .attr('r', 3)
        .attr('stroke', 'black')
        .attr('stroke-width', 10)
        .attr('fill', 'black')
        .attr('fill-opacity', 0)
        .attr('stroke-opacity', 0)
        .on('mouseover', function my28() {
          d3.select(this).attr('fill-opacity', 1);
          Tooltip.style('visibility', 'visible');
        })
        .on('mousemove', function my29(d) {
          const walltime = unixTimestamp2Normal(d.wall_time);
          const vv = scientificNotation(d.value, 7);
          td1.html(walltime);
          td2.html(d.step);
          td3.html(vv);
        })
        .on('mouseout', function my30() {
          d3.select(this).attr('fill-opacity', 0);
          Tooltip.style('visibility', 'hidden');
        });

      // add the legend
      const legend = svg
        .selectAll('.legend')
        .data(smoothdata)
        .enter()
        .append('g')
        .attr('class', 'legend')
        .attr('transform', function my31(d, i) {
          return `translate(0,${i * 20})`;
        });

      legend
        .append('rect')
        .attr('x', -40)
        .attr('y', height + margin.top + 40)
        .attr('width', 18)
        .attr('height', 4)
        .style('fill', function my32(d) {
          return color(d.order);
        });

      legend
        .append('text')
        .attr('x', -16)
        .attr('y', height + margin.top + 40)
        .attr('dy', '.5em')
        .attr('font-size', '10px')
        .style('text-anchor', 'start')
        .text(function my33(d) {
          return `${d.run},${d.tag}`;
        });
      // A function that set idleTimeOut to null
      let idleTimeout;
      function idled() {
        idleTimeout = null;
      }

      // A function that update the chart for given boundaries
      function updateChart() {
        const extent = d3.event.selection;
        if (!extent) {
          if (!idleTimeout) {
            idleTimeout = setTimeout(idled, 350);
            return idleTimeout;
          }
          x.domain(xdomain);
          y.domain(xdomain);
        } else {
          x.domain([x.invert(extent[0][0]), x.invert(extent[1][0])]);
          y.domain([y.invert(extent[1][1]), y.invert(extent[0][1])]);
          line.select('.brush').call(brush.move, null);
        }
        xAxis
          .transition()
          .duration(1000)
          .call(
            d3
              .axisBottom(x)
              .tickSizeOuter(0)
              .ticks(5)
              .tickFormat((d) => {
                return scientificNotation(d, 1);
              })
          );
        yAxis
          .transition()
          .duration(1000)
          .call(
            d3
              .axisLeft(y)
              .tickSizeOuter(0)
              .ticks(5)
              .tickFormat((d) => {
                return scientificNotation(d, 1);
              })
          );
        line
          .selectAll('.smoothline')
          .transition()
          .duration(1000)
          .attr('d', function my87(d) {
            return d3
              .line()
              .x(function my88(d) {
                return x(d.step);
              })
              .y(function my89(d) {
                return y(d.value);
              })(d.value);
          });
        line
          .selectAll('.myCircle')
          .transition()
          .duration(1000)
          .attr('cx', function my90(d) {
            return x(d.step);
          })
          .attr('cy', function my91(d) {
            return y(d.value);
          });
      }
      brush.on('end', updateChart);
      // If user double click, reinitialize the chart
      svg.on('dblclick', function my34() {
        x.domain(xdomain);
        xAxis.transition().call(
          d3
            .axisBottom(x)
            .tickSizeOuter(0)
            .ticks(5)
            .tickFormat((d) => {
              return scientificNotation(d, 1);
            })
        );
        y.domain(ydomain);
        yAxis.transition().call(
          d3
            .axisLeft(y)
            .tickSizeOuter(0)
            .ticks(5)
            .tickFormat((d) => {
              return scientificNotation(d, 1);
            })
        );
        line
          .selectAll('.smoothline')
          .transition()
          .duration(1000)
          .attr('d', function my35(d) {
            return d3
              .line()
              .x(function my36(d) {
                return x(d.step);
              })
              .y(function my37(d) {
                return y(d.value);
              })(d.value);
          });
        line
          .selectAll('.myCircle')
          .transition()
          .duration(1000)
          .attr('cx', function my38(d) {
            return x(d.step);
          })
          .attr('cy', function my39(d) {
            return y(d.value);
          });
      });
    },
    DYMergeSvgDraw() {
      const { legendnumber } = this;
      let data0 = [].concat(JSON.parse(JSON.stringify(this.mergeddata0)));
      let data1 = [].concat(JSON.parse(JSON.stringify(this.mergeddata1)));
      const datamid0 = [].concat(JSON.parse(JSON.stringify(this.mergeddata0)));
      const datamid1 = [].concat(JSON.parse(JSON.stringify(this.mergeddata1)));
      const smooth = this.smoothvalue * 1;
      let yaxis0 = this.yname0[0];
      let yaxis1 = this.yname1[0];
      if (this.yaxis === 'log-linear') {
        let flag = 0;
        for (let i = 0; i < data0.length; i += 1) {
          if (flag === 1) break;
          for (let j = 0; j < data0[i].value.length; j += 1) {
            if (datamid0[i].value[j].value > 0) {
              datamid0[i].value[j].value = Math.log(datamid0[i].value[j].value);
            } else {
              flag = 1;
              break;
            }
          }
        }
        for (let i = 0; i < data1.length; i += 1) {
          if (flag === 1) break;
          for (let j = 0; j < data1[i].value.length; j += 1) {
            if (datamid1[i].value[j].value > 0) {
              datamid1[i].value[j].value = Math.log(datamid1[i].value[j].value);
            } else {
              flag = 1;
              break;
            }
          }
        }
        if (flag === 0) {
          const foo0 = this.yname0[1];
          const foo1 = this.yname1[1];
          yaxis0 = foo0;
          yaxis1 = foo1;
          data0 = datamid0;
          data1 = datamid1;
        }
      }
      const smoothdata0 = [].concat(JSON.parse(JSON.stringify(data0)));
      const smoothdata1 = [].concat(JSON.parse(JSON.stringify(data1)));
      for (let i = 0; i < smoothdata0.length; i += 1) {
        let last = smoothdata0[i].value[0].value;
        for (let j = 1; j < smoothdata0[i].value.length; j += 1) {
          smoothdata0[i].value[j].value =
            last * smooth + (1 - smooth) * smoothdata0[i].value[j].value;
          last = smoothdata0[i].value[j].value;
        }
      }
      for (let i = 0; i < smoothdata1.length; i += 1) {
        let last = smoothdata1[i].value[0].value;
        for (let j = 1; j < smoothdata1[i].value.length; j += 1) {
          smoothdata1[i].value[j].value =
            last * smooth + (1 - smooth) * smoothdata1[i].value[j].value;
          last = smoothdata1[i].value[j].value;
        }
      }
      let dataset00 = [];
      let dataset11 = [];
      let dataset0 = [];
      let dataset1 = [];
      for (let i = 0; i < data0.length; i += 1) {
        data0[i].order = i;
        smoothdata0[i].order = i;
        dataset00 = dataset00.concat(data0[i].value);
        dataset0 = dataset0.concat(smoothdata0[i].value);
      }
      for (let i = 0; i < data1.length; i += 1) {
        data1[i].order = i;
        smoothdata1[i].order = i;
        dataset11 = dataset11.concat(data1[i].value);
        dataset1 = dataset1.concat(smoothdata1[i].value);
      }
      // color palette
      const res0 = data0.map(function my40(d) {
        return d.order;
      }); // list of group names
      const color0 = d3
        .scaleOrdinal()
        .domain(res0)
        .range(['#ed357b', '#1d276e', '#6ec6d0', '#0c9257', '#ffdf1e', '#fe8325']);
      const res1 = data1.map(function my41(d) {
        return d.order;
      }); // list of group names
      const color1 = d3
        .scaleOrdinal()
        .domain(res1)
        .range(['#fe8325', '#ffdf1e', '#0c9257', '#6ec6d0', '#1d276e', '#ed357b']);

      // set the dimensions and margins of the graph
      const margin = { top: 20, right: 70, bottom: 50 + 20 * legendnumber, left: 70 };
      const width = 350 - margin.left - margin.right;
      const height = 250 + 20 * legendnumber - margin.top - margin.bottom;

      // append the svg object to the body of the page
      const svg = d3
        .select(`#${this.classname}`)
        .append('svg')
        .attr('id', `svg${this.classname}`)
        .attr('width', '100%')
        .attr('height', '100%')
        .attr('preserveAspectRatio', 'xMidYMid meet')
        .attr('viewBox', `0 0 350 ${(250 + 20 * legendnumber).toString()}`)
        .append('g')
        .attr('transform', `translate(${margin.left},${margin.top})`);

      // add arrow
      svg
        .append('defs')
        .append('marker')
        .attr('id', `yarrowhead${this.classname}`)
        .attr('markerUnits', 'strokeWidth')
        .attr('markerWidth', '15')
        .attr('markerHeight', '15')
        .attr('viewBox', '0 0 12 12')
        .attr('refX', '5')
        .attr('refY', '6')
        .attr('orient', '-90deg')
        .append('path')
        .attr('d', 'M2,2 L10,6 L2,10 L6,6 L2,2');

      // Add X axis
      const xdomain0 = d3.extent(dataset00, function my42(d) {
        return d.step;
      });
      const xdomain1 = d3.extent(dataset11, function my43(d) {
        return d.step;
      });
      const xdomain = [];
      xdomain[0] = Math.min(xdomain0[0], xdomain1[0]);
      xdomain[1] = Math.max(xdomain0[1], xdomain1[1]);
      const xgap = xdomain[1] - xdomain[0];
      xdomain[0] -= xgap / 4;
      xdomain[1] += xgap / 4;
      const x = d3
        .scaleLinear()
        .domain(xdomain)
        .rangeRound([0, width]);
      const xAxis = svg
        .append('g')
        .attr('transform', `translate(0,${height})`)
        .call(
          d3
            .axisBottom(x)
            .tickSizeOuter(0)
            .ticks(5)
            .tickFormat((d) => {
              return scientificNotation(d, 1);
            })
        );
      svg
        .append('text')
        .attr('text-anchor', 'middle')
        .attr('x', width / 2)
        .attr('y', height + margin.top + 20)
        .text('step');

      // Add Y0 axis
      const ydomain0 = d3.extent(dataset00, function my44(d) {
        return d.value;
      });
      const ygap0 = ydomain0[1] - ydomain0[0];
      ydomain0[0] -= ygap0 / 4;
      ydomain0[1] += ygap0 / 4;
      const y0 = d3
        .scaleLinear()
        .domain(ydomain0)
        .rangeRound([height, 0]);
      const yAxis0 = svg.append('g').call(
        d3
          .axisLeft(y0)
          .tickSizeOuter(0)
          .ticks(5)
          .tickFormat((d) => {
            return scientificNotation(d, 1);
          })
      );
      yAxis0.select('path').attr('marker-end', `url(#yarrowhead${this.classname})`);
      svg
        .append('text')
        .attr('text-anchor', 'middle')
        .attr('transform', 'rotate(-90)')
        .attr('y', -margin.left + 20)
        .attr('x', -height / 2)
        .text(yaxis0);

      // Add Y1 axis
      const ydomain1 = d3.extent(dataset11, function my45(d) {
        return d.value;
      });
      const ygap1 = ydomain1[1] - ydomain1[0];
      ydomain1[0] -= ygap1 / 4;
      ydomain1[1] += ygap1 / 4;
      const y1 = d3
        .scaleLinear()
        .domain(ydomain1)
        .rangeRound([height, 0]);
      const yAxis1 = svg
        .append('g')
        .attr('transform', `translate(${width},0)`)
        .call(
          d3
            .axisRight(y1)
            .tickSizeOuter(0)
            .ticks(5)
            .tickFormat((d) => {
              return scientificNotation(d, 1);
            })
        );
      yAxis1.select('path').attr('marker-end', `url(#yarrowhead${this.classname})`);
      svg
        .append('text')
        .attr('text-anchor', 'middle')
        .attr('transform', 'rotate(-90)')
        .attr('y', width + margin.left - 10)
        .attr('x', -height / 2)
        .text(yaxis1);
      // Add a clipPath: everything out of this area won't be drawn.
      svg
        .append('defs')
        .append('svg:clipPath')
        .attr('id', `clip${this.classname}`)
        .append('svg:rect')
        .attr('width', width)
        .attr('height', height)
        .attr('x', 0)
        .attr('y', 0);

      // Add brushing
      const brush = d3.brush().extent([
        [0, 0],
        [width, height],
      ]);

      // Create the line variable: where both the line and the brush take place
      const line = svg.append('g').attr('clip-path', `url(#clip${this.classname})`);

      // Add the smooth line
      line
        .selectAll('.smoothline0')
        .data(smoothdata0)
        .enter()
        .append('path')
        .attr('class', 'smoothline0')
        .attr('fill', 'none')
        .attr('stroke', function my46(d) {
          return color0(d.order);
        })
        .attr('stroke-width', 1.5)
        .attr('d', function my47(d) {
          return d3
            .line()
            .x(function my48(d) {
              return x(d.step);
            })
            .y(function my49(d) {
              return y0(d.value);
            })(d.value);
        });
      line
        .selectAll('.smoothline1')
        .data(smoothdata1)
        .enter()
        .append('path')
        .attr('class', 'smoothline1')
        .attr('fill', 'none')
        .attr('stroke', function my50(d) {
          return color1(d.order);
        })
        .attr('stroke-width', 1.5)
        .attr('d', function my51(d) {
          return d3
            .line()
            .x(function my52(d) {
              return x(d.step);
            })
            .y(function my53(d) {
              return y1(d.value);
            })(d.value);
        });
      // Add the brushing
      line
        .append('g')
        .attr('class', 'brush')
        .call(brush);
      // create a tooltip

      const Tooltip = d3.select(`#${this.classname}`).select('.tooltip');
      const td1 = d3.select(`#td1${this.classname}`);
      const td2 = d3.select(`#td2${this.classname}`);
      const td3 = d3.select(`#td3${this.classname}`);

      line
        .selectAll('.myCircle0')
        .data(dataset0)
        .enter()
        .append('circle')
        .attr('class', 'myCircle0')
        .attr('cx', function my54(d) {
          return x(d.step);
        })
        .attr('cy', function my55(d) {
          return y0(d.value);
        })
        .attr('r', 3)
        .attr('stroke', 'black')
        .attr('stroke-width', 10)
        .attr('fill', 'black')
        .attr('fill-opacity', 0)
        .attr('stroke-opacity', 0)
        .on('mouseover', function my56() {
          d3.select(this).attr('fill-opacity', 1);
          Tooltip.style('visibility', 'visible');
        })
        .on('mousemove', function my57(d) {
          const walltime = unixTimestamp2Normal(d.wall_time);
          const vv = scientificNotation(d.value, 7);
          td1.html(walltime);
          td2.html(d.step);
          td3.html(vv);
        })
        .on('mouseout', function my58() {
          d3.select(this).attr('fill-opacity', 0);
          Tooltip.style('visibility', 'hidden');
        });
      line
        .selectAll('.myCircle1')
        .data(dataset1)
        .enter()
        .append('circle')
        .attr('class', 'myCircle1')
        .attr('cx', function my59(d) {
          return x(d.step);
        })
        .attr('cy', function my60(d) {
          return y1(d.value);
        })
        .attr('r', 3)
        .attr('stroke', 'black')
        .attr('stroke-width', 10)
        .attr('fill', 'black')
        .attr('fill-opacity', 0)
        .attr('stroke-opacity', 0)
        .on('mouseover', function my61() {
          d3.select(this).attr('fill-opacity', 1);
          Tooltip.style('visibility', 'visible');
        })
        .on('mousemove', function my62(d) {
          const walltime = unixTimestamp2Normal(d.wall_time);
          const vv = scientificNotation(d.value, 7);
          td1.html(walltime);
          td2.html(d.step);
          td3.html(vv);
        })
        .on('mouseout', function my63() {
          d3.select(this).attr('fill-opacity', 0);
          Tooltip.style('visibility', 'hidden');
        });

      const firstdatanumber = smoothdata0.length;
      // add the legend0
      const legend0 = svg
        .selectAll('.legend0')
        .data(smoothdata0)
        .enter()
        .append('g')
        .attr('class', 'legend0')
        .attr('transform', function my64(d, i) {
          return `translate(0,${i * 20})`;
        });

      legend0
        .append('rect')
        .attr('x', -40)
        .attr('y', height + margin.top + 40)
        .attr('width', 18)
        .attr('height', 4)
        .style('fill', function my65(d) {
          return color0(d.order);
        });

      legend0
        .append('text')
        .attr('x', -16)
        .attr('y', height + margin.top + 40)
        .attr('dy', '.5em')
        .attr('font-size', '10px')
        .style('text-anchor', 'start')
        .text(function my66(d) {
          return `${d.run},${d.tag}`;
        });
      // add the legend0
      const legend1 = svg
        .selectAll('.legend1')
        .data(smoothdata1)
        .enter()
        .append('g')
        .attr('class', 'legend1')
        .attr('transform', function my67(d, i) {
          return `translate(0,${(firstdatanumber + i) * 20})`;
        });

      legend1
        .append('rect')
        .attr('x', -40)
        .attr('y', height + margin.top + 40)
        .attr('width', 18)
        .attr('height', 4)
        .style('fill', function my68(d) {
          return color1(d.order);
        });

      legend1
        .append('text')
        .attr('x', -16)
        .attr('y', height + margin.top + 40)
        .attr('dy', '.5em')
        .attr('font-size', '10px')
        .style('text-anchor', 'start')
        .text(function my69(d) {
          return `${d.run},${d.tag}`;
        });
      // A function that set idleTimeOut to null
      let idleTimeout;
      function idled() {
        idleTimeout = null;
      }

      // A function that update the chart for given boundaries
      function updateChart() {
        const extent = d3.event.selection;
        if (!extent) {
          if (!idleTimeout) {
            idleTimeout = setTimeout(idled, 350);
            return idleTimeout;
          }
          x.domain(xdomain);
          y0.domain(ydomain0);
          y1.domain(ydomain1);
        } else {
          x.domain([x.invert(extent[0][0]), x.invert(extent[1][0])]);
          y0.domain([y0.invert(extent[1][1]), y0.invert(extent[0][1])]);
          y1.domain([y1.invert(extent[1][1]), y1.invert(extent[0][1])]);
          line.select('.brush').call(brush.move, null);
        }
        xAxis
          .transition()
          .duration(1000)
          .call(
            d3
              .axisBottom(x)
              .tickSizeOuter(0)
              .ticks(5)
              .tickFormat((d) => {
                return scientificNotation(d, 1);
              })
          );
        yAxis0
          .transition()
          .duration(1000)
          .call(
            d3
              .axisLeft(y0)
              .tickSizeOuter(0)
              .ticks(5)
              .tickFormat((d) => {
                return scientificNotation(d, 1);
              })
          );
        yAxis1
          .transition()
          .duration(1000)
          .call(
            d3
              .axisRight(y1)
              .tickSizeOuter(0)
              .ticks(5)
              .tickFormat((d) => {
                return scientificNotation(d, 1);
              })
          );
        line
          .selectAll('.smoothline0')
          .transition()
          .duration(1000)
          .attr('d', function my92(d) {
            return d3
              .line()
              .x(function my93(d) {
                return x(d.step);
              })
              .y(function my94(d) {
                return y0(d.value);
              })(d.value);
          });
        line
          .selectAll('.smoothline1')
          .transition()
          .duration(1000)
          .attr('d', function my95(d) {
            return d3
              .line()
              .x(function my96(d) {
                return x(d.step);
              })
              .y(function my97(d) {
                return y1(d.value);
              })(d.value);
          });
        line
          .selectAll('.myCircle0')
          .transition()
          .duration(1000)
          .attr('cx', function my98(d) {
            return x(d.step);
          })
          .attr('cy', function my99(d) {
            return y0(d.value);
          });
        line
          .selectAll('.myCircle1')
          .transition()
          .duration(1000)
          .attr('cx', function my100(d) {
            return x(d.step);
          })
          .attr('cy', function my101(d) {
            return y1(d.value);
          });
      }
      brush.on('end', updateChart);
      // If user double click, reinitialize the chart
      svg.on('dblclick', function my70() {
        x.domain(xdomain);
        xAxis.transition().call(
          d3
            .axisBottom(x)
            .tickSizeOuter(0)
            .ticks(5)
            .tickFormat((d) => {
              return scientificNotation(d, 1);
            })
        );
        y0.domain(ydomain0);
        yAxis0.transition().call(
          d3
            .axisLeft(y0)
            .tickSizeOuter(0)
            .ticks(5)
            .tickFormat((d) => {
              return scientificNotation(d, 1);
            })
        );
        y1.domain(ydomain1);
        yAxis1.transition().call(
          d3
            .axisRight(y1)
            .tickSizeOuter(0)
            .ticks(5)
            .tickFormat((d) => {
              return scientificNotation(d, 1);
            })
        );
        line
          .selectAll('.smoothline0')
          .transition()
          .duration(1000)
          .attr('d', function my71(d) {
            return d3
              .line()
              .x(function my72(d) {
                return x(d.step);
              })
              .y(function my73(d) {
                return y0(d.value);
              })(d.value);
          });
        line
          .selectAll('.myCircle0')
          .transition()
          .duration(1000)
          .attr('cx', function my74(d) {
            return x(d.step);
          })
          .attr('cy', function my75(d) {
            return y0(d.value);
          });
        line
          .selectAll('.smoothline1')
          .transition()
          .duration(1000)
          .attr('d', function my76(d) {
            return d3
              .line()
              .x(function my77(d) {
                return x(d.step);
              })
              .y(function my78(d) {
                return y1(d.value);
              })(d.value);
          });
        line
          .selectAll('.myCircle1')
          .transition()
          .duration(1000)
          .attr('cx', function my79(d) {
            return x(d.step);
          })
          .attr('cy', function my80(d) {
            return y1(d.value);
          });
      });
    },
  },
};
</script>

<style lang="less" scoped>
.chart {
  position: relative;
  width: 100%;
  height: 100%;
  background-color: white;
}

.tooltip {
  position: absolute;
  bottom: 5%;
  width: 90%;
  padding: 5px;
  margin-right: 5%;
  margin-left: 5%;
  color: white;
  visibility: hidden;
  background-color: rgba(0, 73, 134);
  border-radius: 5px;
}

.font1 {
  font-size: 30px;
}

.font2 {
  font-size: 10px;
}

table {
  width: 100%;
}

td {
  text-align: center;
}

.td1 {
  width: 40%;
}

.td2 {
  width: 20%;
}
</style>
