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
package org.dubhe.biz.base.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description 资源规格查询结果封装类
 * @date 2021-06-02
 */
@Data
@Accessors(chain = true)
public class QueryResourceSpecsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     *规格名称
     */
    private String specsName;

    /**
     *规格类型(0为CPU, 1为GPU)
     */
    private Boolean resourcesPoolType;

    /**
     *所属业务场景(0:通用，1：dubhe-notebook，2：dubhe-train，3：dubhe-serving，4：dubhe-tadl)
     */
    private Integer module;

    /**
     *CPU数量,单位：核
     */
    private Integer cpuNum;

    /**
     *GPU数量，单位：核
     */
    private Integer gpuNum;

    /**
     *内存大小，单位：M
     */
    private Integer memNum;

    /**
     *工作空间的存储配额，单位：M
     */
    private Integer workspaceRequest;

    /**
     *创建人
     */
    private Long createUserId;

    /**
     *创建时间
     */
    private Timestamp createTime;

    /**
     *更新人
     */
    private Long updateUserId;

    /**
     *更新时间
     */
    private Timestamp updateTime;
}