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

package org.dubhe.k8s.constant;

/**
 * @description K8s label constants
 * @date 2020-04-23
 */
public class K8sLabelConstants {
    /**
     * 资源名标签，由用户传入，标记一系列相关资源，比如创建statefulset，那么对应的pv，pvc，pod，service，ingress等都要打上相同的platform/resource-name
     */
    public final static String BASE_TAG_SOURCE = "platform/resource-name";
    /**
     * 创建者标签，通过api创建的均为 platform 使用常量BASE_TAG_CREATE_BY_VALUE
     */
    public final static String BASE_TAG_CREATE_BY = "platform/create-by";
    /**
     * 父对象名，比如通过statefulset创建的pod其platform/p-name为statefulset的名字
     */
    public final static String BASE_TAG_P_NAME = "platform/p-name";
    /**
     * 业务标签，用于标识业务，由业务层传入
     */
    public final static String BASE_TAG_BUSINESS = "platform/business";
    /**
     * 任务身份标签，用于标识任务身份，由业务层传入
     */
    public final static String BASE_TAG_TASK_IDENTIFY = "platform/task-identify";
    /**
     * 运行环境标签，用于对不同环境回调进行分流
     */
    public final static String PLATFORM_RUNTIME_ENV = "platform/runtime-env";

    /**
     * 节点隔离标签key
     */
    public final static String PLATFORM_TAG_ISOLATION_KEY="platform/node-isolate";
    /**
     * 节点隔离标签value
     */
    public final static String PLATFORM_TAG_ISOLATION_VALUE="{}-isolation-{}";

    /**
     * 节点隔离标签value分隔符
     */
    public final static String PLATFORM_TAG_ISOLATION_VALUE_SPLIT="-isolation-";

    /**
     * 对象类型，对应k8s的kind
     */
    public final static String BASE_TAG_P_KIND = "platform/p-kind";

    public final static String NODE_GPU_LABEL_KEY = "gpu";
    public final static String NODE_GPU_LABEL_VALUE = "gpu";

    public final static String BASE_TAG_CREATE_BY_VALUE = "platform";
}
