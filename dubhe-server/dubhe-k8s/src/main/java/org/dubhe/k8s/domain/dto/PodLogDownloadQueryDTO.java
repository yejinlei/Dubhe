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

package org.dubhe.k8s.domain.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.k8s.domain.vo.PodVO;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @description Pod日志 下载DTO
 * @date 2020-08-21
 */
@Data
@Accessors(chain = true)
@Api("Pod日志 下载DTO")
public class PodLogDownloadQueryDTO {

    @ApiModelProperty("k8s节点信息")
    @NotEmpty(message = "k8s节点信息为空")
    private List<PodVO> podVOList;

}