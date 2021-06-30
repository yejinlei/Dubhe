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
 * @description 元数据枚举
 * @date 2020-11-26
 */
public final class MetaHandlerConstant {

    /**
     * 创建时间字段
     */
    public static final String CREATE_TIME = "createTime";
    /**
     *更新时间字段
     */
    public static final String UPDATE_TIME = "updateTime";
    /**
     *更新人id字段
     */
    public static final String UPDATE_USER_ID = "updateUserId";
    /**
     *创建人id字段
     */
    public static final String CREATE_USER_ID = "createUserId";
    /**
     *资源拥有人id字段
     */
    public static final String ORIGIN_USER_ID = "originUserId";
    /**
     *删除字段
     */
    public static final String DELETED = "deleted";

    private MetaHandlerConstant() {
    }
}
