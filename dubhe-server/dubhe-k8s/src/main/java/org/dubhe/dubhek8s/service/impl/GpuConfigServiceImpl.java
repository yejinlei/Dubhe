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

import org.dubhe.biz.base.dto.GpuConfigDTO;
import org.dubhe.dubhek8s.service.GpuConfigService;
import org.dubhe.k8s.domain.dto.K8sGpuConfigDTO;
import org.dubhe.k8s.service.K8sGpuConfigService;
import org.dubhe.k8s.utils.K8sNameTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @description 用户GPU配置管理服务接口实现类
 * @date 2021-9-6
 */
@Service
public class GpuConfigServiceImpl implements GpuConfigService {
    @Autowired
    K8sNameTool k8sNameTool;

    @Autowired
    K8sGpuConfigService k8sGpuConfigService;

    /**
     * 创建或更新k8s GPU配置
     * @param gpuConfigDTO 用户GPU配置实体
     * @return
     */
    @Override
    public void UpdateGpuConfig(GpuConfigDTO gpuConfigDTO) {
        String namespace = k8sNameTool.getNamespace(gpuConfigDTO.getUserId());
        K8sGpuConfigDTO k8sGpuConfigDTO = new K8sGpuConfigDTO();
        k8sGpuConfigDTO.setNamespace(namespace);
        if (!CollectionUtils.isEmpty(gpuConfigDTO.getGpuResources())) {
            k8sGpuConfigDTO.setGpuResources(gpuConfigDTO.getGpuResources());
        }
        k8sGpuConfigService.UpdateGpuConfig(k8sGpuConfigDTO);
    }
}
