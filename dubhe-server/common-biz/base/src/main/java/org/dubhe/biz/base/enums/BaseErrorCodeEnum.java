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

package org.dubhe.biz.base.enums;

import lombok.Getter;
import org.dubhe.biz.base.exception.ErrorCode;

/**
 * @description 通用异常code
 * @date 2020-11-26
 */
@Getter
public enum BaseErrorCodeEnum implements ErrorCode {

    /**
     * undefined error
     */
    UNDEFINED(10000, "操作成功!"),
    ERROR(10001, "操作失败!"),
    ERROR_SYSTEM(10002, "系统繁忙!"),
    UNAUTHORIZED(401, "无权访问!"),
    /**
     * system 模块异常码
     */
    SYSTEM_USERNAME_ALREADY_EXISTS(20000, "账号已存在!"),
    SYSTEM_CODE_ALREADY_EXISTS(20001, "Code already exists!"),
    SYSTEM_USER_IS_NOT_EXISTS(20002, "用户不存在!"),
    SYSTEM_USER_ALREADY_REGISTER(20003, "账号已注册!"),
    SYSTEM_USER_REGISTER_EMAIL_INFO_EXPIRED(20004, "邮箱验证码已过期!"),
    SYSTEM_USER_EMAIL_ALREADY_EXISTS(20004, "该邮箱已被注册!"),
    SYSTEM_USER_EMAIL_PASSWORD_ERROR(20005, "邮件密码错误!"),
    SYSTEM_USER_EMAIL_CODE_CANNOT_EXCEED_TIMES(20006, "邮件发送不能超过三次!"),
    SYSTEM_USER_EMAIL_OR_CODE_ERROR(20007, "邮箱地址或验证码错误、请重新输入!"),
    SYSTEM_USER_IS_LOCKED(20008, "用户已锁定!"),
    SYSTEM_USER_USERNAME_OR_PASSWORD_ERROR(20009, "账号或密码不正确!"),
    SYSTEM_USER_USERNAME_IS_LOCKED(20010, "账号已锁定，6小时后解锁!"),
    SYSTEM_USER_CANNOT_UPDATE_ADMIN(20011, "仅超级管理员可操作!"),
    SYSTEM_USER_TOKEN_INFO_IS_NULL(20012, "登录信息不存在!"),
    SYSTEM_USER_EMAIL_NOT_EXISTS(20013, "该邮箱未注册!"),
    SYSTEM_USER_CANNOT_DELETE(20014, "系统默认用户不可删除!"),
    SYSTEM_ROLE_CANNOT_DELETE(20015, "系统默认角色不可删除!"),

    DATASET_ADMIN_PERMISSION_ERROR(1310,"无此权限,请联系管理员"),

    ;


    Integer code;
    String msg;

    BaseErrorCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
