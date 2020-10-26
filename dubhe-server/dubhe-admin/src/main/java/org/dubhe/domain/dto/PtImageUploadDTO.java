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

package org.dubhe.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.utils.TrainUtil;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;


/**
 * @description 上传镜像
 * @date 2020-06-11
 */
@Data
@Accessors(chain = true)
public class PtImageUploadDTO implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "镜像文件路径", required = true)
    @NotBlank(message = "镜像文件路径不能为空")
    private String imagePath;

    @ApiModelProperty(value = "镜像名称", required = true)
    @NotBlank(message = "源镜像名称不能为空")
    @Length(min = TrainUtil.NUMBER_ONE, max = TrainUtil.NUMBER_SIXTY_FOUR, message = "镜像名称长度在1-64个字符")
    @Pattern(regexp = TrainUtil.REGEXP_NAME, message = "镜像名称支持字母、数字、英文横杠和下划线")
    private String imageName;

    @ApiModelProperty(value = "镜像版本号", required = true)
    @NotBlank(message = "镜像版本号不能为空")
    @Length(max = TrainUtil.NUMBER_THIRTY_TWO, message = "镜像版本号长度在1-32个字符")
    @Pattern(regexp = TrainUtil.REGEXP_TAG, message = "镜像版本号支持字母、数字、英文横杠、英文.号和下划线")
    private String imageTag;

    @ApiModelProperty("镜像描述")
    @Length(max = TrainUtil.NUMBER_ONE_THOUSAND_AND_TWENTY_FOUR, message = "镜像描述-输入长度不能超过1024个字符")
    private String remark;
}
