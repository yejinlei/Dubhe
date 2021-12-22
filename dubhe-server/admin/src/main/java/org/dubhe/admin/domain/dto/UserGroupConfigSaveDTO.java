package org.dubhe.admin.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.dto.UserGpuConfigDTO;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @date 2021-11-24
 * @description 用户组用户统一配置DTO
 */
@Data
@Accessors(chain = true)
public class UserGroupConfigSaveDTO {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "用户组ID 不能为空")
    private Long groupId;

    @NotNull(message = "Notebook 延迟删除时间配置不能为空")
    private Integer notebookDelayDeleteTime;

    @NotNull(message = "CPU 资源限制配置不能为空")
    private Integer cpuLimit;

    @NotNull(message = "内存资源限制配置不能为空")
    private Integer memoryLimit;

    private Long defaultImageId;

    private List<UserGpuConfigDTO> gpuResources;
}
