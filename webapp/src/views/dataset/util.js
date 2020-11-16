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

import { parseBbox, flatBbox, generateUuid } from '@/utils';
import { bucketName, bucketHost } from '@/utils/minIO';

// 解析 annotation 信息
export const parseAnnotation = (annotationStr, labels) => {
  let result = [];
  try {
    const annotation = JSON.parse(annotationStr);
    result = annotation.map(d => {
      return {
        id: d.id || generateUuid(),
        name: d.name || '',
        data: {
          score: d.score,
          bbox: parseBbox(d.bbox),
          track_id: d.track_id,
          categoryId: d.category_id,
          color: (labels.find(label => label.id === d.category_id) || {}).color || '#000',
        },
      };
    });
  } catch (err) {
    console.error(`解析 annotation 错误`, err);
  }

  return result;
};

// 将 annotation 生成可拖拽的形式
export const withExtent = annotations => {
  return annotations.map(d => ({
    ...d,
    data: {
      ...d.data,
      extent: {
        x0: d.data.bbox.x,
        y0: d.data.bbox.y,
        x1: d.data.bbox.x + d.data.bbox.width,
        y1: d.data.bbox.y + d.data.bbox.height,
      },
    },
  }));
};

// 将annotations 生成字符串
export const stringifyAnnotations = (annotations) => {
  const resultList = annotations.map(d => {
    const { id, data, name = '' } = d;
    const res = {
      id: id || generateUuid(),
      name,
      area: 0,
      iscrowd: 0,
      segmentation: [[]],
      bbox: flatBbox(data.bbox),
      category_id: data.categoryId,
      score: data.score,
    };
    if (data.track_id) {
      res.track_id = data.track_id;
    }
    return res;
  });
  let resultString = '';
  try {
    resultString = JSON.stringify(resultList);
  } catch (err) {
    console.error('解析标注失败', err);
  }
  return resultString;
};

// 解析 minIO 返回的图片
const buildImgUrl = (list = []) => {
  return list.map(d => ({
    url: `${bucketName}/${d.data.objectName}`,
    ...(d.data.meta || {}), // 附加的信息，目前只包括 width, height
  }));
};

// 对文件进行自定义转换
export const withDimensionFile = (result, file) => {
  return new Promise((resolve) => {
    const reader = new FileReader();
    reader.addEventListener("load", () => {
      const img = new Image();
      img.onload = () => resolve({
        ...result,
        data: {
          ...result.data,
          meta: {
            width: img.width,
            height: img.height,
          },
        },
      });
      img.src = reader.result;
    }, false);

    reader.readAsDataURL(file.raw);
  });
};

export const getImgFromMinIO = (res) => {
  return buildImgUrl(res);
};

const defaultTransform = d => ({
  id: d.id,
  url: `${bucketHost}/${d.url}`,
  datasetId: d.datasetId,
});

// 生成完成的图片链接
export const getFullFileUrl = d => `${bucketHost}/${d.url}`;

// 转化文件
export const transformFiles = (rawFiles, callback) => {
  return rawFiles.reduce((arr, cur) => {
    let res = defaultTransform(cur);
    if (typeof callback === 'function') {
      res = { ...res, ...callback(cur) };
    }
    return arr.concat(res);
  }, []);
};

// 转换单个文件
export const transformFile = (rawFile, callback) => {
  let res = defaultTransform(rawFile);
  if (typeof callback === 'function') {
    res = { ...res, ...callback(rawFile) };
  }
  return res;
};

// deprecated
// 获取文件信息
async function checkImg (file){
  const fileUrl = getFullFileUrl(file);
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.onload = () => resolve({
      width: img.width,
      height: img.height,
      ...file,
    });
    img.onerror = (err) => reject(err);

    img.src = fileUrl;
  });
}

// deprecated
// 上传文件之前加一层转换
export const withDimensionFiles = async(files) => {
  return Promise.all(files.map(file => checkImg(file)));
};

// 目标跟踪视频上传参数
export const trackUploadProps = {
  acceptSize: 1024,
  accept: '.mp4,.avi,.mkv,.mov,.webm,.wmv',
  listType: 'text',
  limit: 1,
  multiple: false,
  showFileCount: false,
};

// context 配置
export const labelsSymbol = Symbol('labels');
export const enhanceSymbol = Symbol('enhance');

// 数据集类型
export const dataTypeMap = {
  0: '图片',
  1: '视频',
};

export const dataTypeCodeMap = {
  'IMAGE': 0,
  'VIDEO': 1,
};

// 文件状态
export const fileTypeEnum = {
  0: { label: '全部', abbr: '全部' },
  101: { label: '未标注', abbr: '未标注' },
  102: { label: '手动标注中', abbr: '手动标注中' },
  103: { label: '自动标注完成', abbr: '自动完成' },
  104: { label: '手动标注完成', abbr: '手动完成' },
  105: { label: '未识别', abbr: '未识别'},
  201: { label: '目标跟踪完成', abbr: '跟踪完成' },
  301: { label: '未完成', abbr: '未完成'},
  302: { label: '已完成', abbr: '已完成'},
};
export const fileCodeMap = {
  'ALL': 0,
  'UNANNOTATED': 101,
  'MANUAL_ANNOTATING': 102,
  'AUTO_ANNOTATED': 103,
  'MANUAL_ANNOTATED': 104,
  'UNRECOGNIZED': 105,
  'TRACK_SUCCEED': 201,
  'UNCOMPLETED': 301,
  'COMPLETED': 302,
};

export const annotationCodeMap = {
  'ANNOTATE': 1,
  'CLASSIFY': 2,
  'TRACK': 5,
};

export const annotationMap = {
  1: { name: '目标检测', urlPrefix: 'annotate', component: 'AnnotateDataset' },
  2: { name: '图像分类', urlPrefix: 'classify', component: 'Classify' },
  // 3: { name: '行为分析', urlPrefix: 'analysis' },
  // 4: { name: '异常检测', urlPrefix: 'exception' },
  5: { name: '目标跟踪', urlPrefix: 'track', component: 'TrackDataset' },
};

// 数据集状态
export const datasetStatusMap = {
  101: { name: '未标注', type: 'info' },
  102: { name: '标注中', type: 'warning' },
  103: { name: '自动标注中', type: 'danger' },
  104: { name: '自动标注完成', type: '' },
  105: { name: '标注完成', type: 'success' },
  201: { name: '目标跟踪中', bgColor: '#409EFF', color: '#fff' },
  202: { name: '目标跟踪完成', bgColor: '#409EFF', color: '#fff' },
  203: { name: '目标跟踪失败', bgColor: '#409EFF', color: '#fff' },
  301: { name: '未采样', bgColor: '#a7a7a7', color: '#fff' },
  302: { name: '采样中', bgColor: '#606266', color: '#fff' },
  303: { name: '采样失败', bgColor: '#606266', color: '#fff' },
  401: { name: '数据增强中', bgColor: '#1890ff', color: '#fff' },
  402: { name: '导入中', bgColor: '#606266', color: '#fff' },
};
export const statusCodeMap = {
  101: 'UNANNOTATED', // 未标注
  102: 'ANNOTATING',
  103: 'AUTO_ANNOTATING',
  104: 'AUTO_ANNOTATED',
  105: 'ANNOTATED',
  201: 'TRACKING',
  202: 'TRACK_SUCCEED',
  203: 'TRACK_FAILED',
  301: 'UNSAMPLED',
  302: 'SAMPLING',
  303: 'SAMPLE_FAILED',
  401: 'ENHANCING',
  402: 'IMPORTING',
};

// 标注精度
export const annotationProgressMap = {
  finished: '已完成',
  unfinished: '未完成',
  autoFinished: '自动标注完成',
  finishAutoTrack: '目标跟踪完成',
  annotationNotDistinguishFile: '未识别',
};

export const decompressProgressMap = {
  0: '等待中',
  1: '解压中',
  2: '解压完成',
  3: '解压失败',
};

// 数据增强类型
export const dataEnhanceMap = {
  1: '',
  2: 'success',
  3: 'info',
  4: 'warning',
};

// 根据value取key
export const findKey = (value, data, compare = (a, b) => a === b) => 
{ 
  return Object.keys(data).find(k => compare(data[k], value));
};