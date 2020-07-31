/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

package org.dubhe.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dubhe.base.BaseEntity;
import org.dubhe.domain.entity.PtImage;
import org.dubhe.domain.entity.Team;
import org.dubhe.domain.entity.User;

import javax.validation.constraints.NotBlank;
import java.sql.Timestamp;

/**
 * @description 开发环境
 * @date 2020-03-17
 */

@Data
@TableName("pt_dev_envs")
public class PtDevEnvs extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @NotBlank
    private String name;

    @TableField(value = "remark")
    private String remark;

    @TableField(value = "type")
    @NotBlank
    private String type;

    @TableField(value = "pod_num")
    private Integer podNum;

    @TableField(value = "gpu_num")
    private Integer gpuNum;

    @TableField(value = "mem_num")
    private Integer memNum;

    @TableField(value = "cpu_num")
    private Integer cpuNum;

    @TableField(value = "duration")
    private Integer duration;

    @TableField(value = "start_time")
    private Timestamp startTime;

    @TableField(value = "close_time")
    private Timestamp closeTime;

    /**
     * 数据集
     */
    @TableField(exist = false)
    private PtDataset dataset;

    /**
     * 镜像
     */
    @TableField(exist = false)
    private PtImage image;

    /**
     * 存储
     */
    @TableField(exist = false)
    private PtStorage storage;

    /**
     * 团队
     */
    @TableField(exist = false)
    private Team team;

    /**
     * 创建用户
     */
    @TableField(exist = false)
    private User createUser;


    public void copy(PtDevEnvs source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }

    public @interface Update {
    }
}
