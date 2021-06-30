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

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dubhe.biz.db.entity.BaseEntity;
import org.dubhe.k8s.domain.bo.TaskYamlBO;
import org.dubhe.k8s.enums.K8sTaskStatusEnum;
import org.dubhe.biz.base.utils.StringUtils;

import java.sql.Timestamp;

/**
 * @description k8s任务对象
 * @date 2020-8-31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("k8s_task")
public class K8sTask extends BaseEntity{
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(hidden = true)
    private Long id;

    @TableField(value = "namespace")
    private String namespace;

    @TableField(value = "resource_name")
    private String resourceName;

    @TableField(value = "task_yaml")
    private String taskYaml;

    @TableField(value = "business")
    private String business;

    @TableField(value = "apply_unix_time")
    private Long applyUnixTime;

    @TableField(value = "apply_display_time")
    private Timestamp applyDisplayTime;

    @TableField(value = "apply_status")
    private Integer applyStatus;

    @TableField(value = "stop_unix_time")
    private Long stopUnixTime;

    @TableField(value = "stop_display_time")
    private Timestamp stopDisplayTime;

    @TableField(value = "stop_status")
    private Integer stopStatus;

    public TaskYamlBO getTaskYamlBO(){
        if (StringUtils.isEmpty(taskYaml)){
            return null;
        }
        return JSON.parseObject(taskYaml, TaskYamlBO.class);
    }

    public void setTaskYamlBO(TaskYamlBO taskYamlBO){
        taskYaml = JSON.toJSONString(taskYamlBO);
    }

    public boolean needCreate(Long time){
        boolean needCreate = applyUnixTime < time && K8sTaskStatusEnum.UNEXECUTED.getStatus().equals(applyStatus);
        boolean needDelete = stopUnixTime < time && K8sTaskStatusEnum.UNEXECUTED.getStatus().equals(stopStatus);
        return needCreate && (needCreate ^ needDelete);
    }

    public boolean needDelete(Long time){
        boolean needCreate = applyUnixTime < time && K8sTaskStatusEnum.UNEXECUTED.getStatus().equals(applyStatus);
        boolean needDelete = stopUnixTime < time && K8sTaskStatusEnum.UNEXECUTED.getStatus().equals(stopStatus);
        return needDelete && (needCreate ^ needDelete);
    }

    /**
     * 判断任务是否已超时
     * @param time
     * @return
     */
    public boolean overtime(Long time){
        return applyUnixTime < time && K8sTaskStatusEnum.UNEXECUTED.getStatus().equals(applyStatus) && stopUnixTime < time && K8sTaskStatusEnum.UNEXECUTED.getStatus().equals(stopStatus);
    }
}
