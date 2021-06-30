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
package org.dubhe.k8s.domain.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.MagicNumConstant;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @description Pod基础信息查询入参
 * @date 2020-08-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Api("Pod基础信息查询入参")
public class PodQueryDTO {

    @ApiModelProperty(value = "命名空间", required = true)
    @NotBlank(message = "命名空间不能为空")
    private String namespace;

    @ApiModelProperty(value = "资源名称", required = false)
    private String resourceName;

    @ApiModelProperty(value = "pod名称列表", required = false)
    private List<String> podNames;

    @ApiModelProperty(value = "开始时间(unix时间戳/秒)", required = false)
    private Long startTime;

    @ApiModelProperty(value = "结束时间(unix时间戳/秒)", required = false)
    private Long endTime;

    @ApiModelProperty(value = "步长/秒", required = false)
    private Integer step;

    /**
     * 4小时秒数
     */
    private static final Long FOUR_HOUR_SECONDS = MagicNumConstant.SIXTY_LONG * MagicNumConstant.SIXTY_LONG * MagicNumConstant.FOUR;

    public PodQueryDTO(String namespace, String resourceName) {
        this.namespace = namespace;
        this.resourceName = resourceName;
    }

    /**
     * 未设置部分参数时生成默认参数
     */
    public void generateDefaultParam() {
        if (startTime == null) {
            startTime = System.currentTimeMillis() / MagicNumConstant.THOUSAND_LONG - FOUR_HOUR_SECONDS;
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis() / MagicNumConstant.THOUSAND_LONG;
        }
        if (step == null || step == 0) {
            step = MagicNumConstant.TEN;
        }
    }
}
