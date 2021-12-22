/**
 * Copyright 2020 Tianshu AI Platform. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =============================================================
 */

package org.dubhe.k8s.utils;

import io.fabric8.kubernetes.api.model.Quantity;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @description 通用工具类
 * @date 2021-09-06
 */
@Component
public class K8sCommonUtils {
    @Value("${rdma.enable}")
    private Boolean rdmaEnable;

    /**
     * 添加Rdma资源
     *
     * @param resourcesMap 资源Map
     */
    public void addRdmaResource(Map<String, Quantity> resourcesMap) {
        if (rdmaEnable && resourcesMap != null) {
            resourcesMap.put(K8sParamConstants.RDMA_HCA_RESOURCE_KEY, new Quantity(String.valueOf(MagicNumConstant.ONE)));
        }
    }

    /**
     * 获取rdma资源
     *
     * @return Map<String, Quantity> 资源Map
     */
    public Map<String, Quantity> getRdmaResource() {
        if (rdmaEnable) {
            return new HashMap<String, Quantity>() {
                {
                    put(K8sParamConstants.RDMA_HCA_RESOURCE_KEY, new Quantity(String.valueOf(MagicNumConstant.ONE)));
                }
            };
        }
        return null;
    }
}
