/**
 * Copyright 2020 Zhejiang Lab & The OneFlow Authors. All Rights Reserved.
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

package org.onebrain.operator.redis.key;

import org.onebrain.operator.redis.AbstractKeyPrefix;

/**
 * @description  由operator产生的cr的唯一标识
 * @date 2020-09-23
 */
public class OperatorKey extends AbstractKeyPrefix {

    public OperatorKey(String prefix) {
        super(prefix);
    }

    public OperatorKey(String prefix, int expireSeconds) {
        super(prefix, expireSeconds);
    }

    /**
     * 分布式训练 Key
     */
    public static final OperatorKey CR = new OperatorKey("DistributeTrain");

    /**
     * 分布式训练Job Key
     */
    public static final OperatorKey CR_JOB = new OperatorKey("DistributeTrain:Job");
}
