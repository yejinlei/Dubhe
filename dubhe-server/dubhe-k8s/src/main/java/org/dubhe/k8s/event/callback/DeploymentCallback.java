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

package org.dubhe.k8s.event.callback;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import org.dubhe.base.DataResponseBody;
import org.dubhe.base.ResponseCode;
import org.dubhe.dto.callback.BaseK8sDeploymentCallbackCreateDTO;
import org.dubhe.enums.LogEnum;
import org.dubhe.k8s.cache.ResourceCache;
import org.dubhe.k8s.constant.K8sLabelConstants;
import org.dubhe.k8s.domain.resource.BizDeployment;
import org.dubhe.k8s.enums.WatcherActionEnum;
import org.dubhe.k8s.service.K8sResourceService;
import org.dubhe.utils.K8sCallBackTool;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Observable;

/**
 * @description DeploymentWatcher回调
 * @date 2020-11-26
 */
@Component
public class DeploymentCallback extends Observable {
    @Autowired
    private K8sCallBackTool k8sCallBackTool;
    @Autowired
    private K8sResourceService k8sResourceService;
    @Autowired
    private ResourceCache resourceCache;

    /**
     * http请求超时时间 单位毫秒
     */
    private static final int TIMEOUT_MILLISECOND = 20 * 1000;

    /**
     * 回调
     * @param watcherActionEnum 动作
     * @param deployment deployment实例
     */
    public void deploymentCallback(WatcherActionEnum watcherActionEnum, BizDeployment deployment) {
        try {
            if (deployment == null){
                return;
            }
            String businessLabel = deployment.getBusinessLabel();
            LogUtil.info(LogEnum.BIZ_K8S,"watch deployment {} action:{} readyReplicas:{}",deployment.getName(),watcherActionEnum.getAction(),deployment.getReadyReplicas());
            setChanged();
            notifyObservers(deployment);
            if (StringUtils.isNotEmpty(businessLabel)){
                BaseK8sDeploymentCallbackCreateDTO baseK8sDeploymentCallbackCreateDTO = new BaseK8sDeploymentCallbackCreateDTO(deployment.getNamespace(), deployment.getLabel(K8sLabelConstants.BASE_TAG_SOURCE),deployment.getName(),  deployment.getReadyReplicas() == null?0:deployment.getReadyReplicas(), deployment.getReplicas() == null?0:deployment.getReplicas());
                String url = k8sCallBackTool.getDeploymentCallbackUrl(businessLabel);
                String token = k8sCallBackTool.generateToken();
                HttpResponse httpResponse = HttpRequest.post(url)
                        .header(K8sCallBackTool.K8S_CALLBACK_TOKEN, token)
                        .body(JSON.toJSONString(baseK8sDeploymentCallbackCreateDTO))
                        .timeout(TIMEOUT_MILLISECOND)
                        .execute();
                if (HttpStatus.HTTP_OK != httpResponse.getStatus()){
                    LogUtil.error(LogEnum.BIZ_K8S, "{}  deployment {} {} callback status：{} ", url, deployment.getName(), watcherActionEnum.getAction(),httpResponse.getStatus());
                    return;
                }
                if (StringUtils.isNotEmpty(httpResponse.body())) {
                    DataResponseBody dataResponseBody = null;
                    try{
                        dataResponseBody = JSON.parseObject(httpResponse.body(), DataResponseBody.class);
                    }catch (JSONException e) {
                        LogUtil.error(LogEnum.BIZ_K8S, "{} deployment {} {} httpResponseBody {} error：{} ", url, deployment.getName(), watcherActionEnum.getAction(), httpResponse.body(),e);
                        return;
                    }
                    if (ResponseCode.SUCCESS.equals(dataResponseBody.getCode())) {
                        return;
                    }
                    LogUtil.error(LogEnum.BIZ_K8S, "{}  deployment {} {} callback error：{} ", url, deployment.getName(), watcherActionEnum.getAction(), dataResponseBody.getMsg());
                    return;
                }
                LogUtil.error(LogEnum.BIZ_K8S, "{}  deployment {} {} callback not return", url, deployment.getName(), watcherActionEnum.getAction());
                return;
            }
            LogUtil.info(LogEnum.BIZ_K8S, "watch deployment {} action:{}", deployment.getName(), watcherActionEnum.getAction());
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_K8S, "callback error {}", e.getMessage());
        }
    }
}
