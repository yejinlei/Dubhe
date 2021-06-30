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

package org.dubhe.k8s.interceptor;

import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.utils.K8sCallBackTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @description k8s pod 异步回调拦截器
 * @date 2020-05-28
 */
@Component
public class K8sCallBackPodInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private K8sCallBackTool k8sCallBackTool;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)  {
        String uri = request.getRequestURI();
        LogUtil.debug(LogEnum.BIZ_K8S,"接收到k8s异步请求,URI:{}",uri);
        String k8sCallbackToken = request.getHeader(K8sCallBackTool.K8S_CALLBACK_TOKEN);
        if (StringUtils.isBlank(k8sCallbackToken)){
            LogUtil.warn(LogEnum.BIZ_K8S,"k8s异步回调没有配置【{}】,URI:{}",K8sCallBackTool.K8S_CALLBACK_TOKEN,uri);
            return false;
        }
        boolean pass = k8sCallBackTool.validateToken(k8sCallbackToken);
        if (!pass){
            LogUtil.warn(LogEnum.BIZ_K8S,"k8s异步回调token:【{}】 验证不通过,URI:{}",k8sCallbackToken,uri);
        }
        return pass;
    }

}
