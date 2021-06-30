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

package org.dubhe.admin.rest;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @description 代理转发
 * @date 2020-06-23
 */
@RestController
public class ForwardController {
    @Value("${dubhe-proxy.visual.keyword}")
    private String visual;
    @Value("${dubhe-proxy.visual.server}")
    private String visualServer;
    @Value("${dubhe-proxy.visual.port}")
    private String visualPort;
    @Value("${dubhe-proxy.refine.keyword}")
    private String refine;
    @Value("${dubhe-proxy.refine.server}")
    private String refineServer;
    @Value("${dubhe-proxy.refine.port}")
    private String refinePort;

    RestTemplate restTemplate = new RestTemplate();

    /**
     * 根据不同的请求拼上对应的转发路径
     *
     * @param request http请求
     * @return URI 用于restTemplate的请求路径
     **/
    private URI getURI(HttpServletRequest request) throws URISyntaxException {
        String requestURI = request.getRequestURI();
        String server = null;
        String prefix = "";
        int port = 0;
        if (requestURI.startsWith(StrUtil.SLASH + visual)) {
            prefix = visual;
            server = visualServer;
            port = Integer.parseInt(visualPort);
        } else if (requestURI.startsWith(StrUtil.SLASH + refine)) {
            prefix = refine;
            server = refineServer;
            port = Integer.parseInt(refinePort);
        }

        return new URI("http", null, server, port, requestURI.substring(prefix.length() + 1), request.getQueryString(), null);
    }

    /**
     * 获取请求中的Cookie
     *
     * @param request http请求
     * @return HttpHeaders 用于restTemplate的header
     **/
    private HttpHeaders getHeader(HttpServletRequest request) {
        String cookie = request.getHeader("Cookie");
        HttpHeaders httpHeaders = new HttpHeaders();
        if (null != cookie) {
            httpHeaders.set("Cookie", cookie);
        }
        return httpHeaders;
    }

    /**
     * 转发get请求
     *
     * @param request http请求
     * @return ResponseEntity 返回给前端的响应实体
     **/
    @GetMapping({StrUtil.SLASH + "${dubhe-proxy.visual.keyword}" + StrUtil.SLASH + "**", StrUtil.SLASH + "${dubhe-proxy.refine.keyword}" + StrUtil.SLASH + "**"})
    @ResponseBody
    public ResponseEntity<String> mirrorRest(HttpServletRequest request) throws URISyntaxException {
        URI uri = getURI(request);
        HttpHeaders httpHeaders = getHeader(request);
        ResponseEntity<String> responseEntity =
                restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<String>(httpHeaders), String.class);
        return responseEntity;
    }

    /**
     * 转发get请求
     *
     * @param request http请求
     * @param method 请求方法
     * @param body 请求体
     * @return ResponseEntity 返回给前端的响应实体
     **/
    @RequestMapping({StrUtil.SLASH + "${dubhe-proxy.visual.keyword}" + StrUtil.SLASH + "**", StrUtil.SLASH + "${dubhe-proxy.refine.keyword}" + StrUtil.SLASH + "**"})
    @ResponseBody
    public ResponseEntity<String> mirrorRest(HttpMethod method, HttpServletRequest request, @RequestBody String body) throws URISyntaxException {
        URI uri = getURI(request);
        HttpHeaders httpHeaders = getHeader(request);
        ResponseEntity<String> responseEntity =
                restTemplate.exchange(uri, method, new HttpEntity<String>(body, httpHeaders), String.class);
        return responseEntity;
    }
}
