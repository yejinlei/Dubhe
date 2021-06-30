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

package org.dubhe.measure;

import com.alibaba.fastjson.JSON;
import org.dubhe.biz.base.constant.AuthConst;
import org.dubhe.cloud.unittest.base.BaseTest;
import org.dubhe.measure.domain.dto.PtMeasureCreateDTO;
import org.dubhe.measure.domain.dto.PtMeasureDeleteDTO;
import org.dubhe.measure.domain.dto.PtMeasureUpdateDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description 度量管理接口单元测试
 * @date 2020-11-16
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class PtMeasureTest extends BaseTest {

    /**
     * 查询度量
     */
    @Test
    public void getMeasureTest() throws Exception {
        String accessToken = obtainAccessToken();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("current", "1");
        params.add("size", "10");
        params.add("sort", "id");
        params.add("order", "desc");
        params.add("name", "COCO1");
        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/ptMeasure/info").params(params).header(AuthConst.AUTHORIZATION, AuthConst.ACCESS_TOKEN_PREFIX + accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);
    }


    /**
     * 新建度量
     */
    @Test
    public void addMeasureTest() throws Exception {
        List<String> modelUrls = Arrays.asList(
                "/model/1/202101141540149155j9w/finegraind_alexnet/",
                "/model/1/20210114154105585i867/finegraind_googlenet/",
                "/model/1/20210114154151791m1d6/finegraind_mobilenet_v2/",
                "/model/1/20210114154241707yr9n/finegraind_squeezenet1_0/",
                "/model/1/20210114154309821f0kk/finegraind_squeezenet1_1/"
        );

        PtMeasureCreateDTO ptMeasureCreateDTO = PtMeasureCreateDTO.builder()
                .name("测试111701")
                .description("测试新建度量")
                .datasetId(16L)
                .datasetUrl("/dataset/958/origin/")
                .modelUrls(modelUrls)
                .build();
        mockMvcTest(MockMvcRequestBuilders.post("/ptMeasure"), JSON.toJSONString(ptMeasureCreateDTO),
                MockMvcResultMatchers.status().is2xxSuccessful(), 200);
    }

    /**
     * 修改度量信息
     */
    @Test
    public void updateMeasureTest() throws Exception {
        List<String> modelUrls = Arrays.asList(
                "/model/1/202101141540149155j9w/finegraind_alexnet/",
                "/model/1/20210114154105585i867/finegraind_googlenet/",
                "/model/1/20210114154151791m1d6/finegraind_mobilenet_v2/",
                "/model/1/20210114154241707yr9n/finegraind_squeezenet1_0/",
                "/model/1/20210114154309821f0kk/finegraind_squeezenet1_1/"
        );
        PtMeasureUpdateDTO ptMeasureUpdateDTO = PtMeasureUpdateDTO.builder()
                .id(1L)
                .name("测试111702")
                .description("测试修改度量")
                .datasetUrl("/dataset/958/origin/")
                .modelUrls(modelUrls)
                .build();
        mockMvcTest(MockMvcRequestBuilders.put("/ptMeasure"), JSON.toJSONString(ptMeasureUpdateDTO),
                MockMvcResultMatchers.status().is2xxSuccessful(), 200);
    }

    /**
     * 删除度量
     */
    @Test
    public void deleteMeasureTest() throws Exception {
        Set<Long> ids = Stream.of(1L, 2L).collect(Collectors.toSet());
        PtMeasureDeleteDTO ptMeasureDeleteDTO = PtMeasureDeleteDTO.builder().ids(ids).build();
        mockMvcTest(MockMvcRequestBuilders.delete("/ptMeasure"), JSON.toJSONString(ptMeasureDeleteDTO),
                MockMvcResultMatchers.status().is2xxSuccessful(), 200);
    }

    /**
     * 根据度量名称返回度量文件信息
     */
    @Test
    public void getMeasureByNameTest() throws Exception {
        String accessToken = obtainAccessToken();
        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/ptMeasure/byName").param("name", "COCO").header(AuthConst.AUTHORIZATION, AuthConst.ACCESS_TOKEN_PREFIX + accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);
    }
}

