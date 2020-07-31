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

package org.dubhe.constant;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @description 训练常量
 * @create: 2020-05-12
 */
@Component
@Data
public class TrainJobConstant {

    @Value("${train-job.namespace}")
    private String namespace;

    @Value("${train-job.version-label}")
    private String versionLabel;

    @Value("${train-job.separator}")
    private String separator;

    @Value("${train-job.pod-name}")
    private String podName;

    @Value("${train-job.python-format}")
    private String pythonFormat;

    @Value("${train-job.manage}")
    private String manage;

    @Value("${train-job.out-path}")
    private String outPath;

    @Value("${train-job.log-path}")
    private String logPath;

    @Value("${train-job.visualized-log-path}")
    private String visualizedLogPath;

    @Value("${train-job.docker-dataset-path}")
    private String dockerDatasetPath;

    @Value("${train-job.docker-train-path}")
    private String dockerTrainPath;

    @Value("${train-job.docker-out-path}")
    private String dockerOutPath;

    @Value("${train-job.docker-log-path}")
    private String dockerLogPath;

    @Value("${train-job.docker-dataset}")
    private String dockerDataset;

    @Value("${train-job.docker-visualized-log-path}")
    private String dockerVisualizedLogPath;

    @Value("${train-job.load-path}")
    private String loadPath;

    @Value("${train-job.load-key}")
    private String loadKey;

    @Value("${train-job.eight}")
    private String eight;

    @Value("${train-job.plus-eight}")
    private String plusEight;

    public static final String TRAIN_ID = "trainId";

    public static final String TRAIN_VERSION = "trainVersion";

    public static final String RUN_TIME = "runtime";

    public static final String TRAIN_STATUS = "trainStatus";

    public static final String CREATE_TIME = "createTime";

    public static final String ALGORITHM_NAME = "algorithmName";
}
