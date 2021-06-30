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

package org.dubhe.optimize.enums;

import lombok.Getter;
import org.dubhe.biz.base.exception.ErrorCode;

/**
 * @description 模型优化错误码
 * @date 2020-05-22
 */
@Getter
public enum ModelOptErrorEnum implements ErrorCode {

    /**
     * 该模型优化任务不存在
     */
    MODEL_OPT_TASK_ABSENT(1400, "该模型优化任务不存在"),
    /**
     * 该模型优化任务实例不存在
     */
    MODEL_OPT_TASK_INSTANCE_ABSENT(1401, "该模型优化任务实例不存在"),
    /**
     * 模型优化任务实例创建失败
     */
    MODEL_OPT_TASK_INSTANCE_CREATE_FAILED(1402, "模型优化任务实例创建失败"),
    /**
     * 模型优化任务名称已存在
     */
    MODEL_OPT_TASK_NAME_EXIST(1403, "模型优化任务名称已存在"),
    /**
     * 该任务已存在等待或进行中的实例
     */
    MODEL_OPT_TASK_INSTANCE_EXIST(1404, "该任务已存在等待或进行中的实例"),
    /**
     * 请先取消等待或进行中的实例
     */
    MODEL_OPT_TASK_DELETE_ERROR(1405, "请先取消等待或进行中的实例"),

    /**
     * 请先添加模型优化结果字典
     */
    MODEL_OPT_TASK_RESULT_DICT_EMPTY(1406, "请先添加模型优化结果字典"),

    /**
     * 模型压缩算法不存在
     */
    MODEL_OPT_ALGORITHM_NOT_EXIST(1407, "模型压缩算法不存在"),

    /**
     * 任务未完成，不能下载
     */
    MODEL_OPT_TASK_UNFINISHED(1408, "任务未完成，不能下载"),

    /**
     * 数据集不存在，请选择正确的数据集
     */
    MODEL_OPT_DATASET_ABSENT(1409, "数据集不存在，请选择正确的数据集"),

    /**
     * 当前状态不能进行此操作
     */
    MODEL_OPT_TASK_INSTANCE_STATUS_ERROR(1410, "当前状态不能进行此操作"),

    /**
     * 模型不存在
     */
    MODEL_OPT_MODEL_NOT_EXIST(1411, "模型不存在"),

    /**
     * 请先登录
     */
    NO_USER_LOGGED_IN(1412, "请先登录"),

    /**
     * 当前用户无此操作权限
     */
    NO_PERMISSION(1413, "当前用户无此操作权限"),

    /**
     * 内部错误
     */
    INTERNAL_SERVER_ERROR(1414, "内部错误"),

    /**
     * 字典参数不存在，请先添加字典
     */
    PARAM_DICT_NOT_EXIST(1415, "字典参数不存在，请先添加字典"),

    /**
     * 无可用镜像，请先在字典中配置镜像
     */
    IMAGES_DICT_NOT_EXIST(1416, "无可用镜像，请先在字典中配置镜像"),

    /**
     * 容器启动失败
     */
    CONTAINER_START_FAILED(1417, "容器启动失败"),

    /**
     * 数据集名称已存在
     */
    DATASET_NAME_EXIST(1418, "数据集名称已存在");

    private Integer code;
    private String msg;

    ModelOptErrorEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
