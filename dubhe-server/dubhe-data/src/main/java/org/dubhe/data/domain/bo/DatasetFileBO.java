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

package org.dubhe.data.domain.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * @description 数据集文件Bo
 * @date 2020-06-28
 */
@Data
public class DatasetFileBO implements Serializable {

    /**
     * 图片标注文件地址
     */
    private String annotationPath;

    /**
     * 图片地址
     */
    private String filePath;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 文件状态
     */
    private Integer annotationStatus;

    /**
     * 图片宽
     */
    private Integer width;

    /**
     * 图片高
     */
    private Integer height;

    public DatasetFileBO() {
    }

    public DatasetFileBO(String filePath, String annotationPath, Long fileId, Integer annotationStatus, Integer width, Integer height) {
        this.filePath = filePath;
        this.annotationPath = annotationPath;
        this.fileId = fileId;
        this.annotationStatus = annotationStatus;
        this.width = width;
        this.height = height;
    }

}
