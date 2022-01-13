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
package org.dubhe.tadl.enums;

import lombok.Getter;
import org.dubhe.biz.base.exception.ErrorCode;

/**
 * @description 错误类型
 * @date 2020-12-28
 */
@Getter
public enum TadlErrorEnum implements ErrorCode {


    /**
     * 参数校验/数据校验 错误
     */
    PARAM_ERROR(2001, "参数错误"),
    DATA_ERROR(2002, "数据为空"),
    // 部分数据权限是基于sql注入的方式实现，默认当删除或更改数据没有作任何修改时，抛此异常
    DATA_ABSENT_OR_NO_AUTH(2003, "数据不存在或没有操作权限"),
    TADL_NAME_EXIST(2004, "实验名称已存在"),
    ALGORITHM_NAME_EXIST(2005,"算法名称已存在"),
    /**
     * 数据库操作异常
     */
    DB_ERROR(2200, "数据库出错"),
    DB_SEARCH_ERROR(2201, "数据库查询出错"),
    DB_INSERT_ERROR(2202, "数据库插入出错"),
    DB_UPDATE_ERROR(2203, "数据库更新出错"),


    /**
     * 业务错误
     */
    EXPERIMENT_DOES_NOT_EXIST_ERROR(2300,"实验不存在"),
    EXPERIMENT_TRAIL_NUMBER_ERROR(2301,"更新trial数量有误,不可比当前trial数量小"),
    ALGORITHM_ALREADY_EXISTS_ERROR(2302,"该算法已存在"),
    INTERNAL_SERVER_ERROR(2303, "内部错误"),
    ALGORITHM_VERSION_DOES_NOT_EXIST_ERROR(2303, "算法版本不存在"),
    ALGORITHM_DOES_NOT_EXIST_ERROR(2304, "算法不存在"),
    FILE_OPERATION_ERROR(2305, "文件操作失败"),
    CMD_FORM_ERROR(2306, "算法启动命令生成错误"),
    ALGORITHM_VERSION_ERROR(2307,"非最新版本不能发布"),
    VERSION_ERROR(2308,"当前版本和下一版本与要发布的版本信息不一致"),
    RESOURCE_ERROR(2309,"当前资源规格不存在或已被删除"),
    EXPERIMENT_UPDATE_ERROR(2310,"只有待运行状态才能编辑"),
    RUNTIME_PARAM_UPDATE_ERROR(2311,"当前实验状态不可编辑"),
    UPDATE_MAX_EXEC_DURATION_ERROR(2312,"最大执行时间不能小于当前已执行时间"),
    UPDATE_MAX_TRIAL_NUM_ERROR(2313,"最大 trial 数不能小于非待运行状态的 trial 数"),
    UPDATE_TRIAL_CONCURRENT_NUM_ERROR(2314,"最大并发数不能大于 trial 总数"),
    OPERATION_NOT_ALLOWED(2315, "当前状态不允许进行此操作"),
    EXPERIMENT_CHANGE_ERR_MESSAGE(2316, "当前实验状态不可变更"),
    ALGORITHM_STAGE_DOES_NOT_EXIST_ERROR(2304, "算法阶段不存在");

    TadlErrorEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private Integer code;
    private String msg;

}
