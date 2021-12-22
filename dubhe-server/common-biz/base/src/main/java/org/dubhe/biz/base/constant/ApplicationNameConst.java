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
package org.dubhe.biz.base.constant;

/**
 * @description 服务名常量类 spring.application.name
 * @date 2020-11-05
 */
public class ApplicationNameConst {
    private ApplicationNameConst() {

    }

    /**
     * 授权中心
     */
    public final static String SERVER_AUTHORIZATION = "auth";
    /**
     * 网关
     */
    public final static String SERVER_GATEWAY = "gateway";
    /**
     * 模型开发
     */
    public final static String SERVER_NOTEBOOK = "dubhe-notebook";
    /**
     * 算法管理
     */
    public final static String SERVER_ALGORITHM = "dubhe-algorithm";
    /**
     * 模型管理
     */
    public final static String SERVER_MODEL = "dubhe-model";
    /**
     * 系统管理
     */
    public final static String SERVER_ADMIN = "admin";
    /**
     * 镜像管理
     */
    public final static String SERVER_IMAGE = "dubhe-image";
    /**
     * 度量管理
     */
    public final static String SERVER_MEASURE = "dubhe-measure";
    /**
     * 训练管理
     */
    public final static String SERVER_TRAIN = "dubhe-train";
    /**
     * 模型优化
     */
    public final static String SERVER_OPTIMIZE = "dubhe-optimize";
    /**
     * 数据集管理
     */
    public final static String SERVER_DATA = "dubhe-data";

    /**
     * 云端Serving
     */
    public final static String SERVER_SERVING = "dubhe-serving";


    /**
     * 医学服务
     */
    public final static String SERVER_DATA_DCM = "dubhe-data-dcm";

    /**
     * TADL
     */
    public final static String SERVER_TADL = "dubhe-tadl";
    /**
     * k8s
     */
    public final static String SERVER_K8S = "dubhe-k8s";

    /**
     * 专业版终端
     */
    public final static String TERMINAL = "dubhe-terminal";
}
