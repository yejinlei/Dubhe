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
package org.dubhe.k8s.domain.bo;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.enums.BizEnum;
import org.dubhe.k8s.domain.dto.PodLogQueryDTO;

import java.util.Set;
/**
 * @description LogMonitoringBO实体类
 * @date 2020-05-12
 */
@Data
@Accessors(chain = true)
public class LogMonitoringBO {
    /**
     * 命名空间
     **/
    private String namespace;
    /**
     * 资源名称
     **/
    private String resourceName;
    /**
     * pod名称
     **/
    private String podName;
    /**
     * pod名称,一个resourceName可能对应多个podName
     **/
    private Set<String> podNames;
    /**
     * 日志查询条件：关键字
     **/
    private String logKeyword;
    /**
     * 日志查询时间范围：开始时间
     **/
    private Long beginTimeMillis;
    /**
     * 日志查询时间范围：结束时间
     **/
    private Long endTimeMillis;

    /**
     * 日志查询起始行
     **/
    private Integer from;

    /**
     * 日志查询行数
     **/
    private Integer size;

    /**
     * 业务标签,用于标识一个组的业务模块 比如:TRAIN模块的trainId, TADL模块的experimentId
     */
    private String businessGroupId;

    /**
     * 业务标签,用于标识业务模块
     */
    private BizEnum business;

    public LogMonitoringBO(String namespace,String podName){
        this.namespace = namespace;
        this.podName = podName;
    }

    public LogMonitoringBO(String namespace, PodLogQueryDTO podLogQueryDTO){
        this.namespace = namespace;
        this.podName = podLogQueryDTO.getPodName();
        this.logKeyword = podLogQueryDTO.getLogKeyword();
        this.beginTimeMillis = podLogQueryDTO.getBeginTimeMillis();
        this.endTimeMillis = podLogQueryDTO.getEndTimeMillis();
    }

    public LogMonitoringBO(){
    }
}