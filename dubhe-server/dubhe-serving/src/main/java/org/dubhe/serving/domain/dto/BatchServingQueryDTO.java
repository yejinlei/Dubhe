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
package org.dubhe.serving.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.biz.db.annotation.Query;
import org.dubhe.biz.db.base.PageQueryBase;

import java.io.Serializable;

/**
 * @description 批量服务查询
 * @date 2020-08-27
 */
@Data
public class BatchServingQueryDTO extends PageQueryBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("批量服务ID")
    @Query(propName = "id")
    private Long id;

    @Query(type = Query.Type.LIKE)
    @ApiModelProperty(value = "批量服务名称或ID")
    private String name;

    /**
     * 服务状态：0-异常，1-部署中，2-运行中，3-已停止
     */
    @ApiModelProperty(value = "服务状态")
    @Query(propName = "status")
    private Integer status;

    /**
     * 服务类型：0-Restful，1-gRPC
     */
    @ApiModelProperty(value = "服务类型")
    @Query(propName = "type")
    private String type;
}
