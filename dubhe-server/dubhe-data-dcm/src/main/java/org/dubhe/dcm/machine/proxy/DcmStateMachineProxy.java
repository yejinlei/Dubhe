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
package org.dubhe.dcm.machine.proxy;

import org.dubhe.biz.base.utils.SpringContextHolder;
import org.dubhe.biz.statemachine.dto.StateChangeDTO;
import org.dubhe.biz.statemachine.utils.StateMachineProxyUtil;
import org.dubhe.dcm.machine.statemachine.DcmGlobalStateMachine;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description 代理执行状态机
 * @date 2020-08-27
 */
@Component
public class DcmStateMachineProxy {

    /**
     * 获取全局状态机Bean
     *
     * @return Object
     */
    public static Object getGlobalStateMachine() {
        return SpringContextHolder.getBean(DcmGlobalStateMachine.class);
    }

    /**
     * 代理执行单个状态机的状态切换
     *
     * @param stateChangeDTO 数据集状态切换信息
     */
    public static void proxyExecutionSingleState(StateChangeDTO stateChangeDTO) {
        StateMachineProxyUtil.proxyExecutionSingleState(stateChangeDTO,getGlobalStateMachine());
    }

    /**
     * 代理执行多个状态机的状态切换
     *
     * @param stateChangeDTOList 多个状态机切换信息
     */
    public static void proxyExecutionRelationState(List<StateChangeDTO> stateChangeDTOList) {
        StateMachineProxyUtil.proxyExecutionRelationState(stateChangeDTOList,getGlobalStateMachine());
    }

}
