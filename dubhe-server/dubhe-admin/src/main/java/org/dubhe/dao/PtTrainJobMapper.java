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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dubhe.domain.entity.PtTrainJob;
import org.dubhe.domain.vo.PtTrainVO;

/**
 * @description 训练作业job Mapper 接口
 * @date 2020-04-27
 */
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
	 * @return PtTrainVO
	 */
	Page<PtTrainVO> getPageTrain(Page page, @Param("createUserId") Long createUserId,
			@Param("trainStatus") Integer trainStatus, @Param("trainName") String trainName, @Param("sort") String sort,
			@Param("order") String order);

	/**
     * 根据状态进行统计数量
     *
     * @param userId 当前用户id
     * @param param sql片段
     * @return 统计的数量
     */
	@Select("select count(1) from pt_train_job t1 inner join pt_train t2 on t1.train_id = t2.id  where t1.create_user_id= #{userId} and t1.train_status in ${param} and t1.deleted= 0 and t2.deleted = 0 ")
	Integer selectCountByStatus(@Param("userId") Long userId, @Param("param") String param);

}
