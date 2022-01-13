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

import javax.validation.constraints.NotEmpty;
import java.util.Map;

/**
 * @descripton  统一通用参数实现与校验
 * @date 2020-05-28
 */
@ApiModel(description = "k8s pod异步回调基类")
@Data
public class BaseK8sPodCallbackCreateDTO {

    @ApiModelProperty(required = true,value = "k8s namespace")
    @NotEmpty(message = "namespace 不能为空！")
    private String namespace;

    @ApiModelProperty(required = true,value = "k8s resource name")
    @NotEmpty(message = "resourceName 不能为空！")
    private String resourceName;

    @ApiModelProperty(required = true,value = "k8s pod name")
    @NotEmpty(message = "podName 不能为空！")
    private String podName;

    @ApiModelProperty(required = true,value = "k8s pod parent type")
    @NotEmpty(message = "podParentType 不能为空！")
    private String podParentType;

    @ApiModelProperty(required = true,value = "k8s pod parent name")
    @NotEmpty(message = "podParentName 不能为空！")
    private String podParentName;

    @ApiModelProperty(value = "k8s pod phase",notes = "对应PodPhaseEnum")
    @NotEmpty(message = "phase 不能为空！")
    private String phase;

    @ApiModelProperty(value = "k8s pod containerStatuses state")
    private String messages;

    @ApiModelProperty(value = "k8s pod lables")
    private Map<String,String> lables;

    public BaseK8sPodCallbackCreateDTO(){

    }

    public BaseK8sPodCallbackCreateDTO(String namespace, String resourceName, String podName, String podParentType, String podParentName, String phase, String messages){
        this.namespace = namespace;
        this.resourceName = resourceName;
        this.podName = podName;
        this.podParentType = podParentType;
        this.podParentName = podParentName;
        this.phase = phase;
        this.messages = messages;
    }

    @Override
    public String toString() {
        return "BaseK8sPodCallbackReq{" +
                "namespace='" + namespace + '\'' +
                ", resourceName='" + resourceName + '\'' +
                ", podName='" + podName + '\'' +
                ", podParentType='" + podParentType + '\'' +
                ", podParentName='" + podParentName + '\'' +
                ", phase='" + phase + '\'' +
                ", messages=" + messages +
                '}';
    }
}
