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
package org.dubhe.train.client;

import org.dubhe.biz.base.constant.ApplicationNameConst;
import org.dubhe.biz.base.dto.DictDetailQueryByLabelNameDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.DictDetailVO;
import org.dubhe.train.client.fallback.DictDetailClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @description 字典详情远程服务调用接口
 * @date 2020-12-21
 */
@FeignClient(value = ApplicationNameConst.SERVER_ADMIN, contextId = "dictDetailClient",fallback = DictDetailClientFallback.class)
public interface DictDetailClient {

    /**
     *  根据名称查询字典详情
     * @param dictDetailQueryByLabelNameDTO  label名称
     * @return List<DictDetail> 字典集合
     */
    @GetMapping("/dictDetail/getDictDetails")
    DataResponseBody<List<DictDetailVO>> findDictDetailByName(@SpringQueryMap DictDetailQueryByLabelNameDTO dictDetailQueryByLabelNameDTO);
}
