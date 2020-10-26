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

package org.dubhe.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dubhe.annotation.DataPermission;
import org.dubhe.domain.PtModelInfo;

import java.util.List;

/**
 * @description 模型管理
 * @date 2020-04-02
 */
@DataPermission(ignoresMethod = {"insert"})
public interface PtModelInfoMapper extends BaseMapper<PtModelInfo> {
    /**
     * 根据模型来源和创建者id查询模型详情
     *
     * @param modelResource 模型来源
     * @param originUserId 创建者id
     * @return  List<PtModelInfo> 模型详情的集合
     */
    @Select("select name,id from pt_model_info where deleted=0 and model_resource=#{modelResource} and origin_user_id=#{origin_user_id} and (model_version is not null and LENGTH(TRIM(model_version))>0)")
    List<PtModelInfo> findModelByResource(@Param("modelResource") Integer modelResource, @Param("origin_user_id") Long originUserId);
}
