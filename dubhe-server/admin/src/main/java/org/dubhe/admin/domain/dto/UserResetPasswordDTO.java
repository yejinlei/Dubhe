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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @description 用户重置密码请求实体
 * @date 2020-06-01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "用户重置密码请求实体", description = "用户重置密码请求实体")
public class UserResetPasswordDTO implements Serializable {


    private static final long serialVersionUID = -4249894291904235207L;

    @NotEmpty(message = "邮箱地址不能为空")
    @Pattern(regexp = "^([\\w-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([\\w-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$", message = "邮箱地址格式有误")
    @ApiModelProperty(value = "邮箱地址", name = "email", example = "xxx@163.com")
    private String email;

    @NotNull(message = "密码不能为空")
    @ApiModelProperty(value = "密码", name = "password", example = "123456")
    private String password;

    @NotNull(message = "激活码不能为空")
    @ApiModelProperty(value = "激活码", name = "code", example = "998877")
    private String code;

}
