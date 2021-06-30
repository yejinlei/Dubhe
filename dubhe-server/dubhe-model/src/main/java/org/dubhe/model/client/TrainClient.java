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
package org.dubhe.model.client;

import org.dubhe.biz.base.constant.ApplicationNameConst;
import org.dubhe.biz.base.dto.PtModelStatusQueryDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.model.client.fallback.TrainClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @description 训练管理远程服务调用接口
 * @date 2021-03-04
 */
@FeignClient(value = ApplicationNameConst.SERVER_TRAIN, contextId = "trainClient", fallback = TrainClientFallback.class)
public interface TrainClient {

    /**
     * 查询模型是否在训练中
     *
     * @param ptModelStatusQueryDTO 查询模型对应训练作业job状态参数
     * @return Boolean    模型是在使用（true：使用中；false：未使用）
     **/
    @GetMapping("/trainJob/trainModelStatus")
    DataResponseBody<Boolean> getTrainModelStatus(@SpringQueryMap PtModelStatusQueryDTO ptModelStatusQueryDTO);

}