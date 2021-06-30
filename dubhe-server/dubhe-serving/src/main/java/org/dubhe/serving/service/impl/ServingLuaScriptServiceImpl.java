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

package org.dubhe.serving.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.serving.service.ServingLuaScriptService;
import org.dubhe.serving.service.ServingModelConfigService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description serving lua脚本实现类
 * @date 2020-10-15
 */
@Service
public class ServingLuaScriptServiceImpl implements ServingLuaScriptService {

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    private DefaultRedisScript<List> countCallsScript;

    @Resource
    private ServingModelConfigService servingModelConfigService;

    /**
     * 初始化脚本
     */
    @PostConstruct
    public void initScript() {
        countCallsScript = new DefaultRedisScript<>();
        countCallsScript.setResultType(List.class);
        countCallsScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("countCalls.lua")));
    }

    /**
     * 统计serving id
     *
     * @param configIdList 需要统计的serving config id集合
     * @return
     */
    @Override
    public Map<String, String> countCalls(List<Long> configIdList) {
        if (CollectionUtils.isEmpty(configIdList)) {
            return Collections.emptyMap();
        }

        List statistics = redisTemplate.execute(countCallsScript, new ArrayList<>(configIdList));

        Map<String, String> result = new HashMap<>(NumberConstant.NUMBER_2);
        if (CollectionUtils.isNotEmpty(statistics) && statistics.size() != NumberConstant.NUMBER_2) {
            result.put("callCount", SymbolConstant.ZERO);
            result.put("failedCount", SymbolConstant.ZERO);
        }
        if (statistics == null){
            throw new BusinessException("statistics 不能为空");
        }
        result.put("callCount", statistics.get(NumberConstant.NUMBER_0).toString());
        result.put("failedCount", statistics.get(NumberConstant.NUMBER_1).toString());
        return result;
    }

    /**
     * 根据在线服务id计算请求数量
     *
     * @param servingInfoId 在线服务id
     * @return
     */
    @Override
    public Map<String, String> countCallsByServingInfoId(Long servingInfoId) {
        return this.countCalls(new ArrayList<>(servingModelConfigService.getIdsByServingId(servingInfoId)));
    }

    /**
     * 根据在线模型部署信息id计算请求数量
     *
     * @param configId 模型部署信息id
     * @return
     */
    @Override
    public Map<String, String> countCallsByServingConfigId(Long configId) {
        return countCalls(Collections.singletonList(configId));
    }
}
