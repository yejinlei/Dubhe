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
package org.dubhe.tadl.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.enums.BizEnum;
import org.dubhe.biz.base.enums.ResourcesPoolTypeEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.statemachine.dto.StateChangeDTO;
import org.dubhe.k8s.api.LogMonitoringApi;
import org.dubhe.k8s.api.TrainJobApi;
import org.dubhe.k8s.cache.ResourceCache;
import org.dubhe.k8s.domain.bo.PtJupyterJobBO;
import org.dubhe.k8s.domain.bo.PtMountDirBO;
import org.dubhe.k8s.domain.vo.PtJupyterJobVO;
import org.dubhe.k8s.utils.K8sNameTool;
import org.dubhe.tadl.config.TadlJobConfig;
import org.dubhe.tadl.constant.TadlConstant;
import org.dubhe.tadl.domain.dto.TrialDeleteDTO;
import org.dubhe.tadl.domain.dto.TrialRunParamDTO;
import org.dubhe.tadl.domain.entity.Experiment;
import org.dubhe.tadl.domain.entity.Trial;
import org.dubhe.tadl.enums.ExperimentStatusEnum;
import org.dubhe.tadl.machine.constant.ExperimentEventMachineConstant;
import org.dubhe.tadl.machine.constant.ExperimentStageEventMachineConstant;
import org.dubhe.tadl.machine.constant.TrialEventMachineConstant;
import org.dubhe.tadl.machine.utils.identify.StateMachineStatusUtil;
import org.dubhe.tadl.machine.utils.identify.StateMachineUtil;
import org.dubhe.tadl.service.ExperimentService;
import org.dubhe.tadl.service.TadlRedisService;
import org.dubhe.tadl.service.TadlTrialService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description trial异步处理
 * @date 2021-02-26
 */
@Component
public class TrialJobAsyncTask {

    @Resource
    private K8sNameTool k8sNameTool;
    @Resource
    private TadlJobConfig tadlJobConfig;
    @Resource
    private TrainJobApi trainJobApi;
    @Resource
    private TadlTrialService tadlTrialService;

    @Resource
    private ExperimentService experimentService;
    @Resource
    private ResourceCache resourceCache;

    @Resource
    @Lazy
    private TadlRedisService tadlRedisService;

    @Resource
    private LogMonitoringApi logMonitoringApi;

    @Value("Task:TADL:" + "${spring.profiles.active}_experiment_id_")
    private String experimentIdPrefix;

    @Resource
    private StateMachineStatusUtil stateMachineStatusUtil;

    /**
     * 启动trial
     *
     * @param trialRunParam trial启动参数
     */
    @Async("tadlExecutor")
    @Transactional(rollbackFor = Exception.class)
    public void runTrial(TrialRunParamDTO trialRunParam) {
        try {
            PtJupyterJobBO tadlJobBO = buildJobBO(trialRunParam);
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+"Tadl job name :{}",trialRunParam.getExperimentId(),trialRunParam.getStageId(),trialRunParam.getTrialId(),tadlJobBO.getName());
            Experiment experiment =experimentService.selectById(trialRunParam.getExperimentId());
            if (ExperimentStatusEnum.FAILED_EXPERIMENT_STATE.getValue().equals(experiment.getStatus())){
                LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+"The experiment has failed  {}", trialRunParam.getExperimentId(),trialRunParam.getStageId(),trialRunParam.getTrialId());
                return;
            }
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "K8s pod job info: namespace:{},name,{},cmdLines:{},",trialRunParam.getExperimentId(),tadlJobBO.getNamespace(),tadlJobBO.getNamespace(),tadlJobBO.getCmdLines());
            logMonitoringApi.addTadlLogsToEs(trialRunParam.getExperimentId(),"K8s pod job info: namespace:" + tadlJobBO.getNamespace() + ",cmdLines:" + tadlJobBO.getCmdLines());

            PtJupyterJobVO vo = trainJobApi.create(tadlJobBO);
            if (vo.isSuccess()) {
                LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+"Record trial resource name :{}", trialRunParam.getExperimentId(),trialRunParam.getStageId(),trialRunParam.getTrialId(), tadlJobBO.getName());
                //记录k8s资源信息，并记录 trial 实验开始时间
                tadlTrialService.updateTrial(new LambdaUpdateWrapper<Trial>() {
                    {
                        eq(Trial::getId, trialRunParam.getTrialId())
                                .set(Trial::getResourceName, tadlJobBO.getName());
                    }
                });

                LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+"record trial resource name success. resource name :{}", trialRunParam.getExperimentId(),trialRunParam.getStageId(),trialRunParam.getTrialId(),trialRunParam.getName());
            } else {
                LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+"Failed to start trial experiment.Error message: {}", trialRunParam.getExperimentId(),trialRunParam.getStageId(),trialRunParam.getTrialId(), vo.getMessage());
                experiment.putStatusDetail(TadlConstant.TRIAL_STARTUP_FAILED,vo.getMessage());
                stateMachineStatusUtil.trialExperimentFailedState(trialRunParam.getExperimentId(),trialRunParam.getStageId(),trialRunParam.getTrialId(),experiment.getStatusDetail());
            }

        } catch (Exception e) {
            //变更实验，实验阶段 运行失败
            LogUtil.error(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+" Failed to start trial experiment.Exception message: {}", trialRunParam.getExperimentId(),trialRunParam.getStageId(),trialRunParam.getTrialId(), e.getMessage());
            String statusDetail = StringUtils.putIntoJsonStringMap(TadlConstant.TRIAL_STARTUP_EXCEPTION, e.getMessage(), null);
            StateMachineUtil.stateChange(Arrays.asList(new StateChangeDTO(new Object[]{trialRunParam.getExperimentId(), statusDetail},ExperimentEventMachineConstant.EXPERIMENT_STATE_MACHINE,ExperimentEventMachineConstant.FAILED_EXPERIMENT_EVENT)
                    , new StateChangeDTO(new Object[]{trialRunParam.getStageId()},ExperimentStageEventMachineConstant.EXPERIMENT_STAGE_STATE_MACHINE,ExperimentStageEventMachineConstant.FAILED_EXPERIMENT_STAGE_EVENT) ));
            //因删除运行中的trial存在异步方法中，在不同线程可能会出现因阻塞后查询不到其他已提交的事务，需要开启新的事务进行查询
            tadlRedisService.deleteRunningTrial(trialRunParam.getStageId());
            tadlRedisService.delRedisExperimentInfo(trialRunParam.getExperimentId());
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+" Update experiment  to failed status", trialRunParam.getExperimentId(),trialRunParam.getStageId(),trialRunParam.getTrialId());
            throw new BusinessException("启动trial实验异常");
        }
    }

    /**
     * 删除trial 任务
     * @param trialDeleteDTOList 删除条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTrialList(List<TrialDeleteDTO> trialDeleteDTOList) {
        //三次重试均反馈失败则给予删除失败结果
        int tryTime = 1;
        while (!trialDeleteDTOList.isEmpty()){
            //重试三次
            if (tryTime<=3){
                Iterator<TrialDeleteDTO> intIterator =trialDeleteDTOList.iterator();
                while (intIterator.hasNext()){
                    TrialDeleteDTO trialDeleteDTO = intIterator.next();
                    Boolean result = trainJobApi.delete(trialDeleteDTO.getNamespace(),trialDeleteDTO.getResourceName());
                    //删除trial任务调用成功
                    if (result){
                        intIterator.remove();
                        LogUtil.info(LogEnum.TADL,TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+"The trial experiment began to delete.namespace:{},resource name :{},result :{}",
                                trialDeleteDTO.getExperimentId(),trialDeleteDTO.getStageId(),trialDeleteDTO.getTrialId(),trialDeleteDTO.getNamespace(),trialDeleteDTO.getResourceName(),result);
                    }
                }
                tryTime++;
            }else {
                TrialDeleteDTO  trialDeleteDTO = trialDeleteDTOList.stream().findFirst().get();
                LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG+"Failed to delete running pod.The failed trial size :{} ", trialDeleteDTO.getExperimentId(), trialDeleteDTO.getStageId(),trialDeleteDTOList.size());
                String statusDetail = StringUtils.putIntoJsonStringMap(TadlConstant.TRIAL_TASK_DELETE_EXCEPTION, "k8s trial job删除异常", null);
                List<StateChangeDTO> stateChangeDTOList = trialDeleteDTOList.stream().map(deleteDTO -> new StateChangeDTO(new Object[]{deleteDTO.getTrialId(),statusDetail},TrialEventMachineConstant.TRIAL_STATE_MACHINE,TrialEventMachineConstant.FAILED_TRIAL_EVENT)).collect(Collectors.toList());
                StateMachineUtil.stateChange(stateChangeDTOList);
                break;
            }
        }

    }


    /**
     * 构建运行参数BO
     *
     * @param trialRunParam
     * @return
     */
    private PtJupyterJobBO buildJobBO(TrialRunParamDTO trialRunParam) {
        Map<String, String> map = new HashMap<>(NumberConstant.NUMBER_6);
        //extraLabelMap 需要匹配正则 [a-z0-9]([-a-z0-9]*[a-z0-9])?
        map.put("experiment-id", String.valueOf(trialRunParam.getExperimentId()));
        map.put("stage-id", String.valueOf(trialRunParam.getStageId()));
        map.put("trial-id", String.valueOf(trialRunParam.getTrialId()));
        map.put("redis-stream-recode-id", trialRunParam.getRedisStreamRecodeId());
        String resourceInfo = trialRunParam.getExperimentId() + SymbolConstant.HYPHEN + trialRunParam.getStageId() + SymbolConstant.HYPHEN + trialRunParam.getTrialId() + SymbolConstant.HYPHEN + StringUtils.getRandomString();
        String taskIdentify = resourceCache.getTaskIdentify(trialRunParam.getExperimentId(), trialRunParam.getName(), experimentIdPrefix);
        PtJupyterJobBO bo = new PtJupyterJobBO()
                .setNamespace(trialRunParam.getNamespace())
                .setName(k8sNameTool.generateResourceName(BizEnum.TADL, resourceInfo))
                .setGpuNum(trialRunParam.getGpuNum())
                .setCpuNum(trialRunParam.getCpuNum() * MagicNumConstant.ONE_THOUSAND)
                .setUseGpu(ResourcesPoolTypeEnum.isGpuCode(trialRunParam.getResourcesPoolType()))
                .setMemNum(trialRunParam.getMemNum())
                .setCmdLines(Arrays.asList("-c", trialRunParam.getCommand()))
                .setFsMounts(new HashMap<String, PtMountDirBO>(NumberConstant.NUMBER_6) {{
                    put(tadlJobConfig.getDockerDatasetPath(), new PtMountDirBO(k8sNameTool.getAbsolutePath(trialRunParam.getDatasetPath())));
                    put(tadlJobConfig.getDockerExperimentPath(), new PtMountDirBO(k8sNameTool.getAbsolutePath(trialRunParam.getExperimentPath())));
                }})
                .setImage(tadlJobConfig.getImage())
                .setBusinessLabel(k8sNameTool.getPodLabel(BizEnum.TADL))
                .setTaskIdentifyLabel(taskIdentify)
                .setExtraLabelMap(map);
        return bo;
    }

}
