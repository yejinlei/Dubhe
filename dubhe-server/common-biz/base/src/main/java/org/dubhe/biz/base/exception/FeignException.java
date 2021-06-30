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

package org.dubhe.biz.base.exception;

import lombok.Getter;
import org.dubhe.biz.base.constant.ResponseCode;
import org.dubhe.biz.base.vo.DataResponseBody;

/**
 * @description 远程调用异常
 * @date 2020-11-26
 */
@Getter
public class FeignException extends RuntimeException {

    private DataResponseBody responseBody;

    public FeignException(String msg) {
        super(msg);
        this.responseBody = new DataResponseBody(ResponseCode.BADREQUEST, msg);
    }

    public FeignException(String msg, Throwable cause) {
        super(msg,cause);
        this.responseBody = new DataResponseBody(ResponseCode.BADREQUEST, msg);
    }

    public FeignException(Throwable cause) {
        super(cause);
        this.responseBody = new DataResponseBody(ResponseCode.BADREQUEST);
    }

    public FeignException(Integer code, String msg, String info, Throwable cause) {
        super(msg,cause);
        if (info == null) {
            this.responseBody = new DataResponseBody(code, msg);
        } else {
            this.responseBody = new DataResponseBody(code, msg + ":" + info);
        }
    }

    public FeignException(ErrorCode errorCode, Throwable cause) {
        this(errorCode.getCode(), errorCode.getMsg(), null, cause);
    }

    public FeignException(ErrorCode errorCode, String info, Throwable cause) {
        this(errorCode.getCode(), errorCode.getMsg(), info, cause);
    }

    public FeignException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public FeignException(Integer code, String msg) {
        this.responseBody = new DataResponseBody(code, msg);
    }
}
