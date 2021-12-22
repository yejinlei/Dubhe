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
import org.dubhe.biz.db.entity.BaseEntity;
import org.dubhe.tadl.domain.dto.AlgorithmCreateDTO;
import org.dubhe.tadl.domain.dto.AlgorithmUpdateDTO;

import java.io.Serializable;

@Builder
@ToString
@Data
@TableName("tadl_algorithm")
@ApiModel(value = "Algorithm 对象", description = "算法表")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Algorithm extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @TableField(value = "id",fill = FieldFill.INSERT)
    private Long id;

    @ApiModelProperty("算法名称")
    private String name;

    @ApiModelProperty("模型类别")
    private Integer modelType;

    @ApiModelProperty("算法版本Id")
    private Long algorithmVersionId;

    @ApiModelProperty("算法描述")
    private String description;

    @ApiModelProperty("默认主要指标")
    private String defaultMetric;

    @ApiModelProperty("是否oneShot（0不是，1是）")
    private Boolean oneShot;

    @ApiModelProperty("算法类型")
    private String algorithmType;

    @ApiModelProperty("算法框架")
    private String platform;

    @ApiModelProperty("算法框架版本")
    private String platformVersion;

    @ApiModelProperty("是否支持gpu训练：0支持，1不支持")
    private Boolean gpu;

    public Algorithm(AlgorithmCreateDTO algorithmCreateDTO) {
        this.name=algorithmCreateDTO.getName();
        this.modelType=algorithmCreateDTO.getModelType();
        this.description=algorithmCreateDTO.getDescription();
        this.defaultMetric = algorithmCreateDTO.getDefaultMetric();
        this.oneShot = algorithmCreateDTO.getOneShot();
        this.algorithmType = algorithmCreateDTO.getAlgType();
        this.platform = algorithmCreateDTO.getPlatform();
        this.platformVersion = algorithmCreateDTO.getPlatformVersion();
        this.gpu = algorithmCreateDTO.getGpu();
    }

    public Algorithm(AlgorithmUpdateDTO algorithmUpdateDTO) {
        this.id=algorithmUpdateDTO.getId();
        this.name=algorithmUpdateDTO.getName();
        this.modelType=algorithmUpdateDTO.getModelType();
        this.description=algorithmUpdateDTO.getDescription();
        this.defaultMetric = algorithmUpdateDTO.getDefaultMetric();
        this.oneShot = algorithmUpdateDTO.getOneShot();
        this.algorithmType = algorithmUpdateDTO.getAlgType();
        this.platform = algorithmUpdateDTO.getPlatform();
        this.platformVersion = algorithmUpdateDTO.getPlatformVersion();
        this.gpu = algorithmUpdateDTO.getGpu();
        this.algorithmVersionId=algorithmUpdateDTO.getAlgorithmVersionId();
    }
}
