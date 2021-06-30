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

package org.dubhe.optimize.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.biz.db.annotation.Query;
import org.dubhe.biz.db.base.PageQueryBase;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @description 模型优化任务实例查询条件
 * @date 2020-05-22
 */
@Data
public class ModelOptTaskInstanceQueryDTO extends PageQueryBase implements Serializable {
    @ApiModelProperty("任务ID")
    @Query(propName = "task_id")
    private Long taskId;
    /**
     * 任务状态
     */
    @ApiModelProperty("任务状态")
    @Query
    private String status;
    /**
     * 优化算法
     */
    @ApiModelProperty("优化算法")
    @Query(propName = "algorithm_name")
    private String algorithmName;
    /**
     * 提交时间
     */
    @ApiModelProperty("提交时间")
    private List<Long> createTime;

    @ApiModelProperty(required = false, hidden = true)
    @Query(type = Query.Type.BETWEEN, propName = "create_time")
    private List<Timestamp> createTimeSearch;
    /**
     * 完成时间
     */
    @ApiModelProperty("完成时间")
    @Query(propName = "end_time", type = Query.Type.BETWEEN)
    private List<Timestamp> endTime;

    public void timeConvert() {
        if (!CollectionUtils.isEmpty(this.createTime)) {
            createTimeSearch = new ArrayList<>(createTime.size());
            createTime.forEach(aLong -> createTimeSearch.add(new Timestamp(aLong)));
        }
    }
}
