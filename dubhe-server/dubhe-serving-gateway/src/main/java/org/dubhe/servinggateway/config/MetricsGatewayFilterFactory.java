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

import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.servinggateway.constant.GatewayConstant;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

/**
 * @description 监控指标网关过滤器
 * @date 2020-09-25
 */
@Component
@Slf4j
public class MetricsGatewayFilterFactory extends AbstractGatewayFilterFactory<MetricsGatewayFilterFactory.Config> {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * constructor
     */
    public MetricsGatewayFilterFactory() {
        // 这里需要将自定义的config传过去，否则会报告ClassCastException
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.emptyList();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new MetricsGatewayFilter(config);
    }

    /**
     * 自定义的config类，用来设置传入的参数
     */
    public static class Config {

    }

    private class MetricsGatewayFilter implements GatewayFilter, Ordered {

        private Config config;

        MetricsGatewayFilter(Config config) {
            this.config = config;
        }

        /**
         * 推理请求过滤
         *
         * @param exchange 服务网络交换器
         * @param chain    网关过滤链表
         * @return Mono<Void> 返回过滤后的信息
         */
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            ServerHttpResponse serverHttpResponse = exchange.getResponse();
            ServerHttpResponseDecorator responseDecorator = new ServerHttpResponseDecorator(serverHttpResponse) {
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    if (body instanceof Flux) {
                        return super.writeWith(
                                DataBufferUtils.join(body)
                                        .doOnNext(dataBuffer -> {
                                            // 判断该请求是否为推理请求
                                            boolean isInference = exchange.getRequest().getPath().toString().startsWith(GatewayConstant.INFERENCE_INTERFACE_NAME);
                                            if (isInference) {
                                                // 调用请求是否成功
                                                boolean callFailed = !HttpStatus.OK.equals(serverHttpResponse.getStatusCode());

                                                // 获取路由配置
                                                Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
                                                if(route == null){
                                                    throw  new BusinessException("路由配置不能为空");
                                                }
                                                // 利用redis对接口调用进行计数
                                                Map<String, Object> metadata = route.getMetadata();
                                                String metricsKey = GatewayConstant.INFERENCE_METRICS_PREFIX + metadata.get("servingConfigId");
                                                redisTemplate.opsForHash().increment(metricsKey, GatewayConstant.INFERENCE_CALL_COUNT, NumberConstant.NUMBER_1);
                                                if (callFailed) {
                                                    redisTemplate.opsForHash().increment(metricsKey, GatewayConstant.INFERENCE_FAILED_COUNT, NumberConstant.NUMBER_1);
                                                } else {
                                                    boolean isJsonResponse = true;
                                                    JSONObject inferenceResult = null;
                                                    try {
                                                        inferenceResult = JSON.parseObject(getResponseBody(serverHttpResponse, dataBuffer));
                                                    } catch (Exception e) {
                                                        isJsonResponse = false;
                                                    }
                                                    // 请求非JSON或请求失败则算作调用失败
                                                    if (!isJsonResponse || Objects.isNull(inferenceResult) ||
                                                            !Boolean.TRUE.equals(inferenceResult.getBoolean(GatewayConstant.SUCCESS))) {
                                                        redisTemplate.opsForHash().increment(metricsKey, GatewayConstant.INFERENCE_FAILED_COUNT, NumberConstant.NUMBER_1);
                                                    }
                                                }
                                            }
                                        })
                        );
                    }
                    return super.writeWith(body);
                }
            };
            return chain.filter(exchange.mutate().response(responseDecorator).build());
        }

        /**
         * @param response   请求
         * @param dataBuffer 数据缓冲
         * @return String 返回response body内容
         */
        private String getResponseBody(ServerHttpResponse response, DataBuffer dataBuffer) {
            String bodyContent = "";
            // 获取response的编码
            String contentEncoding = response.getHeaders().getFirst("Content-Encoding");
            // 判断response编码是否为gzip
            if (StringUtils.isNotBlank(contentEncoding) && contentEncoding.contains("gzip")) {
                // 进行gzip解压内容
                bodyContent = decompressWithGZIP(dataBuffer, StandardCharsets.UTF_8);
            } else {
                bodyContent = dataBuffer.toString(StandardCharsets.UTF_8);
            }
            return bodyContent;
        }

        /**
         * 根据输入字符集对缓冲区进行gzip解压
         *
         * @param dataBuffer 数据缓冲区
         * @param charset    字符集编码
         * @return String 返回解压后的数据
         */
        private String decompressWithGZIP(DataBuffer dataBuffer, Charset charset) {
            GZIPInputStream gis;
            String content = "";
            try {
                gis = new GZIPInputStream(new ByteArrayInputStream(dataBuffer.asByteBuffer().array()));

                content = IoUtil.read(gis, charset);
            } catch (IOException e) {
                log.error("解压失败");
            }
            return content;
        }

        /**
         * 获取拦截器优先级
         *
         * @return int 返回优先级
         */
        @Override
        public int getOrder() {
            // -1 is response write filter, must be called before that
            // 需保证过滤器的优先级高于WriteFilter
            return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - NumberConstant.NUMBER_1;
        }
    }
}
