/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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
package org.dubhe.model.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.utils.PtModelUtil;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description TensorFlow SaveModel 模型转换为 ONNX 模型
 * @date 2021-5-26
 */
@Data
@Accessors(chain = true)
public class PtModelConvertOnnxDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty("版本ID")
    @NotNull(message = "版本ID不能为空")
    @Min(value = PtModelUtil.NUMBER_ONE, message = "版本ID不能小于1")
    private Long id;
}
