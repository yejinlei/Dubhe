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
package org.dubhe.k8s.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.dubhe.k8s.dao.K8sGpuConfigMapper;
import org.dubhe.k8s.domain.dto.K8sGpuConfigDTO;
import org.dubhe.k8s.domain.entity.K8sGpuConfig;
import org.dubhe.k8s.service.K8sGpuConfigService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @description 用户GPU配置管理服务接口实现类
 * @date 2021-9-6
 */
@Service
public class K8sGpuConfigServiceImpl implements K8sGpuConfigService {

    @Autowired
    private K8sGpuConfigMapper k8sGpuConfigMapper;

    /**
     * 根据用户 namespace 查询用户配置
     *
     * @param namespace 命名空间
     * @return List<K8sGpuConfig> 用户配置 VO
     */
    @Override
    public List<K8sGpuConfig> findGpuConfig(String namespace) {
        List<K8sGpuConfig> k8sGpuConfigs = k8sGpuConfigMapper.selectList(new QueryWrapper<>(new K8sGpuConfig().setNamespace(namespace)));
        // 如果老用户未初始化GPU配置，则返回默认配置
        if (CollectionUtils.isEmpty(k8sGpuConfigs) && k8sGpuConfigMapper.selectCountByNamespace(namespace) == 0) {
            List<K8sGpuConfig> preUserGpuConfigs = k8sGpuConfigMapper.selectList(new QueryWrapper<>(new K8sGpuConfig().setNamespace("namespace-0")));
            if (CollectionUtil.isNotEmpty(preUserGpuConfigs)) {
                k8sGpuConfigs.addAll(preUserGpuConfigs);
            }
        }
        return k8sGpuConfigs;
    }

    /**
     *  获取用户显卡资源限制配置
     * @param namespace 命名空间
     * @param gpuModel  GPU型号
     * @param k8sLabelKey k8s GPU资源标签key值
     * @return 用户显卡资源限制配置，单位：卡
     */
    @Override
    public Integer getGpuLimit(String namespace, String gpuModel, String k8sLabelKey) {
        K8sGpuConfig k8sGpuConfig = k8sGpuConfigMapper.selectOne(new QueryWrapper<>(new K8sGpuConfig().setNamespace(namespace).setGpuModel(gpuModel).setK8sLabelKey(k8sLabelKey)).last(" limit 1 "));
        Integer gpuLimit = 0;
        if (k8sGpuConfig != null) {
            gpuLimit = k8sGpuConfig.getGpuLimit();
        }
        // 如果老用户未初始化GPU配置，则返回默认配置
        if (k8sGpuConfig == null && k8sGpuConfigMapper.selectCountByNamespace(namespace) == 0) {
            K8sGpuConfig preK8sGpuConfig = k8sGpuConfigMapper.selectOne(new QueryWrapper<>(new K8sGpuConfig().setNamespace("namespace-0").setGpuModel(gpuModel).setK8sLabelKey(k8sLabelKey)));
            if (preK8sGpuConfig != null) {
                gpuLimit = preK8sGpuConfig.getGpuLimit();
            }
        }
        return gpuLimit;
    }

    /**
     * 创建或更新k8s GPU配置
     * @param k8sGpuConfigDTO k8s GPU配置实体
     * @return
     */
    @Override
    public void  UpdateGpuConfig(K8sGpuConfigDTO k8sGpuConfigDTO) {
        if (k8sGpuConfigMapper.selectCount(new QueryWrapper<>(new K8sGpuConfig().setNamespace(k8sGpuConfigDTO.getNamespace()))) > 0) {
            k8sGpuConfigMapper.delete(new QueryWrapper<>(new K8sGpuConfig().setNamespace(k8sGpuConfigDTO.getNamespace())));
        }
        if (!CollectionUtils.isEmpty(k8sGpuConfigDTO.getGpuResources())) {
            List<K8sGpuConfig> k8sGpuConfigs = k8sGpuConfigDTO.getGpuResources().stream().map(x ->
            {
                K8sGpuConfig k8sGpuConfig = new K8sGpuConfig();
                BeanUtils.copyProperties(x, k8sGpuConfig);
                k8sGpuConfig.setNamespace(k8sGpuConfigDTO.getNamespace());
                return k8sGpuConfig;
            }).collect(Collectors.toList());
            k8sGpuConfigMapper.insertBatchs(k8sGpuConfigs);
        }

    }

    /**
     * 删除k8s资源配置
     * @param namespaces  命名空间
     */
    @Override
    public void delete(List<String> namespaces) {
        if(CollectionUtil.isNotEmpty(namespaces)){
            QueryWrapper<K8sGpuConfig> k8sGpuConfigWrapper = new QueryWrapper<>();
            k8sGpuConfigWrapper.in("namespace",namespaces);
            k8sGpuConfigMapper.delete(k8sGpuConfigWrapper);
        }
    }
}
