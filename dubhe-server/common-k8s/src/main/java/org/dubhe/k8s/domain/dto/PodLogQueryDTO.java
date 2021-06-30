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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.MagicNumConstant;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description Pod日志 查询DTO
 * @date 2020-08-14
 */
@Data
@Accessors(chain = true)
@Api("Pod日志 查询DTO")
public class PodLogQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "命名空间", required = true)
    @NotBlank(message = "命名空间不能为空")
    private String namespace;

    @ApiModelProperty("k8s实际pod名称")
    @NotNull(message = "pod名称不能为空")
    private String podName;

    @ApiModelProperty(value = "起始行")
    private Integer startLine;

    @ApiModelProperty(value = "查询行数")
    private Integer lines;

    @ApiModelProperty(value = "日志查询条件：关键字")
    private String logKeyword;

    @ApiModelProperty(value = "日志查询时间范围：开始时间")
    private Long beginTimeMillis;

    @ApiModelProperty(value = "日志查询时间范围：结束时间")
    private Long endTimeMillis;

    public PodLogQueryDTO() {

    }

    public PodLogQueryDTO(String podName) {
        this.podName = podName;
    }

    /**
     * 初始化获取起始行
     * @return
     */
    @JsonIgnore
    public int getQueryStart() {
        if (startLine == null || startLine < MagicNumConstant.ONE) {
            return MagicNumConstant.ONE;
        }
        return startLine;
    }

    /**
     * 初始化获取查询行数
     * @return
     */
    @JsonIgnore
    public int getQueryLines() {
        if (lines == null || lines < MagicNumConstant.ONE || lines > MagicNumConstant.FOUR_THOUSAND) {
            return MagicNumConstant.FOUR_THOUSAND;
        }
        return lines;
    }

}
