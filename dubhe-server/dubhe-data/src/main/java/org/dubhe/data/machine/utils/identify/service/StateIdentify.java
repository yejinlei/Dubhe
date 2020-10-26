package org.dubhe.data.machine.utils.identify.service;

import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.machine.enums.DataStateEnum;


/**
 * @description 状态判断接口
 * @date 2020-09-24
 */
public interface StateIdentify {

    /**
     * 获取数据集状态(指定版本)
     *
     * @param datasetId                 数据集id
     * @param versionName               数据集版本名称
     * @param needFileStateDoIdentify   是否需要查询文件状态判断
     * @return DatasetStatusEnum        数据集状态(指定版本)
     */
    DataStateEnum getStatus(Long datasetId, String versionName, boolean needFileStateDoIdentify);

    /**
     * 获取数据集状态(未指定版本)
     *
     * @param dataset                   数据集
     * @param needFileStateDoIdentify   是否需要查询文件状态判断
     * @return DatasetStatusEnum        数据集状态(指定版本)
     */
    DataStateEnum getStatus(Dataset dataset, boolean needFileStateDoIdentify);


    /**
     * 获取数据集状态(自动标注/目标跟踪回滚使用)
     *
     * @param datasetId                 数据集id
     * @param versionName               数据集版本名称
     * @return DatasetStatusEnum        数据集状态(指定版本)
     */
    DataStateEnum getStatusForRollback(Long datasetId, String versionName);


}
