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

package org.dubhe.terminal.service;

import org.dubhe.biz.base.context.UserContext;
import org.dubhe.terminal.domain.dto.*;
import org.dubhe.terminal.domain.entity.Terminal;
import org.dubhe.terminal.domain.entity.TerminalInfo;
import org.dubhe.terminal.domain.vo.TerminalVO;

import java.util.List;

/**
 * @description 专业版终端
 * @date 2021-07-12
 */
public interface TerminalService {
    /**
     * 创建
     *
     * @param terminalCreateDTO
     * @return TerminalVO
     */
    TerminalVO create(TerminalCreateDTO terminalCreateDTO);

    /**
     * 重启
     *
     * @param terminalCreateDTO
     * @return TerminalVO
     */
    TerminalVO restart(TerminalCreateDTO terminalCreateDTO);

    /**
     * 保存并停止
     *
     * @param terminalPreserveDTO
     * @return boolean
     */
    boolean preserve(TerminalPreserveDTO terminalPreserveDTO);

    /**
     * 删除
     *
     * @param terminalDTO
     * @return boolean
     */
    boolean delete(TerminalDTO terminalDTO);

    /**
     * 查询详情
     *
     * @param terminalDTO
     * @param enableUsername 是否需要查询创建用户名
     * @return
     */
    TerminalVO detail(TerminalDTO terminalDTO,boolean enableUsername);

    /**
     * 获取终端列表
     *
     * @param refreshStatus 是否从k8s刷新当前终端状态
     * @return
     */
    List<TerminalVO> listWithK8sStatus(boolean refreshStatus);

    /**
     * 获取用户对应 终端列表
     *
     * @param user
     * @return
     */
    List<Terminal> list(UserContext user);

    /**
     * terminal 列表获得vo
     *
     * @param terminalList terminal 列表
     * @return
     */
    List<TerminalVO> listVO(List<Terminal> terminalList);

    /**
     * 刷新终端中节点 terminalInfo 状态与 k8s 集群同步
     *
     * @param terminalInfoIdList TerminalInfo id 列表
     * @return
     */
    List<TerminalInfo> refreshTerminalInfoStatus(List<Long> terminalInfoIdList);

    /**
     * 刷新终端 terminal 状态与k8s集群同步
     *
     * @param idList Terminal id列表
     * @return
     */
    List<Terminal> refreshTerminalStatus(List<Long> idList);

    /**
     * k8s回调pod在线服务状态
     *
     * @param times 回调请求次数
     * @param req  回调请求对象
     * @return boolean 返回是否回调成功
     */
    boolean terminalPodCallback(int times, TerminalK8sPodCallbackCreateDTO req);

    /**
     * 推送镜像完成
     *
     * @param terminalId
     */
    void pushImageComplete(Long terminalId,Long userId);

    /**
     * 推送镜像失败
     *
     * @param terminalId
     * @param message 失败信息
     */
    void pushImageError(Long terminalId,String message,Long userId);

    /**
     * 一个终端内所有连接就绪后执行的任务
     *
     * @param terminalId
     */
    void terminalReadyTask(Long terminalId);


    /**
     * 更新终端描述
     *
     * @param  terminalDetailDTO
     * @return boolean
     */
    boolean update(TerminalDetailDTO terminalDetailDTO);
}
