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
package org.dubhe.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * @description 字典实体
 * @date 2020-06-01
 */
@Data
@TableName("dict")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Dict implements Serializable {

    private static final long serialVersionUID = -3995510721958462699L;
    @TableId(value = "id", type = IdType.AUTO)
    @NotNull(groups = Update.class)
    private Long id;

    /**
     * 名称
     */
    @TableField(value = "name")
    @NotBlank
    @Length(max = 255, message = "名称长度不能超过255")
    private String name;

    /**
     * 备注
     */
    @TableField(value = "remark")
    @Length(max = 255, message = "备注长度不能超过255")
    private String remark;


    @TableField(value = "create_time")
    private Timestamp createTime;

    @TableField(exist = false)
    private List<org.dubhe.admin.domain.entity.DictDetail> dictDetails;

    public @interface Update {
    }
}
