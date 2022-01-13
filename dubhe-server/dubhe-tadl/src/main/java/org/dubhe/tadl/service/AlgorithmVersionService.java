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
package org.dubhe.tadl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.dubhe.tadl.domain.entity.AlgorithmVersion;

import java.util.List;

/**
 * @description 算法管理服务类
 * @date 2020-03-22
 */
public interface AlgorithmVersionService {

    /**
     * 创建算法写版本
     *
     * @param algorithmVersion 算法版本
     */
    void insert(AlgorithmVersion algorithmVersion);

    /**
     * 查询算法版本列表
     *
     * @param queryWrapper 查询条件
     * @return 算法版本列表
     */
    List<AlgorithmVersion> selectList(LambdaQueryWrapper<AlgorithmVersion> queryWrapper);

    /**
     * 通过算法版本id获取未删除的算法版本
     *
     * @param algorithmVersionId 算法版本ID
     * @return 算法版本
     */
    AlgorithmVersion selectOneById(Long algorithmVersionId);

    /**
     * 通过算法版本id获取已删除或者未删除的算法版本
     * @param algorithmVersionId 算法版本ID
     * @return 算法版本
     */
    AlgorithmVersion getOneById(Long algorithmVersionId);

    /**
     * 获取指定算法当前使用最大版本号
     *
     * @param algorithmId     数据集ID
     * @return String         指定算法当前使用最大版本号
     */
    String getMaxVersionName(Long algorithmId);

    /**
     * 更新算法版本信息
     *
     * @param algorithmVersion     算法版本对象
     * @return 更新对象id
     */
    void updateAlgorithmVersionById(AlgorithmVersion algorithmVersion);

    /**
     * 更新算法版本
     * @param updateWrapper 变更算法版本条件
     */
    void updateAlgorithmVersion(LambdaUpdateWrapper<AlgorithmVersion> updateWrapper);

    /**
     * 根据条件查询算法版本
     * @param queryWrapper 查询条件
     * @return 算法版本
     */
    AlgorithmVersion selectOne(LambdaQueryWrapper<AlgorithmVersion> queryWrapper);

    /**
     * 变更算法
     * @param id 算法版本id
     * @param deleted 删除状态
     * @return 变更数量
     */
    int updateAlgorithmVersionStatus(Long id, Boolean deleted);

    /**
     * 删除指定算法下所有数据
     * @param algorithmId 算法id
     * @return 变更数量
     */
    int deleteByAlgorithmId(Long algorithmId);
}
