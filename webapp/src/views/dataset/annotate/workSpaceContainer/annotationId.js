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

const validTrackId = (trackId) => {
  if (isNil(trackId) || trackId === -1) return false;
  return trackId;
};

export default {
  name: 'Tag',
  functional: true,
  props: {
    annotate: Object,
    scale: {
      type: Number,
    },
    imgBoundingLeft: Number,
    getLabelName: Function,
  },
  render(h, context) {
    const { props } = context;
    const {
      annotate = {},
      imgBoundingLeft,
    } = props;

    const { data = {}, __type } = annotate;
    const { bbox, color = defaultColor } = data;
    if (isNil(bbox)) return null;
    // 是否为草稿模式
    const isDraft = __type === 0;
    // todo: top
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
      left: addSuffix(pos.x),
      top: addSuffix(pos.y),
    };

    const tagColor = chroma(color).alpha(0.8).toString();

    const trackId = (() => {
      if (annotate.name) return annotate.name;
      if (validTrackId(data.track_id) !== false) {
        return data.track_id;
      }
      return null;
    })();

    if (!trackId) return null;
    return (
      <div class='annotation-label image-tag' style={style}>
        <el-tag color={tagColor} style={{ color: '#fff', border: 'none' }}>{trackId}</el-tag>
      </div>
    );
  },
};
