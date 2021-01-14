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

import lombok.*;
import org.dubhe.data.domain.entity.File;

import java.io.Serializable;

/**
 * @description 文件Bo
 * @date 2020-04-10
 */
@Builder
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class FileBO implements Serializable {

    private Long id;
    private String name;
    private Long datasetId;
    private String url;

    /**
     * 把File对象转换为FileBO对象
     *
     * @param file 文件对象
     * @return 转换后的fileBO
     */
    public static FileBO from(File file) {
        FileBO fileBO = new FileBO();
        fileBO.setName(file.getName());
        fileBO.setId(file.getId());
        fileBO.setDatasetId(file.getDatasetId());
        fileBO.setUrl(file.getUrl());
        return fileBO;
    }

}
