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

package org.dubhe.data.constant;

import lombok.Getter;
import org.dubhe.exception.ErrorCode;

/**
 * @description 错误类型
 * @date 2020-04-10
 */
@Getter
public enum ErrorEnum implements ErrorCode {

    /**
     * 网络异常
     */
    NET_ERROR(1001, "网络异常"),
    PARAM_ERROR(1002, "参数错误"),
    DATA_ERROR(1003, "数据为空"),
    THREAD_ERROR(1004, "线程被中断或执行异常"),
    REQUEST_ERROR(1005, "Http请求异常"),
    // 部分数据权限是基于sql注入的方式实现，默认当删除或更改数据没有作任何修改时，抛此异常
    DATA_ABSENT_OR_NO_AUTH(1006, "数据不存在或没有操作权限"),

    /**
     * 数据库操作异常
     */
    DB_ERROR(1200, "数据库出错"),
    DB_SEARCH_ERROR(1201, "数据库查询出错"),
    DB_INSERT_ERROR(1202, "数据库插入出错"),
    DB_UPDATE_ERROR(1203, "数据库更新出错"),

    /**
     * 数据集文件操作异常
     */
    NAME_DUPLICATED_ERROR(1300, "同一个数据集下不能有重名文件"),
    DATASET_NAME_DUPLICATED_ERROR(1300, "该名称的数据集已存在"),
    DATASET_ABSENT(1301, "数据集不存在"),
    TASK_SPLIT_ABSENT(1302, "任务子集不存在，可能已被清除"),
    FILE_ABSENT(1303, "文件不存在"),
    FILE_EXIST(1304, "文件已存在"),
    VIDEO_EXIST(1305, "数据集存在视频"),
    FILE_DELETE_ERROR(1306, "文件删除失败"),

    /**
     * 数据集标注操作错误
     */
    AUTO_FILE_EMPTY(1400, "没有需要标注的文件(检查文件是否全部标注完成或标注中)"),
    AUTO_ERROR(1401, "当前数据集正在自动标注中，禁止操作"),
    AUTO_NOT_MARKED(1402, "当前数据集未标注不需要清理"),

    /**
     * 无标注信息
     */
    ANNOTATION_EMPTY_ERROR(1500, "无标注信息"),

    /**
     * 标签定义错误
     */
    LABEL_ERROR(1600, "标签名不能为空或非系统自动标注支持的标签"),
    LABEL_NAME_EXIST(1601, "本数据集已有同名标签"),

    /**
     * 数据集操作错误
     */
    AUTO_DATASET_ERROR(1700, "数据集状态只能是未标注和手动标注中才能进行自动标注!"),
    AUTO_LABEL_EMPTY_ERROR(1701, "该数据集未添加系统支持自动标注的标签"),
    DATASET_VERSION_STATUS_NO_SWITCH(1702, "数据集当前状态不允许做版本切换"),
    DATASET_VERSION_ANNOTATION_COPY_EXCEPTION(1703, "数据集文件拷贝异常"),
    DATASET_VERSION_DELETE_CURRENT_ERROR(1704, "当前版本不允许删除"),
    DATASET_ANNOTATION_NOT_FINISH(1705, "数据集标注未完成不允许发布"),
    DATASET_SAMPLE_IS_UNDONE(1706, "数据集未采样,请稍等!"),
    DATASET_REPEAT_ANNOTATION(1707, "数据集已标注,请勿重复标注!"),
    DATASET_SAMPLING(1708, "数据集采样中,请稍等"),
    DATASET_VIDEO_HAS_NOT_BEEN_AUTOMATICALLY_TRACKED(1709, "该数据集视频未自动跟踪完成,请稍等"),
    DATASET_LABEL_EMPTY(1710, "增强类型不能为空!"),
    DATASET_ENHANCEMENT(1711, "该数据集正在增强中,请稍等"),

    /**
     * 数据集版本校验
     */
    DATASET_VERSION_EXIST(1801, "数据集版本已存在!"),
    DATASET_VERSION_PTJOB_STATUS(1802, "当前数据集正在训练不可删除"),
    DATASET_NOT_ENHANCE(1803, "数据集状态只能是自动标注完成、标注完成、目标跟踪完成才能进行数据增强!"),
    DATASET_PUBLIC_ERROR(1900, "不允许操作公共数据集"),
    ;


    ErrorEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private Integer code;
    private String msg;

}
