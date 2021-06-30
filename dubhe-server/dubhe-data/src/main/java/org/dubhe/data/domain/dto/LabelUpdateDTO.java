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
package org.dubhe.data.domain.dto;

import lombok.Data;
import org.dubhe.data.domain.entity.Label;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description 标签修改DTO
 * @date 2020-10-15
 */
@Data
public class LabelUpdateDTO implements Serializable {

    /**
     * 标签ID
     */
    @NotNull(message = "标签id不能为空")
    private Long labelId;

    /**
     * 标签名称
     */
    @NotNull(message = "名称不能为空")
    private String name;

    /**
     * 标签颜色
     */
    @NotNull(message = "颜色不能为空")
    private String color;


    /**
     * 数据集ID
     */
    private Long datasetId;


    /**
     * 更新标签
     *
     * @param labelUpdateDTO 修改标签条件
     * @return Label        标签实体
     */
    public static Label update(LabelUpdateDTO labelUpdateDTO) {
        return new Label(labelUpdateDTO);
    }
}
