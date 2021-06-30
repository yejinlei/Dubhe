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

package org.dubhe.k8s.domain.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.resource.BizDeployment;
import org.dubhe.k8s.domain.resource.BizIngress;
import org.dubhe.k8s.domain.resource.BizSecret;
import org.dubhe.k8s.domain.resource.BizService;

/**
 * @description 模型部署VO
 * @date 2020-09-09
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ModelServingVO extends PtBaseResult<ModelServingVO> {
    private BizSecret bizSecret;
    private BizService bizService;
    private BizDeployment bizDeployment;
    private BizIngress bizIngress;

    public ModelServingVO(BizSecret bizSecret, BizService bizService, BizDeployment bizDeployment, BizIngress bizIngress){
        this.bizSecret = bizSecret;
        this.bizService = bizService;
        this.bizDeployment = bizDeployment;
        this.bizIngress = bizIngress;
    }
}
