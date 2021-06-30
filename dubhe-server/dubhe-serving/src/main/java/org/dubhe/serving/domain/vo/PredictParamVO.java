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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @description 预测参数返回
 * @date 2020-08-28
 */
@Data
public class PredictParamVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "请求方式")
    private String requestMethod;

    @ApiModelProperty(value = "接口url")
    private String url;

    @ApiModelProperty(value = "输入参数<参数名称，类型>")
    private Map<String, String> inputs;

    @ApiModelProperty(value = "输出参数<参数名称，类型>")
    private Map<String, String> outputs;

    @ApiModelProperty(value = "补充参数类型<参数名称，参数类型>")
    private Map<String, Map<String, String>> other;
}
