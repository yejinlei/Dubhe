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

import cx from 'classnames';
import { isNil } from 'lodash';

const chroma = require('chroma-js');

export const defaultColor = 'rgba(102, 181, 245, 1)';
const defaultFill = 'rgba(102, 181, 245, 0.1)';

export default {
  name: 'Bbox',
  functional: true,
  props: {
    annotate: Object,
    scale: {
      type: Number,
      default: 1,
    },
    currentAnnotationId: Object,
    imgBoundingLeft: Number,
    handleClick: Function,
    imgRef: HTMLImageElement,
  },
  render(h, context) {
    const { props } = context;
    const {
      annotate = {},
      imgBoundingLeft,
      currentAnnotationId,
      handleClick,
      ...rest // does this work?
    } = props;
    const { data = {}, __type } = annotate;
    const { bbox, color } = data;

    if (isNil(bbox)) return null;

    const bgColor = color || defaultFill;

    const isActive = currentAnnotationId.value === annotate.id;
    const colorAlpha = isActive ? 0.4 : 0.1;

    const fill = chroma(bgColor).alpha(colorAlpha);

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

    return (
      <g class={cx('bbox-group', {
        active: isActive,
      })} onClick={handleClick(annotate)}>
        <rect
          fill={fill}
          stroke={color || defaultColor}
          strokeWidth={4}
          // {...bounding} spread operator sucks...
          x={pos.x}
          y={pos.y}
          width={pos.width}
          height={pos.height}
          {...rest}
        />
      </g>
    );
  },
};
