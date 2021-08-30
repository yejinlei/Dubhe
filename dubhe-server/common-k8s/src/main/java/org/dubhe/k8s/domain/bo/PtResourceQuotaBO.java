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

package org.dubhe.k8s.domain.bo;

import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.k8s.annotation.K8sValidation;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.resource.BizQuantity;
import org.dubhe.k8s.domain.resource.BizScopedResourceSelectorRequirement;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.k8s.enums.ValidationTypeEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description ResourceQuota BO
 * @date 2020-04-23
 */
@Data
@Accessors(chain = true)
public class PtResourceQuotaBO {
    @K8sValidation(ValidationTypeEnum.K8S_RESOURCE_NAME)
    private String namespace;
    @K8sValidation(ValidationTypeEnum.K8S_RESOURCE_NAME)
    private String name;
    private Map<String, BizQuantity> hard;
    private List<BizScopedResourceSelectorRequirement> scopeSelector;

    /**
     * 添加cpu 限制
     * @param amount 值
     * @param format 单位
     */
    public void addCpuLimitsHard(String amount,String format){
        if (hard == null){
            hard = new HashMap<>();
        }
        hard.put(K8sParamConstants.RESOURCE_QUOTA_CPU_LIMITS_KEY,new BizQuantity(amount,format));
    }

    /**
     * 添加memory限制
     * @param amount 值
     * @param format 单位
     */
    public void addMemoryLimitsHard(String amount,String format){
        if (hard == null){
            hard = new HashMap<>();
        }
        hard.put(K8sParamConstants.RESOURCE_QUOTA_MEMORY_LIMITS_KEY,new BizQuantity(amount,format));
    }

    /**
     * 添加gpu 限制
     * @param amount 值
     */
    public void addGpuLimitsHard(String amount){
        if (hard == null){
            hard = new HashMap<>();
        }
        hard.put(K8sParamConstants.RESOURCE_QUOTA_GPU_LIMITS_KEY,new BizQuantity(amount, SymbolConstant.BLANK));
    }
}
