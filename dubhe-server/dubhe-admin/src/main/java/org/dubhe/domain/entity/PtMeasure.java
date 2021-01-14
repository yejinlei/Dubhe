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
package org.dubhe.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import org.dubhe.base.BaseEntity;

/**
 * @description 度量管理实体类
 * @date 2020-11-16
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@TableName("pt_measure")
@NoArgsConstructor
@AllArgsConstructor
public class PtMeasure extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 度量名称
     */
    @TableField("name")
    private String name;

    /**
     * 度量文件路径
     */
    @TableField("url")
    private String url;

    /**
     * 资源拥有者ID
     */
    @TableField(value = "origin_user_id", fill = FieldFill.INSERT)
    private Long originUserId;

    /**
     * 度量描述信息
     */
    @TableField("description")
    private String description;
}
