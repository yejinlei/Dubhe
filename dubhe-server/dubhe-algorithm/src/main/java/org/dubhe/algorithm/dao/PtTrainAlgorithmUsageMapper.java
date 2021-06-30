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
import org.dubhe.algorithm.domain.entity.PtTrainAlgorithmUsage;
import org.dubhe.biz.base.annotation.DataPermission;

/**
 *
 * @description 用户辅助信息Mapper 接口
 * @date 2020-06-23
 */
@DataPermission(ignoresMethod = "insert")
public interface PtTrainAlgorithmUsageMapper extends BaseMapper<PtTrainAlgorithmUsage> {

}
