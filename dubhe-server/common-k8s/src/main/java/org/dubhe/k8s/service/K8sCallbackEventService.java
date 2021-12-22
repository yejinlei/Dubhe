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
package org.dubhe.k8s.service;

import org.dubhe.k8s.domain.entity.K8sCallbackEvent;
import org.dubhe.k8s.domain.vo.K8sEventVO;
import org.dubhe.k8s.domain.vo.K8sResourceEventResultVO;

import java.util.List;

/**
 * @description:
 * @date: 2021/11/15
 */
public interface K8sCallbackEventService {
    /**
     * 插入k8s callback event事件的信息
     * @param k8sCallbackEvent
     * @return
     */
    boolean insertOrUpdate(K8sCallbackEvent k8sCallbackEvent);


    /**
     * 根据resourceName列表查询对应的历史事件，返回resourceName和事件列表对应关系
     * @param resourceNames
     * @return
     */
    List<K8sResourceEventResultVO> batchQueryByResourceName(List<String> resourceNames);

    /**
     * 根据resourceName列表查询对应的所有历史事件
     * @param resourceNames
     * @return
     */
    List<K8sEventVO> queryByResourceName(List<String> resourceNames);

    /**
     * 根据resourceName和businessType删除单条记录
     * @param resourceName
     * @param businessType
     * @return
     */
    boolean delete(String resourceName, String businessType);
}
