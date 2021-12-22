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
package org.dubhe.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dubhe.admin.domain.entity.UserGpuConfig;
import org.dubhe.biz.base.vo.GpuAllotVO;

import java.util.List;

/**
 * @description 用户GPU配置 Mapper
 * @date 2021-9-2
 */
public interface UserGpuConfigMapper extends BaseMapper<UserGpuConfig> {

    /**
     * 批量添加用户GPU配置
     *
     * @param userGpuConfigs 用户GPU配置实体集合
     */
    void insertBatchs(List<UserGpuConfig> userGpuConfigs);

    /**
     *  根据userId查询用户GPU配置记录数
     * @param userId 用户id
     * @return Integer 用户GPU配置记录数
     */
    @Select("select count(*) from user_gpu_config where user_id= #{userId}")
    Integer selectCountByUserId(@Param("userId") Long userId);

    /**
     * 统计GPU型号配额总量
     *
     * @return GPU具体型号资源配额
     */
    @Select("select gpu_model gpuModel,sum(gpu_limit)allotTotal from user_gpu_config where deleted=0 group by gpu_model")
    List<GpuAllotVO> selectGpuAllotSum();

    /**
     * GPU配额TOP10统计
     */
    @Select("SELECT u.username,gc.user_id,SUM(gc.gpu_limit)gpuLimit FROM user_gpu_config gc, user u WHERE gc.user_id=u.id AND gc.deleted=0" +
            " GROUP BY gc.user_id ORDER BY gpuLimit DESC LIMIT 10")
    List<UserGpuConfig> selectAllotTotal();

    /**
     * 查询某用户具体的GPU型号配额
     *
     * @param userId 用户ID
     * @return GPU型号配额
     */
    @Select("select gpu_model gpuModel,sum(gpu_limit)allotTotal from user_gpu_config where user_id=#{userId} and deleted=0 group by gpuModel")
    List<GpuAllotVO> selectGpuModelTotal(@Param("userId") Long userId);

    /**
     * 根据用户id查询GPU配额总量
     *
     * @param userId 用户ID
     * @return GPU配额总量
     */
    @Select("SELECT IFNULL(SUM(gpu_limit),0)gpuSum FROM user_gpu_config WHERE user_id=#{userId} AND deleted=0")
    int selectGpuLimitSum(@Param("userId") Long userId);

}
