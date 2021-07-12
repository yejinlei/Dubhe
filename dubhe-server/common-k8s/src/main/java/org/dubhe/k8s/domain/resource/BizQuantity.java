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
import org.dubhe.k8s.annotation.K8sField;

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

    public boolean isIllegal() {
        return true;
    }

    /**
     * 单位相同时相减
     * @param bizQuantity 减数
     * @return
     */
    public BizQuantity reduce(BizQuantity bizQuantity){
        if (bizQuantity == null || !bizQuantity.getFormat().equals(format)){
            return this;
        }
        return new BizQuantity(MathUtils.reduce(amount,bizQuantity.getAmount()),format);
    }
}