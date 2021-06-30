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

package org.dubhe.k8s.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @descripton 统一通用参数实现与校验
 * @date 2020-11-26
 */
@ApiModel(description = "k8s deployment异步回调基类")
@Data
public class BaseK8sDeploymentCallbackCreateDTO {
    @ApiModelProperty(required = true, value = "k8s namespace")
    @NotBlank(message = "namespace 不能为空！")
    private String namespace;

    @ApiModelProperty(required = true, value = "k8s resource name")
    @NotBlank(message = "resourceName 不能为空！")
    private String resourceName;

    @ApiModelProperty(required = true, value = "k8s deployment name")
    @NotBlank(message = "deployment 不能为空！")
    private String deploymentName;

    /**
     * deployment已 Running的pod数
     */
    @ApiModelProperty(required = true, value = "k8s deployment readyReplicas")
    @NotNull(message = "readyReplicas 不能为空！")
    private Integer readyReplicas;

    /**
     * deployment总pod数
     */
    @ApiModelProperty(required = true, value = "k8s deployment replicas")
    @NotNull(message = "replicas 不能为空！")
    private Integer replicas;

    public BaseK8sDeploymentCallbackCreateDTO() {

    }

    public BaseK8sDeploymentCallbackCreateDTO(String namespace, String resourceName, String deploymentName, Integer readyReplicas, Integer replicas) {
        this.namespace = namespace;
        this.resourceName = resourceName;
        this.deploymentName = deploymentName;
        this.readyReplicas = readyReplicas;
        this.replicas = replicas;
    }

    @Override
    public String toString() {
        return "BaseK8sDeploymentCallbackCreateDTO{" +
                "namespace='" + namespace + '\'' +
                ", resourceName='" + resourceName + '\'' +
                ", deploymentName='" + deploymentName + '\'' +
                ", readyReplicas='" + readyReplicas + '\'' +
                ", replicas='" + replicas + '\'' +
                '}';
    }
}
