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

package org.dubhe.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dubhe.base.BaseEntity;
import org.dubhe.domain.entity.Team;
import org.dubhe.domain.entity.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @description  存储
 * @date 2020-03-17
 */
@Data
@TableName("pt_storage")
public class PtStorage extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    @NotNull(groups = {Update.class})
    private Long id;

    @TableField(value = "name")
    @NotBlank
    private String name;

    @TableField(value = "size")
    private Integer size;

    @TableField(value = "storageclass")
    @NotBlank
    private String storageclass;

    @TableField(exist = false)
    private Team team;

    @TableField(exist = false)
    private User createUser;


    public void copy(PtStorage source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }

    public @interface Update {
    }
}
