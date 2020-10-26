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
package org.dubhe.k8s.domain.cr;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.*;

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
    private List<EnvVar> env = new ArrayList<EnvVar>();
    private Volume datasetStorage;
    private Volume workspaceStorage;
    private Volume modelStorage;



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

    public List<EnvVar> getEnv() {
        return env;
    }

    public void setEnv(List<EnvVar> env) {
        this.env = env;
    }

    public Volume getDatasetStorage() {
        return datasetStorage;
    }

    public void setDatasetStorage(Volume datasetStorage) {
        this.datasetStorage = datasetStorage;
    }

    public Volume getWorkspaceStorage() {
        return workspaceStorage;
    }

    public void setWorkspaceStorage(Volume workspaceStorage) {
        this.workspaceStorage = workspaceStorage;
    }
    public void setModelStorage(Volume modelStorage) {
        this.modelStorage = modelStorage;
    }

    public Volume getModelStorage() {

        return modelStorage;
    }
}
