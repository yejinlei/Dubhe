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
package org.dubhe.cloud.authconfig.filter;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.dubhe.biz.base.exception.OAuthResponseError;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @description OAuth2 授权异常 过滤器
 * @date 2020-11-18
 */
@Slf4j
public class OAuth2ResponseErrorFilter implements Filter {

    /**
     *  过滤并处理OAuth2异常抛出
     *
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = ((HttpServletResponse) servletResponse);
        try {
            filterChain.doFilter(servletRequest,servletResponse);
        }catch (OAuthResponseError e){
            log.error("鉴权失败!",e);
            response.setStatus(e.getStatusCode().value());
            response.getOutputStream().write(JSON.toJSON(e.getResponseBody()).toString().getBytes());
        }catch (IllegalStateException | ResourceAccessException e){
            log.error("授权服务未发现!",e);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response
                    .getOutputStream()
                    .write(JSON.toJSON(DataResponseFactory.failed(e.getMessage())).toString().getBytes());
        }catch (Exception e){
            log.error("授权异常!",e);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response
                    .getOutputStream()
                    .write(JSON.toJSON(DataResponseFactory.failed("OAuth2 response error!")).toString().getBytes());
        }
    }


}
