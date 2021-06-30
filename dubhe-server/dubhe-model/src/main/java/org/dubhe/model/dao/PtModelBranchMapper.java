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

package org.dubhe.model.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dubhe.biz.base.annotation.DataPermission;
import org.dubhe.model.domain.entity.PtModelBranch;

/**
 * @description 模型版本管理
 * @date 2020-04-02
 */
@DataPermission(ignoresMethod = {"insert"})
public interface PtModelBranchMapper extends BaseMapper<PtModelBranch> {

    /**
     * 变更模型版本删除状态
     * @param id 模型版本id
     * @param deleteFlag 删除状态
     * @return 数量
     */
    @Update("update pt_model_branch set deleted = #{deleteFlag} where id = #{id}")
    int updateStatusById(@Param("id") Long id, @Param("deleteFlag") boolean deleteFlag);

    /**
     * 模型版本详情
     * @param id 模型版本id
     * @return 模型版本实体
     */
    @Select("SELECT * FROM pt_model_branch where id = #{id}")
    PtModelBranch selectAllById(@Param("id") Long id);
}
