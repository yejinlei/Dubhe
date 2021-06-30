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

package org.dubhe.data.util;

import cn.hutool.core.util.ObjectUtil;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.exception.DataSequenceException;
import org.dubhe.data.domain.entity.DataSequence;
import org.dubhe.data.service.DataSequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Queue;
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
    public synchronized Queue<Long> getSequenceByBusinessCode(String businessCode, int number) {
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

        if (idAlloc.getUnUsed() < number) {
            //执行扩容操作
            expansionUsedNumber(businessCode, number);
        }
        //获取ids
        return idAlloc.poll(number);
    }

    /**
     * 扩容
     * @param businessCode 业务编码
     * @param number 数量
     */
    protected void expansionUsedNumber(String businessCode, int number) {
        IdAlloc idAlloc = idAllocConcurrentHashMap.get(businessCode);
        DataSequence dataSequenceNew = dataSequenceService.expansionUsedNumber(businessCode);
        idAlloc.add(dataSequenceNew);
        if(idAlloc.getUnUsed() < number) {
            expansionUsedNumber(businessCode, number);
        }
    }


}
