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
import org.dubhe.model.dao.PtModelTypeMapper;
import org.dubhe.model.domain.dto.PtModelTypeQueryDTO;
import org.dubhe.model.domain.entity.PtModelType;
import org.dubhe.model.service.PtModelTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description 模型格式管理
 * @date 2021-04-26
 */
@Service
public class PtModelTypeServiceImpl implements PtModelTypeService {

    @Autowired
    private PtModelTypeMapper ptModelTypeMapper;

    /**
     * 查询模型格式
     *
     * @param ptModelTypeQueryDTO  模型格式查询参数
     * @return Map<Integer, List < Integer>> 模型格式查询结果
     */
    @Override
    public Map<Integer, List<Integer>> queryAll(PtModelTypeQueryDTO ptModelTypeQueryDTO) {
        QueryWrapper<PtModelType> wrapper = new QueryWrapper<>();
        Integer frameType = ptModelTypeQueryDTO.getFrameType();
        wrapper.eq(frameType != null, "frame_type", frameType);
        List<PtModelType> ptModelTypes = ptModelTypeMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(ptModelTypes)) {
            return null;
        }
        Map<Integer, List<Integer>> ptModelTypeMap = new HashMap<>();
        for (PtModelType ptModelType : ptModelTypes) {
            ptModelTypeMap.put(ptModelType.getFrameType(), Arrays.stream(ptModelType.getModelType().split(",")).map(s -> Integer.parseInt(s.trim())).collect(Collectors.toList()));
        }
        return ptModelTypeMap;
    }
}