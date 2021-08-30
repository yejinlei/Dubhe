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

import org.dubhe.biz.base.constant.MagicNumConstant;

/**
 * @description K8s param constants
 * @date 2020-04-22
 */
public class K8sParamConstants {
    public static final String GPU_RESOURCE_KEY = "nvidia.com/gpu";
    public static final String GPU_MEM_RESOURCE_KEY = "aliyun.com/gpu-mem";
    public static final String QUANTITY_CPU_KEY = "cpu";
    public static final String QUANTITY_MEMORY_KEY = "memory";
    public static final String NODE_READY_TRUE = "True";
    public static final String NODE_STATUS_TRUE = "Ready";
    public static final String NODE_STATUS_FALSE = "NotReady";
    public static final String MEM_UNIT = "Mi";
    public static final String MEM_UNIT_GI = "Gi";
    public static final String MEM_UNIT_KI = "Ki";
    public static final String MEM_UNIT_TI = "Ti";
    public static final String CPU_UNIT = "m";
    public static final String CPU_UNIT_N = "n";
    public static final String RESOURCE_NAME_TEMPLATE = "{}-{}";
    public static final String SUB_RESOURCE_NAME_TEMPLATE = "{}-{}-{}";
    public static final String SECRET_PWD_KEY = "jupyter.default.pwd";
    public static final String SECRET_URL_KEY = "jupyter.base.url";
    public static final String ENV_PWD_KEY = "JUPYTER_DEFAULT_PWD";
    public static final String PYTHONUNBUFFERED = "PYTHONUNBUFFERED";
    public static final String ENV_URL_KEY = "JUPYTER_BASE_URL";
    public static final String VOLUME_PREFIX = "volume-";
    public static final int RESOURCE_NAME_SUFFIX_LENGTH = MagicNumConstant.FIVE;
    public static final String TOKEN="token=";
    public static final String SVC_SUFFIX = "svc";
    public static final String INGRESS_SUFFIX = "ingress";
    public static final String SHM_NAME = "shm";
    public static final String SHM_MEDIUM = "Memory";
    public static final String SHM_MOUNTPATH = "/dev/shm";
    public static final String INGRESS_MAX_UPLOAD_SIZE = "100m";

    public static final String SECRET_TLS_TYPE = "kubernetes.io/tls";
    public static final String SECRET_TLS_TLS_CRT = "tls.crt";
    public static final String SECRET_TLS_TLS_KEY = "tls.key";
    /**
     * k8s hostPath 卷类型
     */
    public static final String HOST_PATH_TYPE = "Directory";

    //Ingress annotations key
    public static final String INGRESS_PROXY_BODY_SIZE_KEY = "nginx.ingress.kubernetes.io/proxy-body-size";
    public static final String INGRESS_CLASS_KEY = "kubernetes.io/ingress.class";
    public static final String INGRESS_SSL_REDIRECT_KEY = "nginx.ingress.kubernetes.io/ssl-redirect";
    public static final String INGRESS_BACKEND_PROTOCOL_KEY = "nginx.ingress.kubernetes.io/backend-protocol";
    /**
     * k8s资源对象名称校验正则表达式
     */
    public static final String K8S_RESOURCE_NAME_REGEX = "[a-z0-9]([-a-z0-9]*[a-z0-9])?";
    /**
     * resourceName最大长度
     */
    public static final Integer RESOURCE_NAME_LENGTH = 45;
    /**
     * podName 占位符
     */
    public static String POD_NAME_PLACEHOLDER = "pod-name-placeholder";

    /**
     * ResourceQuota cpu 限制key
     */
    public static final String RESOURCE_QUOTA_CPU_LIMITS_KEY = "limits.cpu";

    /**
     * ResourceQuota memory 限制key
     */
    public static final String RESOURCE_QUOTA_MEMORY_LIMITS_KEY = "limits.memory";
    /**
     * ResourceQuota gpu 限制key
     */
    public static final String RESOURCE_QUOTA_GPU_LIMITS_KEY = "requests.nvidia.com/gpu";

    //pod containerID 前缀
    public static final String CONTAINER_ID_PREFIX = "docker://";

    public static final String WAITING_REASON_CONTAINER_CREATING = "ContainerCreating";

}
