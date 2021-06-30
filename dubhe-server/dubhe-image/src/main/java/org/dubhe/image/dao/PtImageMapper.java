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

package org.dubhe.image.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dubhe.biz.base.annotation.DataPermission;
import org.dubhe.image.domain.entity.PtImage;

import java.util.List;


/**
 * @description 镜像 Mapper 接口
 * @date 2020-04-27
 */
@DataPermission(ignoresMethod = {"insert"})
public interface PtImageMapper extends BaseMapper<PtImage> {

    @Select("select * from pt_image where project_name=#{projectName} and (create_user_id=#{userId} or image_resource=#{imageResource})")
    List<PtImage> getImageNameList(String projectName, Long userId, Integer imageResource);

    /**
     * 还原回收数据
     *
     * @param id            镜像id
     * @param deleteFlag    删除标识
     */
    @Update("update pt_image set deleted = #{deleteFlag} where id = #{id}")
    void updateDeletedById(@Param("id") Long id, @Param("deleteFlag") boolean deleteFlag);

}
