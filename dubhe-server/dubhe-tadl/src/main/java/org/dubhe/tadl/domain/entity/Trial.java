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
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.db.entity.BaseEntity;
import org.dubhe.tadl.domain.dto.ExperimentCreateDTO;
import org.dubhe.tadl.domain.dto.ExperimentUpdateDTO;
import org.dubhe.tadl.enums.TrialStatusEnum;
import org.dubhe.tadl.constant.RedisKeyConstant;
import org.dubhe.tadl.utils.TimeCalculateUtil;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Builder
@ToString
@Data
@TableName("tadl_trial")
@ApiModel(value = "Trial 对象", description = "tadl 试验详情表")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Trial extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @TableField(value = "id")
    private Long id;

    @ApiModelProperty("实验id")
    private Long experimentId;

    @ApiModelProperty("实验阶段")
    private Long stageId;

    @ApiModelProperty("trial名称")
    private String name;

    @ApiModelProperty("启动时间")
    private Timestamp startTime;

    @ApiModelProperty("结束时间")
    private Timestamp endTime;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("顺序")
    private Integer sequence;

    @ApiModelProperty("k8s实验资源值")
    private String resourceName;


    public static List<Trial> from(ExperimentCreateDTO experimentCreateDTO, List<ExperimentStage> experimentStageList,Experiment experiment) {
        return new ArrayList<Trial>() {{
            experimentCreateDTO.getStage().forEach(v -> {
                experimentStageList.forEach(s -> {
                    //判断 stageID 一致 stageId 一致则获取数据库中 实验id与实验阶段id
                    if (v.getAlgorithmStageId().equals(s.getAlgorithmStageId())) {
                        //根据阶段的最大 trial 数写 trial
                        int i = NumberConstant.NUMBER_0;
                        //experiment  （stage1 * 10） + （stage2 * 1） + （stage * 3）= trialNum
                        while (i++ < v.getMaxTrialNum()) {
                            Trial trial = new Trial() {{
                                setExperimentId(s.getExperimentId());
                                //写实验阶段ID
                                setStageId(s.getId());
                                setStatus(TrialStatusEnum.TO_RUN.getVal());
                                setCreateUserId(experiment.getCreateUserId());
                            }};
                            trial.setName(s.getExperimentId() + RedisKeyConstant.COLON + s.getId() + RedisKeyConstant.COLON + v.getStageOrder() + RedisKeyConstant.COLON + i);
                            trial.setSequence(i);
                            add(trial);
                        }
                    }
                });
            });
        }};
    }

    public static List<Trial> from(ExperimentUpdateDTO experimentUpdateDTO, List<ExperimentStage> experimentStageList, Experiment experiment) {
        return new ArrayList<Trial>() {{
            experimentUpdateDTO.getStage().forEach(v -> {
                experimentStageList.forEach(s -> {
                    //判断 stageID 一致 stageId 一致则获取数据库中 实验id与实验阶段id
                    if (v.getAlgorithmStageId().equals(s.getAlgorithmStageId())) {
                        //根据阶段的最大 trial 数写 trial
                        int i = NumberConstant.NUMBER_0;
                        //experiment  （stage1 * 10） + （stage2 * 1） + （stage * 3）= trialNum
                        while (i++ < v.getMaxTrialNum()) {
                            Trial trial = new Trial() {{
                                setExperimentId(s.getExperimentId());
                                //写实验阶段ID
                                setStageId(s.getId());
                                setStatus(TrialStatusEnum.TO_RUN.getVal());
                                setCreateUserId(experiment.getCreateUserId());
                            }};
                            trial.setName(s.getExperimentId() + RedisKeyConstant.COLON + s.getId() + RedisKeyConstant.COLON + v.getStageOrder() + RedisKeyConstant.COLON + i);
                            trial.setSequence(i);
                            add(trial);
                        }
                    }
                });
            });
        }};
    }

    public static List<TimeCalculateUtil.RunTime> from(List<Trial> trialList){
        return new ArrayList<TimeCalculateUtil.RunTime>(){{
            trialList.forEach(val->{
                add(new TimeCalculateUtil.RunTime(){{
                    setEnd(val.getEndTime());
                    setStart(val.getStartTime());
                }});
            });
        }};
    }
}
