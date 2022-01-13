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
package org.dubhe.tadl.machine.utils.identify.setting;

import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.tadl.enums.TrialStatusEnum;
import org.dubhe.tadl.enums.ExperimentStageStateEnum;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description 状态判断类
 * @date 2020-09-24
 */
@Component
public class ExperimentStageStateSelect {

    /**
     * 已完成
     *
     * @param stateList     实验阶段状态的并集
     * @return              实验状态枚举
     */
    public ExperimentStageStateEnum isFinished(List<Integer> stateList) {
        return stateList.contains(TrialStatusEnum.FINISHED.getVal())&&stateList.size()==NumberConstant.NUMBER_1 ?
                ExperimentStageStateEnum.FINISHED_EXPERIMENT_STAGE_STATE : null;
    }

}