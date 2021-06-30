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

package org.dubhe.data.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.data.constant.Constant;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description 数据集版本
 * @date 2020-05-14
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DatasetVersionCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据集ID
     */
    @ApiModelProperty(value = "数据集Id")
    @NotNull(message = "数据集ID不能为空")
    private Long datasetId;

    /**
     * 版本名称
     */
    @ApiModelProperty(value = "发布版本号:" + Constant.DATASET_VERSION_NAME_REGEXP_NOTE)
    private String versionName;

    /**
     * 版本说明
     */
    @ApiModelProperty(value = "版本说明")
    @Max(value = MagicNumConstant.FIFTY, message = "版本说明长度应小于50字符!")
    private String versionNote;

    /**
     * 是否进行ofRecord转换
     */
    @ApiModelProperty(value = "ofRecord转换")
    @NotNull(message = "转换标志不能为空")
    private Integer ofRecord;

    public @interface Create {
    }

    /**
     * 版本号规则检测
     * @return
     */
    public boolean checkVersionName() {
        return StringUtils.stringMatch(this.versionName, Constant.DATASET_VERSION_NAME_REGEXP);
    }

}
