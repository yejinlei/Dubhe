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
package org.dubhe.tadl.service.impl;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.db.utils.WrapperHelp;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.domain.dto.PodQueryDTO;
import org.dubhe.k8s.domain.vo.PodVO;
import org.dubhe.k8s.service.PodService;
import org.dubhe.k8s.utils.K8sNameTool;
import org.dubhe.tadl.dao.TrialMapper;
import org.dubhe.tadl.domain.dto.TrialDTO;
import org.dubhe.tadl.domain.entity.*;
import org.dubhe.tadl.domain.vo.TrialListVO;
import org.dubhe.tadl.enums.ExperimentStageStateEnum;
import org.dubhe.tadl.enums.ExperimentStatusEnum;
import org.dubhe.tadl.enums.TadlErrorEnum;
import org.dubhe.tadl.enums.TrialStatusEnum;
import org.dubhe.tadl.service.*;
import org.dubhe.tadl.utils.TimeCalculateUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description Tadl 服务实现类
 * @date 2020-12-28
 */
@Service
public class TadlTrialServiceImpl implements TadlTrialService {

    @Resource
    private TrialMapper trialMapper;

    @Resource
    private UserContextService userContextService;

    @Resource
    private K8sNameTool k8sNameTool;

    /**
     * 算法阶段服务
     */
    @Resource
    private AlgorithmStageService algorithmStageService;

    /**
     * 算法服务
     */
    @Resource
    private AlgorithmService algorithmService;

    /**
     * 实验阶段服务
     */
    @Resource
    private ExperimentStageService experimentStageService;
    /**
     * 实验服务
     */
    @Resource
    private ExperimentService experimentService;

    /**
     * 获取Trial中所有属性
     */
    private final Field[] fields = Trial.class.getDeclaredFields();

    /**
     * trial data 业务对象
     */
    @Resource
    private TrialDataService trialDataService;

    @Resource
    private PodService podService;

    /**
     * trial列表查询
     *
     * @param trialDTO trial查询dto
     * @return trial列表
     */
    @Override
    public Map<String, Object> listVO(TrialDTO trialDTO) {
        if (trialDTO.getCurrent() == null || trialDTO.getSize() == null) {
            throw new BusinessException(TadlErrorEnum.PARAM_ERROR);
        }

        Experiment experiment = experimentService.selectById(trialDTO.getExperimentId());

        if (experiment == null) {
            throw new BusinessException("实验不存在");
        }
        String namespace = k8sNameTool.getNamespace(experiment.getCreateUserId());

        QueryWrapper<Trial> wrapper = WrapperHelp.getWrapper(trialDTO);
        //实验不能为删除
        wrapper.lambda().eq(Trial::getDeleted, MagicNumConstant.ZERO);
        //根据名称和id查询
        if (StringUtils.isNotBlank(trialDTO.getSearchParam())) {
            wrapper.lambda().and(w ->
                    w.eq(Trial::getId, trialDTO.getSearchParam())
                            .or()
                            .like(Trial::getName, trialDTO.getSearchParam())
            );
        }
        //排序
        if (StringUtils.isNotBlank(trialDTO.getSort())) {
            for (Field field : fields) {
                if (field.getName().equals(trialDTO.getSort())) {
                    field.setAccessible(true);
                    TableField annotation = field.getAnnotation(TableField.class);
                    if (annotation == null) {
                        continue;
                    }
                    if ("desc".equals(trialDTO.getOrder())) {
                        wrapper.orderByDesc(annotation.value());
                    } else if ("asc".equals(trialDTO.getOrder())) {
                        wrapper.orderByAsc(annotation.value());
                    }
                }
            }
        } else {
            wrapper.lambda().orderByDesc(Trial::getUpdateTime);
        }
        ExperimentStage experimentStage = experimentStageService.selectOne(trialDTO.getExperimentId(),trialDTO.getStageOrder());
        AlgorithmStage algorithmStage = algorithmStageService.getOneById(experimentStage.getAlgorithmStageId());
        Algorithm algorithm = algorithmService.getOneById(algorithmStage.getAlgorithmId());
        wrapper.lambda().eq(Trial::getStageId,experimentStage.getId());
        //组装数据
        Page<TrialListVO> pages = new Page<TrialListVO>() {{
            setCurrent(trialDTO.getCurrent());
            setSize(trialDTO.getSize());
            setTotal(trialMapper.selectCount(wrapper));
            List<TrialListVO> collect = trialMapper.selectList(
                    wrapper
                            .last(" limit " + (trialDTO.getCurrent() - NumberConstant.NUMBER_1) * trialDTO.getSize() + ", " + trialDTO.getSize())
            ).stream().map(TrialListVO::from).peek(val->{
                val.setRunTime(TimeCalculateUtil.getRunTime(val.getEndTime(),val.getStartTime()));
                val.setValue(trialDataService.selectOneByTrialId(val.getId()).getValue());
                val.setResourceName(experimentStage.getResourceName());
                val.setExecuteScript(algorithmStage.getExecuteScript());
                val.setAlgorithmName(algorithm.getName());
                PodVO podVO = podService
                        .getPods(new PodQueryDTO(namespace, val.getK8sResourceName())).stream().findFirst().orElse(null);
                if (podVO != null){
                val.setPodName(podVO.getPodName());
                }
            }).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(collect)) {
                setRecords(collect);
            }
        }};
        return PageUtil.toPage(pages);
    }

    /**
     * 根据实验阶段ID查询trial状态列表
     *
     * @param experimentStageId      实验阶段id
     * @return List<Integer>         trial 状态set
     */
    @Override
    public List<Integer> getExperimentStageStateByTrial(Long experimentStageId) {
        if (experimentStageId == null) {
            LogUtil.error(LogEnum.TADL, "experimentStageId isEmpty");
            return null;
        }
        return trialMapper.getExperimentStageStateByTrial(experimentStageId);
    }

    /**
     * 批量写入trial
     *
     * @param trials trial 列表
     */
    @Override
    public void insertList(List<Trial> trials) {
        trialMapper.saveList(trials);
    }

    /**
     * 获取 trial 列表
     *
     * @param wrapper 查询条件
     * @return
     */
    @Override
    public List<Trial> getTrialList(LambdaQueryWrapper<Trial> wrapper){
        return trialMapper.selectList(wrapper);
    }


    /**
     * 删除 trial
     *
     * @param wrapper 删除实验 wrapper
     */
    @Override
    public void delete(LambdaQueryWrapper<Trial> wrapper){
        trialMapper.delete(wrapper);
    }

    @Override
    public Integer selectCount(LambdaQueryWrapper<Trial> wrapper) {
        return trialMapper.selectCount(wrapper);
    }

    /**
     * 获取当前阶段最佳的精度
     *
     * @param experimentId 实验ID
     * @param stageId      阶段ID
     * @return 当前阶段最佳精度
     */
    @Override
    public double getBestData(Long experimentId, Long stageId) {
        return trialMapper.getBestData(experimentId,stageId);
    }

    /**
     * 查询一个 trial
     *
     * @param id trial ID
     * @return trial
     */
    @Override
    public Trial selectOne(Long id){
        return trialMapper.selectById(id);
    }

    @Override
    public Integer updateTrial(LambdaUpdateWrapper<Trial> wrapper) {
        return trialMapper.update(null,wrapper);
    }

    @Override
    public void updateTrialFailed(Long trialId,String statusDetail) {
        trialMapper.updateTrialFailed(trialId, TrialStatusEnum.FAILED.getVal(),
                ExperimentStageStateEnum.FAILED_EXPERIMENT_STAGE_STATE.getCode(),
                ExperimentStatusEnum.FAILED_EXPERIMENT_STATE.getValue(),statusDetail);
    }

    @Override
    public List<PodVO> getPods(Long id) {
        // 从会话中获取用户信息
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }

        Trial trial= trialMapper.selectById(id);
        if (trial == null) {
            return Collections.emptyList();
        }
        Experiment experiment = experimentService.selectById(trial.getExperimentId());
        if (experiment == null) {
            throw new BusinessException("实验不存在");
        }

        String namespace = k8sNameTool.getNamespace(experiment.getCreateUserId());
        List<PodVO> podVOList = podService
                .getPods(new PodQueryDTO(namespace, trial.getResourceName()));
        return podVOList;
    }

}
