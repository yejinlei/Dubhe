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
package org.dubhe.tadl.client.fallback;


import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.QueryResourceSpecsVO;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;
import org.dubhe.tadl.client.AdminServiceClient;
import org.springframework.stereotype.Component;

/**
 * @description Feign 熔断处理类
 * @date 2020-11-04
 */
@Component
public class AdminServiceFallback implements AdminServiceClient {


    /**
     * 获取资源规格信息
     *
     * @param id 资源ID
     * @return 资源信息
     */
    @Override
    public DataResponseBody<QueryResourceSpecsVO> queryTadlResourceSpecs(Long id) {
        return DataResponseFactory.failed("call admin server queryTadlResourceSpecs error ");
    }

}
