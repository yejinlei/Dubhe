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

package org.dubhe.k8s.api.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.fabric8.kubernetes.api.model.ResourceQuotaBuilder;
import io.fabric8.kubernetes.api.model.ResourceQuotaList;
import io.fabric8.kubernetes.api.model.ScopeSelector;
import io.fabric8.kubernetes.api.model.ScopeSelectorBuilder;
import io.fabric8.kubernetes.api.model.ScopedResourceSelectorRequirement;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.dubhe.enums.LogEnum;
import org.dubhe.k8s.api.ResourceQuotaApi;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.bo.PtResourceQuotaBO;
import org.dubhe.k8s.domain.resource.BizQuantity;
import org.dubhe.k8s.domain.resource.BizResourceQuota;
import org.dubhe.k8s.enums.K8sResponseEnum;
import org.dubhe.k8s.utils.BizConvertUtils;
import org.dubhe.k8s.utils.K8sUtils;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description 资源配额 接口实现
 * @date 2020-04-23
 */
public class ResourceQuotaApiImpl implements ResourceQuotaApi {
    private KubernetesClient client;

    public ResourceQuotaApiImpl(K8sUtils k8sUtils) {
        this.client = k8sUtils.getClient();
    }

    /**
     * 创建 ResourceQuota
     *
     * @param bo ResourceQuota BO
     * @return BizResourceQuota ResourceQuota 业务类
     */
    @Override
    public BizResourceQuota create(PtResourceQuotaBO bo) {
        try {
            LogUtil.info(LogEnum.BIZ_K8S,"Input bo={}", bo);
            Gson gson = new Gson();
            List<ScopedResourceSelectorRequirement> scopeSelector = gson.fromJson(gson.toJson(bo.getScopeSelector()), new TypeToken<List<ScopedResourceSelectorRequirement>>() {
            }.getType());
            Map<String, Quantity> hard = new HashMap<>();
            for (Map.Entry<String, BizQuantity> obj : bo.getHard().entrySet()) {
                hard.put(obj.getKey(), new Quantity(obj.getValue().getAmount(), obj.getValue().getFormat()));
            }
            ScopeSelector item = new ScopeSelectorBuilder().addAllToMatchExpressions(scopeSelector).build();
            ResourceQuota resourceQuota = new ResourceQuotaBuilder().withNewMetadata().withName(bo.getName()).endMetadata()
                    .withNewSpec().withHard(hard).withNewScopeSelectorLike(item).endScopeSelector().endSpec().build();
            BizResourceQuota bizResourceQuota = BizConvertUtils.toBizResourceQuota(client.resourceQuotas().inNamespace(bo.getNamespace()).create(resourceQuota));
            LogUtil.info(LogEnum.BIZ_K8S,"Output {}", bizResourceQuota);
            return bizResourceQuota;
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "ResourceQuotaApiImpl.create error, param:{} error:{}", bo, e);
            return new BizResourceQuota().error(String.valueOf(e.getCode()),e.getMessage());
        }
    }

    /**
     * 根据命名空间查询ResourceQuota集合
     *
     * @param namespace 命名空间
     * @return List<BizResourceQuota> ResourceQuota 业务类集合
     */
    @Override
    public List<BizResourceQuota> list(String namespace) {
        try {
            LogUtil.info(LogEnum.BIZ_K8S,"Input namespace={}", namespace);
            if (StringUtils.isEmpty(namespace)) {
                ResourceQuotaList resourceQuotaList = client.resourceQuotas().inAnyNamespace().list();
                return resourceQuotaList.getItems().parallelStream().map(obj -> BizConvertUtils.toBizResourceQuota(obj)).collect(Collectors.toList());
            } else {
                ResourceQuotaList resourceQuotaList = client.resourceQuotas().inNamespace(namespace).list();
                List<BizResourceQuota> bizResourceQuotaList = resourceQuotaList.getItems().parallelStream().map(obj -> BizConvertUtils.toBizResourceQuota(obj)).collect(Collectors.toList());
                LogUtil.info(LogEnum.BIZ_K8S,"Output {}", bizResourceQuotaList);
                return bizResourceQuotaList;
            }
        }catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "ResourceQuotaApiImpl.list error, param:[namespace]={},error:{}", namespace,e);
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * 删除ResourceQuota
     *
     * @param namespace 命名空间
     * @param name ResourceQuota 名称
     * @return PtBaseResult 基础结果类
     */
    @Override
    public PtBaseResult delete(String namespace, String name) {
        LogUtil.info(LogEnum.BIZ_K8S,"Input namespace={};name={}", namespace,name);
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
            return new PtBaseResult().baseErrorBadRequest();
        }
        try {
            if (client.resourceQuotas().inNamespace(namespace).withName(name).delete()){
                return new PtBaseResult();
            }else {
                return K8sResponseEnum.REPEAT.toPtBaseResult();
            }
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "ResourceQuotaApiImpl.delete error, param:[namespace]={}, [name]={}, error:{}",namespace, name, e);
            return new PtBaseResult(String.valueOf(e.getCode()),e.getMessage());
        }
    }
}
