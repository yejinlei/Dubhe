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
package org.dubhe.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.dubhe.dto.StateChangeDTO;
import org.dubhe.enums.LogEnum;
import org.dubhe.exception.StateMachineException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @description 代理执行状态机
 * @date 2020-08-27
 */
@Component
public class StateMachineProxyUtil {

    /**
     * 代理执行单个状态机的状态切换
     *
     * @param stateChangeDTO 数据集状态切换信息
     * @param objectService  服务类
     */
    public static void proxyExecutionSingleState(StateChangeDTO stateChangeDTO,Object objectService) {
        checkSingleParam(stateChangeDTO);
        //获取全局状态机中的指定状态机
        Field field = ReflectionUtils.findField(objectService.getClass(), stateChangeDTO.getStateMachineType());
        if (field == null) {
            throw new StateMachineException("在全局状态机中未找到指定状态机");
        }
        //获取需要执行的状态机对象
        Object stateMachineObject = SpringContextHolder.getBean(field.getName());
        try {
            //获取目标执行方法的参数类型
            List<Class<T>> paramTypesList = getMethodParamTypes(stateMachineObject, stateChangeDTO.getEventMethodName());
            //构造目标执行方法
            Method method = ReflectionUtils.findMethod(stateMachineObject.getClass(), stateChangeDTO.getEventMethodName(), paramTypesList.toArray(new Class[paramTypesList.size()]));
            if (stateChangeDTO.getObjectParam().length != paramTypesList.size()) {
                LogUtil.error(LogEnum.STATE_MACHINE, " 目标执行方法参数 {} 与传入的数量不一致 {}  ", paramTypesList.size(), stateChangeDTO.getObjectParam().length);
            } else {
                ReflectionUtils.invokeMethod(method, stateMachineObject, stateChangeDTO.getObjectParam());
            }
        } catch (ClassNotFoundException e) {
            LogUtil.error(LogEnum.STATE_MACHINE, "未找到指定类： {} ", e);
        }
    }

    /**
     * 代理执行多个状态机的状态切换
     *
     * @param stateChangeDTOList 多个状态机切换信息
     * @param objectService  服务类
     */
    public static void proxyExecutionRelationState(List<StateChangeDTO> stateChangeDTOList,Object objectService) {
        if (!CollectionUtils.isEmpty(stateChangeDTOList)) {
            for (StateChangeDTO stateChangeDTO : stateChangeDTOList) {
                proxyExecutionSingleState(stateChangeDTO,objectService);
            }
        }
    }

    /**
     * 校验参数是否正常
     *
     * @param stateChangeDTO  数据集状态切换信息
     */
    public static void checkSingleParam(StateChangeDTO stateChangeDTO) {
        if (StringUtils.isEmpty(stateChangeDTO.getStateMachineType())) {
            throw new StateMachineException("未指定状态机类");
        }
        if (StringUtils.isEmpty(stateChangeDTO.getEventMethodName())) {
            throw new StateMachineException("未指定状态机需要执行事件");
        }
    }

    /**
     * 根据方法名获取所有参数的类型
     *
     * @param classInstance 类实例
     * @param methodName    方法名
     * @return List<Class<T>> 对象集合
     * @throws ClassNotFoundException
     */
    public static List<Class<T>> getMethodParamTypes(Object classInstance, String methodName) throws ClassNotFoundException {
        List<Class<T>> paramTypes = new ArrayList<>();
        Method[] methods = classInstance.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class<?>[] params = method.getParameterTypes();
                for (Class<?> classParamType : params) {
                    paramTypes.addAll(Collections.singleton((Class<T>) Class.forName(classParamType.getName())));
                }
                break;
            }
        }
        return paramTypes;
    }

}
