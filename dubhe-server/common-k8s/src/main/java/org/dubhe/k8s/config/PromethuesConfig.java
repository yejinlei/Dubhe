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
package org.dubhe.k8s.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @description
 * @date 2021-11-19
 */
@Getter
@Configuration
public class PromethuesConfig {

	/**
	 * 查询某个节点的gpu使用率
	 */
	@Value("${k8s.prometheus.gpu-usage-query-param}")
	private String k8sPrometheusGpuUsageQueryParam;

	@Value("${k8s.prometheus.gpu-usage-rate-query-param}")
	private String k8sGpuUsageRateQueryParam;

	@Value("${k8s.prometheus.cpu-usage-rate-query-param}")
	private String k8sCpuUsageRateQueryParam;

	@Value("${k8s.prometheus.mem-usage-rate-query-param}")
	private String k8sMemUsageRateQueryParam;

	@Value("${k8s.prometheus.gpu-usage-namespace-query-param}")
	private String k8sGpuUsageByNamespaceQueryParam;

	@Value("${k8s.prometheus.cpu-usage-namespace-query-param}")
	private String k8sCpuUsageByNamespaceQueryParam;

	@Value("${k8s.prometheus.mem-usage-namespace-query-param}")
	private String k8sMemUsageByNamespaceQueryParam;


}
