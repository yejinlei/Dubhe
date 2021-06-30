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

package org.dubhe.optimize.service;

import org.dubhe.biz.base.dto.PtModelStatusQueryDTO;
import org.dubhe.optimize.domain.dto.ModelOptTaskInstanceCancelDTO;
import org.dubhe.optimize.domain.dto.ModelOptTaskInstanceDeleteDTO;
import org.dubhe.optimize.domain.dto.ModelOptTaskInstanceDetailDTO;
import org.dubhe.optimize.domain.dto.ModelOptTaskInstanceQueryDTO;
import org.dubhe.optimize.domain.dto.ModelOptTaskInstanceResubmitDTO;
import org.dubhe.optimize.domain.dto.callback.ModelOptK8sPodCallbackCreateDTO;
import org.dubhe.optimize.domain.entity.ModelOptTaskInstance;
import org.dubhe.optimize.domain.vo.ModelOptTaskInstanceQueryVO;

import java.util.Map;

/**
 * @description 模型优化任务实例
 * @date 2020-05-22
 */
public interface ModelOptTaskInstanceService {
    /**
     * 分页查询任务执行记录实例列表
     *
     * @param instanceQueryDTO 模型优化实例查询参数包装类
     * @return Map<String, Object> 分页对象
     */
    Map<String, Object> queryAll(ModelOptTaskInstanceQueryDTO instanceQueryDTO);

    /**
     * 新增任务实例
     *
     * @param modelOptTaskInstance 模型优化实例对象
     */
    void create(ModelOptTaskInstance modelOptTaskInstance);

    /**
     * 重新提交任务实例
     *
     * @param resubmitDTO 重新提交任务实例参数
     */
    void resubmit(ModelOptTaskInstanceResubmitDTO resubmitDTO);

    /**
     * 取消模型优化任务
     *
     * @param cancelDTO 取消模型优化任务参数
     */
    void cancel(ModelOptTaskInstanceCancelDTO cancelDTO);

    /**
     * 查看单个任务实例详情
     *
     * @param detailDTO 查看任务实例详情参数
     * @return ModelOptTaskInstanceVO 任务实例对象
     */
    ModelOptTaskInstanceQueryVO getInstDetail(ModelOptTaskInstanceDetailDTO detailDTO);

    /**
     * k8s回调模型优化方法
     *
     * @param req 模型优化自定义回调参数类
     * @return 返回回调状态
     */
    boolean modelOptCallBack(ModelOptK8sPodCallbackCreateDTO req);

    /**
     * 查询K8S并同步相关实例状态
     */
    void syncInstanceStatus();

    /**
     * 校验该任务是否存在进行中和等待中的实例
     *
     * @param taskId   任务ID
     * @return Boolean 是否存在进行中和等待中的实例
     */
    Boolean checkUnfinishedInst(Long taskId);

    /**
     * 根据任务id删除实例
     *
     * @param taskId 任务id
     * @return Integer 删除实例数量
     */
    int deleteByTaskId(Long taskId);

    /**
     * 删除任务实例
     *
     * @param modelOptTaskInstanceDeleteDTO
     */
    void delete(ModelOptTaskInstanceDeleteDTO modelOptTaskInstanceDeleteDTO);

    /**
     *  获取模型是否在使用
     * @param ptModelStatusQueryDTO 查询模型状态DTO
     * @return Boolean 是否在用（true：使用中；false：未使用）
     */
    Boolean getOptimizeModelStatus(PtModelStatusQueryDTO ptModelStatusQueryDTO);
}
