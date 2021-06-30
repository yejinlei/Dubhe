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

package org.dubhe.admin.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.admin.domain.entity.DictDetail;
import org.hibernate.validator.constraints.Length;


import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description 字典详情修改DTO
 * @date 2020-06-29
 */
@Data
public class DictDetailUpdateDTO implements Serializable {

    private static final long serialVersionUID = -1936563127368448645L;

    @NotNull(groups = DictDetail.Update.class)
    private Long id;

    @ApiModelProperty(value = "字典标签")
    @Length(max = 255, message = "字典标签长度不能超过255")
    private String label;

    @ApiModelProperty(value = "字典值")
    @Length(max = 255, message = "字典值长度不能超过255")
    private String value;

    @ApiModelProperty(value = "排序")
    private String sort = "999";

    @ApiModelProperty(value = "字典id")
    private Long dictId;

    private Timestamp createTime;

    public @interface Update {
    }
}
