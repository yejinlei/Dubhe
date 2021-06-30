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

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description ES数据查询DTO
 * @date 2020-03-24
 */
@Data
public class EsDataFileDTO implements Serializable {

    /**
     * 名称
     */
    private String name;

    /**
     * 内容
     */
    private String content;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 数据集ID
     */
    private Long datasetId;

    /**
     * 创建用户
     */
    private Long createUserId;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 更新用户
     */
    private Long updateUserId;

    /**
     * 更新时间
     */
    private Timestamp updateTime;

    /**
     * 文件类型
     */
    private Integer fileType;

    /**
     * 增强类型
     */
    private Integer enhanceType;

    /**
     * 用户ID
     */
    private Long originUserId;

    /**
     * 预测值
     */
    private Double prediction;

    /**
     * 标签ID
     */
    private Long labelId;

    /**
     * 标注信息
     */
    private String annotation;

    /**
     * 版本名称
     */
    private String versionName;
}
