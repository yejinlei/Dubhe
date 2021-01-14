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

package org.dubhe.service;

import org.dubhe.domain.dto.PtModelBranchCreateDTO;
import org.dubhe.domain.dto.PtModelBranchDeleteDTO;
import org.dubhe.domain.dto.PtModelBranchUpdateDTO;
import org.dubhe.domain.dto.PtModelBranchQueryDTO;
import org.dubhe.domain.vo.*;

import java.util.Map;

/**
 * @description 模型版本管理
 * @date 2020-03-24
 */
public interface PtModelBranchService {

    /**
     * 查询数据分页
     *
     * @param ptModelBranchQueryDTO 模型版本管理查询参数
     * @return Map<String, Object>  模型版本管理分页对象
     */
    Map<String, Object> queryAll(PtModelBranchQueryDTO ptModelBranchQueryDTO);

    /**
     * 创建
     *
     * @param ptModelBranchCreateDTO 模型版本管理创建对象
     * @return PtModelBranchCreateVO 模型版本管理返回创建VO
     */
    PtModelBranchCreateVO create(PtModelBranchCreateDTO ptModelBranchCreateDTO);

    /**
     * 编辑
     *
     * @param ptModelBranchUpdateDTO 模型版本管理修改对象
     * @return PtModelBranchUpdateVO 模型版本管理返回更新VO
     */
    PtModelBranchUpdateVO update(PtModelBranchUpdateDTO ptModelBranchUpdateDTO);

    /**
     * 多选删除
     *
     * @param ptModelBranchDeleteDTO 模型版本管理删除对象
     * @return PtModelBranchDeleteVO 模型版本管理返回删除VO
     */
    PtModelBranchDeleteVO deleteAll(PtModelBranchDeleteDTO ptModelBranchDeleteDTO);
}
