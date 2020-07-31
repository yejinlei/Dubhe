/**
 * Copyright 2019-2020 Zheng Jie
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
 */
package org.dubhe.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;
import org.dubhe.domain.entity.Dict;

import java.io.Serializable;
import java.util.List;

/**
 * @description  查询实体及关联对象
 * @date 2020-03-26
 */
public interface DictMapper extends BaseMapper<Dict> {
  /**
   * 查询实体及关联对象
   *
   * @param queryWrapper
   * @return
   */
  @Select("select * from dict ${ew.customSqlSegment}")
  @Results(id = "dictMapperResults",
    value = {
            @Result(property = "id", column = "id"),
            @Result(property = "dictDetails",
                    column = "id",
                    many = @Many(select = "org.dubhe.dao.DictDetailMapper.selectByDictId",
          fetchType = FetchType.LAZY))})
  List<Dict> selectCollList(@Param("ew") Wrapper<Dict> queryWrapper);

  /**
   * 分页查询实体及关联对象
   *
   * @param page
   * @param queryWrapper
   * @return
   */
  @Select("select * from dict ${ew.customSqlSegment}")
  @ResultMap(value = "dictMapperResults")
  IPage<Dict> selectCollPage(Page<Dict> page, @Param("ew") Wrapper<Dict> queryWrapper);

  /**
   * 根据ID查询实体及关联对象
   *
   * @param id
   * @return
   */
  @Select("select * from dict where id=#{id}")
  @ResultMap("dictMapperResults")
  Dict selectCollById(Serializable id);

  /**
   * 根据Name查询实体及关联对象
   *
   * @param name
   * @return
   */
  @Select("select * from dict where name=#{name}")
  @ResultMap("dictMapperResults")
  Dict selectCollByName(String name);
}
