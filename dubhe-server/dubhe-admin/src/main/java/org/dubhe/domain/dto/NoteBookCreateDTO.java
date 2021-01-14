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

package org.dubhe.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.utils.NotebookUtil;

import javax.validation.constraints.*;

/**
 * @description  创建notebook请求体
 * @date 2020-06-28
 */
@Data
public class NoteBookCreateDTO {

    @NotBlank
    @Pattern(regexp = NotebookUtil.K8S_NOTEBOOK_REGEX, message = "Notebook 名称仅支持字母、数字、汉字、英文横杠和下划线")
    @Size(max = 30, message = "名称内容超长")
    @ApiModelProperty(value = "notebook 名称")
    private String noteBookName;

    @Size(max = 255, message = "描述内容超长")
    @ApiModelProperty(value = "notebook 描述")
    private String description;

    @ApiModelProperty(hidden = true)
    private String url;

    @ApiModelProperty(hidden = true)
    private Integer totalRunMin;

    @NotNull
    @Min(value = NotebookUtil.CPU_MIN_NUMBER, message = "最少需要一个CPU")
    @Max(value = NotebookUtil.CPU_MAX_NUMBER, message = "CPU数量超过限制，不可大于8")
    @ApiModelProperty(value = "cpu数量")
    private Integer cpuNum;

    @Min(value = NotebookUtil.GPU_MIN_NUMBER, message = "最少需要一个GPU")
    @Max(value = NotebookUtil.GPU_MAX_NUMBER, message = "GPU数量超过限制，不可大于4")
    @ApiModelProperty(value = "gpu数量")
    private Integer gpuNum;

    @NotNull
    @Min(value = NotebookUtil.MEMORY_MIN_NUMBER, message = "最少需要1G内存")
    @Max(value = NotebookUtil.MEMORY_MAX_NUMBER, message = "内存超出限制，不可大于8")
    @ApiModelProperty(value = "内存大小")
    private Integer memNum;

    @NotNull
    @Min(value = NotebookUtil.DISK_MEMORY_MIN_NUMBER, message = "至少需要1G硬盘内存")
    @Max(value = NotebookUtil.DISK_MEMORY_MAX_NUMBER, message = "硬盘内存超出限制，不可大于1024")
    @ApiModelProperty(value = "硬盘内存大小")
    private Integer diskMemNum;

    @ApiModelProperty("镜像名称")
    @NotNull(message = "请选择镜像")
    private String k8sImageName;
}
