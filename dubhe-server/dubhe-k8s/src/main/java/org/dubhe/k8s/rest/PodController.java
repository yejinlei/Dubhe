/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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
package org.dubhe.k8s.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.base.DataResponseBody;
import org.dubhe.factory.DataResponseFactory;
import org.dubhe.k8s.domain.dto.PodLogDownloadQueryDTO;
import org.dubhe.k8s.domain.dto.PodLogQueryDTO;
import org.dubhe.k8s.domain.dto.PodQueryDTO;
import org.dubhe.k8s.service.PodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @description Pod信息Rest API
 * @date 2020-08-14
 */
@Api(tags = "k8s：Pod")
@RestController
@RequestMapping("/api/{version}/pod")
public class PodController {

    @Autowired
    private PodService podService;

    @GetMapping
    @ApiOperation("获取pod节点")
    public DataResponseBody getPods(@Validated PodQueryDTO podQueryDTO) {
        return DataResponseFactory.success(podService.getPods(podQueryDTO));
    }

    @GetMapping("/log")
    @ApiOperation("pod日志查询")
    public DataResponseBody getPodLog(@Validated PodLogQueryDTO podLogQueryDTO) {
        return DataResponseFactory.success(podService.getPodLog(podLogQueryDTO));
    }

    @GetMapping("/log/download")
    @ApiOperation("单pod日志下载")
    public DataResponseBody downLoadPodLog(@Validated PodLogQueryDTO podLogQueryDTO) {
        return DataResponseFactory.success(podService.getPodLogStr(podLogQueryDTO));
    }

    @PostMapping("/log/download")
    @ApiOperation("自选pod日志下载")
    public DataResponseBody downLoadPodLog(@Validated @RequestBody PodLogDownloadQueryDTO podLogDownloadQueryDTO, HttpServletResponse response) {
        podService.downLoadPodLog(podLogDownloadQueryDTO,response);
        return DataResponseFactory.success();
    }

    @PostMapping("/log/count")
    @ApiOperation("统计Pod日志数量")
    public DataResponseBody getLogCount(@Validated @RequestBody PodLogDownloadQueryDTO podLogDownloadQueryDTO) {
        return DataResponseFactory.success(podService.getLogCount(podLogDownloadQueryDTO));
    }


}
