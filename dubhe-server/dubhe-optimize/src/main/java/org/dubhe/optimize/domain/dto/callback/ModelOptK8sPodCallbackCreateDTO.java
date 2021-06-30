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

package org.dubhe.optimize.domain.dto.callback;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.dubhe.k8s.domain.dto.BaseK8sPodCallbackCreateDTO;

/**
 * @description 模型优化自定义回调参数类
 * @date 2020-06-03
 */
@ApiModel(description = "k8s pod异步回调Algorithm")
@Data
public class ModelOptK8sPodCallbackCreateDTO extends BaseK8sPodCallbackCreateDTO {
    @Override
    public String toString() {
        return super.toString();
    }
}
