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
package org.dubhe.admin.enums;

/**
 * @description
 * @date 2021-11-17
 */
public enum ResourceTypeEnum {
	GPU_TYPE(1, "gpu"),
	CPU_TYPE(2, "cpu"),
	MEMORY_TYPE(3, "memory");


	private Integer code;

	private String desc;

	ResourceTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public Integer getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}

	@Override
	public String toString() {
		return "[" + this.code + "]" + this.desc;
	}

	public static boolean isGpuType(Integer code) {
		return GPU_TYPE.code.equals(code);
	}
}
