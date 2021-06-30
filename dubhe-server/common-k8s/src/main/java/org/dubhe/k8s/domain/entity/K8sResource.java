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

package org.dubhe.k8s.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.biz.db.entity.BaseEntity;

/**
 * @description k8s资源对象
 * @date 2020-07-10
 */
@Data
@TableName("k8s_resource")
public class K8sResource extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(hidden = true)
    private Long id;

    @TableField(value = "kind")
    private String kind;

    @TableField(value = "namespace")
    private String namespace;

    @TableField(value = "name")
    private String name;

    @TableField(value = "resource_name")
    private String resourceName;

    @TableField(value = "env")
    private String env;

    @TableField(value = "business")
    private String business;

    public K8sResource(String kind,String namespace,String name,String resourceName,String env,String business){
        this.kind = kind;
        this.namespace = namespace;
        this.name = name;
        this.resourceName = resourceName;
        this.env = env;
        this.business = business;
    }
}
