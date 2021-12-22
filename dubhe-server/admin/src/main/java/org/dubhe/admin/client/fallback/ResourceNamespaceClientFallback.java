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
package org.dubhe.admin.client.fallback;

import org.dubhe.admin.client.ResourceNamespaceClient;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.UserAllotVO;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;

import java.util.List;
import java.util.Map;

/**
 * @description
 * @date 2021-11-19
 */
public class ResourceNamespaceClientFallback implements ResourceNamespaceClient {

	@Override
	public DataResponseBody<List<UserAllotVO>> getResourceNamespace(Integer resourceType, String sumDay) {
		return DataResponseFactory.failed("Call MetricsApi.getNamespaceUsageRate error");
	}

	@Override
	public DataResponseBody<Map<Long, String>> getResourceUsageByUser(Integer resourceType, String sumDay, String namespaces) {
		return DataResponseFactory.failed("Call MetricsApi.getResourceUsageByUser error");
	}
}
