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
import org.dubhe.datasetutil.common.constant.BusinessConstant;
import org.dubhe.datasetutil.dao.DataLabelMapper;
import org.dubhe.datasetutil.domain.entity.DataLabel;
import org.dubhe.datasetutil.service.DataLabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description 数据集标签服务接口实现
 * @date 2020-10-14
 */
@Service
public class DataLabelServiceImpl implements DataLabelService {

    @Autowired
    private DataLabelMapper dataLabelMapper;

    /**
     * 批量保存数据集标签 (分批)
     *
     * @param listDataLabel 数据集标签
     */
    @Override
    public void saveBatchDataLabel(List<DataLabel> listDataLabel) {
        int listSize = listDataLabel.size();
        if(listSize > BusinessConstant.SUB_LENGTH){
           List<List<DataLabel>> partitionList =  Lists.partition(listDataLabel,BusinessConstant.SUB_LENGTH);
            for (List<DataLabel> subPartitionList: partitionList) {
                dataLabelMapper.saveBatchDataLabel(subPartitionList);
            }
        }else{
            dataLabelMapper.saveBatchDataLabel(listDataLabel);
        }
    }
}
