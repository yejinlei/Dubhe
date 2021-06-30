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

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description 算法详情VO
 * @date 2020-04-29
 */
@Data
@Accessors(chain = true)
public class ModelOptAlgorithmQureyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 算法名称
     */
    private String algorithmName;

    /**
     * 算法描述
     */
    private String description;

    /**
     * 算法来源(1为我的算法，2为预置算法)
     */
    private Integer algorithmSource;

    /**
     * 环境镜像名称
     */
    private String imageName;

    /**
     * 代码目录
     */
    private String codeDir;

    /**
     * 运行命令
     */
    private String runCommand;

    /**
     * 运行参数
     */
    private JSONObject runParams;

    /**
     * 算法用途
     */
    private String algorithmUsage;

    /**
     * 算法精度
     */
    private String accuracy;

    /**
     * P4推理速度（ms）
     */
    private Integer p4InferenceSpeed;

    /**
     * 训练输出结果（1是，0否）
     */
    private Boolean isTrainModelOut;

    /**
     * 训练输出（1是，0否）
     */
    private Boolean isTrainOut;

    /**
     * 可视化日志（1是，0否）
     */
    private Boolean isVisualizedLog;

    /**
     * 算法状态
     */
    private Integer algorithmStatus;

    /**
     * 创建人ID
     */
    private Integer createUserId;

    /**
     * 修改人ID
     */
    private Integer updateUserId;

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
    private Integer originUserId;
}
