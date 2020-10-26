/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

package org.dubhe.config;

import com.alibaba.fastjson.JSON;
import org.dubhe.constant.StringConstant;
import org.dubhe.constatnts.UserConstant;
import org.dubhe.dto.GlobalRequestRecordDTO;
import org.dubhe.enums.LogEnum;
import org.dubhe.utils.JwtUtils;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.dubhe.constant.StringConstant.K8S_CALLBACK_URI;

/**
 * @description 全局请求拦截器 用于日志收集
 * @date 2020-08-13
 */
@Order(1)
@Component
@WebFilter(filterName = "GlobalFilter", urlPatterns = "/**")
public class GlobalFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {

        long start = System.currentTimeMillis();
        HttpServletRequest request = ((HttpServletRequest) servletRequest);
        HttpServletResponse response = ((HttpServletResponse) servletResponse);

        GlobalRequestRecordDTO dto = new GlobalRequestRecordDTO();

        try {
            if (StringUtils.isNotBlank(request.getContentType()) && request.getContentType().contains(StringConstant.MULTIPART)) {
                chain.doFilter(request, response);
            } else {
                checkScheduleRequest(request);
                checkK8sCallback(request);

                RequestBodyWrapper requestBodyWrapper = new RequestBodyWrapper(request);
                ResponseBodyWrapper responseBodyWrapper = new ResponseBodyWrapper(response);
                dto.setRequestBody(requestBodyWrapper.getBodyString());
                chain.doFilter(requestBodyWrapper, responseBodyWrapper);

                if (StringConstant.JSON_REQUEST.equals(responseBodyWrapper.getContentType())) {
                    final String responseBody = responseBodyWrapper.getResponseBody();
                    dto.setResponseBody(responseBody);
                } else {
                    responseBodyWrapper.flush();
                }
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.GLOBAL_REQ, "Global request record error : {}", e);
            throw e;
        } finally {
            buildGlobalRequestDTO(dto, request, response);
            dto.setTimeCost(System.currentTimeMillis() - start);
            LogUtil.info(LogEnum.GLOBAL_REQ, "Global request record: {}", dto);
            LogUtil.cleanTrace();
        }
    }

    /**
     * 构建全局请求对象
     *
     * @param dto
     * @param request
     * @param response
     */
    private void buildGlobalRequestDTO(GlobalRequestRecordDTO dto, HttpServletRequest request, HttpServletResponse response) {
        dto.setClientHost(request.getRemoteHost());
        dto.setParams(JSON.toJSONString(request.getParameterMap()));
        dto.setMethod(request.getMethod());
        dto.setUri(request.getRequestURI());
        //身份认证信息
        String token = request.getHeader(UserConstant.USER_TOKEN_KEY);
        dto.setAuthorization(token);
        if (token != null) {
            String userName = JwtUtils.getUserName(token);
            dto.setUsername(userName);
        }
        dto.setContentType(response.getContentType());
        dto.setStatus(response.getStatus());
    }

    /**
     * 检查是否是前端的定时请求
     *
     * @param request 请求信息
     * @return 是否是前端的定时请求
     */
    private boolean checkScheduleRequest(HttpServletRequest request) {

        if (StringConstant.REQUEST_METHOD_GET.equals(request.getMethod())
                && StringUtils.isNotBlank(request.getParameter(LogUtil.SCHEDULE_LEVEL))) {
            LogUtil.startScheduleTrace();
            return true;
        }

        return false;
    }

    /**
     * 校验请求是否为k8s回调
     * @param request 请求信息
     * @return 是否为k8s回调
     */
    private boolean checkK8sCallback(HttpServletRequest request) {
        if (request.getRequestURI() != null && request.getRequestURI().contains(K8S_CALLBACK_URI)) {
            LogUtil.startK8sCallbackTrace();
            return true;
        }
        return false;
    }

    @Override
    public void destroy() {

    }
}
