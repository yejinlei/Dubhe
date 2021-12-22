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
package org.dubhe.admin.client.fallback;

import org.dubhe.admin.client.SystemNodeClient;
import org.dubhe.biz.base.dto.QueryUserK8sResourceDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.QueryUserResourceSpecsVO;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;

import java.util.List;

/**
 * @description SystemNodeClient 熔断处理
 * @date 2021-9-7
 */
public class SystemNodeClientFallback implements SystemNodeClient {

    @Override
    public DataResponseBody<List<QueryUserResourceSpecsVO>> queryUserK8sResource(List<QueryUserK8sResourceDTO> queryUserK8sResources) {
        return DataResponseFactory.failed("Call SystemNode server queryUserK8sResource error");
    }
}
