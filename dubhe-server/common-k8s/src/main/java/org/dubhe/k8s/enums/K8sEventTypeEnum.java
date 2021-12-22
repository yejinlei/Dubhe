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

import lombok.Getter;
import lombok.Setter;
import org.dubhe.biz.base.constant.SymbolConstant;

/**
 * @description:
 * @date: 2021/11/15
 */
public enum K8sEventTypeEnum {
    OOMKilled("OOMKilled", "请检查内存配置是否满足运行要求"),
    COMPLETED("Completed", "pod正常终止事件"),
    ;

    /**
     * k8s callback回调的reason
     */
    @Getter
    @Setter
    private String reason;

    @Getter
    @Setter
    private String message;

    K8sEventTypeEnum(String reason, String message) {
        this.reason = reason;
        this.message = message;
    }

    /**
     * 由event的reason获取类型enum
     * @param reason
     * @return
     */
    public static K8sEventTypeEnum to(String reason) {
        for (K8sEventTypeEnum type : K8sEventTypeEnum.values()) {
            if (type.getReason().equals(reason)) {
                return type;
            }
        }
        return K8sEventTypeEnum.COMPLETED;
    }

    /**
     * 构造完整的消息
     * @param typeEnum
     * @return
     */
    public static String buildMessage(K8sEventTypeEnum typeEnum) {
        return typeEnum.getReason() + SymbolConstant.COLON + SymbolConstant.SPACE + typeEnum.getMessage();
    }
}
