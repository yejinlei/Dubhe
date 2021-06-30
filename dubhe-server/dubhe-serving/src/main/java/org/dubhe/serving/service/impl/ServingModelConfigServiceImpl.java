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

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.serving.dao.ServingModelConfigMapper;
import org.dubhe.serving.domain.entity.ServingModelConfig;
import org.dubhe.serving.service.ServingModelConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @description 模型配置
 * @date 2020-08-26
 */
@Service
public class ServingModelConfigServiceImpl extends ServiceImpl<ServingModelConfigMapper, ServingModelConfig> implements ServingModelConfigService {

    @Resource
    private ServingModelConfigMapper servingModelConfigMapper;

    @Override
    public Set<Long> getIdsByServingId(Long servingId) {
        return servingModelConfigMapper.getIdsByServingId(servingId);
    }
}
