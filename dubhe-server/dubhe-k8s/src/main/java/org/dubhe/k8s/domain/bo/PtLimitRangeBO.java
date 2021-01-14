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

package org.dubhe.k8s.domain.bo;

import org.dubhe.k8s.annotation.K8sValidation;
import org.dubhe.k8s.domain.resource.BizLimitRangeItem;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.k8s.enums.ValidationTypeEnum;

import java.util.List;

/**
 * @description LimitRange BO
 * @date 2020-04-23
 */
@Data
@Accessors(chain = true)
public class PtLimitRangeBO {
    @K8sValidation(ValidationTypeEnum.K8S_RESOURCE_NAME)
    private String namespace;
    @K8sValidation(ValidationTypeEnum.K8S_RESOURCE_NAME)
    private String name;
    private List<BizLimitRangeItem> limits;
}
