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

package org.dubhe.model.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.biz.base.utils.PtModelUtil;
import org.dubhe.biz.db.base.PageQueryBase;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * @description 查询模型管理
 * @date 2020-03-24
 */
@Data
public class PtModelInfoQueryDTO extends PageQueryBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("模型名称")
    @Length(max = PtModelUtil.NUMBER_ONE_HUNDRED_TWENTY_EIGHT, message = "模型名称-输入长度不能超过128个字符")
    private String name;

    @ApiModelProperty("模型分类")
    @Length(max = PtModelUtil.NUMBER_ONE_HUNDRED_TWENTY_EIGHT, message = "模型分类-输入长度不能超过128个字符")
    private String modelClassName;

    @ApiModelProperty("模型是否为预置模型（0默认模型，1预置模型,2炼知模型）")
    @Min(value = PtModelUtil.NUMBER_ZERO, message = "模型来源错误")
    @Max(value = PtModelUtil.NUMBER_TWO, message = "模型来源错误")
    private Integer modelResource;

    @ApiModelProperty("模型是否打包，0未打包， 1 已打包")
    private Integer packaged;

}
