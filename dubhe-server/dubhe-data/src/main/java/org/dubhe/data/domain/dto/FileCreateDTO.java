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

package org.dubhe.data.domain.dto;

import cn.hutool.core.io.FileUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.data.domain.entity.File;
import org.dubhe.data.machine.constant.FileStateCodeConstant;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description 文件
 * @date 2020-04-29
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "File dto", description = "文件信息")
public class FileCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "文件路径不能为空")
    @ApiModelProperty(value = "资源访问路径")
    private String url;

    @ApiModelProperty(value = "帧间隔")
    @NotNull(message = "帧间隔不能为空")
    @Min(value = NumberConstant.NUMBER_1, message = "帧间隔不能小于1")
    private Integer frameInterval;

    @ApiModelProperty(value = "父Id")
    private Long pid;

    @ApiModelProperty(value = "文件状态")
    private Integer status;

    @ApiModelProperty(value = "增强类型")
    private Integer enhanceType;

    @ApiModelProperty(value = "处理人")
    private Long createUserId;

    @ApiModelProperty(value = "图片宽")
    private Integer width;

    @ApiModelProperty(value = "图片高")
    private Integer height;

    @ApiModelProperty(value = "文件名")
    private String name;

    @ApiModelProperty(value = "文件内容")
    private String content;


    public FileCreateDTO(String url, Long pid, Integer status, Integer enhanceType, Long userId, Integer width, Integer height) {
        this.url = url;
        this.pid = pid;
        this.status = status;
        this.enhanceType = enhanceType;
        this.createUserId = userId;
        this.width = width;
        this.height = height;
    }

    public static File toFile(FileCreateDTO dto, long id, long datasetUserId) {
        File file = File.builder()
                .name(FileUtil.mainName(dto.getUrl()))
                .datasetId(id)
                .status(FileStateCodeConstant.NOT_ANNOTATION_FILE_STATE)
                .url(dto.getUrl())
                .pid(dto.getPid())
                .enhanceType(dto.getEnhanceType())
                .width(dto.width)
                .height(dto.height)
                .frameInterval(MagicNumConstant.ZERO)
                .build();
        file.setCreateUserId(dto.getCreateUserId());
        file.setOriginUserId(datasetUserId);
        return file;
    }

    public static File toFile(FileCreateDTO dto, long id, int type, long pid) {
        return File.builder()
                .name(FileUtil.mainName(dto.getUrl()))
                .datasetId(id)
                .status(FileStateCodeConstant.NOT_ANNOTATION_FILE_STATE)
                .url(dto.getUrl())
                .fileType(type)
                .pid(pid)
                .frameInterval(dto.getFrameInterval())
                .build();
    }

}
