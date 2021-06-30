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
package org.dubhe.recycle.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.db.base.PageQueryBase;

import java.io.Serializable;
import java.util.List;

/**
 * @description 回收任务列表查询条件
 * @date 2020-09-23
 */
@Data
@Accessors(chain = true)
public class RecycleTaskQueryDTO extends PageQueryBase implements Serializable {
    private static final long serialVersionUID = 2016225581479036412L;

    /**
     * 具体值参考RecycleStatusEnum
     */
    @ApiModelProperty(value = "回收任务状态(0:待回收，1:已回收，2:回收失败，3：回收中，4：还原中，5：已还原)")
    private Integer recycleStatus;

    @ApiModelProperty(value = "回收类型(0文件，1数据库表数据)")
    private Integer recycleType;

    @ApiModelProperty(value = "回收模块")
    private String recycleModel;

    @ApiModelProperty(value = "回收任务ID")
    private List<Long> recycleTaskIdList;


}
