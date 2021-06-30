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
package org.dubhe.admin.rest;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.admin.domain.dto.TeamCreateDTO;
import org.dubhe.admin.domain.dto.TeamQueryDTO;
import org.dubhe.admin.domain.dto.TeamUpdateDTO;
import org.dubhe.admin.service.TeamService;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.constant.Permissions;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

/**
 * @description 团队管理 控制器
 * @date 2020-06-01
 */
@Api(tags = "系统：团队管理")
@ApiIgnore
@RestController
@RequestMapping("/teams")
public class TeamController {
  private final TeamService teamService;

  public TeamController(TeamService teamService) {
    this.teamService = teamService;
  }

  @ApiOperation("获取单个团队信息")
  @GetMapping(value = "/{id}")
  @PreAuthorize(Permissions.SYSTEM_TEAM)
  public DataResponseBody getTeam(@PathVariable Long id) {
    return new DataResponseBody(teamService.findById(id));
  }

  @ApiOperation("导出团队数据")
  @GetMapping(value = "/download")
  @PreAuthorize(Permissions.SYSTEM_TEAM)
  public void download(HttpServletResponse response, TeamQueryDTO criteria) throws IOException {
    teamService.download(teamService.queryAll(criteria), response);
  }

  @ApiOperation("返回全部的团队")
  @GetMapping(value = "/all")
  @PreAuthorize(Permissions.SYSTEM_TEAM)
  public DataResponseBody getAll(@PageableDefault(value = 2000, sort = {"level"}, direction = Sort.Direction.ASC) Page page) {
    return new DataResponseBody(teamService.queryAll(page));
  }

  @ApiOperation("查询团队")
  @GetMapping
  @PreAuthorize(Permissions.SYSTEM_TEAM)
  public DataResponseBody getTeams(TeamQueryDTO criteria, Page page) {
    System.out.println("com here");
    return new DataResponseBody(teamService.queryAll(criteria, page));
  }

  @ApiOperation("新增团队")
  @PostMapping
  @PreAuthorize(Permissions.SYSTEM_TEAM)
  public DataResponseBody create(@Valid @RequestBody TeamCreateDTO resources) {
    return new DataResponseBody(teamService.create(resources));
  }

  @ApiOperation("修改团队")
  @PutMapping
  @PreAuthorize(Permissions.SYSTEM_TEAM)
  public DataResponseBody update(@Valid @RequestBody TeamUpdateDTO resources) {
    teamService.update(resources);
    return new DataResponseBody();
  }
}
