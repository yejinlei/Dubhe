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
package org.dubhe.tadl.machine.utils.identify;


import org.dubhe.biz.statemachine.dto.StateChangeDTO;
import org.dubhe.tadl.machine.proxy.StateMachineProxy;


import java.util.List;

/**
 * @description 状态机工具类   业务层注入此类调用代理方法
 * @date 2020-08-27
 */
public class StateMachineUtil {

    /**
     * 执行单个状态机的状态切换
     *
     * @param stateChangeDTO 状态切换信息
     */
    public static void stateChange(StateChangeDTO stateChangeDTO) {
        StateMachineProxy.proxyExecutionSingleState(stateChangeDTO);
    }

    /**
     * 执行关联状态机的状态切换
     *
     * @param stateChangeDTOList 状态切换信息
     */
    public static void stateChange(List<StateChangeDTO> stateChangeDTOList) {
        StateMachineProxy.proxyExecutionRelationState(stateChangeDTOList);
    }

}
