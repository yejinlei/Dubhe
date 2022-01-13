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

package org.dubhe.train.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @description 镜像
 * @date 2020-04-27
 */
@Data
@Accessors(chain = true)
public class PtImageAndAlgorithmVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("镜像名称")
    private String imageName;

    @ApiModelProperty("镜像路径")
    private String imageUrl;

    @ApiModelProperty("代码目录")
    private String codeDir;

    @ApiModelProperty("运行命令")
    private String runCommand;

    @ApiModelProperty("训练模型输出目录")
    private Boolean isTrainModelOut;

    @ApiModelProperty("训练输出目录")
    private Boolean isTrainOut;

    @ApiModelProperty("输出可视化日志")
    private Boolean isVisualizedLog;

    @JsonIgnore
    @ApiModelProperty(value = "pip包路径",hidden = true)
    private String pipSitePackagePath;

}
