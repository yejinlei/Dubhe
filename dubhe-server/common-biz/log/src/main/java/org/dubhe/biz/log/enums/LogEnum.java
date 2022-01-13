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

package org.dubhe.biz.log.enums;

import lombok.Getter;

/**
 * @description 日志类型枚举类
 * @date 2020-06-23
 */
@Getter
public enum LogEnum {

    // 系统报错日志
    SYS_ERR,
    // 用户请求日志
    REST_REQ,
    //全局请求日志
    GLOBAL_REQ,
    // 训练模块
    BIZ_TRAIN,
    //算法管理模块
    BIZ_ALGORITHM,
    // 系统模块
    BIZ_SYS,
    // 模型模块
    BIZ_MODEL,
    // 模型优化
    MODEL_OPT,
    // 数据集模块
    BIZ_DATASET,
    // k8s模块
    BIZ_K8S,
    //note book
    NOTE_BOOK,
    //NFS UTILS
    NFS_UTIL,
    //localFileUtil
    LOCAL_FILE_UTIL,
    //FILE UTILS
    FILE_UTIL,
    //FILE UTILS
    UPLOAD_TEMP,
    //STATE MACHINE
    STATE_MACHINE,
    //全局垃圾回收
    GARBAGE_RECYCLE,
    //DATA_SEQUENCE
    DATA_SEQUENCE,
    //IO UTIL
    IO_UTIL,
    // 日志切面
    LOG_ASPECT,
    // 远程调用
    REMOTE_CALL,
    // 网关
    GATEWAY,
    // Redis
    REDIS,
    //镜像
    IMAGE,
    //度量
    MEASURE,
    //云端Serving
    SERVING,
    //专业版终端
    TERMINAL,
    //tadl
    TADL;

    /**
     * 判断日志类型不能为空
     *
     * @param  logType 日志类型
     * @return boolean 返回类型
     */
    public static boolean isLogType(LogEnum logType) {

        if (logType != null) {
            return true;
        }
        return false;
    }
}
