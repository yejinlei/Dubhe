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

package org.dubhe.exception;

import lombok.Getter;
import org.dubhe.base.DataResponseBody;
import org.dubhe.base.ResponseCode;

/**
 * @description 权限异常
 * @date 2020-02-23
 */
@Getter
public class UnauthorizedException extends RuntimeException {

    private DataResponseBody responseBody;
    private Throwable cause;

    public UnauthorizedException(String msg) {
        this.responseBody = new DataResponseBody(ResponseCode.UNAUTHORIZED, msg);
    }

    public UnauthorizedException(String msg, Throwable cause) {
        this.cause = cause;
        this.responseBody = new DataResponseBody(ResponseCode.UNAUTHORIZED, msg);
    }

    public UnauthorizedException(Throwable cause) {
        this.cause = cause;
        this.responseBody = new DataResponseBody(ResponseCode.UNAUTHORIZED);
    }
}
