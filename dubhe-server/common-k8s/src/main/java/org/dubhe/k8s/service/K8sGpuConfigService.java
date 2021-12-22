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
package org.dubhe.k8s.service;

import org.dubhe.k8s.domain.dto.K8sGpuConfigDTO;
import org.dubhe.k8s.domain.entity.K8sGpuConfig;

import java.util.List;

/**
 * @description 用户GPU配置管理服务接口
 * @date 2021-9-6
 */
public interface K8sGpuConfigService {

    /**
     * 根据用户 namespace 查询用户配置
     *
     * @param namespace 命名空间
     * @return List<K8sGpuConfig> 用户配置 VO
     */
    List<K8sGpuConfig> findGpuConfig(String namespace);

    /**
     *  获取用户显卡资源限制配置
     * @param namespace 命名空间
     * @param gpuModel  GPU型号
     * @param k8sLabelKey k8s GPU资源标签key值
     * @return 用户显卡资源限制配置，单位：卡
     */
    Integer getGpuLimit(String namespace,String gpuModel,String k8sLabelKey);


    /**
     * 创建或更新k8s GPU配置
     * @param k8sGpuConfigDTO k8s GPU配置实体
     * @return
     */
    void UpdateGpuConfig(K8sGpuConfigDTO k8sGpuConfigDTO);

    /**
     * 删除k8s资源配置
     * @param namespaces  命名空间
     */
    void delete(List<String> namespaces);
}
