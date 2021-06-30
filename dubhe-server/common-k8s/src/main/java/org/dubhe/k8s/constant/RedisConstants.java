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

package org.dubhe.k8s.constant;

import org.dubhe.biz.base.constant.MagicNumConstant;

/**
 * @description redis 常量
 * @date 2020-12-17
 */
public class RedisConstants {
    /**
     * 分布式锁过期时间
     */
    public static final Long DELAY_CUD_RESOURCE_EXPIRE_TIME = MagicNumConstant.TEN_LONG;
    /**
     * 分布式锁
     */
    public static final String DELAY_CUD_RESOURCE_KEY = "k8sclient:task:k8s_delay_cud_resource";
    /**
     * 延时启动队列
     */
    public static final String DELAY_APPLY_ZSET_KEY = "k8sclient:task:delay_apply_zset";
    /**
     * 延时停止队列
     */
    public static final String DELAY_STOP_ZSET_KEY = "k8sclient:task:delay_stop_zset";
    /**
     * 延时队列值
     */
    public static final String DELAY_ZSET_VALUE = "%s__%s";
}
