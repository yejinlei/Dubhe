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
package org.dubhe.tadl.constant;

import java.io.File;

/**
 * @description 常量
 * @date 2020-12-16
 */
public class TadlConstant {

    /**
     * 模块路径
     */
    public static final String MODULE_URL_PREFIX = "/";

    /**
     * 算法工程名
     */
    public static final String ALGORITHM_PROJECT_NAME = "TADL";

    /**
     * 算法cmd符号
     */
    public static final String PARAM_SYMBOL = "--";

    /**
     * 算法配置文件后缀名
     */
    public static final String ALGORITHM_CONFIGURATION_FILE_SUFFIX = ".yaml";

    /**
     * yaml
     */
    public static final String ALGORITHM_YAML = "yaml/";

    /**
     * 算法文件转化python脚本文件名
     */
    public static final String ALGORITHM_TRANSFORM_FILE_NAME = "transformParam.py";

    /**
     * zip后缀名
     */
    public static final String ZIP_SUFFIX = ".zip";

    /**
     * 默认的版本
     */
    public static final String DEFAULT_VERSION = "V0001";

    /**
     * 版本名称的首字母
     */
    public static final String DATASET_VERSION_PREFIX = "V";

    /**
     * 常量数据
     */
    public static final int NUMBER_ZERO = 0;

    public static final int NUMBER_ONE = 1;

    public static final String EXECUTE_SCRIPT_PATH = File.separator + "algorithm" + File.separator + "TADL" + File.separator + "pytorch";
    public static final String MODEL_SELECTED_SPACE_PATH = File.separator + "model_selected_space" + File.separator + "model_selected_space.json";
    public static final String RESULT_PATH = File.separator + "result" + File.separator + "result.json";
    public static final String LOG_PATH = File.separator + "log";
    public static final String SEARCH_SPACE_PATH = File.separator + "search_space.json";
    public static final String BEST_SELECTED_SPACE_PATH = File.separator + "best_selected_space.json";
    public static final String BEST_CHECKPOINT_DIR = File.separator + "best_checkpoint" + File.separator;

    public static final String AND = "&&";

    public static final String RUN_PARAMETER = "run_parameter";

    public static final String MODEL_SELECTED_SPACE_PATH_STRING = "model_selected_space_path";
    public static final String RESULT_PATH_STRING = "result_path";
    public static final String LOG_PATH_STRING = "log_path";
    public static final String EXPERIMENT_DIR_STRING = "experiment_dir";
    public static final String SEARCH_SPACE_PATH_STRING = "search_space_path";
    public static final String BEST_SELECTED_SPACE_PATH_STRING = "best_selected_space_path";
    public static final String BEST_CHECKPOINT_DIR_STRING = "best_checkpoint_dir";
    public static final String DATA_DIR_STRING = "data_dir";
    public static final String TRIAL_ID_STRING = "trial_id";

    public static final String LOCK = "lock";

    public static final String SEARCH_SPACE_FILENAME = "search_space.json";
    public static final String BEST_SELECTED_SPACE_FILENAME = "best_selected_space.json";

    public static final String RESULT_JSON_TYPE = "accuracy";

    /**
     * 实验步骤流程日志
     */
    public static final String EXPERIMENT_STAGE_FLOW_LOG = "(stage_id = {})";
    public static final String EXPERIMENT_TRIAL_FLOW_LOG = "(trial_id = {})";
    public static final String PROCESS_TRIAL_KEYWORD_LOG = "The experiment id:{},stage id:{},trial id:{}.";
    public static final String PROCESS_STAGE_KEYWORD_LOG = "The experiment id:{},stage id:{}.";
    public static final String PROCESS_EXPERIMENT_FLOW_LOG = "The experiment id:{}.";

    /**
     * 状态详情记录
     */
    public static final String TRIAL_TASK_DELETE_EXCEPTION = "TRIAL任务删除异常";
    public static final String ADMIN_SERVER_EXCEPTION = "admin服务异常";
    public static final String TRIAL_STARTUP_COMMAND_ASSEMBLY_EXCEPTION = "TRIAL启动命令组装异常";
    public static final String ABNORMAL_EXPERIMENTAL_PROCESS = "实验流程异常";
    public static final String EXPERIMENT_RUN_FAILED = "实验运行失败";
    public static final String ABNORMAL_OPERATION_OF_ALGORITHM = "算法运行异常";
    public static final String TRIAL_STARTUP_FAILED = "TRIAL启动失败";
    public static final String TRIAL_STARTUP_EXCEPTION = "TRIAL启动异常";
    public static final String REDIS_STREAM_DATA_CONVERSION_EXCEPTION = "REDIS STREAM 数据转换异常";
    public static final String DISTRIBUTED_LOCK_ACQUISITION_FAILED = "分布式锁获取失败";
    public static final String STAGE_OVERTIME = "实验阶段超时";
    public static final String UNKNOWN_EXCEPTION= "未知异常";
    public static final String REDIS_MESSAGE_QUEUE_EXCEPTION = "REDIS消息队列异常";

}
