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

package org.dubhe.admin.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description pod的实体类
 * @date 2020-06-03
 */
@Data
public class PodDTO {

    @ApiModelProperty(value = "pod的name")
    private String podName;

    @ApiModelProperty(value = "pod的内存")
    private String podMemory;

    @ApiModelProperty(value = "pod的cpu")
    private String podCpu;

    @ApiModelProperty(value = "pod的显卡")
    private String podCard;

    @ApiModelProperty(value = "pod的状态")
    private String status;

    @ApiModelProperty(value = "node的name")
    private String nodeName;

    @ApiModelProperty(value = "pod的创建时间")
    private String podCreateTime;


}
