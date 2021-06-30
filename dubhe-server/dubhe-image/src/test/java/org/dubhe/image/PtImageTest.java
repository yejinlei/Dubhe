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

package org.dubhe.image;

import com.alibaba.fastjson.JSON;
import org.dubhe.biz.base.constant.AuthConst;
import org.dubhe.cloud.unittest.base.BaseTest;
import org.dubhe.image.domain.dto.PtImageDeleteDTO;
import org.dubhe.image.domain.dto.PtImageUpdateDTO;
import org.dubhe.image.domain.dto.PtImageUploadDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;

/**
 * @description 镜像接口单元测试
 * @date 2020-06-28
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class PtImageTest extends BaseTest {

    /**
     * 查询镜像
     */
    @Test
    public void getImageTest() throws Exception {
        String accessToken = obtainAccessToken();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("current", "1");
        params.add("size", "10");
        params.add("sort", "id");
        params.add("order", "desc");
        params.add("imageResource", "0");
        params.add("imageNameOrId", "oneflow");
        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/ptImage/info").params(params).header(AuthConst.AUTHORIZATION, AuthConst.ACCESS_TOKEN_PREFIX + accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);
    }

    /**
     * 通过projectName查询镜像
     */
    @Test
    public void getTagsByImageNameTest() throws Exception {
        String accessToken = obtainAccessToken();
        String imageName = "redis";
        mockMvc.perform(
                MockMvcRequestBuilders.get("/ptImage")
                        .param("imageName", imageName).header(AuthConst.AUTHORIZATION, AuthConst.ACCESS_TOKEN_PREFIX + accessToken))
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

        mockMvcTest(MockMvcRequestBuilders.post("/ptImage/uploadImage"), JSON.toJSONString(ptImageUploadDTO),
                MockMvcResultMatchers.status().is2xxSuccessful(), 200);
    }

    /**
     * 修改镜像信息
     */
    @Test
    public void updateImageTest() throws Exception {
        PtImageUpdateDTO imageUpdateDTO = new PtImageUpdateDTO();
        imageUpdateDTO.setIds(Arrays.asList());
        imageUpdateDTO.setRemark("");
        mockMvcTest(MockMvcRequestBuilders.put("/ptImage"), JSON.toJSONString(imageUpdateDTO),
                MockMvcResultMatchers.status().is2xxSuccessful(), 200);
    }

    /**
     * 删除镜像
     */
    @Test
    public void deleteImageTest() throws Exception {
        PtImageDeleteDTO imageDeleteDTO = new PtImageDeleteDTO();
        imageDeleteDTO.setIds(Arrays.asList());
        mockMvcTest(MockMvcRequestBuilders.delete("/ptImage"), JSON.toJSONString(imageDeleteDTO),
                MockMvcResultMatchers.status().is2xxSuccessful(), 200);
    }


    /**
     *
     * 获取镜像名称列表
     */
    @Test
    public void getImageNameListTest() throws Exception {
        mockMvcTest(MockMvcRequestBuilders.get("/ptImage/imageNameList"), "",
                MockMvcResultMatchers.status().is2xxSuccessful(), 200);
    }
}

