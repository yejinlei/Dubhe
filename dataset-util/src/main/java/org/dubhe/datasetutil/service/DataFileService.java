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
package org.dubhe.datasetutil.service;

import org.dubhe.datasetutil.domain.entity.DataFile;
import java.util.List;

/**
 * @description 文件服务
 * @date 2020-09-17
 */
public interface DataFileService {

    /**
     * 批量写入文件内容
     *
     * @param dataFiles 文件数据集合
     */
    void saveBatchDataFile(List<DataFile> dataFiles);

    /**
     * 创建新表
     *
     * @param tableName 表名称
     */
    void createNewTable(String tableName);

    /**
     * 删除数据集文件通过数据集ID
     *
     * @param datasetId 数据集ID
     */
    void deleteFileByDatasetId(long datasetId);
}
