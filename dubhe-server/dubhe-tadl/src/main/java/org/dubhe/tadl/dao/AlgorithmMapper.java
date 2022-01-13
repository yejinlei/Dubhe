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
package org.dubhe.tadl.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dubhe.tadl.domain.entity.Algorithm;

/**
 * @description 算法Mapper
 * @date 2021-03-10
 */
public interface AlgorithmMapper extends BaseMapper<Algorithm> {

    /**
     * 通过id查询算法
     * @param id 算法id
     * @return Algorithm
     */
    @Select("select * from tadl_algorithm where id = #{id}")
    Algorithm getOneById(@Param("id") Long id);

}
