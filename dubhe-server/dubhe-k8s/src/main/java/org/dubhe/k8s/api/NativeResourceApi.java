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
package org.dubhe.k8s.api;

import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;

/**
 * @description Kubernetes 原生资源对象操作
 * @date 2020-08-28
 */
public interface NativeResourceApi {
    /**
     * 通过 yaml创建
     * @param crYaml cr定义yaml脚本
     * @return List<HasMetadata>
     */
    List<HasMetadata> create(String crYaml);

    /**
     * 通过 yaml删除
     * @param crYaml cr定义yaml脚本
     * @return boolean true删除成功 false删除失败
     */
    boolean delete(String crYaml);
}
