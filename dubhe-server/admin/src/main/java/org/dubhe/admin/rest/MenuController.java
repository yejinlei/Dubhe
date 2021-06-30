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
package org.dubhe.admin.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.admin.domain.dto.MenuCreateDTO;
import org.dubhe.admin.domain.dto.MenuDTO;
import org.dubhe.admin.domain.dto.MenuDeleteDTO;
import org.dubhe.admin.domain.dto.MenuQueryDTO;
import org.dubhe.admin.domain.dto.MenuUpdateDTO;
import org.dubhe.admin.domain.entity.Menu;
import org.dubhe.admin.service.MenuService;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @description 菜单管理 控制器
 * @date 2020-06-01
 */
@Api(tags = "系统：菜单管理")
@RestController
@RequestMapping("/menus")
@SuppressWarnings("unchecked")
public class MenuController {


    @Autowired
    private MenuService menuService;

    @ApiOperation("导出菜单数据")
    @GetMapping(value = "/download")
    @PreAuthorize(Permissions.MENU_DOWNLOAD)
    public void download(HttpServletResponse response, MenuQueryDTO criteria) throws IOException {
        menuService.download(menuService.queryAll(criteria), response);
    }

    @ApiOperation("返回全部的菜单")
    @GetMapping(value = "/tree")
    public DataResponseBody getMenuTree() {
        return new DataResponseBody(menuService.getMenuTree(menuService.findByPid(0L)));
    }

    @ApiOperation("查询菜单")
    @GetMapping
    @PreAuthorize(Permissions.MENU)
    public DataResponseBody getMenus(MenuQueryDTO criteria) {
        List<MenuDTO> menuDtoList = menuService.queryAll(criteria);
        return new DataResponseBody(menuService.buildTree(menuDtoList));
    }

    @ApiOperation("新增菜单")
    @PostMapping
    @PreAuthorize(Permissions.MENU_CREATE)
    public DataResponseBody create(@Valid @RequestBody MenuCreateDTO resources) {
        return new DataResponseBody(menuService.create(resources));
    }

    @ApiOperation("修改菜单")
    @PutMapping
    @PreAuthorize(Permissions.MENU_EDIT)
    public DataResponseBody update(@Valid @RequestBody MenuUpdateDTO resources) {
        menuService.update(resources);
        return new DataResponseBody();
    }

    @ApiOperation("删除菜单")
    @DeleteMapping
    @PreAuthorize(Permissions.MENU_DELETE)
    public DataResponseBody delete(@Valid @RequestBody MenuDeleteDTO deleteDTO) {
        Set<Menu> menuSet = new HashSet<>();
        Set<Long> ids = deleteDTO.getIds();
        for (Long id : ids) {
            List<Menu> menuList = menuService.findByPid(id);
            menuSet.add(menuService.findOne(id));
            menuSet = menuService.getDeleteMenus(menuList, menuSet);
        }
        menuService.delete(menuSet);
        return new DataResponseBody();
    }
}
