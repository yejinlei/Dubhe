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
package org.dubhe.serving.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.k8s.domain.vo.PtPodsVO;

import java.util.List;

/**
 * @description 服务部署模型的监控信息VO对象
 * @date 2020-10-14
 */
@Data
@Accessors(chain = true)
public class ServingConfigMetricsVO {

	@ApiModelProperty(value = "id")
	private Long id;

	@ApiModelProperty(value = "模型名称")
	private String modelName;

	@ApiModelProperty(value = "模型版本")
	private String modelVersion;

	@ApiModelProperty(value = "pod监控信息")
	private List<ServingPodMetricsVO> podList;

	@ApiModelProperty(value = "调用失败次数")
	private String failNum;

	@ApiModelProperty(value = "调用总次数")
	private String totalNum;
}
