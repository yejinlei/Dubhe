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

package org.dubhe.biz.dataresponse.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.dubhe.biz.base.constant.ResponseCode;
import org.dubhe.biz.base.enums.BaseErrorCodeEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.exception.CaptchaException;
import org.dubhe.biz.base.exception.FeignException;
import org.dubhe.biz.base.exception.OAuthResponseError;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;


/**
 * @description 全局异常处理器
 * @date 2020-02-23
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    /**
     * 处理自定义异常
     */
    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<DataResponseBody> loginException(AccessDeniedException e) {
        // 打印堆栈信息
        LogUtil.error(LogEnum.SYS_ERR, "未授权引起异常的堆栈信息：{}", e);
        return buildResponseEntity(HttpStatus.UNAUTHORIZED,
                DataResponseFactory.failed(BaseErrorCodeEnum.UNAUTHORIZED.getCode(),BaseErrorCodeEnum.UNAUTHORIZED.getMsg()));
    }


    /**
     * 处理自定义异常
     */
    @ExceptionHandler(value = IllegalStateException.class)
    public ResponseEntity<DataResponseBody> illegalStateException(IllegalStateException e) {
        // 打印堆栈信息
        LogUtil.error(LogEnum.SYS_ERR, "服务未发现引起异常的堆栈信息：{}", e);
        return buildResponseEntity(HttpStatus.NOT_FOUND, DataResponseFactory.failed(e.getMessage()));
    }

    /**
     * 处理OAuth2 授权异常
     */
    @ExceptionHandler(value = OAuthResponseError.class)
    public ResponseEntity<DataResponseBody> oAuth2ResponseError(OAuthResponseError e) {
        // 打印堆栈信息
        LogUtil.error(LogEnum.SYS_ERR, "OAuth2 授权异常引发的异常的堆栈信息：{}", e);
        return buildResponseEntity(e.getStatusCode(), e.getResponseBody());
    }


    /**
     * 处理所有不可知的异常
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<DataResponseBody> handleException(Throwable e) {
        // 打印堆栈信息
        LogUtil.error(LogEnum.SYS_ERR, "引发的异常的堆栈信息：{}", e);
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR,
                DataResponseFactory.failed(e.getMessage()));
    }


    /**
     * 处理自定义异常
     */
    @ExceptionHandler(value = BusinessException.class)
    public ResponseEntity<DataResponseBody> badRequestException(BusinessException e) {
        // 打印堆栈信息
        LogUtil.error(LogEnum.SYS_ERR, "引发的异常的堆栈信息：{}", e);
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

    /**
     * 远程调用异常
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<DataResponseBody> feignException(FeignException e) {
        // 打印堆栈信息
        LogUtil.error(LogEnum.SYS_ERR, "引发的异常的堆栈信息：{}", e);
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR,
                DataResponseFactory.failed(e.getResponseBody().getMsg()));
    }


    /**
     * 验证码异常
     */
    @ExceptionHandler(CaptchaException.class)
    public ResponseEntity<DataResponseBody> captchaException(CaptchaException e) {
        // 打印堆栈信息
        LogUtil.error(LogEnum.SYS_ERR, "引发的异常的堆栈信息：{}", e);
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR,
                DataResponseFactory.failed(e.getResponseBody().getMsg()));
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
