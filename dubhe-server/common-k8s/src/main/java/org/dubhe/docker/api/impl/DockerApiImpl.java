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

package org.dubhe.docker.api.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.command.CommitCmd;
import com.github.dockerjava.api.model.AuthConfig;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.docker.api.DockerApi;
import org.dubhe.docker.config.DubheDockerJavaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description docker api实现类
 * @date 2021-07-06
 */
@Service
public class DockerApiImpl implements DockerApi {
    @Autowired
    private DubheDockerJavaConfig dubheDockerJavaConfig;
    /**
     * 非强制删除镜像
     *
     * @param dockerClient docker连接
     * @param image repository:tag
     * @return boolean 成功true，失败false
     */
    @Override
    public boolean removeImage(DockerClient dockerClient, String image) {
        LogUtil.info(LogEnum.TERMINAL, "DockerApiImpl removeImage image:{}",image);
        try{
            dockerClient.removeImageCmd(image).withForce(false).exec();
            return true;
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL, "DockerApiImpl removeImage error:{}",e.getMessage(), e);
            return false;
        }
    }

    /**
     * 删除镜像
     *
     * @param dockerClient docker连接
     * @param image repository:tag
     * @param force true:强制删除 false:非强制
     * @return boolean 成功true，失败false
     */
    @Override
    public boolean removeImage(DockerClient dockerClient, String image, boolean force) {
        LogUtil.info(LogEnum.TERMINAL, "DockerApiImpl removeImage image:{} force:{}",image,force);
        try{
            dockerClient.removeImageCmd(image).withForce(force).exec();
            return true;
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL, "DockerApiImpl removeImage error:{}",e.getMessage(), e);
            return false;
        }
    }

    /**
     * docker commit
     *
     * @param dockerClient docker连接
     * @param containerId 容器id
     * @param repository 仓库
     * @param tag 标签
     * @return
     */
    @Override
    public String commit(DockerClient dockerClient, String containerId, String repository, String tag) {
        LogUtil.info(LogEnum.TERMINAL, "DockerApiImpl commit containerId:{} repository:{} tag:{}",containerId,repository,tag);
        try{
            CommitCmd commitCmd = dockerClient.commitCmd(containerId).withRepository(repository).withTag(tag);
            return commitCmd.exec();
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL, "DockerApiImpl removeImage error:{}",e.getMessage(), e);
            return e.getMessage();
        }
    }

    @Override
    public boolean push(DockerClient dockerClient, String image, ResultCallbackTemplate resultCallback) {
        LogUtil.info(LogEnum.TERMINAL, "DockerApiImpl push image:{}",image);
        try{
            AuthConfig authConfig = new AuthConfig();
            authConfig.withUsername(dubheDockerJavaConfig.getHarborUserName()).withPassword(dubheDockerJavaConfig.getHarborPassword());
            dockerClient.pushImageCmd(image).withAuthConfig(authConfig).exec(resultCallback);
            return true;
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL, "DockerApiImpl push error:{}",e.getMessage(), e);
            return false;
        }
    }
}
