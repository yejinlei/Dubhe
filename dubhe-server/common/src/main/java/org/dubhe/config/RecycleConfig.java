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
package org.dubhe.config;

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
     * 删除服务器无效文件(大文件)
     * 示例：rsync --delete-before -d /空目录 /需要回收的源目录
     */
    public static final String DEL_COMMAND = "ssh %s@%s \"mkdir -p %s; rsync --delete-before -d %s %s; rmdir %s %s\"";
}