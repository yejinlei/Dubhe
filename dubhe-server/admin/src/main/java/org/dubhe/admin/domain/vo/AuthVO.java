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
package org.dubhe.admin.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.admin.domain.entity.Permission;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * @description
 * @date 2021-05-17
 */
@Data
public class AuthVO implements Serializable {

    private static final long serialVersionUID = -1584916470133872539L;

    @ApiModelProperty("权限组id")
    private Long id;

    @ApiModelProperty("权限组名称")
    private String authCode;

    @ApiModelProperty("创建人")
    private Long createUserId;

    @ApiModelProperty("修改人")
    private Long updateUserId;

    @ApiModelProperty("创建时间")
    private Timestamp createTime;

    @ApiModelProperty("修改时间")
    private Timestamp updateTime;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("权限列表")
    private List<Permission> permissions;
}
