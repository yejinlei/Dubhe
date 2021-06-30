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

package org.dubhe.serving.enums;

import lombok.Getter;
import org.dubhe.biz.base.exception.ErrorCode;

/**
 * @description 云端Serving错误码
 * @date 2020-08-25
 */
@Getter
public enum ServingErrorEnum implements ErrorCode {

    SERVING_NAME_EXIST(1600, "服务名称已存在"),
    INTERNAL_SERVER_ERROR(1601, "内部错误"),
    SERVING_INFO_ABSENT(1602, "服务信息不存在"),
    OPERATION_NOT_ALLOWED(1603, "当前状态不允许进行此操作"),
    CPU_SPECS_DICT_NOT_EXIST(1604, "cpu规格字典不存在"),
    GPU_SPECS_DICT_NOT_EXIST(1605, "gpu规格字典不存在"),
    NFS_MODEL_COPY_ERROR(1606, "nfs模型拷贝失败"),
    MODEL_FRAME_TYPE_NOT_SUPPORTED(1607, "平台暂不支持该框架模型部署"),
    RESOURCE_TYPE_NOT_EXIST(1608, "该节点类型不存在"),
    MODEL_NOT_EXIST(1609, "模型不存在"),
    PREDICT_IMAGE_EMPTY(1610, "预测图片为空"),
    IMAGE_FORMAT_ERROR(1611, "图片格式暂不支持"),
    GRPC_PROTOCOL_NOT_SUPPORTED(1612, "gRPC协议不支持灰度发布"),
    INFERENCE_IMAGE_EMPTY(1613, "待推理图片为空"),
    SERVING_NOT_WORKING(1614, "服务未运行，请先部署模型"),
    URI_EMPTY(1615, "服务域名为空"),
    PROTOCOL_NOT_SUPPORTED(1616, "服务协议暂不支持"),
    MODEL_FILE_NOT_EXIST(1617, "模型文件不存在"),
    FRAME_TYPE_NOT_SUPPORTED(1618, "框架类型暂不支持"),
    INPUT_FILE_NOT_EXIST(1619, "输入文件不存在"),
    NODE_NUMBER_LESS_THAN_TWO(1620, "请至少选2个节点"),
    UPLOAD_SHELL_FILE(1621, "上传查询推荐进度脚本失败"),
    CPU_NOT_SUPPORTED_BY_ONEFLOW(1622, "Oneflow框架暂不支持CPU类型部署"),
    IMAGE_CONVERT_BASE64_FAIL(1623, "图片转换为base64错误"),
    ERROR_SYSTEM(1624, "系统繁忙!"),
    ALGORITHM_NOT_EXIST(1625, "算法不存在"),
    SCRIPT_NOT_EXIST(1626, "推理脚本文件不存在"),
    CALL_ALGORITHM_SERVER_FAIL(1627, "算法服务调用失败"),
    CALL_IMAGE_SERVER_FAIL(1628, "镜像服务调用失败"),
    IMAGE_NOT_EXIST(1629, "镜像不存在"),
    DATABASE_ERROR(1630, "数据库操作失败"),
    MODEL_CONFIG_NOT_EXIST(1631, "模型配置信息不存在"),
    ;

    private Integer code;
    private String msg;

    ServingErrorEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
