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

package org.dubhe.model;

import com.alibaba.fastjson.JSON;
import org.dubhe.biz.base.constant.AuthConst;
import org.dubhe.cloud.unittest.base.BaseTest;
import org.dubhe.model.domain.dto.PtModelInfoCreateDTO;
import org.dubhe.model.domain.dto.PtModelInfoDeleteDTO;
import org.dubhe.model.domain.dto.PtModelInfoUpdateDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

/**
 * @description 模型管理模块单元测试
 * @date 2020-05-06
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ModelInfoTest extends BaseTest {

    /**
     * 查询任务参数列表
     * 无条件分页查询
     */
    @Test
    public void ptModelInfoQueryTest() throws Exception {
        String accessToken = obtainAccessToken();
        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/api/ptModelInfo").header(AuthConst.AUTHORIZATION, AuthConst.ACCESS_TOKEN_PREFIX + accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);

    }

    /**
     * 新增ModelInfo
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(false)
    public void ptModelInfoCreateTest() throws Exception {
        PtModelInfoCreateDTO ptModelInfoCreateDTO = new PtModelInfoCreateDTO();
        ptModelInfoCreateDTO.setName("untilTestingCreateTest")
                .setFrameType(1)
                .setModelType(1)
                .setModelClassName("测试")
                .setModelDescription("untilTestingCreateTest");
        mockMvcTest(MockMvcRequestBuilders.post("/api/ptModelInfo"), JSON.toJSONString(ptModelInfoCreateDTO), MockMvcResultMatchers.status().isOk(), 200);
    }

    /**
     * 更新ModelInfo
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(false)
    public void ptModelInfoUpdateTest() throws Exception {
        PtModelInfoUpdateDTO ptModelInfoUpdateDTO = new PtModelInfoUpdateDTO();
        ptModelInfoUpdateDTO.setId(117L)
                .setName("untilTestingUpdateTest" + System.currentTimeMillis())
                .setFrameType(1)
                .setModelType(1)
                .setModelClassName("测试")
                .setModelDescription("untilTestingUpdateTest");
        mockMvcTest(MockMvcRequestBuilders.put("/api/ptModelInfo"), JSON.toJSONString(ptModelInfoUpdateDTO), MockMvcResultMatchers.status().isOk(), 200);
    }

    /**
     * 删除任务参数
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(false)
    public void ptTrainAlgorithmDeleteTest() throws Exception {
        Long[] ids = {117L};
        PtModelInfoDeleteDTO ptModelInfoDeleteDTO = new PtModelInfoDeleteDTO();
        ptModelInfoDeleteDTO.setIds(ids);
        mockMvcTest(MockMvcRequestBuilders.delete("/api/ptModelInfo"), JSON.toJSONString(ptModelInfoDeleteDTO), MockMvcResultMatchers.status().isOk(), 200);
    }


}
