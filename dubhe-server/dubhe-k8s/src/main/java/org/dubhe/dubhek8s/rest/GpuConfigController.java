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
package org.dubhe.dubhek8s.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.biz.base.dto.GpuConfigDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.dubhek8s.service.GpuConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description k8s GPU配置
 * @date 2021-9-6
 */
@Api(tags = "k8s GPU配置管理")
@RestController
@RequestMapping("/gpuConfig")
public class GpuConfigController {

    @Autowired
    GpuConfigService gpuConfigService;

    @ApiOperation("通过用户 ID 更新用户配置(用于 admin 模块内部调用)")
    @PostMapping(value = "update")
    public DataResponseBody updateGpuConfig(@RequestBody GpuConfigDTO gpuConfigDTO) {
        gpuConfigService.UpdateGpuConfig(gpuConfigDTO);
        return new DataResponseBody();
    }
}
