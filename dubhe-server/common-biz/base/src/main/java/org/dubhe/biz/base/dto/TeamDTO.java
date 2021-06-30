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

package org.dubhe.biz.base.dto;

import lombok.Data;
import org.dubhe.biz.base.dto.UserSmallDTO;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * @description 团队转换DTO
 * @date 2020-06-01
 */
@Data
public class TeamDTO implements Serializable {

    private static final long serialVersionUID = -7049447715255649751L;
    private Long id;

    private String name;

    private Boolean enabled;

    private Long pid;

    /**
     * 团队成员
     */
    private List<UserSmallDTO> teamUserList;

    private Timestamp createTime;

    public String getLabel() {
        return name;
    }
}
