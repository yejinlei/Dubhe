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

package org.dubhe.biz.file.dto;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * @description 文件详情
 * @date 2021-05-07
 */
@Builder
@Data
public class FileDTO implements Serializable {

    /**
     * 文件名称
     */
    private String name;
    /**
     * 文件路径
     */
    private String path;
    /**
     * 文件最近一次修改时间
     */
    private Date lastModified;
    /**
     * 文件大小
     */
    private long size;
    /**
     * 是否文件夹
     */
    private boolean dir;

}
