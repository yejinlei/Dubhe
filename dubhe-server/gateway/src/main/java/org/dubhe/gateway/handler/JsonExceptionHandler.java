/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dubhe.gateway.handler;

import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description 网关异常处理器
 * 参考org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler
 * @date 2020-12-23
 */
public class JsonExceptionHandler implements ErrorWebExceptionHandler {

    /**
     * Strategy for reading from a {@link ReactiveHttpInputMessage} and decoding
     * the stream of bytes to Objects of type {@code <T>}.
     */
    private List<HttpMessageReader<?>> messageReaderList;
    /**
     * Strategy for encoding a stream of objects of type {@code <T>} and writing
     * the encoded stream of bytes to an {@link ReactiveHttpOutputMessage}.
     */
    private List<HttpMessageWriter<?>> messageWriterList;
    /**
     * 视图解析器
     */
    private List<ViewResolver> viewResolverList;

    /**
     * 有参构造函数
     * @param viewResolverList
     * @param messageWriterList
     * @param messageReaderList
     */
    public JsonExceptionHandler(List<ViewResolver> viewResolverList, List<HttpMessageWriter<?>> messageWriterList, List<HttpMessageReader<?>> messageReaderList) {
        Assert.notNull(messageWriterList, "messageWriterList must not be null!");
        Assert.notNull(messageReaderList, "messageReaderList must not be null!");
        this.viewResolverList = viewResolverList;
        this.messageWriterList = messageWriterList;
        this.messageReaderList = messageReaderList;
    }

    /**
     * ServerWebExchange, Context
     */
    private class ResponseContext implements ServerResponse.Context {
        @Override
        public List<HttpMessageWriter<?>> messageWriters() {
            return messageWriterList;
        }
        @Override
        public List<ViewResolver> viewResolvers() {
            return viewResolverList;
        }
    }

    /**
     * 异常处理
     * @param exchange the web exchange to write to
     * @param ex the exception to handle
     * @return to indicate when exception handling is complete
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpRequest request = exchange.getRequest();
        HttpStatus httpStatus;
        String msg;
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException responseStatusException = (ResponseStatusException) ex;
            httpStatus = responseStatusException.getStatus();
            msg = responseStatusException.getMessage();
        }  else {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            msg = httpStatus.getReasonPhrase();
        }
        Map<String, Object> result = new HashMap<>(4, 1);
        result.put(HTTP_STATUS, httpStatus);
        result.put(MSG, new DataResponseBody(httpStatus.value(),msg));
        LogUtil.error(LogEnum.GATEWAY,"JsonExceptionHandler handle error path：{} exception：{} ", request.getPath(), ex);
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }
        resultThreadLocal.set(result);
        ServerRequest serverRequest = ServerRequest.create(exchange, this.messageReaderList);
        return RouterFunctions.route(
                RequestPredicates.all(),
                this::renderErrorResponse
                ).route(serverRequest)
                .switchIfEmpty(Mono.error(ex))
                .flatMap((handler) -> handler.handle(serverRequest))
                .flatMap((response) -> write(exchange, response));

    }

    /**
     * 存储当前线程异常信息
     */
    private ThreadLocal<Map<String, Object>> resultThreadLocal = new ThreadLocal<>();
    /**
     * 请求状态
     */
    private final static String HTTP_STATUS = "httpStatus";
    /**
     * 异常信息
     */
    private final static String MSG= "body";

    /**
     * 构造响应体
     * @param request the request to handle
     * @return the handler function to route to if the predicate applies
     */
    protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> result = resultThreadLocal.get();
        resultThreadLocal.remove();
        return ServerResponse
                .status((HttpStatus) result.get(HTTP_STATUS))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(result.get(MSG)));
    }


    /**
     * 写入输出流
     * @param exchange the web exchange to write to
     * @param response the HTTP response
     * @return
     */
    private Mono<? extends Void> write(ServerWebExchange exchange,
                                       ServerResponse response) {
        exchange.getResponse()
                .getHeaders()
                .setContentType(
                        response.headers().getContentType()
                );
        return response.writeTo(exchange, new ResponseContext());
    }

}