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
package org.dubhe.model.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.model.domain.dto.PtModelSuffixDTO;
import org.dubhe.model.service.PtModelSuffixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description 模型后缀名管理
 * @date 2021-04-26
 */
@Api(tags = "模型管理：模型后缀名管理")
@RestController
@RequestMapping("/ptModelSuffix")
public class PtModelSuffixController {

    @Autowired
    private PtModelSuffixService ptModelSuffixService;

    @GetMapping
    @ApiOperation("查询模型后缀名")
    public DataResponseBody getModelSuffix(@Validated PtModelSuffixDTO ptModelSuffixDTO) {
        return new DataResponseBody(ptModelSuffixService.getModelSuffix(ptModelSuffixDTO));
    }
}