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

import { isNil } from 'lodash';
import { addSuffix, chroma, colorByLuminance } from '@/utils';

import { defaultColor } from './bbox';

export default {
  name: 'Tag',
  functional: true,
  props: {
    annotate: Object,
    offset: Function,
    transformer: Object,
    getLabelName: Function,
    brush: Object,
    currentAnnotationId: String,
  },
  render(h, context) {
    const { props } = context;
    const {
      annotate = {},
      getLabelName,
      offset,
      transformer,
      brush,
    } = props;

    const { data = {}, id } = annotate;
    const { bbox, color = defaultColor } = data;

    // 当前在拖拽中不展示
    if(props.currentAnnotationId === id && brush.isBrushing) return null;

    if (isNil(bbox)) return null;
    // 是否为草稿模式

    const pos = offset(props.annotate);

    const style = {
      width: addSuffix(pos.width),
      left: addSuffix(pos.x),
      top: addSuffix(pos.y),
      color: colorByLuminance(color),
    };

    // 匹配当前标注
    if(annotate.id === transformer.id) {
      style.transform = `translate(${transformer.dx}px, ${transformer.dy}px)`;
    }

    const tagColor = chroma(color).alpha(0.8).toString();
    const tagName = getLabelName(data.categoryId);

    if (!tagName) return null;
    return (
      <div class='annotation-label image-tag' style={style}>
        <el-tag color={tagColor} disable-transitions style={{ color: 'inherit', border: 'none' }}>{tagName}</el-tag>
      </div>
    );
  },
};
