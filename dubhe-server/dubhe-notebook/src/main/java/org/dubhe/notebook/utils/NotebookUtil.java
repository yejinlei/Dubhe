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

package org.dubhe.notebook.utils;

import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.file.enums.BizPathEnum;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.utils.K8sCallBackTool;

/**
 * @description Notebook 工具类
 * @date 2020-04-27
 */
public class NotebookUtil {

    private static final char SEPARATOR = '-';
    private static final String NOTEBOOK = "notebook";
    private static final String NAMESPACE = "namespace";
    private static final String RESOURCE_NAME = "resource-name";
    private static final String K8S_MOUNT_PATH = "/workspace";
    private static final String K8S_FILE_SEPARATOR = "/";
    private static final int TRUNCATION_INDEX = 250;
    private static final int NOTEBOOK_TIMEOUT_SECOND = 180;
    private static final String JUPYTER_TOKEN_KEY = "?token=";

    public static final String K8S_REGEX = "[a-z0-9]([-a-z0-9]*[a-z0-9])?";
    public static final String K8S_NOTEBOOK_REGEX = "^[\\u4e00-\\u9fa5_a-zA-Z0-9\\-]+$";
    public static final String NOTEBOOK_NOT_EXISTS = "Notebook不存在";
    public static final Long ANONYMITY_USER_ID = 0L;

    public static final int CPU_MIN_NUMBER = 1;
    public static final int CPU_MAX_NUMBER = 8;
    public static final int GPU_MAX_NUMBER = 4;
    public static final int GPU_MIN_NUMBER = 0;
    public static final int MEMORY_MIN_NUMBER = 1;
    public static final int MEMORY_MAX_NUMBER = 8;
    public static final int DISK_MEMORY_MIN_NUMBER = 1;
    public static final int DISK_MEMORY_MAX_NUMBER = 1024;

    public static final String FAILED = "失败!";

    private NotebookUtil() {

    }

    /**
     * 验证是否包含失败关键字
     *
     * @param info
     * @return boolean
     */
    public static boolean validateFailedInfo(String info) {
        return info != null && info.contains(FAILED);
    }

    /**
     * 获取当前登录人ID
     * 如果当前没登录人，则返回匿名用户
     *
     * @return long
     */
    public static long getCurUserId(UserContextService userContextService) {
        Long curUserId = userContextService.getCurUserId();
        return curUserId == null ? ANONYMITY_USER_ID : curUserId;
    }

    /**
     * @param userId 当前登录人
     * @return namespace
     * @deprecated use K8sNameTool.generateNameSpace(..) instead
     * 根据当前用户 生成 Notebook的NameSpace
     */
    @Deprecated
    public static String generateNameSpace(long userId) {
        return NOTEBOOK + SEPARATOR + NAMESPACE + SEPARATOR + userId;
    }

    /**
     * @return String 容器内映射到宿主机的路径
     */
    public static String getK8sMountPath() {
        return K8S_MOUNT_PATH;
    }

    /**
     * 获取k8s响应结果的tMessage信息
     * 如果超长(250)，则截断
     *
     * @param info
     * @return String
     */
    public static String getK8sStatusInfo(String info) {
        if (info == null) {
            return SymbolConstant.BLANK;
        }
        return StringUtils.truncationString(info, TRUNCATION_INDEX);
    }


    /**
     * 获取k8s响应结果的tMessage信息
     * 如果超长(250)，则截断
     *
     * @param ptBaseResult
     * @return String
     */
    public static String getK8sStatusInfo(PtBaseResult ptBaseResult) {
        if (ptBaseResult == null) {
            return SymbolConstant.BLANK;
        }
        return getK8sStatusInfo(ptBaseResult.getMessage());
    }

    /**
     * 获取k8s异常结果的tMessage信息
     * 如果超长(250)，则截断
     *
     * @param e 异常
     * @return String
     */
    public static String getK8sStatusInfo(Exception e) {
        if (e == null) {
            return SymbolConstant.BLANK;
        } else if (e instanceof NullPointerException) {
            return "K8s NullPointerException";
        }
        return getK8sStatusInfo(e.getMessage());
    }

    /**
     * 验证 jupyter URL 是否包含token
     *
     * @param jupyterUrl
     * @return boolean
     */
    public static boolean checkUrlContainsToken(String jupyterUrl) {
        if (StringUtils.isBlank(jupyterUrl)) {
            return false;
        }
        return jupyterUrl.contains(JUPYTER_TOKEN_KEY);
    }

    /**
     * 获取超时时间秒
     *
     * @return  Long
     */
    public static Long getTimeoutSecondLong() {
        return K8sCallBackTool.getTimeoutSecondLong(NOTEBOOK_TIMEOUT_SECOND);
    }


    /**
     * 生成第三方notebook名称
     *
     * @param bizPathEnum 业务源
     * @param sourceId   第三方源主键
     * @return String
     */
    public static String generateName(BizPathEnum bizPathEnum, long sourceId) {
        return bizPathEnum.getBizCode() + SEPARATOR + sourceId;
    }
}
