package org.dubhe.terminal.service.impl;


import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.binding.MapperMethod;
import org.dubhe.terminal.dao.TerminalInfoMapper;
import org.dubhe.terminal.domain.entity.TerminalInfo;
import org.dubhe.terminal.service.TerminalInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * @description 专业版终端节点实现
 * @date 2021-10-28
 */
@Service
public class TerminalInfoServiceImpl extends ServiceImpl<TerminalInfoMapper,TerminalInfo> implements TerminalInfoService {

    /**
     * 批量更新
     * @param terminalInfoList TerminalInfo 列表
     * @return
     */
    @Override
    public boolean updateBatchById(List<TerminalInfo> terminalInfoList) {
        return updateBatchById(terminalInfoList,terminalInfoList.size());
    }
}
