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

package org.dubhe.admin.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * @description 字典查询转换DTO
 * @date 2020-06-01
 */
@Data
public class MenuDTO implements Serializable {

    private Long id;

    @ApiModelProperty(value = "菜单类型: 0目录，1页面，2权限，3外链")
    private Integer type;

    @ApiModelProperty(value = "权限标识")
    private String permission;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "菜单排序")
    private Long sort;

    @ApiModelProperty(value = "路径或外链URL")
    private String path;

    @ApiModelProperty(value = "组件路径")
    private String component;

    @ApiModelProperty(value = "上级菜单ID")
    private Long pid;

    @ApiModelProperty(value = "路由缓存 keep-alive")
    private Boolean cache;

    @ApiModelProperty(value = "菜单栏不显示")
    private Boolean hidden;

    @ApiModelProperty(value = "路由名称")
    private String componentName;

    @ApiModelProperty(value = "菜单图标")
    private String icon;

    @ApiModelProperty(value = "页面布局类型")
    private String layout;


    private List<MenuDTO> children;

    private Timestamp createTime;

    @ApiModelProperty(value = "回到上一级")
    private String backTo;

    @ApiModelProperty(value = "扩展配置")
    private String extConfig;
}
