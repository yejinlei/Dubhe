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

import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.springframework.scheduling.annotation.Async;

/**
 * @description 垃圾回收执行接口
 * @date 2021-01-20
 */
public interface CustomRecycleService {

    /**
     * 自定义回收入口
     * 注意：
     *  1:异步执行避免服务调用者等待
     *  2:不开启事务，避免大事务，且可以小事务分批执行
     * @param dto 资源回收创建对象
     */
    @Async
    void recycle(RecycleCreateDTO dto);

    /**
     * 自定义回收超时时间（单位：秒）
     * @return 默认60秒
     */
    default long getRecycleOverSecond(){
        return 60L;
    }

    /**
     * 还原资源回收
     * @param dto 资源回收创建对象
     */
    default void restore(RecycleCreateDTO dto){
        throw new BusinessException("还原资源回收暂未实现！");
    }

}
