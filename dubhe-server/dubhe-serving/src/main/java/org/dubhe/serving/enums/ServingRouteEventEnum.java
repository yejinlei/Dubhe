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
package org.dubhe.serving.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @description 路由事件类型
 * @date 2020-09-14
 */
public enum ServingRouteEventEnum {

	/**
	 * 新增路由
	 */
	SAVE("_SAVE", "新增路由"),
	/**
	 * 移除路由
	 */
	DELETE("_DELETE", "移除路由");


	/**
	 * 关键字
	 */
	private String code;

	/**
	 * 描述
	 */
	private String description;

	ServingRouteEventEnum(String code, String description) {
		this.code = code;
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	private static final Map<String, ServingRouteEventEnum> SERVING_ROUTE_EVENT_ENUM_MAP = new HashMap<String, ServingRouteEventEnum>() {
		{
			for (ServingRouteEventEnum enums : ServingRouteEventEnum.values()) {
				put(enums.getCode(), enums);
			}
		}
	};

	/**
	 * 根据createResource获取BizNfsEnum
	 *
	 * @param code
	 * @return
	 */
	public static ServingRouteEventEnum getByCode(String code) {
		return SERVING_ROUTE_EVENT_ENUM_MAP.get(code);
	}
}
