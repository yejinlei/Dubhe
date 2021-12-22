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
package org.dubhe.k8s.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dubhe.k8s.domain.entity.K8sGpuConfig;

import java.util.List;
import java.util.Set;

/**
 * @description k8s GPU配置 Mapper
 * @date 2021-9-2
 */
public interface K8sGpuConfigMapper extends BaseMapper<K8sGpuConfig> {

    /**
     * 批量添加用户GPU配置
     *
     * @param userGpuConfigs 用户GPU配置实体集合
     */
    void insertBatchs(List<K8sGpuConfig> userGpuConfigs);

    /**
     *  根据namespace查询用户GPU配置记录数
     * @param namespace 用户id
     * @return Integer 用户GPU配置记录数
     */
    @Select("select count(*) from k8s_gpu_config where namespace= #{namespace}")
    Integer selectCountByNamespace(@Param("namespace") String namespace);

}
