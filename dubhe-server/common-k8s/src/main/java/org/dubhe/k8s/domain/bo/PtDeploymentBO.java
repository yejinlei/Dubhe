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

import lombok.Data;
import org.dubhe.k8s.annotation.K8sValidation;
import org.dubhe.k8s.enums.ValidationTypeEnum;

/**
 * @description deployment bo
 * @date 2020-05-26
 */
@Data
public class PtDeploymentBO {
    /**
     * 命名空间
     **/
    @K8sValidation(ValidationTypeEnum.K8S_RESOURCE_NAME)
    private String namespace;
    /**
     * 资源名称
     **/
    @K8sValidation(ValidationTypeEnum.K8S_RESOURCE_NAME)
    private String name;
    /**
     * GPU数量，0表示共享显卡，null表示不使用显卡
     **/
    private Integer gpuNum;
    /**
     * 内存数量单位 Mi
     **/
    private Integer memNum;
    /**
     * CPU数量 1000代表占用一个核心
     **/
    private Integer cpuNum;
    /**
     * 镜像名称
     **/
    private String image;
    /**
     * 业务标签,用于标识业务模块
     **/
    private String businessLabel;
    /**
     * 任务身份标签,用于标识任务身份
     **/
    private String taskIdentifyLabel;
}
