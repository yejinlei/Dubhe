package org.dubhe.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @description 模型优化模块我的数据集创建参数
 * @date 2021-01-06
 */
@Data
public class ModelOptDatasetCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "名称", required = true)
    @NotEmpty(message = "数据集名称不能为空")
    private String name;

    @ApiModelProperty(value = "路径", required = true)
    @NotEmpty(message = "数据集路径不能为空")
    private String path;
}
