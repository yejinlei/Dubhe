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

package org.dubhe.model;

import org.dubhe.domain.dto.PtModelInfoCreateDTO;
import org.dubhe.domain.dto.PtModelInfoDeleteDTO;
import org.dubhe.domain.dto.PtModelInfoQueryDTO;
import org.dubhe.domain.dto.PtModelInfoUpdateDTO;
import org.dubhe.domain.vo.PtModelInfoCreateVO;
import org.dubhe.service.impl.PtModelInfoServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

/**
 * @description 模型管理模块单元测试
 * @date 2020-05-06
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ModelInfoTest {
    @Autowired
    private PtModelInfoServiceImpl ptModelInfoServiceImpl;

    /**
     * 查询任务参数列表
     * 无条件分页查询
     */
    @Test
    public void ptModelInfoQueryTest() {
        PtModelInfoQueryDTO ptModelInfoQueryCriteria = new PtModelInfoQueryDTO();

        Map<String, Object> results = ptModelInfoServiceImpl.queryAll(ptModelInfoQueryCriteria);
        System.out.println(results.toString());
    }

    /**
     * 新增ModelInfo
     */
    @Test
    public void ptModelInfoCreateTest() {
        PtModelInfoCreateDTO ptModelInfoCreateDTO = new PtModelInfoCreateDTO();
        ptModelInfoCreateDTO.setName("test")
                .setFrameType(1)
                .setModelType(1)
                .setModelClassName("测试")
                .setModelDescription("test");
        PtModelInfoCreateVO ptModelInfoCreateVO = ptModelInfoServiceImpl.create(ptModelInfoCreateDTO);
        System.out.println(ptModelInfoCreateVO.toString());
    }

    /**
     * 更新ModelInfo
     */
    @Test
    public void ptModelInfoUpdateTest() {
        PtModelInfoUpdateDTO ptModelInfoUpdateDTO = new PtModelInfoUpdateDTO();
        ptModelInfoUpdateDTO.setId((long) 1)
                .setName("test")
                .setFrameType(1)
                .setModelType(1)
                .setModelClassName("测试")
                .setModelDescription("test");
        ptModelInfoServiceImpl.update(ptModelInfoUpdateDTO);
    }

    /**
     * 删除任务参数
     */
    @Test
    public void ptTrainAlgorithmDeleteTest() {
        Long[] ids = {(long) 1, (long) 2};
        PtModelInfoDeleteDTO ptModelInfoDeleteDTO = new PtModelInfoDeleteDTO();
        ptModelInfoDeleteDTO.setIds(ids);
        ptModelInfoServiceImpl.deleteAll(ptModelInfoDeleteDTO);
    }


}
