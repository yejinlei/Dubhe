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

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.tadl.domain.entity.AlgorithmVersion;

import java.io.Serializable;
@Data
public class AlgorithmVersionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @TableField(value = "算法版本id", fill = FieldFill.INSERT)
    private Long id;

    @ApiModelProperty("算法id")
    private Long algorithmId;

    @ApiModelProperty("版本名称")
    private String versionName;

    @ApiModelProperty("版本说明")
    private String description;

    @ApiModelProperty("版本来源")
    private String versionSource;

    @ApiModelProperty("数据转换")
    private Integer dataConversion;

    public static AlgorithmVersionVO from(AlgorithmVersion algorithmVersion) {
        return new AlgorithmVersionVO() {{
            setId(algorithmVersion.getId());
            setAlgorithmId(algorithmVersion.getAlgorithmId());
            if (!StringUtils.isEmpty(algorithmVersion.getVersionName())){
                setVersionName(algorithmVersion.getVersionName());
            }
            if (!StringUtils.isEmpty(algorithmVersion.getDescription())){
                setDescription(algorithmVersion.getDescription());
            }
        }};
    }

}
