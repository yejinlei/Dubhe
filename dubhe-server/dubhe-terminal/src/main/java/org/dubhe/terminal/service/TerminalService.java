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

import org.dubhe.terminal.domain.dto.TerminalCreateDTO;
import org.dubhe.terminal.domain.dto.TerminalDTO;
import org.dubhe.terminal.domain.dto.TerminalK8sPodCallbackCreateDTO;
import org.dubhe.terminal.domain.dto.TerminalPreserveDTO;
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
     * @return
     */
    TerminalVO detail(TerminalDTO terminalDTO);

    /**
     * 查询列表
     *
     * @return
     */
    List<TerminalVO> list();

    /**
     * 刷新 TerminalInfo 状态
     *
     * @param id
     */
    TerminalInfo refreshTerminalInfoStatus(Long id);

    /**
     * 刷新 Terminal 状态
     *
     * @param id
     * @return
     */
    Terminal refreshTerminalStatus(Long id);

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
}
