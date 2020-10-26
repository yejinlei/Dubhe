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
package org.dubhe.datasetutil.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.dubhe.datasetutil.common.base.BaseEntity;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description 数据集类
 * @date 2020-9-17
 */
@Data
@TableName("data_dataset")
public class Dataset extends BaseEntity implements Serializable  {

    /**
     * 删除标识
     */
    @TableField("deleted")
    private Boolean deleted = false;

    /**
     * 数据集名称
     */
    private String name;

    /**
     * 数据集备注
     */
    private String remark;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 数据集类型
     */
    private Integer dataType;

    /**
     * 数据集状态
     */
    private Integer annotateType;

    /**
     * 任务id
     */
    private Long teamId;

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * url
     */
    private String uri;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 当前版本号
     */
    private String currentVersionName;

    /**
     * 是否为导入
     */
    @TableField(value = "is_import")
    private boolean isImport;

    /**
     * 用户导入数据集压缩包地址
     */
    private String archiveUrl;

    /**
     * 解压状态
     */
    private Integer decompressState;

    /**
     * 解压失败原因
     */
    private String decompressFailReason;

    /**
     * 是否置顶
     */
    @TableField(value = "is_top")
    private boolean isTop;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 修改时间
     */
    private Timestamp updateTime;

    /**
     * 创建人id
     */
    private Long createUserId;

    /**
     * 修改人id
     */
    private Long updateUserId;

    /**
     * 拥有人id
     */
    private Long originUserId;

    public Dataset() {}

}
