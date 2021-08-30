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

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.dubhe.terminal.domain.entity.Terminal;
import org.dubhe.terminal.domain.entity.TerminalInfo;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description 终端VO
 * @date 2021-07-13
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class TerminalVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "终端连接名称, 长度在1-32个字符", required = false)
    private String name;

    @ApiModelProperty(value = "数据来源名称, 长度在1-127个字符")
    private String dataSourceName;

    @ApiModelProperty(value = "数据来源路径, 长度在1-127个字符")
    private String dataSourcePath;

    @ApiModelProperty(value = "镜像版本")
    private String imageTag;

    @ApiModelProperty(value = "镜像名称")
    private String imageName;

    @ApiModelProperty(value = "镜像全路径")
    private String imageUrl;

    @ApiModelProperty(value = "运行节点数")
    private Integer runningNode;

    @ApiModelProperty(value = "服务总节点数")
    private Integer totalNode;

    @ApiModelProperty(value = "节点规格是否相同:0相同 1:不同")
    private boolean sameInfo;

    @ApiModelProperty(value = "上次启动时刻")
    private Date lastStartTime;

    @ApiModelProperty(value = "上次停止时刻")
    private Date lastStopTime;

    @ApiModelProperty(value = "连接详情", required = true)
    private List<TerminalInfoVO> info;

    @ApiModelProperty(value = "服务状态：0-异常，1-保存中，2-运行中,3-已停止")
    private Integer status;

    @ApiModelProperty("状态对应的详情信息")
    private String statusDetail;

    public TerminalVO(Terminal terminal,List<TerminalInfo> terminalInfoList){
        this.id = terminal.getId();
        this.name = terminal.getName();
        this.dataSourceName = terminal.getDataSourceName();
        this.dataSourcePath = terminal.getDataSourcePath();
        this.imageTag = terminal.getImageTag();
        this.imageName = terminal.getImageName();
        this.imageUrl = terminal.getImageUrl();
        this.lastStartTime = terminal.getLastStartTime();
        this.lastStopTime = terminal.getLastStopTime();
        this.runningNode = terminal.getRunningNode();
        this.totalNode = terminal.getTotalNode();
        this.sameInfo = terminal.isSameInfo();
        this.status = terminal.getStatus();
        this.statusDetail = terminal.getStatusDetail();

        if (!CollectionUtils.isEmpty(terminalInfoList)){
            info = new ArrayList<>();
            terminalInfoList.forEach(terminalInfo -> {
                info.add(new TerminalInfoVO(terminalInfo));
            });
        }
    }
}
