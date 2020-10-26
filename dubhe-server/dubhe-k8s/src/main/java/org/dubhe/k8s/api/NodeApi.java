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

package org.dubhe.k8s.api;

import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.resource.BizNode;
import org.dubhe.k8s.enums.LackOfResourcesEnum;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description Node(k8s节点)操作接口
 * @date 2020-07-03
 */
public interface NodeApi {
    /**
     * 根据节点名称查询节点信息
     *
     * @param nodeName 节点名称
     * @return BizNode Node 业务类
     */
    BizNode get(String nodeName);

    /**
     * 查询所有节点信息
     *
     * @return List<BizNode> Node 业务类集合
     */
    List<BizNode> listAll();

    /**
     * 给节点添加单个标签
     *
     * @param nodeName 节点名称
     * @param labelKey 标签的键
     * @param labelValue 标签的值
     * @return PtBaseResult 基础结果类
     */
    PtBaseResult addLabel(String nodeName, String labelKey, String labelValue);

    /**
     * 给节点添加多个标签
     *
     * @param nodeName 节点名称
     * @param labels 标签Map
     * @return PtBaseResult 基础结果类
     */
    PtBaseResult addLabels(String nodeName, Map<String, String> labels);

    /**
     * 删除节点单个标签
     *
     * @param nodeName 节点名称
     * @param labelKey 标签的键
     * @return PtBaseResult 基础结果类
     */
    PtBaseResult deleteLabel(String nodeName, String labelKey);

    /**
     * 删除节点的多个标签
     *
     * @param nodeName 节点名称
     * @param labels 标签键集合
     * @return PtBaseResult 基础结果类
     */
    PtBaseResult deleteLabels(String nodeName, Set<String> labels);

    /**
     * 设置节点是否可调度
     *
     * @param nodeName 节点名称
     * @param schedulable 参数true或false
     * @return PtBaseResult 基础结果类
     */
    PtBaseResult schedulable(String nodeName, boolean schedulable);

    /**
     * 查询集群资源是否充足
     *
     * @param cpuNum 单位为m 1核等于1000m
     * @param memNum 单位为Mi 1Mi等于1024Ki
     * @param gpuNum 单位为显卡，即"1"表示1张显卡
     * @return LackOfResourcesEnum 资源缺乏枚举类
     */
    LackOfResourcesEnum isAllocatable(Integer cpuNum, Integer memNum, Integer gpuNum);



}
