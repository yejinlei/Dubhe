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

import java.io.Serializable;

/**
 * @description 返回删除训练任务结果
 * @date 2020-04-28
 */
@Data
public class PtTrainJobDeleteVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("trainID,如果只传递trainID,代表删除该trainID下的所有job")
    private Long trainId;

    @ApiModelProperty("训练作业jobID")
    private Long id;

}
