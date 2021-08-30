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

package org.dubhe.k8s.api.impl;

import com.alibaba.fastjson.JSON;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.k8s.annotation.K8sValidation;
import org.dubhe.k8s.api.NamespaceApi;
import org.dubhe.k8s.api.ResourceQuotaApi;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.resource.BizNamespace;
import org.dubhe.k8s.enums.K8sResponseEnum;
import org.dubhe.k8s.enums.ValidationTypeEnum;
import org.dubhe.k8s.utils.BizConvertUtils;
import org.dubhe.k8s.utils.K8sUtils;
import org.dubhe.k8s.utils.LabelUtils;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.base.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description NamespaceApi 实现类
 * @date 2020-04-15
 */
public class NamespaceApiImpl implements NamespaceApi {

    private KubernetesClient client;

    @Autowired
    private ResourceQuotaApi resourceQuotaApi;

    @Autowired
    private UserContextService userContextService;

    @Value("${user.config.cpu-limit}")
    private Integer cpuLimit;

    @Value("${user.config.memory-limit}")
    private Integer memoryLimit;

    @Value("${user.config.gpu-limit}")
    private Integer gpuLimit;



    public NamespaceApiImpl(K8sUtils k8sUtils) {
        this.client = k8sUtils.getClient();
    }

    /**
     * 创建NamespaceLabels，为null则不添加标签
     *
     * @param namespace 命名空间
     * @param labels 标签Map
     * @return BizNamespace Namespace 业务类
     */
    @Override
    public BizNamespace create(@K8sValidation(ValidationTypeEnum.K8S_RESOURCE_NAME) String namespace, Map<String, String> labels) {
        try {
            BizNamespace bizNamespace = get(namespace);
            if (bizNamespace != null){
                return bizNamespace;
            }
            Namespace ns = new NamespaceBuilder().withNewMetadata().withName(namespace).addToLabels(LabelUtils.getBaseLabels(namespace, labels)).endMetadata().build();
            Namespace res = client.namespaces().create(ns);
            resourceQuotaApi.create(res.getMetadata().getName(),res.getMetadata().getName(),cpuLimit,memoryLimit,gpuLimit);
            return BizConvertUtils.toBizNamespace(res);
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "NamespaceApiImpl.create error, param:[namespace]={}, [labels]={},error:{}",namespace, labels, e);
            return new BizNamespace().error(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 根据namespace查询BizNamespace
     *
     * @param namespace 命名空间
     * @return BizNamespace Namespace 业务类
     */
    @Override
    public BizNamespace get(String namespace) {
        if (StringUtils.isEmpty(namespace)) {
            return new BizNamespace().baseErrorBadRequest();
        }
        Namespace namespaceEntity = client.namespaces().withName(namespace).get();
        return BizConvertUtils.toBizNamespace(namespaceEntity);
    }

    /**
     * 查询所有的BizNamespace
     *
     * @return List<BizNamespace> Namespace 业务类集合
     */
    @Override
    public List<BizNamespace> listAll() {
        NamespaceList namespaceList = client.namespaces().list();
        if (namespaceList == null || CollectionUtils.isEmpty(namespaceList.getItems())) {
            return Collections.emptyList();
        }
        return namespaceList.getItems().parallelStream().map(obj -> BizConvertUtils.toBizNamespace(obj)).collect(Collectors.toList());
    }

    /**
     * 根据label标签查询所有的BizNamespace
     *
     * @param labelKey 标签的键
     * @return List<BizNamespace> Namespace 业务类集合
     */
    @Override
    public List<BizNamespace> list(String labelKey) {
        if (StringUtils.isEmpty(labelKey)) {
            return Collections.EMPTY_LIST;
        }
        NamespaceList namespaceList = client.namespaces().withLabel(labelKey, null).list();
        if (namespaceList == null || CollectionUtils.isEmpty(namespaceList.getItems())) {
            return Collections.EMPTY_LIST;
        }
        return namespaceList.getItems().parallelStream().map(obj -> BizConvertUtils.toBizNamespace(obj)).collect(Collectors.toList());
    }

    /**
     * 根据label标签集合查询所有的BizNamespace数据
     *
     * @param labels 标签键的集合
     * @return List<BizNamespace> Namespace业务类集合
     */
    @Override
    public List<BizNamespace> list(Set<String> labels) {
        if (CollectionUtils.isEmpty(labels)) {
            return Collections.EMPTY_LIST;
        }
        Map<String, String> map = new HashMap<>();
        Iterator<String> it = labels.iterator();
        while (it.hasNext()) {
            //根据label的key查询,无需关系value值
            map.put(it.next(), null);
        }
        NamespaceList namespaceList = client.namespaces().withLabels(map).list();
        if (namespaceList == null || CollectionUtils.isEmpty(namespaceList.getItems())) {
            return Collections.EMPTY_LIST;
        }
        return namespaceList.getItems().parallelStream().map(obj -> BizConvertUtils.toBizNamespace(obj)).collect(Collectors.toList());
    }

    /**
     * 删除命名空间
     *
     * @param namespace 命名空间
     * @return PtBaseResult 基础结果类
     */
    @Override
    public PtBaseResult delete(String namespace) {
        LogUtil.info(LogEnum.BIZ_K8S, "Param of delete namespace {}", namespace);
        if (StringUtils.isEmpty(namespace)) {
            return new PtBaseResult().baseErrorBadRequest();
        }
        try {
            if (client.namespaces().withName(namespace).delete()) {
                return new PtBaseResult();
            } else {
                return K8sResponseEnum.REPEAT.toPtBaseResult();
            }
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "NamespaceApiImpl.delete error, param:[namespace]={}, error:{}", namespace, e);
            return new PtBaseResult(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 删除命名空间的标签
     *
     * @param namespace 命名空间
     * @param labelKey 标签的键
     * @return PtBaseResult 基础结果类
     */
    @Override
    public PtBaseResult removeLabel(String namespace, String labelKey) {
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(labelKey)) {
            return new PtBaseResult().baseErrorBadRequest();
        }
        try {
            client.namespaces().withName(namespace)
                    .edit()
                    .editMetadata()
                    .removeFromLabels(labelKey)
                    .endMetadata()
                    .done();
            return new PtBaseResult();
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "NamespaceApiImpl.removeLabel error, param:[namespace]={}, [labelKey]={}, error:{}", namespace, labelKey, e);
            return new PtBaseResult(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 删除命名空间下的多个标签
     *
     * @param namespace 命名空间
     * @param labels 标签键的集合
     * @return PtBaseResult 基础结果类
     */
    @Override
    public PtBaseResult removeLabels(String namespace, Set<String> labels) {
        if (StringUtils.isEmpty(namespace) || CollectionUtils.isEmpty(labels)) {
            return new PtBaseResult().baseErrorBadRequest();
        }
        try {
            Map<String, String> map = new HashMap<>();
            Iterator<String> it = labels.iterator();
            while (it.hasNext()) {
                //根据label的key查询,无需关系value值
                map.put(it.next(), null);
            }
            client.namespaces().withName(namespace)
                    .edit()
                    .editMetadata()
                    .removeFromLabels(map)
                    .endMetadata()
                    .done();
            return new PtBaseResult();
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "NamespaceApiImpl.removeLabel error, param:[namespace]={}, [labels]={},error:{}", namespace, labels, e);
            return new PtBaseResult(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 将labelKey和labelValue添加到指定的命名空间
     *
     * @param namespace 命名空间
     * @param labelKey 标签的键
     * @param labelValue 标签的值
     * @return PtBaseResult 基础结果类
     */
    @Override
    public PtBaseResult addLabel(String namespace, String labelKey, String labelValue) {
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(labelKey)) {
            return new PtBaseResult().baseErrorBadRequest();
        }
        try {
            client.namespaces().withName(namespace).edit().editMetadata().addToLabels(labelKey, labelValue).endMetadata().done();
            return new PtBaseResult();
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "NamespaceApiImpl.addLabel error, param:[namespace]={}, [labelKey]={}, [labelValue]={}, error:{}", namespace, labelKey, labelValue, e);
            return new PtBaseResult(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 将多个label标签添加到指定的命名空间
     *
     * @param namespace 命名空间
     * @param labels 标签
     * @return PtBaseResult 基础结果类
     */
    @Override
    public PtBaseResult addLabels(String namespace, Map<String, String> labels) {
        if (StringUtils.isEmpty(namespace) || CollectionUtils.isEmpty(labels)) {
            return new PtBaseResult().baseErrorBadRequest();
        }
        try {
            client.namespaces().withName(namespace).edit().editMetadata().addToLabels(labels).endMetadata().done();
            return new PtBaseResult();
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "NamespaceApiImpl.addLabels error, param:[namespace]={}, [labels]={},error:{}",namespace, JSON.toJSONString(labels), e);
            return new PtBaseResult(String.valueOf(e.getCode()), e.getMessage());
        }
    }


    /**
     * 命名空间的资源限制
     *
     * @param namespace 命名空间
     * @param quota 资源限制参数类
     * @return ResourceQuota 资源限制参数类
     */
    @Override
    public ResourceQuota addResourceQuota(String namespace, ResourceQuota quota) {
        return client.resourceQuotas().inNamespace(namespace).create(quota);
    }

    /**
     * 获得命名空间下的所有的资源限制
     *
     * @param namespace 命名空间
     * @return List<ResourceQuota> 资源限制参数类
     */
    @Override
    public List<ResourceQuota> listResourceQuotas(String namespace) {
        return client.resourceQuotas().inNamespace(namespace).list().getItems();
    }

    /**
     * 解除对命名空间的资源限制
     *
     * @param namespace 命名空间
     * @param quota 资源限制参数类
     * @return boolean  true成功 false失败
     */
    @Override
    public boolean removeResourceQuota(String namespace, ResourceQuota quota) {
        return client.resourceQuotas().inNamespace(namespace).delete(quota);
    }
}
