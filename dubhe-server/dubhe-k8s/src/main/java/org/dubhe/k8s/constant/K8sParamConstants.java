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

package org.dubhe.k8s.constant;

import org.dubhe.base.MagicNumConstant;

/**
 * @description K8s param constants
 * @date 2020-04-22
 */
public class K8sParamConstants {
    public static final String GPU_RESOURCE_KEY = "nvidia.com/gpu";
    public static final String QUANTITY_CPU_KEY = "cpu";
    public static final String QUANTITY_MEMORY_KEY = "memory";
    public static final String NODE_READY_TRUE = "True";
    public static final String NODE_STATUS_TRUE = "Ready";
    public static final String NODE_STATUS_FALSE = "NotReady";
    public static final String MEM_UNIT = "Mi";
    public static final String CPU_UNIT = "m";
    public static final String RESOURCE_NAME_TEMPLATE = "{}-{}";
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

    public static final String INGRESS_PROXY_BODY_SIZE_KEY = "nginx.ingress.kubernetes.io/proxy-body-size";
    /**
     * k8s资源对象名称校验正则表达式
     */
    public static final String K8S_RESOURCE_NAME_REGEX = "[a-z0-9]([-a-z0-9]*[a-z0-9])?";
    /**
     * resourceName最大长度
     */
    public static final Integer RESOURCE_NAME_LENGTH = 45;
}
