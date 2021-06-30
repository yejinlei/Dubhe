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

package org.dubhe.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.admin.domain.dto.TeamCreateDTO;
import org.dubhe.biz.base.dto.TeamDTO;
import org.dubhe.admin.domain.dto.TeamQueryDTO;
import org.dubhe.admin.domain.dto.TeamUpdateDTO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @description  团队服务 Service
 * @date 2020-06-01
 */
public interface TeamService {

  /**
   * 获取团队列表
   *
   * @param criteria 查询团队列表条件
   * @return java.util.List<org.dubhe.domain.dto.TeamDTO> 团队列表条件
   */
  List<TeamDTO> queryAll(TeamQueryDTO criteria);

  /**
   * 查询团队列表
   *
   * @param page 分页请求实体
   * @return java.util.List<org.dubhe.domain.dto.TeamDTO> 团队列表
   */
  List<TeamDTO> queryAll(Page page);

  /**
   * 分页查询团队列表
   *
   * @param criteria 查询请求条件
   * @param page     分页实体
   * @return java.lang.Object 团队列表
   */
  Object queryAll(TeamQueryDTO criteria, Page page);

  /**
   * 根据ID插叙团队信息
   *
   * @param id id
   * @return org.dubhe.domain.dto.TeamDTO 团队返回实例
   */
  TeamDTO findById(Long id);

  /**
   * 新增团队信息
   *
   * @param resources 团队新增请求实体
   * @return org.dubhe.domain.dto.TeamDTO 团队返回实例
   */
  TeamDTO create(TeamCreateDTO resources);

  /**
   * 修改团队
   *
   * @param resources 团队修改请求实体
   */
  void update(TeamUpdateDTO resources);

  /**
   * 团队删除
   *
   * @param deptDtos 团队删除列表
   */
  void delete(Set<TeamDTO> deptDtos);



  /**
   * 团队信息导出
   *
   * @param teamDtos 团队列表
   * @param response 导出http响应
   */
  void download(List<TeamDTO> teamDtos, HttpServletResponse response) throws IOException;

}
