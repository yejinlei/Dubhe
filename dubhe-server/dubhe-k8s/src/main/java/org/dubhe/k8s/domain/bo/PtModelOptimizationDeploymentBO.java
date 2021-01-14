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

import java.util.List;

/**
 * @description 模型压缩 Deployment BO
 * 在继承中使用 @Accessors(chain = true) 在set父类方法后会返回父类对象，故不使用
 * @date 2020-05-26
 */
@Data
public class PtModelOptimizationDeploymentBO extends PtDeploymentBO {
    /**
     * 挂载到dataset的数据集的路径
     **/
    private String datasetDir;
    /**
     * 本docker内的路径，挂载到datasetDir,默认值/dataset
     **/
    private String datasetMountPath;
    /**
     * 数据集是否只读
     **/
    private Boolean datasetReadOnly;
    /**
     * 执行命令
     **/
    private List<String> cmdLines;

    /**
     * nfs存储路径，且能被挂载,不传会生成默认的
     **/
    private String workspaceDir;
    /**
     * 本docker内的路径，挂载到workspaceDir,默认值默认值/workspace
     **/
    private String workspaceMountPath;

    /**
     * nfs存储路径，且能被挂载,默认
     **/
    private String outputDir;
    /**
     * 本docker内的路径，挂载到outputDir,默认值默认值/output
     **/
    private String outputMountPath;

}
