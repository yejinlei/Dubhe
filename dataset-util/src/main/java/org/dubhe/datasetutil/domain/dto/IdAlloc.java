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
package org.dubhe.datasetutil.domain.dto;

import lombok.Data;
import org.dubhe.datasetutil.common.base.MagicNumConstant;

/**
 * @description ID策略实体
 * @date 2020-10-16
 */
@Data
public class IdAlloc {

    /**
     * 起始位置
     */
    private long startNumber;

    /**
     * 结束位置
     */
    private long endNumber;

    /**
     * 可用数量
     */
    private long usedNumber;

    public IdAlloc() {
        this.startNumber = MagicNumConstant.ZERO;
        this.endNumber = MagicNumConstant.ZERO;
        this.usedNumber = MagicNumConstant.ZERO;
    }

}
