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

package org.dubhe.k8s.api;

import io.fabric8.kubernetes.api.model.PersistentVolume;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.bo.PtPersistentVolumeClaimBO;
import org.dubhe.k8s.domain.resource.BizPersistentVolumeClaim;

import java.util.List;

/**
 * @description k8s中资源为持久卷声明操作接口
 * @date 2020-07-03
 */
public interface PersistentVolumeClaimApi {
    /**
     * 创建PVC
     *
     * @param bo PVC BO
     * @return BizPersistentVolumeClaim PVC业务类
     */
    BizPersistentVolumeClaim create(PtPersistentVolumeClaimBO bo);

    /**
     * 创建挂载文件存储服务 PV
     *
     * @param bo PVC bo
     * @return BizPersistentVolumeClaim PVC业务类
     */
    BizPersistentVolumeClaim createWithFsPv(PtPersistentVolumeClaimBO bo);

    /**
     * 创建挂载直接存储 PV
     *
     * @param bo PVC BO
     * @return BizPersistentVolumeClaim PVC业务类
     */
    BizPersistentVolumeClaim createWithDirectPv(PtPersistentVolumeClaimBO bo);

    /**
     * 创建创建自动挂载nfs动态存储的 PVC
     *
     * @param bo PVC bo
     * @return BizPersistentVolumeClaim PVC 业务类
     */
    BizPersistentVolumeClaim createDynamicNfs(PtPersistentVolumeClaimBO bo);

    /**
     * 查询命名空间下所有PVC
     *
     * @param namespace 命名空间
     * @return List<BizPersistentVolumeClaim> PVC 业务类集合
     */
    List<BizPersistentVolumeClaim> list(String namespace);

    /**
     * 回收存储（recycle 的pv才能回收）
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return PtBaseResult 基础结果类
     */
    PtBaseResult recycle(String namespace, String resourceName);

    /**
     * 删除具体的PVC
     *
     * @param namespace 命名空间
     * @param pvcName PVC名称
     * @return PtBaseResult 基础结果类
     */
    PtBaseResult delete(String namespace, String pvcName);

    /**
     * 拼接storageClassName
     *
     * @param pvcName PVC 名称
     * @return String storageClassName
     */
    String getStorageClassName(String pvcName);

    /**
     * 删除PV
     *
     * @param pvName PV 名称
     * @return boolean true删除成功 false删除失败
     */
    boolean deletePv(String pvName);

    /**
     * 删除PV
     *
     * @param resourceName 资源名称
     * @return boolean true成功 false失败
     */
    boolean deletePvByResourceName(String resourceName);

    /**
     * 查询PV
     *
     * @param pvName PV 名称
     * @return PersistentVolume PV 实体类
     */
    PersistentVolume getPv(String pvName);
}
