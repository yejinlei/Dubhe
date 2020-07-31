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


import org.dubhe.domain.dto.PtModelBranchCreateDTO;
import org.dubhe.domain.dto.PtModelBranchDeleteDTO;
import org.dubhe.domain.dto.PtModelBranchQueryDTO;
import org.dubhe.domain.dto.PtModelBranchUpdateDTO;
import org.dubhe.domain.vo.PtModelBranchCreateVO;
import org.dubhe.service.impl.PtModelBranchServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

/**
 * @description 分支管理
 * @date 2020-05-06
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ModelBranchTest {
    @Autowired
    private PtModelBranchServiceImpl ptModelBranchServiceImpl;

    /**
     * 查询ModelBranch
     */
    @Test
    public void ptModelBranchQueryTest() {
        PtModelBranchQueryDTO ptModelBranchQueryCriteria = new PtModelBranchQueryDTO();
        ptModelBranchQueryCriteria.setParentId(1);


        Map<String, Object> results = ptModelBranchServiceImpl.queryAll(ptModelBranchQueryCriteria);
        System.out.println(results.toString());
    }


    /**
     * 新增ModelBranch
     */
    @Test
    public void ptModelBranchCreateTest() {
        PtModelBranchCreateDTO ptModelBranchCreateDTO = new PtModelBranchCreateDTO();
        ptModelBranchCreateDTO.setParentId((long) 1)
                .setModelAddress("http://10.0.0.1");
        PtModelBranchCreateVO ptModelBranchCreateVO = ptModelBranchServiceImpl.create(ptModelBranchCreateDTO);
        System.out.println(ptModelBranchCreateVO.toString());
    }

    /**
     * 修改ModelBranch
     */
    @Test
    public void ptModelBranchUpdateTest() {
        PtModelBranchUpdateDTO ptModelBranchUpdateDTO = new PtModelBranchUpdateDTO();
        ptModelBranchUpdateDTO.setParentId((long) 1)
                .setModelAddress("http://10.0.0.1");
        ptModelBranchServiceImpl.update(ptModelBranchUpdateDTO);
    }

    /**
     * 删除ModelBranch
     */
    @Test
    public void ptModelBranchDeleteTest() {
        Long[] ids = {(long) 1, (long) 2};
        PtModelBranchDeleteDTO ptModelBranchDeleteDTO =new PtModelBranchDeleteDTO();
        ptModelBranchDeleteDTO.setIds(ids);

        ptModelBranchServiceImpl.deleteAll(ptModelBranchDeleteDTO);
    }
}
