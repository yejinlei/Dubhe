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

package org.dubhe.data.service.dataset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

/**
 * @date: 2020-05-14
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class DatasetApiTest {

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    /**
     * 数据集创建
     * @throws Exception
     */
    @Test
    public void create() throws Exception {
        log.info("数据集创建mock测试");
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(DataFactory.createDataset());
        String url = "/api/data/datasets";
        MvcResult mvcResult = MockUtil.post(requestJson,mockMvc,url);
        log.info("数据集创建测试:"+mvcResult.getResponse());
    }

    @Test
    public void publish() throws Exception {
        Long datasetId = 1L;
        String versionNum = "V2000";
        String versionNote = "新版本发布";
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(DataFactory.datasetVersionPublish(datasetId,versionNum,versionNote));
        String url = "/api/data/datasets/versions";
        MvcResult mvcResult = MockUtil.post(requestJson,mockMvc,url);
        log.info("数据集发布测试:"+mvcResult.getResponse());
    }

    @Test
    public void versionSwitch() throws Exception {
        String bus = "数据集版本切换";
        String url = "/api/data/datasets/versions/1";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("versionNum","V1000");
        MockUtil.showResponse(MockUtil.put(url,params,mockMvc),bus);
    }

    @Test
    public void datasetVersionList() throws Exception {
        String bus = "数据集版本列表";
        String url = "/api/data/datasets/versions/1";
        MockUtil.showResponse(MockUtil.get(url,mockMvc),bus);
    }

    @Test
    public void datasetVersionDelete() throws Exception {
        String bus = "数据集版本删除";
        String url = "/api/data/datasets/versions/1";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("versionNum","V2000");
        MockUtil.showResponse(MockUtil.delete(url,params,mockMvc),bus);
    }

}
