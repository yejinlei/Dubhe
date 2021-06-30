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

package org.dubhe.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;
import org.dubhe.admin.dao.provider.TeamProvider;
import org.dubhe.admin.domain.entity.Team;

import java.io.Serializable;
import java.util.List;

/**
 * @description  团队 mapper
 * @date 2020-03-25
 */
public interface TeamMapper extends BaseMapper<Team> {

  /**
   * 根据ID查询名称
   *
   * @param userId  用户id
   * @return List<Team> 团队列表
   */
  @SelectProvider(type = TeamProvider.class, method = "findByUserId")
  List<Team> findByUserId(Long userId);


  /**
   * 根据ID查询团队实体及关联对象
   *
   * @param id 序列id
   * @return 团队
   */
  @Select("select * from team where id=#{id}")
  @Results(id = "teamMapperResults",
    value = {
      @Result(column = "id", property = "teamUserList",
        many = @Many(select = "org.dubhe.admin.dao.UserMapper.findByTeamId",
          fetchType = FetchType.LAZY))})
  Team selectCollById(Serializable id);
}
