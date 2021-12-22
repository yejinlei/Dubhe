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
package org.dubhe.train.constant;

/**
 * @description 常量
 * @date 2020-12-16
 */
public class TrainConstant {

    /**
     * createTime
     **/
    public static final String CREATE_TIME = "create_time";

    public static final String DATASET_VOLUME_MOUNTS = "/dataset";

    public static final String WORKSPACE_VOLUME_MOUNTS = "/workspace";

    public static final String MODEL_VOLUME_MOUNTS = "/model";


    /**
     * 训练文件目录pattern,eg: /${minio.bucketName}/train-manage/1/xxx
     */
    public static final String TRAIN_PATH_PATTERN = "%s/%s/%s";

    /**
     * 训练文件相对目录pattern, eg: /train-manage/1/xxx
     */
    public static final String TRAIN_RELATIVE_PATH_PATTERN = "/%s/%s/%s";

    /**
     * 训练运行命令的长度限制
     */
    public static final int RUN_COMMAND_LENGTH_LIMIT = 8192;
}
