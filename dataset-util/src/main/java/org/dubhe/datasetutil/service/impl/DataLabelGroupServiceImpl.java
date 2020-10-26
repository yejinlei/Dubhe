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
import org.dubhe.datasetutil.dao.DataGroupLabelMapper;
import org.dubhe.datasetutil.dao.DataLabelGroupMapper;
import org.dubhe.datasetutil.domain.entity.DataGroupLabel;
import org.dubhe.datasetutil.domain.entity.DataLabelGroup;
import org.dubhe.datasetutil.service.DataLabelGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @description 数据集标签组服务实现接口
 * @date 2020-10-14
 */
@Service
public class DataLabelGroupServiceImpl extends ServiceImpl<DataGroupLabelMapper, DataGroupLabel> implements DataLabelGroupService {

    @Autowired
    private DataLabelGroupMapper dataLabelGroupMapper;

    /**
     * 根据标签组名查询
     *
     * @param labelGroupName 标签组名称
     * @return int 数量
     */
    @Override
    public int selectByLabelGroupName(String labelGroupName) {
        return dataLabelGroupMapper.selectByLabelGroupName(labelGroupName);
    }

    /**
     * 保存标签组名称
     *
     * @param dataLabelGroup 标签组名称
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDataGroupLabel(DataLabelGroup dataLabelGroup) {
        dataLabelGroupMapper.insert(dataLabelGroup);
    }
}
