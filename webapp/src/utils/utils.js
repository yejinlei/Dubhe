/*
* Copyright 2019-2020 Zheng Jie
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/**
 * utils, 通用方法
 */

/**
 * Parse the time to string
 * @param {(Object|string|number)} time
 * @param {string} cFormat
 * @returns {string}
 */
export function parseTime(time, cFormat) {
  if (arguments.length === 0) {
    return null;
  }
  const format = cFormat || '{y}-{m}-{d} {h}:{i}:{s}';
  let date;
  if (typeof time === 'undefined' || time === null || time === 'null') {
    return '';
  } if (typeof time === 'object') {
    date = time;
  } else {
    if ((typeof time === 'string')) {
      if ((/^[0-9]+$/.test(time))) {
        time = parseInt(time, 10);
      } else {
        time = time.replace(/ /g, "T");
      }
    }
    if ((typeof time === 'number') && (time.toString().length === 10)) {
      time *= 1000;
    }
    date = new Date(time);
  }
  const formatObj = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay(),
    S: date.getMilliseconds(),
  };
  const time_str = format.replace(/{(y|m|d|h|i|s|a|S)+}/g, (result, key) => {
    let value = formatObj[key];
    // Note: getDay() returns 0 on Sunday
    if (key === 'a') { return ['日', '一', '二', '三', '四', '五', '六'][value]; }
    if (result.length > 0 && value < 10) {
      value = `0${value}`;
    }
    return value || 0;
  });
  return time_str;
}

/**
 * @param {string} input value
 * @returns {number} output value
 */
export function byteLength(str) {
  // returns the byte length of an utf8 string
  let s = str.length;
  for (let i = str.length - 1; i >= 0; i -= 1) {
    const code = str.charCodeAt(i);
    if (code > 0x7f && code <= 0x7ff) s += 1;
    else if (code > 0x7ff && code <= 0xffff) s += 2;
    if (code >= 0xDC00 && code <= 0xDFFF) i -= 1;
  }
  return s;
}

/**
 * @param {string} url
 * @returns {Object}
 */
export function param2Obj(url) {
  const search = url.split('?')[1];
  if (!search) {
    return {};
  }
  return JSON.parse(
    `{"${
    decodeURIComponent(search)
      .replace(/"/g, '\\"')
      .replace(/&/g, '","')
      .replace(/=/g, '":"')
      .replace(/\+/g, ' ')
    }"}`,
  );
}

/**
 * @param {Function} func
 * @param {number} wait
 * @param {boolean} immediate
 * @return {*}
 */
export function debounce(func, wait, immediate) {
  let timeout; let args; let context; let timestamp; let result;

  const later = () => {
    // 据上一次触发时间间隔
    const last = +new Date() - timestamp;

    // 上次被包装函数被调用时间间隔 last 小于设定时间间隔 wait
    if (last < wait && last > 0) {
      timeout = setTimeout(later, wait - last);
    } else {
      timeout = null;
      // 如果设定为immediate===true，因为开始边界已经调用过了此处无需调用
      if (!immediate) {
        result = func.apply(context, args);
        if (!timeout) context = args = null;
      }
    }
  };

  // eslint-disable-next-line func-names
  return function (...args) {
    context = this;
    timestamp = +new Date();
    const callNow = immediate && !timeout;
    // 如果延时不存在，重新设定延时
    if (!timeout) timeout = setTimeout(later, wait);
    if (callNow) {
      result = func.apply(context, args);
      context = args = null;
    }

    return result;
  };
}

/**
 * @param {string} unixTimestamp
 * @returns {string} normalTime
 */
export function unixTimestamp2Normal(unixTimestamp) {
  const unixTimestampLocal = new Date(unixTimestamp * 1000);
  const commonTime = unixTimestampLocal.toLocaleString('en-GB', { hour12: false });
  const tim = commonTime.split('/');
  const year = tim[2].split(',')[0];
  const month = tim[1];
  const day = tim[0];
  const tt = tim[2].split(',')[1];
  return `${year}/${month}/${day}${tt}`;
}

/**
 * @param {number} num
 * @param {number} fix
 * @returns {string} scientificnumber
 */
export function scientificNotation(num, fix) {
  const absd = Math.abs(num);
  if (absd > 10000) {
    const numLen = absd.toString().length - 1;
    num = `${num / Math.pow(10, numLen)}e+${numLen}`;
  } else if (absd < 0.01 && absd !== 0) {
    const dString = absd.toString();
    let i = 3;
    for (; i < dString.length; i += 1) {
      if (dString[i] !== '0') {
        break;
      }
    }
    num = `${(num * Math.pow(10, i - 1)).toFixed(fix)}e-${i - 1}`;
  } else {
    num = num.toFixed(fix);
  }
  return num;
}

/**
 * Check if an element has a class
 * @param {HTMLElement} elm
 * @param {string} cls
 * @returns {boolean}
 */
export function hasClass(ele, cls) {
  return !!ele.className.match(new RegExp(`(\\s|^)${cls}(\\s|$)`));
}

/**
 * Add class to element
 * @param {HTMLElement} elm
 * @param {string} cls
 */
export function addClass(ele, cls) {
  if (!hasClass(ele, cls)) ele.className += ` ${cls}`;
}

/**
 * Remove class from element
 * @param {HTMLElement} elm
 * @param {string} cls
 */
export function removeClass(ele, cls) {
  if (hasClass(ele, cls)) {
    const reg = new RegExp(`(\\s|^)${cls}(\\s|$)`);
    ele.className = ele.className.replace(reg, ' ');
  }
}

/**
 * 下载文件
 * @param {*} data 文件内容
 * @param {*} fileName 文件名
 */
export function downloadFile(data, fileName) {
  const link = document.createElement('a');
  link.style.display = 'none';
  link.href = window.URL.createObjectURL(new Blob([data]));
  link.setAttribute('download', fileName || `${parseTime(new Date())}`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

export function convertMapToList(map, key = 'key', value = 'value') {
  const list = [];
  map = map || {};
  const mapKeyList = Object.keys(map);
  mapKeyList.forEach(mapKey => {
    const obj = {};
    obj[key] = mapKey;
    obj[value] = map[mapKey];
    list.push(obj);
  });
  return list;
}

export function stringIsValidPythonVariable(str) {
  if (typeof str !== 'string') {
    return false;
  }
  const pattern = /^[_a-zA-Z][_a-zA-Z0-9]*$/;
  return pattern.test(str);

}
