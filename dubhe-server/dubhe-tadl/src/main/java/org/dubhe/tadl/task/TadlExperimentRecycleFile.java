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
package org.dubhe.tadl.task;

import cn.hutool.core.util.StrUtil;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.dto.RecycleDetailCreateDTO;
import org.dubhe.recycle.global.AbstractGlobalRecycle;
import org.dubhe.recycle.utils.RecycleTool;
import org.dubhe.tadl.service.ExperimentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@RefreshScope
@Component(value = "tadlExperimentRecycleFile")
public class TadlExperimentRecycleFile extends AbstractGlobalRecycle {
    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private RecycleTool recycleTool;

    @Override
    protected boolean clearDetail(RecycleDetailCreateDTO detail, RecycleCreateDTO dto) throws Exception {
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

    @Override
    protected void rollback(RecycleCreateDTO dto) {
        if (Objects.isNull(dto)||Objects.isNull(dto.getRemark())){
            LogUtil.error(LogEnum.TADL,"实验文件恢复异常");
            return;
        }
        //实验文件恢复
        experimentService.updateExperimentDeletedById(Long.valueOf(dto.getRemark()),Boolean.FALSE);
    }
}
