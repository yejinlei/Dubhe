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

import org.dubhe.k8s.domain.bo.LogMonitoringBO;
import org.dubhe.k8s.domain.vo.LogMonitoringVO;


/**
 * @description 日志监控接口
 * @date 2020-07-03
 */
public interface LogMonitoringApi {

    /**
     * 添加日志到ES
     *
     * @param podName Pod名称
     * @param namespace 命名空间
     * @return boolean 日志添加是否成功
     */
    boolean addLogsToEs(String podName,String namespace);

    /**
     * 日志查询方法
     *
     * @param from 日志查询起始值，初始值为1，表示从第一条日志记录开始查询
     * @param size 日志查询记录数
     * @param logMonitoringBo 日志查询bo
     * @return LogMonitoringVO 日志查询结果类
     */
    LogMonitoringVO searchLog(int from, int size, LogMonitoringBO logMonitoringBo);

}
