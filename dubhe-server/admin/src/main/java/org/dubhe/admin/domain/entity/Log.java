/**
 * Copyright 2019-2020 Zheng Jie
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
 */
package org.dubhe.admin.domain.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description 日志实体
 * @date 2020-06-01
 */
@Data
@TableName("log")
@NoArgsConstructor
public class Log implements Serializable {

    private static final long serialVersionUID = -4447644691937249474L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 操作用户
     */
    @TableField(value = "username")
    private String username;

    /**
     * 描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 方法名
     */
    @TableField(value = "label")
    private String method;

    /**
     * 参数
     */
    @TableField(value = "text")
    private String params;

    /**
     * 日志类型
     */
    @TableField(value = "log_type")
    private String logType;

    /**
     * 请求ip
     */
    @TableField(value = "request_ip")
    private String requestIp;

    /**
     * 浏览器
     */
    @TableField(value = "browser")
    private String browser;

    /**
     * 请求耗时
     */
    @TableField(value = "time")
    private Long time;

    /**
     * 异常详细
     */
    @TableField(value = "exception_detail")
    private byte[] exceptionDetail;

    /**
     * 创建日期
     */
    @TableField(value = "create_time")
    private Timestamp createTime;

    public Log(String logType, Long time) {
        this.logType = logType;
        this.time = time;
    }
}
