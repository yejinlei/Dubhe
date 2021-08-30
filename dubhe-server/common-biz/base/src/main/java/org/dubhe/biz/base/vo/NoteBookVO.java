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
import java.util.Date;

/**
 * @description 返回前端请求体
 * @date 2020-04-28
 */
@Data
public class NoteBookVO implements Serializable {

    /**
     * ID
     */
    private Long id;

    /**
     * 所属用户
     */
    private Long userId;

    /**
     * NoteBook 名称
     */
    private String name;

    /**
     * NoteBook 名称
     */
    private String noteBookName;

    /**
     * 备注描述
     */
    private String description;

    /**
     * 可访问jupyter地址
     */
    private String url;

    /**
     * CPU数量
     */
    private Integer cpuNum;

    /**
     * GPU数量
     */
    private Integer gpuNum;

    /**
     * 内存大小（M）
     */
    private Integer memNum;

    /**
     * 硬盘内存大小（M）
     */
    private Integer diskMemNum;

    /**
     * 0运行，1停止, 2删除, 3启动中，4停止中，5删除中，6运行异常（暂未启用）
     */
    private Integer status;

    /**
     * 状态对应的详情信息
     */
    private String statusDetail;

    /**
     * k8s响应状态码
     */
    private String k8sStatusCode;

    /**
     * k8s响应状态信息
     */
    private String k8sStatusInfo;

    private String k8sImageName;

    /**
     * k8s中pvc存储路径
     */
    private String k8sPvcPath;

    private Date createTime;

    private Date updateTime;


    /**
     * 数据集名称
     */
    private String dataSourceName;

    /**
     * 数据集路径
     */
    private String dataSourcePath;

    /**
     * 算法ID
     */
    private Long algorithmId;

    /**
     * 资源拥有者ID
     */
    private Long originUserId;


    /**
     * pip包路径
     */
    private String pipSitePackagePath;
}
