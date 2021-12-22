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
package org.dubhe.admin.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.admin.domain.dto.UserResourceListDTO;
import org.dubhe.admin.domain.dto.UserResourceQueryDTO;
import org.dubhe.admin.service.UserResourceService;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.UserAllotVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @description 用户资源控制层
 * @date 2021-11-23
 */
@Api(tags = "控制台：用户统计")
@RestController
@RequestMapping("/resource")
public class UserResourceController {

	@Autowired
	private UserResourceService userResourceService;


	@ApiOperation("用户Top统计")
	@GetMapping("/total")
	public DataResponseBody<List<UserAllotVO>> getUserResourceTotal(@Validated UserResourceQueryDTO resourceQueryDTO) {
		return new DataResponseBody(userResourceService.getResourceTotal(resourceQueryDTO));
	}

	@ApiOperation("用户资源统计列表")
	@GetMapping("/list")
	public DataResponseBody getUserResourceList(UserResourceListDTO UserResourceListDTO) {
		return new DataResponseBody(userResourceService.getResourceList(UserResourceListDTO));
	}
}
