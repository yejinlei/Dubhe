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

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description 字典详情DTO
 * @date 2020-06-01
 */
@Data
public class DictDetailDTO implements Serializable {

    private static final long serialVersionUID = 1521993584428225098L;

    @ApiModelProperty(value = "字典详情id")
    private Long id;

    @ApiModelProperty(value = "字典label")
    private String label;

    @ApiModelProperty(value = "字典详情value")
    private String value;

    @ApiModelProperty(value = "排序")
    private String sort;

    @ApiModelProperty(value = "字典id")
    private Long dictId;

    private Timestamp createTime;

    private Timestamp updateTime;
}
