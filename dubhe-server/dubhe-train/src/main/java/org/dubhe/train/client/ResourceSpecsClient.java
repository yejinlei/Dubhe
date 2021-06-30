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
package org.dubhe.train.client;

import org.dubhe.biz.base.constant.ApplicationNameConst;
import org.dubhe.biz.base.dto.QueryResourceSpecsDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.QueryResourceSpecsVO;
import org.dubhe.train.client.fallback.ResourceSpecsClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @description 资源规格管理远程服务调用接口
 * @date 2021-06-02
 */
@FeignClient(value = ApplicationNameConst.SERVER_ADMIN, contextId = "resourceSpecsClient", fallback = ResourceSpecsClientFallback.class)
public interface ResourceSpecsClient {

    /**
     *  查询资源规格
     * @param queryResourceSpecsDTO 查询资源规格请求实体
     * @return QueryResourceSpecsVO 资源规格返回结果实体类
     */
    @GetMapping("/resourceSpecs/queryResourceSpecs")
    DataResponseBody<QueryResourceSpecsVO> queryResourceSpecs(@SpringQueryMap QueryResourceSpecsDTO queryResourceSpecsDTO);
}