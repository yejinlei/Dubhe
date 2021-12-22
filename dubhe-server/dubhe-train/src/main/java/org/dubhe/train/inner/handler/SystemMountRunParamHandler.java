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
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.k8s.domain.bo.DistributeTrainBO;
import org.dubhe.k8s.domain.bo.PtJupyterJobBO;
import org.dubhe.train.config.TrainJobConfig;
import org.dubhe.train.domain.dto.BaseTrainJobDTO;
import org.dubhe.train.enums.TrainSystemRunParamEnum;

import javax.annotation.Resource;
import java.util.Objects;

import static org.dubhe.biz.base.constant.StringConstant.PYTHON_COMMAND_PATTERN;

/**
 * @description  挂载类系统参数handler
 * @date 2021-09-22
 */
public class SystemMountRunParamHandler implements SystemRunParamHandler {

    @Resource
    private TrainJobConfig trainJobConfig;

    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;

    public String buildSystemRunCommand(PtJupyterJobBO jobBo, DistributeTrainBO distributeTrainBO, BaseTrainJobDTO baseTrainJobDTO, boolean isTrainModelOut,
                                        boolean isTrainOut, boolean isVisualizedLog, String paramName, boolean needCreate) {
        String dir = null, paramValue = null;

        switch (Objects.requireNonNull(TrainSystemRunParamEnum.valueOf(paramName))) {
            case data_url:
                if (StringUtils.isBlank(baseTrainJobDTO.getDataSourcePath())) {
                    break;
                }
                paramValue = trainJobConfig.getDockerDatasetPath();
                dir = fileStoreApi.getRootDir() + fileStoreApi.getBucket().substring(1) + baseTrainJobDTO.getDataSourcePath();
                break;
            case val_data_url:
                if (StringUtils.isBlank(baseTrainJobDTO.getValDataSourcePath())) {
                    break;
                }
                paramValue = trainJobConfig.getDockerValDatasetPath();
                dir = fileStoreApi.formatPath(fileStoreApi.getRootDir() + fileStoreApi.getBucket() + baseTrainJobDTO.getValDataSourcePath());
                break;
            case model_load_dir:
                if (StringUtils.isBlank(baseTrainJobDTO.getModelPath())) {
                    break;
                }
                paramValue = trainJobConfig.getDockerModelPath();
                dir = fileStoreApi.formatPath(fileStoreApi.getRootDir() + fileStoreApi.getBucket() + baseTrainJobDTO.getModelPath());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + Objects.requireNonNull(TrainSystemRunParamEnum.to(paramName)));
        }

        if (StringUtils.isBlank(paramValue)) {
            return null;
        }

        if (needCreate) {
            if (jobBo != null) {
                jobBo.putFsMounts(paramValue, dir);
            } else if (distributeTrainBO != null) {
                distributeTrainBO.putFsMounts(paramValue, dir);
            }
        }

        String userParamName = buildParamName(paramName, baseTrainJobDTO.getRunParamsNameMap());
        return String.format(PYTHON_COMMAND_PATTERN , userParamName, paramValue);

    }
}
