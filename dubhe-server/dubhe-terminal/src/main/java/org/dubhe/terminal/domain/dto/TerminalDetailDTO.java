package org.dubhe.terminal.domain.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;

/**
 * @description 更新连接描述DTO
 * @date 2021-10-28
 */

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class TerminalDetailDTO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @Min(value = MagicNumConstant.ONE, message = "id数值不合法")
    private Long id;

    @ApiModelProperty("描述")
    @Length(max = MagicNumConstant.TWO_HUNDRED, message = "描述内容错误-输入长度不能超过200个字符")
    private String description;

    public TerminalDetailDTO(Long id, String description) {
        this.id = id;
        this.description = description;
    }
}
