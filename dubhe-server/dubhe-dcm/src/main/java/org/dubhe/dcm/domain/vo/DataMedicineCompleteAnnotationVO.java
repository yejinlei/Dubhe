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
package org.dubhe.dcm.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @description 医学数据集完成标注文件VO
 * @date 2020-11-17
 */
@Data
public class DataMedicineCompleteAnnotationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("序列实例UID")
    private String StudyInstanceUID;

    @ApiModelProperty("研究实例UID")
    private String SeriesInstanceUID;

    @ApiModelProperty("完成标注文件")
    private String annotations;
}
