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

import { isNil } from 'lodash';
import { addSuffix } from '@/utils';

import { defaultColor } from './bbox';

const chroma = require('chroma-js');

// 分数最小宽度
const MinWidth = 48;

export default {
  name: 'Score',
  functional: true,
  props: {
    annotate: Object,
    scale: {
      type: Number,
    },
    imgBoundingLeft: Number,
  },
  render(h, context) {
    const { props } = context;
    const {
      annotate = {},
      imgBoundingLeft,
    } = props;

    const { data = {}, __type } = annotate;
    const { bbox, color = defaultColor, score = 1 } = data;
    if (isNil(bbox)) return null;
    // 是否为草稿模式
    const isDraft = __type === 0;

    const paddingLeft = (props.scale < 1 && !isNil(imgBoundingLeft))
      ? imgBoundingLeft
      : 0;

    const pos = isDraft ? {
      x: bbox.x,
      y: bbox.y,
      width: bbox.width,
      height: bbox.height,
    } : {
      x: bbox.x * props.scale + paddingLeft,
      y: bbox.y * props.scale,
      width: bbox.width * props.scale,
      height: bbox.height * props.scale,
    };

    const style = {
      width: addSuffix(pos.width),
      left: addSuffix(pos.x + Math.min((pos.width - MinWidth) / 2, 0)),
      top: addSuffix(Math.max(pos.y - 30, 0)),
      minWidth: addSuffix(MinWidth),
    };

    const boxStyle = {
      backgroundColor: chroma(color).alpha(0.8),
    };

    return (
      <div class='annotation-score-row tc' style={style}>
        <span class='score' style={boxStyle}>{Math.floor(score * 100)}<span class='unit'>分</span></span>
      </div>
    );
  },
};
