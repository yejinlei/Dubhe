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

import java.sql.Timestamp;

/**
 * @description ES数据同步DTO
 * @date 2020-03-24
 */
@Data
public class EsTransportDTO {

    /**
     * 状态
     */
    private Integer annotationStatus;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件url
     */
    private String url;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 更新人ID
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
     * 文件ID
     */
    private Long id;

    /**
     * 标签ID
     */
    private Long labelId;
}
