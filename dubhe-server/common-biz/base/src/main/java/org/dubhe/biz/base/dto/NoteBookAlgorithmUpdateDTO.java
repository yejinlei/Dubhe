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

package org.dubhe.biz.base.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @description 算法更新notebook对象
 * @date 2020-12-14
 */
@Data
public class NoteBookAlgorithmUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *   NoteBookID
     */
    @NotEmpty(message = "NoteBookID不能为空")
    private List<Long> notebookIdList;

    /**
     *   算法ID
     */
    @NotNull(message = "算法ID不能为空")
    private Long algorithmId;


}
