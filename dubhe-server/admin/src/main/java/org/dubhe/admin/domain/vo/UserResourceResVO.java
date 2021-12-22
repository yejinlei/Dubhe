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
package org.dubhe.admin.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.vo.GpuAllotVO;

import java.util.List;

/**
 * @description 用户资源分页列表VO
 * @date 2021-11-23
 */
@Data
@Accessors(chain = true)
public class UserResourceResVO {

	@ApiModelProperty("编号")
	private Long id;

	@ApiModelProperty("用户名")
	private String userName;

	@ApiModelProperty("账户名（昵称）")
	private String nickName;

	@ApiModelProperty("GPU配额")
	private String gpu;

	@ApiModelProperty("GPU具体型号配额")
	private List<GpuAllotVO> gpuModelAllots;

	@ApiModelProperty("7天内GPU峰值使用率")
	private String gpu7;

	@ApiModelProperty("7天内GPU峰值使用量")
	private String gpu7unit;

	@ApiModelProperty("15天内GPU峰值使用率")
	private String gpu15;

	@ApiModelProperty("15天内GPU峰值使用量")
	private String gpu15unit;

	@ApiModelProperty("内存配额")
	private String mem;

	@ApiModelProperty("7天内内存峰值使用率")
	private String mem7;

	@ApiModelProperty("7天内内存峰值使用量")
	private String mem7unit;

	@ApiModelProperty("15天内内存峰值使用率")
	private String mem15;

	@ApiModelProperty("15天内内存峰值使用量")
	private String mem15unit;

	@ApiModelProperty("CPU配额")
	private String cpu;

	@ApiModelProperty("7天内CPU峰值使用率")
	private String cpu7;

	@ApiModelProperty("7天内CPU峰值使用量")
	private String cpu7unit;

	@ApiModelProperty("15天内CPU峰值使用率")
	private String cpu15;

	@ApiModelProperty("15天内CPU峰值使用量")
	private String cpu15unit;

}
