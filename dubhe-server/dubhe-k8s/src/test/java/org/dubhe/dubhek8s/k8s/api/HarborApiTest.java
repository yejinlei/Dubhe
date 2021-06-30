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

package org.dubhe.dubhek8s.k8s.api;




import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.dubhek8s.DubheK8sApplication;
import org.dubhe.harbor.api.impl.HarborApiImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;


/**
 * @description HarborApi测试类
 * @date 2020-5-21
 */
@SpringBootTest(classes= DubheK8sApplication.class)
@RunWith(SpringRunner.class)
public class HarborApiTest {
    @Autowired
    HarborApiImpl harborApi;

    @Test
    public void searhImageNames()  {
        /**参数为镜像仓库项目组名称**/
         harborApi.searchImageNames(Arrays.asList("xxx"));
    }
    @Test
    public void searchImageByProject(){
        /**参数为镜像仓库项目组名称**/
        harborApi.searchImageByProjects(Arrays.asList("xxx"));
    }

    @Test
    public void isExistImage(){
        /**参数为镜像路径**/
        harborApi.isExistImage("xxx");
    }
    @Test
    public void findImageList(){
        harborApi.findImageList();
    }
    @Test
    public void findImagePage(){
        harborApi.findImagePage(new Page());
    }

}

