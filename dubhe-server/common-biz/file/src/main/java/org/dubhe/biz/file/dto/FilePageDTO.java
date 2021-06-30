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

import lombok.Data;
import java.util.List;

/**
 * @description 文件分页查询相应实体
 * @date 2021-06-16
 */
@Data
public class FilePageDTO {

    /**
     * 查询路径
     */
    private String filePath;
    /**
     * 页码
     */
    private int pageNum;
    /**
     * 页容量
     */
    private int pageSize;
    /**
     * 记录数
     */
    private Long total;
    /**
     * 页集合
     */
    private List<FileDTO> rows;

}
