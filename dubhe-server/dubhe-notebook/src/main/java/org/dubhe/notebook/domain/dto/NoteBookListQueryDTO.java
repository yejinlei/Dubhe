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

package org.dubhe.notebook.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.biz.db.annotation.Query;

import java.io.Serializable;

/**
 * @description 查询请求体
 * @date 2020-06-28
 */
@Data
public class NoteBookListQueryDTO implements Serializable {

    @ApiModelProperty("0运行中，1停止, 2删除, 3启动中，4停止中，5删除中，6运行异常（暂未启用）")
    private Integer status;

    @Query(propName = "notebook_name", type = Query.Type.LIKE)
    @ApiModelProperty("notebook名称")
    private String noteBookName;

    @Query(propName = "origin_user_id", type = Query.Type.EQ)
    @ApiModelProperty(value = "所属用户ID", hidden = true)
    private Long userId;

}
