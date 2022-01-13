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
import org.dubhe.tadl.dao.AlgorithmVersionMapper;
import org.dubhe.tadl.domain.entity.AlgorithmVersion;
import org.dubhe.tadl.enums.TadlErrorEnum;
import org.dubhe.tadl.service.AlgorithmVersionService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description 算法版本服务实现类
 * @date 2021-03-22
 */
@Service
public class AlgorithmVersionServiceImpl extends ServiceImpl<AlgorithmVersionMapper, AlgorithmVersion> implements AlgorithmVersionService{

    @Resource
    private AlgorithmVersionMapper algorithmVersionMapper;
    /**
     * 创建算法写版本
     *
     * @param algorithmVersion 算法版本
     */
    @Override
    public void insert(AlgorithmVersion algorithmVersion) {
        baseMapper.insert(algorithmVersion);
    }

    /**
     * 查询算法版本列表
     *
     * @param queryWrapper 查询条件
     * @return
     */
    @Override
    public List<AlgorithmVersion> selectList(LambdaQueryWrapper<AlgorithmVersion> queryWrapper) {
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 查询算法版本
     *
     * @param algorithmVersionId 算法版本ID
     * @return 算法版本
     */
    @Override
    public AlgorithmVersion selectOneById(Long algorithmVersionId) {
        AlgorithmVersion algorithmVersion = baseMapper.selectById(algorithmVersionId);
        if (ObjectUtils.isEmpty(algorithmVersion)) {
            throw new BusinessException(TadlErrorEnum.ALGORITHM_VERSION_DOES_NOT_EXIST_ERROR);
        }
        return algorithmVersion;
    }

    @Override
    public AlgorithmVersion getOneById(Long algorithmVersionId) {
        AlgorithmVersion algorithmVersion = algorithmVersionMapper.getOneById(algorithmVersionId);
        if (ObjectUtils.isEmpty(algorithmVersion)) {
            throw new BusinessException(TadlErrorEnum.ALGORITHM_VERSION_DOES_NOT_EXIST_ERROR);
        }
        return algorithmVersion;
    }

    /**
     * 获取指定算法当前使用最大版本号
     *
     * @param algorithmId     数据集ID
     * @return String         指定算法当前使用最大版本号
     */
    @Override
    public String getMaxVersionName(Long algorithmId){
        return baseMapper.getMaxVersionName(algorithmId);
    }

    /**
     * 更新算法版本信息
     *
     * @param algorithmVersion     算法版本对象
     * @return 更新对象id
     */
    @Override
    public void updateAlgorithmVersionById(AlgorithmVersion algorithmVersion) {
        baseMapper.updateById(algorithmVersion);
    }

    @Override
    public void updateAlgorithmVersion(LambdaUpdateWrapper<AlgorithmVersion> updateWrapper) {
        baseMapper.update(null,updateWrapper);
    }

    @Override
    public AlgorithmVersion selectOne(LambdaQueryWrapper<AlgorithmVersion> queryWrapper) {
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public int updateAlgorithmVersionStatus(Long id, Boolean deleted) {
        return algorithmVersionMapper.updateAlgorithmVersionStatus(id,deleted);
    }

    @Override
    public int deleteByAlgorithmId(Long algorithmId) {
        return algorithmVersionMapper.delete(new LambdaUpdateWrapper<AlgorithmVersion>(){{
            eq(AlgorithmVersion::getAlgorithmId,algorithmId);
        }});
    }
}
