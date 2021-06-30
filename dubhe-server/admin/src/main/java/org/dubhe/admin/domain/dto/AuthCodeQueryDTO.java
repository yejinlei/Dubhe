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
import org.dubhe.biz.db.base.PageQueryBase;

import java.io.Serializable;

/**
 * @description 分页查看权限组列表
 * @date 2021-05-14
 */

@Data
public class AuthCodeQueryDTO extends PageQueryBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "权限组名称")
    private String authCode;

}
