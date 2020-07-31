/** Copyright 2020 Zhejiang Lab. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =============================================================
 */

package org.dubhe;

import com.alibaba.fastjson.JSON;
import org.dubhe.domain.dto.PtTrainAlgorithmQueryDTO;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * @description 代理转发单元测试
 * @date 2020-06-23
 */

public class ForwardTest extends BaseTest {
    @Test
    public void ForwardControllerTest() throws Exception {
        PtTrainAlgorithmQueryDTO ptTrainAlgorithmQueryDTO = new PtTrainAlgorithmQueryDTO();
        ptTrainAlgorithmQueryDTO.setAlgorithmSource(1).setCurrent(1).setSize(10).setSort("id").setOrder("asc");
        mockMvcTest(MockMvcRequestBuilders.get("/forward/v1/algorithm"), JSON.toJSONString(ptTrainAlgorithmQueryDTO), MockMvcResultMatchers.status().isOk(), 200);
    }
}
