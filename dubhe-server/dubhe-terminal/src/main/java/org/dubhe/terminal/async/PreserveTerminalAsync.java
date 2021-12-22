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

package org.dubhe.terminal.async;

import com.github.dockerjava.api.DockerClient;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.docker.api.DockerApi;
import org.dubhe.docker.callback.TerminalPushImageResultCallback;
import org.dubhe.docker.enums.DockerOperationEnum;
import org.dubhe.docker.utils.DockerCallbackTool;
import org.dubhe.terminal.config.TerminalConfig;
import org.dubhe.terminal.dao.TerminalMapper;
import org.dubhe.terminal.domain.dto.TerminalPreserveDTO;
import org.dubhe.terminal.domain.entity.Terminal;
import org.dubhe.terminal.enums.TerminalStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @description 保存终端异步任务
 * @date 2021-09-24
 */
@Component
public class PreserveTerminalAsync {
    @Autowired
    private DockerApi dockerApi;

    @Autowired
    private TerminalMapper terminalMapper;

    @Autowired
    private TerminalConfig terminalConfig;

    @Autowired
    private DockerCallbackTool dockerCallbackTool;

    /**
     * 提交并推送镜像
     *
     * @param dockerClient
     * @param containerID
     * @param newImageRepository
     * @param terminalPreserveDTO
     * @param terminal
     */
    @Async("terminalExecutor")
    public void commitAndPush(DockerClient dockerClient, String containerID, String newImageRepository, TerminalPreserveDTO terminalPreserveDTO, Terminal terminal) {
        try {
            LogUtil.info(LogEnum.TERMINAL,"commitAndPush containerID {} newImageRepository {} terminalPreserveDTO {} terminal {}",containerID,newImageRepository,terminalPreserveDTO,terminal);
            dockerApi.commit(dockerClient,containerID,newImageRepository,terminalPreserveDTO.getImageTag());
            terminal.setStatus(null);
            terminal.putStatusDetail(TerminalStatusEnum.SAVING.getDescription(),"push 镜像...");
            terminalMapper.updateById(terminal);
            boolean pushResult = dockerApi.push(dockerClient,newImageRepository+ SymbolConstant.COLON+terminalPreserveDTO.getImageTag(),new TerminalPushImageResultCallback(dockerCallbackTool.getCallbackUrl(SymbolConstant.LOCAL_HOST,terminalConfig.getServerPort(), DockerOperationEnum.PUSH.getType()),terminal.getId(),dockerClient,terminal.getCreateUserId()));
            if (!pushResult){
                LogUtil.error(LogEnum.TERMINAL,"master 推送镜像错误 terminalPreserveDTO:{}",terminalPreserveDTO);
                throw new BusinessException("推送镜像错误:");
            }
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL,"master 保存容器错误:{}",e.getMessage(),e);
            throw new BusinessException("保存容器错误:"+e.getMessage());
        }
    }
}
