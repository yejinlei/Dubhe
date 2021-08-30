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

package org.dubhe.terminal.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.dubhe.biz.db.entity.BaseEntity;
import org.dubhe.terminal.domain.entity.TerminalInfo;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description 终端信息VO
 * @date 2021-07-13
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class TerminalInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "terminalId")
    private Long terminalId;

    @TableField(value = "name")
    private String name;

    @ApiModelProperty(value = "服务状态：0-异常，1-部署中，2-运行中,3-停止中，4-已停止")
    private Integer status;

    @ApiModelProperty("状态对应的详情信息")
    private String statusDetail;

    @ApiModelProperty(value = "ssh命令")
    private String ssh;

    @ApiModelProperty(value = "ssh 密码")
    private String sshPassword;

    @ApiModelProperty(value = "ssh 用户")
    private String sshUser;

    @ApiModelProperty(value = "CPU数量(核)")
    private Integer cpuNum;

    @ApiModelProperty(value = "内存大小（M）")
    private Integer memNum;

    @ApiModelProperty(value = "GPU数量（核）")
    private Integer gpuNum;

    @ApiModelProperty(value = "磁盘大小（M）")
    private Integer diskMemNum;

    @ApiModelProperty(value = "是否master节点:false 否 true:是")
    private boolean masterFlag;

    public TerminalInfoVO(TerminalInfo terminalInfo){
        this.id = terminalInfo.getId();
        this.terminalId = terminalInfo.getTerminalId();
        this.name = terminalInfo.getName();
        this.status = terminalInfo.getStatus();
        this.ssh = terminalInfo.getSsh();
        this.sshPassword = terminalInfo.getSshPassword();
        this.sshUser = terminalInfo.getSshUser();
        this.cpuNum = terminalInfo.getCpuNum();
        this.memNum = terminalInfo.getMemNum();
        this.gpuNum = terminalInfo.getGpuNum();
        this.diskMemNum = terminalInfo.getDiskMemNum();
        this.masterFlag = terminalInfo.isMasterFlag();
        this.statusDetail = terminalInfo.getStatusDetail();
    }
}
