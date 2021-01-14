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

package org.dubhe.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.domain.entity.Team;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description： 镜像
 * @date 2020-03-17
 */
@Data
public class PtImageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("镜像ID")
    private Long id;

    @ApiModelProperty("镜像名称")
    private String name;

    @ApiModelProperty("镜像描述")
    private String remark;

    @ApiModelProperty("类型")
    private String type;

    @ApiModelProperty("标签")
    private String label;

    @ApiModelProperty("地址")
    private String uri;

    @ApiModelProperty("团队")
    private Team team;

    @ApiModelProperty("创建人")
    private UserSmallDTO createUser;

    @ApiModelProperty("创建时间")
    private Timestamp createTime;

    @ApiModelProperty("更新时间")
    private Timestamp updateTime;

    @ApiModelProperty("删除(0正常，1已删除)")
    private Boolean deleted;

    @ApiModelProperty("资源拥有者ID")
    private Long originUserId;
}
