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
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.tadl.dao.AlgorithmStageMapper;
import org.dubhe.tadl.domain.entity.AlgorithmStage;
import org.dubhe.tadl.enums.TadlErrorEnum;
import org.dubhe.tadl.service.AlgorithmStageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description 算法管理阶段服务实现类
 * @date 2021-03-22
 */
@Service
public class AlgorithmStageServiceImpl extends ServiceImpl<AlgorithmStageMapper, AlgorithmStage> implements AlgorithmStageService {

    @Resource
    private AlgorithmStageMapper algorithmStageMapper;
    /**
     * 创建算法写算法阶段
     *
     * @param algorithmStage 算法阶段
     */
    @Override
    public void insertStages(List<AlgorithmStage> algorithmStage) {
        algorithmStage.forEach(baseMapper::insert);
    }

    /**
     * 根据条件查询阶段列表
     *
     * @param wrapper 条件
     * @return 阶段列表
     */
    @Override
    public List<AlgorithmStage> selectList(LambdaQueryWrapper<AlgorithmStage> wrapper) {
        return baseMapper.selectList(wrapper);
    }

    /**
     * 根据ID查询实验阶段对象
     *
     * @param stageId 实验阶段ID
     * @return 实验阶段对象
     */
    @Override
    public AlgorithmStage selectOneById(Long stageId) {
        return baseMapper.selectById(stageId);
    }

    /**
     * 更新实验阶段
     *
     * @param algorithmStage 实验阶段
     */
    @Override
    public void updateAlgorithmStage(List<AlgorithmStage> algorithmStage) {
        algorithmStage.forEach(stage -> baseMapper.update(stage, new LambdaUpdateWrapper<AlgorithmStage>() {{
                    eq(AlgorithmStage::getId,stage.getId());
                    eq(AlgorithmStage::getStageOrder,stage.getStageOrder());
                    eq(AlgorithmStage::getName,stage.getName());
                    eq(AlgorithmStage::getAlgorithmId,stage.getAlgorithmId());
                    eq(AlgorithmStage::getAlgorithmVersionId,stage.getAlgorithmVersionId());
                }})
        );
    }



    @Override
    public void updateAlgorithmStage(LambdaUpdateWrapper<AlgorithmStage> updateWrapper) {
        baseMapper.update(null,updateWrapper);
    }

    @Override
    public int updateStageStatusByVersionId(Long versionId, Boolean deleted) {
        return algorithmStageMapper.updateStageStatusByVersionId(versionId,deleted);
    }

    @Override
    public int deleteByAlgorithmId(Long algorithmId) {
        return algorithmStageMapper.delete(new LambdaUpdateWrapper<AlgorithmStage>(){{
            eq(AlgorithmStage::getAlgorithmId,algorithmId);
        }});
    }

    @Override
    public AlgorithmStage getOneById(Long algorithmStageId) {
        AlgorithmStage algorithmStage = algorithmStageMapper.getOneById(algorithmStageId);
        if (algorithmStage == null){
            throw new BusinessException(TadlErrorEnum.ALGORITHM_STAGE_DOES_NOT_EXIST_ERROR);
        }
        return  algorithmStage;
    }
}
