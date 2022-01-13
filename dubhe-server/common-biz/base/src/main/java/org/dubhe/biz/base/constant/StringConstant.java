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

import java.util.regex.Pattern;

/**
 * @description 字符串constant
 * @date 2020-05-14
 */
public final class StringConstant {

    public static final String MSIE = "MSIE";
    public static final String MOZILLA = "Mozilla";
    public static final String REQUEST_METHOD_GET = "GET";

    /**
     * 字母、数字、英文横杠和下划线匹配
     */
    public static final String REGEXP_NAME = "^[a-zA-Z0-9\\-\\_\\u4e00-\\u9fa5]+$";

    /**
     * 字母、数字、英文横杠、英文.号和下划线
     */
    public static final String REGEXP_TAG = "^[a-zA-Z0-9\\-\\_\\.]+$";

    /**
     * 算法名称支持字母、数字、汉字、英文横杠和下划线
     */
    public static final String REGEXP_ALGORITHM = "^[a-zA-Z0-9\\-\\_\\u4e00-\\u9fa5]+$";

    /**
     * 资源规格名称支持字母、数字、汉字、英文横杠、下划线和空白字符
     */
    public static final String REGEXP_SPECS = "^[a-zA-Z0-9\\-\\_\\s\\u4e00-\\u9fa5]+$";

    /**
     * 整数匹配
     */
    public static final Pattern PATTERN_NUM = Pattern.compile("^[-\\+]?[\\d]*$");
    /**
     * 数字匹配
     */
    public static final String NUMBER ="(\\d+)";
    /**
     * 整数匹配
     */
    public static final Pattern PATTERN_NUMBER  = Pattern.compile("(\\d+)");
    /**
     * 小数匹配
     */
    public static final Pattern PATTERN_DECIMAL = Pattern.compile("(\\d+\\.\\d+)");



    /**
     * 公共字段
     */
    public static final String CREATE_TIME = "createTime";
    public static final String UPDATE_TIME = "updateTime";
    public static final String UPDATE_USER_ID = "updateUserId";
    public static final String CREATE_USER_ID = "createUserId";
    public static final String ORIGIN_USER_ID = "originUserId";
    public static final String DELETED = "deleted";
    public static final String UTF8 = "utf-8";
    public static final String JSON_REQUEST = "application/json";
    public static final String CREATE_TIME_SQL = "create_time";
    public static final String UPDATE_TIME_SQL = "update_time";
    public static final String COMMON = "common";
    /**
     * k8s回调公共路径
     */
    public static final String K8S_CALLBACK_URI = "/api/k8s/callback/pod";
    /**
     * 资源回收远程调用路径
     */
    public static final String RECYCLE_CALL_URI = "/api/recycle/call/";
    public static final String K8S_CALLBACK_PATH_DEPLOYMENT = "/api/k8s/callback/deployment";
    public static final String MULTIPART = "multipart/form-data";

    public static final String PIP_SITE_PACKAGE ="pip-site-package";

    /**
     * 分页内容
     */
    public static final String RESULT = "result";
    /**
     * 排序规则
     */
    public static final String SORT_ASC = "asc";

    public static final String SORT_DESC = "desc";

    public static final String QUERY = "query";

    public static final String NGINX_LOWERCASE = "nginx";

    public static final String TRUE_LOWERCASE = "true";

    public static final String GRPC_CAPITALIZE = "GRPC";

    public static final String ID = "id";

    public static final String START_LOW = "start";
    public static final String END_LOW = "end";
    public static final String STEP_LOW = "step";

    /**
     * 任务缓存
     */
    public static final String CACHE_TASK_ID ="task_id";
    public static final String CACHE_TASK_NAME ="task_name";


    private StringConstant() {
    }
}
