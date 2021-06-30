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

package org.dubhe.admin.domain.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.dubhe.admin.domain.entity.DictDetail;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * @description 字典新增DTO
 * @date 2020-06-01
 */
@Data
public class DictCreateDTO implements Serializable {

    private static final long serialVersionUID = -901581636964448858L;

    @NotBlank(message = "字典名称不能为空")
    @Length(max = 255, message = "名称长度不能超过255")
    private String name;

    @Length(max = 255, message = "备注长度不能超过255")
    private String remark;

    private Timestamp createTime;

    @TableField(exist = false)
    private List<DictDetail> dictDetails;

}
