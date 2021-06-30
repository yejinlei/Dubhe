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
import org.dubhe.model.domain.dto.PtModelBranchCreateDTO;
import org.dubhe.model.domain.dto.PtModelBranchDeleteDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

/**
 * @description 分支管理
 * @date 2020-05-06
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ModelBranchTest extends BaseTest {

    /**
     * 查询ModelBranch
     */
    @Test
    public void ptModelBranchQueryTest() throws Exception {
        String accessToken = obtainAccessToken();
        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/api/ptModelBranch").param("parentId", String.valueOf(113)).header(AuthConst.AUTHORIZATION, AuthConst.ACCESS_TOKEN_PREFIX + accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);

    }


    /**
     * 新增ModelBranch
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(false)
    public void ptModelBranchCreateTest() throws Exception {
        PtModelBranchCreateDTO ptModelBranchCreateDTO = new PtModelBranchCreateDTO();
        ptModelBranchCreateDTO.setParentId(114L)
                .setModelAddress("/model/1/20201130170019114pij1/").setModelSource(1);
        mockMvcTest(MockMvcRequestBuilders.post("/api/ptModelBranch"), JSON.toJSONString(ptModelBranchCreateDTO), MockMvcResultMatchers.status().isOk(), 200);
    }


    /**
     * 删除ModelBranch
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(false)
    public void ptModelBranchDeleteTest() throws Exception {
        Long[] ids = {94L};
        PtModelBranchDeleteDTO ptModelBranchDeleteDTO = new PtModelBranchDeleteDTO();
        ptModelBranchDeleteDTO.setIds(ids);
        mockMvcTest(MockMvcRequestBuilders.delete("/api/ptModelBranch"), JSON.toJSONString(ptModelBranchDeleteDTO), MockMvcResultMatchers.status().isOk(), 200);

    }
}
