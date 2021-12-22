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
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.db.entity.BaseEntity;
import org.dubhe.tadl.domain.dto.ExperimentCreateDTO;
import org.dubhe.tadl.domain.dto.ExperimentUpdateDTO;
import org.dubhe.tadl.enums.ExperimentStatusEnum;

import java.io.Serializable;
import java.sql.Timestamp;

@Builder
@ToString
@Data
@TableName("tadl_experiment")
@ApiModel(value = "Experiment 对象", description = "实验表")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Experiment extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @TableField(value = "id")
    private Long id;

    @ApiModelProperty("实验名称")
    private String name;

    @ApiModelProperty("实验描述")
    private String description;

    @ApiModelProperty("算法id")
    private Long algorithmId;

    @ApiModelProperty("算法版本id")
    private Long algorithmVersionId;

    @ApiModelProperty("模型类型")
    @TableField(value = "model_type")
    private Integer modelType;

    @ApiModelProperty("实验状态：（待运行：101 等待中：102，运行中：103，已暂停：104已终止：105，已完成：106，运行失败：107）")
    private Integer status;

    @ApiModelProperty("启动时间")
    @TableField(value = "start_time")
    private Timestamp startTime;

    @ApiModelProperty("结束时间")
    @TableField(value = "end_time")
    private Timestamp endTime;

    /**
     * 状态对应的详情信息
     */
    @TableField(value = "status_detail")
    private String statusDetail;

    public Experiment(ExperimentCreateDTO experimentCreateDTO) {
        this.name = experimentCreateDTO.getName();
        this.description = experimentCreateDTO.getDescription();
        this.algorithmId = experimentCreateDTO.getAlgorithmId();
        this.algorithmVersionId = experimentCreateDTO.getAlgorithmVersionId();
        this.modelType = experimentCreateDTO.getModelType();
        this.status = ExperimentStatusEnum.TO_RUN_EXPERIMENT_STATE.getValue();
    }
    public Experiment(ExperimentUpdateDTO experimentUpdateDTO) {
        this.id=experimentUpdateDTO.getId();
        this.name = experimentUpdateDTO.getName();
        this.description = experimentUpdateDTO.getDescription();
        this.algorithmId = experimentUpdateDTO.getAlgorithmId();
        this.algorithmVersionId = experimentUpdateDTO.getAlgorithmVersionId();
        this.modelType = experimentUpdateDTO.getModelType();
        this.status = ExperimentStatusEnum.TO_RUN_EXPERIMENT_STATE.getValue();
    }
    /**
     * put 键值
     *
     * @param key 键
     * @param value 值
     */
    public void putStatusDetail(String key,String value){
        statusDetail = StringUtils.putIntoJsonStringMap(key,value,statusDetail);
    }

    /**
     * 移除 键值
     *
     * @param key 键
     */
    public void removeStatusDetail(String key){
        statusDetail = StringUtils.removeFromJsonStringMap(key,statusDetail);
    }
}
