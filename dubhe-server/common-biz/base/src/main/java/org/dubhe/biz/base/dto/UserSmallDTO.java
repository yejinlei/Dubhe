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

/**
 * @description 用户信息DTO
 * @date 2020-06-01
 */
@Data
public class UserSmallDTO {
    private String username;
    private String nickName;

    public UserSmallDTO() {}

    public UserSmallDTO(UserDTO userDTO) {
        this.username = userDTO.getUsername();
        this.nickName = userDTO.getNickName();
    }

}
