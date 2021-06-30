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

package org.dubhe.recycle.interceptor;

import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.recycle.utils.RecycleTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @description 资源回收调用拦截器
 * @date 2021-01-21
 */
@Component
public class RecycleCallInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private RecycleTool recycleTool;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)  {
        String uri = request.getRequestURI();
        LogUtil.debug(LogEnum.GARBAGE_RECYCLE,"资源回收接收到请求,URI:{}",uri);
        String token = request.getHeader(RecycleTool.RECYCLE_TOKEN);
        if (StringUtils.isBlank(token)){
            LogUtil.warn(LogEnum.GARBAGE_RECYCLE,"资源回收没有token配置【{}】,URI:{}",RecycleTool.RECYCLE_TOKEN,uri);
            return false;
        }
        boolean pass = recycleTool.validateToken(token);
        if (!pass){
            LogUtil.warn(LogEnum.GARBAGE_RECYCLE,"资源回收token:【{}】 验证不通过,URI:{}",token,uri);
        }
        return pass;
    }

}
