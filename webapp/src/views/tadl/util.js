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

import { isNil, invert } from 'lodash';
import { ONE_DAY, ONE_HOUR, ONE_MINUTE, getValueFromMap } from '@/utils';
import { expStageAccuracy, expStageIntermediate } from '@/api/tadl';

// 实验状态枚举值
export const EXPERIMENT_STATUS_MAP = {
  TO_RUN: { value: 101, label: '待运行', bgColor: '#BFBFBF' },
  WAITING: { value: 102, label: '等待中', bgColor: '#409EFF' },
  RUNNING: { value: 103, label: '运行中', bgColor: '#1890FF' },
  PAUSED: { value: 104, label: '已暂停', bgColor: '#409EFF' },
  FINISHED: { value: 202, label: '已完成', bgColor: '#52C41A' },
  FAILED: { value: 203, label: '运行失败', bgColor: '#F5222D' },
};

// 成功的实验
export const expIsFinished = (code) => [202].includes(code);
// 进行中实验
export const expInprogress = (code) => [101, 102, 103, 104, 105].includes(code);
// 可暂停实验
export const expEnablePause = (code) => [101, 102, 103].includes(code);
// 可停止实验
export const expEnableStop = (code) => [102, 103, 104].includes(code);
// 可启动实验
export const expEnableStart = (code) => [101, 104, 203].includes(code);

// Trial 状态枚举值
export const TRIAL_STATUS_MAP = {
  toRun: { value: 101, label: '待运行', bgColor: '#BFBFBF' },
  waiting: { value: 102, label: '等待中', bgColor: '#409EFF' },
  running: { value: 103, label: '运行中', bgColor: '#1890FF' },
  finished: { value: 201, label: '已完成', bgColor: '#52C41A' },
  failed: { value: 202, label: '运行失败', bgColor: '#F5222D' },
  // unknown: { value: 203, label: '未知', bgColor: '#409EFF' },
};

// 阶段状态枚举值
export const STAGE_STATUS_MAP = {
  TO_RUN: { value: 101, label: '待运行', bgColor: '#BFBFBF' },
  RUNNING: { value: 102, label: '运行中', bgColor: '#1890FF' },
  FINISHED: { value: 201, label: '已完成', bgColor: '#52C41A' },
  FAILED: { value: 202, label: '运行失败', bgColor: '#F5222D' },
};

// 阶段顺序
export const STAGE_SEQUENCE = {
  TRAIN: 1,
  SELECT: 2,
  RETRAIN: 3,
};

// 模型类型枚举值
export const MODEL_TYPE_ENUM = {
  ImageClassify: { value: 101, label: '图像分类' }, // 图像分类
  TextClassify: { value: 301, label: '文本分类' }, // 文本分类
};

// 提供获取模型字段基类方法
export const getExpByCode = (value, key) => getValueFromMap(EXPERIMENT_STATUS_MAP, value, key);

// 提供获取模型字段基类方法
export const getModelByCode = (value, key) => getValueFromMap(MODEL_TYPE_ENUM, value, key);

// 提供获取 Trial 基类方法
export const getTrialByCode = (value, key) => getValueFromMap(TRIAL_STATUS_MAP, value, key);

// 根据阶段 order 获取名称
export const getStageName = (stageOrder) => invert(STAGE_SEQUENCE)[stageOrder];
export const getStageOrder = (stageName) => STAGE_SEQUENCE[stageName];

// 刷新频率
export const refreshControls = [
  { icon: 'el-icon-remove-outline', label: '关闭自动刷新', value: 0 },
  { icon: 'el-icon-timer', label: '每 10s 刷新', value: 10 },
  { icon: 'el-icon-timer', label: '每 20s 刷新', value: 20 },
  { icon: 'el-icon-timer', label: '每 30s 刷新', value: 30 },
  { icon: 'el-icon-timer', label: '每 60s 刷新', value: 60 },
];

// 时间格式
export const timeFmts = [
  { label: 'day', value: 'day' },
  { label: 'hour', value: 'hour' },
  { label: 'min', value: 'min' },
];

/**
 * 运行时间格式化
 * @param {Number} ms 运行毫秒数
 * @returns {String} 返回格式化的时间
 */
export const runTimeFormatter = (ms) => {
  let day;
  let hour;
  let minute;
  if (ms > ONE_DAY) {
    day = Math.floor(ms / ONE_DAY);
    ms %= ONE_DAY;
  }
  if (ms > ONE_HOUR) {
    hour = Math.floor(ms / ONE_HOUR);
    ms %= ONE_HOUR;
  }
  if (ms > ONE_MINUTE) {
    minute = Math.floor(ms / ONE_MINUTE);
  }
  const dayStr = isNil(day) ? '' : `${day}day `;
  const hourStr = isNil(hour) && !dayStr ? '' : `${hour || 0}hour `;
  const minStr = isNil(minute) && !hourStr ? '' : `${minute || 0}min`;
  return `${dayStr}${hourStr}${minStr}`;
};

// 根据时间单位解析时间
export const parseRunTime = (time, unit) => {
  const unitMap = {
    day: ONE_DAY,
    hour: ONE_HOUR,
    min: ONE_MINUTE,
  };
  return time * unitMap[unit] || 0;
};

// 提取图数据的data
export const extractData = (raw) => {
  const { xField, yField } = raw.config;
  const data = raw.data.map((point) => {
    return {
      [xField]: String(point[xField]),
      [yField]: point[yField],
    };
  });
  return data.flat(); // 将二维数组压平成一维数组
};

export const extractScatterData = (raw) => {
  const { xField, yField } = raw.config;
  const data = raw.data.map((point) => {
    return {
      [xField]: point[xField],
      [yField]: point[yField],
    };
  });
  return data.flat(); // 将二维数组压平成一维数组
};

export const extractSeriesData = (raw) => {
  const { xField, yField, seriesField } = raw.config;
  const data = raw.data.map((d) => {
    return d.list.map((point) => {
      return {
        [xField]: String(point[xField]),
        [yField]: point[yField],
        [seriesField]: d[seriesField],
      };
    });
  });
  return data.flat(); // 将二维数组压平成一维数组
};

const metricMap = {
  accuracy: expStageAccuracy,
  intermediate: expStageIntermediate,
};

// 根据指定metric获取数据
export const fetchMetric = async (experimentId, stageOrder, metricStr) => {
  const metric = await metricMap[metricStr](experimentId, stageOrder);
  return metric;
};
