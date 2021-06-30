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

package org.dubhe.notebook.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @description notebook配置
 * @date 2020-05-12
 */
@Component
@Data
@ConfigurationProperties(prefix = "notebook-specs")
public class NoteBookConfig {

    /**
     * cpu数量（核）
     */
    private Integer cpuNum;

    /**
     * gpu数量（核）
     */
    private Integer gpuNum;

    /**
     * 内存大小（M）
     */
    private Integer memNum;

    /**
     * 工作空间配额（m）
     */
    private Integer diskMemNum;

}
