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
package org.dubhe.serving.task;

import cn.hutool.core.util.StrUtil;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.dto.RecycleDetailCreateDTO;
import org.dubhe.recycle.global.AbstractGlobalRecycle;
import org.dubhe.recycle.utils.RecycleTool;
import org.dubhe.serving.service.BatchServingService;
import org.dubhe.serving.service.ServingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description serving在线服务文件资源回收/还原
 * @date 2021-04-07
 */
@RefreshScope
@Component(value = "servingRecycleFile")
public class ServingRecycleFile extends AbstractGlobalRecycle {

    @Autowired
    private RecycleTool recycleTool;

    @Autowired
    private ServingService servingService;

    /**
     * 自定义回收serving文件实现
     *
     * @param detail 数据清理详情参数
     * @param dto 资源回收创建对象
     * @return true 继续执行,false 中断任务详情回收(本次无法执行完毕，创建新任务到下次执行)
     */
    @Override
    protected boolean clearDetail(RecycleDetailCreateDTO detail, RecycleCreateDTO dto) throws Exception {
        //清理serving回收文件
        if (StrUtil.isBlank(detail.getRecycleCondition())) {
            throw new BusinessException("回收条件不能为空！");
        }
        if (detail.getRecycleCondition().contains(StrUtil.COMMA)) {
            List<String> recycleUrls = StrUtil.split(detail.getRecycleCondition(), ',');
            recycleUrls.forEach(url ->
                    recycleTool.delTempInvalidResources(url)
            );
        } else {
            recycleTool.delTempInvalidResources(detail.getRecycleCondition());
        }
        return true;
    }

    /**
     * 数据还原
     *
     * @param dto 数据清理参数
     */
    @Override
    protected void rollback(RecycleCreateDTO dto) {
        servingService.recycleRollback(dto);
    }
}
