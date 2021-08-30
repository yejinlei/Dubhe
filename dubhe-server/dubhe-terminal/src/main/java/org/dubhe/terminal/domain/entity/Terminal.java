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

package org.dubhe.terminal.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.utils.DateUtil;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.db.entity.BaseEntity;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @description 终端
 * @date 2021-07-08
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("terminal")
public class Terminal extends BaseEntity {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    @NotNull(groups = {Update.class})
    private Long id;
    /**
     * 名称
     */
    @TableField(value = "name")
    private String name;
    /**
     * 镜像名
     */
    @TableField(value = "image_name")
    private String imageName;
    /**
     * 镜像全路径
     */
    @TableField(value = "image_url")
    private String imageUrl;
    /**
     * 镜像版本
     */
    @TableField(value = "image_tag")
    private String imageTag;
    /**
     * 数据集名称
     */
    @TableField(value = "data_source_name")
    private String dataSourceName;
    /**
     * 数据集路径
     */
    @TableField(value = "data_source_path")
    private String dataSourcePath;
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
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 上次启动时刻
     */
    @TableField("last_start_time")
    private Date lastStartTime;

    /**
     * 上次停止时刻
     */
    @TableField("last_stop_time")
    private Date lastStopTime;

    /**
     *节点规格是否相同:0相同 1:不同
     */
    @TableField("same_info")
    private boolean sameInfo;

    /**
     * 服务状态：0-异常，1-部署中，2-运行中,3-停止中，4-已停止
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 状态对应的详情信息
     */
    @TableField(value = "status_detail")
    private String statusDetail;

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

    public void setUpdateInfo(Long userId){
        setUpdateTime(DateUtil.getCurrentTimestamp());
        setUpdateUserId(userId);
    }

    /**
     * 获取镜像 路径
     *
     * @return String
     */
    public String getImagePath(){
        if (StringUtils.isEmpty(imageUrl)){
            return null;
        }
        StringBuffer imageProject = new StringBuffer();
        String[] strings = imageUrl.split(SymbolConstant.SLASH);
        for (int i = MagicNumConstant.ZERO;i < strings.length - MagicNumConstant.ONE;i++){
            if (i == strings.length - MagicNumConstant.TWO){
                imageProject.append(strings[i]);
            }else {
                imageProject.append(strings[i]+SymbolConstant.SLASH);
            }
        }
        return imageProject.toString();
    }

    /**
     * 获取镜像 project
     *
     * @return String
     */
    public String getImageProject(){
        if (StringUtils.isEmpty(imageUrl)){
            return null;
        }
        String[] strings = imageUrl.split(SymbolConstant.SLASH);
        return strings.length > 0 ? strings[MagicNumConstant.ZERO] : null;
    }
}
