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

import com.alibaba.fastjson.JSON;
import org.dubhe.domain.dto.PtImageUploadDTO;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @description 镜像接口单元测试
 * @date 2020-06-28
 */
public class PtImageTest extends BaseTest {

    /**
     * 查询镜像
     */
    @Test
    public void getImageTest() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("current", "1");
        params.add("size", "10");
        params.add("sort", "id");
        params.add("order", "desc");
        params.add("imageResource", "1");
        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/ptImage/info").param("imageResource", "1"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);
    }

    /**
     * 通过projectName查询镜像
     */
    @Test
    public void getTagsByImageNameTest() throws Exception {
        String imageName = "redis";
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/ptImage")
                        .param("imageName", imageName))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
    }

    /**
     * 上传镜像包到harbor
     */
    @Test
    public void uploadImageTest() throws Exception {
        PtImageUploadDTO ptImageUploadDTO = new PtImageUploadDTO();
        ptImageUploadDTO.setImageName("mysql");
        ptImageUploadDTO.setImagePath("F:/mysql.tar");
        ptImageUploadDTO.setImageTag("5.7");
        ptImageUploadDTO.setRemark("测试上传镜像");

        mockMvcTest(MockMvcRequestBuilders.post("/api/v1/ptImage/uploadImage"), JSON.toJSONString(ptImageUploadDTO),
                MockMvcResultMatchers.status().is2xxSuccessful(), 200);
    }

}

