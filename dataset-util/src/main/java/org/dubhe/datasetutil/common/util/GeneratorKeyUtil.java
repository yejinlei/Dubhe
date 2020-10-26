/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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
package org.dubhe.datasetutil.common.util;

import cn.hutool.core.util.ObjectUtil;
import org.dubhe.datasetutil.common.base.MagicNumConstant;
import org.dubhe.datasetutil.common.enums.LogEnum;
import org.dubhe.datasetutil.common.exception.DataSequenceException;
import org.dubhe.datasetutil.domain.dto.IdAlloc;
import org.dubhe.datasetutil.domain.entity.DataSequence;
import org.dubhe.datasetutil.service.DataSequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @description 生成ID工具类
 * @date 2020-09-23
 */
@Component
public class GeneratorKeyUtil {

    @Autowired
    private DataSequenceService dataSequenceService;

    private ConcurrentHashMap<String, IdAlloc> idAllocConcurrentHashMap = new ConcurrentHashMap<>();

    /**
     * 根据业务编码，数量获取序列号
     *
     * @param businessCode 业务编码
     * @param number       数量
     * @return Long 起始位置
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized Long getSequenceByBusinessCode(String businessCode, int number) {
        if (StringUtils.isEmpty(businessCode)) {
            throw new DataSequenceException("业务编码不可为空");
        }
        if (number == MagicNumConstant.ZERO) {
            throw new DataSequenceException("需要获取的序列号长度不可为0或者空");
        }
        IdAlloc idAlloc = idAllocConcurrentHashMap.get(businessCode);
        if (ObjectUtil.isNull(idAlloc)) {
            idAlloc = new IdAlloc();
            idAllocConcurrentHashMap.put(businessCode, idAlloc);
        }

        if (idAlloc.getUsedNumber() == MagicNumConstant.ZERO) {
            DataSequence dataSequence = getDataSequence(businessCode);
            updateDataSequence(businessCode);
            idAlloc.setStartNumber(dataSequence.getStart());
            idAlloc.setEndNumber(dataSequence.getStart() + dataSequence.getStep() - MagicNumConstant.ONE);
            idAlloc.setUsedNumber(idAlloc.getEndNumber() - idAlloc.getStartNumber() + MagicNumConstant.ONE);
        }
        if (idAlloc.getUsedNumber() <= number) {
            expansionUsedNumber(businessCode, number);
        }
        long returnStartNumber = idAlloc.getStartNumber();
        idAlloc.setStartNumber(idAlloc.getStartNumber() + number);
        idAlloc.setUsedNumber(idAlloc.getUsedNumber() - number);
        return returnStartNumber;
    }

    /**
     * 根据业务编码获取配置信息
     * @param businessCode 业务编码
     * @return DataSequence 数据索引
     */
    private DataSequence getDataSequence(String businessCode) {
        DataSequence dataSequence = dataSequenceService.getSequence(businessCode);
        if (dataSequence == null || dataSequence.getStart() == null || dataSequence.getStep() == null) {
            throw new DataSequenceException("配置出错，请检查data_sequence表配置");
        }
        return dataSequence;
    }

    /**
     * 根据业务编码更新起始值
     * @param businessCode 业务编码
     */
    private void updateDataSequence(String businessCode) {
        dataSequenceService.updateSequenceStart(businessCode);
    }

    /**
     * 多次扩容
     * @param businessCode 业务编码
     * @param number 数量
     */
    private void expansionUsedNumber(String businessCode, int number) {
        IdAlloc idAlloc = idAllocConcurrentHashMap.get(businessCode);
        updateDataSequence(businessCode);
        DataSequence dataSequenceNew = getDataSequence(businessCode);
        idAlloc.setEndNumber(idAlloc.getEndNumber() + dataSequenceNew.getStep());
        idAlloc.setUsedNumber(idAlloc.getEndNumber() - idAlloc.getStartNumber() + MagicNumConstant.ONE);
        if (idAlloc.getUsedNumber() <= number) {
            expansionUsedNumber(businessCode, number);
        }
    }
}
