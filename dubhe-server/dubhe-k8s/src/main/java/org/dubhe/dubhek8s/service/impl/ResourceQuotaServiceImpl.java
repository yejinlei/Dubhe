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
package org.dubhe.dubhek8s.service.impl;

import org.dubhe.biz.base.dto.ResourceQuotaDTO;
import org.dubhe.dubhek8s.handler.WebSocketServer;
import org.dubhe.dubhek8s.service.ResourceQuotaService;
import org.dubhe.k8s.api.ResourceQuotaApi;
import org.dubhe.k8s.domain.resource.BizResourceQuota;
import org.dubhe.k8s.utils.K8sNameTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description ResourceQuotaService 实现类
 * @date 2021-7-21
 */
@Service
public class ResourceQuotaServiceImpl implements ResourceQuotaService {
    @Autowired
    ResourceQuotaApi resourceQuotaApi;
    @Autowired
    K8sNameTool k8sNameTool;
    @Autowired
    WebSocketServer webSocketServer;
    @Override
    public boolean UpdateResourceQuota(ResourceQuotaDTO resourceQuotaDTO) {
        String namespace = k8sNameTool.getNamespace(resourceQuotaDTO.getUserId());
        BizResourceQuota bizResourceQuota = resourceQuotaApi.create(namespace, namespace, resourceQuotaDTO.getCpuLimit(),
                resourceQuotaDTO.getMemoryLimit(), resourceQuotaDTO.getGpuLimit());
        webSocketServer.sendToClient(resourceQuotaDTO.getUserId());
       return bizResourceQuota.isSuccess();
    }
}
