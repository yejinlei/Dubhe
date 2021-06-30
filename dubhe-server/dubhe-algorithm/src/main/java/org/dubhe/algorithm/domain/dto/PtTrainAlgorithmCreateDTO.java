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

package org.dubhe.algorithm.domain.dto;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.algorithm.utils.TrainUtil;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @desciption 创建算法条件
 * @date 2020-04-29
 */
@Data
@Accessors(chain = true)
public class PtTrainAlgorithmCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "算法名称，输入长度不能超过32个字符", required = true)
    @NotBlank(message = "算法名称不能为空")
    @Length(max = MagicNumConstant.THIRTY_TWO, message = "算法名称-输入长度不能超过32个字符")
    @Pattern(regexp = TrainUtil.REGEXP, message = "算法名称支持字母、数字、汉字、英文横杠和下划线")
    private String algorithmName;

    @ApiModelProperty("算法描述，输入长度不能超过256个字符")
    @Length(max = MagicNumConstant.INTEGER_TWO_HUNDRED_AND_FIFTY_FIVE, message = "算法描述-输入长度不能超过256个字符")
    private String description;

    @ApiModelProperty(value = "镜像版本")
    private String imageTag;

    @ApiModelProperty(value = "镜像名称")
    private String imageName;

    @ApiModelProperty(value = "创建算法来源(true:由fork创建算法，false：其它创建算法方式，null:默认false)")
    private Boolean fork;

    @ApiModelProperty(value = "上传算法是否支持推理(true:可推理，false：不可推理，null:默认false)")
    private Boolean inference;

    @ApiModelProperty(value = "代码目录（路径规则：/algorithm-manage/{userId}/{YYYYMMDDhhmmssSSS+四位随机数}/用户上传的算法具体文件(zip文件）名称或从notebook跳转时为/notebook/{userId}/{YYYYMMDDhhmmssSSS+四位随机数}/）", required = true)
    @NotBlank(message = "代码目录不能为空")
    @Length(max = MagicNumConstant.ONE_HUNDRED_TWENTY_EIGHT, message = "代码目录-输入长度不能超过128个字符")
    private String codeDir;

    @ApiModelProperty(value = "运行命令,管理员使用")
    @Length(max = MagicNumConstant.ONE_HUNDRED_TWENTY_EIGHT, message = "运行命令-输入长度不能超过128个字符")
    private String runCommand;

    @ApiModelProperty("运行参数(算法来源为我的算法时为调优参数，算法来源为预置算法时为运行参数)，管理员使用")
    private JSONObject runParams;

    @ApiModelProperty("算法来源(1为我的算法，2为预置算法)，管理员使用")
    private Integer algorithmSource;

    @ApiModelProperty("算法用途，输入长度不能超过128个字符")
    @Length(max = MagicNumConstant.ONE_HUNDRED_TWENTY_EIGHT, message = "算法用途-输入长度不能超过128个字符")
    private String algorithmUsage;

    @ApiModelProperty("是否输出训练结果，不填则默认为true")
    private Boolean isTrainModelOut;

    @ApiModelProperty("是否输出训练信息，不填则默认为true")
    private Boolean isTrainOut;

    @ApiModelProperty("是否输出可视化日志，不填则默认为false")
    private Boolean isVisualizedLog;

    @ApiModelProperty("noteBookId")
    private Long noteBookId;

    @ApiModelProperty("资源拥有者ID")
    private Long originUserId;

}
