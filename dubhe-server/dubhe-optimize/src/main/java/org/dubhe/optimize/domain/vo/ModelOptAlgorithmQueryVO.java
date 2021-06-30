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
package org.dubhe.optimize.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @description 模型优化算法返回
 * @date 2021-01-05
 */
@Data
public class ModelOptAlgorithmQueryVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("算法类型")
    private Integer type;

    @ApiModelProperty("算法名称")
    private String algorithm;

    @ApiModelProperty("算法路径")
    private String algorithmPath;
}
