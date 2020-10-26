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
package org.dubhe;

import org.dubhe.domain.dto.RecycleTaskCreateDTO;
import org.dubhe.domain.entity.RecycleTask;
import org.dubhe.enums.RecycleModuleEnum;
import org.dubhe.enums.RecycleTypeEnum;
import org.dubhe.service.RecycleTaskService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @description 垃圾回收功能单元测试
 * @date 2020-09-17
 */
public class RecycleTaskTest extends BaseTest {


    @Autowired
    private RecycleTaskService recycleTaskService;


    @Test
    public void addRecycleTaskTest() {
        RecycleTaskCreateDTO recycleTaskCreateDTO = new RecycleTaskCreateDTO();
        recycleTaskCreateDTO.setRecycleCondition("delete * from recycle_task from recycle_status=0")
                .setRecycleDelayDate(7)
                .setRecycleModule(RecycleModuleEnum.BIZ_TRAIN.getValue())
                .setRecycleType(RecycleTypeEnum.TABLE_DATA.getCode());

        recycleTaskService.createRecycleTask(recycleTaskCreateDTO);
    }

    @Test
    public void getRecycleTaskTest() {

        List<RecycleTask> recycleTaskList = recycleTaskService.getRecycleTaskList();
        System.out.println(recycleTaskList);
    }

}
