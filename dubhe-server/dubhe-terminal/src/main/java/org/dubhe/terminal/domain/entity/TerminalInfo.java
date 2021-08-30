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
import org.dubhe.biz.base.utils.DateUtil;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.db.entity.BaseEntity;

import javax.validation.constraints.NotNull;

/**
 * @description 终端详情
 * @date 2021-07-12
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("terminal_info")
public class TerminalInfo extends BaseEntity {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    @NotNull(groups = {Update.class})
    private Long id;

    /**
     * 主键
     */
    @TableField(value = "terminal_id")
    private Long terminalId;
    /**
     * 名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * k8s 资源名称
     */
    @TableField(value = "k8s_resource_name")
    private String k8sResourceName;

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
     * ssh命令
     */
    @TableField(value = "ssh")
    private String ssh;

    /**
     * ssh 密码
     */
    @TableField(value = "ssh_password")
    private String sshPassword;

    /**
     * CPU数量(核)
     */
    @TableField(value = "cpu_num")
    private Integer cpuNum;

    /**
     * 内存大小（M）
     */
    @TableField(value = "mem_num")
    private Integer memNum;

    /**
     * GPU数量（核）
     */
    @TableField(value = "gpu_num")
    private Integer gpuNum;

    /**
     * 磁盘大小（M）
     */
    @TableField(value = "disk_mem_num")
    private Integer diskMemNum;

    /**
     * 资源拥有者ID
     */
    @TableField(value = "origin_user_id",fill = FieldFill.INSERT)
    private Long originUserId;

    /**
     * pod ip
     */
    @TableField(value = "pod_ip")
    private String podIp;

    /**
     * ssh端口
     */
    @TableField(value = "ssh_port")
    private Integer sshPort;

    /**
     * ssh 用户
     */
    @TableField(value = "ssh_user")
    private String sshUser;

    /**
     *是否master节点:false 否 true:是
     */
    @TableField("master_flag")
    private boolean masterFlag;

    public TerminalInfo(Long id,Long terminalId,Integer cpuNum,Integer memNum,Integer gpuNum,Integer diskMemNum,String k8sResourceName,Long originUserId,String sshUser,String sshPwd){
        this.id = id;
        this.terminalId = terminalId;
        this.cpuNum = cpuNum;
        this.memNum = memNum;
        this.gpuNum = gpuNum;
        this.diskMemNum = diskMemNum;
        this.k8sResourceName = k8sResourceName;
        this.originUserId = originUserId;
        this.setCreateUserId(originUserId);
        this.sshUser = sshUser;
        this.sshPassword = sshPwd;
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

    public void setUpdateInfo(Long userId){
        setUpdateTime(DateUtil.getCurrentTimestamp());
        setUpdateUserId(userId);
    }
}
