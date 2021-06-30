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
 * @description 版本管理删除返回对应id
 * @date 2020-5-15
 */
@Data
@Accessors(chain = true)
public class PtModelBranchQueryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 版本ID
     */
    private Long id;

    /**
     * 父ID
     */
    private Long parentId;

    /**
     * 版本号
     */
    private String version;

    /**
     * 模型地址
     */
    private String modelAddress;

    /**
     * 模型路径
     */
    private String modelPath;

    /**
     * 模型来源(0用户上传，1训练输出，2模型优化)
     */
    private Integer modelSource;

    /**
     * 算法ID
     */
    private Long algorithmId;

    /**
     * 算法名称
     */
    private String algorithmName;

    /**
     * 算法来源(1为我的算法，2为预置算法)
     */
    private Integer algorithmSource;

    /**
     * 文件拷贝状态(0文件拷贝中，1文件拷贝成功，2文件拷贝失败)
     */
    private Integer status;

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
     * 模型名称
     */
    private String name;

    /**
     * 模型描述
     */
    private String modelDescription;

    /**
     * 是否能提供服务（true:能，false：否）
     */
    private Boolean servingModel;
}
