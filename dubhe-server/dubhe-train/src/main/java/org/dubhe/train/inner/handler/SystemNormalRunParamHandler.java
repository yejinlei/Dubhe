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

import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.k8s.domain.bo.DistributeTrainBO;
import org.dubhe.k8s.domain.bo.PtJupyterJobBO;
import org.dubhe.train.domain.dto.BaseTrainJobDTO;
import org.dubhe.train.enums.ResourcesPoolTypeEnum;
import org.dubhe.train.enums.TrainSystemRunParamEnum;


import static org.dubhe.biz.base.constant.StringConstant.PYTHON_COMMAND_PATTERN;
import static org.dubhe.train.enums.TrainSystemRunParamEnum.gpu_num_per_node;

/**
 * @description TODO
 * @date 2021-09-27
 */
public class SystemNormalRunParamHandler implements SystemRunParamHandler {
    @Override
    public String buildSystemRunCommand(PtJupyterJobBO jobBo, DistributeTrainBO distributeTrainBO, BaseTrainJobDTO baseTrainJobDTO,
                                        boolean isTrainModelOut, boolean isTrainOut, boolean isVisualizedLog, String paramName, boolean needCreate) {
        String paramValue = "";
        TrainSystemRunParamEnum paramEnum = TrainSystemRunParamEnum.valueOf(paramName);
        if (gpu_num_per_node.equals(paramEnum) && ResourcesPoolTypeEnum.isGpuCode(baseTrainJobDTO.getResourcesPoolType())) {
            paramValue = baseTrainJobDTO.getGpuNum().toString();
        }
        if (StringUtils.isBlank(paramValue)) {
            return null;
        }
        String userParamName = buildParamName(paramName, baseTrainJobDTO.getRunParamsNameMap());
        return String.format(PYTHON_COMMAND_PATTERN, userParamName, paramValue);
    }
}