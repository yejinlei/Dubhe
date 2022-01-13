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
package org.dubhe.tadl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.tadl.dao.TrialDataMapper;
import org.dubhe.tadl.domain.entity.TrialData;
import org.dubhe.tadl.service.TrialDataService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description 试验数据服务类
 * @date 2020-03-22
 */
@Service
public class TrialDataServiceImpl extends ServiceImpl<TrialDataMapper, TrialData> implements TrialDataService {

    /**
     * 获取 trial 列表
     *
     * @param wrapper 查询条件
     * @return tarial 数据列表
     */
    @Override
    public List<TrialData> getTrialDataList(LambdaQueryWrapper<TrialData> wrapper){
        return baseMapper.selectList(wrapper);
    }

    /**
     *  根据trialId获取trial data
     *
     * @param trialId trialId
     * @return
     */
    @Override
    public TrialData selectOneByTrialId(Long trialId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<TrialData>()
             .eq(TrialData::getTrialId,trialId)
        );
    }

    /**
     * 删除 trial data
     *
     * @param wrapper 删除实验 wrapper
     */
    @Override
    public void delete(LambdaQueryWrapper<TrialData> wrapper){
        baseMapper.delete(wrapper);
    }

    /**
     * 批量写入trial data
     *
     * @param trialDataList trial 列表
     */
    @Override
    public void insertList(List<TrialData> trialDataList) {
        baseMapper.saveList(trialDataList);
    }

}
