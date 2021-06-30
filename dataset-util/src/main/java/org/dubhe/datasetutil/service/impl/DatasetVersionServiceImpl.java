package org.dubhe.datasetutil.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.datasetutil.dao.DatasetVersionMapper;
import org.dubhe.datasetutil.domain.entity.DatasetVersion;
import org.dubhe.datasetutil.service.DatasetVersionService;
import org.springframework.stereotype.Service;

/**
 * @description 版本数据处理
 * @date 2021-03-23
 */
@Service
public class DatasetVersionServiceImpl extends ServiceImpl<DatasetVersionMapper, DatasetVersion> implements DatasetVersionService {

    @Override
    public DatasetVersion getByDatasetIdAndVersionNum(Long datasetId, String versionNum) {
        QueryWrapper<DatasetVersion> queryWrapper = new QueryWrapper<DatasetVersion>();
        queryWrapper.eq("dataset_id", datasetId);
        queryWrapper.eq("version_name", versionNum);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public void insertVersion(Long datasetId, String versionNum, String versionNote) {
        baseMapper.insert(new DatasetVersion(datasetId, versionNum, versionNote));
    }

}
