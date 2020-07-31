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
package org.dubhe.rest;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.dubhe.base.DataResponseBody;
import org.dubhe.constant.Permissions;
import org.dubhe.domain.dto.DictCreateDTO;
import org.dubhe.domain.dto.DictDeleteDTO;
import org.dubhe.domain.dto.DictQueryDTO;
import org.dubhe.domain.dto.DictUpdateDTO;
import org.dubhe.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

/**
 * @Description 字典管理 控制器
 * @Date 2020-06-01
 */
@Api(tags = "系统：字典管理")
@RestController
@RequestMapping("/api/{version}/dict")
public class DictController {

  private static final String ENTITY_NAME = "dict";
  @Autowired
  private DictService dictService;


  @ApiOperation("导出字典数据")
  @GetMapping(value = "/download")
  @RequiresPermissions(Permissions.SYSTEM_DICT)
  public void download(HttpServletResponse response, DictQueryDTO criteria) throws IOException {
    dictService.download(dictService.queryAll(criteria), response);
  }

  @ApiOperation("查询字典")
  @GetMapping(value = "/all")
  @RequiresPermissions(Permissions.SYSTEM_DICT)
  public DataResponseBody all() {
    return new DataResponseBody(dictService.queryAll(new DictQueryDTO()));
  }

  @ApiOperation("查询字典")
  @GetMapping
  @RequiresPermissions(Permissions.SYSTEM_DICT)
  public DataResponseBody getDicts(DictQueryDTO resources, Page page) {
    return new DataResponseBody(dictService.queryAll(resources, page));
  }

  @ApiOperation("新增字典")
  @PostMapping
  @RequiresPermissions(Permissions.SYSTEM_DICT)
  public DataResponseBody create(@Valid @RequestBody DictCreateDTO resources) {
    return new DataResponseBody(dictService.create(resources));
  }

  @ApiOperation("修改字典")
  @PutMapping
  @RequiresPermissions(Permissions.SYSTEM_DICT)
  public DataResponseBody update(@Valid @RequestBody DictUpdateDTO resources) {
    dictService.update(resources);
    return new DataResponseBody();
  }

  @ApiOperation("批量删除字典")
  @DeleteMapping
  @RequiresPermissions(Permissions.SYSTEM_DICT)
  public DataResponseBody delete(@RequestBody DictDeleteDTO dto) {
    dictService.deleteAll(dto.getIds());
    return new DataResponseBody();
  }
}
