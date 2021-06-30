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

package org.dubhe.dubhek8s.event.callback;

import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.cache.ResourceCache;
import org.dubhe.k8s.constant.K8sLabelConstants;
import org.dubhe.k8s.domain.dto.BaseK8sDeploymentCallbackCreateDTO;
import org.dubhe.k8s.domain.resource.BizDeployment;
import org.dubhe.k8s.enums.WatcherActionEnum;
import org.dubhe.k8s.service.K8sResourceService;
import org.dubhe.k8s.utils.K8sCallBackTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
    @Autowired
    private RestTemplate restTemplate;

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

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add(K8sCallBackTool.K8S_CALLBACK_TOKEN, token);
                HttpEntity<String> entity = new HttpEntity<>(JSON.toJSONString(baseK8sDeploymentCallbackCreateDTO), headers);
                ResponseEntity<DataResponseBody> responseEntity = restTemplate.postForEntity(url,entity,DataResponseBody.class);

                if (HttpStatus.HTTP_OK == responseEntity.getStatusCodeValue()){
                    DataResponseBody dataResponseBody = responseEntity.getBody();
                    LogUtil.info(LogEnum.BIZ_K8S, "{} deployment {} {} code {} msg：{} ", url, deployment.getName(), watcherActionEnum.getAction(), dataResponseBody.getCode(),dataResponseBody.getMsg());
                }else {
                    LogUtil.error(LogEnum.BIZ_K8S, "{}  deployment {} {} callback status：{} ", url, deployment.getName(), watcherActionEnum.getAction(),responseEntity.getStatusCodeValue());
                }
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_K8S, "deployment {} callback {} error {}",deployment,watcherActionEnum.getAction(), e.getMessage(),e);
        }
    }
}
