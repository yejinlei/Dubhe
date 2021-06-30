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
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.db.entity.BaseEntity;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @description 批量服务
 * @date 2020-08-24
 */
@Data
@Accessors(chain = true)
@TableName(value = "serving_batch", autoResultMap = true)
public class BatchServing extends BaseEntity {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    @NotNull(groups = {BaseEntity.Update.class})
    private Long id;
    /**
     * 服务名称
     */
    @TableField(value = "name")
    private String name;
    /**
     * 服务状态：0-异常，1-部署中，2-运行中，3-已停止
     */
    @TableField(value = "status")
    private String status;
    /**
     * 状态对应的详情信息
     */
    @TableField(value = "status_detail")
    private String statusDetail;
    /**
     * 进度
     */
    @TableField("progress")
    private String progress;
    /**
     * 描述
     */
    @TableField("description")
    private String description;
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
     * 模型地址
     */
    @TableField("model_address")
    private String modelAddress;
    /**
     * 模型框架
     */
    @TableField("frame_type")
    private Integer frameType;
    /**
     * 输入数据目录
     */
    @TableField("input_path")
    private String inputPath;
    /**
     * 输出数据目录
     */
    @TableField("output_path")
    private String outputPath;
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
    @TableField(value = "deploy_params", typeHandler = FastjsonTypeHandler.class)
    private JSONObject deployParams;
    /**
     * 任务开始时间
     */
    @TableField("start_time")
    private Timestamp startTime;
    /**
     * 任务结束时间
     */
    @TableField("end_time")
    private Timestamp endTime;
    /**
     * 资源信息
     */
    @TableField(value = "resource_info")
    private String resourceInfo;
    /**
     * 模型来源:0-我的模型，1-预置模型
     */
    @TableField("model_resource")
    private Integer modelResource;

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
     * 资源拥有者ID
     */
    @TableField(value = "origin_user_id",fill = FieldFill.INSERT)
    private Long originUserId;

    /**
     * @return 每个节点的GPU数量
     */
    public Integer getGpuNum(){
        return JSONObject.parseObject(poolSpecsInfo.replace("\\", "")).getInteger("gpuNum");
    }

    /**
     * @return cpu数量
     */
    public Integer getCpuNum(){
        return JSONObject.parseObject(poolSpecsInfo.replace("\\", "")).getInteger("cpuNum");
    }

    /**
     * @return cpu数量
     */
    public Integer getMemNum(){
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
    /**
     * put 键值
     *
     * @param key 键
     * @param value 值
     */
    public void putStatusDetail(String key,String value){
        statusDetail = StringUtils.putIntoJsonStringMap(key,value,statusDetail);
    }

    /**
     * 移除 键值
     *
     * @param key 键
     */
    public void removeStatusDetail(String key){
        statusDetail = StringUtils.removeFromJsonStringMap(key,statusDetail);
    }
}
