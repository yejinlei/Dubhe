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
package org.dubhe.tadl.rest;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.tadl.constant.TadlConstant;
import org.dubhe.tadl.domain.dto.*;
import org.dubhe.tadl.service.AlgorithmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @description 算法管理
 * @date 2021-03-22
 */
@Api(tags = "TADL：算法管理")
@RestController
@RequestMapping(TadlConstant.MODULE_URL_PREFIX + "/algorithm")
public class AlgorithmController {

    /**
     * 算法服务实现类
     */
    @Autowired
    private AlgorithmService algorithmService;


    @ApiOperation(value = "解压zip包")
    @GetMapping(value = "/unzip")
    public DataResponseBody unzip(@RequestParam(value = "zipPath") String zipPath) {
        algorithmService.unzip(zipPath);
        return new DataResponseBody();
    }

    @ApiOperation(value = "复制算法")
    @GetMapping(value = "/copy")
    public DataResponseBody copyAlgorithm(@RequestParam(value = "zipPath") String zipPath) {
        algorithmService.copyAlgorithm(zipPath);
        return new DataResponseBody();
    }

    @ApiOperation(value = "获取yaml")
    @GetMapping(value = "/yaml")
    public DataResponseBody readYaml(@Valid AlgorithmYamlQueryDTO algorithmYamlQueryDTO) {
        return new DataResponseBody(algorithmService.readYaml(algorithmYamlQueryDTO));
    }

    @ApiOperation(value = "上传算法")
    @PostMapping(value = "/upload")
    public DataResponseBody create(@Valid @RequestBody AlgorithmCreateDTO algorithmCreateDTO) {
        algorithmService.create(algorithmCreateDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "算法列表")
    @GetMapping(value = "/query")
    public DataResponseBody query(@RequestParam(value = "content", required = false) String content) {
        return new DataResponseBody(algorithmService.query(content));
    }

    @ApiOperation(value = "编辑算法")
    @PostMapping(value = "/update")
    public DataResponseBody update(@Valid @RequestBody AlgorithmUpdateDTO algorithmUpdateDTO) {
        algorithmService.update(algorithmUpdateDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "删除算法")
    @DeleteMapping
    public DataResponseBody delete(@Valid @RequestBody AlgorithmVersionDeleteDTO algorithmVersionDeleteDTO) {
        algorithmService.delete(algorithmVersionDeleteDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "算法详情")
    @GetMapping(value = "/{algorithmId}/query")
    public DataResponseBody query(@PathVariable("algorithmId") Integer algorithmId,
                                  @RequestParam(value = "stageOrder", required = false) Integer stageOrder,
                                  @RequestParam(value = "algorithmVersionId") Long algorithmVersionId) {
        return new DataResponseBody(algorithmService.query(algorithmId,stageOrder,algorithmVersionId));
    }

    @ApiOperation(value = "获取即将要发布的版本号")
    @GetMapping(value = "/{algorithmId}/next/version")
    public DataResponseBody getNextVersionName(@PathVariable("algorithmId") Long algorithmId) {
        return new DataResponseBody(algorithmService.getNextVersionName(algorithmId));
    }

    @ApiOperation(value = "发布搜索策略")
    @PostMapping(value = "/push/version")
    public DataResponseBody publish(@Valid @RequestBody AlgorithmVersionCreateDTO algorithmVersionCreateDTO) {
        algorithmService.publish(algorithmVersionCreateDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "算法版本切换")
    @PutMapping(value = "/version/switch")
    public DataResponseBody versionSwitch(@Valid @RequestBody AlgorithmVersionSwitchDTO algorithmVersionSwitchDTO) {
        algorithmService.versionSwitch(algorithmVersionSwitchDTO.getAlgorithmId(),algorithmVersionSwitchDTO.getAlgorithmVersionId());
        return new DataResponseBody();
    }
}
