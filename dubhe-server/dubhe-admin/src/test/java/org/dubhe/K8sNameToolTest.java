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


import org.dubhe.enums.BizEnum;
import org.dubhe.enums.BizNfsEnum;
import org.dubhe.utils.K8sNameTool;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * @description K8sNameToolTest测试类
 * @date 2020-05-13
 */
@ActiveProfiles(value = "notebook")
@RunWith(SpringRunner.class)
@SpringBootTest
public class K8sNameToolTest {

    @Autowired
    private K8sNameTool k8sNameTool;

    @Test
    public void generateResourceName(){
        Assert.assertEquals("notebook-rn-info", k8sNameTool.generateResourceName(BizEnum.NOTEBOOK,"info"));
    }

    @Test
    public void generateNameSpace(){
        Assert.assertEquals("namespace-0", k8sNameTool.generateNamespace(0L));
    }

    @Test
    public void getUserIdFromNameSpace(){
        Assert.assertSame(0L, k8sNameTool.getUserIdFromNamespace("namespace-0"));
    }


    @Test
    public void getNfsPath(){
        Assert.assertTrue(k8sNameTool.getNfsPath(BizNfsEnum.NOTEBOOK,0L).startsWith("/notebook/0/"));
    }

    @Test
    public void getAbsoluteNfsPath(){
        Assert.assertEquals("/nfs/dubhe-dev/notebook/0/20200513162830yody/",k8sNameTool.getAbsoluteNfsPath("/notebook/0/20200513162830yody/"));
    }

    @Test
    public void appendBucket(){
        Assert.assertEquals("/dubhe-dev/notebook/0/20200513162830yody/",k8sNameTool.appendBucket("/notebook/0/20200513162830yody/"));
        Assert.assertEquals("/dubhe-dev/notebook/0/20200513162830yody/",k8sNameTool.appendBucket("notebook/0/20200513162830yody/"));
    }

    @Test
    public void convertNfsPath(){
        Assert.assertEquals("/algorithm-manage/0/20200513162830yody/",k8sNameTool.convertNfsPath(
                "/notebook/0/20200513162830yody/"
        ,BizNfsEnum.NOTEBOOK
        ,BizNfsEnum.ALGORITHM));
    }

    @Test
    public void validateBizNfsPath(){
        Assert.assertTrue(k8sNameTool.validateBizNfsPath(
                "/notebook/0/20200513162830yody/"
                ,BizNfsEnum.NOTEBOOK));
        Assert.assertFalse(k8sNameTool.validateBizNfsPath(
                "/notebook/0/20200513162830yody/"
                ,BizNfsEnum.ALGORITHM));
        Assert.assertFalse(k8sNameTool.validateBizNfsPath(
                "/dubhe-dev/notebook/0/20200513162830yody/"
                ,BizNfsEnum.ALGORITHM));
        Assert.assertFalse(k8sNameTool.validateBizNfsPath(
                "/nfs/dubhe-dev/notebook/0/20200513162830yody/"
                ,BizNfsEnum.ALGORITHM));
    }

    @Test
    public void getByCreateResource(){
        Assert.assertSame(BizNfsEnum.ALGORITHM , BizNfsEnum.getByCreateResource(1));
        Assert.assertNull( BizNfsEnum.getByCreateResource(10));
    }

    @Test
    public void removeNfsRootPath(){
        Assert.assertEquals( "/dubhe-dev/algorithm-manage/0/20200519111111test", k8sNameTool.removeNfsRootPath("/nfs/dubhe-dev/algorithm-manage/0/20200519111111test"));
        Assert.assertEquals( "/nfs2/dubhe-dev/algorithm-manage/0/20200519111111test", k8sNameTool.removeNfsRootPath("/nfs2/dubhe-dev/algorithm-manage/0/20200519111111test"));
    }

    @Test
    public void getPodLabel(){
        Assert.assertEquals( "notebook", k8sNameTool.getPodLabel(BizEnum.NOTEBOOK));
    }

    @Test
    public void getDatasetPath(){
        Assert.assertEquals( "/dataset", k8sNameTool.getDatasetPath());
    }
}
