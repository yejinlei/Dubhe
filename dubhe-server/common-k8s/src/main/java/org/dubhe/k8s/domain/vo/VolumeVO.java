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

package org.dubhe.k8s.domain.vo;

import io.fabric8.kubernetes.api.model.EmptyDirVolumeSource;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.PtBaseResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @description 存储卷配置VO
 * @date 2020-09-10
 */
@Data
@Accessors(chain = true)
public class VolumeVO extends PtBaseResult<VolumeVO> {
    private List<VolumeMount> volumeMounts;
    private List<Volume> volumes;

    /**
     * 添加存储卷
     * @param volumeMount Kubernetes VolumeMount
     */
    public void addVolumeMount(VolumeMount volumeMount){
        if (volumeMounts == null){
            volumeMounts = new ArrayList<>();
        }
        volumeMounts.add(volumeMount);
    }

    /**
     * 添加存储卷
     * @param volume Kubernetes volume
     */
    public void addVolume(Volume volume){
        if (volumes == null){
            volumes = new ArrayList<>();
        }
        volumes.add(volume);
    }

    /**
     * 添加shm
     */
    public void addShmFsVolume(Quantity shmMemory){
        addVolumeMount(new VolumeMountBuilder()
                    .withName(K8sParamConstants.SHM_NAME)
                    .withMountPath(K8sParamConstants.SHM_MOUNTPATH)
                    .build());

        addVolume(new VolumeBuilder()
                    .withName(K8sParamConstants.SHM_NAME)
                    .withEmptyDir(new EmptyDirVolumeSource(K8sParamConstants.SHM_MEDIUM, shmMemory))
                    .build());
    }
}
