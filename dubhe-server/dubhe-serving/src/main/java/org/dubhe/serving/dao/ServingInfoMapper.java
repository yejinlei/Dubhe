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
package org.dubhe.serving.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.dubhe.biz.base.annotation.DataPermission;
import org.dubhe.serving.domain.entity.ServingInfo;


/**
 * @description 服务信息管理
 * @date 2020-08-25
 */
@DataPermission(ignoresMethod = {"insert", "rollbackById", "updateStatusDetail"})
public interface ServingInfoMapper extends BaseMapper<ServingInfo> {

    /**
     * 还原回收数据
     *
     * @param id            serving id
     * @param deleteFlag    删除标识
     * @return int 数量
     */
    @Update("update serving_info set deleted = #{deleteFlag} where id = #{id}")
    int rollbackById(@Param("id") Long id, @Param("deleteFlag") boolean deleteFlag);

    /**
     * 修改状态详情
     * @param id  serving id
     * @param statusDetail 状态详情
     * @return int 数量
     */
    @Update("update serving_info set status_detail = #{statusDetail} where id = #{id}")
    int updateStatusDetail(@Param("id") Long id, @Param("statusDetail") String statusDetail);
}
