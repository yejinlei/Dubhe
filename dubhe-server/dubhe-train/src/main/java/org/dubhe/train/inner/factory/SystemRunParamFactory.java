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

package org.dubhe.train.inner.factory;

import lombok.Setter;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.train.enums.TrainSystemRunParamEnum;
import org.dubhe.train.inner.handler.SystemRunParamHandler;

import java.util.Map;

/**
 * @description  系统运行参数处理工厂类
 * @date 2021-09-22
 */
public class SystemRunParamFactory {

    @Setter
    private Map<String, SystemRunParamHandler> systemRunParamHandlerMap;

    /**
     * 根据系统参数获取相应的handler
     *
     * @param systemParam
     * @return
     */
    public SystemRunParamHandler getHandler(TrainSystemRunParamEnum systemParam) {
        if (StringUtils.isBlank(systemParam.getParamType())) {
            return null;
        }
        SystemRunParamHandler systemRunParamHandler =  systemRunParamHandlerMap.get(systemParam.getParamType());
        if (systemRunParamHandler == null) {
            throw new BusinessException("不支持的系统参数类型");
        }
        return systemRunParamHandler;
    }
}
