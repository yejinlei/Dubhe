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

package org.dubhe.docker.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.docker.config.DubheDockerJavaConfig;
import org.dubhe.docker.constant.DockerCallbackConstant;
import org.dubhe.docker.domain.dto.DockerPushCallbackDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description docker回调相关工具类
 * @date 2020-07-13
 */
@Component
public class DockerCallbackTool {
    /**
     * http请求超时时间 单位毫秒
     */
    private static final int TIMEOUT_MILLISECOND = 20 * 1000;

    @Autowired
    private DubheDockerJavaConfig dubheDockerJavaConfig;

    /**
     * 获取回调地址
     *
     * @param host 主机
     * @param port 端口
     * @param action 动作
     * @return
     */
    public String getCallbackUrl(String host,String port,String action){
        return SymbolConstant.HTTP_SLASH+host+SymbolConstant.COLON+port+ DockerCallbackConstant.DOCKER_CALLBACK_URI+action;
    }

    /**
     * 镜像推送回调
     *
     * @param dockerPushCallbackDTO 回调参数
     * @param url 回调地址
     * @param count 重试计数
     */
    public static void sendPushCallback(DockerPushCallbackDTO dockerPushCallbackDTO, String url,Integer count){
        try{
            LogUtil.info(LogEnum.TERMINAL, "{} sendPushCallback {} count {}", url, dockerPushCallbackDTO,count);
            HttpResponse httpResponse = HttpRequest.post(url)
                            .body(JSON.toJSONString(dockerPushCallbackDTO))
                            .timeout(TIMEOUT_MILLISECOND)
                            .execute();
            LogUtil.info(LogEnum.TERMINAL, "{} sendPushCallback {} count {} status：{}", url, dockerPushCallbackDTO,count,httpResponse.getStatus());
            //重试
            if (HttpStatus.HTTP_OK != httpResponse.getStatus() && count > MagicNumConstant.ZERO){
                Thread.sleep(MagicNumConstant.ONE_THOUSAND);
                sendPushCallback(dockerPushCallbackDTO,url,--count);
            }
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL, "{} sendPushCallback {} count {} error：{} ", url, dockerPushCallbackDTO,count,e.getMessage(),e);
        }
    }
}
