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

package org.dubhe.data.constant;

import lombok.Getter;
import org.dubhe.biz.base.exception.ErrorCode;

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
    DATASET_ABSENT(1301, "数据集不存在"),
    TASK_SPLIT_ABSENT(1302, "任务子集不存在，可能已被清除"),
    FILE_ABSENT(1303, "文件不存在"),
    FILE_EXIST(1304, "文件已存在"),
    VIDEO_EXIST(1305, "数据集存在视频"),
    FILE_DELETE_ERROR(1306, "文件删除失败"),
    DATASET_TYPE_MODIFY_ERROR(1307, "数据集存在文件不可更改数据类型"),
    DATASET_ANNOTATION_MODIFY_ERROR(1308, "非未标注状态不可更改标注类型"),
    DATASET_PUBLIC_LIMIT_ERROR(1309, "预置数据集不可操作"),
    DATASET_NAME_DUPLICATED_ERROR(1310, "数据集已存在"),
    TASK_ABSENT(1311, "任务不存在"),
    ES_DATA_DELETE_ERROR(1312,"删除es数据错误"),


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
    LABEL_NAME_DUPLICATION(1602, "标签名称或标签ID重复，请检查"),
    LABEL_NOT_EXISTS(1603, "标签不存在"),
    LABEL_NAME_COLOR_NOT_NULL(1604, "JSON文件中标签名称和颜色不能为空"),
    LABEL_PUBLIC_EORROR(1605, "普通用户不允许操作公共标签"),
    LABEL_GROUP_ID_IS_NULL(1606, "标签组ID为null"),
    LABEL_FORMAT_IS_ERROR(1607, "标签格式不正确"),

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
    DATASET_TRACK_TYPE_ERROR(1712, "数据集类型只能是目标跟踪才能进行跟踪!"),
    DATASET_DELETE_ERROR(1713, "数据集数据大数据删除异常!"),
    DATASET_PUBLISH_ERROR(1714, "数据集必须是发布后才能操作!"),
    DATASET_SWITCH_VERSION_ERROR(1715, "数据集必须版本回退后才能操作!"),
    DATASET_VERSION_FILE_IS_ERROR(1716, "数据集版本文件不存在!"),
    DATASET_ORIGINAL_FILE_IS_EMPTY(1717, "数据集无原始图片无法进行增强"),
    DATASET_NOT_ANNOTATION(1718, "数据集暂不支持自动标注"),
    DATASET_NOT_OPERATIONS_BASE_DATASET(1719, "禁止操作内置的数据集"),
    DATASET_PUBLISH_REJECT(1720, "文本暂不支持多版本发布"),
    DATASET_CHECK_VERSION_ERROR(1721,"目标版本不存在"),

    /**
     * 数据集版本校验
     */
    DATASET_VERSION_EXIST(1801, "数据集版本已存在!"),
    DATASET_VERSION_PTJOB_STATUS(1802, "当前数据集正在训练不可删除"),
    DATASET_NOT_ENHANCE(1803, "数据集状态只能是自动标注完成、标注完成、目标跟踪完成才能进行数据增强!"),
    DATASET_PUBLIC_ERROR(1900, "不允许操作公共数据集"),

    /**
     * 标签组错误
     */
    LABELGROUP_NAME_DUPLICATED_ERROR(1901, "标签组名已存在"),
    LABELGROUP_PUBLIC_ERROR(1902, "不允许操作公共标签组"),
    LABELGROUP_IN_USE_STATUS(1903, "当前标签组内标签正在使用，无法操作"),
    LABELGROUP_JSON_FILE_ERROR(1904, "请上传json格式文件"),
    LABELGROUP_JSON_FILE_SIZE_ERROR(1905, "文件大小不能超过5M"),
    LABELGROUP_JSON_FILE_FORMAT_ERROR(1906, "请输入正确的JSON内容"),
    LABELGROUP_DOES_NOT_EXIST(1907, "标签组不存在"),
    LABELGROUP_FILE_NAME_NOT_EXIST(1908, "请输入文件名称"),
    LABELGROUP_LABELG_ID_ERROR(1909, "标签ID异常"),
    LABELGROUP_OPERATE_LABEL_ID_ERROR(1910, "不允许操作公共标签组中的标签"),
    LABELGROUP_LABEL_NAME_ERROR(1911, "请输入正确预置标签组标签"),
    LABELGROUP_LABEL_GROUP_EDIT_ERROR(1912, "标签组下标签不许修改"),
    LABELGROUP_LABEL_GROUP_QUOTE_DEL_ERROR(1913, "标签组已被数据集引用,无法删除!"),
    LABEL_NAME_REPEAT(1914, "标签名称已存在!"),
    LABEL_PREPARE_IS_TXT(1915, "请选择文本预制标签进行自动标注!"),
    LABEL_AUTHORITY_ERROR(1915, "无权限操作当前标签"),
    LABEL_QUOTE_DEL_ERROR(1913, "标签已被引用,无法删除!"),

    /**
     * 医学数据集错误
     */
    MEDICINE_AUTO_DATASET_ERROR(2001, "当前医学数据集不可自动标注"),
    MEDICINE_MEDICAL_ALREADY_EXISTS(2002, "当前类型的医学数据集已存在"),
    DATAMEDICINE_ABSENT(2003, "医学数据集不存在"),
    DATAMEDICINE_AUTOMATIC(2004, "数据集正在自动标注中,请稍等!"),
    MEDICINE_NAME_ERROR(2005, "当前名称已存在"),
    MEDICINE_MEDICAL_ALREADY_EXISTS_RESTORE(2006, "当前类型的医学数据集已存在,请确认后在进行还原");

    ErrorEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private Integer code;
    private String msg;

}
