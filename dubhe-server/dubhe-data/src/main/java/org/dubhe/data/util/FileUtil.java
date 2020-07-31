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

package org.dubhe.data.util;

import org.dubhe.data.constant.Constant;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.service.DatasetService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;

/**
 * @description 文件工具
 * @date 2020-04-10
 */
@Component
public class FileUtil {

    @Value("${data.files.rootPath:dataset/}")
    private String datasetRootPath;
    @Resource
    @Lazy
    private DatasetService datasetService;

    /**
     * 获取数据集根路径
     *
     * @param datasetId 数据集ID
     * @return String 数据集根路径
     */
    public String getDatasetAbsPath(Long datasetId) {
        return datasetRootPath + datasetId;
    }

    /**
     * 获取数据集标注文件地址
     *
     * @param datasetId 数据集id
     * @return String   数据集标注文件地址
     */
    public String getAnnotationDirAbsPath(Long datasetId) {
        return getDatasetAbsPath(datasetId) + File.separator + Constant.DATASET_ANNOTATION_PATH;
    }

    /**
     * 获取数据集指定文件标注地址(支持多版本)
     *
     * @param datasetId 数据集ID
     * @param fileName  文件名称
     * @return String 数据集指定文件标注地址(支持多版本)
     */
    public String getAnnotationAbsPath(Long datasetId, String fileName) {
        Dataset dataset = datasetService.getOneById(datasetId);
        return getAnnotationDirAbsPath(datasetId) +
                (StringUtils.isEmpty(dataset.getCurrentVersionName()) ? "" : dataset.getCurrentVersionName() + File.separator)
                + fileName;
    }

}
