package org.dubhe.dcm.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description 医学数据集状态VO
 * @date 2021-01-13
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ScheduleVO {

    @ApiModelProperty("未标注")
    private Integer unfinished;

    @ApiModelProperty("标注完成")
    private Integer finished;

    @ApiModelProperty("自动标注完成")
    private Integer autoFinished;

    @ApiModelProperty("标注中")
    private Integer manualAnnotating;
}
