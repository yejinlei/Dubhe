/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

package org.dubhe.constant;

/**
 * @description 权限标识，对应 menu 表中的 permission 字段
 * @date 2020-05-14
 */
public final class Permissions {

    /**
     * 数据管理
     */
	public static final String DATA = "data";
	public static final String DATA_DATASET = "data:dataset";

	/**
	 * 模型开发
	 */
	public static final String DEVELOPMENT = "development";
	public static final String DEVELOPMENT_NOTEBOOK = "development:notebook";
	public static final String DEVELOPMENT_ALGORITHM = "development:algorithm";
	
	/**
	 * 训练管理
	 */
	public static final String TRAINING = "training";
	public static final String TRAINING_IMAGE = "training:image";
	public static final String TRAINING_JOB = "training:job";

	/**
	 * 模型管理
	 */
	public static final String MODEL = "model";
	public static final String MODEL_MODEL = "model:model";

	/**
	 * 控制台
	 */
	public static final String SYSTEM = "system";
	public static final String SYSTEM_USER = "system:user";
	public static final String SYSTEM_ROLE = "system:role";
	public static final String SYSTEM_MENU = "system:menu";
	public static final String SYSTEM_DICT = "system:dict";
	public static final String SYSTEM_NODE = "system:node";
	public static final String SYSTEM_LOG = "system:log";
	public static final String SYSTEM_TEAM = "system:team";

	private Permissions() {
	}
}
