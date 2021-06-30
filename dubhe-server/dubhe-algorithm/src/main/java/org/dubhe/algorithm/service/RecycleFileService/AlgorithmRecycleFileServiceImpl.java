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
package org.dubhe.algorithm.service.RecycleFileService;

import org.dubhe.algorithm.service.PtTrainAlgorithmService;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.dto.RecycleDetailCreateDTO;
import org.dubhe.recycle.global.AbstractGlobalRecycle;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description 算法文件自定义还原
 * @date 2021-03-22
 */
@RefreshScope
@Component(value = "algorithmRecycleFile")
public class AlgorithmRecycleFileServiceImpl extends AbstractGlobalRecycle {

    /**
     * 算法 service
     */
    @Resource
    private PtTrainAlgorithmService ptTrainAlgorithmService;

    /**
     * 此方法不用，算法文件使用回收默认方法
     *
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
     *  算法文件自定义还原方法
     * @param dto 还原实体
     */
    @Override
    protected void rollback(RecycleCreateDTO dto) {
        ptTrainAlgorithmService.algorithmRecycleFileRollback(dto);
    }
}