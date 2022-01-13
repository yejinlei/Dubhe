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
package org.dubhe.tadl.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @description
 * @date 2021-04-08
 */
@Data
public class TrialStopParamDTO implements Serializable {

    /**
     * 实验id
     */
    private Long experimentId;
    /**
     * trial id
     */
    private Long trialId;
    /**
     * 阶段id
     */
    private String stageId;
    /**
     * k8s 资源命名空间名称
     */
    private String namespace;
}
