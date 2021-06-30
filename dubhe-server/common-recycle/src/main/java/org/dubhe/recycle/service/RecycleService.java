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
package org.dubhe.recycle.service;

import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.entity.Recycle;
import org.dubhe.recycle.domain.entity.RecycleDetail;
import org.dubhe.recycle.enums.RecycleStatusEnum;

/**
 * @description 通用回收垃圾 服务类
 * @date 2021-02-03
 */
public interface RecycleService {


    /**
     * 创建垃圾回收任务
     *
     * @param recycleCreateDTO 垃圾回收任务信息
     */
    void createRecycleTask(RecycleCreateDTO recycleCreateDTO);

    /**
     * 修改回收任务状态
     *
     * @param recycle 回收任务
     * @param statusEnum 回收状态
     * @param recycleResponse 回收响应
     * @param userId 操作用户
     */
    void updateRecycle(Recycle recycle, RecycleStatusEnum statusEnum, String recycleResponse, long userId);

    /**
     * 修改回收任务详情状态
     *
     * @param recycleDetail 回收任务详情
     * @param statusEnum 回收状态
     * @param recycleResponse 回收响应
     * @param userId 操作用户
     */
    void updateRecycleDetail(RecycleDetail recycleDetail, RecycleStatusEnum statusEnum, String recycleResponse, long userId);

}
