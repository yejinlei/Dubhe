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

import com.alibaba.fastjson.JSON;
import org.dubhe.dubhek8s.DubheK8sApplication;
import org.dubhe.k8s.api.LimitRangeApi;
import org.dubhe.k8s.domain.bo.PtLimitRangeBO;
import org.dubhe.k8s.domain.resource.BizLimitRangeItem;
import org.dubhe.k8s.domain.resource.BizQuantity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @description LimitRangeApiTest测试类
 * @date  2020-04-23
 */
@SpringBootTest(classes= DubheK8sApplication.class)
@RunWith(SpringRunner.class)
public class LimitRangeApiTest {
    @Resource
    private LimitRangeApi limitRangeApi;

    @Test
    public void create() {
        PtLimitRangeBO bo = new PtLimitRangeBO();
        bo.setNamespace("namespace");bo.setName("limit-range");
        List<BizLimitRangeItem> limits = new ArrayList<>();
        BizLimitRangeItem bizLimitRangeItem = new BizLimitRangeItem();
        bizLimitRangeItem.set_default(new HashMap<String, BizQuantity>(5){
            {
                put("cpu",new BizQuantity("2",""));
                put("memory",new BizQuantity("512","Mi"));
            }
        });
        bizLimitRangeItem.setDefaultRequest(new HashMap<String, BizQuantity>(5){
            {
                put("cpu",new BizQuantity("0.5",""));
                put("memory",new BizQuantity("256","Mi"));
            }
        });
        bizLimitRangeItem.setMax(new HashMap<String, BizQuantity>(5){
            {
                put("cpu",new BizQuantity("3",""));
                put("memory",new BizQuantity("800","Mi"));
            }
        });
        bizLimitRangeItem.setMin(new HashMap<String, BizQuantity>(5){
            {
                put("cpu",new BizQuantity("0.3",""));
                put("memory",new BizQuantity("100","Mi"));
            }
        });
        bizLimitRangeItem.setMaxLimitRequestRatio(new HashMap<String, BizQuantity>(5){
            {
                put("cpu",new BizQuantity("2",""));
                put("memory",new BizQuantity("2",""));
            }
        });
        limits.add(bizLimitRangeItem);
        bo.setLimits(limits);
        System.out.println("create = "+ JSON.toJSONString(limitRangeApi.create(bo)));
    }

    @Test
    public void list() {
        System.out.println("list = "+JSON.toJSONString(limitRangeApi.list("namespace")));
    }

    @Test
    public void delete() {
        System.out.println("delete = "+limitRangeApi.delete("namespace","limit-range"));
    }
}
