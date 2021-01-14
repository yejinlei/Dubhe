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

import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.bo.DistributeTrainBO;
import org.dubhe.k8s.domain.resource.BizDistributeTrain;

/**
 * @description k8s中资源为DistributeTrain的操作接口
 * @date 2020-07-07
 */
public interface DistributeTrainApi {
    BizDistributeTrain create(DistributeTrainBO bo);

    /**
     * 删除
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return PtBaseResult
     */
    PtBaseResult deleteByResourceName(String namespace, String resourceName);

    /**
     * 根据namespace和resourceName查询cr信息
     *
     * @param namespce 命令空间
     * @param resourceName 资源名称
     * @return BizDistributeTrain  自定义资源转换类集合
     */
    BizDistributeTrain get(String namespce,String resourceName);
    /**
     * 根据名称查cr
     *
     * @param crName 自定义资源名称
     * @return BizDistributeTrain 自定义资源类
     */
    BizDistributeTrain findDisByName(String crName);

    /**
     * 通过 yaml创建
     * @param crYaml cr定义yaml脚本
     * @return BizDistributeTrain 自定义资源类
     */
    BizDistributeTrain create(String crYaml);

    /**
     * 通过 yaml删除
     * @param crYaml cr定义yaml脚本
     * @return boolean true 删除成功 false删除失败
     */
    boolean delete(String crYaml);
}
