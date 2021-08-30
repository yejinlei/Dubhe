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

package org.dubhe.train.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @description 训练常量
 * @date 2020-05-12
 */
@Component
@Data
@ConfigurationProperties(prefix = "train-job")
public class TrainJobConfig {

    private String namespace;

    private String versionLabel;

    private String separator;

    private String podName;

    private String pythonFormat;

    private String manage;

    private String modelPath;

    private String outPath;

    private String visualizedLogPath;

    private String dockerDatasetPath;

    private String dockerTrainPath;

    private String dockerTrainModelPath;

    private String dockerTrainOutPath;

    private String dockerDataset;

    private String dockerModelPath;

    private String dockerTeacherModelPath;

    private String dockerTeacherModelKey;

    private String dockerStudentModelKey;

    private String dockerStudentModelPath;

    private String atlasAnaconda;

    private String atlasPythonioencoding;

    private String dockerValDatasetPath;

    private String dockerPipSitePackagePath;

    private String loadValDatasetKey;

    private String dockerVisualizedLogPath;

    private String loadPath;

    private String loadKey;

    private String eight;

    private String plusEight;

    private String nodeIps;

    private String nodeNum;

    private String gpuNumPerNode;

    public static final String TRAIN_ID = "trainId";

    public static final String TRAIN_VERSION = "trainVersion";

    public static final String RUN_TIME = "runtime";

    public static final String TRAIN_STATUS = "trainStatus";

    public static final String CREATE_TIME = "createTime";

    public static final String ALGORITHM_NAME = "algorithmName";

}
