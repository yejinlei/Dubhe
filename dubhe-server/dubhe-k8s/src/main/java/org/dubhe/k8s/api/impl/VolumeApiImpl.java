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

import cn.hutool.core.collection.CollectionUtil;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.k8s.api.PersistentVolumeClaimApi;
import org.dubhe.k8s.api.VolumeApi;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.bo.BuildNfsVolumeBO;
import org.dubhe.k8s.domain.bo.PtMountDirBO;
import org.dubhe.k8s.domain.bo.PtPersistentVolumeClaimBO;
import org.dubhe.k8s.domain.resource.BizPersistentVolumeClaim;
import org.dubhe.k8s.domain.vo.VolumeVO;
import org.dubhe.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @description Kubernetes Volume api implements
 * @date 2020-09-10
 */
@Service
public class VolumeApiImpl implements VolumeApi {
    @Autowired
    private PersistentVolumeClaimApi persistentVolumeClaimApi;

    /**
     * 构建Nfs存储卷
     *
     * @param bo Nfs存储卷参数
     * @return
     */
    @Override
    public VolumeVO buildNfsVolumes(BuildNfsVolumeBO bo) {
        if (bo == null || StringUtils.isEmpty(bo.getNfs())){
            return new VolumeVO().errorBadRequest();
        }
        VolumeVO volumeVO = new VolumeVO();
        if (CollectionUtil.isNotEmpty(bo.getNfsMounts())){
            int i = MagicNumConstant.ZERO;
            for (Map.Entry<String, PtMountDirBO> mount : bo.getNfsMounts().entrySet()) {
                boolean availableMount = (mount != null && StringUtils.isNotEmpty(mount.getKey()) && mount.getValue() != null && StringUtils.isNotEmpty(mount.getValue().getDir()));
                if (availableMount){
                    boolean success = mount.getValue().isRecycle()?buildNfsPvcVolumes(bo,volumeVO,mount.getKey(),mount.getValue(),i):buildNfsVolumes(volumeVO,bo.getNfs(),mount.getKey(),mount.getValue(),i);
                    if (!success){
                        break;
                    }
                    i++;
                }
            }
        }
        return volumeVO;
    }

    /**
     * 构建存储卷
     *
     * @param mountPath 挂载路径
     * @param dirBO 挂载路径参数
     * @param num 名称序号
     * @return boolean
     */
    private boolean buildNfsVolumes(VolumeVO volumeVO, String nfs, String mountPath, PtMountDirBO dirBO, int num){
        if (volumeVO == null || StringUtils.isEmpty(nfs) || StringUtils.isEmpty(mountPath) || dirBO == null){
            return false;
        }
        volumeVO.addVolumeMount(new VolumeMountBuilder()
                .withName(K8sParamConstants.VOLUME_PREFIX+num)
                .withMountPath(mountPath)
                .withReadOnly(dirBO.isReadOnly())
                .build());
        volumeVO.addVolume(new VolumeBuilder()
                .withName(K8sParamConstants.VOLUME_PREFIX+num)
                .withNewNfs()
                .withPath(dirBO.getDir())
                .withServer(nfs)
                .endNfs()
                .build());
        return true;
    }

    /**
     * 按照存储资源声明挂载存储
     *
     * @param mountPath 挂载路径
     * @param dirBO 挂载路径参数
     * @param i 名称序号
     * @return boolean
     */
    private boolean buildNfsPvcVolumes(BuildNfsVolumeBO bo, VolumeVO volumeVO, String mountPath, PtMountDirBO dirBO, int i){
        BizPersistentVolumeClaim bizPersistentVolumeClaim = persistentVolumeClaimApi.createWithNfsPv(new PtPersistentVolumeClaimBO(bo.getNamespace(),bo.getResourceName(),dirBO));
        if (bizPersistentVolumeClaim.isSuccess()){
            volumeVO.addVolumeMount(new VolumeMountBuilder()
                    .withName(K8sParamConstants.VOLUME_PREFIX+i)
                    .withMountPath(mountPath)
                    .withReadOnly(dirBO.isReadOnly())
                    .build());
            volumeVO.addVolume(new VolumeBuilder()
                    .withName(K8sParamConstants.VOLUME_PREFIX+i)
                    .withNewPersistentVolumeClaim(bizPersistentVolumeClaim.getName(), dirBO.isReadOnly())
                    .build());
            return true;
        }else {
            volumeVO.error(bizPersistentVolumeClaim.getCode(),bizPersistentVolumeClaim.getMessage());
        }
        return false;
    }
}
