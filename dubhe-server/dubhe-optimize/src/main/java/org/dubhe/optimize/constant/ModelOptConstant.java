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

package org.dubhe.optimize.constant;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * @description 模型优化常量
 * @date 2020-05-25
 */
@Component
@Data
public class ModelOptConstant {

    /**
     * 正序排序规则
     */
    public static final String SORT_ASC = "asc";
    /**
     * 倒叙排序规则
     */
    public static final String SORT_DESC = "desc";
    /**
     * 整数匹配
     */
    public static final Pattern PATTERN_NUM = Pattern.compile("^[-\\+]?[\\d]*$");
    /**
     * 分页内容
     */
    public static final String RESULT = "result";

    /**
     * 模型优化任务结果字典
     */
    public static final String TASK_RESULT_DICT_NAME = "opt_result";
    /**
     * 数据集挂载路径
     */
    public static final String DATASET_MOUNT_PATH = "/usr/local/dataset";
    /**
     *算法挂载路径
     */
    public static final String ALGORITHM_MOUNT_PATH = "/usr/local/algorithm";
    /**
     *输入模型挂载路径
     */
    public static final String INPUT_MODEL_BEFORE_MOUNT_PATH = "/usr/local/input/model";
    /**
     *输出日志挂载路径
     */
    public static final String OUTPUT_LOG_MOUNT_PATH = "/usr/local/output/log";
    /**
     *压缩前评估结果挂载路径
     */
    public static final String OUTPUT_RESULT_BEFORE_MOUNT_PATH = "/usr/local/output/result/before";
    /**
     *输出模型挂载路径
     */
    public static final String OUTPUT_MODEL_MOUNT_PATH = "/usr/local/output/model";
    /**
     *压缩后评估结果挂载路径
     */
    public static final String OUTPUT_RESULT_AFTER_MOUNT_PATH = "/usr/local/output/result/after";
    /**
     * 使用的CPU配额
     */
    public static final int CPU_NUM = 2048;
    /**
     * 使用的GPU配额
     */
    public static final int GPU_NUM = 1;
    /**
     * 使用的内存配额
     */
    public static final int MEMORY_NUM = 8000;
    /**
     * CNN剪枝算法启动命令
     */
    public static final String OPT_START_SLIMMING_COMMAND = "cd /usr/local/algorithm && python run.py --prune_method=%s --model=%s --data_type=%s --dataset_dir=%s --model_save_dir=%s --log_dir=%s --before_result_dir=%s --after_result_dir=%s";
    /**
     * 量化算法启动命令
     */
    public static final String OPT_START_QUANTIFY_COMMAND = "cd /usr/local/algorithm && python3 of_cnn_evaluate.py --model=%s --model_load_dir=%s --val_data_dir=%s --log_dir=%s --result_dir=%s --use_tensorrt=False --use_int8_online=False && python3 of_cnn_evaluate.py --model=%s --model_load_dir=%s --val_data_dir=%s --log_dir=%s --result_dir=%s --use_tensorrt=True --use_int8_online=True";
    /**
     * 非内置运行命令
     */
    public static final String MY_OPT_COMMAND = "cd %s && %s";
    /**
     * 模型优化结果日志路径
     */
    public static final String OPTIMIZE_LOG = "/log";
    /**
     * 模型优化结果保存模型路径
     */
    public static final String OPTIMIZE_MODEL = "/model";
    /**
     * 模型优化结果保存优化前模型评估参数
     */
    public static final String OPTIMIZE_JSON_BEFORE = "/before";
    /**
     * 模型优化结果保存优化后模型评估参数
     */
    public static final String OPTIMIZE_JSON_AFTER = "/after";
    /**
     * 模型优化结果JSON文件名称
     */
    public static final String OPTIMIZE_JSON_NAME = "/results_eval.json";
    /**
     * 我的压缩算法根路径
     */
    public static final String MY_OPT_ALGORITHM_ROOT_PATH = "/model-opt/myAlgorithms/";
    /**
     * 我的数据集
     */
    public static final String MY_OPT_DATASET_ROOT_PATH = "/model-opt/myDataset/";
    /**
     * 算法准确度名称
     */
    public static final String ACCURACY = "accuracy";
    /**
     * 复制模型后缀文件夹名称
     */
    public static final String COPY_MODEL_POSTFIX = "/model";
    /**
     * 基于神经元权重剪枝
     */
    public static final String NEURONAL_PRUNING = "基于神经元权重剪枝";

}
