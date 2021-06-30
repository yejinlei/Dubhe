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
package org.dubhe.model.service;

import org.dubhe.model.domain.dto.PtModelTypeQueryDTO;

import java.util.List;
import java.util.Map;

/**
 * @description 模型格式管理
 * @date 2021-04-26
 */
public interface PtModelTypeService {
    /**
     * 查询模型格式
     *
     * @param ptModelTypeQueryDTO  模型格式查询参数
     * @return Map<Integer, List < Integer>> 模型格式查询结果
     */
    Map<Integer, List<Integer>> queryAll(PtModelTypeQueryDTO ptModelTypeQueryDTO);

}