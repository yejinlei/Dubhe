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

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import org.dubhe.datasetutil.common.constant.BusinessConstant;
import org.dubhe.datasetutil.dao.DataGroupLabelMapper;
import org.dubhe.datasetutil.domain.entity.DataGroupLabel;
import org.dubhe.datasetutil.service.DataGroupLabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description 标签与标签组的服务接口实现
 * @date 2020-10-21
 */
@Service
public class DataGroupLabelServiceImpl extends ServiceImpl<DataGroupLabelMapper, DataGroupLabel> implements DataGroupLabelService {

    @Autowired
    private DataGroupLabelMapper dataGroupLabelMapper;

    /**
     * 插入标签组
     *
     * @param listDataGroupLabel 标签组数据
     */
    @Override
    public void saveDataGroupLabel(List<DataGroupLabel> listDataGroupLabel) {
        int listSize = listDataGroupLabel.size();
        if (listSize > BusinessConstant.SUB_LENGTH) {
            List<List<DataGroupLabel>> partitionList = Lists.partition(listDataGroupLabel, BusinessConstant.SUB_LENGTH);
            for (List<DataGroupLabel> subPartitionList : partitionList) {
                dataGroupLabelMapper.saveDataGroupLabel(subPartitionList);
            }
        } else {
            dataGroupLabelMapper.saveDataGroupLabel(listDataGroupLabel);
        }
    }
}
