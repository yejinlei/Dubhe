package org.dubhe.serving.domain.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.dubhe.k8s.domain.dto.BaseK8sPodCallbackCreateDTO;

/**
 * @description k8s pod异步回调云端serving
 * @date 2021-06-24
 */
@ApiModel(description = "k8s pod异步回调云端serving")
@Data
public class ServingK8sPodCallbackCreateDTO extends BaseK8sPodCallbackCreateDTO {

    @Override
    public String toString() {
        return super.toString();
    }
}
