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
 * @description  验证码异常
 * @date 2020-02-23
 */
@Getter
public class CaptchaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private DataResponseBody responseBody;
    private Throwable cause;

    public CaptchaException(String msg) {
        this.responseBody = new DataResponseBody(ResponseCode.BADREQUEST, msg);
    }

    public CaptchaException(String msg, Throwable cause) {
        this.cause = cause;
        this.responseBody = new DataResponseBody(ResponseCode.BADREQUEST, msg);
    }

    public CaptchaException(Throwable cause) {
        this.cause = cause;
        this.responseBody = new DataResponseBody(ResponseCode.BADREQUEST);
    }
}
