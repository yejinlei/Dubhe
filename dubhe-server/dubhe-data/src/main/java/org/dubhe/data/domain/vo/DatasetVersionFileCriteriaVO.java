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

package org.dubhe.data.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dubhe.biz.db.annotation.Query;

/**
 * @description 数据集版本文件查询条件
 * @date 2020-05-26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DatasetVersionFileCriteriaVO {

    @Query(type = Query.Type.EQ, propName = "dataset_id")
    private Long datasetId;

    @Query(type = Query.Type.IN, propName = "annotation_status")
    private int annotationStatus;

}
