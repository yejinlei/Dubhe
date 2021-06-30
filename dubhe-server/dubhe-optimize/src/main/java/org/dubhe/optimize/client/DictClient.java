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

package org.dubhe.optimize.client;

import org.dubhe.biz.base.constant.ApplicationNameConst;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.DictVO;
import org.dubhe.optimize.client.fallback.DictClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * @description 字典远程服务调用类
 * @date 2021-01-19
 */
@FeignClient(value = ApplicationNameConst.SERVER_ADMIN, contextId = "dictClient",fallback = DictClientFallback.class)
public interface DictClient {

    /**
     *  根据名称查询字典详情
     * @param name  名称
     * @return DataResponseBody<DictVO> 字典
     */
    @GetMapping("/dict/{name}")
    DataResponseBody<DictVO> findDictByName(@PathVariable String name);
}
