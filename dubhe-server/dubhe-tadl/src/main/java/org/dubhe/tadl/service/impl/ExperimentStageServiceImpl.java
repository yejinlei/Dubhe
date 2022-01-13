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

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.beanutils.BeanUtils;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.file.utils.MinioUtil;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.redis.utils.RedisUtils;
import org.dubhe.tadl.constant.RedisKeyConstant;
import org.dubhe.tadl.constant.TadlConstant;
import org.dubhe.tadl.dao.ExperimentStageMapper;
import org.dubhe.tadl.dao.TrialMapper;
import org.dubhe.tadl.domain.dto.*;
import org.dubhe.tadl.domain.entity.*;
import org.dubhe.tadl.domain.vo.ExperimentStageParamVO;
import org.dubhe.tadl.domain.vo.RuntimeParamVO;
import org.dubhe.tadl.domain.vo.StageOutlineVO;
import org.dubhe.tadl.domain.vo.TrialVO;
import org.dubhe.tadl.enums.*;
import org.dubhe.tadl.service.*;
import org.dubhe.tadl.utils.PathUtil;
import org.dubhe.tadl.utils.TimeCalculateUtil;
import org.dubhe.tadl.utils.YamlParseUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * @description 实验阶段管理服务实现类
 * @date 2021-03-22
 */
@Service
public class ExperimentStageServiceImpl extends ServiceImpl<ExperimentStageMapper, ExperimentStage> implements ExperimentStageService {


    /**
     * 路径工具类
     */
    @Resource
    private PathUtil pathUtil;

    /**
     * MinIO工具类
     */
    @Resource
    private MinioUtil minioUtil;

    /**
     * minIO桶名
     */
    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * 实验阶段 mapper
     */
    @Resource
    private ExperimentStageMapper experimentStageMapper;

    /**
     * trial 业务对象
     */
    @Resource
    private TadlTrialService trialService;

    /**
     * trial data 业务对象
     */
    @Resource
    private TrialDataService trialDataService;

    /**
     * 算法阶段服务
     */
    @Resource
    private AlgorithmStageService algorithmStageService;

    @Resource
    private ExperimentService experimentService;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private TrialMapper trialMapper;

    @Resource
    private RedissonClient redissonClient;

    private static HashSet<Integer> prohibitEdit = new HashSet<Integer>() {{
        add(ExperimentStatusEnum.FINISHED_EXPERIMENT_STATE.getValue());
    }};

    /**
     * 查询单条实验阶段记录
     *
     * @param experimentStageId 实验阶段id
     * @return 实验阶段对象
     */
    @Override
    public ExperimentStage selectById(Long experimentStageId) {
        return baseMapper.selectById(experimentStageId);
    }

    /**
     * 根据实验ID查询实验阶段状态列表
     *
     * @param experimentId 实验id
     * @return List<Integer>    实验阶段状态列表
     */
    @Override
    public List<Integer> getExperimentStateByStage(Long experimentId) {
        if (experimentId == null) {
            LogUtil.error(LogEnum.TADL, "experimentId isEmpty");
            return null;
        }
        return experimentStageMapper.getExperimentStateByStage(experimentId);
    }

    /**
     * 根据实验id获取实验阶段列表
     *
     * @param experimentId 实验id
     * @return 实验阶段列表
     */
    @Override
    public List<ExperimentStage> getExperimentStageListByExperimentId(Long experimentId) {
        return baseMapper.selectList(new LambdaQueryWrapper<ExperimentStage>().eq(ExperimentStage::getExperimentId, experimentId));
    }

    /**
     * 获取 experimentStage 列表，并根据阶段顺序升序排列
     *
     * @param wrapper 查询条件
     * @return 实验阶段列表
     */
    @Override
    public List<ExperimentStage> getExperimentStageList(LambdaQueryWrapper<ExperimentStage> wrapper) {
        return baseMapper.selectList(wrapper);
    }

    /**
     * 根据实验id获取实验阶段状态列表
     * @return 实验阶段列表
     */
    @Override
    public List<ExperimentStage> getStatusListSorted() {
        return baseMapper.selectList(new LambdaQueryWrapper<ExperimentStage>()
                .select(ExperimentStage::getId,
                        ExperimentStage::getExperimentId,
                        ExperimentStage::getStageName,
                        ExperimentStage::getStageOrder,
                        ExperimentStage::getStatus,
                        ExperimentStage::getEndTime,
                        ExperimentStage::getUpdateTime,
                        ExperimentStage::getBeginTime,
                        ExperimentStage::getRunTime)
                .orderByAsc(ExperimentStage::getExperimentId)
                .orderByAsc(ExperimentStage::getStageOrder)
        );

    }

    /**
     * 创建实验阶段
     *
     * @param experimentStage 实验阶段
     */
    @Override
    public void insert(ExperimentStage experimentStage) {
        baseMapper.insert(experimentStage);
    }

    /**
     * 根据阶段 id 更新实验阶段
     *
     * @param experimentStage 实验阶段
     */
    @Override
    public void updateExperimentStageById(ExperimentStage experimentStage) {
        baseMapper.updateById(experimentStage);
    }

    /**
     * 查询阶段概览
     *
     * @param experimentId 实验ID
     * @param stageOrder   阶段排序
     * @return 阶段概览
     */
    @Override
    public StageOutlineVO query(Long experimentId, Integer stageOrder) {
        // 查询阶段ID
        ExperimentStage experimentStage = selectOne(experimentId, stageOrder);
        // 查询当前阶段trial数量
        Integer count = trialService.selectCount(new LambdaQueryWrapper<Trial>() {{
            eq(Trial::getExperimentId, experimentId);
            eq(Trial::getStageId, experimentStage.getId());
            eq(Trial::getDeleted, false);
        }});
        StageOutlineVO outlineVO = new StageOutlineVO();
        if (ObjectUtils.isEmpty(count)) {
            return outlineVO;
        }
        return new StageOutlineVO() {{
            // 查询每个状态的数量
            if (count >= NumberConstant.NUMBER_1) {
                setFailed(trialService.selectCount(new LambdaQueryWrapper<Trial>() {{
                    eq(Trial::getExperimentId, experimentId);
                    eq(Trial::getStageId, experimentStage.getId());
                    eq(Trial::getDeleted, false);
                    and(qw -> qw.eq(Trial::getStatus, TrialStatusEnum.FAILED.getVal()).or().eq(Trial::getStatus, TrialStatusEnum.UNKNOWN.getVal()));
                }}));
                setFinished(trialService.selectCount(new LambdaQueryWrapper<Trial>() {{
                    eq(Trial::getExperimentId, experimentId);
                    eq(Trial::getStageId, experimentStage.getId());
                    eq(Trial::getDeleted, false);
                    eq(Trial::getStatus, TrialStatusEnum.FINISHED.getVal());
                }}));
                setWaiting(trialService.selectCount(new LambdaQueryWrapper<Trial>(){{
                    eq(Trial::getExperimentId,experimentId);
                    eq(Trial::getStageId,experimentStage.getId());
                    eq(Trial::getDeleted,false);
                    eq(Trial::getStatus,TrialStatusEnum.WAITING.getVal());
                }}));
                setRunning(trialService.selectCount(new LambdaQueryWrapper<Trial>() {{
                    eq(Trial::getExperimentId, experimentId);
                    eq(Trial::getStageId, experimentStage.getId());
                    eq(Trial::getDeleted, false);
                    eq(Trial::getStatus, TrialStatusEnum.RUNNING.getVal());
                }}));
                setToRun(trialService.selectCount(new LambdaQueryWrapper<Trial>() {{
                    eq(Trial::getExperimentId, experimentId);
                    eq(Trial::getStageId, experimentStage.getId());
                    eq(Trial::getDeleted, false);
                    eq(Trial::getStatus, TrialStatusEnum.TO_RUN.getVal());
                }}));
            } else {
                // 查询最佳的精度
                setSum(count);
                setBestData(trialService.getBestData(experimentId, experimentStage.getId()));
            }
        }};
    }

    /**
     * 查询实验阶段参数
     *
     * @param experimentId 实验ID
     * @param stageOrder   阶段ID
     * @return 实验运行参数VO
     */
    @Override
    public ExperimentStageParamVO queryStageParam(Long experimentId, Integer stageOrder) {
        // 获取实验阶段对象
        ExperimentStage experimentStage = selectOne(experimentId, stageOrder);
        // 获取算法阶段对象
        AlgorithmStage algorithmStage = algorithmStageService.getOneById(experimentStage.getAlgorithmStageId());
        // 计算运行时间
        long stageRunTime = experimentStage.getRunTime() == null ? 0 : experimentStage.getRunTime();
        // 判断状态，如果是运行中，则now-beginTime，其他状态，则就是runTime
        if (ExperimentStageStateEnum.RUNNING_EXPERIMENT_STAGE_STATE.getCode().equals(experimentStage.getStatus())
                && !ObjectUtils.isEmpty(experimentStage.getBeginTime())) {
            stageRunTime = System.currentTimeMillis() - experimentStage.getBeginTime().getTime() + stageRunTime;
        }
        ExperimentStageParamVO experimentStageParamVO = new ExperimentStageParamVO() {{
            setDatasetId(algorithmStage.getDatasetId());
            setDatasetName(algorithmStage.getDatasetName());
            setEndTime(experimentStage.getEndTime());
            setExecuteScript(algorithmStage.getExecuteScript());
            setResourceName(experimentStage.getResourceName());
            setStartTime(experimentStage.getStartTime());
        }};
        experimentStageParamVO.setRunTime(stageRunTime);
        return experimentStageParamVO;
    }

    /**
     * 查询实验阶段运行参数
     *
     * @param experimentId 实验ID
     * @param stageOrder   阶段ID
     * @return 实验运行参数VO
     */
    @Override
    public RuntimeParamVO queryRuntimeParam(Long experimentId, Integer stageOrder) {
        // 获取实验阶段对象
        ExperimentStage experimentStage = selectOne(experimentId, stageOrder);
        // 获取算法阶段对象
        Integer trialNum = trialMapper.getTrialCountOfStatus(experimentId, experimentStage.getId(), TrialStatusEnum.FINISHED.getVal());
        // 获取运行时间
        Long runTime = getRunTime(experimentStage);

        return new RuntimeParamVO() {{
            setRunTime(runTime);
            setMaxTrialNum(experimentStage.getMaxTrialNum());
            setMaxExecDuration(experimentStage.getMaxExecDuration());
            setTrialNum(trialNum);
            setTrialConcurrentNum(experimentStage.getTrialConcurrentNum());
            setMaxExecDurationUnit(experimentStage.getMaxExecDurationUnit());
        }};
    }

    private Long getRunTime(ExperimentStage experimentStage) {
        Long runTime = experimentStage.getRunTime();
        //判断状态，如果是运行中，则now-updateTime，其他状态，则就是runTime
        if (ExperimentStageStateEnum.RUNNING_EXPERIMENT_STAGE_STATE.getCode().equals(experimentStage.getStatus())) {
            runTime = System.currentTimeMillis() - experimentStage.getBeginTime().getTime() + runTime;
        }
        return runTime;
    }

    /**
     * 查询实验阶段运行参数
     *
     * @param experimentId 实验ID
     * @param stageOrder   阶段ID
     * @return 实验阶段对象
     */
    @Override
    public ExperimentStage selectOne(Long experimentId, Integer stageOrder) {
        ExperimentStage experimentStage = baseMapper.selectOne(new LambdaQueryWrapper<ExperimentStage>() {{
            eq(ExperimentStage::getExperimentId, experimentId);
            eq(ExperimentStage::getStageOrder, stageOrder);
        }});
        if (Objects.isNull(experimentStage)) {
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG+" Experimental stage is null", experimentId);
            throw new BusinessException("实验阶段不存在");
        }
        return experimentStage;
    }

    /**
     * 获取实验当前阶段 yaml 配置
     *
     * @param experimentId 实验ID
     * @param stageOrder   阶段ID
     * @return yaml
     */
    @Override
    public String getConfiguration(Long experimentId, Integer stageOrder) {
        if (!experimentService.queryEmpty(experimentId)) {
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_DOES_NOT_EXIST_ERROR);
        }
        try {
            return minioUtil.readString(
                    bucketName,
                    pathUtil.getExperimentYamlPath(StringUtils.EMPTY, experimentId) + StageEnum.getStageName(stageOrder) + TadlConstant.ALGORITHM_CONFIGURATION_FILE_SUFFIX
            );
        } catch (Exception e) {
            throw new BusinessException(TadlErrorEnum.PARAM_ERROR);
        }
    }

    /**
     * 修改实验阶段yaml
     *
     * @param updateStageYamlDTO 修改yaml DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfiguration(UpdateStageYamlDTO updateStageYamlDTO) {
        Experiment experiment = experimentService.selectById(updateStageYamlDTO.getExperimentId());
        ExperimentStage experimentStage = selectOne(updateStageYamlDTO.getExperimentId(), updateStageYamlDTO.getStageOrder());
        if (ObjectUtils.isEmpty(experimentStage) || ObjectUtils.isEmpty(experiment)) {
            LogUtil.error(LogEnum.TADL, "实验不存在. 实验id:{}", updateStageYamlDTO.getExperimentId());
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_DOES_NOT_EXIST_ERROR);
        }
        //若实验是已完成状态，则不能编辑实验阶段的yaml
        if (prohibitEdit.contains(experiment.getStatus())) {
            LogUtil.error(LogEnum.TADL, "当前实验状态不可编辑.experiment id:{},experiment status :{}", experiment.getId(), experiment.getStatus());
            throw new BusinessException(TadlErrorEnum.RUNTIME_PARAM_UPDATE_ERROR);
        }
        //若实验阶段非待运行中的状态，不能对该实验阶段的yaml进行编辑
        if (!experimentStage.getStatus().equals(ExperimentStageStateEnum.TO_RUN_EXPERIMENT_STAGE_STATE.getCode())) {
            LogUtil.error(LogEnum.TADL, "只有待运行状态才能编辑.stage id:{},experiment status :{}", experimentStage.getId(), experimentStage.getStatus());
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_UPDATE_ERROR);
        }
        //变更实验阶段相关 最大trial次数,最大运行时间，trail并发数量
        updateStageDetail(updateStageYamlDTO);
        try {
            //写算法 yaml 文件到 minio
            minioUtil.writeString(
                    bucketName,
                    pathUtil.getExperimentYamlPath(StringUtils.EMPTY, updateStageYamlDTO.getExperimentId())
                            .replaceFirst(TadlConstant.MODULE_URL_PREFIX, StringUtils.EMPTY) + StageEnum.getStageName(updateStageYamlDTO.getStageOrder()) + TadlConstant.ALGORITHM_CONFIGURATION_FILE_SUFFIX,
                    updateStageYamlDTO.getYaml()
            );
        } catch (Exception e) {
            LogUtil.error(LogEnum.TADL, "修改实验阶段yaml异常. 异常原因:{}", e.getMessage());
            throw new BusinessException(TadlErrorEnum.FILE_OPERATION_ERROR);
        }
    }

    /**
     * 根据yaml变更数据库中的实验阶段数据
     * @param updateStageYamlDTO 修改yaml DTO
     */
    public void updateStageDetail(UpdateStageYamlDTO updateStageYamlDTO) {

        HashMap<String, Object> readerMap = YamlParseUtil.YamlParse(updateStageYamlDTO.getYaml());

        JSONObject updateStageJson = getUpdateStageJson(readerMap);

        //分离yaml文件中最大运行时间参数和时间单位
        String maxExecDurationAndUnit = (String) updateStageJson.get("maxExecDuration");
        //最大运行时间和单位判空校验
        if (Objects.isNull(maxExecDurationAndUnit)) {
            LogUtil.error(LogEnum.TADL, "The experiment id:{},stage order:{}.Maximum running time cannot be empty!", updateStageYamlDTO.getExperimentId(), updateStageYamlDTO.getStageOrder());
            throw new BusinessException("当前阶段最长持续时间不能为空");
        }
        String maxExeDuration ;
        Matcher matcher = StringConstant.PATTERN_DECIMAL.matcher(maxExecDurationAndUnit);
        //matcher.find用来判断该字符串中是否含有与"(\\d+\\.\\d+)"相匹配的子串
        if (matcher.find()) {
            //group()中的参数：0表示匹配整个正则，1表示匹配第一个括号的正则,2表示匹配第二个正则,在这只有一个括号,即1和0是一样的
            maxExeDuration = matcher.group(NumberConstant.NUMBER_1);
        } else {
            //如果匹配不到小数，就进行整数匹配
            matcher = StringConstant.PATTERN_NUMBER.matcher(maxExecDurationAndUnit);
            //如果有整数相匹配
            maxExeDuration =matcher.find()? matcher.group(NumberConstant.NUMBER_1):"";
        }
        //时间参数判空校验
        if (StringUtils.isBlank(maxExeDuration)){
            throw new BusinessException("当前阶段最长持续时间参数异常");
        }
        updateStageJson.put("maxExecDuration", maxExeDuration);

        String maxExecDurationUnit = maxExecDurationAndUnit.replaceAll(maxExeDuration, "");
        //时间单位校验
        if (!TimeUnitEnum.isValid(maxExecDurationUnit)) {
            LogUtil.error(LogEnum.TADL, "The experiment id:{},stage order:{}.The maximum running time unit in the yaml configuration file is abnormal.", updateStageYamlDTO.getExperimentId(), updateStageYamlDTO.getStageOrder());
            throw new BusinessException("当前阶段最长持续时间单位异常");
        }
        updateStageJson.put("maxExecDurationUnit", maxExecDurationUnit);
        ExperimentStageDetailUpdateDTO experimentStageDetailUpdateDTO = new ExperimentStageDetailUpdateDTO();
        try {
            BeanUtils.populate(experimentStageDetailUpdateDTO, updateStageJson);
        } catch (Exception e) {
            LogUtil.error(LogEnum.TADL, "The experiment id:{},stage order:{}.Abnormal data conversion during the test phase!Error message：{}", updateStageYamlDTO.getExperimentId(), updateStageYamlDTO.getStageOrder(), e.getMessage());
            throw new BusinessException("实验阶段数据转换异常!");
        }
        //各参数的赋值规范校验
        validatorCheck(experimentStageDetailUpdateDTO);
        //业务 trail并发数量 需要小于等于当前阶段最大trial数量 校验
        if (experimentStageDetailUpdateDTO.getTrialConcurrentNum() > experimentStageDetailUpdateDTO.getMaxTrialNum()) {
            LogUtil.error(LogEnum.TADL, "The experiment id:{},stage order:{}.The maximum number of concurrent sessions cannot be greater than the total number of trials", updateStageYamlDTO.getExperimentId(), updateStageYamlDTO.getStageOrder());
            throw new BusinessException(TadlErrorEnum.UPDATE_TRIAL_CONCURRENT_NUM_ERROR);
        }

        //变更实验阶段相关数据
        this.update(new LambdaUpdateWrapper<ExperimentStage>()
                .eq(ExperimentStage::getExperimentId, updateStageYamlDTO.getExperimentId())
                        .eq(ExperimentStage::getStageOrder, updateStageYamlDTO.getStageOrder())
                        .set(ExperimentStage::getTrialConcurrentNum, experimentStageDetailUpdateDTO.getTrialConcurrentNum())
                        .set(ExperimentStage::getMaxTrialNum, experimentStageDetailUpdateDTO.getMaxTrialNum())
                        .set(ExperimentStage::getMaxExecDuration, experimentStageDetailUpdateDTO.getMaxExecDuration())
                        .set(ExperimentStage::getMaxExecDurationUnit, experimentStageDetailUpdateDTO.getMaxExecDurationUnit())
        );


    }

    /**
     * validator 进行校验字段规范
     * @param experimentStageDetailUpdateDTO
     */
    private void validatorCheck(ExperimentStageDetailUpdateDTO experimentStageDetailUpdateDTO) {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<ExperimentStageDetailUpdateDTO>> constraintViolationSet = validator.validate(experimentStageDetailUpdateDTO);
        if (!CollectionUtils.isEmpty(constraintViolationSet)) {
            List<String> validatorMessageList = constraintViolationSet.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            String errorMessage = StringUtils.join(validatorMessageList, ",");
            LogUtil.error(LogEnum.TADL, "The experiment id:{},stage order:{}.Validator error message:{}", errorMessage);
            throw new BusinessException(errorMessage);
        }
    }

    /**
     * 获取yaml中需要变更的实验阶段数据
     * @param readerMap yaml文件内容
     * @return JSONObject
     */
    private JSONObject getUpdateStageJson(Map<String, Object> readerMap) {
        JSONObject updateStageJson = new JSONObject();
        Field[] declaredFields = ExperimentStageDetailUpdateDTO.class.getDeclaredFields();
        for (Field field : declaredFields) {
            String fieldName = StrUtil.toUnderlineCase(field.getName());
            updateStageJson.put(field.getName(), readerMap.get(fieldName));
        }
        return updateStageJson;
    }

    @Override
    public List<TrialVO> queryTrialRep(Long experimentId, Integer stageOrder) {
        ExperimentStage experimentStage = selectOne(experimentId, stageOrder);
        List<TrialData> trialDataList = trialDataService.getTrialDataList(new LambdaQueryWrapper<TrialData>() {{
            eq(TrialData::getExperimentId, experimentId);
            eq(TrialData::getStageId, experimentStage.getId());
            orderByDesc(TrialData::getSequence);
            last("limit 5");
        }});
        AlgorithmStage algorithmStage = algorithmStageService.selectOneById(experimentStage.getAlgorithmStageId());
        List<TrialVO> trialVOS = TrialVO.from(trialDataList, algorithmStage);
        trialVOS.forEach(val -> {
            Trial trial = trialService.selectOne(val.getTrialId());
            val.setStatus(trial.getStatus());
            if (!trial.getStatus().equals(TrialStatusEnum.TO_RUN.getVal())) {
                val.setRunTime(TimeCalculateUtil.getRunTime(trial.getEndTime(), trial.getStartTime()));
            }
        });
        return trialVOS;
    }

    @Override
    public Integer updateExperimentStage(LambdaUpdateWrapper<ExperimentStage> wrapper) {
        return experimentStageMapper.update(null, wrapper);
    }

    @Override
    public Integer insertExperimentStageList(List<ExperimentStage> experimentStageList) {

        return experimentStageMapper.insertExperimentStageList(experimentStageList);
    }

    /**
     * 修改当前阶段运行参数
     * 已终止，终止中，已完成不可以修改
     */
    public void prohibitEdit(Long experimentId, ExperimentStage experimentStage) {
        //判断实验状态是否可以修改
        Experiment experiment = experimentService.selectById(experimentId);
        if (prohibitEdit.contains(experiment.getStatus()) &&
                (
                        !ExperimentStageStateEnum.TO_RUN_EXPERIMENT_STAGE_STATE.getCode().equals(experimentStage.getStatus()) ||
                                !ExperimentStageStateEnum.RUNNING_EXPERIMENT_STAGE_STATE.getCode().equals(experimentStage.getStatus())
                )
        ) {
            LogUtil.error(LogEnum.TADL,TadlConstant.PROCESS_STAGE_KEYWORD_LOG+"当前实验状态不可编辑.实验阶段状态:{}",experimentStage.getExperimentId(),
                    experimentStage.getId(),ExperimentStageStateEnum.getState(experimentStage.getStatus()).getDescription());
            throw new BusinessException(TadlErrorEnum.RUNTIME_PARAM_UPDATE_ERROR);
        }
    }

    /**
     * 修改最大运行时间
     *
     * 最长持续时间：
     *      1. 实验最长持续时间不可少于当前已运行时间且运行时间单位为系统有效单位
     *
     * @param maxExecDurationUpdateDTO     最大执行时间
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMaxExecDuration(MaxExecDurationUpdateDTO maxExecDurationUpdateDTO) {
        Experiment experiment = experimentService.selectById(maxExecDurationUpdateDTO.getExperimentId());
        if (ObjectUtils.isEmpty(experiment)) {
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_DOES_NOT_EXIST_ERROR);
        }
        ExperimentStage experimentStage = selectOne(maxExecDurationUpdateDTO.getExperimentId(), maxExecDurationUpdateDTO.getStageOrder());
        prohibitEdit(maxExecDurationUpdateDTO.getExperimentId(), experimentStage);
        Long runTime = getRunTime(experimentStage);
        if (runTime >= TimeCalculateUtil.getTime(maxExecDurationUpdateDTO.getMaxExecDurationUnit(), maxExecDurationUpdateDTO.getMaxExecDuration())) {
            throw new BusinessException(TadlErrorEnum.UPDATE_MAX_EXEC_DURATION_ERROR);
        }
        //构建更新条件
        LambdaUpdateWrapper<ExperimentStage> lambdaUpdateWrapper = new LambdaUpdateWrapper<ExperimentStage>() {{
            eq(ExperimentStage::getExperimentId, maxExecDurationUpdateDTO.getExperimentId());
            eq(ExperimentStage::getStageOrder, maxExecDurationUpdateDTO.getStageOrder());
            set(ExperimentStage::getMaxExecDuration, maxExecDurationUpdateDTO.getMaxExecDuration());
            set(ExperimentStage::getMaxExecDurationUnit, maxExecDurationUpdateDTO.getMaxExecDurationUnit());
        }};
        update(lambdaUpdateWrapper);
        ExperimentStageDetailUpdateDTO experimentStageDetailUpdateDTO = new ExperimentStageDetailUpdateDTO();
        try {
            BeanUtils.copyProperties(experimentStageDetailUpdateDTO, maxExecDurationUpdateDTO);
        } catch (Exception e) {
            LogUtil.error(LogEnum.TADL, "The experiment id:{},stage order:{}.Abnormal data conversion during the test phase!Error message：{}", maxExecDurationUpdateDTO.getExperimentId(), maxExecDurationUpdateDTO.getStageOrder(), e.getMessage());
            throw new BusinessException("实验阶段数据转换异常!");
        }
        updateStageYaml(maxExecDurationUpdateDTO.getExperimentId(),maxExecDurationUpdateDTO.getStageOrder(),experimentStageDetailUpdateDTO);

    }

    /**
     * 当前阶段运行参数变更内容更新到mino上的yaml文件
     * @param experimentId 实验id
     * @param stageOrder 算法阶段id
     * @param experimentStageDetailUpdateDTO 更新内容
     */
    private void updateStageYaml(Long experimentId,Integer stageOrder,ExperimentStageDetailUpdateDTO experimentStageDetailUpdateDTO) {
        try {
            String yamlString = minioUtil.readString(
                    bucketName,
                    pathUtil.getExperimentYamlPath(StringUtils.EMPTY, experimentId) + StageEnum.getStageName(stageOrder) + TadlConstant.ALGORITHM_CONFIGURATION_FILE_SUFFIX
            );
            HashMap<String, Object> yamlMap = YamlParseUtil.YamlParse(yamlString);
            if (Objects.isNull(yamlMap)){
                LogUtil.error(LogEnum.TADL,"The experiment id:{},stage order:{}.Yaml file content is empty!",experimentId,stageOrder);
                throw new BusinessException("yaml文件内容为空");
            }
            if (!Objects.isNull(experimentStageDetailUpdateDTO.getMaxExecDurationUnit())){
                String maxExecDuration =experimentStageDetailUpdateDTO.getMaxExecDuration()+experimentStageDetailUpdateDTO.getMaxExecDurationUnit();
                yamlMap.put(StrUtil.toUnderlineCase("maxExecDuration"),maxExecDuration);
            }else{
                Field[] declaredFields = experimentStageDetailUpdateDTO.getClass().getDeclaredFields();
                for (Field field:declaredFields){
                    // 私有属性必须设置访问权限
                    field.setAccessible(true);
                    String fieldName = StrUtil.toUnderlineCase(field.getName());
                    if (!Objects.isNull(field.get(experimentStageDetailUpdateDTO))){
                        yamlMap.put(fieldName, field.get(experimentStageDetailUpdateDTO));
                    }
                }
            }
            //写算法 yaml 文件到 minio
            minioUtil.writeString(
                    bucketName,
                    pathUtil.getExperimentYamlPath(StringUtils.EMPTY, experimentId)
                            .replaceFirst(TadlConstant.MODULE_URL_PREFIX, StringUtils.EMPTY) + StageEnum.getStageName(stageOrder) + TadlConstant.ALGORITHM_CONFIGURATION_FILE_SUFFIX,
                    new Yaml().dumpAsMap(yamlMap)
            );
        } catch (Exception e) {
            LogUtil.error(LogEnum.TADL,"File operation abnormal.Error message:{}",e.getMessage());
            throw new BusinessException(TadlErrorEnum.FILE_OPERATION_ERROR);
        }
    }

    /**
     * 修改最大 trial 数
     *
     * 最大 trial 数
     *      1. 实验最大trial数不可少于已完成数量（判断标准？）
     *
     * @param maxTrialNumUpdateDTO  最大trial数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMaxTrialNum(MaxTrialNumUpdateDTO maxTrialNumUpdateDTO) {
        //校验实验是否存在
        Experiment experiment = experimentService.selectById(maxTrialNumUpdateDTO.getExperimentId());
        if (ObjectUtils.isEmpty(experiment)) {
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_DOES_NOT_EXIST_ERROR);
        }
        ExperimentStage experimentStage = selectOne(maxTrialNumUpdateDTO.getExperimentId(), maxTrialNumUpdateDTO.getStageOrder());
        //校验当前阶段状态是否能够修改最大trial数
        prohibitEdit(maxTrialNumUpdateDTO.getExperimentId(), experimentStage);
        //查询已完成的 运行中的，已失败的，未知异常,等待中状态的等状态的 trial数量
        LambdaQueryWrapper<Trial> queryWrapper = new LambdaQueryWrapper<Trial>() {{
            eq(Trial::getExperimentId, maxTrialNumUpdateDTO.getExperimentId());
            eq(Trial::getStageId, experimentStage.getId());
            eq(Trial::getDeleted, NumberConstant.NUMBER_0);
            ne(Trial::getStatus,TrialStatusEnum.TO_RUN.getVal());
        }};
        Integer before = trialService.selectCount(queryWrapper);
        //修改的最大 trial 数需要不小于上述trial数量
        if (maxTrialNumUpdateDTO.getMaxTrialNum()< before ) {
            throw new BusinessException(TadlErrorEnum.UPDATE_MAX_TRIAL_NUM_ERROR);
        }

        //查询实验阶段的trial数量
        LambdaQueryWrapper<Trial> querySum = new LambdaQueryWrapper<Trial>() {{
            eq(Trial::getExperimentId, maxTrialNumUpdateDTO.getExperimentId());
            eq(Trial::getStageId, experimentStage.getId());
            eq(Trial::getDeleted, NumberConstant.NUMBER_0);
        }};
        Integer sum = trialService.selectCount(querySum);
        RLock lock = redissonClient.getLock(TadlConstant.LOCK + SymbolConstant.COLON+ maxTrialNumUpdateDTO.getExperimentId()+SymbolConstant.COLON + maxTrialNumUpdateDTO.getStageOrder());
        try{
            lock.lock(10, TimeUnit.SECONDS);
            //校验当前阶段状态是否能够修改最大trial数
            prohibitEdit(maxTrialNumUpdateDTO.getExperimentId(), selectOne(maxTrialNumUpdateDTO.getExperimentId(), maxTrialNumUpdateDTO.getStageOrder()));
            //判断需要增减 trial
            if (maxTrialNumUpdateDTO.getMaxTrialNum() > sum) {
                increaseTrials(maxTrialNumUpdateDTO, experimentStage, sum);
            } else {
                reduceTrials(maxTrialNumUpdateDTO, experimentStage, sum);
            }
        }catch(BusinessException businessException){
            throw businessException;
        }catch(Exception e){
            LogUtil.error(LogEnum.TADL,TadlConstant.PROCESS_STAGE_KEYWORD_LOG+"获取分布式锁失败:{}",experimentStage.getExperimentId(),experimentStage.getId(),e.getMessage());
            throw new BusinessException("获取分布式锁失败");
        }finally {
            lock.unlock();
        }


        //构建更新条件
        LambdaUpdateWrapper<ExperimentStage> lambdaUpdateWrapper = new LambdaUpdateWrapper<ExperimentStage>() {{
            eq(ExperimentStage::getExperimentId, maxTrialNumUpdateDTO.getExperimentId());
            eq(ExperimentStage::getStageOrder, maxTrialNumUpdateDTO.getStageOrder());
            set(ExperimentStage::getMaxTrialNum, maxTrialNumUpdateDTO.getMaxTrialNum());
        }};
        update(lambdaUpdateWrapper);
        //查询已完成的 trial 数量
        Integer after = trialService.selectCount(queryWrapper);
        if (after > maxTrialNumUpdateDTO.getMaxTrialNum()) {
            LogUtil.error(LogEnum.TADL, "The experiment id:{},stage order:{}.The maximum number of trials cannot be less than the number of trials currently completed", maxTrialNumUpdateDTO.getExperimentId(), maxTrialNumUpdateDTO.getStageOrder());
            throw new BusinessException(TadlErrorEnum.UPDATE_MAX_TRIAL_NUM_ERROR);
        }
        ExperimentStageDetailUpdateDTO experimentStageDetailUpdateDTO = new ExperimentStageDetailUpdateDTO();
        try {
            BeanUtils.copyProperties(experimentStageDetailUpdateDTO, maxTrialNumUpdateDTO);
        } catch (Exception e) {
            LogUtil.error(LogEnum.TADL, "The experiment id:{},stage order:{}.Abnormal data conversion during the test phase!Error message：{}", maxTrialNumUpdateDTO.getExperimentId(), maxTrialNumUpdateDTO.getStageOrder(), e.getMessage());
            throw new BusinessException("实验阶段数据转换异常!");
        }
        updateStageYaml(maxTrialNumUpdateDTO.getExperimentId(),maxTrialNumUpdateDTO.getStageOrder(),experimentStageDetailUpdateDTO);

    }

    /**
     * 减少trial数量
     * @param maxTrialNumUpdateDTO 修改参数
     * @param experimentStage 实验阶段
     * @param sum 已有的trial数量
     */
    private void reduceTrials(MaxTrialNumUpdateDTO maxTrialNumUpdateDTO, ExperimentStage experimentStage, Integer sum) {
        //trial ToRun
        LambdaQueryWrapper<Trial> queryToRun = new LambdaQueryWrapper<Trial>() {{
            eq(Trial::getExperimentId, maxTrialNumUpdateDTO.getExperimentId());
            eq(Trial::getStageId, experimentStage.getId());
            eq(Trial::getDeleted, NumberConstant.NUMBER_0);
            eq(Trial::getStatus, TrialStatusEnum.TO_RUN.getVal());
        }};
        List<Long> toRun = trialService.getTrialList(queryToRun).stream().sorted(Comparator.comparing(Trial::getSequence).reversed()).map(Trial::getId).collect(Collectors.toList());
        List<Long> deleteTrial = toRun.subList(NumberConstant.NUMBER_0, sum - maxTrialNumUpdateDTO.getMaxTrialNum());
        //删除多余的trial
        LambdaQueryWrapper<Trial> updateWrapper = new LambdaQueryWrapper<Trial>() {{
            eq(Trial::getDeleted, NumberConstant.NUMBER_0);
            in(Trial::getId, deleteTrial);
            eq(Trial::getStatus, TrialStatusEnum.TO_RUN.getVal());

        }};
        trialService.delete(updateWrapper);
        trialDataService.delete(new LambdaQueryWrapper<TrialData>() {{
            eq(TrialData::getDeleted, NumberConstant.NUMBER_0);
            in(TrialData::getTrialId, deleteTrial);
        }});
    }

    /**
     * 增加trial数量
     * @param maxTrialNumUpdateDTO 修改参数
     * @param experimentStage 实验阶段
     * @param sum 已有的trial数量
     */
    private void increaseTrials(MaxTrialNumUpdateDTO maxTrialNumUpdateDTO, ExperimentStage experimentStage, Integer sum) {
        List<Trial> trials = new ArrayList<Trial>() {{
            for (int i = sum + 1; i <= maxTrialNumUpdateDTO.getMaxTrialNum(); i++) {
                Trial trial = new Trial();
                trial.setExperimentId(maxTrialNumUpdateDTO.getExperimentId());
                //写实验阶段ID
                trial.setStageId(experimentStage.getId());
                trial.setStatus(TrialStatusEnum.TO_RUN.getVal());
                //trialName = 实验ID + stageID + stageOrder + trialSequence
                trial.setName(maxTrialNumUpdateDTO.getExperimentId() + RedisKeyConstant.COLON + experimentStage.getId() + RedisKeyConstant.COLON + experimentStage.getStageOrder() + RedisKeyConstant.COLON + i);
                trial.setSequence(i);
                add(trial);
            }
        }};
        trialService.insertList(trials);
        List<TrialData> trialDataList = TrialData.from(trials);
        trialDataService.insertList(trialDataList);
    }

    /**
     * 修改 trial 并发数
     *
     * trial 并发数
     *      1. trial 并发数不能大于当前 trial 总数 ，如果是单一的 trial 则不能修改
     *
     * @param trialConcurrentNumUpdateDTO    最大并发数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTrialConcurrentNum(TrialConcurrentNumUpdateDTO trialConcurrentNumUpdateDTO) {
        Experiment experiment = experimentService.selectById(trialConcurrentNumUpdateDTO.getExperimentId());
        if (ObjectUtils.isEmpty(experiment)) {
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_DOES_NOT_EXIST_ERROR);
        }
        ExperimentStage experimentStage = selectOne(trialConcurrentNumUpdateDTO.getExperimentId(), trialConcurrentNumUpdateDTO.getStageOrder());
        prohibitEdit(trialConcurrentNumUpdateDTO.getExperimentId(), experimentStage);
        //查询 trial 数量
        LambdaQueryWrapper<Trial> queryWrapper = new LambdaQueryWrapper<Trial>() {{
            eq(Trial::getExperimentId, trialConcurrentNumUpdateDTO.getExperimentId());
            eq(Trial::getDeleted, NumberConstant.NUMBER_0);
            eq(Trial::getStageId, experimentStage.getId());
        }};
        Integer count = trialService.selectCount(queryWrapper);
        if (count < trialConcurrentNumUpdateDTO.getTrialConcurrentNum()) {
            LogUtil.error(LogEnum.TADL, "The experiment id:{},stage order:{}.The maximum number of concurrent sessions cannot be greater than the total number of trials", trialConcurrentNumUpdateDTO.getExperimentId(), trialConcurrentNumUpdateDTO.getStageOrder());
            throw new BusinessException(TadlErrorEnum.UPDATE_TRIAL_CONCURRENT_NUM_ERROR);
        }
        //构建更新条件
        LambdaUpdateWrapper<ExperimentStage> lambdaUpdateWrapper = new LambdaUpdateWrapper<ExperimentStage>() {{
            eq(ExperimentStage::getExperimentId, trialConcurrentNumUpdateDTO.getExperimentId());
            eq(ExperimentStage::getStageOrder, trialConcurrentNumUpdateDTO.getStageOrder());
            set(ExperimentStage::getTrialConcurrentNum, trialConcurrentNumUpdateDTO.getTrialConcurrentNum());
        }};
        update(lambdaUpdateWrapper);
        ExperimentStageDetailUpdateDTO experimentStageDetailUpdateDTO = new ExperimentStageDetailUpdateDTO();
        try {
            BeanUtils.copyProperties(experimentStageDetailUpdateDTO, trialConcurrentNumUpdateDTO);
        } catch (Exception e) {
            LogUtil.error(LogEnum.TADL, "The experiment id:{},stage order:{}.Abnormal data conversion during the test phase!Error message：{}", trialConcurrentNumUpdateDTO.getExperimentId(), trialConcurrentNumUpdateDTO.getStageOrder(), e.getMessage());
            throw new BusinessException("实验阶段数据转换异常!");
        }
        updateStageYaml(trialConcurrentNumUpdateDTO.getExperimentId(),trialConcurrentNumUpdateDTO.getStageOrder(),experimentStageDetailUpdateDTO);

    }

    @Override
    public void saveExpiredTimeToRedis(Long experimentId, Long experimentStageId) {
        ExperimentStage experimentStage = experimentStageMapper.getExperimentStateByExperimentIdAndStageId(experimentId, experimentStageId);
        if (Objects.isNull(experimentStage)) {
            LogUtil.error(LogEnum.TADL, "根据实验id:{}和实验阶段id:{}未能找到对应实验数据");
            return;
        }
        if (Objects.isNull(experimentStage.getMaxExecDuration()) || Objects.isNull(experimentStage.getMaxExecDurationUnit())) {
            LogUtil.info(LogEnum.TADL, "未设置过期时间");
            return;
        }

        //获取过期时间，转化为毫秒
        Long expiredTimeOfMS = TimeCalculateUtil.getTime(experimentStage.getMaxExecDurationUnit(), experimentStage.getMaxExecDuration());

        //获取数据库中保存的暂停前的已运行时间
        Long runTime = Objects.isNull(experimentStage.getRunTime()) ? Long.valueOf(0) : experimentStage.getRunTime();
        //获取当前时间戳，加上设置的过期时间，再减去数据库中保存的已运行时间，得到过期的时间戳
        Long now = System.currentTimeMillis();
        Long expiredTime = now + expiredTimeOfMS - runTime;
        LogUtil.info(LogEnum.TADL, "过期时间戳为：当前时间：{} + 过期时间：{} - 已运行时间：{} = {}", now, expiredTimeOfMS, runTime, expiredTime);

        //存入redis的zset中,score为过期时间，member为experimentStageId_stage_id
        redisUtils.zAdd(RedisKeyConstant.EXPERIMENT_STAGE_EXPIRED_TIME_SET, expiredTime, experimentId + RedisKeyConstant.COLON + experimentStageId);
    }

    @Override
    public void pauseExpiredTimeToRedis(Long experimentId, Long experimentStageId) {
        ExperimentStage experimentStage = experimentStageMapper.getExperimentStateByExperimentIdAndStageId(experimentId, experimentStageId);
        if (Objects.isNull(experimentStage)) {
            LogUtil.error(LogEnum.TADL, "根据实验id:{}和实验阶段id:{}未能找到对应实验数据", experimentId, experimentStageId);
            return;
        }

        //设置成2099年
        long pauseTime;
        try {
            pauseTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS").parse("2099/01/01 11:11:11:111").getTime();
        } catch (ParseException e) {
            LogUtil.error(LogEnum.TADL, "日期转化错误,实验id:{},实验阶段id:{}", experimentId, experimentStageId);
            throw new BusinessException("日期转化错误");
        }

        //更新至zset中
        redisUtils.zAdd(RedisKeyConstant.EXPERIMENT_STAGE_EXPIRED_TIME_SET, pauseTime, experimentId + RedisKeyConstant.COLON + experimentStageId);
    }

    @Override
    public void removeExpiredTimeToRedis(Long experimentId, Long experimentStageId) {
        ExperimentStage experimentStage = experimentStageMapper.getExperimentStateByExperimentIdAndStageId(experimentId, experimentStageId);
        if (Objects.isNull(experimentStage)) {
            LogUtil.error(LogEnum.TADL, "根据实验id:{}和实验阶段id:{}未能找到对应实验数据");
            return;
        }

        //从zset中删除元素
        redisUtils.zRem(RedisKeyConstant.EXPERIMENT_STAGE_EXPIRED_TIME_SET, experimentId + RedisKeyConstant.COLON + experimentStageId);
    }
}
