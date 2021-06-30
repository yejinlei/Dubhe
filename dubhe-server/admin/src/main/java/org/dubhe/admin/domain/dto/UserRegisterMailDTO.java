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
 * @description 用户注册邮箱请求实体
 * @date 2020-06-01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "发送邮箱验证码请求实体", description = "发送邮箱验证码请求实体")
public class UserRegisterMailDTO implements Serializable {
    private static final long serialVersionUID = 5063855150803214253L;

    @NotNull(message = "类型不能为空")
    @ApiModelProperty(value = "类型  1 用户注册 2 修改邮箱 3 其他 4 忘记密码", name = "type", example = "1")
    private Integer type;

    @NotEmpty(message = "邮箱地址不能为空")
    @Pattern(regexp = "^([\\w-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([\\w-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$", message = "邮箱地址格式有误")
    @ApiModelProperty(value = "邮箱地址", name = "email", example = "xxx@163.com")
    private String email;

}
