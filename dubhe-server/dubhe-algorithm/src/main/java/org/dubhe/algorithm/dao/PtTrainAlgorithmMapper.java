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

package org.dubhe.algorithm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dubhe.algorithm.domain.entity.PtTrainAlgorithm;
import org.dubhe.biz.base.annotation.DataPermission;

import java.util.List;
import java.util.Set;

/**
 * @description 训练算法Mapper
 * @date 2020-04-27
 */
@DataPermission(ignoresMethod = {"insert", "selectPreAlgorithm"})
public interface PtTrainAlgorithmMapper extends BaseMapper<PtTrainAlgorithm> {

    /**
     *  根据算法id查询算法信息
     * @param id 算法id
     * @return PtTrainAlgorithm 算法信息
     */
    @Select("select * from pt_train_algorithm where id= #{id}")
    PtTrainAlgorithm selectAllById(@Param("id") Long id);

    /**
     *  根据算法id集合查询对应的算法信息
     * @param ids 算法集合id
     * @return List<PtTrainAlgorithm> 算法信息集合
     */
    @Select({
            "<script>",
            "select * from pt_train_algorithm",
            "where id in ",
            "<foreach item='item' index='index' collection='ids'",
            "open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"
    })
    List<PtTrainAlgorithm> selectAllBatchIds(@Param("ids") Set<Long> ids);

    /**
     * 算法还原
     * @param id 算法id
     * @param deleteFlag 删除状态
     * @return 数量
     */
    @Update("update pt_train_algorithm set deleted = #{deleteFlag} where id = #{id}")
    int updateStatusById(@Param("id") Long id, @Param("deleteFlag") boolean deleteFlag);

    /**
     * 查询可推理预置算法
     * @return List<PtTrainAlgorithmQueryVO> 返回可推理预置算法集合
     */
    @Select("select * from pt_train_algorithm where deleted = 0 and inference=1 and algorithm_source=2 order by id desc")
    List<PtTrainAlgorithm> selectPreAlgorithm();
}
