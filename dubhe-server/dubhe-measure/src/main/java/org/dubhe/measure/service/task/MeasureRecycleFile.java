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
package org.dubhe.measure.service.task;

import cn.hutool.core.util.StrUtil;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.measure.service.PtMeasureService;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.dto.RecycleDetailCreateDTO;
import org.dubhe.recycle.global.AbstractGlobalRecycle;
import org.dubhe.recycle.utils.RecycleTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @description 度量文件资源回收/还原
 * @date 2021-03-23
 */
@RefreshScope
@Component(value = "measureRecycleFile")
public class MeasureRecycleFile extends AbstractGlobalRecycle {


    @Autowired
    private PtMeasureService ptMeasureService;

    @Autowired
    private RecycleTool recycleTool;

    /**
     * 自定义回收度量文件实现
     *
     * @param detail 数据清理详情参数
     * @param dto 资源回收创建对象
     * @return true 继续执行,false 中断任务详情回收(本次无法执行完毕，创建新任务到下次执行)
     */
    @Override
    protected boolean clearDetail(RecycleDetailCreateDTO detail, RecycleCreateDTO dto) {
        //清理度量文件
        if (StrUtil.isBlank(detail.getRecycleCondition())) {
            throw new BusinessException("回收条件不能为空！");
        }
        recycleTool.delTempInvalidResources(detail.getRecycleCondition());
        return true;
    }

    /**
     * 数据还原
     *
     * @param dto 数据清理参数
     */
    @Override
    protected void rollback(RecycleCreateDTO dto) {
        ptMeasureService.recycleRollback(dto);
    }
}
