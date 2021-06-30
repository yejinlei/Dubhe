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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.fabric8.kubernetes.api.model.LimitRange;
import io.fabric8.kubernetes.api.model.LimitRangeBuilder;
import io.fabric8.kubernetes.api.model.LimitRangeItem;
import io.fabric8.kubernetes.api.model.LimitRangeList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.k8s.api.LimitRangeApi;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.bo.PtLimitRangeBO;
import org.dubhe.k8s.domain.resource.BizLimitRange;
import org.dubhe.k8s.enums.K8sResponseEnum;
import org.dubhe.k8s.utils.BizConvertUtils;
import org.dubhe.k8s.utils.K8sUtils;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.base.utils.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description LimitRangeApi 实现类
 * @date 2020-04-23
 */
public class LimitRangeApiImpl implements LimitRangeApi {
    private KubernetesClient client;

    public LimitRangeApiImpl(K8sUtils k8sUtils) {
        this.client = k8sUtils.getClient();
    }

    /**
     * 创建LimitRange
     *
     * @param bo LimitRange BO
     * @return BizLimitRange LimitRange 业务类
     */
    @Override
    public BizLimitRange create(PtLimitRangeBO bo) {
        try {
            LogUtil.info(LogEnum.BIZ_K8S, "Input {}", bo);
            Gson gson = new Gson();
            List<LimitRangeItem> limits = gson.fromJson(gson.toJson(bo.getLimits()), new TypeToken<List<LimitRangeItem>>() {
            }.getType());
            LimitRange limitRange = new LimitRangeBuilder().withNewMetadata().withName(bo.getName()).endMetadata()
                    .withNewSpec().withLimits(limits).endSpec().build();
            BizLimitRange bizLimitRange = BizConvertUtils.toBizLimitRange(client.limitRanges().inNamespace(bo.getNamespace()).create(limitRange));
            LogUtil.info(LogEnum.BIZ_K8S, "Output {}", bizLimitRange);
            return bizLimitRange;
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "LimitRangeApiImpl.create error, param:{} error:{}", bo, e);
            return new BizLimitRange().error(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 查询命名空间下所有LimitRange
     *
     * @param namespace 命名空间
     * @return List<BizLimitRange> LimitRange 业务类集合
     */
    @Override
    public List<BizLimitRange> list(String namespace) {
        try {
            LogUtil.info(LogEnum.BIZ_K8S, "Input namespace={}", namespace);
            if (StringUtils.isEmpty(namespace)) {
                LimitRangeList limitRangeList = client.limitRanges().inAnyNamespace().list();
                return limitRangeList.getItems().parallelStream().map(obj -> BizConvertUtils.toBizLimitRange(obj)).collect(Collectors.toList());
            } else {
                LimitRangeList limitRangeList = client.limitRanges().inNamespace(namespace).list();
                List<BizLimitRange> bizLimitRangeList = limitRangeList.getItems().parallelStream().map(obj -> BizConvertUtils.toBizLimitRange(obj)).collect(Collectors.toList());
                LogUtil.info(LogEnum.BIZ_K8S, "Output {}", bizLimitRangeList);
                return bizLimitRangeList;
            }
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "LimitRangeApiImpl.list error, param:[namespace]={},error:", namespace, e);
            return Collections.EMPTY_LIST;
        }

    }

    /**
     * 删除LimitRange
     *
     * @param namespace 命名空间
     * @param name LimitRange 名称
     * @return PtBaseResult 基本结果类
     */
    @Override
    public PtBaseResult delete(String namespace, String name) {
        LogUtil.info(LogEnum.BIZ_K8S, "Input namespace={};name={}", namespace, name);
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
            return new PtBaseResult().baseErrorBadRequest();
        }
        try {
            if (client.limitRanges().inNamespace(namespace).withName(name).delete()) {
                return new PtBaseResult();
            } else {
                return K8sResponseEnum.REPEAT.toPtBaseResult();
            }
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "LimitRangeApiImpl.delete error, param:[namespace]={}, [name]={}, error:",namespace, name, e);
            return new PtBaseResult(String.valueOf(e.getCode()), e.getMessage());
        }
    }
}
