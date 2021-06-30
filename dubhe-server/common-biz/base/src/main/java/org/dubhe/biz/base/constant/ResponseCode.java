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

package org.dubhe.biz.base.constant;

/**
 * @description  业务返回状态码
 * @date 2020-02-23
 */
public class ResponseCode {
    public static Integer SUCCESS = 200;
    public static Integer UNAUTHORIZED = 401;
    public static Integer TOKEN_ERROR = 403;
    public static Integer ERROR = 10000;
    public static Integer ENTITY_NOT_EXIST = 10001;
    public static Integer BADREQUEST = 10002;
    public static Integer SERVICE_ERROR = 10003;
    public static Integer DOCKER_ERROR = 10004;

}
