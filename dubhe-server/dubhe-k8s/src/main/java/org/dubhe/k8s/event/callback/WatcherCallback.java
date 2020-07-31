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

package org.dubhe.k8s.event.callback;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import org.dubhe.base.DataResponseBody;
import org.dubhe.base.ResponseCode;
import org.dubhe.dto.callback.BaseK8sPodCallbackCreateDTO;
import org.dubhe.enums.LogEnum;
import org.dubhe.k8s.cache.ResourceCache;
import org.dubhe.k8s.constant.K8sLabelConstants;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.enums.PodPhaseEnum;
import org.dubhe.k8s.enums.WatcherActionEnum;
import org.dubhe.k8s.service.K8sResourceService;
import org.dubhe.utils.K8sCallBackTool;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.SpringContextHolder;
import org.dubhe.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Observable;

/**
 * @description watcher回调
 * @date 2020-06-02
 */
@Component
public class WatcherCallback extends Observable {
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
            String businessLabel = pod.getBusinessLabel();
            LogUtil.info(LogEnum.BIZ_K8S,"watch pod {} action:{} phase:{}",pod.getName(),watcherActionEnum.getAction(),pod.getPhase());
            setChanged();
            notifyObservers(pod);
            if (StringUtils.isNotEmpty(businessLabel) && needCallback(watcherActionEnum,pod)){
                dealWithDeleted(watcherActionEnum,pod);
                BaseK8sPodCallbackCreateDTO baseK8sPodCallbackCreateDTO = new BaseK8sPodCallbackCreateDTO(pod.getNamespace(), pod.getLabel(K8sLabelConstants.BASE_TAG_SOURCE),pod.getName(), pod.getPhase(), pod.getContainerStateMessages());
                String url = k8sCallBackTool.getPodCallbackUrl(businessLabel);
                String token = k8sCallBackTool.generateToken();
                String response = HttpRequest.post(url)
                        .header(K8sCallBackTool.K8S_CALLBACK_TOKEN, token)
                        .body(JSON.toJSONString(baseK8sPodCallbackCreateDTO))
                        .timeout(TIMEOUT_MILLISECOND)
                        .execute().body();
                if (StringUtils.isNotEmpty(response)) {
                    DataResponseBody dataResponseBody = JSON.parseObject(response, DataResponseBody.class);
                    if (ResponseCode.SUCCESS.equals(dataResponseBody.getCode())) {
                        return;
                    }
                    LogUtil.error(LogEnum.BIZ_K8S, "{}  pod {} {} callback error：{} ", url, pod.getName(), watcherActionEnum.getAction(), dataResponseBody.getMsg());
                    return;
                }
                LogUtil.error(LogEnum.BIZ_K8S, "{}  pod {} {} callback not return", url, pod.getName(), watcherActionEnum.getAction());
                return;
            }
            LogUtil.info(LogEnum.BIZ_K8S, "watch pod {} action:{} phase:{}", pod.getName(), watcherActionEnum.getAction(), pod.getPhase());
        } catch (JSONException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "callback error，return string cannot be resolved to Json：{}", e.getMessage());
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_K8S, "callback error {}", e.getMessage());
        }
    }

    /**
     * 处理pod删除
     *
     * @param watcherActionEnum 监控枚举类
     * @param pod Pod对象
     */
    private void dealWithDeleted(WatcherActionEnum watcherActionEnum, BizPod pod) {
        if (WatcherActionEnum.DELETED.getAction().equals(watcherActionEnum.getAction())){
            pod.setPhase(PodPhaseEnum.DELETED.getPhase());
            resourceCache.cachePod(pod.getLabel(K8sLabelConstants.BASE_TAG_SOURCE),pod.getName());
            k8sResourceService.create(pod);
        }
    }

    /**
     * 非删除的pending,Unknown状态不回调
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
            return !(PodPhaseEnum.PENDING.getPhase().equals(pod.getPhase()) || PodPhaseEnum.UNKNOWN.getPhase().equals(pod.getPhase()));
        }
    }
}
