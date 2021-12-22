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

import cn.hutool.core.util.StrUtil;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.ResultUtil;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.k8s.domain.bo.DistributeTrainBO;
import org.dubhe.k8s.domain.bo.PtJupyterJobBO;
import org.dubhe.train.config.TrainJobConfig;
import org.dubhe.train.domain.dto.BaseTrainJobDTO;
import org.dubhe.train.enums.TrainSystemRunParamEnum;
import org.dubhe.train.inner.TrainFileInnerService;

import javax.annotation.Resource;
import java.util.Objects;

import static org.dubhe.biz.base.constant.StringConstant.PYTHON_COMMAND_PATTERN;
import static org.dubhe.train.constant.TrainErrorConstant.CREATE_DIR_ERROR;

/**
 * @description  输出类系统参数handler
 * @date 2021-09-22
 */
public class SystemOutRunParamHandler implements SystemRunParamHandler {

    @Resource
    private TrainFileInnerService trainFileInnerService;

    @Resource
    private TrainJobConfig trainJobConfig;

    @Resource
    private UserContextService userContextService;

    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;

    public String buildSystemRunCommand(PtJupyterJobBO jobBo, DistributeTrainBO distributeTrainBO, BaseTrainJobDTO baseTrainJobDTO, boolean isTrainModelOut,
                                        boolean isTrainOut, boolean isVisualizedLog, String paramName, boolean needCreate) {
        Long userId = userContextService.getCurUserId();
        String commonPath = trainFileInnerService.buildTrainCommonPath(userId, baseTrainJobDTO.getJobName());
        String relativeCommonPath = trainFileInnerService.buildTrainRelativePath(userId, baseTrainJobDTO.getJobName());

        String pathValue = null, paramValue = null;

        switch (Objects.requireNonNull(TrainSystemRunParamEnum.valueOf(paramName))) {
            case train_model_out:
                if (isTrainModelOut) {
                    pathValue = trainJobConfig.getModelPath();
                    paramValue = trainJobConfig.getTrainModelPathValue();
                    baseTrainJobDTO.setTrainModelPath(relativeCommonPath + StrUtil.SLASH + pathValue);
                }
                break;
            case train_out:
                if (isTrainOut) {
                    pathValue = trainJobConfig.getOutPath();
                    paramValue = trainJobConfig.getTrainOutPathValue();
                    baseTrainJobDTO.setTrainOutPath(relativeCommonPath + StrUtil.SLASH + pathValue);
                }
                break;
            case train_visualized_log:
                if (isVisualizedLog) {
                    pathValue = trainJobConfig.getVisualizedLogPath();
                    paramValue = trainJobConfig.getVisualizedLogPathValue();
                    baseTrainJobDTO.setVisualizedLogPath(relativeCommonPath + StrUtil.SLASH + pathValue);
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + paramName);
        }

        if (StringUtils.isBlank(paramValue)) {
            return null;
        }

        if (needCreate) {
            boolean result = fileStoreApi.createDir(fileStoreApi.getRootDir() + commonPath + StrUtil.SLASH + pathValue);
            ResultUtil.isTrue(result, CREATE_DIR_ERROR);
        }

        String userParamName = buildParamName(paramName, baseTrainJobDTO.getRunParamsNameMap());
        return String.format(PYTHON_COMMAND_PATTERN, userParamName, paramValue);
    }
}
