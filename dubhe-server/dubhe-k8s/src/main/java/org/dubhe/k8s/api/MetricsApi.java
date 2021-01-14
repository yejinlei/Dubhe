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

import org.dubhe.k8s.domain.vo.PtContainerMetricsVO;
import org.dubhe.k8s.domain.vo.PtNodeMetricsVO;
import org.dubhe.k8s.domain.vo.PtPodsVO;

import java.util.List;

/**
 * @description 监控信息查询接口
 * @date 2020-07-03
 */
public interface MetricsApi {
    /**
     * 获取k8s所有节点当前cpu、内存用量
     *
     * @return List<PtNodeMetricsVO> NodeMetrics 结果类集合
     */
    List<PtNodeMetricsVO> getNodeMetrics();

    /**
     * 获取k8s所有Pod资源用量的实时信息
     *
     * @return List<PtPodsVO> Pod资源用量结果类集合
     */
    List<PtPodsVO> getPodsMetricsRealTime();

    /**
     * 获取k8s所有pod当前cpu、内存用量的实时使用情况
     *
     * @return List<PtPodsVO> Pod资源用量结果类集合
     */
    List<PtPodsVO> getPodMetricsRealTime();

    /**
     * 获取k8s resourceName 下pod当前cpu、内存用量的实时使用情况
     * @param namespace
     * @param resourceName
     * @return
     */
    List<PtPodsVO> getPodMetricsRealTime(String namespace,String resourceName);

    /**
     * 查询命名空间下所有Pod的cpu和内存使用
     *
     * @param namespace 命名空间
     * @return List<PtContainerMetricsVO> Pod资源用量结果类集合
     */
    List<PtContainerMetricsVO> getContainerMetrics(String namespace);

    /**
     * 查询所有命名空间下所有pod的cpu和内存使用
     *
     * @return List<PtContainerMetricsVO> Pod资源用量结果类集合
     */
    List<PtContainerMetricsVO> getContainerMetrics();

}
