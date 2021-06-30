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
package org.dubhe.serving.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.constant.StringConstant;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;



/**
 * @description 创建服务
 * @date 2020-08-24
 */
@Data
@Accessors(chain = true)
public class ServingInfoCreateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "服务名称", required = true)
    @NotNull(message = "服务名称不能为空")
    @Size(max = 50, message = "服务名称长度超过50")
    @Pattern(regexp = StringConstant.REGEXP_NAME, message = "服务名称仅支持字母、数字、汉字、英文横杠和下划线")
    private String name;

    @ApiModelProperty(value = "服务类型：0-Restful，1-gRPC", required =true)
    @Min(value = NumberConstant.NUMBER_0, message = "服务类型参数错误")
    @Max(value = NumberConstant.NUMBER_1, message = "服务类型参数错误")
    private Integer type;

    @ApiModelProperty(value = "描述")
    @Size(max = 200, message = "描述长度超过200")
    private String description;

    @ApiModelProperty(value = "模型配置列表")
    private List<ServingModelConfigDTO> modelConfigList;

}
