/**
 * Copyright 2020 Tianshu AI Platform. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =============================================================
 */

package org.dubhe.train.inner.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.train.config.TrainJobConfig;
import org.dubhe.train.domain.dto.BaseTrainJobDTO;
import org.dubhe.train.enums.ResourcesPoolTypeEnum;
import org.dubhe.train.enums.TrainSystemRunParamEnum;
import org.dubhe.train.inner.RunCommandInnerService;
import org.dubhe.train.inner.factory.SystemRunParamFactory;
import org.dubhe.train.inner.handler.SystemRunParamHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description 训练命令内部服务实现类
 * @date 2021-09-22
 */
@Component
public class RunCommandInnerServiceImpl implements RunCommandInnerService {
    @Resource
    private TrainJobConfig trainJobConfig;

    @Resource
    private SystemRunParamFactory systemRunParamFactory;

    @Override
    public String buildDisplayRunCommand(BaseTrainJobDTO baseTrainJobDTO, boolean isTrainModelOut,
                                         boolean isTrainOut, boolean isVisualizedLog, String runCommand) {
        StringBuilder sb = new StringBuilder();
        sb.append(runCommand);
        // 拼接out,log和dataset
        String pattern = trainJobConfig.getPythonFormat();

        for (TrainSystemRunParamEnum systemRunParamEnum : TrainSystemRunParamEnum.values()) {
            SystemRunParamHandler systemRunParamHandler = systemRunParamFactory.getHandler(systemRunParamEnum);
            if (systemRunParamHandler == null) {
                continue;
            }
            String param = systemRunParamHandler.buildSystemRunCommand(null, null, baseTrainJobDTO, isTrainModelOut,
                    isTrainOut, isVisualizedLog, systemRunParamEnum.name(), false);
            if (StringUtils.isNotBlank(param)) {
                sb.append(param);
            }
        }

        JSONObject runParams = baseTrainJobDTO.getRunParams();
        if (null != runParams && !runParams.isEmpty()) {
            runParams.forEach((k, v) ->
                    sb.append(pattern).append(k).append(SymbolConstant.FLAG_EQUAL).append(v).append(StrUtil.SPACE)
            );
        }
        // 在用户自定以参数拼接晚后拼接固定参数，防止被用户自定义参数覆盖
        if (ResourcesPoolTypeEnum.isGpuCode(baseTrainJobDTO.getResourcesPoolType()) && baseTrainJobDTO.getGpuNum() != null) {
            // 需要GPU
            sb.append(pattern).append(trainJobConfig.getGpuNumPerNode()).append(SymbolConstant.FLAG_EQUAL).append(baseTrainJobDTO.getGpuNum()).append(StrUtil.SPACE);
        }
        return sb.toString();
    }
}
