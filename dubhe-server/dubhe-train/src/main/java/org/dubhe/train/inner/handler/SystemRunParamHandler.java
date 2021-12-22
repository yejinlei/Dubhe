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

package org.dubhe.train.inner.handler;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.MapUtils;
import org.dubhe.biz.base.utils.MapUtil;
import org.dubhe.k8s.domain.bo.DistributeTrainBO;
import org.dubhe.k8s.domain.bo.PtJupyterJobBO;
import org.dubhe.train.domain.dto.BaseTrainJobDTO;
import org.dubhe.train.enums.TrainSystemRunParamEnum;

import java.util.Map;


/**
 * @description  系统运行参数处理接口
 * @date 2021-09-22
 */
public interface SystemRunParamHandler {
    /**
     * 构造系统运行命令参数
     *
     * @param jobBo
     * @param baseTrainJobDTO
     * @param paramName
     * @return
     */
    String buildSystemRunCommand(PtJupyterJobBO jobBo, DistributeTrainBO distributeTrainBO, BaseTrainJobDTO baseTrainJobDTO, boolean isTrainModelOut,
                                 boolean isTrainOut, boolean isVisualizedLog, String paramName, boolean needCreate);


    /**
     * 获取最终的系统参数名称
     *
     * @param defaultParamName
     * @param runParamsNameMap
     * @return
     */
    default String buildParamName(String defaultParamName, JSONObject runParamsNameMap) {
        String userParamName = defaultParamName;
        Map<String, String> paramNameMap = MapUtil.convertJsonObject(runParamsNameMap);
        String inputParamName = TrainSystemRunParamEnum.valueOf(defaultParamName).getInputParam();
        if (MapUtils.isNotEmpty(paramNameMap) && paramNameMap.containsKey(inputParamName)) {
            userParamName = paramNameMap.get(inputParamName);
        }
        return userParamName;
    }
}
