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

package org.dubhe.model.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.db.entity.BaseEntity;

import javax.validation.constraints.NotNull;

/**
 * @description 模型版本管理
 * @date 2020-03-24
 */

@Data
@Accessors(chain = true)
@TableName("pt_model_branch")
public class PtModelBranch extends BaseEntity {

    /**
     * 版本ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @NotNull(groups = {Update.class})
    private Long id;

    /**
     * 父ID
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 版本号
     */
    @TableField(value = "version")
    private String version;

    /**
     * 模型地址
     */
    @TableField(value = "url")
    private String modelAddress;

    /**
     * 模型路径
     */
    @TableField(value = "model_path")
    private String modelPath;

    /**
     * 模型来源
     */
    @TableField(value = "model_source")
    private Integer modelSource;

    /**
     * 算法ID
     */
    @TableField(value = "algorithm_id")
    private Long algorithmId;

    /**
     * 算法名称
     */
    @TableField(value = "algorithm_name")
    private String algorithmName;

    /**
     * 算法来源(1为我的算法，2为预置算法)
     */
    @TableField(value = "algorithm_source")
    private Integer algorithmSource;

    /**
     * 文件拷贝状态 (0文件拷贝中，1文件拷贝成功，2文件拷贝失败)
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 团队ID
     */
    @TableField(value = "team_id")
    private Integer teamId;

    /**
     * 资源拥有者ID
     */
    @TableField(value = "origin_user_id", fill = FieldFill.INSERT)
    private Long originUserId;

}
