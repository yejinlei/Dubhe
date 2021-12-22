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
package org.dubhe.tadl.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class AlgorithmYamlQueryDTO {

    @ApiModelProperty(value = "zip压缩包路径（路径规则：/algorithm-manage/{userId}/{YYYYMMDDhhmmssSSS+四位随机数}/用户上传的算法具体文件(zip文件）名称或从notebook跳转时为/notebook/{userId}/{YYYYMMDDhhmmssSSS+四位随机数}/）", required = true)
    @Length(max = MagicNumConstant.ONE_HUNDRED_TWENTY_EIGHT, message = "zip压缩包路径-输入长度不能超过128个字符")
    private String zipPath;

    @ApiModelProperty("算法名称")
    private String algorithm;

    @ApiModelProperty(value = "算法阶段")
    @NotNull(message = "算法阶段参数不能为空")
    private Integer stageOrder;

    @ApiModelProperty(value = "算法版本名称")
    private String versionName;

}
