package org.dubhe.terminal.service;

import org.dubhe.terminal.domain.entity.TerminalInfo;

import java.util.List;

/**
 * @description 专业版终端节点
 * @date 2021-10-28
 */
public interface TerminalInfoService {
    /**
     * 批量更新
     * @param terminalInfoList TerminalInfo 列表
     * @return
     */
    boolean updateBatchById(List<TerminalInfo> terminalInfoList);
}
