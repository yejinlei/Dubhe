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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @description 自定义数据集
 * @date 2020-07-28
 */
@Data
@ApiModel
public class DatasetCustomCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "数据集名称")
    @Size(min = 1, max = 50, message = "数据名长度范围只能是1~50", groups = Create.class)
    private String name;

    @ApiModelProperty(value = "数据集描述")
    private String desc;

    @ApiModelProperty(value = "数据集压缩包地址")
    @NotNull(message = "请上传压缩包!", groups = Create.class)
    private String archiveUrl;

    @ApiModelProperty(value = "数据集类型")
    @NotNull(message = "请指定上传的数据集类型!", groups = Create.class)
    private Integer datasetType;

    @ApiModelProperty(value = "标注类型")
    @NotNull(message = "请指定需要的标注类型!", groups = Create.class)
    private Integer annotateType;

    public @interface Create {
    }

}
