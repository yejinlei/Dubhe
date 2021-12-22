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
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.MagicNumConstant;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description 查询用户资源规格
 * @date 2021-09-07
 */
@Data
@Accessors(chain = true)
public class QueryUserResourceSpecsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户id")
    @Min(value = MagicNumConstant.ONE, message = "用户id，不能小于1")
    private Long userId;

    @ApiModelProperty(value = "所属业务场景(0:通用，1：dubhe-notebook，2：dubhe-train，3：dubhe-serving，4：dubhe-tadl，5：dubhe-optimize))", required = true)
    @NotNull(message = "所属业务场景不能为空")
    @Min(value = MagicNumConstant.ZERO, message = "所属业务场景错误")
    @Max(value = MagicNumConstant.FIVE, message = "所属业务场景错误")
    private Integer module;

    @ApiModelProperty("规格类型(0为CPU, 1为GPU)")
    @NotNull(message = "规格类型(0为CPU, 1为GPU)不能为空")
    private Boolean resourcesPoolType;

    @ApiModelProperty(value = "节点个数")
    @Min(value = MagicNumConstant.ONE, message = "节点个数，默认为1个")
    private Integer resourcesPoolNode;

    @ApiModelProperty(value = "GPU型号(例如：v100)")
    private String gpuModel;

    @ApiModelProperty(value = "k8s GPU资源标签key值(例如：nvidia.com/gpu)")
    private String k8sLabelKey;

    @ApiModelProperty(value = "多GPU，true：GPU数大于1核，false:GPU数等于1核")
    private Boolean multiGpu;
}