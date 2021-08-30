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
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;
import org.dubhe.docker.constant.DockerCallbackConstant;
import org.dubhe.docker.domain.dto.DockerPushCallbackDTO;
import org.dubhe.terminal.service.TerminalService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description
 * @date 2021-07-27
 */
@Api(tags = "docker 回调")
@RestController
@RequestMapping(DockerCallbackConstant.DOCKER_CALLBACK_URI)
public class DockerCallbackController {

    @Resource
    private TerminalService terminalService;

    @PostMapping("/push")
    @ApiOperation("推送镜像失败")
    //@PreAuthorize(Permissions.TERMINAL_CREATE)
    public DataResponseBody pushImageError(@Validated @RequestBody DockerPushCallbackDTO dockerPushCallbackDTO) {
        if (dockerPushCallbackDTO.isError()){
            terminalService.pushImageError(dockerPushCallbackDTO.getTerminalId(),dockerPushCallbackDTO.getErrorMessage(),dockerPushCallbackDTO.getUserId());
        }else {
            terminalService.pushImageComplete(dockerPushCallbackDTO.getTerminalId(),dockerPushCallbackDTO.getUserId());
        }

        return DataResponseFactory.success("docker回调处理中");
    }
}
