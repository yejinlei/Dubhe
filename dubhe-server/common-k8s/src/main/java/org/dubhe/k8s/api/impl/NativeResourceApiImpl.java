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

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.k8s.api.NativeResourceApi;
import org.dubhe.k8s.utils.K8sUtils;
import org.dubhe.biz.log.utils.LogUtil;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @description 本地资源实现类
 * @date 2020-08-28
 */
public class NativeResourceApiImpl implements NativeResourceApi {
    private KubernetesClient client;
    public NativeResourceApiImpl(K8sUtils k8sUtils) {
        this.client = k8sUtils.getClient();
    }

    /**
     *
     * @param crYaml cr定义yaml脚本
     * @return List<HasMetadata> HasMetadata资源集合
     */
    @Override
    public List<HasMetadata> create(String crYaml) {
        LogUtil.info(LogEnum.BIZ_K8S, "Param of create crYaml {}", crYaml);
        try {
            return client.load(new ByteArrayInputStream(crYaml.getBytes())).deletingExisting().createOrReplace();
        }catch (KubernetesClientException e){
            LogUtil.error(LogEnum.BIZ_K8S, "Create NativeResource error:{} ,yml:{}", e,crYaml);
            return new ArrayList<>();
        }
    }

    /**
     *
     * @param crYaml cr定义yaml脚本
     * @return boolean true删除成功 false删除失败
     */
    @Override
    public boolean delete(String crYaml) {
        LogUtil.info(LogEnum.BIZ_K8S, "Param of delete crYaml {}", crYaml);
        try {
            return client.load(new ByteArrayInputStream(crYaml.getBytes())).delete();
        }catch (KubernetesClientException e){
            LogUtil.error(LogEnum.BIZ_K8S, "Delete NativeResource error:{} ,yml:{}", e,crYaml);
            return false;
        }
    }
}
