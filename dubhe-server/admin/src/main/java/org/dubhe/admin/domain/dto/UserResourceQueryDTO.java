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

import javax.validation.constraints.NotNull;

/**
 * @description 用户资源统计DTO
 * @date 2021-11-17
 */
@Data
public class UserResourceQueryDTO {

	@ApiModelProperty("资源统计类型")
	@NotNull
	private Integer statType;

	@ApiModelProperty("资源类型")
	@NotNull
	private Integer resourceType;

	@ApiModelProperty("统计时间段")
	private String sumDay;
}
