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

import http from '@/utils/VisualUtils/request';
import port from '@/utils/VisualUtils/api';

/* eslint-disable */

const state = {
  categoryInfo: '', // 存放自己的类目信息
  detailData: '', // 具体数据
  clickState: false,
  showrun: {},
  totaltag: '',
  freshInfo: {},
  errorMessage: '',
  showFlag: {
    firstTime: true
  }
}

const getters = {
  categoryInfo: (state) => state.categoryInfo,
  detailData: (state) => state.detailData,
  showrun: (state) => state.showrun,
  getTotaltag: (state) => state.totaltag,
  getFreshInfo: (state) => state.freshInfo,
  getErrorMessage: (state) => state.errorMessage,
  getShowFlag: (state) => state.showFlag
}

const actions = {
  async getSelfCategoryInfo (context, param) {
    let initDetailData = {}
    // 根据自己的类目增加相应的判断
    for (let i = 0; i < param[1].length; i++) {
      Object.keys(param[1][i]).forEach(value => {
        initDetailData[value] = []
      })
    }
    context.commit('setSelfCategoryInfo', param)
    context.commit('setInitDetailDataInfo', initDetailData)
    if (param[2]['initStateFlag']) {
      if (typeof (param[1]) === 'object') {
      } else if (param[1][0] === 'true') {
        // 处理需要数据请求
      }
    }
  },
  async getData (context, param) {
    // 类目  tags
    if (context.state.detailData[param[0]].length === 0) {
      for (let j = 0; j < param[1].length; j++) {
        for (let i = 0; i < context.state.categoryInfo[0].length; i++) {
          if (Object.keys(context.state.categoryInfo[1][i]).indexOf(param[0]) > -1) {
            if (context.state.categoryInfo[1][i][param[0]].indexOf(param[1][j]) > -1) {
              let parameter = {
                run: context.state.categoryInfo[0][i],
                tag: param[1][j]
              }
              await http.useGet(port.category[param[0]], parameter).then(res => { // port.category.scalar 'scalar' 换成你需要的接口
                if (+res.data.code !== 200) {
                  context.commit('setErrorMessage', res.data.msg + '_' + new Date().getTime())
                  return
                }
                context.commit('setDetailData', [param[0], { 'run': context.state.categoryInfo[0][i], 'value': res.data.data }])
              })
            }
          }
        }
      }
    }
  }
}

const mutations = {
  setShowFlag: (state, param) => {
    state.showFlag[param[0]] = param[1]
  },
  setSelfCategoryInfo: (state, param) => {
    state.categoryInfo = param
  },
  setInitDetailDataInfo: (state, param) => {
    state.detailData = param
  },
  setDetailData: (state, param) => {
    state.detailData[param[0]].push(param[1])
  },
  setClickState: (state, param) => {
    state.clickState = param
  },
  setshowrun: (state, param) => {
    for (let i = 0; i < state.categoryInfo[0].length; i++) {
      if (param.indexOf(state.categoryInfo[0][i]) > -1) {
        state.showrun[state.categoryInfo[0][i]] = true
      } else {
        state.showrun[state.categoryInfo[0][i]] = false
      }
    }
  },
  setTotaltag: (state, param) => {
    state.totaltag = param
  },
  setFreshInfo: (state, param) => {
    state.freshInfo[param[0]] = param[1] // false表示类目被打开
  },
  setErrorMessage: (state, param) => {
    state.errorMessage = param
  }
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}
