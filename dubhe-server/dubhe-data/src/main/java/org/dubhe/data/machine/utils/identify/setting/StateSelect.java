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

import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.data.machine.constant.FileStateCodeConstant;
import org.dubhe.data.machine.enums.DataStateEnum;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

/**
 * @description 状态判断类
 * @date 2020-09-24
 */
@Component
public class StateSelect {

    /**
     * 未采样
     *
     * @param stateList     数据集下文件状态的并集
     * @return              数据集状态枚举
     */
    public DataStateEnum isInit(List<Integer> stateList) {
        if (stateList.size() == NumberConstant.NUMBER_1 && stateList.contains(FileStateCodeConstant.NOT_ANNOTATION_FILE_STATE)){
            return DataStateEnum.NOT_ANNOTATION_STATE;
        }
        return null;
    }

    /**
     * 手动标注中
     *
     * @param stateList     数据集下文件状态的并集
     * @return              数据集状态枚举
     */
    public DataStateEnum isManualAnnotating(List<Integer> stateList) {
        if (stateList.size() > 1 && stateList.contains(FileStateCodeConstant.NOT_ANNOTATION_FILE_STATE)) {
            return DataStateEnum.MANUAL_ANNOTATION_STATE;
        }
        return stateList.contains(FileStateCodeConstant.MANUAL_ANNOTATION_FILE_STATE) ? DataStateEnum.MANUAL_ANNOTATION_STATE : null;
    }

    /**
     * 自动标注完成
     *
     * @param stateList     数据集下文件状态的并集
     * @return              数据集状态枚举
     */
    public DataStateEnum isAutoFinished(List<Integer> stateList) {
        HashSet<Integer> states = new HashSet<Integer>() {{
            add(FileStateCodeConstant.AUTO_TAG_COMPLETE_FILE_STATE);
            add(FileStateCodeConstant.ANNOTATION_NOT_DISTINGUISH_FILE_STATE);
            add(FileStateCodeConstant.ANNOTATION_COMPLETE_FILE_STATE);
            add(FileStateCodeConstant.TARGET_COMPLETE_FILE_STATE);
        }};
        switch (stateList.size()) {
            case NumberConstant.NUMBER_1:
                if (stateList.contains(FileStateCodeConstant.AUTO_TAG_COMPLETE_FILE_STATE)||
                        stateList.contains(FileStateCodeConstant.ANNOTATION_NOT_DISTINGUISH_FILE_STATE)){
                    return DataStateEnum.AUTO_TAG_COMPLETE_STATE;
                }
                return null;
            case NumberConstant.NUMBER_2:
            case NumberConstant.NUMBER_3:
            case NumberConstant.NUMBER_4:
                for (Integer fileState : stateList) {
                    if (!states.contains(fileState)) {
                        return null;
                    };
                }
                return DataStateEnum.AUTO_TAG_COMPLETE_STATE;
            default:
                return null;
        }
    }

    /**
     * 标注完成
     *
     * @param stateList     数据集下文件状态的并集
     * @return              数据集状态枚举
     */
    public DataStateEnum isFinished(List<Integer> stateList) {
        return stateList.contains(FileStateCodeConstant.ANNOTATION_COMPLETE_FILE_STATE)&&stateList.size()==NumberConstant.NUMBER_1 ?
                DataStateEnum.ANNOTATION_COMPLETE_STATE : null;
    }

    /**
     * 目标跟踪完成
     *
     * @param stateList     数据集下文件状态的并集
     * @return              数据集状态枚举
     */
    public DataStateEnum isFinishedTrack(List<Integer> stateList) {
        if (stateList.size() > NumberConstant.NUMBER_1 && !stateList.contains(FileStateCodeConstant.ANNOTATION_COMPLETE_FILE_STATE)) {
            return null;
        }
        return stateList.contains(FileStateCodeConstant.TARGET_COMPLETE_FILE_STATE)?DataStateEnum.TARGET_COMPLETE_STATE:null;
    }

}