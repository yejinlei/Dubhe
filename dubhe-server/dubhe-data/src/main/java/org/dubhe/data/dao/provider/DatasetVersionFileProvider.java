/**
 * Copyright 2020 Tianshu AI Platform. All Rights Reserved.
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

package org.dubhe.data.dao.provider;

import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.data.constant.Constant;
import org.dubhe.data.constant.DataStatusEnum;
import org.dubhe.data.constant.FileTypeEnum;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description DatasetVersionFile sql构建类
 * @date 2020-05-15
 */
public class DatasetVersionFileProvider {

    /**
     * 删除图片(版本)
     *
     * @param param 查询参数
     * @return String sql
     */
    public static String deleteShip(Map<String, Object> param) {
        String versionName = param.get("versionName") == null ? null : (String) param.get("versionName");
        List<Long> fileIds = (List) param.get("fileIds");
        String sql = "update data_dataset_version_file set status = 1,changed = 1 " +
                "where dataset_id = %d and file_id in (%s) and version_name " + (versionName == null ? "is null" : " = '%s'");
        Long datasetId = (Long) param.get("datasetId");
        return String.format(sql, datasetId, StringUtils.join(fileIds, ","), versionName);
    }

    /**
     * 新增关系版本变更
     *
     * @param param 查询参数
     * @return String sql
     */
    public static String newShipVersionNameChange(Map<String, Object> param) {
        Long datasetId = (Long) param.get("datasetId");
        String versionSource = param.get("versionSource") == null ? null : (String) param.get("versionSource");
        String versionTarget = (String) param.get("versionTarget");
        String sql = "update data_dataset_version_file set status = 2,backup_status = annotation_status, version_name = '%s' " +
                "where dataset_id = %d and status = 0 and version_name " + (versionSource == null ? " is null " : " = '%s'");
        return String.format(sql, versionTarget, datasetId, versionSource);
    }

    /**
     * 按数据集和版本查找文件状态列表
     *
     * @param param 查询参数
     * @return String sql
     */
    public static String findFileStatusListByDatasetAndVersion(Map<String, Object> param) {
        Long datasetId = (Long) param.get("datasetId");
        String versionName = param.get("versionName") == null ? null : (String) param.get("versionName");
        String sql = "select distinct annotation_status from data_dataset_version_file " +
                "where dataset_id = %d " + (versionName == null ? "" : "and version_name  = '%s'") + " and status !=1";
        return String.format(sql, datasetId, versionName);
    }

    /**
     * 回退图片状态
     *
     * @param param 查询参数
     * @return String sql
     */
    public static String rollbackFileAndAnnotationStatus(Map<String, Object> param) {
        Long datasetId = (Long) param.get("datasetId");
        String versionName = String.valueOf(param.get("versionName"));
        int changed = (int) param.get("changed");
        String sql = "update data_dataset_version_file " +
                "set " +
                "status=(case status " +
                "when " + DataStatusEnum.ADD.getValue() + " then " + DataStatusEnum.DELETE.getValue() +
                " when " + DataStatusEnum.DELETE.getValue() + " then " + DataStatusEnum.NORMAL.getValue() +
                " else status end )," +
                "annotation_status = backup_status," +
                "changed = " + Constant.UNCHANGED +
                " where dataset_id= " + datasetId + " and version_name= '" + versionName + "' and changed = " + changed;
        return sql;
    }


    /**
     * 分页查询数据集文件中间表
     *
     * @param param 查询参数
     * @return String sql
     */
    public static String getListByDatasetIdAndAnnotationStatus(Map<String, Object> param) {
        Long datasetId = Long.parseLong(param.get("datasetId").toString());
        String versionName = param.get("versionName") == null ? " is null " : " = '"+ param.get("versionName")+"'";
        Set<Integer> status = FileTypeEnum.getStatus((Integer) param.get("status"));
        Long offset = Long.parseLong(param.get("offset").toString());
        Integer limit = (Integer) param.get("limit");
        return new StringBuffer()
                .append("select * from data_dataset_version_file a INNER JOIN(select id from data_dataset_version_file where ")
                .append("dataset_id=").append(datasetId)
                .append("   and status in (").append(DataStatusEnum.ADD.getValue()).append(",").append(DataStatusEnum.NORMAL.getValue()).append(")  ")
                .append(status == null || status.isEmpty() ? "" : "   and annotation_status in (" + status.toString().replace("[", "").replace("]", "") + ") ")
                .append("   and version_name ").append(versionName)
                .append("   limit ").append(offset).append(",").append(limit)
                .append(") b on a.id = b.id")
                .toString();
    }


}
