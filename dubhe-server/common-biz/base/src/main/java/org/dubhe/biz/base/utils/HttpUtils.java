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

package org.dubhe.biz.base.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * @description HttpUtil
 * @date 2020-04-30
 */
@Slf4j
public class HttpUtils {

    private HttpUtils() {

    }

    /**
     *  判断http请求是否成功
     *
     * @param httpCode
     *      1XX Informational(信息性状态码)
     *      2XX Success(成功状态码)
     *      3XX Redirection(重定向状态码)
     *      4XX Client Error(客户端错误状态码)
     *      5XX Server Error(服务器错误状态码)
     * @return
     */
    public static boolean isSuccess(String httpCode){
        if (StringUtils.isBlank(httpCode)){
            return false;
        }
        return httpCode.length() == 3 && httpCode.startsWith("2");
    }

    /**
     *  判断http请求是否成功
     *
     * @param httpCode
     *      1XX Informational(信息性状态码)
     *      2XX Success(成功状态码)
     *      3XX Redirection(重定向状态码)
     *      4XX Client Error(客户端错误状态码)
     *      5XX Server Error(服务器错误状态码)
     * @return
     */
    public static boolean isSuccess(int httpCode) {
        return isSuccess(String.valueOf(httpCode));
    }

}
