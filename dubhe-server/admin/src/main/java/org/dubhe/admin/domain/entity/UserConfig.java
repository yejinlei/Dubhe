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
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.db.entity.BaseEntity;

/**
 * @description 用户配置实体
 * @date 2021-06-30
 */
@Data
@TableName("user_config")
@Accessors(chain = true)
public class UserConfig extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableId(value = "user_id")
    private Long userId;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String nickName;

    @TableId(value = "notebook_delay_delete_time")
    private Integer notebookDelayDeleteTime;

    @TableId(value = "cpu_limit")
    private Integer cpuLimit;

    @TableId(value = "memory_limit")
    private Integer memoryLimit;

    @TableId(value = "default_image_id")
    private Long defaultImageId;
}
