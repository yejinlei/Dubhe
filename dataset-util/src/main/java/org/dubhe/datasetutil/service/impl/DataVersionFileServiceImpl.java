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
import org.dubhe.datasetutil.common.constant.BusinessConstant;
import org.dubhe.datasetutil.dao.DataVersionFileMapper;
import org.dubhe.datasetutil.domain.entity.DataVersionFile;
import org.dubhe.datasetutil.service.DataVersionFileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @description 数据集文件 服务实现类
 * @date 2020-09-17
 */
@Service
public class DataVersionFileServiceImpl extends ServiceImpl<DataVersionFileMapper, DataVersionFile> implements DataVersionFileService {


    /**
     * 插入数据集文件数据
     *
     * @param listDataVersionFile 数据集文件数据集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatchDataFileVersion(List<DataVersionFile> listDataVersionFile) {
        baseMapper.saveBatchDataFileVersion(listDataVersionFile);
    }


    /**
     * 创建新表
     *
     * @param tableName 表名称
     */
    @Override
    public void createNewTable(String tableName){
        int count = baseMapper.selectCountByTableName(tableName);
        if(count == 0){
            if((BusinessConstant.DATA_DATASET_VERSION_FILE+BusinessConstant.TABLE_SUFFIX).equals(tableName)){
                baseMapper.createNewTableOne();
            }else {
                baseMapper.createNewTableTwo();
            }

        }
    }

    /**
     * 删除数据集版本通过数据集ID
     *
     * @param datasetId 数据集ID
     */
    @Override
    public void deleteVersionByDatasetId(long datasetId) {
        baseMapper.deleteVersionByDatasetId(datasetId);
        baseMapper.deleteVersionFileByDatasetId(datasetId);
    }
}
