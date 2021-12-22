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
package org.dubhe.tadl.client;

import org.dubhe.biz.base.constant.ApplicationNameConst;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.QueryResourceSpecsVO;
import org.dubhe.tadl.client.fallback.AdminServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @description feign调用demo
 * @date 2020-11-04
 */
@FeignClient(value = ApplicationNameConst.SERVER_ADMIN, fallback = AdminServiceFallback.class)
public interface AdminServiceClient {

    /**
     * 获取资源规格信息
     *
     * @param id 资源ID
     * @return 资源信息
     */
    @GetMapping(value = "/resourceSpecs/queryTadlResourceSpecs")
    DataResponseBody<QueryResourceSpecsVO> queryTadlResourceSpecs(@RequestParam("id")Long id);


}
