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

package org.dubhe.biz.base.vo;


import lombok.Data;
import org.dubhe.biz.base.constant.ResponseCode;
import org.slf4j.MDC;

import java.io.Serializable;

/**
 * @description 统一的公共响应体
 * @date 2020-03-16
 */
@Data
public class DataResponseBody<T> implements Serializable {

    /**
     * 返回状态码
     */
    private Integer code;
    /**
     * 返回信息
     */
    private String msg;
    /**
     * 泛型数据
     */
    private T data;
    /**
     * 链路追踪ID
     */
    private String traceId;

    public DataResponseBody() {
        this(ResponseCode.SUCCESS, null);
    }

    public DataResponseBody(T data) {
        this(ResponseCode.SUCCESS, null, data);
    }

    public DataResponseBody(Integer code, String msg) {
        this(code, msg, null);
    }

    public DataResponseBody(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.traceId = MDC.get("traceId");
    }

    /**
     * 判断是否响应成功
     * @return ture 成功，false 失败
     */
    public boolean succeed(){
        return ResponseCode.SUCCESS.equals(this.code);
    }

}
