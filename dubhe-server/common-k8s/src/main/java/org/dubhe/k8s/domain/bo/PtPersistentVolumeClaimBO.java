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

package org.dubhe.k8s.domain.bo;

import cn.hutool.core.util.RandomUtil;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.k8s.domain.resource.BizQuantity;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.k8s.enums.AccessModeEnum;
import org.dubhe.k8s.enums.PvReclaimPolicyEnum;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @description PVC BO
 * @date 2020-04-23
 */
@Data
@Accessors(chain = true)
public class PtPersistentVolumeClaimBO {
    private String namespace;
    private String pvcName;
    /**
     * 指定StorageClass 的 provisioner
     **/
    private String provisioner;
    /**
     * 指定StorageClass 的 volumeBindingMode
     **/
    private String volumeBindingMode;
    /**
     * 资源配额
     **/
    private Map<String, BizQuantity> capacity;
    /**
     * PVC的accessModes ReadWriteOnce:单node的读写 ReadOnlyMany:多node的只读 ReadWriteMany:多node的读写
     **/
    private Set<String> accessModes;
    /**
     * 标签
     **/
    private Map<String, String> labels;
    /**
     * 用户填写的资源名称
     **/
    private String resourceName;
    /**
     * 关于provisioner的配置，目前用不到
     **/
    private Map<String, String> parameters;
    /**
     * 存储限额
     **/
    private String limit;
    /**
     * 存储配额
     **/
    private String request;
    /**
     * pv挂载存储路径
     **/
    private String path;

    /**
     * 回收策略
     */
    private String reclaimPolicy;

    public PtPersistentVolumeClaimBO() {

    }

    public PtPersistentVolumeClaimBO(PtJupyterResourceBO bo) {
        this.labels = new HashMap<>();
        this.namespace = bo.getNamespace();
        this.request = bo.getWorkspaceRequest();
        this.limit = bo.getWorkspaceLimit();
        this.path = bo.getWorkspaceDir();
        this.resourceName = bo.getName();
        this.accessModes = new HashSet<String>() {{
            add(AccessModeEnum.READ_WRITE_ONCE.getType());
        }};
        this.setPvcName(bo.getName() + "-" + RandomUtil.randomString(MagicNumConstant.FIVE));
    }

    public PtPersistentVolumeClaimBO(String namespace,String resourceName,PtMountDirBO bo){
        this.labels = new HashMap<>();
        this.namespace = namespace;
        this.request = bo.getRequest();
        this.limit = bo.getLimit();
        this.path = bo.getDir();
        this.resourceName = resourceName;
        this.accessModes = new HashSet<String>(){{
            add(AccessModeEnum.READ_WRITE_ONCE.getType());
        }};
        this.setPvcName(resourceName+"-"+RandomUtil.randomString(MagicNumConstant.FIVE));
        if (bo.isRecycle()){
            this.reclaimPolicy = PvReclaimPolicyEnum.RECYCLE.getPolicy();
        }else {
            this.reclaimPolicy = PvReclaimPolicyEnum.RETAIN.getPolicy();
        }
    }
}
