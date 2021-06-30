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

import http from '@/utils/VisualUtils/request';
import port from '@/utils/VisualUtils/api';

const state = {
  initshowrun: {},
  categoryInfo: '', // 存放自己的类目信息
  detailData: '', // 具体全部数据
  clickState: false,
  smoothvalue: 0, // 平滑参数
  yaxis: 'linear', // y轴数据类型
  checkeditem: {}, // 选中的数据
  mergeditem: {}, // 参与合并的数据
  startmerged: false, // 是否开始合并
  endmerged: false, // 是否结束合并
  mergestep: 0, // 合并时执行的步骤
  backstep: 0, // 还原时执行的步骤
  checkedorder: [], // 区分主副图表
  mergednumber: 0, // 合并图表的数量
  backednumber: [], // 还原图表的数量
  cleancheck: false,
  totaltag: {},
  freshInfo: {},
  freshnumber: 0,
  errorMessage: '',
  downLoadArray: [], // 下载svg图暂存id
  grade: {},
  checked: {},
  mergedorder: {},
  showFlag: {},
  subisshow: {},
};

const getters = {
  initshowrun: (state) => state.initshowrun,
  smoothvalue: (state) => state.smoothvalue,
  categoryInfo: (state) => state.categoryInfo,
  detailData: (state) => state.detailData,
  yaxis: (state) => state.yaxis,
  checkeditem: (state) => state.checkeditem,
  mergeditem: (state) => state.mergeditem,
  startmerged: (state) => state.startmerged,
  endmerged: (state) => state.endmerged,
  mergestep: (state) => state.mergestep,
  backstep: (state) => state.backstep,
  checkedorder: (state) => state.checkedorder,
  mergednumber: (state) => state.mergednumber,
  backednumber: (state) => state.backednumber,
  cleancheck: (state) => state.cleancheck,
  getTotaltag: (state) => state.totaltag,
  getFreshInfo: (state) => state.freshInfo,
  getErrorMessage: (state) => state.errorMessage,
  freshnumber: (state) => state.freshnumber,
  getDownLoadArray: (state) => state.downLoadArray,
  grade: (state) => state.grade,
  checked: (state) => state.checked,
  mergedorder: (state) => state.mergedorder,
  showFlag: (state) => state.showFlag,
  subisshow: (state) => state.subisshow,
};

const actions = {
  async getSelfCategoryInfo(context, param) {
    const initDetailData = {};
    const initshowrun = {};
    for (let i = 0; i < param[1].length; i += 1) {
      Object.keys(param[1][i]).forEach((value) => {
        initDetailData[value] = [];
        if (!(value in initshowrun)) {
          initshowrun[value] = {};
        }
        initshowrun[value][param[0][i]] = true;
      });
    }
    context.commit('setinitshowrun', initshowrun);
    context.commit('setSelfCategoryInfo', param);
    context.commit('setInitDetailDataInfo', initDetailData);
    if (param[2].initStateFlag) {
      context.commit('setfreshnumber');
    }
  },
  async getData(context, param) {
    if (context.state.detailData[param[0]].length === 0) {
      for (let j = 0; j < param[1].length; j += 1) {
        for (let i = 0; i < context.state.categoryInfo[0].length; i += 1) {
          if (Object.keys(context.state.categoryInfo[1][i]).indexOf(param[0]) > -1) {
            if (context.state.categoryInfo[1][i][param[0]].indexOf(param[1][j]) > -1) {
              const parameter = {
                run: context.state.categoryInfo[0][i],
                tag: param[1][j],
              };
              // eslint-disable-next-line no-await-in-loop
              await http.useGet(port.category.scalar, parameter).then((res) => {
                // port.category.scalar 'scalar' 换成你需要的接口
                if (+res.data.code !== 200) {
                  context.commit('setErrorMessage', `${res.data.msg}_${new Date().getTime()}`);
                  return;
                }
                context.commit('setDetailData', [
                  param[0],
                  { run: context.state.categoryInfo[0][i], value: res.data.data },
                ]);
              });
            }
          }
        }
      }
    }
  },
};

const mutations = {
  setinitshowrun: (state, param) => {
    state.initshowrun = param;
  },
  setSelfCategoryInfo: (state, param) => {
    state.categoryInfo = param;
  },
  setInitDetailDataInfo: (state, param) => {
    state.detailData = param;
  },
  setDetailData: (state, param) => {
    state.detailData[param[0]].push(param[1]);
  },
  setClickState: (state, param) => {
    state.clickState = param;
  },
  setFreshInfo: (state, param) => {
    const foo = param[1];
    state.freshInfo[param[0]] = foo;
  },
  setfreshnumber: (state) => {
    state.freshnumber += 1;
  },
  setErrorMessage: (state, param) => {
    state.errorMessage = param;
  },
  setsmoothvalue: (state, param) => {
    state.smoothvalue = param;
  },
  setyaxis: (state, param) => {
    state.yaxis = param;
  },
  // 当选中时，增加合并项
  setcheckeditem: (state, param) => {
    if (!Object.prototype.hasOwnProperty.call(state.checkeditem, param[0])) {
      state.checkeditem[param[0]] = [];
    }
    if (JSON.stringify(state.checkeditem[param[0]]).indexOf(JSON.stringify(param[1])) === -1) {
      state.checkeditem[param[0]].push(param[1]);
    }
  },
  // 当取消选中时，删除相应的合并项
  deletecheckeditem: (state, param) => {
    if (state.checkeditem[param[0]].length === 1) {
      delete state.checkeditem[param[0]];
    } else {
      const index = JSON.stringify(state.checkeditem[param[0]]).indexOf(JSON.stringify(param[1]));
      if (index > -1) {
        state.checkeditem[param[0]].splice(index, 1);
      }
    }
  },
  // 合并函数
  merge: (state) => {
    state.mergeditem[state.checkedorder[0]] = state.checkeditem;
    if (state.startmerged === true) {
      state.startmerged = false;
    }
    state.startmerged = true;
  },
  reducemergeditem: (state, param) => {
    delete state.mergeditem[param];
  },
  back: (state) => {
    state.startmerged = false;
    if (state.endmerged === true) {
      state.endmerged = false;
    }
    state.endmerged = true;
    state.mergestep = 0;
  },
  setmergestep: (state) => {
    state.mergestep += 1;
  },
  // 当选中时，在序列末尾增加相应的序列号
  addcheckedorder: (state, param) => {
    if (state.endmerged === true) {
      state.endmerged = false;
    }
    if (state.checkedorder.indexOf(param) === -1) {
      state.checkedorder.push(param);
    }
  },
  // 当取消选中时，删除相应的序列号
  reducecheckedorder: (state, param) => {
    const index = state.checkedorder.indexOf(param);
    if (index > -1) {
      state.checkedorder.splice(index, 1);
    }
  },
  setdatainit: (state) => {
    state.checkeditem = [];
    for (let i = 0; i < state.checkedorder.length; i += 1) {
      state.downLoadArray.pop();
    }
    state.checkedorder = [];
    state.mergestep = 0;
    state.startmerged = false;
    state.mergednumber += 1;
  },
  addbackednumber: (state, param) => {
    if (state.endmerged === true) {
      state.endmerged = false;
    }
    if (state.backednumber.indexOf(param) === -1) {
      state.backednumber.push(param);
    }
  },
  reducebackednumber: (state, param) => {
    const index = state.backednumber.indexOf(param);
    if (index > -1) {
      state.backednumber.splice(index, 1);
    }
  },
  setTotaltag: (state, param) => {
    state.totaltag = param;
  },
  setDownLoadArray: (state, param) => {
    if (state.downLoadArray.indexOf(param) === -1) {
      state.downLoadArray.push(param);
    }
  },
  deleteDownLoadArray: (state, param) => {
    const index = state.downLoadArray.indexOf(param);
    if (index > -1) {
      state.downLoadArray.splice(index, 1);
    }
  },
  setmergedorder: (state, param) => {
    const foo = param[1];
    state.mergedorder[param[0]] = foo;
  },
  setgrade: (state, param) => {
    const foo = param[1];
    state.grade[param[0]] = foo;
  },
  setchecked: (state, param) => {
    const foo = param[1];
    state.checked[param[0]] = foo;
  },
  setshowFlag: (state, param) => {
    const foo = param[1];
    state.showFlag[param[0]] = foo;
  },
  setsubisshow: (state, param) => {
    const foo = param[1];
    state.subisshow[param[0]] = foo;
  },
};

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations,
};
