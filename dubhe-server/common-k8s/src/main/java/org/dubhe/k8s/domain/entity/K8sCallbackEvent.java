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
package org.dubhe.k8s.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dubhe.biz.db.entity.BaseEntity;

import java.util.Date;

/**
 * @description k8s callback event信息
 * @date 2021-11-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("k8s_callback_event")
public class K8sCallbackEvent extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(hidden = true)
    private Long id;

    @TableField(value = "resource_name")
    private String resourceName;

    @TableField(value = "event_type")
    private String eventType;

    @TableField(value = "business_type")
    private String businessType;

    @TableField(value = "message")
    private String message;

    @TableField(value = "start_time")
    private String startTime;

    @TableField(value = "finish_time")
    private String finishTime;

    @TableField(value = "container_id")
    private String containerId;
}
