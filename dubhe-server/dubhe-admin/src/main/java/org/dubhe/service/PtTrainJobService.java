/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

package org.dubhe.service;

import org.dubhe.domain.dto.*;
import org.dubhe.domain.vo.*;

import java.util.List;
import java.util.Map;

/**
 * @description 训练作业job 服务类
 * @date 2020-04-27
 */
public interface PtTrainJobService {


    /**
     * 作业列表展示
     *
     * @param ptTrainQueryDTO 查询作业列表参数
     * @return Map<String, Object>      作业列表分页数据
     **/
    Map<String, Object> getTrainJob(PtTrainQueryDTO ptTrainQueryDTO);


    /**
     * 作业不同版本job列表展示
     *
     * @param ptTrainJobVersionQueryDTO 查询作业不同版本job列表参数
     * @return List<PtTrainJobDetailVO> 训练版本查询详情集合
     **/
    List<PtTrainJobDetailVO> getTrainJobVersion(PtTrainJobVersionQueryDTO ptTrainJobVersionQueryDTO);


    /**
     * 创建训练job
     *
     * @param ptTrainJobCreateDTO 创建训练job参数
     * @return List<Long>           id集合
     */
    List<Long> createTrainJobVersion(PtTrainJobCreateDTO ptTrainJobCreateDTO);


    /**
     * 修改训练job
     *
     * @param ptTrainJobUpdateDTO   修改训练job参数
     * @return List<Long>           id集合
     **/
    List<Long> updateTrainJob(PtTrainJobUpdateDTO ptTrainJobUpdateDTO);


    /**
     * 删除训练job
     *
     * @param ptTrainJobDeleteDTO 删除训练job参数
     * @return PtTrainJobDeleteVO 返回删除训练任务结果
     **/
    PtTrainJobDeleteVO deleteTrainJob(PtTrainJobDeleteDTO ptTrainJobDeleteDTO);


    /**
     * 停止训练job
     *
     * @param ptTrainJobStopDTO 停止训练job参数
     * @return PtTrainJobStopVO 停止训练任务结果
     **/
    PtTrainJobStopVO stopTrainJob(PtTrainJobStopDTO ptTrainJobStopDTO);


    /**
     * 查询训练作业job状态
     *
     * @param ptTrainDataSourceStatusQueryDTO 查询训练作业job状态参数
     * @return HashedMap<String, Boolean>     数据集路径-是否可以删除 的map集合
     **/
    Map<String, Boolean> getTrainDataSourceStatus(PtTrainDataSourceStatusQueryDTO ptTrainDataSourceStatusQueryDTO);


    /**
     * 我的训练任务统计
     *
     * @return PtTrainJobStatisticsMineVO 统计信息
     **/
    PtTrainJobStatisticsMineVO statisticsMine();

    /**
     * 根据jobId查询训练任务详情查询
     *
     * @param ptTrainJobDetailQueryDTO 根据jobId查询训练任务详情查询条件
     * @return PtTrainQueryJobDetailVO 根据jobId查询训练任务详情返回结果
     */
    PtTrainJobDetailQueryVO getTrainJobDetail(PtTrainJobDetailQueryDTO ptTrainJobDetailQueryDTO);

    /**
     * 恢复训练
     *
     * @param ptTrainJobResumeDTO 恢复训练请求参数
     */
    void resumeTrainJob(PtTrainJobResumeDTO ptTrainJobResumeDTO);

    /**
     * 获取job在grafana监控的地址
     *
     * @param jobId                     任务ID
     * @return List<PtJobMetricsGrafanaVO>    Pod Metrics Grafana url
     */
    List<PtJobMetricsGrafanaVO> getGrafanaUrl(Long jobId);
}
