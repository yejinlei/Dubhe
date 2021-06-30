package org.dubhe.datasetutil.service;

import org.dubhe.datasetutil.domain.entity.DatasetVersion;

/**
 * @description TODO
 * @date 2021-03-23
 */
public interface DatasetVersionService {

    DatasetVersion getByDatasetIdAndVersionNum(Long datasetId, String versionNum);

    void insertVersion(Long datasetId, String versionNum, String versionNote);

}
