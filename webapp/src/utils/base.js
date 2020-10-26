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

import { format, parseISO, isDate } from 'date-fns';
import { isEqual, isPlainObject, isNil, findIndex, findLastIndex } from 'lodash';
import { nanoid } from 'nanoid';

const chroma = require('chroma-js');

export const duplicate = (arr, callback) => {
  const index = findIndex(arr, callback);
  const lastIndex = findLastIndex(arr, callback);
  return index !== lastIndex;
};

// 合并多个属性
export function mergeProps(...args) {
  const props = {};
  args.forEach((p = {}) => {
    for (const [k, v] of Object.entries(p)) {
      props[k] = props[k] || {};
      if (isPlainObject(v)) {
        Object.assign(props[k], v);
      } else {
        props[k] = v;
      }
    }
  });
  return props;
}

// 生成唯一 id
export const generateUuid = (count = 6) => nanoid(count);

// 支持正则表达式的 endsWith
export const endsWith = (string, endStr) => {
  // 区分正则和普通字符
  if (endStr instanceof RegExp) {
    return endStr.test(string);
  }
  return typeof endStr === 'string' && string.endsWith(endStr);
};

// 支持正则表达式的 endsWith
export const startsWith = (string, startStr) => {
  // 区分正则和普通字符
  if (startStr instanceof RegExp) {
    return startStr.test(string);
  }
  return typeof startStr === 'string' && string.startsWith(startStr);
};

// 数组新增字段
export const add = (arr, ...newData) => {
  return [...arr, ...newData];
};

// 删除数组中指定索引的字段
export const remove = (arr, i) => {
  if (i < 0) return arr;
  return ([
    ...arr.slice(0, i),
    ...arr.slice(i + 1),
  ]);
};

// 替换数组中指定索引的项
export const replace = (arr, i, ...newData) => {
  if (!Array.isArray(arr) || typeof i !== 'number') return arr;
  return ([
    ...arr.slice(0, i),
    ...newData,
    ...arr.slice(i + 1),
  ]);
};

// add suffix style
export const addSuffix = (prop, suffix = 'px') => `${prop}${suffix}`;

// clamp，如果 num 位于 [min, max] 之间，就返回 num，否则返回最近边界值
export const clamp = (num, min, max) =>
  Math.max(Math.min(num, Math.max(min, max)), Math.min(min, max));

// 小于 9 数字添加 0 前缀
export const leadingZero = (num, targetLength = 2, char = '0') => {
  const prefix = num < 0 ? '-' : '';
  const number = Math.abs(parseFloat(num));
  return prefix + String(number).padStart(targetLength, char);
};

// 保留几位小数
// scale 放大倍数，length: 保留小数点位数
// 0.5122 => 51
export const toFixed = (num, scale = 2, length = 2) => {
  // eslint-disable-next-line
  return Math.floor(num * Math.pow(10, scale + length)) / Math.pow(10, length);
};

// 生成精准时间戳（正常情况下 Date.now() 能满足，只有需要绝对精准的时候才启用）
export const performanceTiming = () => {
  if (window.performance.now) {
    return toFixed(window.performance.timing.navigationStart + window.performance.now(), 3, 3);
  }
  return Date.now();
};

// 日期格式化
export const formatDateTime = (datetime, formatter = 'yyyy-MM-dd HH:mm:ss') => {
  const input = isDate(datetime) ? datetime : parseISO(datetime);
  return format(input, formatter);
};

// 抛出 http 异常
export const HttpError = (message, code) => {
  const error = new Error(message);
  error.name = 'HttpError';
  if (code != null) {
    error.code = code;
  }

  return Promise.reject(error);
};

// 抛出 Assert 异常，本质是打断当前流程，不需要弹窗展示
export const AssertError = (message, code) => {
  const error = new Error(message);
  error.name = 'AssertError';
  if (code != null) {
    error.code = code;
  }

  throw error;
};

// 返回当前值
export const identity = d => d;

// 数组间基于属性对比
export const isEqualByProp = (arr1, arr2, prop) => {
  return isEqual(arr1.map(d => d[prop]), arr2.map(d => d[prop]));
};

// 根据背景色深浅来设置颜色
export const colorByLuminance = (color) => {
  if(isNil(color) || color === '') {
    return '#333';
  } 
  const colorMap = {
    dark: '#333',
    light: '#fff',
  };
  const luminance = chroma(color).luminance();
  const theme = luminance < 0.5 ? 'light' : 'dark';
  return colorMap[theme];
};

export { chroma };
