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

import java.sql.Timestamp;

/**
 * @description 垃圾回收主表
 * @date 2021-02-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "recycle")
public class Recycle extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 回收模块
     */
    @TableField(value = "recycle_module")
    private Integer recycleModule;

    /**
     * 回收延迟时间,以天为单位
     */
    @TableField(value = "recycle_delay_date")
    private Timestamp recycleDelayDate;

    /**
     * 回收定制化方式
     */
    @TableField(value = "recycle_custom")
    private String recycleCustom;

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

    /**
     * 还原定制化方式
     */
    @TableField(value = "restore_custom")
    private String restoreCustom;

}
