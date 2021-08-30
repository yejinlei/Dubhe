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

package org.dubhe.notebook.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.db.entity.BaseEntity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * @description note book 实体
 * @date 2020-04-28
 */
@Data
@TableName("notebook")
public class NoteBook extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(hidden = true)
    private Long id;

    @TableField(value = "origin_user_id",fill = FieldFill.INSERT)
    @ApiModelProperty(hidden = true)
    private Long originUserId;

    @TableField(value = "name")
    @JsonIgnore
    private String name;

    @TableField(value = "notebook_name")
    private String noteBookName;

    @TableField(value = "description")
    private String description;

    public final static String COLUMN_URL = "url";
    @TableField(value = COLUMN_URL)
    @ApiModelProperty(hidden = true)
    private String url;

    @TableField(value = "total_run_min")
    @ApiModelProperty(hidden = true)
    private Integer totalRunMin;

    @TableField(value = "cpu_num")
    @ApiModelProperty(value = "cpu数量")
    private Integer cpuNum;

    @TableField(value = "gpu_num")
    @ApiModelProperty(value = "gpu数量")
    private Integer gpuNum;

    @TableField(value = "mem_num")
    @ApiModelProperty(value = "内存大小")
    private Integer memNum;

    @TableField(value = "disk_mem_num")
    @ApiModelProperty(value = "硬盘内存大小")
    private Integer diskMemNum;


    public final static String COLUMN_STATUS = "status";
    /**
     * 0运行中，1停止, 2删除, 3启动中，4停止中，5删除中，6运行异常（暂未启用）
     */
    @TableField(value = COLUMN_STATUS)
    @ApiModelProperty(hidden = true)
    private Integer status;

    @TableField(value = "status_detail")
    @ApiModelProperty(value = "状态对应的详情信息")
    private String statusDetail;

    @TableField(value = "last_start_time")
    @ApiModelProperty(hidden = true)
    private Date lastStartTime;

    @TableField(value = "last_operation_timeout")
    @ApiModelProperty(hidden = true)
    private Long lastOperationTimeout;

    /**
     * 0 - notebook 创建 1- 其它系统创建
     */
    @TableField(value = "create_resource")
    @ApiModelProperty(hidden = true)
    private Integer createResource;

    @TableField(value = "k8s_status_code")
    @ApiModelProperty(hidden = true)
    private String k8sStatusCode;

    @TableField(value = "k8s_status_info")
    @ApiModelProperty(hidden = true)
    private String k8sStatusInfo;

    @TableField(value = "k8s_namespace")
    @ApiModelProperty(hidden = true)
    private String k8sNamespace;

    @TableField(value = "k8s_resource_name")
    @ApiModelProperty(hidden = true)
    private String k8sResourceName;

    @TableField(value = "k8s_image_name")
    @ApiModelProperty("镜像名称")
    @NotNull(message = "请选择镜像")
    private String k8sImageName;

    @TableField(value = "k8s_mount_path")
    @ApiModelProperty(hidden = true)
    private String k8sMountPath;

    @TableField(value = "k8s_pvc_path")
    @ApiModelProperty(hidden = true)
    private String k8sPvcPath;

    @TableField(value = "data_source_name")
    @ApiModelProperty(hidden = true)
    @Size(max = 255, message = "数据集名称超长")
    private String dataSourceName;

    @TableField(value = "data_source_path")
    @Size(max = 255, message = "数据集路径超长")
    @ApiModelProperty(hidden = true)
    private String dataSourcePath;

    @TableField(value = "algorithm_id")
    @ApiModelProperty(hidden = true)
    private Long algorithmId;


    @TableField(value = "pip_site_package_path")
    @Size(max = 255, message = "pip包路径")
    @ApiModelProperty(hidden = true)
    private String pipSitePackagePath;

    /**
     * put 键值
     *
     * @param key 键
     * @param value 值
     */
    public void putStatusDetail(String key,String value){
        statusDetail = StringUtils.putIntoJsonStringMap(key,value,statusDetail);
    }

    /**
     * 移除 键值
     *
     * @param key 键
     */
    public void removeStatusDetail(String key){
        statusDetail = StringUtils.removeFromJsonStringMap(key,statusDetail);
    }
}
