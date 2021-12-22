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
package org.dubhe.recycle.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @description 垃圾回收机制配置常量
 * @date 2020-09-21
 */
@Data
@Component
@ConfigurationProperties(prefix = "recycle.timeout")
public class RecycleConfig {

    /**
     * 回收无效文件的默认有效时长
     */
    private Integer date;

    /**
     * 用户上传文件至临时路径下后文件最大有效时长，以小时为单位
     */
    private Integer fileValid;

    /**
     * 用户删除某一算法后，其算法文件最大有效时长，以天为单位
     */
    private Integer algorithmValid;

    /**
     * 用户删除某一模型后，其模型文件最大有效时长，以天为单位
     */
    private Integer modelValid;

    /**
     * 用户删除训练任务后，其训练管理文件最大有效时长，以天为单位
     */
    private Integer trainValid;

    /**
     * 用户删除度量文件后，其度量文件最大有效时长，以天为单位
     */
    private Integer measureValid;

    /**
     * 用户删除镜像后，其镜像最大有效时长，以天为单位
     */
    private Integer imageValid;

    /**
     * 回收serving相关文件后，回收文件最大有效时长，以天为单位
     */
    private Integer servingValid;
    /**
     * 用户删除tadl算法版本文件后，文件最大有效时长，以天为单位
     */
    private Integer tadlValid;

}