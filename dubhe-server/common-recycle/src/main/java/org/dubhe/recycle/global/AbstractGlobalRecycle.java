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
package org.dubhe.recycle.global;

import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.dto.RecycleDetailCreateDTO;
import org.dubhe.recycle.domain.entity.Recycle;
import org.dubhe.recycle.domain.entity.RecycleDetail;
import org.dubhe.recycle.enums.RecycleStatusEnum;
import org.dubhe.recycle.service.CustomRecycleService;
import org.dubhe.recycle.service.RecycleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @description 数据集清理回收类
 * @date 2020-10-09
 */
public abstract class AbstractGlobalRecycle implements CustomRecycleService {

    /**
     * 回收服务
     */
    @Autowired
    private RecycleService recycleService;

    /**
     * 回收线程初始时间
     */
    private final ThreadLocal<Long> startTimeLocal = new ThreadLocal<>();

    /**
     * 重写自定义回收入口
     * 注意：
     *  1:异步执行避免服务调用者等待
     *  2:不开启事务，避免大事务，且可以小事务分批执行
     * @param dto 资源回收创建对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recycle(RecycleCreateDTO dto) {
        if (dto == null || dto.getId() == null){
            // 非法入参
            return;
        }
        long userId = dto.getUpdateUserId();
        try {
            // 回收前置处理
            // 设置 线程回收开始时间
            startTimeLocal.set(System.currentTimeMillis());
            Recycle recycle = getRecycle(dto);
            // 标记执行中
            recycleService.updateRecycle(recycle, RecycleStatusEnum.DOING,null,userId);
            try {
                // 回收业务详情
                clear(dto);
                // 回收成功
                recycleService.updateRecycle(recycle, RecycleStatusEnum.SUCCEEDED,null,userId);
            } catch (Exception e) {
                // 回收失败
                LogUtil.error(LogEnum.GARBAGE_RECYCLE, "custom recycle {} error {},", recycle.getId(), e);
                recycleService.updateRecycle(recycle, RecycleStatusEnum.FAILED,e.getMessage(),userId);
            }
        }finally {
            // 清除 线程回收开始时间
            startTimeLocal.remove();
        }
    }

    /**
     * 清理回收详情
     * @param dto 资源回收创建对象
     */
    private void clear(RecycleCreateDTO dto){
        long userId = dto.getUpdateUserId();
        for (RecycleDetailCreateDTO detail:dto.getDetailList()) {
            RecycleDetail recycleDetail = getRecycleDetail(detail);
            if (RecycleStatusEnum.SUCCEEDED.getCode().equals(recycleDetail.getRecycleStatus())){
                // 跳过已删除成功任务详情
                continue;
            }
            recycleService.updateRecycleDetail(recycleDetail, RecycleStatusEnum.DOING,null,userId);
            try {
                boolean continued = clearDetail(detail, dto);
                recycleService.updateRecycleDetail(recycleDetail, RecycleStatusEnum.SUCCEEDED, null, userId);
                if (!continued){
                    // 中断任务详情回收
                    break;
                }
            }catch (Exception e) {
                LogUtil.error(LogEnum.GARBAGE_RECYCLE, "custom recycle detail{} error {},", recycleDetail.getId(), e);
                recycleService.updateRecycleDetail(recycleDetail, RecycleStatusEnum.FAILED,e.getMessage(),userId);
            }
            // 同步最新状态，以备this.addNewRecycleTask
            detail.setRecycleStatus(recycleDetail.getRecycleStatus());
        }
    }

    /**
     * 单个回收详情清理
     *
     * @param detail 数据清理详情参数
     * @param dto 资源回收创建对象
     * @return  true 继续执行,false 中断任务详情回收(本次无法执行完毕，创建新任务到下次执行)
     */
    protected abstract boolean clearDetail(RecycleDetailCreateDTO detail,RecycleCreateDTO dto) throws Exception;

    /**
     * 获取Recycle
     * @param recycleCreateDTO 数据清理参数
     * @return Recycle
     */
    private Recycle getRecycle(RecycleCreateDTO recycleCreateDTO){
        Recycle recycle = new Recycle();
        BeanUtils.copyProperties(recycleCreateDTO, recycle);
        return recycle;
    }

    /**
     * 获取RecycleDetail
     * @param recycleDetailCreateDTO 数据清理详情参数
     * @return RecycleDetail
     */
    private RecycleDetail getRecycleDetail(RecycleDetailCreateDTO recycleDetailCreateDTO){
        RecycleDetail recycleDetail = new RecycleDetail();
        BeanUtils.copyProperties(recycleDetailCreateDTO, recycleDetail);
        return recycleDetail;
    }


    /**
     * 超时校验
     *
     * @return true: 未超时，可继续资源回收 false: 已超时
     */
    protected boolean validateOverTime() {
        return (System.currentTimeMillis() - startTimeLocal.get()) / NumberConstant.NUMBER_1000 < getRecycleOverSecond();
    }


    /**
     * 初始化开始时间
     *
     */
    protected void initOverTime() {
        startTimeLocal.set(System.currentTimeMillis());
    }

    /**
     * 新增回收任务
     *
     * @param dto  数据清理参数
     */
    protected void addNewRecycleTask(RecycleCreateDTO dto) {
        if (Objects.nonNull(dto)) {
            recycleService.createRecycleTask(dto);
        }
    }

    /**
     * 重写自定义还原入口
     *
     * @param dto
     */
    @Override
    public void restore(RecycleCreateDTO dto){
        if (dto == null || dto.getId() == null){
            // 非法入参
            return;
        }
        long userId = dto.getUpdateUserId();
        Recycle recycle = getRecycle(dto);
        // 标记还原中
        recycleService.updateRecycle(recycle, RecycleStatusEnum.RESTORING,null,userId);
        try {
            // 业务回收
            rollback(dto);
            // 还原成功
            recycleService.updateRecycle(recycle, RecycleStatusEnum.RESTORED,null,userId);
        } catch (Exception e) {
            // 还原失败，回归初始状态
            recycleService.updateRecycle(recycle, RecycleStatusEnum.PENDING, e.getMessage(),userId);
            // 抛出异常
            throw e;
        }
    }

    /**
     * 业务数据还原
     *
     * @param dto 数据还原参数
     */
    protected abstract void rollback(RecycleCreateDTO dto);

}
