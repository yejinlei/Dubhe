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

package org.dubhe.servinggateway.constant;

/**
 * @description gateway常量类
 * @date 2020-09-28
 */
public class GatewayConstant {

	/**
	 * 路由权重断言的分组名前缀
	 */
	public static final String GROUP_PREFIX = "servingInfo";
	/**
	 * 路由ID的前缀
	 */
	public static final String ROUTE_PREFIX = "route-";
	/**
	 * redis中以hash形式存储的路由key
	 */
	public static final String SERVING_GATEWAY_ROUTES = "serving:gateway:routes";
	/**
	 * 路由信息stream
	 */
	public final static String SERVING_STREAM = "serving_stream";
	/**
	 * redis中推理调用指标的key的前缀
	 */
	public final static String INFERENCE_METRICS_PREFIX = "serving:inference:metrics:";
	/**
	 * 部署模型的推理接口名称
	 */
	public static final String INFERENCE_INTERFACE_NAME = "/inference";
	/**
	 * 推理服务的调用次数(redis存储的key)
	 */
	public static final String INFERENCE_CALL_COUNT = "callCount";
	/**
	 * 推理服务的成功调用次数(redis存储的key)
	 */
	public static final String INFERENCE_FAILED_COUNT = "failedCount";
	/**
	 * 成功
	 */
	public static final String SUCCESS = "success";
	/**
	 * AES加密方式
	 */
	public static final String AES = "AES";
	/**
	 * MD5加密方式
	 */
	public static final String MD5 = "MD5";

}
