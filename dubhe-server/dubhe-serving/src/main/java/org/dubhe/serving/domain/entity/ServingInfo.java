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

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.db.entity.BaseEntity;

import javax.validation.constraints.NotNull;

/**
 * @description serving信息
 * @date 2020-08-24
 */
@Data
@Accessors(chain = true)
@TableName("serving_info")
public class ServingInfo extends BaseEntity {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    @NotNull(groups = {Update.class})
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
     * 运行节点数
      */
    @TableField(value = "running_node")
    private Integer runningNode;
    /**
     * 服务总节点数
     */
    @TableField(value = "total_node")
    private Integer totalNode;
    /**
     * 服务类型：0-Restful，1-gRPC
     */
    @TableField("type")
    private Integer type;
    /**
     * 描述
     */
    @TableField("description")
    private String description;
    /**
     * 服务请求接口uuid
     */
    @TableField("uuid")
    private String uuid;
    /**
     * 模型来源:0-我的模型，1-预置模型
     */
    @TableField("model_resource")
    private Integer modelResource;
    /**
     * 资源拥有者ID
     */
    @TableField(value = "origin_user_id",fill = FieldFill.INSERT)
    private Long originUserId;
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
