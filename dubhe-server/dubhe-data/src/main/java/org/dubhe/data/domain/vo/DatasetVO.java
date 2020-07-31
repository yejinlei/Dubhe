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

package org.dubhe.data.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.domain.dto.TeamSmallDTO;
import org.dubhe.domain.dto.UserSmallDTO;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description 数据集VO
 * @date 2020-04-10
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DatasetVO implements Serializable {

    private Long id;

    private String name;

    private String remark;

    private Integer type;
    private String uri;
    private Integer dataType;
    private Integer annotateType;
    private Integer status;
    private Timestamp createTime;
    private Timestamp updateTime;
    private TeamSmallDTO team;
    private UserSmallDTO createUser;
    private UserSmallDTO updateUser;
    private ProgressVO progress;
    private String currentVersionName;
    private boolean isImport;
    private Integer decompressState;

    public static DatasetVO from(Dataset dataset) {
        DatasetVO datasetVO = new DatasetVO();
        if (dataset == null) {
            return null;
        }
        datasetVO.setId(dataset.getId());
        datasetVO.setName(dataset.getName());
        datasetVO.setRemark(dataset.getRemark());
        datasetVO.setCreateTime(dataset.getCreateTime());
        datasetVO.setUpdateTime(dataset.getUpdateTime());
        datasetVO.setType(dataset.getType());
        datasetVO.setDataType(dataset.getDataType());
        datasetVO.setAnnotateType(dataset.getAnnotateType());
        datasetVO.setStatus(dataset.getStatus());
        datasetVO.setDecompressState(dataset.getDecompressState());
        datasetVO.setImport(dataset.isImport());
        return datasetVO;
    }

}
