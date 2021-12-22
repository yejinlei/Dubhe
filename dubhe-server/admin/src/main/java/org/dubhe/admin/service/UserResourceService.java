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
package org.dubhe.admin.service;

import org.dubhe.admin.domain.dto.UserResourceListDTO;
import org.dubhe.admin.domain.dto.UserResourceQueryDTO;
import org.dubhe.admin.domain.vo.UserResourceResVO;
import org.dubhe.biz.base.vo.UserAllotVO;

import java.util.List;
import java.util.Map;

/**
 * @description 用户资源统计接口层
 * @date 2021-11-23
 */
public interface UserResourceService {

	/**
	 * 用户资源统计
	 *
	 * @param resourceQueryDTO 查询DTO实体
	 * @return List<UserAllotVO> 用户资源Top数据
	 */
	List<UserAllotVO> getResourceTotal(UserResourceQueryDTO resourceQueryDTO);

	/**
	 * 用户资源统计列表
	 *
	 * @return List<UserResourceResVO> 用户资源列表VO实体
	 */
	Map<String, Object> getResourceList(UserResourceListDTO resourceListDTO);

}
