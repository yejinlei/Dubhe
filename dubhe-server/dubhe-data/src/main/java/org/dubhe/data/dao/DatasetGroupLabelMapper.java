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

package org.dubhe.data.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Delete;
import org.dubhe.data.domain.entity.DatasetGroupLabel;

/**
 * @description 标签组标签中间表 Mapper 接口
 * @date 2020-09-22
 */
public interface DatasetGroupLabelMapper extends BaseMapper<DatasetGroupLabel> {


    /**
     * 根据标签组ID删除标签和标签组的关联关系
     *
     * @param groupId 标签组ID
     */
    @Delete("delete from data_group_label where label_group_id = #{groupId} ")
    void deleteByGroupId(@Param("groupId") Long groupId);

}
