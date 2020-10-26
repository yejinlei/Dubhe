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
import org.dubhe.datasetutil.dao.DataVersionFileMapper;
import org.dubhe.datasetutil.domain.dto.DataVersionFile;
import org.dubhe.datasetutil.service.DataVersionFileService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description 数据集文件 服务实现类
 * @date 2020-09-17
 */
@Service
public class DataVersionFileServiceImpl  extends ServiceImpl <DataVersionFileMapper, DataVersionFile> implements DataVersionFileService {


    /**
     * 插入数据集文件数据
     *
     * @param listDataVersionFile 数据集文件数据集合
     */
    @Override
    public void saveBatchDataFileVersion(List<DataVersionFile> listDataVersionFile) {
       baseMapper.saveBatchDataFileVersion(listDataVersionFile);
    }
}
