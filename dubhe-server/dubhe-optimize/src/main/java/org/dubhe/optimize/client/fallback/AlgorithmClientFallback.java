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
package org.dubhe.optimize.client.fallback;

import org.dubhe.biz.base.dto.ModelOptAlgorithmCreateDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;
import org.dubhe.optimize.client.AlgorithmClient;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @description 算法管理远程调用熔断类
 * @date 2021-01-18
 */
public class AlgorithmClientFallback implements AlgorithmClient {

    /**
     * 模型优化上传算法熔断
     * @param modelOptAlgorithmCreateDTO 模型优化上传算法DTO
     * @return DataResponseBody 返回调用结果
     */
    @Override
    public DataResponseBody uploadAlgorithm(@RequestBody ModelOptAlgorithmCreateDTO modelOptAlgorithmCreateDTO) {
        return DataResponseFactory.failed("call dubhe-algorithm server selectById error");
    }

}
