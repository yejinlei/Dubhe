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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.admin.domain.dto.LogQueryDTO;
import org.dubhe.admin.service.LogService;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.cloud.authconfig.utils.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @description 日志管理 控制器
 * @date 2020-06-01
 */
@Api(tags = "监控：日志管理")
@ApiIgnore
@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize(Permissions.SYSTEM_LOG)
    public void download(HttpServletResponse response, LogQueryDTO criteria) throws IOException {
        criteria.setLogType("INFO");
        logService.download(logService.queryAll(criteria), response);
    }

    @ApiOperation("导出错误数据")
    @GetMapping(value = "/error/download")
    @PreAuthorize(Permissions.SYSTEM_LOG)
    public void errorDownload(HttpServletResponse response, LogQueryDTO criteria) throws IOException {
        criteria.setLogType("ERROR");
        logService.download(logService.queryAll(criteria), response);
    }

    @GetMapping
    @ApiOperation("日志查询")
    @PreAuthorize(Permissions.SYSTEM_LOG)
    public ResponseEntity<Object> getLogs(LogQueryDTO criteria, Page pageable) {
        criteria.setLogType("INFO");
        return new ResponseEntity<>(logService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @GetMapping(value = "/user")
    @ApiOperation("用户日志查询")
    public ResponseEntity<Object> getUserLogs(LogQueryDTO criteria, Page page) {
        criteria.setLogType("INFO");
        criteria.setBlurry(JwtUtils.getCurUser().getUsername());
        return new ResponseEntity<>(logService.queryAllByUser(criteria, page), HttpStatus.OK);
    }

    @GetMapping(value = "/error")
    @ApiOperation("错误日志查询")
    @PreAuthorize(Permissions.SYSTEM_LOG)
    public ResponseEntity<Object> getErrorLogs(LogQueryDTO criteria, Page page) {
        criteria.setLogType("ERROR");
        return new ResponseEntity<>(logService.queryAll(criteria, page), HttpStatus.OK);
    }

    @GetMapping(value = "/error/{id}")
    @ApiOperation("日志异常详情查询")
    @PreAuthorize(Permissions.SYSTEM_LOG)
    public ResponseEntity<Object> getErrorLogs(@PathVariable Long id) {
        return new ResponseEntity<>(logService.findByErrDetail(id), HttpStatus.OK);
    }

    @DeleteMapping(value = "/del/error")
    @ApiOperation("删除所有ERROR日志")
    @PreAuthorize(Permissions.SYSTEM_LOG)
    public ResponseEntity<Object> delAllByError() {
        logService.delAllByError();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(value = "/del/info")
    @ApiOperation("删除所有INFO日志")
    @PreAuthorize(Permissions.SYSTEM_LOG)
    public ResponseEntity<Object> delAllByInfo() {
        logService.delAllByInfo();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
