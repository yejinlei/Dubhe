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

package org.dubhe.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * @Description 字典查询转换DTO
 * @Date 2020-06-01
 */
@Data
public class MenuDTO implements Serializable {

    private Long id;

    private Integer type;

    private String permission;

    private String name;

    private Long sort;

    private String path;

    private String component;

    private Long pid;

    private Boolean cache;

    private Boolean hidden;

    private String componentName;

    private String icon;

    private String layout;

    private List<MenuDTO> children;

    private Timestamp createTime;
}
