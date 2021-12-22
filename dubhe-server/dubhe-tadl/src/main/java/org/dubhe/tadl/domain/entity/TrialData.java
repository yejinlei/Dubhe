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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.dubhe.biz.db.entity.BaseEntity;
import org.dubhe.tadl.utils.TimeCalculateUtil;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Builder
@ToString
@Data
@TableName("tadl_trial_data")
@ApiModel(value = "TrialData 对象", description = "tadl trial运行结果表")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TrialData extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @TableField(value = "id")
    private Long id;

    @ApiModelProperty("实验id")
    private Long experimentId;

    @ApiModelProperty("阶段id")
    private Long stageId;

    @ApiModelProperty("trial id")
    private Long trialId;

    @ApiModelProperty("指标类型")
    private String type;

    @ApiModelProperty("序列")
    private Integer sequence;

    @ApiModelProperty("类别")
    private String category;

    @ApiModelProperty("最优数据")
    private Double value;

    public static List<TrialData> from(List<Trial> trialList) {
        return new ArrayList<TrialData>(){{
            trialList.forEach(trial -> {
                TrialData trialData = new TrialData();
                trialData.setSequence(trial.getSequence());
                trialData.setExperimentId(trial.getExperimentId());
                trialData.setStageId(trial.getStageId());
                trialData.setTrialId(trial.getId());
                add(trialData);
            });
        }};
    }
}
