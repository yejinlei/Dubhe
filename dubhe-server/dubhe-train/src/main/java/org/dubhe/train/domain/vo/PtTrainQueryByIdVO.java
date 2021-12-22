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

package org.dubhe.train.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description 根据训练作业ID查询训练查询结果
 * @date 2021-10-27
 */
@Data
@Accessors(chain = true)
public class PtTrainQueryByIdVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("训练作业ID")
    private Long id;

    @ApiModelProperty("训练作业名")
    private String trainName;

    @ApiModelProperty("训练作业有效job版本数量")
    private Integer versionNum;

    @ApiModelProperty("训练作业job总版本数量")
    private Integer totalNum;

    @ApiModelProperty("创建人")
    private Long createUserId;

    @ApiModelProperty("创建时间")
    private Timestamp createTime;

    @ApiModelProperty("更新人")
    private Long updateUserId;

    @ApiModelProperty("更新时间")
    private Timestamp updateTime;

    @ApiModelProperty("资源拥有者ID")
    private Long originUserId;
}
