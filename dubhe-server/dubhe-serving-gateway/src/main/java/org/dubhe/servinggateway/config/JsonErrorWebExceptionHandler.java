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

package org.dubhe.servinggateway.config;

import org.dubhe.biz.base.constant.NumberConstant;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * @description 以Json形式返回的web异常处理器
 * @date 2020-09-22
 */
public class JsonErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {

    /**
     * 创建json形式异常处理器
     *
     * @param errorAttributes    异常信息
     * @param resourceProperties 资源配置信息
     * @param errorProperties    错误配置信息
     * @param applicationContext 应用程序配置
     */
    public JsonErrorWebExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties, ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }

    /**
     * 封装异常处理信息
     *
     * @param request           请求体
     * @param includeStackTrace
     * @return Map<String, Object> 返回封装后的信息
     */
    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        // 自定义异常消息封装
        String msg = "服务暂不可用，请稍后再试！";
        Throwable error = super.getError(request);
        int responseStatus = ((ResponseStatusException) error).getStatus().value();
        if (responseStatus == HttpStatus.NOT_FOUND.value()) {
            msg = "服务未启动或异常，请停止检查或稍后再试！";
        }
        Map<String, Object> errorAttributes = new HashMap<>(NumberConstant.NUMBER_6);
        errorAttributes.put("code", responseStatus);
        errorAttributes.put("msg", msg);
        errorAttributes.put("data", null);
        return errorAttributes;
    }

    /**
     * 路由处理
     *
     * @param errorAttributes 异常属性
     * @return RouterFunction<ServerResponse> 返回路由处理结果
     */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    /**
     * 获取定制HTTP响应码
     *
     * @param errorAttributes 异常属性
     * @return int 返回HTTP响应码
     */
    @Override
    protected int getHttpStatus(Map<String, Object> errorAttributes) {
        // 这里其实可以根据errorAttributes里面的属性定制HTTP响应码
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
