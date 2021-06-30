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

package org.dubhe.auth.exception;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

/**
 * @description 定义自已的异常处理类
 * @date 2020-12-21
 */
@JsonSerialize(using = CustomerOauthExceptionSerializer.class)
public class CustomerOauthException extends OAuth2Exception {
    public CustomerOauthException(String msg, Throwable t) {
        super(msg, t);
    }

    public CustomerOauthException(String msg) {
        super(msg);
    }

}
