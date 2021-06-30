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
package org.dubhe.model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.dubhe.model.dao.PtModelSuffixMapper;
import org.dubhe.model.domain.dto.PtModelSuffixDTO;
import org.dubhe.model.domain.entity.PtModelSuffix;
import org.dubhe.model.service.PtModelSuffixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description 模型后缀名管理
 * @date 2021-04-26
 */
@Service
public class PtModelSuffixServiceImpl implements PtModelSuffixService {

    @Autowired
    private PtModelSuffixMapper ptModelSuffixMapper;

    /**
     * 查询模型后缀名
     *
     * @param ptModelSuffixDTO  模型后缀名查询参数
     * @return Map<Integer, String> 模型后缀名查询结果
     */
    @Override
    public Map<Integer, String> getModelSuffix(PtModelSuffixDTO ptModelSuffixDTO) {
        QueryWrapper<PtModelSuffix> wrapper = new QueryWrapper<>();
        Integer modelType = ptModelSuffixDTO.getModelType();
        wrapper.eq(modelType != null, "model_type", modelType);
        List<PtModelSuffix> ptModelSuffixs = ptModelSuffixMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(ptModelSuffixs)) {
            return null;
        }
        Map<Integer, String> ptModelSuffixMap = new HashMap<>();
        for (PtModelSuffix ptModelSuffix : ptModelSuffixs) {
            ptModelSuffixMap.put(ptModelSuffix.getModelType(), ptModelSuffix.getModelSuffix());
        }
        return ptModelSuffixMap;
    }

}