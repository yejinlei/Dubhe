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

import org.dubhe.tadl.enums.ExperimentStatusEnum;
import org.dubhe.tadl.enums.ExperimentStageStateEnum;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * @description 状态判断设置类
 * @date 2020-09-24
 */
@Component
public class StateIdentifySetting {

    /**
     * 实验状态需要使用阶段状态去判断的
     */
    public static final Set<ExperimentStatusEnum> NEED_EXPERIMENT_STAGE_STATE_DO_IDENTIFY = new HashSet<ExperimentStatusEnum>() {{
        add(ExperimentStatusEnum.FINISHED_EXPERIMENT_STATE);
    }};

    /**
     * 实验阶段状态需要使用阶段状态去判断的
     */
    public static final Set<ExperimentStageStateEnum> NEED_TRIAL_STATE_DO_IDENTIFY = new HashSet<ExperimentStageStateEnum>() {{
        add(ExperimentStageStateEnum.FINISHED_EXPERIMENT_STAGE_STATE);
    }};

}
