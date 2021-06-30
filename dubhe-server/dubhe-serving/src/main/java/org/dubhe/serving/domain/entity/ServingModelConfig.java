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
package org.dubhe.serving.domain.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.db.entity.BaseEntity;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @description Serving模型配置
 * @date 2020-08-24
 */
@Data
@Accessors(chain = true)
@TableName(value = "serving_model_config", autoResultMap = true)
public class ServingModelConfig extends BaseEntity {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    @NotNull(groups = {Update.class})
    private Long id;
    /**
     * Serving信息id
     */
    @TableField("serving_id")
    private Long servingId;
    /**
     * 模型id
     */
    @TableField("model_id")
    private Long modelId;
    /**
     * 模型分支id
     */
    @TableField(value = "model_branch_id")
    private Long modelBranchId;
    /**
     * 模型名称
     */
    @TableField("model_name")
    private String modelName;
    /**
     * 模型路径
     */
    @TableField("model_address")
    private String modelAddress;
    /**
     * 模型版本
     */
    @TableField("model_version")
    private String modelVersion;
    /**
     * 框架类型
     */
    @TableField("frame_type")
    private Integer frameType;
    /**
     * 灰度发布分流（%）
     */
    @TableField("release_rate")
    private Integer releaseRate;
    /**
     * 节点类型(0为CPU，1为GPU)
     */
    @TableField("resources_pool_type")
    private Integer resourcesPoolType;
    /**
     * 节点规格
     */
    @TableField("resources_pool_specs")
    private String resourcesPoolSpecs;
    /**
     * 规格信息
     */
    @TableField("pool_specs_info")
    private String poolSpecsInfo;
    /**
     * 节点个数
     */
    @TableField("resources_pool_node")
    private Integer resourcesPoolNode;
    /**
     * 部署参数
     */
    @TableField(value = "deploy_params", typeHandler = JacksonTypeHandler.class)
    private JSONObject deployParams;
    /**
     * 模型来源(0-我的模型，1-预置模型)
     */
    @TableField("model_resource")
    private Integer modelResource;
    /**
     * 模型部署url
     */
    @TableField(value = "url")
    private String url;
    /**
     * 资源信息
     */
    @TableField(value = "resource_info")
    private String resourceInfo;
    /**
     * 部署id(用于回滚)
     */
    @TableField(value = "deploy_id")
    private String deployId;
    /**
     * deployment已 Running的pod数
     */
    @TableField(value = "ready_replicas")
    private Integer readyReplicas;
    /**
     * 是否使用脚本
     */
    @TableField(value = "use_script")
    private Boolean useScript;
    /**
     * 推理脚本路径
     */
    @TableField(value = "script_path")
    private String scriptPath;

    /**
     * 算法ID
     */
    @TableField(value = "algorithm_id")
    private Long algorithmId;

    /**
     * 镜像URL
     **/
    @TableField("image")
    private String image;

    /**
     * 镜像名称
     **/
    @TableField("image_name")
    private String imageName;

    /**
     * 镜像版本
     **/
    @TableField("image_tag")
    private String imageTag;

    /**
     * @return 每个节点的GPU数量
     */
    public Integer getGpuNum() {
        return JSONObject.parseObject(poolSpecsInfo.replace("\\", "")).getInteger("gpuNum");
    }

    /**
     * @return cpu数量
     */
    public Integer getCpuNum() {
        return JSONObject.parseObject(poolSpecsInfo.replace("\\", "")).getInteger("cpuNum");
    }

    /**
     * @return cpu数量
     */
    public Integer getMemNum() {
        return JSONObject.parseObject(poolSpecsInfo.replace("\\", "")).getInteger("memNum");
    }

    /**
     * @return 部署参数
     */
    public String getDeployParam() {
        StringBuilder params = new StringBuilder();
        for (String key : deployParams.keySet()) {
            if(Objects.nonNull(deployParams.get(key))){
                params.append(" --").append(key).append("='").append(deployParams.get(key)).append("'");
            }
        }
        return params.toString();
    }
}
