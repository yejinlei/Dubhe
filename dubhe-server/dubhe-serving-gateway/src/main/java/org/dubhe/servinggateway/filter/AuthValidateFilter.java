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

package org.dubhe.servinggateway.filter;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;
import org.dubhe.servinggateway.utils.TokenValidateUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

/**
 * @description 自定义权限过滤器
 * @date 2020-10-09
 */
//@Component
public class AuthValidateFilter implements GlobalFilter, Ordered {

    @Resource
    private TokenValidateUtil tokenValidateUtil;

    /**
     * 推理请求权限过滤
     *
     * @param exchange 服务网络交换器
     * @param chain    网关过滤链表
     * @return Mono<Void> 返回过滤后的信息
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        // 请求不包含AUTHORIZATION则直接设置请求状态码401
        if (StringUtils.isBlank(authToken)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

            DataResponseBody result = DataResponseFactory.failed(HttpStatus.UNAUTHORIZED.value(), "缺少验签参数");
            return exchange.getResponse().writeWith(Flux.just(this.getBodyBuffer(exchange.getResponse(), result)));
        }

        // 验证token正确性
        boolean pass = tokenValidateUtil.validateToken(authToken);
        if (!pass) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

            DataResponseBody result = DataResponseFactory.failed(HttpStatus.UNAUTHORIZED.value(), "token错误");
            return exchange.getResponse().writeWith(Flux.just(this.getBodyBuffer(exchange.getResponse(), result)));
        }
        return chain.filter(exchange);
    }

    /**
     * 封装返回值
     *
     * @param response http响应体
     * @param result   返回结果
     * @return DataBuffer 返回数据缓冲
     */
    private DataBuffer getBodyBuffer(ServerHttpResponse response, DataResponseBody result) {
        return response.bufferFactory().wrap(JSONObject.toJSONBytes(result));
    }

    /**
     * 获取拦截器优先级
     *
     * @return int 返回优先级
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + NumberConstant.NUMBER_1000;
    }
}
