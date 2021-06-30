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

package org.dubhe.dubhek8s.k8s;

import com.alibaba.fastjson.JSON;
import org.dubhe.biz.redis.utils.RedisUtils;
import org.dubhe.dubhek8s.DubheK8sApplication;
import org.dubhe.k8s.cache.ResourceCache;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @description ResourceCacheTest测试类
 * @date 2020-5-20
 */
@SpringBootTest(classes= DubheK8sApplication.class)
@RunWith(SpringRunner.class)
public class ResourceCacheTest {
    @Resource
    private ResourceCache resourceCache;
    @Autowired
    private RedisUtils redisUtils;

    @Test
    public void cachePod() {
        resourceCache.cachePod("res","notebook1");
    }

    @Test
    public void getPodNameByResourceName() {
        Set<String> podNames = resourceCache.getPodNameByResourceName("res","notebook1");
        System.out.println(JSON.toJSONString(podNames));
    }

    @Test
    public void getResourceNameByPodName() {
        System.out.println(resourceCache.getResourceNameByPodName("res","notebook1-u5x14-0"));
    }

    @Test
    public void deletePodCacheByResourceName() {
        System.out.println(resourceCache.deletePodCacheByResourceName("res","notebook1"));
    }

    @Test
    public void deletePodCacheByPodName() {
        System.out.println(resourceCache.deletePodCacheByPodName("res","notebook1-u5x14-0"));
    }

    @Test
    public void cachePods(){
        String namespace = "namespace-41",resourceName = "notebook-rn-algorithm-153",podName = "notebook-rn-algorithm-153-7djrk-0";
        resourceCache.cachePods(namespace,resourceName);
    }

    @Test
    public void getDistributedLock(){
        System.out.println(redisUtils.getDistributedLock("87jkssshjk","fhfgsssygfjfgh",10));
        System.out.println(redisUtils.getDistributedLock("87jkssshjk","fhfgsssygfjfgh",10));
        System.out.println(redisUtils.releaseDistributedLock("87jkssshjk","fhfgsssygfjfgh"));
        System.out.println(redisUtils.getDistributedLock("87jkssshjk","fhfgsssygfjfgh",10));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(redisUtils.getDistributedLock("87jkssshjk","fhfgsssygfjfgh",10));
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(redisUtils.getDistributedLock("87jkssshjk","fhfgsssygfjfgh",10));
    }
}
