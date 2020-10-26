/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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
package org.dubhe.service;

import org.dubhe.constant.NumberConstant;
import org.dubhe.domain.dto.RecycleTaskCreateDTO;
import org.dubhe.domain.entity.RecycleTask;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

/**
 * @description 数据集清理回收类
 * @date 2020-10-09
 */
public abstract class AbstractGlobalRecycle {


    /**
     * 回收服务
     */
    @Autowired
    private RecycleTaskService recycleTaskService;

    /**
     * 初始时间
     */
    private long startTime;

    /**
     * 默认超时时间5小时(秒)
     */
    private final int OVER_SECOND = 5*60*60;

    /**
     * 数据清理方法
     *
     * @param object 数据清理参数
     */
    public abstract void clear(Object object) throws Exception;



    /**
     * 校验超时时间
     *
     * @param task 数据回收数据回收实体
     * @return  true: 超时 false: 未超时
     */
    public boolean checkoutOverTime(RecycleTask task){
        boolean overTimeFlag = false;
        if(startTime == 0){
            startTime = System.currentTimeMillis();
        }
        long currentTime = System.currentTimeMillis();
        // 6*60*60 六小时
        if((currentTime -startTime) / NumberConstant.NUMBER_1000 >OVER_SECOND){
            addNewRecycleTask(task);
            overTimeFlag = true;
        }
        return !overTimeFlag;
    }


    /**
     * 新增回收任务
     *
     * @param task  数据回收数据回收实体
     */
    public void addNewRecycleTask(RecycleTask task){
        if(!Objects.isNull(task)){
            RecycleTaskCreateDTO recycleTaskCreateDTO = RecycleTaskCreateDTO.builder()
                    .recycleNote(task.getRecycleNote())
                    .recycleModule(task.getRecycleModule())
                    .recycleType(task.getRecycleType())
                    .recycleDelayDate(NumberConstant.NUMBER_1)
                    .recycleCondition(task.getRecycleCondition())
                    .recycleCustom(task.getRecycleCustom())
                    .build();
            recycleTaskCreateDTO.setCreateUserId(task.getCreateUserId());
            recycleTaskCreateDTO.setUpdateUserId(task.getUpdateUserId());
            recycleTaskService.createRecycleTask(recycleTaskCreateDTO);
        }

    }

}
