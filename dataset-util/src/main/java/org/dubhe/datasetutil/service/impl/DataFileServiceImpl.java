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
import lombok.extern.slf4j.Slf4j;
import org.dubhe.datasetutil.common.constant.BusinessConstant;
import org.dubhe.datasetutil.dao.DataFileMapper;
import org.dubhe.datasetutil.domain.entity.DataFile;
import org.dubhe.datasetutil.service.DataFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description 数据集文件 服务实现类
 * @date 2020-09-17
 */
@Slf4j
@Service
public class DataFileServiceImpl extends ServiceImpl<DataFileMapper, DataFile> implements DataFileService {

    @Autowired
    private DataFileMapper dataFileMapper;

    /**
     * 批量写入文件内容
     *
     * @param listDataFile 文件数据集合
     */
    @Override
    public void saveBatchDataFile(List<DataFile> listDataFile) {
        int listSize = listDataFile.size();
        if (listSize > BusinessConstant.SUB_LENGTH) {
            List<List<DataFile>> partitionList = Lists.partition(listDataFile, BusinessConstant.SUB_LENGTH);
            for (List<DataFile> subPartitionList : partitionList) {
                dataFileMapper.saveBatchDataFile(subPartitionList);
            }
        } else {
            dataFileMapper.saveBatchDataFile(listDataFile);
        }
    }

}
