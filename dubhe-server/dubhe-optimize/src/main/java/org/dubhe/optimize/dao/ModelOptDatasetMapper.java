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
package org.dubhe.optimize.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.dubhe.optimize.domain.entity.ModelOptDataset;

import java.util.List;

/**
 * @description 模型优化数据集
 * @date 2021-01-06
 */
public interface ModelOptDatasetMapper extends BaseMapper<ModelOptDataset> {

    /**
     *  获取所有数据集
     * @return List<ModelOptDataset> 数据集集合
     */
    @Select("select id, name, path from model_opt_dataset where deleted = 0 order by id desc")
    List<ModelOptDataset> getAllDataset();
}
