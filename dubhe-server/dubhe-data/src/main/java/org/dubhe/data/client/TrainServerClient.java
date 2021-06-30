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
package org.dubhe.data.client;

import org.dubhe.biz.base.constant.ApplicationNameConst;
import org.dubhe.biz.base.dto.PtTrainDataSourceStatusQueryDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.data.client.fallback.TrainServerFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * @description feign调用训练服务接口
 * @date 2020-11-04
 */
@FeignClient(value = ApplicationNameConst.SERVER_TRAIN, fallback = TrainServerFallback.class)
public interface TrainServerClient {

    /**
     * 数据集状态展示
     *
     * @param ptTrainDataSourceStatusQueryDTO 查询数据集对应训练状态查询条件
     * @return
     */
    @GetMapping("/trainJob/dataSourceStatus")
    public DataResponseBody<Map<String, Boolean>> getTrainDataSourceStatus(@Validated @SpringQueryMap PtTrainDataSourceStatusQueryDTO ptTrainDataSourceStatusQueryDTO);

}
