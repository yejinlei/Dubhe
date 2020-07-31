/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

package org.dubhe.domain.vo;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @description: 训练作业规格
 * @date 2020-05-06
 */
@Data
@Accessors(chain = true)
public class PtTrainJobSpecsQueryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("训练作业规格ID")
    private Integer id;

    @ApiModelProperty("训练作业规格名称")
    private String specsName;

    @ApiModelProperty(value = "规格类型(0为CPU, 1为GPU)")
    private Integer resourcesPoolType;

    @ApiModelProperty(value = "规格信息")
    private JSONObject specsInfo;

}
