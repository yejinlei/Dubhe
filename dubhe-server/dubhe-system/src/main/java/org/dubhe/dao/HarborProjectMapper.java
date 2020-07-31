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
import org.apache.ibatis.annotations.Select;
import org.dubhe.domain.entity.HarborProject;

import java.util.List;

/**
 * @desc:
 * @date 2020.05.26
 */
public interface HarborProjectMapper extends BaseMapper<HarborProject> {

    /**
     * 获取渠道对应harborProject
     * @param source
     * @return
     */
    @Select("select distinct image_name from harbor_project where deleted = 0 and create_resource = #{source}")
    List<String> findByCreateSource(int source);
}
