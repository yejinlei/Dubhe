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

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description 模型管理查询返回对应信息
 * @date 2020-5-15
 */
@Data
public class PtModelInfoQueryVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 模型ID
     */
    private Long id;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 框架类型
     */
    private Integer frameType;

    /**
     * 模型文件的格式（后缀名）
     */
    private Integer modelType;

    /**
     * 模型描述
     */
    private String modelDescription;

    /**
     * 模型分类
     */
    private String modelClassName;

    /**
     * 模型地址
     */
    private String modelAddress;

    /**
     * 模型版本
     */
    private String version;

    /**
     * 模型是否为预置模型（0默认模型，1预置模型）
     */
    private Integer modelResource;

    /**
     * 模型版本总的个数
     */
    private Integer totalNum;

    /**
     * 团队ID
     */
    private Integer teamId;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 修改人ID
     */
    private Long updateUserId;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 修改时间
     */
    private Timestamp updateTime;

    /**
     * 数据拥有人ID
     */
    private Long originUserId;

    /**
     * 模型是否已经打包 0未打包 1 已经打包（目前仅对炼知模型）
     */
    private Integer packaged;

    /**
     * 模型打包tags信息
     */
    private String tags;

    /**
     * 是否能提供服务（true:能，false：否）
     */
    private Boolean servingModel;
}
