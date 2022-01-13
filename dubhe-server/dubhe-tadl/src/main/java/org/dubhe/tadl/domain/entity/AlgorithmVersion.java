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
package org.dubhe.tadl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.db.entity.BaseEntity;
import org.dubhe.tadl.domain.dto.AlgorithmCreateDTO;
import org.dubhe.tadl.domain.dto.AlgorithmUpdateDTO;

import java.io.Serializable;

@Builder
@ToString
@Data
@TableName("tadl_algorithm_version")
@ApiModel(value = "AlgorithmVersion 对象", description = "算法版本表")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AlgorithmVersion extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @TableField(value = "算法版本id", fill = FieldFill.INSERT)
    private Long id;

    @ApiModelProperty("算法id")
    private Long algorithmId;

    @ApiModelProperty("版本名称")
    private String versionName;

    @ApiModelProperty("版本说明")
    private String description;

    @ApiModelProperty("版本来源")
    private String versionSource;

    @ApiModelProperty("数据转换")
    private Integer dataConversion;

    public AlgorithmVersion(AlgorithmCreateDTO algorithmCreateDTO) {

    }

    public AlgorithmVersion(AlgorithmUpdateDTO algorithmUpdateDTO) {
        this.description=algorithmUpdateDTO.getDescription();
    }
}
