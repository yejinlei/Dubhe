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

package org.dubhe.k8s.domain.resource;

import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.utils.MathUtils;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.k8s.annotation.K8sField;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.utils.UnitConvertUtils;

/**
 * @description BizQuantity实体类
 * @date 2020-04-22
 */
@Data
@Accessors(chain = true)
public class BizQuantity {
    @K8sField("amount")
    private String amount;
    @K8sField("format")
    private String format;

    public BizQuantity() {

    }

    public BizQuantity(String amount, String format) {
        this.amount = amount;
        this.format = format;
    }

    /**
     * 不同单位相减
     *
     * @param bizQuantity 减数
     * @param limitsKey 类型
     * @return BizQuantity
     */
    public BizQuantity reduce(BizQuantity bizQuantity,String limitsKey){
        if (bizQuantity == null || StringUtils.isAllEmpty(limitsKey)){
            return this;
        }
        switch (limitsKey){
            case K8sParamConstants.RESOURCE_QUOTA_CPU_LIMITS_KEY :
                Long cpuDiff = UnitConvertUtils.cpuFormatToN(amount,format) - UnitConvertUtils.cpuFormatToN(bizQuantity.getAmount(),bizQuantity.getFormat());
                return new BizQuantity(String.valueOf(cpuDiff),K8sParamConstants.CPU_UNIT_N);
            case K8sParamConstants.RESOURCE_QUOTA_MEMORY_LIMITS_KEY :
                Long memDiff = UnitConvertUtils.memFormatToMi(amount,format) - UnitConvertUtils.memFormatToMi(bizQuantity.getAmount(),bizQuantity.getFormat());
                return new BizQuantity(String.valueOf(memDiff),K8sParamConstants.MEM_UNIT);
            case K8sParamConstants.RESOURCE_QUOTA_GPU_LIMITS_KEY :
                return new BizQuantity(MathUtils.reduce(amount,bizQuantity.getAmount()),format);
            default:
                return this;
        }
    }
}
