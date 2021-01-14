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

import org.dubhe.k8s.domain.PtBaseResult;

/**
 * @description K8s response enum
 * @date 2020-05-09
 */
public enum K8sResponseEnum {
    /**
     * 成功
     */
    SUCCESS("200", ""),

    /**
     * 没有对应的资源
     */
    NOT_FOUND("403", "请求的资源不存在"),
    /**
     * 创建，删除等操作的重复提交
     */
    REPEAT("404", "重复提交"),
    /**
     * 资源已存在
     */
    EXISTS("409", "资源已存在"),
    /**
     * 参数缺失
     */
    BAD_REQUEST("400", "参数缺失"),
    /**
     * 先决条件错误
     */
    PRECONDITION_FAILED("412", "先决条件错误"),

    /**
     * k8s-client 内部错误
     */
    INTERNAL_SERVER_ERROR("500", "内部错误"),
    /**
     * 资源不足
     */
    LACK_OF_RESOURCES("516", "资源不足"),
    ;

    K8sResponseEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    private String code;
    private String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public PtBaseResult toPtBaseResult() {
        return new PtBaseResult(this.code, this.message);
    }
}
