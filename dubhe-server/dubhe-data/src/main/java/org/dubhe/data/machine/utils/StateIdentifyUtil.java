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

package org.dubhe.data.machine.utils;

import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.machine.enums.DataStateEnum;
import org.dubhe.data.machine.utils.identify.data.DataHub;
import org.dubhe.data.machine.utils.identify.setting.StateIdentifySetting;
import org.dubhe.data.machine.utils.identify.setting.StateSelect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * @description 状态判断实现类
 * @date 2020-09-24
 */
@Component
public class StateIdentifyUtil{


    /**
     * 数据查询处理
     */
    @Autowired
    private DataHub dataHub;

    /**
     * 状态判断类
     */
    @Autowired
    private StateSelect stateSelect;

    /**
     * 状态判断中所有的自定义方法数组
     */
    private final Method[] method = ReflectionUtils.getDeclaredMethods(StateSelect.class);

    /**
     * 获取数据集状态(指定版本)
     *
     * @param datasetId               数据集id
     * @param versionName             数据集版本名称
     * @param needFileStateDoIdentify 是否需要查询文件状态判断
     * @return DatasetStatusEnum      数据集状态(指定版本)
     */
    public DataStateEnum getStatus(Long datasetId, String versionName, boolean needFileStateDoIdentify) {
        return needFileStateDoIdentify ? new IdentifyDatasetStateByFileState(datasetId, versionName, StateIdentifySetting.NEED_FILE_STATE_DO_IDENTIFY)
                .getStatus() : dataHub.getDatasetStatus(datasetId);
    }

    /**
     * 获取数据集状态(未指定版本)
     *
     * @param dataset                 数据集
     * @param needFileStateDoIdentify 是否需要查询文件状态判断
     * @return DatasetStatusEnum      数据集状态(指定版本)
     */
    public DataStateEnum getStatus(Dataset dataset, boolean needFileStateDoIdentify) {
        return needFileStateDoIdentify ? new IdentifyDatasetStateByFileState(dataset.getId(), dataset.getCurrentVersionName(), StateIdentifySetting.NEED_FILE_STATE_DO_IDENTIFY)
                .getStatus() : dataHub.getDatasetStatus(dataset.getId());
    }

    /**
     * 获取数据集状态(自动标注/目标跟踪回滚使用)
     *
     * @param datasetId   数据集id
     * @param versionName 数据集版本名称
     * @return DatasetStatusEnum    数据集状态(指定版本)
     */
    public DataStateEnum getStatusForRollback(Long datasetId, String versionName) {
        return new IdentifyDatasetStateByFileState(datasetId, versionName, StateIdentifySetting.ROLL_BACK_FOR_STATE).getStatus();
    }


    class IdentifyDatasetStateByFileState {

        /**
         * 判断得到的数据集状态
         */
        public DataStateEnum state;

        /**
         * 会查询文件的状态去对数据集的状态做判断
         *
         * @param datasetId   数据集ID
         * @param versionName 数据集版本名称
         */
        public IdentifyDatasetStateByFileState(Long datasetId, String versionName, Set<DataStateEnum> dataStateEnums) {
            state = dataHub.getDatasetStatus(datasetId);
            if (dataStateEnums.contains(state)) {
                List<Integer> stateList = dataHub.getFileStatusListByDatasetAndVersion(datasetId, versionName);
                if (stateList == null || stateList.isEmpty()) {
                    state = DataStateEnum.NOT_ANNOTATION_STATE;
                    return;
                }
                for (Method stateSelectMethod : method) {
                    state = (DataStateEnum) ReflectionUtils.invokeMethod(stateSelectMethod, stateSelect, new Object[]{stateList});
                    if (state != null) {
                        return;
                    }
                }
            }
        }

        DataStateEnum getStatus() {
            return this.state;
        }
    }
}