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
import org.dubhe.model.client.fallback.BatchServingClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @description 云端Serving：批量服务管理远程服务调用接口
 * @date 2021-03-09
 */
@FeignClient(value = ApplicationNameConst.SERVER_SERVING, contextId = "batchServingClient", fallback = BatchServingClientFallback.class)
public interface BatchServingClient {

    /**
     *  获取模型是否在使用
     * @param ptModelStatusQueryDTO 查询模型状态DTO
     * @return Boolean 是否在用（true：使用中；false：未使用）
     */
    @GetMapping("/batchServices/getModelStatus")
    DataResponseBody<Boolean> getServingModelStatus(@SpringQueryMap PtModelStatusQueryDTO ptModelStatusQueryDTO);
}