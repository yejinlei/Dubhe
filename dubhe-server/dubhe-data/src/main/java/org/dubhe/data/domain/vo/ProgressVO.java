/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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
 * =============================================================
 */

package org.dubhe.data.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dubhe.base.MagicNumConstant;

import java.io.Serializable;

/**
 * @description 数据集状态
 * @date 2020-04-10
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProgressVO implements Serializable {

    @Builder.Default
    private Long finished = MagicNumConstant.ZERO_LONG;
    @Builder.Default
    private Long unfinished = MagicNumConstant.ZERO_LONG;
    @Builder.Default
    private Long autoFinished = MagicNumConstant.ZERO_LONG;
    @Builder.Default
    private Long finishAutoTrack = MagicNumConstant.ZERO_LONG;
    @Builder.Default
    private Long annotationNotDistinguishFile = MagicNumConstant.ZERO_LONG;

}