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

package org.dubhe.data.service.dataset;

import org.dubhe.enums.DatasetTypeEnum;
import org.dubhe.data.domain.dto.DatasetCreateDTO;
import org.dubhe.data.domain.dto.DatasetVersionCreateDTO;
import java.util.UUID;

/**
 * @description 使用简单工厂模式 生成测试数据
 * @date 2020-05-15 09:42
 */
public class DataFactory {

    /**
     * 创建一个随机名称的数据集
     * @return
     */
    public static DatasetCreateDTO createDataset() {
        DatasetCreateDTO dataset = new DatasetCreateDTO();
        // 标注类型：0分类,1目标检测
        dataset.setAnnotateType(0);
        // 数据类型:0图片，1视频
        dataset.setDataType(0);
        dataset.setName(UUID.randomUUID().toString() + "_数据集");
        dataset.setRemark("测试备注");
        // 类型 0: private 私有数据,  1:team  团队数据  2:public 公开数据
        dataset.setType(DatasetTypeEnum.PRIVATE.getValue());
        return dataset;
    }

    /**
     * 数据集版本发布用
     * @param id           数据集ID
     * @param versionNum   版本名称
     * @param versionNote  版本说明
     * @return
     */
    public static DatasetVersionCreateDTO datasetVersionPublish(Long id, String versionNum, String versionNote) {
        DatasetVersionCreateDTO datasetVersionCreateDTO = new DatasetVersionCreateDTO();
        datasetVersionCreateDTO.setDatasetId(id);
        datasetVersionCreateDTO.setVersionName(versionNum);
        datasetVersionCreateDTO.setVersionNote(versionNote);
        return datasetVersionCreateDTO;
    }

}
