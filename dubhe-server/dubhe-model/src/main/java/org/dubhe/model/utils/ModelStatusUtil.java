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
package org.dubhe.model.utils;

import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.dto.PtModelStatusQueryDTO;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.model.client.BatchServingClient;
import org.dubhe.model.client.ModelOptTaskInstanceClient;
import org.dubhe.model.client.ServingClient;
import org.dubhe.model.client.TrainClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description 验证模型使用状态
 * @date 2021-03-11
 */
@Component
public class ModelStatusUtil {
    @Autowired
    private TrainClient trainClient;

    @Autowired
    private ModelOptTaskInstanceClient modelOptTaskInstanceClient;

    @Autowired
    private BatchServingClient batchServingClient;

    @Autowired
    private ServingClient servingClient;

    /**
     * 删除时远程调用查询关联业务模块是否有正在使用该模型
     * 该方法查询关联业务模块是否有正在使用该模型，如果有异常（如用户未部署其关联业务模块，网络故障等异常情况），则继续删除
     * @param ids 模型id集合
     */
    public void queryModelStatus(UserContext user, PtModelStatusQueryDTO ptModelStatusQueryDTO, List<Long> ids) {
        //查询模型是否在训练中
        DataResponseBody<Boolean> trainModelBranchStatus = null;
        try {
            trainModelBranchStatus = trainClient.getTrainModelStatus(ptModelStatusQueryDTO);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_MODEL, "train:Remote call exception while user {} deleting model. Delete exception according to ID array {}，The exception is:{}", user.getUsername(), ids, e);
        }
        if (trainModelBranchStatus != null && trainModelBranchStatus.succeed()) {
            Boolean trainStatus = trainModelBranchStatus.getData();
            if (trainStatus) {
                throw new BusinessException("该模型处于训练中，不可删除");
            }
        }
        //查询模型是否在模型优化中
        DataResponseBody<Boolean> optimizeModelBranchStatus = null;
        try {
            optimizeModelBranchStatus = modelOptTaskInstanceClient.getOptimizeModelStatus(ptModelStatusQueryDTO);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_MODEL, "model-optimize:Remote call exception while user {} deleting model. Delete exception according to ID array {}，The exception is:{}", user.getUsername(), ids, e);
        }
        if (optimizeModelBranchStatus != null && optimizeModelBranchStatus.succeed()) {
            Boolean trainStatus = optimizeModelBranchStatus.getData();
            if (trainStatus) {
                throw new BusinessException("该模型处于模型优化中，不可删除");
            }
        }
        //查询模型是否在模型部署中-在线服务
        DataResponseBody<Boolean> servingModelBranchStatus = null;
        try {
            servingModelBranchStatus = servingClient.getServingModelStatus(ptModelStatusQueryDTO);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_MODEL, "serving:Remote call exception while user {} deleting model. Delete exception according to ID array {}，The exception is:{}", user.getUsername(), ids, e);
        }
        if (servingModelBranchStatus != null && servingModelBranchStatus.succeed()) {
            Boolean trainStatus = servingModelBranchStatus.getData();
            if (trainStatus) {
                throw new BusinessException("该模型处于模型部署中-在线服务，不可删除");
            }
        }
        //查询模型是否在模型部署中-批量服务
        DataResponseBody<Boolean> batchServingModelBranchStatus = null;
        try {
            batchServingModelBranchStatus = batchServingClient.getServingModelStatus(ptModelStatusQueryDTO);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_MODEL, "serving:Remote call exception while user {} deleting model. Delete exception according to ID array {}，The exception is:{}", user.getUsername(), ids, e);
        }
        if (batchServingModelBranchStatus != null && batchServingModelBranchStatus.succeed()) {
            Boolean trainStatus = batchServingModelBranchStatus.getData();
            if (trainStatus) {
                throw new BusinessException("该模型处于模型部署中-批量服务，不可删除");
            }
        }
    }

}