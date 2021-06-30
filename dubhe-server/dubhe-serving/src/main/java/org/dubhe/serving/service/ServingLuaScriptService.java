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

package org.dubhe.serving.service;

import java.util.List;
import java.util.Map;

/**
 * @description serving lua脚本接口类
 * @date 2020-10-15
 */
public interface ServingLuaScriptService {
    /**
     * 统计传入config ID的推理调用数和失败数
     *
     * @param configIdList 需要统计的serving config id集合
     * @return
     */
    Map<String, String> countCalls(List<Long> configIdList);

    /**
     * 统计部署服务的推理调用次数
     *
     * @param servingInfoId
     * @return
     */
    Map<String, String> countCallsByServingInfoId(Long servingInfoId);

    /**
     * 统计传入config ID的推理调用数和失败数
     *
     * @param configId
     * @return
     */
    Map<String, String> countCallsByServingConfigId(Long configId);
}
