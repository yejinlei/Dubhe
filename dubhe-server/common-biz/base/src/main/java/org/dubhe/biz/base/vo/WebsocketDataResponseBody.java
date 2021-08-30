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
 * @description Websocket 统一的公共响应体
 * @date 2021-07-20
 */
@Data
public class WebsocketDataResponseBody<T> implements Serializable {

    /**
     * 返回状态码
     */
    private Integer code;
    /**
     * 返回信息
     */
    private String msg;
    /**
     * 返回主题
     */
    private String topic;
    /**
     * 泛型数据
     */
    private T data;
    /**
     * 链路追踪ID
     */
    private String traceId;

    public WebsocketDataResponseBody() {
        this(ResponseCode.SUCCESS, null,null);
    }


    public WebsocketDataResponseBody(String topic, T data) {
        this(ResponseCode.SUCCESS, null, topic, data);
    }

    public WebsocketDataResponseBody(Integer code, String msg, String topic) {
        this(code, msg, topic, null);
    }

    public WebsocketDataResponseBody(Integer code, String msg, String topic, T data) {
        this.code = code;
        this.msg = msg;
        this.topic = topic;
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
