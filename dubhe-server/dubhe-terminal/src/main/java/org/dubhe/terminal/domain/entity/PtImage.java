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

package org.dubhe.terminal.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.db.entity.BaseEntity;

/**
 * @description 镜像
 * @date 2020-04-27
 */
@Data
@Accessors(chain = true)
@TableName(value = "pt_image", autoResultMap = true)
public class PtImage extends BaseEntity {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 镜像名称
     */
    @TableField(value = "image_name")
    private String imageName;

    /**
     * 镜像地址
     */
    @TableField(value = "image_url")
    private String imageUrl;

    /**
     * 镜像版本
     */
    @TableField(value = "image_tag")
    private String imageTag;

    /**
     * 镜像描述
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * projectName
     */
    @TableField(value = "project_name")
    private String projectName;

    /**
     * 镜像来源
     */
    @TableField(value = "image_resource")
    private Integer imageResource;


    /**
     * 镜像状态
     */
    @TableField(value = "image_status")
    private Integer imageStatus;

    /**
     * 资源拥有者ID
     */
    @TableField(value = "origin_user_id", fill = FieldFill.INSERT)
    private Long originUserId;

    //镜像ssh密码
    @TableField(value = "ssh_pwd")
    private String sshPwd;

    //镜像ssh用户
    @TableField(value = "ssh_user")
    private String sshUser;
}
