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
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.dubhe.biz.base.utils.SpringContextHolder;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.constant.K8sLabelConstants;
import org.dubhe.k8s.dao.K8sResourceMapper;
import org.dubhe.k8s.domain.entity.K8sResource;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.enums.K8sKindEnum;
import org.dubhe.k8s.service.K8sResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @description k8s资源服务实现类
 * @date 2020-07-13
 */
@Service
public class K8sResourceServiceImpl implements K8sResourceService {
    @Autowired
    private K8sResourceMapper k8sResourceMapper;

    /**
     * 根据pod插入
     *
     * @param pod Pod对象
     * @return int 插入数量
     */
    @Override
    public int create(BizPod pod) {
        return create(new K8sResource(K8sKindEnum.POD.getKind(),pod.getNamespace(),pod.getName(),pod.getLabel(K8sLabelConstants.BASE_TAG_SOURCE), SpringContextHolder.getActiveProfile(),pod.getBusinessLabel()));
    }

    /**
     * 插入
     *
     * @param k8sResource
     * @return int 插入数量
     */
    @Override
    public int create(K8sResource k8sResource) {
        if (null == k8sResource){
            return 0;
        }
        QueryWrapper<K8sResource> queryK8sResourceJonWrapper = new QueryWrapper<>(k8sResource);
        List<K8sResource> list = k8sResourceMapper.selectList(queryK8sResourceJonWrapper);
        if (CollectionUtil.isEmpty(list)){
            LogUtil.info(LogEnum.BIZ_K8S,"insert k8sResource:{}",k8sResource);
            return k8sResourceMapper.insert(k8sResource);
        }else {
            LogUtil.warn(LogEnum.BIZ_K8S,"k8sResource already exist:{}",k8sResource);
            return 0;
        }
    }

    /**
     * 根据资源名查询
     *
     * @param kind 资源类型
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return List<K8sResource> K8sResource集合
     */
    @Override
    public List<K8sResource> selectByResourceName(String kind, String namespace, String resourceName) {
        if (StringUtils.isEmpty(kind) || StringUtils.isEmpty(namespace) || StringUtils.isEmpty(resourceName)) {
            return new ArrayList<>(0);
        }
        QueryWrapper<K8sResource> queryK8sResourceJonWrapper = new QueryWrapper<>();
        queryK8sResourceJonWrapper.eq("kind", kind)
                .eq("namespace", namespace)
                .eq("resource_name", resourceName)
                .eq("env", SpringContextHolder.getActiveProfile())
                .eq("deleted", 0)
                .orderByDesc("create_time");
        return k8sResourceMapper.selectList(queryK8sResourceJonWrapper);
    }

    /**
     * 根据对象名查询
     *
     * @param kind 资源名称
     * @param namespace 命名空间
     * @param name 资源名字
     * @return List<K8sResource>  K8sResource集合
     */
    @Override
    public List<K8sResource> selectByName(String kind, String namespace, String name) {
        if (StringUtils.isEmpty(kind) || StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
            return new ArrayList<>(0);
        }
        QueryWrapper<K8sResource> queryK8sResourceJonWrapper = new QueryWrapper<>();
        queryK8sResourceJonWrapper.eq("kind", kind)
                .eq("namespace", namespace)
                .eq("name", name)
                .eq("env", SpringContextHolder.getActiveProfile())
                .eq("deleted", 0)
                .orderByDesc("create_time");
        return k8sResourceMapper.selectList(queryK8sResourceJonWrapper);
    }

    /**
     * 根据resourceName删除
     * @param kind 资源类型
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return int 删除数量
     */
    @Override
    public int deleteByResourceName(String kind, String namespace, String resourceName) {
        if (StringUtils.isEmpty(kind) || StringUtils.isEmpty(namespace) || StringUtils.isEmpty(resourceName)) {
            return 0;
        }
        UpdateWrapper<K8sResource> updateK8sResourceJonWrapper = new UpdateWrapper<>();
        updateK8sResourceJonWrapper.eq("kind", kind)
                .eq("namespace", namespace)
                .eq("resource_name", resourceName)
                .eq("env", SpringContextHolder.getActiveProfile())
                .eq("deleted", 0).set("deleted", 1);

        return k8sResourceMapper.update(null,updateK8sResourceJonWrapper);
    }

    @Override
    public int deleteByName(String kind, String namespace, String name) {
        if (StringUtils.isEmpty(kind) || StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
            return 0;
        }
        UpdateWrapper<K8sResource> updateK8sResourceJonWrapper = new UpdateWrapper<>();
        updateK8sResourceJonWrapper.eq("kind", kind)
                .eq("namespace", namespace)
                .eq("name", name)
                .eq("env", SpringContextHolder.getActiveProfile())
                .eq("deleted", 0).set("deleted", 1);

        return k8sResourceMapper.update(null,updateK8sResourceJonWrapper);
    }
}
