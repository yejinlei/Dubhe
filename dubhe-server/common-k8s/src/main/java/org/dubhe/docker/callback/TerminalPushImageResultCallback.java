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

package org.dubhe.docker.callback;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.model.PushResponseItem;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.docker.domain.dto.DockerPushCallbackDTO;
import org.dubhe.docker.utils.DockerCallbackTool;

import java.io.IOException;

/**
 * @description 镜像推送回调
 * @date 2021-07-22
 */
public class TerminalPushImageResultCallback extends ResultCallbackTemplate<TerminalPushImageResultCallback,PushResponseItem> {
    private Long terminalId;
    //回调地址
    private String url;

    private PushResponseItem latestItem = null;

    private DockerClient dockerClient;

    private Long userId;

    public TerminalPushImageResultCallback(){

    }

    public TerminalPushImageResultCallback(String url, Long terminalId, DockerClient dockerClient,Long userId){
        this.url = url;
        this.terminalId = terminalId;
        this.dockerClient = dockerClient;
        this.userId = userId;
    }

    @Override
    public void onNext(PushResponseItem item) {
        this.latestItem = item;
        LogUtil.info(LogEnum.TERMINAL,"push image item: {}",item.toString());
        if (item.getErrorDetail() != null){
            try {
                DockerCallbackTool.sendPushCallback(new DockerPushCallbackDTO(terminalId,item.getErrorDetail().getMessage(),true,userId),url, MagicNumConstant.THREE);
            } finally {
                try {
                    dockerClient.close();
                } catch (IOException e) {
                    LogUtil.error(LogEnum.TERMINAL,"push terminalId {} error:"+e.getMessage(),terminalId,e);
                    throw new BusinessException("push error:"+e.getMessage());
                }
            }
        }
    }

    @Override
    public void onError(Throwable throwable){
        super.onError(throwable);
        LogUtil.error(LogEnum.TERMINAL,"push image onError: {}",throwable.getMessage());
    }

    @Override
    public void onComplete(){
        super.onComplete();
        LogUtil.info(LogEnum.TERMINAL,"push image onComplete terminalId: {}",terminalId);
        try{
            DockerCallbackTool.sendPushCallback(new DockerPushCallbackDTO(terminalId,userId),url,MagicNumConstant.THREE);
        }finally {
            try {
                dockerClient.close();
            } catch (IOException e) {
                LogUtil.error(LogEnum.TERMINAL,"push terminalId {} error:"+e.getMessage(),terminalId,e);
                throw new BusinessException("push error:"+e.getMessage());
            }
        }
    }
}
