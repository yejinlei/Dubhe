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
 * @description 用户常量类
 * @date 2020-06-01
 */
public class UserConstant {

    public final static String SEX_MEN = "男";

    public final static String SEX_WOMEN = "女";

    /**
     * redis key
     */
    /**
     * 用户发送邮件限制次数
     */
    public final static String USER_EMAIL_LIMIT_COUNT = "user:email:limit:count:";

    /**
     * 用户邮箱注册信息发送验证码
     */
    public final static String USER_EMAIL_REGISTER = "user:email:register:";

    /**
     * 用户邮箱激修改信息发送验证码
     */
    public final static String USER_EMAIL_UPDATE = "user:email:update:";

    /**
     * 用户邮箱忘记发送验证码
     */
    public final static String USER_EMAIL_RESET_PASSWORD = "user:email:reset-password:";

    /**
     * 用户其他操作发送验证码
     */
    public final static String USER_EMAIL_OTHER = "user:email:other:";

    /**
     * 一天的秒数 24x60x60
     */
    public final static long DATE_SECOND = 86400;

    /**
     * 发邮件次数
     */
    public final static int COUNT_SENT_EMAIL = 3;

    /**
     * 账号密码不正确登录失败次数
     */
    public final static int COUNT_LOGIN_FAIL = 5;

    /**
     * 用户登录限制次数
     */
    public final static String USER_LOGIN_LIMIT_COUNT = "user:login:limit:count:";

    /**
     * 初始化管理员ID
     */
    public final static Integer ADMIN_USER_ID = 1;

    /**
     * 管理员角色ID
     */
    public final static int ADMIN_ROLE_ID = 1;

    /**
     * 注册用户角色ID
     */
    public final static int REGISTER_ROLE_ID = 2;

    /**
     * 默认资源用户ID
     */
    public final static Long DEFAULT_ORIGIN_USER_ID = 0L;

    /**
     * 默认创建人ID
     */
    public final static Long DEFAULT_CREATE_USER_ID = 0L;

}
