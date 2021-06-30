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
package org.dubhe.model.service.RecycleFileService;

import org.dubhe.model.service.PtModelBranchService;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.dto.RecycleDetailCreateDTO;
import org.dubhe.recycle.global.AbstractGlobalRecycle;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description 模型文件自定义还原
 * @date 2021-03-23
 */
@RefreshScope
@Component(value = "modelRecycleFile")
public class ModelRecycleFileServiceImpl extends AbstractGlobalRecycle {

    /**
     * 模型版本service
     */
    @Resource
    private PtModelBranchService ptModelBranchService;

    /**
     *此方法不用，模型文件使用回收默认方法
     * @param detail 数据清理详情参数
     * @param dto 资源回收创建对象
     * @return
     * @throws Exception
     */
    @Override
    protected boolean clearDetail(RecycleDetailCreateDTO detail, RecycleCreateDTO dto) throws Exception {
        return false;
    }

    /**
     *
     * @param dto 数据还原参数
     */
    @Override
    protected void rollback(RecycleCreateDTO dto) {
        ptModelBranchService.modelRecycleFileRollback(dto);
    }
}