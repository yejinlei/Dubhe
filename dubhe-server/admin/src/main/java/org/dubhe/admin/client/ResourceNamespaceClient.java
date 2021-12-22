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
package org.dubhe.admin.client;

import org.dubhe.admin.client.fallback.ResourceNamespaceClientFallback;
import org.dubhe.biz.base.constant.ApplicationNameConst;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.UserAllotVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @description 远程调用
 * @date 2021-11-19
 */
@FeignClient(value = ApplicationNameConst.SERVER_K8S, contextId = "ResourceNamespaceClient", fallback = ResourceNamespaceClientFallback.class)
public interface ResourceNamespaceClient {

	/**
	 * 查看用户资源用量峰值
	 *
	 * @param resourceType 资源类型
	 * @param sumDay 统计时间段
	 * @return List<UserAllotVO> 用户资源用量峰值VO 实体类
	 */
	@GetMapping("/namespace/ResourceUsage")
	DataResponseBody<List<UserAllotVO>> getResourceNamespace(@RequestParam(value = "resourceType") Integer resourceType,
	                                                         @RequestParam(value = "sumDay") String sumDay);

	/**
	 * 查询用户某段时间内的资源用量峰值
	 *
	 * @param resourceType 资源类型
	 * @param sumDay 统计时间段
	 * @param namespaces 用户命名空间   
	 * @return String 资源用量峰值
	 */
	@GetMapping("/namespace/ResourceByUser")
	DataResponseBody<Map<Long, String>> getResourceUsageByUser(@RequestParam(value = "resourceType") Integer resourceType,
	                                                           @RequestParam(value = "sumDay") String sumDay,
	                                                           @RequestParam(value = "namespaces") String namespaces);
}
