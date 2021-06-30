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
package org.dubhe.datasetutil.service.impl;

import com.google.common.collect.Lists;
import org.dubhe.datasetutil.common.base.MagicNumConstant;
import org.dubhe.datasetutil.dao.DatasetDataLabelMapper;
import org.dubhe.datasetutil.domain.entity.DatasetDataLabel;
import org.dubhe.datasetutil.service.DatasetDataLabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description  数据集和标签服务实现类
 * @date 2020-10-14
 */
@Service
public class DatasetDataLabelServiceImpl implements DatasetDataLabelService {

    @Autowired
    private DatasetDataLabelMapper datasetDataLabelMapper;

    private static final int SUB_LENGTH = MagicNumConstant.FIVE_THOUSAND;

    /**
     * 批量保存数据集标签
     *
     * @param listDatasetDataLabel 数据集标签集合
     */
    @Override
    public void saveBatchDatasetDataLabel(List<DatasetDataLabel> listDatasetDataLabel) {
        if(listDatasetDataLabel.size() > SUB_LENGTH){
            List<List<DatasetDataLabel>> partitionList =  Lists.partition(listDatasetDataLabel,SUB_LENGTH);
            for (List<DatasetDataLabel> subPartitionList: partitionList) {
                datasetDataLabelMapper.saveBatchDatasetDataLabel(subPartitionList);
            }
        }else{
            datasetDataLabelMapper.saveBatchDatasetDataLabel(listDatasetDataLabel);
        }
    }


    /**
     * 删除数据集标签关系通过数据集ID
     *
     * @param datasetId 数据集ID
     */
    @Override
    public void deleteDatasetLabelByDatasetId(long datasetId) {
        datasetDataLabelMapper.deleteDatasetLabelByDatasetId(datasetId);
    }


}
