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

package org.dubhe.notebook.client;

import org.dubhe.biz.base.constant.ApplicationNameConst;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.DatasetVO;
import org.dubhe.notebook.client.fallback.DatasetClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @description 数据集远程调用接口
 * @date 2021-05-28
 */
@FeignClient(value = ApplicationNameConst.SERVER_DATA, contextId = "datasetClient", fallback = DatasetClientFallback.class)
public interface DatasetClient {


    /**
     * 获取数据集详情
     *
     * @return 数据集详情
     */
    @GetMapping(value = "/datasets/{datasetId}")
    DataResponseBody<DatasetVO> get(@PathVariable(value="datasetId") int datasetId);

}
