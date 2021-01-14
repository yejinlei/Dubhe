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

package org.dubhe.k8s.domain;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.enums.K8sResponseEnum;

import java.io.Serializable;

/**
 * @description base result
 * @date 2020-04-23
 */
@Data
@Accessors(chain = true)
public class PtBaseResult<T extends PtBaseResult> implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 错误码 符合kubernertes错误码
     **/
    private String code = "200";
    /**
     * 错误信息
     **/
    private String message;

    public PtBaseResult() {
    }

    public PtBaseResult(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public T error(String code, String message) {
        this.code = code;
        this.message = message;
        return (T) this;
    }

    public T errorBadRequest() {
        this.code = K8sResponseEnum.BAD_REQUEST.getCode();
        this.message = K8sResponseEnum.BAD_REQUEST.getMessage();
        return (T) this;
    }

    public T validationErrorRequest(String fieldName) {
        this.code = K8sResponseEnum.PRECONDITION_FAILED.getCode();
        this.message = StrUtil.format("{} 字段校验失败,k8s资源命名规则必须符合 {},且长度不超过 {}", fieldName, K8sParamConstants.K8S_RESOURCE_NAME_REGEX, K8sParamConstants.RESOURCE_NAME_LENGTH);
        return (T) this;
    }

    public T baseErrorBadRequest() {
        this.code = K8sResponseEnum.BAD_REQUEST.getCode();
        this.message = K8sResponseEnum.BAD_REQUEST.getMessage();
        return (T) this;
    }

    public boolean isSuccess() {
        return K8sResponseEnum.SUCCESS.getCode().equals(code);
    }
}
