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

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.utils.SpringContextHolder;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.dubhek8s.handler.WebSocketServer;
import org.dubhe.k8s.cache.ResourceCache;
import org.dubhe.k8s.constant.K8sLabelConstants;
import org.dubhe.k8s.domain.dto.BaseK8sPodCallbackCreateDTO;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.enums.PodPhaseEnum;
import org.dubhe.k8s.enums.WatcherActionEnum;
import org.dubhe.k8s.service.K8sResourceService;
import org.dubhe.k8s.utils.K8sCallBackTool;
import org.dubhe.k8s.utils.K8sNameTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Observable;


/**
 * @description watcher回调
 * @date 2020-06-02
 */
@Component
public class PodCallback extends Observable {
    @Autowired
    private K8sCallBackTool k8sCallBackTool;
    @Autowired
    private K8sResourceService k8sResourceService;
    @Autowired
    private ResourceCache resourceCache;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private WebSocketServer webSocketServer;
    @Autowired
    private K8sNameTool k8sNameTool;


    private static final String POD_CONDITION_STATUS_FALSE = "False";

    /**
     * pod事件回调
     * Action 为 DELETED 则 pod的phase视为 DELETED
     * pod.getPhase = PodPhaseEnum 中 Running，Succeeded，Deleted，Failed会发生回调
     *
     * @param watcherActionEnum 监控枚举类
     * @param pod Pod对象
     */
    public void podCallback(WatcherActionEnum watcherActionEnum, BizPod pod) {
        try {
            if (pod == null){
                return;
            }
            // 推送集群资源监控信息
            Long userId = k8sNameTool.getUserIdFromNamespace(pod.getNamespace());
            if (userId != null){
                webSocketServer.sendToClient(userId);
            }
            String businessLabel = pod.getBusinessLabel();
            LogUtil.info(LogEnum.BIZ_K8S,"watch pod {} action:{} phase:{}",pod.getName(),watcherActionEnum.getAction(),pod.getPhase());
            cachePod(watcherActionEnum,pod);
            String waitingReason = dealWithWaiting(watcherActionEnum, pod);
            setChanged();
            notifyObservers(pod);
            if (StringUtils.isNotEmpty(businessLabel) && needCallback(watcherActionEnum,pod)){
                dealWithDeleted(watcherActionEnum,pod);
                BaseK8sPodCallbackCreateDTO baseK8sPodCallbackCreateDTO = new BaseK8sPodCallbackCreateDTO(pod.getNamespace(), pod.getLabel(K8sLabelConstants.BASE_TAG_SOURCE),pod.getName(), pod.getLabel(K8sLabelConstants.BASE_TAG_P_KIND), pod.getLabel(K8sLabelConstants.BASE_TAG_P_NAME), pod.getPhase(), waitingReason);
                baseK8sPodCallbackCreateDTO.setLables(pod.getLabels());
                String url = k8sCallBackTool.getPodCallbackUrl(businessLabel);
                String token = k8sCallBackTool.generateToken();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add(K8sCallBackTool.K8S_CALLBACK_TOKEN, token);
                HttpEntity<String> entity = new HttpEntity<>(JSON.toJSONString(baseK8sPodCallbackCreateDTO), headers);
                ResponseEntity<DataResponseBody> responseEntity = restTemplate.postForEntity(url,entity,DataResponseBody.class);

                if (HttpStatus.HTTP_OK == responseEntity.getStatusCodeValue()){
                    DataResponseBody dataResponseBody = responseEntity.getBody();
                    LogUtil.info(LogEnum.BIZ_K8S, "{} pod {} {} code {} msg：{} ", url, pod.getName(), watcherActionEnum.getAction(), dataResponseBody.getCode(),dataResponseBody.getMsg());
                }else {
                    LogUtil.error(LogEnum.BIZ_K8S, "{} pod {} {} callback status：{} ", url, pod.getName(), watcherActionEnum.getAction(),responseEntity.getStatusCodeValue());
                }
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_K8S, "pod {} callback {} error {}",pod,watcherActionEnum.getAction(), e);
        }
    }

    /**
     * 处理pod添加事件，添加时将podName缓存到redis
     *
     * @param watcherActionEnum 监控枚举类
     * @param pod Pod对象
     */
    private void cachePod(WatcherActionEnum watcherActionEnum, BizPod pod) {
        if (WatcherActionEnum.ADDED.getAction().equals(watcherActionEnum.getAction())){
            resourceCache.cachePod(pod.getLabel(K8sLabelConstants.BASE_TAG_SOURCE),pod.getName());
        } else if (PodPhaseEnum.RUNNING.getPhase().equals(pod.getPhase())) {
            if(!resourceCache.isPodNameCached(pod.getName())) {
                resourceCache.cachePod(pod.getLabel(K8sLabelConstants.BASE_TAG_SOURCE),pod.getName());
            }
        }
    }

    /**
     * 处理pod Waiting
     *
     * @param watcherActionEnum 监控枚举类
     * @param pod Pod对象
     */
    private String dealWithWaiting(WatcherActionEnum watcherActionEnum, BizPod pod) {
        String waitingMessgae = SymbolConstant.BLANK;
        if (WatcherActionEnum.DELETED == watcherActionEnum){
            return waitingMessgae;
        }
        // 如果 Pod 运行失败（如ImagePullBackOff），从 ContainerStatuses 获取 Waiting message 返回
        if (CollectionUtil.isNotEmpty(pod.getContainerStatuses())
            && null != pod.getContainerStatuses().get(MagicNumConstant.ZERO).getWaiting()){
            String waitingReason = pod.getContainerStatuses().get(MagicNumConstant.ZERO).getWaiting().getReason();
            waitingMessgae = pod.getContainerStatuses().get(MagicNumConstant.ZERO).getWaiting().getMessage();
            if(waitingReason == null || "ContainerCreating".equals(waitingReason)){
                return "任务已下发到 kubernetes";
            }

            // 将 Phase 置为 FAILED
            pod.setPhase(PodPhaseEnum.FAILED.getPhase());
            return waitingMessgae;
        }
        // 如果 Pod 处于 Pending 状态，从 Condition 取出最新信息返回
        if (StringUtils.equals(pod.getPhase(),PodPhaseEnum.PENDING.getPhase())){
            if (CollectionUtil.isNotEmpty(pod.getConditions())
                    && StringUtils.equals(POD_CONDITION_STATUS_FALSE, pod.getConditions().get(MagicNumConstant.ZERO).getStatus())){
                waitingMessgae = pod.getConditions().get(MagicNumConstant.ZERO).getMessage();
                return waitingMessgae;
            }
            return "任务已下发到 kubernetes";
        }
        return waitingMessgae;
    }

    /**
     * 处理pod删除
     *
     * @param watcherActionEnum 监控枚举类
     * @param pod Pod对象
     */
    private void dealWithDeleted(WatcherActionEnum watcherActionEnum, BizPod pod) {
        if (WatcherActionEnum.DELETED == watcherActionEnum){
            pod.setPhase(PodPhaseEnum.DELETED.getPhase());
            resourceCache.cachePod(pod.getLabel(K8sLabelConstants.BASE_TAG_SOURCE),pod.getName());
            k8sResourceService.create(pod);
        }
    }

    /**
     * 非删除的Unknown状态不回调
     *
     * @param watcherActionEnum 监控枚举类
     * @param pod Pod对象
     * @return boolean true 回调 false 不回调
     */
    private boolean needCallback(WatcherActionEnum watcherActionEnum, BizPod pod) {
        /**环境不匹配则不回调**/
        if (!SpringContextHolder.getActiveProfile().equals(pod.getLabel(K8sLabelConstants.PLATFORM_RUNTIME_ENV))) {
            return false;
        }
        if (WatcherActionEnum.DELETED.getAction().equals(watcherActionEnum.getAction())) {
            return true;
        } else {
            return !(PodPhaseEnum.UNKNOWN.getPhase().equals(pod.getPhase()));
        }
    }
}
