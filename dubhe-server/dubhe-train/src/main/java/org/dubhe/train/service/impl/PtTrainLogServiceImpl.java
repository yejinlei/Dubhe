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

package org.dubhe.train.service.impl;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.StringUtils;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.api.LogMonitoringApi;
import org.dubhe.k8s.domain.bo.LogMonitoringBO;
import org.dubhe.k8s.domain.dto.PodQueryDTO;
import org.dubhe.k8s.domain.vo.LogMonitoringVO;
import org.dubhe.k8s.domain.vo.PodVO;
import org.dubhe.k8s.service.PodService;
import org.dubhe.k8s.utils.K8sNameTool;
import org.dubhe.train.dao.PtTrainJobMapper;
import org.dubhe.train.domain.dto.PtTrainLogQueryDTO;
import org.dubhe.train.domain.entity.PtTrainJob;
import org.dubhe.train.domain.vo.PtTrainLogQueryVO;
import org.dubhe.train.service.PtTrainLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;


/**
 * @description 训练日志 服务实现类
 * @date 2020-05-08
 */
@Service
public class PtTrainLogServiceImpl implements PtTrainLogService {

    @Autowired
    private LogMonitoringApi logMonitoringApi;

    @Autowired
    private PtTrainJobMapper ptTrainJobMapper;

    @Autowired
    private K8sNameTool k8sNameTool;

    @Autowired
    private PodService podService;

    @Autowired
    private UserContextService userContextService;

    /**
     * 查询训练任务运行日志
     *
     * @param ptTrainLogQueryDTO   训练日志查询
     * @return PtTrainLogQueryVO   返回训练日志查询
     **/
    @Override
    public PtTrainLogQueryVO queryTrainLog(PtTrainLogQueryDTO ptTrainLogQueryDTO) {
        PtTrainJob ptTrainJob = ptTrainJobMapper.selectById(ptTrainLogQueryDTO.getJobId());
        if (null == ptTrainJob) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "It is illegal for the user {} to look up the training job log with jobId as {}", userContextService.getCurUserId(), ptTrainLogQueryDTO.getJobId());
            throw new BusinessException("内部错误");
        }
        String namespace = k8sNameTool.generateNamespace(ptTrainJob.getCreateUserId());
        /** 查询日志起始行 **/
        Integer startLine = null == ptTrainLogQueryDTO.getStartLine() ? NumberConstant.NUMBER_1 : ptTrainLogQueryDTO.getStartLine();
        /** 查询日志总行数 **/
        Integer lines = null == ptTrainLogQueryDTO.getLines() ? NumberConstant.NUMBER_50 : ptTrainLogQueryDTO.getLines();
        /** 拼接请求es的参数 **/
        LogMonitoringBO logMonitoringBo = new LogMonitoringBO();
        logMonitoringBo.setNamespace(namespace)
                .setResourceName(ptTrainJob.getJobName());

        PtTrainLogQueryVO ptTrainLogQueryVO = new PtTrainLogQueryVO();

        LogMonitoringVO result = logMonitoringApi.searchLogByResName(startLine, lines, logMonitoringBo);
        List<String> list = result.getLogs();

        if (CollectionUtils.isEmpty(list)) {
            ptTrainLogQueryVO.setContent(list);
            ptTrainLogQueryVO.setStartLine(startLine);
            ptTrainLogQueryVO.setEndLine(startLine - 1);
            ptTrainLogQueryVO.setLines(0);
            return ptTrainLogQueryVO;
        }

        ptTrainLogQueryVO.setContent(list).setStartLine(startLine)
                .setEndLine(Long.valueOf(startLine + result.getTotalLogs()).intValue() - 1)
                .setLines(Long.valueOf(result.getTotalLogs()).intValue()).setJobName(ptTrainJob.getJobName());
        return ptTrainLogQueryVO;
    }

    /**
     * 字符串换行
     *
     * @param content      内容
     * @return strContent  返回字符串内容
     */
    @Override
    public String getTrainLogString(List<String> content) {
        if (content == null) {
            return SymbolConstant.BLANK;
        }
        return StringUtils.join(content, StrUtil.CRLF);
    }

    /**
     * 获取训练任务的Pod
     *
     * @param id 训练作业job表 id
     * @return 训练节点信息
     */
    @Override
    public List<PodVO> getPods(Long id) {
        PtTrainJob ptTrainJob = ptTrainJobMapper.selectById(id);
        if (ptTrainJob == null) {
            return Collections.emptyList();
        }
        String nameSpace = k8sNameTool.generateNamespace(ptTrainJob.getCreateUserId());
        return podService.getPods(new PodQueryDTO(nameSpace, ptTrainJob.getJobName()));
    }
}
