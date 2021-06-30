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
package org.dubhe.algorithm.client;

import org.dubhe.algorithm.client.fallback.NoteBookClientFallback;
import org.dubhe.biz.base.constant.ApplicationNameConst;
import org.dubhe.biz.base.dto.NoteBookAlgorithmQueryDTO;
import org.dubhe.biz.base.dto.NoteBookAlgorithmUpdateDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

/**
 * @description notebook远程服务调用接口
 * @date 2020-12-14
 */
@FeignClient(value = ApplicationNameConst.SERVER_NOTEBOOK, contextId = "noteBookClient", fallback = NoteBookClientFallback.class)
public interface NoteBookClient {

    /**
     * 更新notebook算法ID
     *
     * @param  noteBookAlgorithmUpdateDTO 更新notebook算法ID DTO
     */
    @PutMapping(value = "/notebooks/algorithm")
    void updateNoteBookAlgorithm(NoteBookAlgorithmUpdateDTO noteBookAlgorithmUpdateDTO);

    /**
     * 获取notebook算法ID
     *
     * @param  noteBookAlgorithmQueryDTO 获取notebook算法ID DTO
     * @return 算法ID
     */
    @GetMapping(value = "/notebooks/algorithm")
    DataResponseBody<List<Long>> getNoteBookIdByAlgorithm(@SpringQueryMap NoteBookAlgorithmQueryDTO noteBookAlgorithmQueryDTO);
}
