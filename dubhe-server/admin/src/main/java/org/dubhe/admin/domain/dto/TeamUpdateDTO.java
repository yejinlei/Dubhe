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

import lombok.Data;
import org.dubhe.admin.domain.entity.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * @description
 * @date 2020-06-29
 */
@Data
public class TeamUpdateDTO implements Serializable {

    private static final long serialVersionUID = 8922409236439269071L;

    @NotNull
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    private Boolean enabled;

    /**
     * 团队成员
     */
    private List<User> teamUserList;

    private Timestamp createTime;


}
