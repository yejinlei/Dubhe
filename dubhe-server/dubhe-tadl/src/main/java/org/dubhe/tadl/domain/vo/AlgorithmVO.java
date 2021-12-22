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
package org.dubhe.tadl.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.tadl.domain.entity.Algorithm;

import java.io.Serializable;
import java.util.List;


@Data
public class AlgorithmVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("实验名称")
    private String name;
    @ApiModelProperty("模型类别")
    private Integer modelType;

    @ApiModelProperty("算法版本Id")
    private Long algorithmVersionId;

    @ApiModelProperty("算法描述")
    private String description;

    @ApiModelProperty("默认主要指标")
    private String defaultMetric;

    @ApiModelProperty("是否oneShot（0不是，1是）")
    private Boolean oneShot;

    @ApiModelProperty("算法类型")
    private String algType;

    @ApiModelProperty("算法框架")
    private String platform;

    @ApiModelProperty("算法框架版本")
    private String platformVersion;

    @ApiModelProperty("是否支持gpu训练：0支持，1不支持")
    private Boolean gpu;

    @ApiModelProperty("算法版本VO")
    private List<AlgorithmVersionVO> algorithmVersionVOList;

    @ApiModelProperty("算法阶段VO")
    private List<AlgorithmStageVO> stage;

    @ApiModelProperty("yaml")
    private String yaml;

    public static AlgorithmVO from(Algorithm algorithm) {
        return new AlgorithmVO() {{
            setId(algorithm.getId());
            setName(algorithm.getName());
            setAlgorithmVersionId(algorithm.getAlgorithmVersionId());
            setDescription(algorithm.getDescription());
            setModelType(algorithm.getModelType());
            setDefaultMetric(algorithm.getDefaultMetric());
            setOneShot(algorithm.getOneShot());
            setAlgType(algorithm.getAlgorithmType());
            setPlatform(algorithm.getPlatform());
            setPlatformVersion(algorithm.getPlatformVersion());
            setGpu(algorithm.getGpu());
        }};
    }

}
