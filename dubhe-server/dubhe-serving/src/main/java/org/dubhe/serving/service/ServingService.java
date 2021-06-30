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

import org.dubhe.biz.base.dto.PtModelStatusQueryDTO;
import org.dubhe.k8s.domain.vo.PodVO;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.serving.domain.dto.PredictParamDTO;
import org.dubhe.serving.domain.dto.ServingInfoCreateDTO;
import org.dubhe.serving.domain.dto.ServingInfoDeleteDTO;
import org.dubhe.serving.domain.dto.ServingInfoDetailDTO;
import org.dubhe.serving.domain.dto.ServingInfoQueryDTO;
import org.dubhe.serving.domain.dto.ServingInfoUpdateDTO;
import org.dubhe.serving.domain.dto.ServingK8sDeploymentCallbackCreateDTO;
import org.dubhe.serving.domain.dto.ServingK8sPodCallbackCreateDTO;
import org.dubhe.serving.domain.dto.ServingStartDTO;
import org.dubhe.serving.domain.dto.ServingStopDTO;
import org.dubhe.serving.domain.vo.PredictParamVO;
import org.dubhe.serving.domain.vo.ServingInfoCreateVO;
import org.dubhe.serving.domain.vo.ServingInfoDeleteVO;
import org.dubhe.serving.domain.vo.ServingInfoDetailVO;
import org.dubhe.serving.domain.vo.ServingInfoUpdateVO;
import org.dubhe.serving.domain.vo.ServingMetricsVO;
import org.dubhe.serving.domain.vo.ServingModelConfigVO;
import org.dubhe.serving.domain.vo.ServingStartVO;
import org.dubhe.serving.domain.vo.ServingStopVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @description 云端服务管理
 * @date 2020-08-25
 */
public interface ServingService {
    /**
     * 查询分页数据
     *
     * @param servingInfoQueryDTO 服务查询参数
     * @return Map<String, Object> 云端服务分页对象
     */
    Map<String, Object> query(ServingInfoQueryDTO servingInfoQueryDTO);

    /**
     * 创建服务
     *
     * @param servingInfoCreateDTO 服务创建参数
     * @return ServingInfoCreateVO 服务创建返回对象
     */
    ServingInfoCreateVO create(ServingInfoCreateDTO servingInfoCreateDTO);

    /**
     * 修改服务
     *
     * @param servingInfoUpdateDTO 服务对象修改
     * @return ServingInfoUpdateVO 服务修改返回对象
     */
    ServingInfoUpdateVO update(ServingInfoUpdateDTO servingInfoUpdateDTO);

    /**
     * 删除服务
     *
     * @param servingInfoDeleteDTO 服务对象删除
     * @return ServingInfoDeleteVO 服务删除返回对象
     */
    ServingInfoDeleteVO delete(ServingInfoDeleteDTO servingInfoDeleteDTO);

    /**
     * 获取服务详情
     *
     * @param servingInfoDetailDTO 获取服务详情参数
     * @return ServingInfoDetailVO 服务详情返回对象
     */
    ServingInfoDetailVO getDetail(ServingInfoDetailDTO servingInfoDetailDTO);

    /**
     * 启动服务
     *
     * @param servingStartDTO 启动服务参数
     * @return ServingStartVO 启动服务返回对象
     */
    ServingStartVO start(ServingStartDTO servingStartDTO);

    /**
     * 预测
     *
     * @param id    预测服务ID
     * @param url   预测地址
     * @param files 需要预测的图片文件
     * @return ServingPredictVO 预测返回对象
     */
    String predict(Long id, String url, MultipartFile[] files);

    /**
     * 停止服务
     *
     * @param servingStopDTO 停止服务参数
     * @return ServingStopVO 停止服务返回对象
     */
    ServingStopVO stop(ServingStopDTO servingStopDTO);

    /**
     * 获取预测参数
     *
     * @param predictParamDTO 获取预测参数服务
     * @return PredictParamVO 预测参数返回对象
     */
    PredictParamVO getPredictParam(PredictParamDTO predictParamDTO);

    /**
     * 发送路由更新消息
     *
     * @param saveIdList   新增的路由ID列表
     * @param deleteIdList 删除的路由ID列表
     */
    void notifyUpdateServingRoute(List<Long> saveIdList, List<Long> deleteIdList);

    /**
     * 获取modelConfigId下pod信息
     *
     * @param id 服务配置id
     * @return 服务配下的pod信息
     */
    List<PodVO> getPods(Long id);

    /**
     * 获取在线服务的监控信息
     *
     * @param id 模型部署信息id
     * @return ServingMetricsVO 返回监控信息对象
     */
    ServingMetricsVO getMetricsDetail(Long id);

    /**
     * k8s回调deployment在线服务状态
     *
     * @param req   回调请求对象
     * @return boolean 返回是否回调成功
     */
    boolean servingDeploymentCallback(ServingK8sDeploymentCallbackCreateDTO req);

    /**
     * k8s回调pod在线服务状态
     *
     * @param times 回调请求次数
     * @param req  回调请求对象
     * @return boolean 返回是否回调成功
     */
    boolean servingPodCallback(int times, ServingK8sPodCallbackCreateDTO req);
    /**
     * 获取在线服务回滚信息列表
     *
     * @param servingId 在线服务id
     * @return Map<String, List < ServingModelConfigVO>> 返回回滚信息列表
     */
    Map<String, List<ServingModelConfigVO>> getRollbackList(Long servingId);

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
