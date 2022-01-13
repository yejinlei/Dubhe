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

package org.dubhe.k8s.enums;

import org.dubhe.biz.base.constant.ApplicationNameConst;
import org.dubhe.biz.base.enums.BizEnum;
import org.dubhe.biz.base.utils.StringUtils;

import static org.dubhe.biz.base.constant.SymbolConstant.BLANK;

/**
 * @description 业务标签->服务名 映射枚举类
 * @date 2020-12-8
 */
public enum BusinessLabelServiceNameEnum {
    /**
     * 模型开发
     */
    NOTEBOOK(BizEnum.NOTEBOOK.getBizCode(), ApplicationNameConst.SERVER_NOTEBOOK),
    /**
     * 训练管理
     */
    TRAIN(BizEnum.ALGORITHM.getBizCode(), ApplicationNameConst.SERVER_TRAIN),
    /**
     * 模型优化
     */
    MODEL_OPTIMIZE(BizEnum.MODEL_OPT.getBizCode(), ApplicationNameConst.SERVER_OPTIMIZE),
    /**
     * 云端Serving
     */
    SERVING(BizEnum.SERVING.getBizCode(), ApplicationNameConst.SERVER_SERVING),
    /**
     * 批量服务
     */
    BATCH_SERVING(BizEnum.BATCH_SERVING.getBizCode(), ApplicationNameConst.SERVER_SERVING),
    /**
     * 专业版终端
     */
    TERMINAL(BizEnum.TERMINAL.getBizCode(), ApplicationNameConst.TERMINAL),
    /**
     * TADL
     */
    TADL(BizEnum.TADL.getBizCode(), ApplicationNameConst.SERVER_TADL);
    /**
     * 业务标签
     */
    private String businessLabel;
    /**
     * 服务名
     */
    private String serviceName;

    public String getBusinessLabel() {
        return businessLabel;
    }

    public String getServiceName() {
        return serviceName;
    }

    BusinessLabelServiceNameEnum(String businessLabel, String serviceName) {
        this.businessLabel = businessLabel;
        this.serviceName = serviceName;
    }

    public static String getServiceNameByBusinessLabel(String businessLabel) {
        for (BusinessLabelServiceNameEnum businessLabelServiceNameEnum : BusinessLabelServiceNameEnum.values()) {
            if (StringUtils.equals(businessLabel, businessLabelServiceNameEnum.getBusinessLabel())) {
                return businessLabelServiceNameEnum.getServiceName();
            }
        }
        return BLANK;
    }

    public static String getBusinessLabelByServiceName(String serviceName) {
        for (BusinessLabelServiceNameEnum businessLabelServiceNameEnum : BusinessLabelServiceNameEnum.values()) {
            if (StringUtils.equals(serviceName, businessLabelServiceNameEnum.getServiceName())) {
                return businessLabelServiceNameEnum.getBusinessLabel();
            }
        }
        return BLANK;
    }
}
