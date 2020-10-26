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
package org.dubhe.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.dubhe.base.BaseEntity;

import java.util.Date;

/**
 * @description 垃圾回收
 * @date 2020-09-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "recycle_task")
public class RecycleTask extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 回收模块
     */
    @TableField(value = "recycle_module")
    private Integer recycleModule;

    /**
     * 回收类型(0文件，1数据库表数据
     */
    @TableField(value = "recycle_type")
    private Integer recycleType;

    /**
     * 回收定制化方式
     */
    @TableField(value = "recycle_custom")
    private String recycleCustom;

    /**
     * 回收条件(回收表数据sql、回收文件绝对路径)
     */
    @TableField(value = "recycle_condition")
    private String recycleCondition;

    /**
     * 回收延迟时间,以天为单位
     */
    @TableField(value = "recycle_delay_date")
    private Date recycleDelayDate;

    /**
     * 回收状态(0:待回收，1:已回收，2:回收失败)
     */
    @TableField(value = "recycle_status")
    private Integer recycleStatus;

    /**
     * 回收备注
     */
    @TableField(value = "recycle_note")
    private String recycleNote;


}
