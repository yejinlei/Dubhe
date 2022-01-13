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
package org.dubhe.tadl.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.dubhe.tadl.domain.entity.AlgorithmStage;
import org.dubhe.tadl.domain.entity.TrialData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TrialVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "=id")
    private Long id;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("阶段id")
    private Long stageId;

    @ApiModelProperty("序列")
    private Integer sequence;

    @ApiModelProperty("指标类型")
    private String type;

    @ApiModelProperty("trial id")
    private Long trialId;

    @ApiModelProperty("类别")
    private String category;

    @ApiModelProperty("command命令所使用py文件")
    private String executeScript;

    @ApiModelProperty("运行时间")
    private Long runTime;

    @ApiModelProperty("实验资源值")
    private String resourceName;

    @ApiModelProperty("最优数据")
    private Double value;


    public static List<TrialVO> from(List<TrialData> trialDataList, AlgorithmStage algorithmStage) {
        ArrayList<TrialVO> trialVOS = new ArrayList<>();
        trialDataList.forEach(data -> {
            TrialVO vo = new TrialVO();
            vo.setId(data.getId());
            vo.setStageId(data.getStageId());
            vo.setSequence(data.getSequence());
            vo.setType(data.getType());
            vo.setTrialId(data.getTrialId());
            vo.setCategory(data.getCategory());
            vo.setExecuteScript(algorithmStage.getExecuteScript());
            vo.setValue(data.getValue());
            trialVOS.add(vo);
        });
        return trialVOS;
    }
}
