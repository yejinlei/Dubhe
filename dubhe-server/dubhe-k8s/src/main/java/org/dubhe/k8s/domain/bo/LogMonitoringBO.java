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

package org.dubhe.k8s.domain.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

/**
 * @description LogMonitoringBO实体类
 * @date 2020-05-12
 */
@Data
@Accessors(chain = true)
public class LogMonitoringBO {
    /**
     * 索引库名称
     **/
    private String indexName;
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

}
