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
package org.dubhe.recycle.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.dubhe.biz.db.entity.BaseEntity;

/**
 * @description 垃圾回收详情表
 * @date 2021-02-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "recycle_detail")
public class RecycleDetail extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "recycle_id")
    private Long recycleId;

    /**
     * 回收类型(0文件，1数据库表数据
     */
    @TableField(value = "recycle_type")
    private Integer recycleType;

    /**
     * 回收条件(回收表数据sql、回收文件绝对路径)
     */
    @TableField(value = "recycle_condition")
    private String recycleCondition;

    /**
     * 回收状态
     */
    @TableField(value = "recycle_status")
    private Integer recycleStatus;

    /**
     * 回收说明
     */
    @TableField(value = "recycle_note")
    private String recycleNote;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 回收响应信息
     */
    @TableField(value = "recycle_response")
    private String recycleResponse;

}
