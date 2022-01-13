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
import org.dubhe.k8s.domain.bo.LogMonitoringBO;
import org.dubhe.k8s.domain.vo.LogMonitoringVO;

import java.util.List;


/**
 * @description 日志监控接口
 * @date 2020-07-03
 */
public interface LogMonitoringApi {

    /**
     * 添加Pod日志到ES,无日志参数，默认从k8s集群查询日志添加到ES
     *
     * @param podName Pod名称
     * @param namespace 命名空间
     * @return boolean 日志添加是否成功
     */
    boolean addLogsToEs(String podName,String namespace);

    /**
     * 添加Pod自定义日志到ES
     *
     * @param podName Pod名称
     * @param namespace 命名空间
     * @param logList 日志信息
     * @return boolean 日志添加是否成功
     */
    boolean addLogsToEs(String podName, String namespace, List<String> logList);

    /**
     * 日志查询方法
     *
     * @param from 日志查询起始值，初始值为1，表示从第一条日志记录开始查询
     * @param size 日志查询记录数
     * @param logMonitoringBo 日志查询bo
     * @return LogMonitoringVO 日志查询结果类
     */
    LogMonitoringVO searchLogByResName(int from, int size, LogMonitoringBO logMonitoringBo);

    /**
     * 日志查询方法
     *
     * @param from 日志查询起始值，初始值为1，表示从第一条日志记录开始查询
     * @param size 日志查询记录数
     * @param logMonitoringBo 日志查询bo
     * @return LogMonitoringVO 日志查询结果类
     */
    LogMonitoringVO searchLogByPodName(int from, int size, LogMonitoringBO logMonitoringBo);

    /**
     * Pod 日志总量查询方法
     *
     * @param logMonitoringBo 日志查询bo
     * @return long Pod 产生的日志总量
     */
    long searchLogCountByPodName(LogMonitoringBO logMonitoringBo);


    /**
     * 日志查询方法
     *
     * @param logMonitoringBo 日志查询bo
     * @return LogMonitoringVO 日志查询结果类
     */
    LogMonitoringVO searchLog(LogMonitoringBO logMonitoringBo);

    /**
     * 添加 TADL 服务日志到 Elasticsearch
     *
     * @param experimentId Experiment ID
     * @param log 日志
     * @return boolean 日志添加是否成功
     */
    boolean addTadlLogsToEs(long experimentId, String log);

    /**
     * TADL 服务日志查询方法
     *
     * @param from 日志查询起始值，初始值为1，表示从第一条日志记录开始查询
     * @param size 日志查询记录数
     * @param experimentId TADL Experiment ID
     * @return LogMonitoringVO 日志查询结果类
     */
    LogMonitoringVO searchTadlLogById(int from, int size, long experimentId);
}
