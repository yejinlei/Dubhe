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

package org.dubhe.data.machine.utils.identify.setting;

import org.dubhe.data.machine.enums.DataStateEnum;
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
     * 回退状态（自动标注失败/目标跟踪失败）使用
     */
    public static final Set<DataStateEnum> ROLL_BACK_FOR_STATE = new HashSet<DataStateEnum>() {{
        //自动标注中
        add(DataStateEnum.AUTOMATIC_LABELING_STATE);
        //目标跟踪中
        add(DataStateEnum.TARGET_FOLLOW_STATE);
        //增强中
        add(DataStateEnum.STRENGTHENING_STATE);
    }};


    /**
     * 数据集状态需要使用文件状态去判断的
     */
    public static final Set<DataStateEnum> NEED_FILE_STATE_DO_IDENTIFY = new HashSet<DataStateEnum>() {{
        //未标注
        add(DataStateEnum.NOT_ANNOTATION_STATE);
        //手动标注中
        add(DataStateEnum.MANUAL_ANNOTATION_STATE);
        //自动标注完成
        add(DataStateEnum.AUTO_TAG_COMPLETE_STATE);
        //标注完成
        add(DataStateEnum.ANNOTATION_COMPLETE_STATE);
        //目标跟踪完成
        add(DataStateEnum.TARGET_COMPLETE_STATE);
        //导入中
        add(DataStateEnum.IN_THE_IMPORT_STATE);
        //采样中
        add(DataStateEnum.SAMPLING_STATE);
    }};

}
