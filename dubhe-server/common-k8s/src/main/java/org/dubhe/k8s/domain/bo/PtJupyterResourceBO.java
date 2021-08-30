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
import lombok.experimental.Accessors;
import org.dubhe.k8s.annotation.K8sValidation;
import org.dubhe.k8s.enums.ValidationTypeEnum;

/**
 * @description Notebook BO
 * @date 2020-04-17
 */
@Data
@Accessors(chain = true)
public class PtJupyterResourceBO {
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
     * GPU数量
     **/
    private Integer gpuNum;
    /**
     * 是否使用gpu true：使用；false：不用
     **/
    private Boolean useGpu;
    /**
     * 内存数量 单位Mi Gi
     **/
    private Integer memNum;
    /**
     * CPU数量
     **/
    private Integer cpuNum;
    /**
     * 镜像名称
     **/
    private String image;

    /**
     * nfs存储路径，存在且能被挂载，此路径内容不能通过接口销毁，不传不会挂载
     **/
    private String datasetDir;
    /**
     * 本docker内的路径，挂载到datasetDir
     **/
    private String datasetMountPath;
    /**
     * datasetDir是否只读
     **/
    private Boolean datasetReadOnly;

    /**
     * nfs存储路径，存在且能被挂载，此路径内容在调用PersistentVolumeClaimApi.recycle后销毁,不传会生成默认的
     **/
    private String workspaceDir;
    /**
     * 本docker内的路径，挂载到datasetDir
     **/
    private String workspaceMountPath;
    /**
     * workspaceDir的存储配额  必须
     **/
    private String workspaceRequest;
    /**
     * workspaceDir的存储限额 非必须
     **/
    private String workspaceLimit;
    /**
     * used in internal
     **/
    private String workspacePvcName;
    /**
     * 业务标签,用于标识业务模块
     **/
    private String businessLabel;
    /**
     * 任务身份标签,用于标识任务唯一身份
     **/
    private String taskIdentifyLabel;
    /**
     * 定时删除时间，单位：分钟
     **/
    private Integer delayDeleteTime;
    /**
     * pip包路径
     */
    private String pipSitePackageDir;
    /**
     * k8s内pip包路径
     */
    private String pipSitePackageMountPath;
}
