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

package org.dubhe.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.IbatisException;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.dubhe.base.DataResponseBody;
import org.dubhe.base.ResponseCode;
import org.dubhe.enums.LogEnum;
import org.dubhe.exception.*;
import org.dubhe.utils.LogUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

/**
 * @description 处理异常
 * @date 2020-02-23
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有不可知的异常
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<DataResponseBody> handleException(Throwable e) {
        // 打印堆栈信息
        LogUtil.error(LogEnum.SYS_ERR, "引起异常的堆栈信息：{}", e);
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR,
                new DataResponseBody(ResponseCode.ERROR, e.getMessage()));
    }

    /**
     * UnauthorizedException
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<DataResponseBody> badCredentialsException(UnauthorizedException e) {
        // 打印堆栈信息
        LogUtil.error(LogEnum.SYS_ERR, "引起异常的堆栈信息：{}", e);
        String message = "坏的凭证".equals(e.getMessage()) ? "用户名或密码不正确" : e.getMessage();
        return buildResponseEntity(HttpStatus.UNAUTHORIZED, new DataResponseBody(ResponseCode.ERROR, message));
    }

    /**
     * 处理自定义异常
     */
    @ExceptionHandler(value = BusinessException.class)
    public ResponseEntity<DataResponseBody> badRequestException(BusinessException e) {
        // 打印堆栈信息
        LogUtil.error(LogEnum.SYS_ERR, "引起异常的堆栈信息：{}", e);
        return buildResponseEntity(HttpStatus.OK, e.getResponseBody());
    }

    /**
     * 处理自定义异常
     */
    @ExceptionHandler(value = IbatisException.class)
    public ResponseEntity<DataResponseBody> persistenceException(IbatisException e) {
        // 打印堆栈信息
        LogUtil.error(LogEnum.SYS_ERR, "引起异常的堆栈信息：{}", e);
        return buildResponseEntity(HttpStatus.OK, new DataResponseBody(ResponseCode.ERROR, e.getMessage()));
    }

    /**
     * 处理自定义异常
     */
    @ExceptionHandler(value = AuthenticationException.class)
    public ResponseEntity<DataResponseBody> badRequestException(AuthenticationException e) {
        // 打印堆栈信息
        LogUtil.error(LogEnum.SYS_ERR, "引起异常的堆栈信息：{}", e);
        return buildResponseEntity(HttpStatus.OK, new DataResponseBody(ResponseCode.UNAUTHORIZED, "无权访问"));
    }

    /**
     * shiro 异常捕捉
     */
    @ExceptionHandler(value = ShiroException.class)
    public ResponseEntity<DataResponseBody> accountException(ShiroException e) {
        // 打印堆栈信息
        LogUtil.error(LogEnum.SYS_ERR, "引起异常的堆栈信息：{}", e);
        ResponseEntity<DataResponseBody> responseEntity;
        if (e instanceof IncorrectCredentialsException) {
            responseEntity = buildResponseEntity(HttpStatus.OK, new DataResponseBody(ResponseCode.ERROR, "密码不正确"));
        } else if (e instanceof UnknownAccountException) {
            responseEntity = buildResponseEntity(HttpStatus.OK, new DataResponseBody(ResponseCode.ERROR, "此账户不存在"));
        } else if (e instanceof LockedAccountException) {
            responseEntity = buildResponseEntity(HttpStatus.OK, new DataResponseBody(ResponseCode.ERROR, "未知的账号"));
        } else if (e instanceof UnknownAccountException) {
            responseEntity = buildResponseEntity(HttpStatus.OK, new DataResponseBody(ResponseCode.ERROR, "账户已被禁用"));
        } else {
            responseEntity = buildResponseEntity(HttpStatus.OK,
                    new DataResponseBody(ResponseCode.UNAUTHORIZED, "无权访问"));
        }
        return responseEntity;
    }

    /**
     * 处理自定义异常
     */
    @ExceptionHandler(value = LoginException.class)
    public ResponseEntity<DataResponseBody> loginException(LoginException e) {
        // 打印堆栈信息
        LogUtil.error(LogEnum.SYS_ERR, "引起异常的堆栈信息：{}", e);
        return buildResponseEntity(HttpStatus.UNAUTHORIZED, e.getResponseBody());
    }

    /**
     * 处理自定义异常
     */
    @ExceptionHandler(value = CaptchaException.class)
    public ResponseEntity<DataResponseBody> captchaException(CaptchaException e) {
        // 打印堆栈信息
        LogUtil.error(LogEnum.SYS_ERR, "引起异常的堆栈信息：{}", e);
        return buildResponseEntity(HttpStatus.OK, e.getResponseBody());
    }

    /**
     * 处理自定义异常
     */
    @ExceptionHandler(value = NotebookBizException.class)
    public ResponseEntity<DataResponseBody> captchaException(NotebookBizException e) {
        // 打印堆栈信息
        LogUtil.error(LogEnum.SYS_ERR, "引起异常的堆栈信息：{}", e);
        return buildResponseEntity(HttpStatus.OK, e.getResponseBody());
    }

    /**
     * 处理所有接口数据验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DataResponseBody> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 打印堆栈信息
        LogUtil.error(LogEnum.SYS_ERR, "引起异常的堆栈信息：{}", e);
        String[] str = Objects.requireNonNull(e.getBindingResult().getAllErrors().get(0).getCodes())[1].split("\\.");
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        String msg = "不能为空";
        if (msg.equals(message)) {
            message = str[1] + ":" + message;
        }
        return buildResponseEntity(HttpStatus.BAD_REQUEST, new DataResponseBody(ResponseCode.ERROR, message));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<DataResponseBody> bindException(BindException e) {
        // 打印堆栈信息
        LogUtil.error(LogEnum.SYS_ERR, "引起异常的堆栈信息：{}", e);
        ObjectError error = e.getAllErrors().get(0);
        return buildResponseEntity(HttpStatus.BAD_REQUEST,
                new DataResponseBody(ResponseCode.ERROR, error.getDefaultMessage()));
    }

    /**
     * 统一返回
     *
     * @param httpStatus
     * @param responseBody
     * @return
     */
    private ResponseEntity<DataResponseBody> buildResponseEntity(HttpStatus httpStatus, DataResponseBody responseBody) {
        return new ResponseEntity<>(responseBody, httpStatus);
    }
}
