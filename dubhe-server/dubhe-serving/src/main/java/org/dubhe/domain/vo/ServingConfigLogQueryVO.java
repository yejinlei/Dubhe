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
package org.dubhe.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @description
 * @date 2020-09-25
 */
@Data
@Accessors(chain = true)
public class ServingConfigLogQueryVO {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty("log内容")
	private List<String> content;

	@ApiModelProperty(value = "任务名称")
	private Long servingConfigId;

	@ApiModelProperty(value = "起始行")
	private Integer startLine;

	@ApiModelProperty(value = "结束行")
	private Integer endLine;

	@ApiModelProperty(value = "lines")
	private Integer lines;
}
