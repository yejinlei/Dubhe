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

package org.dubhe.terminal.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.biz.base.annotation.ApiVersion;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.terminal.domain.dto.TerminalCreateDTO;
import org.dubhe.terminal.domain.dto.TerminalDTO;
import org.dubhe.terminal.domain.dto.TerminalPreserveDTO;
import org.dubhe.terminal.service.TerminalService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @description 专业版终端
 * @date 2021-07-12
 */
@Api(tags = "专业版：终端")
@RestController
@ApiVersion(1)
@RequestMapping("/terminals")
public class TerminalController {
    @Resource
    private TerminalService terminalService;

    @PostMapping("/create")
    @ApiOperation("创建")
    //@PreAuthorize(Permissions.TERMINAL_CREATE)
    public DataResponseBody create(@Validated @RequestBody TerminalCreateDTO terminalCreateDTO) {
        return new DataResponseBody(terminalService.create(terminalCreateDTO));
    }

    @PostMapping("/restart")
    @ApiOperation("重新启动")
    //@PreAuthorize(Permissions.TERMINAL_RESTART)
    public DataResponseBody restart(@Validated @RequestBody TerminalCreateDTO terminalCreateDTO) {
        return new DataResponseBody(terminalService.restart(terminalCreateDTO));
    }

    @PostMapping("/preserve")
    @ApiOperation("保存并停止")
    //@PreAuthorize(Permissions.TERMINAL_PRESAVE)
    public DataResponseBody preserve(@Validated @RequestBody TerminalPreserveDTO terminalPreserveDTO) {
        return new DataResponseBody(terminalService.preserve(terminalPreserveDTO));
    }

    @PostMapping("/delete")
    @ApiOperation("删除")
    //@PreAuthorize(Permissions.TERMINAL_DELETE)
    public DataResponseBody delete(@Validated @RequestBody TerminalDTO terminalDTO) {
        return new DataResponseBody(terminalService.delete(terminalDTO));
    }

    @GetMapping("/detail")
    @ApiOperation("根据terminalId查询详情")
    //@PreAuthorize(Permissions.TERMINAL_DETAIL)
    public DataResponseBody detail(@Validated TerminalDTO terminalDTO) {
        return new DataResponseBody(terminalService.detail(terminalDTO));
    }

    @GetMapping("/list")
    @ApiOperation("连接列表")
    //@PreAuthorize(Permissions.TERMINAL_LIST)
    public DataResponseBody list() {
        return new DataResponseBody(terminalService.list());
    }


}
