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

package org.onebrain.operator.constants;

/**
 * @description k8s常量
 * @date 2020-09-23
 */
public class KubeConstants {

    public static final String DISTRIBUTE_TRAIN_LABEL = "dt-name";
    public static final String STATEFULSET_LABEL = "dt-ss-name";
    public static final String JOB_LABEL = "dt-job-name";
    public static final String MASTER_CONTAINER_NAME = "distribute-train-master";
    public static final String SLAVE_CONTAINER_NAME = "distribute-train-slave";
    public final static String USER_DIR_SYSTEM_PROPERTY = "user.dir";
    //不许重试
    public static final Integer BACKOFFLIMIT = 0;

    public static final String CHARSET = "utf-8";

    public static final String ENV_NODE_NUM = "NODE_NUM";

    public static final String VOLUME_SHM = "dshm";
}
