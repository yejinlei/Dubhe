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

package org.dubhe.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.domain.dto.TeamCreateDTO;
import org.dubhe.domain.dto.TeamDTO;
import org.dubhe.domain.dto.TeamQueryDTO;
import org.dubhe.domain.dto.TeamUpdateDTO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @Description : 团队服务 Service
 * @Date 2020-06-01
 */
public interface TeamService {

  /**
   * 查询所有数据
   *
   * @param criteria 条件
   * @return /
   */
  List<TeamDTO> queryAll(TeamQueryDTO criteria);

  /**
   * 不带条件分页查询
   *
   * @param page 分页参数
   * @return /
   */
  List<TeamDTO> queryAll(Page page);

  /**
   * 待条件分页查询
   *
   * @param criteria 条件
   * @param page     分页参数
   * @return /
   */
  Object queryAll(TeamQueryDTO criteria, Page page);

  /**
   * 根据ID查询
   *
   * @param id /
   * @return /
   */
  TeamDTO findById(Long id);

  /**
   * 创建
   *
   * @param resources /
   * @return /
   */
  TeamDTO create(TeamCreateDTO resources);

  /**
   * 编辑
   *
   * @param resources /
   */
  void update(TeamUpdateDTO resources);

  /**
   * 删除
   *
   * @param deptDtos /
   */
  void delete(Set<TeamDTO> deptDtos);



  /**
   * 导出数据
   *
   * @param queryAll 待导出的数据
   * @param response /
   * @throws IOException /
   */
  void download(List<TeamDTO> queryAll, HttpServletResponse response) throws IOException;

}
