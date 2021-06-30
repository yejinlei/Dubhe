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

package org.dubhe.algorithm.domain.vo;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description 训练算法返回列表
 * @date 2020-04-27
 */
@Data
public class PtTrainAlgorithmQueryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "算法ID")
    private Long id;

    @ApiModelProperty(value = "算法名称")
    private String algorithmName;

    @ApiModelProperty(value = "描述信息")
    private String description;

    @ApiModelProperty(value = "算法来源")
    private Integer algorithmSource;

    @ApiModelProperty(value = "镜像名称")
    private String imageName;

    @ApiModelProperty(value = "算法文件大小")
    private String algorithmFileSize;

    @ApiModelProperty(value = "镜像版本")
    private String imageTag;

    @ApiModelProperty(value = "代码目录")
    private String codeDir;

    @ApiModelProperty(value = "运行命令")
    private String runCommand;

    @ApiModelProperty(value = "运行参数")
    private JSONObject runParams;

    @ApiModelProperty(value = "算法用途")
    private String algorithmUsage;

    @ApiModelProperty(value = "精度")
    private String accuracy;

    @ApiModelProperty(value = "P4推理速度")
    private Integer p4InferenceSpeed;

    @ApiModelProperty(value = "输出结果（1是，0否）")
    private Boolean isTrainModelOut;

    @ApiModelProperty(value = "输出信息（1是，0否)")
    private Boolean isTrainOut;

    @ApiModelProperty(value = "可视化日志（1是，0否）")
    private Boolean isVisualizedLog;

    @ApiModelProperty(value = "算法是否支持推理（1可推理，0不可推理）")
    private Boolean inference;

    @ApiModelProperty(value = "创建人")
    private Long createUserId;

    @ApiModelProperty(value = "创建时间")
    private Timestamp createTime;

    @ApiModelProperty(value = "更新人")
    private Long updateUserId;

    @ApiModelProperty(value = "更新时间")
    private Timestamp updateTime;

    @ApiModelProperty(value = "资源拥有者ID")
    private Long originUserId;
}
