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
package org.dubhe.k8s.domain.cr;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.*;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description 自定义资源dt详情类
 * @date 2020-07-07
 */
@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class DistributeTrainSpec implements KubernetesResource {
    private Integer size;
    private String image;
    private String imagePullPolicy;
    private String masterCmd;
    private ResourceRequirements masterResources;
    private String slaveCmd;
    private ResourceRequirements slaveResources;
    private Map<String, String> nodeSelector = new HashMap<>();
    private List<EnvVar> env = new ArrayList<>();
    /**
     * 内部映射
     */
    private List<VolumeMount> volumeMounts;
    /**
     * 外部挂载
     */
    private List<Volume> volumes;

    /**
     * 容忍度
     */
    private List<Toleration> tolerations = new ArrayList<>();

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImagePullPolicy() {
        return imagePullPolicy;
    }

    public void setImagePullPolicy(String imagePullPolicy) {
        this.imagePullPolicy = imagePullPolicy;
    }

    public String getMasterCmd() {
        return masterCmd;
    }

    public void setMasterCmd(String masterCmd) {
        this.masterCmd = masterCmd;
    }

    public ResourceRequirements getMasterResources() {
        return masterResources;
    }

    public void setMasterResources(ResourceRequirements masterResources) {
        this.masterResources = masterResources;
    }

    public String getSlaveCmd() {
        return slaveCmd;
    }

    public void setSlaveCmd(String slaveCmd) {
        this.slaveCmd = slaveCmd;
    }

    public ResourceRequirements getSlaveResources() {
        return slaveResources;
    }

    public void setSlaveResources(ResourceRequirements slaveResources) {
        this.slaveResources = slaveResources;
    }

    public Map<String, String> getNodeSelector() {
        return nodeSelector;
    }

    public void setNodeSelector(Map<String, String> nodeSelector) {
        this.nodeSelector = nodeSelector;
    }

    public void addNodeSelector(Map<String, String> nodeSelector){
        if (CollectionUtils.isEmpty(nodeSelector)){
            return;
        }
        if (this.nodeSelector == null){
            this.nodeSelector = nodeSelector;
        }
        this.nodeSelector.putAll(nodeSelector);
    }

    public List<EnvVar> getEnv() {
        return env;
    }

    public void setEnv(List<EnvVar> env) {
        this.env = env;
    }

    public List<VolumeMount> getVolumeMounts() {
        return volumeMounts;
    }

    public void setVolumeMounts(List<VolumeMount> volumeMounts) {
        this.volumeMounts = volumeMounts;
    }

    public List<Volume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<Volume> volumes) {
        this.volumes = volumes;
    }

    public List<Toleration> getTolerations() {
        return tolerations;
    }

    public void setTolerations(List<Toleration> tolerations) {
        this.tolerations = tolerations;
    }

    public void addTolerations(List<Toleration> tolerations){
        if (CollectionUtils.isEmpty(tolerations)){
            return ;
        }
        if (this.tolerations == null){
            this.tolerations = tolerations;
        }
        this.tolerations.addAll(tolerations);
    }
}
