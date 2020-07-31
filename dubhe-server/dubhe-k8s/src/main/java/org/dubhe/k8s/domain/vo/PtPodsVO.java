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

package org.dubhe.k8s.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description 封装pod信息
 * @date 2020-06-03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PtPodsVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * pod名称
     **/
    private String podName;
    /**
     * cpu用量
     **/
    private String cpuUsageAmount;
    /**
     * cpu用量 单位 1核=1000m,1m=1000000n
     **/
    private String cpuUsageFormat;
    /**
     * 内存用量
     **/
    private String memoryUsageAmount;
    /**
     * 内存用量单位
     **/
    private String memoryUsageFormat;
    /****/
    private String nodeName;
    /**
     * 设置status状态值
     **/
    private String status;
    /**
     * 设置gpu的使用情况
     **/
    private String gpuUsed;
}
