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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description 批量自动标注
 * @date 2020-04-21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchAnnotationInfoCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "标注信息不能为空")
    private List<AnnotationInfoCreateDTO> annotations;

    public Map<Long, AnnotationInfoCreateDTO> toMap() {
        Map<Long, AnnotationInfoCreateDTO> res = new HashMap<>(annotations.size());
        for (AnnotationInfoCreateDTO annotation : annotations) {
            res.put(annotation.getId(), annotation);
        }
        return res;
    }

}
