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

package org.dubhe.train.service;


import org.dubhe.k8s.domain.vo.PodVO;
import org.dubhe.train.domain.dto.PtTrainLogQueryDTO;
import org.dubhe.train.domain.vo.PtTrainLogQueryVO;

import java.util.List;

/**
 * @description 训练日志服务类
 * @date 2020-05-08
 */
public interface PtTrainLogService {

    /**
     * 查询训练任务运行日志
     *
     * @param ptTrainLogQueryDTO  训练日志查询
     * @return PtTrainLogQueryVO   返回训练日志查询
     **/
    PtTrainLogQueryVO queryTrainLog(PtTrainLogQueryDTO ptTrainLogQueryDTO);

    /**
     *
     * 字符串换行
     * @param content  个数
     * @return String  字符串
     */
	String getTrainLogString(List<String> content);

    /**
     * 获取训练任务的Pod
     *
     * @param id 训练作业job表 id
     * @return 训练任务的Pod
     */
    List<PodVO> getPods(Long id);
}
