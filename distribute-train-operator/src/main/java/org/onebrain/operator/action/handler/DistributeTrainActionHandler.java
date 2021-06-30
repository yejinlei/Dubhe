 /**
 * Copyright 2020 Tianshu AI Platform. All Rights Reserved.
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

package org.onebrain.operator.action.handler;

import org.onebrain.operator.crd.DistributeTrain;

/**
 * @description 分布式训练的事件处理器
 * @date 2020-09-23
 */
public interface DistributeTrainActionHandler {

    /**
     * 处理相应的事件
     * @param distributeTrain 分布式训练信息
     */
    void handlerAction(DistributeTrain distributeTrain);
}
