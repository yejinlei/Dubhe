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

package org.dubhe.serving.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.dubhe.biz.base.dto.PtModelStatusQueryDTO;
import org.dubhe.k8s.domain.vo.PodVO;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.serving.domain.dto.*;
import org.dubhe.serving.domain.entity.BatchServing;
import org.dubhe.serving.domain.vo.*;

import java.util.List;
import java.util.Map;

/**
 * @description 批量服务管理
 * @date 2020-08-26
 */
public interface BatchServingService extends IService<BatchServing> {

    /**
     * 批量服务查询
     *
     * @param batchServingQueryDTO 批量服务查询参数
     * @return Map<String, Object> 批量服务查询返回分页对象
     */
    Map<String, Object> query(BatchServingQueryDTO batchServingQueryDTO);

    /**
     * 创建批量服务
     *
     * @param batchServingCreateDTO 批量服务创建参数
     * @return BatchServingCreateVO 批量服务创建返回结果
     */
    BatchServingCreateVO create(BatchServingCreateDTO batchServingCreateDTO);

    /**
     * 修改批量服务
     *
     * @param batchServingUpdateDTO 批量服务修改参数
     * @return BatchServingUpdateVO 批量服务修改返回结果
     */
    BatchServingUpdateVO update(BatchServingUpdateDTO batchServingUpdateDTO);

    /**
     * 删除批量服务
     *
     * @param batchServingDeleteDTO 批量服务删除参数
     * @return BatchServingDeleteVO 批量服务删除返回结果
     */
    BatchServingDeleteVO delete(BatchServingDeleteDTO batchServingDeleteDTO);

    /**
     * 启动批量服务
     *
     * @param batchServingStartDTO 批量服务启动参数
     * @return BatchServingStartVO 批量服务启动返回结果
     */
    BatchServingStartVO start(BatchServingStartDTO batchServingStartDTO);

    /**
     * 停止批量服务
     *
     * @param batchServingStopDTO 批量服务停止参数
     * @return BatchServingStopVO 批量服务停止返回结果
     */
    BatchServingStopVO stop(BatchServingStopDTO batchServingStopDTO);

    /**
     * 获取批量服务详情
     *
     * @param batchServingDetailDTO 批量服务详情参数
     * @return BatchServingDetailVO 批量服务详情返回结果
     */
    BatchServingDetailVO getDetail(BatchServingDetailDTO batchServingDetailDTO);

    /**
     * k8s回调批量服务状态
     *
     * @param times 回调请求次数
     * @param req 回调请求对象
     * @return boolean 返回回调结果
     */
    boolean batchServingCallback(int times, BatchServingK8sPodCallbackCreateDTO req);

    /**
     * @param id 服务配置id
     * @return List<PodVO> 服务配下的pod信息
     */
    List<PodVO> getPods(Long id);

    /**
     * 轮询返回状态及进度
     * @param id 批量服务id
     * @return BatchServingQueryVO 返回查询结果
     */
    BatchServingQueryVO queryStatusAndProgress(Long id);

    /**
     * 判断模型是否正在使用
     * @param ptModelStatusQueryDTO 模型查询条件
     * @return Boolean 是否在用（true：使用中；false：未使用）
     */
    Boolean getServingModelStatus(PtModelStatusQueryDTO ptModelStatusQueryDTO);

    /**
     * serving批量服务文件回收还原
     *
     * @param dto 还原DTO对象
     */
    void recycleRollback(RecycleCreateDTO dto);
}
