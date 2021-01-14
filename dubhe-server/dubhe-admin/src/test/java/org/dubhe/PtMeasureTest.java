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

package org.dubhe;

import com.alibaba.fastjson.JSON;
import org.dubhe.domain.dto.PtMeasureDTO;
import org.dubhe.domain.dto.PtMeasureDeleteDTO;
import org.dubhe.domain.dto.PtMeasureUpdateDTO;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description 度量管理接口单元测试
 * @date 2020-11-16
 */
public class PtMeasureTest extends BaseTest {

    /**
     * 查询度量
     */
    @Test
    public void getMeasureTest() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("current", "1");
        params.add("size", "10");
        params.add("sort", "id");
        params.add("order", "desc");
        params.add("name", "COCO1");
        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/ptMeasure/info").params(params))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);
    }


    /**
     * 新建度量
     */
    @Test
    public void addMeasureTest() throws Exception {
        PtMeasureDTO ptMeasureCreateDTO = PtMeasureDTO.builder()
                .name("测试111701")
                .description("测试新建度量")
                .url("/nfs/dubhe-dev/upload-temp/1/5b07d66ad27a11eab7cffa3a5ae24f00.json")
                .build();

        mockMvcTest(MockMvcRequestBuilders.post("/api/v1/ptMeasure"), JSON.toJSONString(ptMeasureCreateDTO),
                MockMvcResultMatchers.status().is2xxSuccessful(), 200);
    }

    /**
     * 修改度量信息
     */
    @Test
    public void updateMeasureTest() throws Exception {
        PtMeasureUpdateDTO ptMeasureUpdateDTO = PtMeasureUpdateDTO.builder()
                .id(1L)
                .name("COCO1")
                .description("测试修改度量")
                .url("E:/chromeDownload/5b07d66ad27a11eab7cffa3a5ae24f00.json")
                .build();

        mockMvcTest(MockMvcRequestBuilders.put("/api/v1/ptMeasure"), JSON.toJSONString(ptMeasureUpdateDTO),
                MockMvcResultMatchers.status().is2xxSuccessful(), 200);
    }

    /**
     * 删除度量
     */
    @Test
    public void deleteMeasureTest() throws Exception {
        Set<Long> ids = Stream.of(1L, 2L).collect(Collectors.toSet());
        PtMeasureDeleteDTO ptMeasureDeleteDTO = PtMeasureDeleteDTO.builder().ids(ids).build();
        mockMvcTest(MockMvcRequestBuilders.delete("/api/v1/ptMeasure"), JSON.toJSONString(ptMeasureDeleteDTO),
                MockMvcResultMatchers.status().is2xxSuccessful(), 200);
    }

    /**
     * 根据度量名称返回度量文件信息
     */
    @Test
    public void getMeasureByNameTest() throws Exception {
        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/ptMeasure/byName").param("name", "COCO"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);
    }
}

