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

package org.dubhe.service;

import org.dubhe.domain.dto.PtModelInfoCreateDTO;
import org.dubhe.domain.dto.PtModelInfoDeleteDTO;
import org.dubhe.domain.dto.PtModelInfoQueryDTO;
import org.dubhe.domain.dto.PtModelInfoUpdateDTO;
import org.dubhe.domain.vo.PtModelInfoCreateVO;
import org.dubhe.domain.vo.PtModelInfoDeleteVO;
import org.dubhe.domain.vo.PtModelInfoUpdateVO;

import java.util.Map;

/**
 * @description 模型管理
 * @date 2020-03-24
 */
public interface PtModelInfoService {

    /**
     * 查询数据分页
     *
     * @param ptModelInfoQueryDTO  模型管理查询参数
     * @return Map<String, Object> 模型管理分页对象
     */
    Map<String, Object> queryAll(PtModelInfoQueryDTO ptModelInfoQueryDTO);

    /**
     * 创建
     *
     * @param ptModelInfoCreateDTO 模型管理创建对象
     * @return PtModelInfoCreateVO 模型管理返回创建VO
     */
    PtModelInfoCreateVO create(PtModelInfoCreateDTO ptModelInfoCreateDTO);

    /**
     * 编辑
     *
     * @param ptModelInfoUpdateDTO 模型管理修改对象
     * @return PtModelInfoUpdateVO 模型管理返回更新VO
     */
    PtModelInfoUpdateVO update(PtModelInfoUpdateDTO ptModelInfoUpdateDTO);

    /**
     * 多选删除
     *
     * @param ptModelInfoDeleteDTO 模型管理删除对象
     * @return PtModelInfoDeleteVO 模型管理返回删除VO
     */
    PtModelInfoDeleteVO deleteAll(PtModelInfoDeleteDTO ptModelInfoDeleteDTO);
}
