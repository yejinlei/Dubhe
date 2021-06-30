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
import org.dubhe.biz.db.base.PageQueryBase;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description 标签组列表查询条件
 * @date 2020-9-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LabelGroupQueryVO extends PageQueryBase implements Serializable {

    /**
     * 标签组ID
     */
    @Query(type = Query.Type.EQ, propName = "id")
    private Long id;

    /**
     * 标签组名称
     */
    @Query(type = Query.Type.LIKE, propName = "name")
    private String name;

    /**
     * 标签组类型：0: private 私有标签组,  1:public 公开标签组
     */
    @Query(type = Query.Type.EQ, propName = "type")
    private Integer type;

    /**
     * 修改时间
     */
    private Timestamp updateTime;
    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 标签组描述
     */
    private String remark;

    /**
     * 标签数量
     */
    private Integer count;

    /**
     * 操作类型 1:Json编辑器操作类型 2:自定义操作类型 3:导入操作类型
     */
    private Integer operateType;

    /**
     * 标签组类型:0:视觉,1:文本
     */
    private Integer labelGroupType;
}
