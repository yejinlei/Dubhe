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
import org.dubhe.annotation.Query;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

/**
 * @description 文件查询条件
 * @date 2020-04-10
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileQueryCriteriaVO {

    @Query(type = Query.Type.IN, propName = "id")
    private Set<Long> ids;

    @Query(type = Query.Type.IN)
    private Set<Integer> status;

    @Query(type = Query.Type.LIKE)
    private String name;

    @Query(type = Query.Type.EQ)
    private String url;

    @Query(type = Query.Type.BETWEEN, propName = "create_time")
    private List<Timestamp> createTime;

    @Query(type = Query.Type.EQ, propName = "dataset_id")
    private Long datasetId;

    @Query(type = Query.Type.ORDER_BY)
    private String order;

    @Query(type = Query.Type.EQ, propName = "file_type")
    private Integer fileType;

    @Query(type = Query.Type.IN, propName = "create_user_id")
    private Set<Long> createUserIds;

}
