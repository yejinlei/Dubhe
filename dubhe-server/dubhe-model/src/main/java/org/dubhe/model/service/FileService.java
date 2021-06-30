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

package org.dubhe.model.service;


import org.dubhe.biz.base.context.UserContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.function.Consumer;

/**
 * @description 文件服务
 * @date 2021-01-20
 */
@Component
public interface FileService {


    /**
     * 将临时区文件加载到相应的服务路径
     *
     * @param tempPath   临时文件目录
     * @param user       当前用户
     * @return 拷贝后的路径
     */
    String transfer(@NotNull String tempPath, @NotNull UserContext user);

    /**
     * 文件拷贝
     * @param sourcePath      源文件路径
     * @param user            当前用户
     * @param successCallback 拷贝成功回调
     * @param failCallback    拷贝失败回调
     */
    @Async(value = "taskRunner")
    void copyFileAsync(String sourcePath, UserContext user, Consumer<String> successCallback, Consumer<Exception> failCallback);

    /**
     * 验证源文件路径是否存在，若不存在直接跑出异常
     * @param sourcePath       源文件路径
     */
    void validatePath(String sourcePath);

    /**
     * 获取绝对路径
     * @param relativePath     相对路径
     * @return 绝对路径
     */
    String getAbsolutePath(String relativePath);

    /**
     *  我的模型转预置模型 模型校验与拷贝
     *  @param sourcePath 源路径
     * @return String 目标路径
     */
    String convertPreset(String sourcePath,UserContext user);
}
