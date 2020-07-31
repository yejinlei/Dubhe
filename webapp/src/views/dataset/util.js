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
  }));
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

// 上传文件之前加一层转换
export const withDimensionFiles = async(files) => {
  return Promise.all(files.map(file => checkImg(file)));
};

// context 配置
export const labelsSymbol = Symbol('labels');
export const enhanceSymbol = Symbol('enhance');

// 数据集类型
export const dataTypeMap = {
  0: '图片',
  1: '视频',
};

// 文件状态
export const fileTypeEnum = {
  0: { label: '全部', abbr: '全部' },
  1: { label: '未标注', abbr: '未标注' },
  2: { label: '自动标注完成', abbr: '自动完成' },
  3: { label: '手动标注完成', abbr: '手动完成' },
  4: { label: '自动目标跟踪完成', abbr: '跟踪完成' },
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
  0: { name: '未标注', type: 'info' },
  1: { name: '标注中', type: 'warning' },
  2: { name: '自动标注中', type: 'danger' },
  3: { name: '自动标注完成', type: '' },
  4: { name: '标注完成', type: 'success' },
  5: { name: '未采样', bgColor: '#a7a7a7', color: '#fff' },
  6: { name: '目标跟踪完成', bgColor: '#409EFF', color: '#fff' },
  7: { name: '采样中', bgColor: '#606266', color: '#fff' },
  8: { name: '数据增强中', bgColor: '#1890ff', color: '#fff' },
};

// 标注精度
export const annotationProgressMap = {
  finished: '已完成',
  unfinished: '未完成',
  autoFinished: '自动标注完成',
  finishAutoTrack: '目标跟踪完成',
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
