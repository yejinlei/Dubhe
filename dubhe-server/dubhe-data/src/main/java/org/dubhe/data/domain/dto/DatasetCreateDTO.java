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

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dubhe.biz.base.annotation.EnumValue;
import org.dubhe.biz.base.enums.DatasetTypeEnum;
import org.dubhe.cloud.authconfig.utils.JwtUtils;
import org.dubhe.data.constant.AnnotateTypeEnum;
import org.dubhe.data.constant.Constant;
import org.dubhe.data.constant.DatatypeEnum;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.machine.constant.DataStateCodeConstant;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @description 数据集
 * @date 2020-04-17
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DatasetCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "数据名不能为空", groups = Create.class)
    @Size(min = 1, max = 50, message = "数据名长度范围只能是1~50", groups = Create.class)
    private String name;

    @ApiModelProperty(notes = "备注信息")
    private String remark;

    @ApiModelProperty(notes = "类型 0: private 私有数据,  1:team  团队数据  2:public 公开数据")
    @NotNull(message = "类型不能为空", groups = Create.class)
    @EnumValue(enumClass = DatasetTypeEnum.class, enumMethod = "isValid",
            message = "类型参数不对,请使用 0-私有数据,  1-团队数据  2-公开数据", groups = Create.class)
    private Integer type;

    @ApiModelProperty(notes = "团队编号")
    private Long teamId;

    @ApiModelProperty(notes = "数据类型:0图片，1视频, 2文本")
    @NotNull(message = "数据类型不能为空", groups = Create.class)
    @EnumValue(enumClass = DatatypeEnum.class, enumMethod = "isValid", message = Constant.DATA_TYPE_RULE, groups = Create.class)
    private Integer dataType;

    @ApiModelProperty(notes = "标注类型：2分类,1目标检测,5目标跟踪 6 文本分类")
    @NotNull(message = "数据用于标注的类型不能为空", groups = Create.class)
    @EnumValue(enumClass = AnnotateTypeEnum.class, enumMethod = "isValid", message = Constant.ANNOTATE_TYPE_RULE, groups = Create.class)
    private Integer annotateType;

    @ApiModelProperty(notes = "标签组Id")
    private Long labelGroupId;

    @ApiModelProperty(value = "预置标签类型 2:imageNet  3:MS COCO")
    private Integer presetLabelType;

    @ApiModelProperty(value = "是否用户导入")
    @TableField(value = "is_import")
    private boolean isImport;


    public @interface Create {
    }

    /**
     * DatasetCreateDTO 转换Dataset
     *
     * @param datasetCreateDTO  数据集创建DTO
     * @return  数据集
     */
    public static Dataset from(DatasetCreateDTO datasetCreateDTO) {
        Dataset dataset = new Dataset(datasetCreateDTO);
        dataset.setStatus(DataStateCodeConstant.NOT_ANNOTATION_STATE);
        return dataset;
    }

    /**
     * 更新数据集
     *
     * @param datasetCreateDTO 数据集创建DTO
     * @return 数据集
     */
    public static Dataset update(DatasetCreateDTO datasetCreateDTO) {
        return new Dataset(datasetCreateDTO);
    }

}
