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
package org.dubhe.k8s.enums;

import lombok.Getter;
import org.dubhe.utils.StringUtils;

import static org.dubhe.constant.SymbolConstant.BLANK;

/**
 * @description 容器在k8s处理时的异常情况
 * @date 2020-9-22
 */
@Getter
public enum ContainerStatusesStateEnum {
    /**
     * ImagePullBackOff 镜像拉取失败
     */
    IMAGE_PULL_BACK_OFF("ImagePullBackOff", "Failed to pull image"),
    /**
     * ErrImagePull 镜像拉取失败
     */
    ERR_IMAGE_PULL("ErrImagePull", "Failed to pull image"),
    /**
     * CrashLoopBackOff 容器启动失败
     */
    CRASH_LOOP_BACK_OFF("CrashLoopBackOff", "Failed to run container"),
    /**
     * ContainerCreating 容器创建中
     */
    CONTAINER_CREATING("ContainerCreating", "Container is being created")

    ;
    /**
     * k8s错误编码
     */
    private String reason;
    /**
     * 错误编码解释
     */
    private String message;

    ContainerStatusesStateEnum(String reason, String message) {
        this.reason = reason;
        this.message = message;
    }
    public static String getStateMessage(String reason){
        for (ContainerStatusesStateEnum containerStatusesStateEnum : ContainerStatusesStateEnum.values()) {
            if (StringUtils.equals(reason, containerStatusesStateEnum.getReason() )){
                return containerStatusesStateEnum.getMessage();
            }
        }
        return BLANK;
    }
}
