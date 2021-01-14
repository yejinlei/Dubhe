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

package org.dubhe.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.dubhe.domain.entity.ModelOptBuiltIn;
import org.dubhe.domain.vo.ModelOptAlgorithmQueryVO;
import org.dubhe.domain.vo.ModelOptDatasetQueryVO;
import org.dubhe.domain.vo.ModelOptModelQueryVO;

import java.util.List;

/**
 * @description 内置模型
 * @date 2020-05-22
 */
public interface ModelOptBuiltInMapper extends BaseMapper<ModelOptBuiltIn> {
    /**
     * 获取模型优化数据集
     * @param type 算法类型
     * @return 模型优化数据集列表
     */
    List<ModelOptDatasetQueryVO> getDataset(@Param("type") Integer type, @Param("model") String model, @Param("algorithm") String algorithm);

    /**
     * 获取模型优化教师模型
     * @param type
     * @return
     */
    List<ModelOptModelQueryVO> getModel(@Param("type") Integer type, @Param("dataset") String dataset, @Param("algorithm") String algorithm);

    /**
     * 获取模型优化算法
     * @param type
     * @return
     */
    List<ModelOptAlgorithmQueryVO> getAlgorithm(@Param("type") Integer type, @Param("model") String model, @Param("dataset") String dataset);

}
