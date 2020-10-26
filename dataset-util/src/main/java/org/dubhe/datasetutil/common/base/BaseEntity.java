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
package org.dubhe.datasetutil.common.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description  Entity基础类
 * @date 2020-03-15
 */
@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 4936056317364745513L;

    /**
     * 删除标识
     **/
    @TableField(value = "deleted",fill = FieldFill.INSERT)
    @TableLogic
    private Boolean deleted = false;

    /**
     * 创建人id
     **/
    @TableField(value = "create_user_id",fill = FieldFill.INSERT)
    private Long createUserId;

    /**
     * 修改人id
     **/
    @TableField(value = "update_user_id",fill = FieldFill.INSERT_UPDATE)
    private Long updateUserId;

    /**
     * 创建时间
     **/
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Timestamp createTime;

    /**
     * 修改时间
     **/
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private Timestamp updateTime;

}
