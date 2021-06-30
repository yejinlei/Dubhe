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

import javax.validation.constraints.Size;

/**
 * @description 修改密码的
 * @date 2020-06-01
 */
@Data
public class UserPassUpdateDTO {

    @Size(max = 255, message = "密码长度不能超过255")
    private String oldPass;

    @Size(max = 255, message = "密码长度不能超过255")
    private String newPass;
}
