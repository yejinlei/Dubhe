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

package org.dubhe.train.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.dubhe.biz.base.annotation.DataPermission;
import org.dubhe.train.domain.entity.PtTrainJob;
import org.dubhe.train.domain.vo.PtTrainVO;

import java.util.Collection;

/**
 * @description 训练作业job Mapper 接口
 * @date 2020-04-27
 */
@DataPermission(ignoresMethod = {"insert", "getPageTrain", "updateStatusDetailById"})
public interface PtTrainJobMapper extends BaseMapper<PtTrainJob> {

    /**
     * 获取训练列表，并进行分页。
     *
     * @param page         页
     * @param createUserId 用户id
     * @param trainStatus  训练状态
     * @param trainName    训练名称
     * @param sort         排序字段
     * @param order        排序方式
     *
     * @param trainIds
     * @return PtTrainVO
     */
    Page<PtTrainVO> getPageTrain(Page page, @Param("createUserId") Long createUserId,
                                 @Param("trainStatus") Integer trainStatus, @Param("trainName") String trainName, @Param("sort") String sort,
                                 @Param("order") String order, @Param("trainIds") Collection<Long> trainIds);

    /**
     * 根据id修改statusDetail
     * @param id 训练版本id
     * @param statusDetail 训练状态详情
     * @return 数量
     */
    @Update("update pt_train_job set status_detail = #{statusDetail} where id = #{id}")
    int updateStatusDetailById(@Param("id") Long id, @Param("statusDetail") String statusDetail);

}
