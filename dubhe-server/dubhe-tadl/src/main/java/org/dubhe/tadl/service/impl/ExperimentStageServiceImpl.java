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
 * @description ?????????????????????????????????
 * @date 2021-03-22
 */
@Service
public class ExperimentStageServiceImpl extends ServiceImpl<ExperimentStageMapper, ExperimentStage> implements ExperimentStageService {


    /**
     * ???????????????
     */
    @Resource
    private PathUtil pathUtil;

    /**
     * MinIO?????????
     */
    @Resource
    private MinioUtil minioUtil;

    /**
     * minIO??????
     */
    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * ???????????? mapper
     */
    @Resource
    private ExperimentStageMapper experimentStageMapper;

    /**
     * trial ????????????
     */
    @Resource
    private TadlTrialService trialService;

    /**
     * trial data ????????????
     */
    @Resource
    private TrialDataService trialDataService;

    /**
     * ??????????????????
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
     * ??????????????????????????????
     *
     * @param experimentStageId ????????????id
     * @return ??????????????????
     */
    @Override
    public ExperimentStage selectById(Long experimentStageId) {
        return baseMapper.selectById(experimentStageId);
    }

    /**
     * ????????????ID??????????????????????????????
     *
     * @param experimentId ??????id
     * @return List<Integer>    ????????????????????????
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
     * ????????????id????????????????????????
     *
     * @param experimentId ??????id
     * @return ??????????????????
     */
    @Override
    public List<ExperimentStage> getExperimentStageListByExperimentId(Long experimentId) {
        return baseMapper.selectList(new LambdaQueryWrapper<ExperimentStage>().eq(ExperimentStage::getExperimentId, experimentId));
    }

    /**
     * ?????? experimentStage ??????????????????????????????????????????
     *
     * @param wrapper ????????????
     * @return ??????????????????
     */
    @Override
    public List<ExperimentStage> getExperimentStageList(LambdaQueryWrapper<ExperimentStage> wrapper) {
        return baseMapper.selectList(wrapper);
    }

    /**
     * ????????????id??????????????????????????????
     * @return ??????????????????
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
     * ??????????????????
     *
     * @param experimentStage ????????????
     */
    @Override
    public void insert(ExperimentStage experimentStage) {
        baseMapper.insert(experimentStage);
    }

    /**
     * ???????????? id ??????????????????
     *
     * @param experimentStage ????????????
     */
    @Override
    public void updateExperimentStageById(ExperimentStage experimentStage) {
        baseMapper.updateById(experimentStage);
    }

    /**
     * ??????????????????
     *
     * @param experimentId ??????ID
     * @param stageOrder   ????????????
     * @return ????????????
     */
    @Override
    public StageOutlineVO query(Long experimentId, Integer stageOrder) {
        // ????????????ID
        ExperimentStage experimentStage = selectOne(experimentId, stageOrder);
        // ??????????????????trial??????
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
            // ???????????????????????????
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
                // ?????????????????????
                setSum(count);
                setBestData(trialService.getBestData(experimentId, experimentStage.getId()));
            }
        }};
    }

    /**
     * ????????????????????????
     *
     * @param experimentId ??????ID
     * @param stageOrder   ??????ID
     * @return ??????????????????VO
     */
    @Override
    public ExperimentStageParamVO queryStageParam(Long experimentId, Integer stageOrder) {
        // ????????????????????????
        ExperimentStage experimentStage = selectOne(experimentId, stageOrder);
        // ????????????????????????
        AlgorithmStage algorithmStage = algorithmStageService.getOneById(experimentStage.getAlgorithmStageId());
        // ??????????????????
        long stageRunTime = experimentStage.getRunTime() == null ? 0 : experimentStage.getRunTime();
        // ???????????????????????????????????????now-beginTime???????????????????????????runTime
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
     * ??????????????????????????????
     *
     * @param experimentId ??????ID
     * @param stageOrder   ??????ID
     * @return ??????????????????VO
     */
    @Override
    public RuntimeParamVO queryRuntimeParam(Long experimentId, Integer stageOrder) {
        // ????????????????????????
        ExperimentStage experimentStage = selectOne(experimentId, stageOrder);
        // ????????????????????????
        Integer trialNum = trialMapper.getTrialCountOfStatus(experimentId, experimentStage.getId(), TrialStatusEnum.FINISHED.getVal());
        // ??????????????????
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
        //???????????????????????????????????????now-updateTime???????????????????????????runTime
        if (ExperimentStageStateEnum.RUNNING_EXPERIMENT_STAGE_STATE.getCode().equals(experimentStage.getStatus())) {
            runTime = System.currentTimeMillis() - experimentStage.getBeginTime().getTime() + runTime;
        }
        return runTime;
    }

    /**
     * ??????????????????????????????
     *
     * @param experimentId ??????ID
     * @param stageOrder   ??????ID
     * @return ??????????????????
     */
    @Override
    public ExperimentStage selectOne(Long experimentId, Integer stageOrder) {
        ExperimentStage experimentStage = baseMapper.selectOne(new LambdaQueryWrapper<ExperimentStage>() {{
            eq(ExperimentStage::getExperimentId, experimentId);
            eq(ExperimentStage::getStageOrder, stageOrder);
        }});
        if (Objects.isNull(experimentStage)) {
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG+" Experimental stage is null", experimentId);
            throw new BusinessException("?????????????????????");
        }
        return experimentStage;
    }

    /**
     * ???????????????????????? yaml ??????
     *
     * @param experimentId ??????ID
     * @param stageOrder   ??????ID
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
     * ??????????????????yaml
     *
     * @param updateStageYamlDTO ??????yaml DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfiguration(UpdateStageYamlDTO updateStageYamlDTO) {
        Experiment experiment = experimentService.selectById(updateStageYamlDTO.getExperimentId());
        ExperimentStage experimentStage = selectOne(updateStageYamlDTO.getExperimentId(), updateStageYamlDTO.getStageOrder());
        if (ObjectUtils.isEmpty(experimentStage) || ObjectUtils.isEmpty(experiment)) {
            LogUtil.error(LogEnum.TADL, "???????????????. ??????id:{}", updateStageYamlDTO.getExperimentId());
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_DOES_NOT_EXIST_ERROR);
        }
        //????????????????????????????????????????????????????????????yaml
        if (prohibitEdit.contains(experiment.getStatus())) {
            LogUtil.error(LogEnum.TADL, "??????????????????????????????.experiment id:{},experiment status :{}", experiment.getId(), experiment.getStatus());
            throw new BusinessException(TadlErrorEnum.RUNTIME_PARAM_UPDATE_ERROR);
        }
        //?????????????????????????????????????????????????????????????????????yaml????????????
        if (!experimentStage.getStatus().equals(ExperimentStageStateEnum.TO_RUN_EXPERIMENT_STAGE_STATE.getCode())) {
            LogUtil.error(LogEnum.TADL, "?????????????????????????????????.stage id:{},experiment status :{}", experimentStage.getId(), experimentStage.getStatus());
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_UPDATE_ERROR);
        }
        //???????????????????????? ??????trial??????,?????????????????????trail????????????
        updateStageDetail(updateStageYamlDTO);
        try {
            //????????? yaml ????????? minio
            minioUtil.writeString(
                    bucketName,
                    pathUtil.getExperimentYamlPath(StringUtils.EMPTY, updateStageYamlDTO.getExperimentId())
                            .replaceFirst(TadlConstant.MODULE_URL_PREFIX, StringUtils.EMPTY) + StageEnum.getStageName(updateStageYamlDTO.getStageOrder()) + TadlConstant.ALGORITHM_CONFIGURATION_FILE_SUFFIX,
                    updateStageYamlDTO.getYaml()
            );
        } catch (Exception e) {
            LogUtil.error(LogEnum.TADL, "??????????????????yaml??????. ????????????:{}", e.getMessage());
            throw new BusinessException(TadlErrorEnum.FILE_OPERATION_ERROR);
        }
    }

    /**
     * ??????yaml???????????????????????????????????????
     * @param updateStageYamlDTO ??????yaml DTO
     */
    public void updateStageDetail(UpdateStageYamlDTO updateStageYamlDTO) {

        HashMap<String, Object> readerMap = YamlParseUtil.YamlParse(updateStageYamlDTO.getYaml());

        JSONObject updateStageJson = getUpdateStageJson(readerMap);

        //??????yaml????????????????????????????????????????????????
        String maxExecDurationAndUnit = (String) updateStageJson.get("maxExecDuration");
        //???????????????????????????????????????
        if (Objects.isNull(maxExecDurationAndUnit)) {
            LogUtil.error(LogEnum.TADL, "The experiment id:{},stage order:{}.Maximum running time cannot be empty!", updateStageYamlDTO.getExperimentId(), updateStageYamlDTO.getStageOrder());
            throw new BusinessException("??????????????????????????????????????????");
        }
        String maxExeDuration ;
        Matcher matcher = StringConstant.PATTERN_DECIMAL.matcher(maxExecDurationAndUnit);
        //matcher.find??????????????????????????????????????????"(\\d+\\.\\d+)"??????????????????
        if (matcher.find()) {
            //group()???????????????0???????????????????????????1????????????????????????????????????,2???????????????????????????,????????????????????????,???1???0????????????
            maxExeDuration = matcher.group(NumberConstant.NUMBER_1);
        } else {
            //????????????????????????????????????????????????
            matcher = StringConstant.PATTERN_NUMBER.matcher(maxExecDurationAndUnit);
            //????????????????????????
            maxExeDuration =matcher.find()? matcher.group(NumberConstant.NUMBER_1):"";
        }
        //????????????????????????
        if (StringUtils.isBlank(maxExeDuration)){
            throw new BusinessException("??????????????????????????????????????????");
        }
        updateStageJson.put("maxExecDuration", maxExeDuration);

        String maxExecDurationUnit = maxExecDurationAndUnit.replaceAll(maxExeDuration, "");
        //??????????????????
        if (!TimeUnitEnum.isValid(maxExecDurationUnit)) {
            LogUtil.error(LogEnum.TADL, "The experiment id:{},stage order:{}.The maximum running time unit in the yaml configuration file is abnormal.", updateStageYamlDTO.getExperimentId(), updateStageYamlDTO.getStageOrder());
            throw new BusinessException("??????????????????????????????????????????");
        }
        updateStageJson.put("maxExecDurationUnit", maxExecDurationUnit);
        ExperimentStageDetailUpdateDTO experimentStageDetailUpdateDTO = new ExperimentStageDetailUpdateDTO();
        try {
            BeanUtils.populate(experimentStageDetailUpdateDTO, updateStageJson);
        } catch (Exception e) {
            LogUtil.error(LogEnum.TADL, "The experiment id:{},stage order:{}.Abnormal data conversion during the test phase!Error message???{}", updateStageYamlDTO.getExperimentId(), updateStageYamlDTO.getStageOrder(), e.getMessage());
            throw new BusinessException("??????????????????????????????!");
        }
        //??????????????????????????????
        validatorCheck(experimentStageDetailUpdateDTO);
        //?????? trail???????????? ????????????????????????????????????trial?????? ??????
        if (experimentStageDetailUpdateDTO.getTrialConcurrentNum() > experimentStageDetailUpdateDTO.getMaxTrialNum()) {
            LogUtil.error(LogEnum.TADL, "The experiment id:{},stage order:{}.The maximum number of concurrent sessions cannot be greater than the total number of trials", updateStageYamlDTO.getExperimentId(), updateStageYamlDTO.getStageOrder());
            throw new BusinessException(TadlErrorEnum.UPDATE_TRIAL_CONCURRENT_NUM_ERROR);
        }

        //??????????????????????????????
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
     * validator ????????????????????????
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
     * ??????yaml????????????????????????????????????
     * @param readerMap yaml????????????
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
     * ??????????????????????????????
     * ????????????????????????????????????????????????
     */
    public void prohibitEdit(Long experimentId, ExperimentStage experimentStage) {
        //????????????????????????????????????
        Experiment experiment = experimentService.selectById(experimentId);
        if (prohibitEdit.contains(experiment.getStatus()) &&
                (
                        !ExperimentStageStateEnum.TO_RUN_EXPERIMENT_STAGE_STATE.getCode().equals(experimentStage.getStatus()) ||
                                !ExperimentStageStateEnum.RUNNING_EXPERIMENT_STAGE_STATE.getCode().equals(experimentStage.getStatus())
                )
        ) {
            LogUtil.error(LogEnum.TADL,TadlConstant.PROCESS_STAGE_KEYWORD_LOG+"??????????????????????????????.??????????????????:{}",experimentStage.getExperimentId(),
                    experimentStage.getId(),ExperimentStageStateEnum.getState(experimentStage.getStatus()).getDescription());
            throw new BusinessException(TadlErrorEnum.RUNTIME_PARAM_UPDATE_ERROR);
        }
    }

    /**
     * ????????????????????????
     *
     * ?????????????????????
     *      1. ???????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param maxExecDurationUpdateDTO     ??????????????????
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
        //??????????????????
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
            LogUtil.error(LogEnum.TADL, "The experiment id:{},stage order:{}.Abnormal data conversion during the test phase!Error message???{}", maxExecDurationUpdateDTO.getExperimentId(), maxExecDurationUpdateDTO.getStageOrder(), e.getMessage());
            throw new BusinessException("??????????????????????????????!");
        }
        updateStageYaml(maxExecDurationUpdateDTO.getExperimentId(),maxExecDurationUpdateDTO.getStageOrder(),experimentStageDetailUpdateDTO);

    }

    /**
     * ?????????????????????????????????????????????mino??????yaml??????
     * @param experimentId ??????id
     * @param stageOrder ????????????id
     * @param experimentStageDetailUpdateDTO ????????????
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
                throw new BusinessException("yaml??????????????????");
            }
            if (!Objects.isNull(experimentStageDetailUpdateDTO.getMaxExecDurationUnit())){
                String maxExecDuration =experimentStageDetailUpdateDTO.getMaxExecDuration()+experimentStageDetailUpdateDTO.getMaxExecDurationUnit();
                yamlMap.put(StrUtil.toUnderlineCase("maxExecDuration"),maxExecDuration);
            }else{
                Field[] declaredFields = experimentStageDetailUpdateDTO.getClass().getDeclaredFields();
                for (Field field:declaredFields){
                    // ????????????????????????????????????
                    field.setAccessible(true);
                    String fieldName = StrUtil.toUnderlineCase(field.getName());
                    if (!Objects.isNull(field.get(experimentStageDetailUpdateDTO))){
                        yamlMap.put(fieldName, field.get(experimentStageDetailUpdateDTO));
                    }
                }
            }
            //????????? yaml ????????? minio
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
     * ???????????? trial ???
     *
     * ?????? trial ???
     *      1. ????????????trial???????????????????????????????????????????????????
     *
     * @param maxTrialNumUpdateDTO  ??????trial???
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMaxTrialNum(MaxTrialNumUpdateDTO maxTrialNumUpdateDTO) {
        //????????????????????????
        Experiment experiment = experimentService.selectById(maxTrialNumUpdateDTO.getExperimentId());
        if (ObjectUtils.isEmpty(experiment)) {
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_DOES_NOT_EXIST_ERROR);
        }
        ExperimentStage experimentStage = selectOne(maxTrialNumUpdateDTO.getExperimentId(), maxTrialNumUpdateDTO.getStageOrder());
        //????????????????????????????????????????????????trial???
        prohibitEdit(maxTrialNumUpdateDTO.getExperimentId(), experimentStage);
        //?????????????????? ??????????????????????????????????????????,?????????????????????????????? trial??????
        LambdaQueryWrapper<Trial> queryWrapper = new LambdaQueryWrapper<Trial>() {{
            eq(Trial::getExperimentId, maxTrialNumUpdateDTO.getExperimentId());
            eq(Trial::getStageId, experimentStage.getId());
            eq(Trial::getDeleted, NumberConstant.NUMBER_0);
            ne(Trial::getStatus,TrialStatusEnum.TO_RUN.getVal());
        }};
        Integer before = trialService.selectCount(queryWrapper);
        //??????????????? trial ????????????????????????trial??????
        if (maxTrialNumUpdateDTO.getMaxTrialNum()< before ) {
            throw new BusinessException(TadlErrorEnum.UPDATE_MAX_TRIAL_NUM_ERROR);
        }

        //?????????????????????trial??????
        LambdaQueryWrapper<Trial> querySum = new LambdaQueryWrapper<Trial>() {{
            eq(Trial::getExperimentId, maxTrialNumUpdateDTO.getExperimentId());
            eq(Trial::getStageId, experimentStage.getId());
            eq(Trial::getDeleted, NumberConstant.NUMBER_0);
        }};
        Integer sum = trialService.selectCount(querySum);
        RLock lock = redissonClient.getLock(TadlConstant.LOCK + SymbolConstant.COLON+ maxTrialNumUpdateDTO.getExperimentId()+SymbolConstant.COLON + maxTrialNumUpdateDTO.getStageOrder());
        try{
            lock.lock(10, TimeUnit.SECONDS);
            //????????????????????????????????????????????????trial???
            prohibitEdit(maxTrialNumUpdateDTO.getExperimentId(), selectOne(maxTrialNumUpdateDTO.getExperimentId(), maxTrialNumUpdateDTO.getStageOrder()));
            //?????????????????? trial
            if (maxTrialNumUpdateDTO.getMaxTrialNum() > sum) {
                increaseTrials(maxTrialNumUpdateDTO, experimentStage, sum);
            } else {
                reduceTrials(maxTrialNumUpdateDTO, experimentStage, sum);
            }
        }catch(BusinessException businessException){
            throw businessException;
        }catch(Exception e){
            LogUtil.error(LogEnum.TADL,TadlConstant.PROCESS_STAGE_KEYWORD_LOG+"????????????????????????:{}",experimentStage.getExperimentId(),experimentStage.getId(),e.getMessage());
            throw new BusinessException("????????????????????????");
        }finally {
            lock.unlock();
        }


        //??????????????????
        LambdaUpdateWrapper<ExperimentStage> lambdaUpdateWrapper = new LambdaUpdateWrapper<ExperimentStage>() {{
            eq(ExperimentStage::getExperimentId, maxTrialNumUpdateDTO.getExperimentId());
            eq(ExperimentStage::getStageOrder, maxTrialNumUpdateDTO.getStageOrder());
            set(ExperimentStage::getMaxTrialNum, maxTrialNumUpdateDTO.getMaxTrialNum());
        }};
        update(lambdaUpdateWrapper);
        //?????????????????? trial ??????
        Integer after = trialService.selectCount(queryWrapper);
        if (after > maxTrialNumUpdateDTO.getMaxTrialNum()) {
            LogUtil.error(LogEnum.TADL, "The experiment id:{},stage order:{}.The maximum number of trials cannot be less than the number of trials currently completed", maxTrialNumUpdateDTO.getExperimentId(), maxTrialNumUpdateDTO.getStageOrder());
            throw new BusinessException(TadlErrorEnum.UPDATE_MAX_TRIAL_NUM_ERROR);
        }
        ExperimentStageDetailUpdateDTO experimentStageDetailUpdateDTO = new ExperimentStageDetailUpdateDTO();
        try {
            BeanUtils.copyProperties(experimentStageDetailUpdateDTO, maxTrialNumUpdateDTO);
        } catch (Exception e) {
            LogUtil.error(LogEnum.TADL, "The experiment id:{},stage order:{}.Abnormal data conversion during the test phase!Error message???{}", maxTrialNumUpdateDTO.getExperimentId(), maxTrialNumUpdateDTO.getStageOrder(), e.getMessage());
            throw new BusinessException("??????????????????????????????!");
        }
        updateStageYaml(maxTrialNumUpdateDTO.getExperimentId(),maxTrialNumUpdateDTO.getStageOrder(),experimentStageDetailUpdateDTO);

    }

    /**
     * ??????trial??????
     * @param maxTrialNumUpdateDTO ????????????
     * @param experimentStage ????????????
     * @param sum ?????????trial??????
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
        //???????????????trial
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
     * ??????trial??????
     * @param maxTrialNumUpdateDTO ????????????
     * @param experimentStage ????????????
     * @param sum ?????????trial??????
     */
    private void increaseTrials(MaxTrialNumUpdateDTO maxTrialNumUpdateDTO, ExperimentStage experimentStage, Integer sum) {
        List<Trial> trials = new ArrayList<Trial>() {{
            for (int i = sum + 1; i <= maxTrialNumUpdateDTO.getMaxTrialNum(); i++) {
                Trial trial = new Trial();
                trial.setExperimentId(maxTrialNumUpdateDTO.getExperimentId());
                //???????????????ID
                trial.setStageId(experimentStage.getId());
                trial.setStatus(TrialStatusEnum.TO_RUN.getVal());
                //trialName = ??????ID + stageID + stageOrder + trialSequence
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
     * ?????? trial ?????????
     *
     * trial ?????????
     *      1. trial ??????????????????????????? trial ?????? ????????????????????? trial ???????????????
     *
     * @param trialConcurrentNumUpdateDTO    ???????????????
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
        //?????? trial ??????
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
        //??????????????????
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
            LogUtil.error(LogEnum.TADL, "The experiment id:{},stage order:{}.Abnormal data conversion during the test phase!Error message???{}", trialConcurrentNumUpdateDTO.getExperimentId(), trialConcurrentNumUpdateDTO.getStageOrder(), e.getMessage());
            throw new BusinessException("??????????????????????????????!");
        }
        updateStageYaml(trialConcurrentNumUpdateDTO.getExperimentId(),trialConcurrentNumUpdateDTO.getStageOrder(),experimentStageDetailUpdateDTO);

    }

    @Override
    public void saveExpiredTimeToRedis(Long experimentId, Long experimentStageId) {
        ExperimentStage experimentStage = experimentStageMapper.getExperimentStateByExperimentIdAndStageId(experimentId, experimentStageId);
        if (Objects.isNull(experimentStage)) {
            LogUtil.error(LogEnum.TADL, "????????????id:{}???????????????id:{}??????????????????????????????");
            return;
        }
        if (Objects.isNull(experimentStage.getMaxExecDuration()) || Objects.isNull(experimentStage.getMaxExecDurationUnit())) {
            LogUtil.info(LogEnum.TADL, "?????????????????????");
            return;
        }

        //????????????????????????????????????
        Long expiredTimeOfMS = TimeCalculateUtil.getTime(experimentStage.getMaxExecDurationUnit(), experimentStage.getMaxExecDuration());

        //??????????????????????????????????????????????????????
        Long runTime = Objects.isNull(experimentStage.getRunTime()) ? Long.valueOf(0) : experimentStage.getRunTime();
        //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        Long now = System.currentTimeMillis();
        Long expiredTime = now + expiredTimeOfMS - runTime;
        LogUtil.info(LogEnum.TADL, "????????????????????????????????????{} + ???????????????{} - ??????????????????{} = {}", now, expiredTimeOfMS, runTime, expiredTime);

        //??????redis???zset???,score??????????????????member???experimentStageId_stage_id
        redisUtils.zAdd(RedisKeyConstant.EXPERIMENT_STAGE_EXPIRED_TIME_SET, expiredTime, experimentId + RedisKeyConstant.COLON + experimentStageId);
    }

    @Override
    public void pauseExpiredTimeToRedis(Long experimentId, Long experimentStageId) {
        ExperimentStage experimentStage = experimentStageMapper.getExperimentStateByExperimentIdAndStageId(experimentId, experimentStageId);
        if (Objects.isNull(experimentStage)) {
            LogUtil.error(LogEnum.TADL, "????????????id:{}???????????????id:{}??????????????????????????????", experimentId, experimentStageId);
            return;
        }

        //?????????2099???
        long pauseTime;
        try {
            pauseTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS").parse("2099/01/01 11:11:11:111").getTime();
        } catch (ParseException e) {
            LogUtil.error(LogEnum.TADL, "??????????????????,??????id:{},????????????id:{}", experimentId, experimentStageId);
            throw new BusinessException("??????????????????");
        }

        //?????????zset???
        redisUtils.zAdd(RedisKeyConstant.EXPERIMENT_STAGE_EXPIRED_TIME_SET, pauseTime, experimentId + RedisKeyConstant.COLON + experimentStageId);
    }

    @Override
    public void removeExpiredTimeToRedis(Long experimentId, Long experimentStageId) {
        ExperimentStage experimentStage = experimentStageMapper.getExperimentStateByExperimentIdAndStageId(experimentId, experimentStageId);
        if (Objects.isNull(experimentStage)) {
            LogUtil.error(LogEnum.TADL, "????????????id:{}???????????????id:{}??????????????????????????????");
            return;
        }

        //???zset???????????????
        redisUtils.zRem(RedisKeyConstant.EXPERIMENT_STAGE_EXPIRED_TIME_SET, experimentId + RedisKeyConstant.COLON + experimentStageId);
    }
}
