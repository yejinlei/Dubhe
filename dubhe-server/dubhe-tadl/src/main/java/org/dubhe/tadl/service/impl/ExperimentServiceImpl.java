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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.dto.UserDTO;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.ReflectionUtils;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.vo.QueryResourceSpecsVO;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.db.utils.WrapperHelp;
import org.dubhe.biz.file.dto.FileDTO;
import org.dubhe.biz.file.utils.LocalFileUtil;
import org.dubhe.biz.file.utils.MinioUtil;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.permission.base.BaseService;
import org.dubhe.biz.redis.utils.RedisUtils;
import org.dubhe.biz.statemachine.dto.StateChangeDTO;
import org.dubhe.cloud.authconfig.service.AdminClient;
import org.dubhe.k8s.api.LogMonitoringApi;
import org.dubhe.k8s.domain.vo.LogMonitoringVO;
import org.dubhe.k8s.utils.K8sNameTool;
import org.dubhe.recycle.config.RecycleConfig;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.dto.RecycleDetailCreateDTO;
import org.dubhe.recycle.enums.RecycleModuleEnum;
import org.dubhe.recycle.enums.RecycleResourceEnum;
import org.dubhe.recycle.enums.RecycleTypeEnum;
import org.dubhe.recycle.service.RecycleService;
import org.dubhe.recycle.utils.RecycleTool;
import org.dubhe.tadl.client.AdminServiceClient;
import org.dubhe.tadl.constant.RedisKeyConstant;
import org.dubhe.tadl.constant.TadlConstant;
import org.dubhe.tadl.dao.ExperimentMapper;
import org.dubhe.tadl.dao.TrialMapper;
import org.dubhe.tadl.domain.dto.*;
import org.dubhe.tadl.domain.entity.*;
import org.dubhe.tadl.domain.vo.*;
import org.dubhe.tadl.enums.*;
import org.dubhe.tadl.machine.constant.ExperimentEventMachineConstant;
import org.dubhe.tadl.machine.constant.ExperimentStageEventMachineConstant;
import org.dubhe.tadl.machine.constant.TrialEventMachineConstant;
import org.dubhe.tadl.machine.utils.identify.StateMachineStatusUtil;
import org.dubhe.tadl.machine.utils.identify.StateMachineUtil;
import org.dubhe.tadl.service.*;
import org.dubhe.tadl.utils.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.io.File;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description ???????????????????????????
 * @date 2021-03-22
 */
@Service
public class ExperimentServiceImpl extends ServiceImpl<ExperimentMapper, Experiment> implements ExperimentService {

    /**
     * ??????????????????
     */
    @Resource
    private LocalFileUtil localFileUtil;

    /**
     * ????????????
     */
    @Resource
    private AdminClient adminClient;

    /**
     * trial ??????
     */
    @Resource
    private TadlTrialService trialService;

    /**
     * ??????????????????
     */
    @Resource
    private ExperimentStageService experimentStageService;

    /**
     * minIO??????
     */
    @Value("${minio.bucketName}")
    private String bucketName;

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
     * ????????????
     */
    @Resource
    private AlgorithmService algorithmService;

    /**
     * ??????????????????
     */
    @Resource
    private AlgorithmVersionService algorithmVersionService;

    /**
     * ??????????????????
     */
    @Resource
    private AlgorithmStageService algorithmStageService;

    @Resource
    private UserContextService userContextService;

    @Resource
    private CmdUtil cmdUtil;

    @Resource
    private AdminServiceClient adminServiceClient;

    @Resource
    private TrialDataService trialDataService;

    @Resource
    private TadlRedisService tadlRedisService;

    @Resource
    private K8sNameTool k8sNameTool;

    @Resource
    private TrialMapper trialMapper;

    @Resource
    private RecycleConfig recycleConfig;

    @Resource
    private RecycleService recycleService;

    @Resource
    private ExperimentMapper experimentMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private LogMonitoringApi logMonitoringApi;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private StateMachineStatusUtil stateMachineStatusUtil;


    private final static List<String> FILE_NAMES;

    static {
        FILE_NAMES = ReflectionUtils.getFieldNames(ExperimentQueryVO.class);
    }


    /**
     * ??????Experiment???????????????
     */
    private final Field[] fields = Experiment.class.getDeclaredFields();

    /**
     * ?????? id ??????????????????
     *
     * @param experimentId ??????id
     * @return ????????????
     */
    @Override
    public boolean queryEmpty(Long experimentId) {
        Integer count = baseMapper.selectCount(
                new LambdaQueryWrapper<Experiment>()
                        .eq(Experiment::getDeleted, MagicNumConstant.ZERO)
                        .eq(Experiment::getId, experimentId)
        );
        return count.equals(NumberConstant.NUMBER_1);
    }

    /**
     * ????????????????????????
     *
     * @param experimentId ??????id
     * @return ????????????
     */
    @Override
    public Experiment selectById(Long experimentId) {
        return baseMapper.selectById(experimentId);
    }

    @Override
    public Experiment selectOne(LambdaQueryWrapper<Experiment> queryWrapper) {
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * ??????????????????
     *
     * @param userId ??????id
     * @return ????????????
     */
    private String getUserName(Long userId) {
        if (ObjectUtils.isEmpty(userId)) {
            throw new BusinessException(TadlErrorEnum.DB_SEARCH_ERROR);
        }
        UserDTO userDTO = adminClient.getUsers(userId).getData();
        return userDTO == null ? null : userDTO.getNickName();
    }

    @Override
    public Map<String, Object> query(ExperimentQueryDTO experimentQueryDTO) {
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("???????????????????????????");
        }
        LogUtil.info(LogEnum.TADL, "User {} queried the experiment list with the query of {}", user.getUsername(), JSONObject.toJSONString(experimentQueryDTO));
        if (experimentQueryDTO.getCurrent() == null || experimentQueryDTO.getSize() == null) {
            throw new BusinessException(TadlErrorEnum.PARAM_ERROR);
        }
        String name = experimentQueryDTO.getName();
        //??????id???????????????
        if (StringUtils.isNotBlank(name)) {
            //????????????
            if (StringConstant.PATTERN_NUM.matcher(name).matches()) {
                experimentQueryDTO.setId(Long.parseLong(name));
                experimentQueryDTO.setName(null);
                Map<String, Object> map = queryExperiment(experimentQueryDTO, user);
                if (((List<ExperimentQueryVO>) map.get(StringConstant.RESULT)).size() > NumberConstant.NUMBER_0) {
                    return map;
                } else {
                    experimentQueryDTO.setId(null);
                    experimentQueryDTO.setName(name);
                }
            }
        }
        return queryExperiment(experimentQueryDTO, user);
    }

    public Map<String, Object> queryExperiment(ExperimentQueryDTO experimentQueryDTO, UserContext user) {
        QueryWrapper<Experiment> wrapper = WrapperHelp.getWrapper(experimentQueryDTO);
        //?????????????????????????????????
        if (!BaseService.isAdmin(user)) {
            wrapper.eq("create_user_id", user.getId());
        }
        Page page = new Page(null == experimentQueryDTO.getCurrent() ? NumberConstant.NUMBER_1 : experimentQueryDTO.getCurrent(),
                null == experimentQueryDTO.getSize() ? NumberConstant.NUMBER_10 : experimentQueryDTO.getSize());
        try {
            //??????????????????????????????????????????????????????????????????????????????
            String column = experimentQueryDTO.getSort() != null && FILE_NAMES.contains(experimentQueryDTO.getSort()) ? StringUtils.humpToLine(experimentQueryDTO.getSort()) : "update_time";
            //????????????
            boolean isAsc = StringUtils.isBlank(experimentQueryDTO.getOrder()) || StringUtils.equals(experimentQueryDTO.getOrder(), StringConstant.SORT_DESC) ? false : true;
            wrapper.orderBy(true, isAsc, column);
        } catch (Exception e) {
            LogUtil.error(LogEnum.TADL, "Query experiment list with an exception, query info:{}???exception info:{}", JSONObject.toJSONString(experimentQueryDTO), e);
            throw new BusinessException(TadlErrorEnum.DB_SEARCH_ERROR);
        }
        IPage<Experiment> experiments = baseMapper.selectPage(page, wrapper);

        //???????????????stage????????????map
        List<ExperimentStage> experimentStageList = experimentStageService.getStatusListSorted();
        Map<Long, List<ExperimentStage>> experimentStageMap = new HashMap<>();

        for (ExperimentStage experimentStage : experimentStageList) {
            if (CollectionUtils.isEmpty(experimentStageMap.get(experimentStage.getExperimentId()))) {
                List<ExperimentStage> stageList = new ArrayList<>();
                stageList.add(experimentStage);
                experimentStageMap.put(experimentStage.getExperimentId(), stageList);
            } else {
                experimentStageMap.get(experimentStage.getExperimentId()).add(experimentStage);
            }
        }

        List<Long> userIdList = new ArrayList<>();
        List<ExperimentQueryVO> queryVOList = experiments.getRecords().stream().map(experiment -> {
            ExperimentQueryVO queryVO = new ExperimentQueryVO();
            BeanUtils.copyProperties(experiment, queryVO);
            if (!userIdList.contains(experiment.getCreateUserId())) {
                userIdList.add(experiment.getCreateUserId());
            }
            //??????runTime
            queryVO.setRunTime(0L);

            if (!CollectionUtils.isEmpty(experimentStageMap.get(experiment.getId()))) {
                List<ExperimentQueryVO.StageVO> stageVOList = new ArrayList<>();
                experimentStageMap.get(experiment.getId()).forEach(experimentStage -> {
                    ExperimentQueryVO.StageVO stageVO = new ExperimentQueryVO.StageVO();
                    BeanUtils.copyProperties(experimentStage, stageVO);
                    stageVOList.add(stageVO);
                    Long stageRunTime = experimentStage.getRunTime() == null ? 0 : experimentStage.getRunTime();
                    //???????????????????????????????????????now-beginTime???????????????????????????runTime
                    if (ExperimentStageStateEnum.RUNNING_EXPERIMENT_STAGE_STATE.getCode().equals(experimentStage.getStatus())
                            && !ObjectUtils.isEmpty(experimentStage.getBeginTime())) {
                        stageRunTime = System.currentTimeMillis() - experimentStage.getBeginTime().getTime() + stageRunTime;
                    }
                    queryVO.setRunTime(queryVO.getRunTime() + stageRunTime);
                });
                queryVO.setStages(stageVOList);
            }
            return queryVO;
        }).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(queryVOList)) {
            List<UserDTO> userList = adminClient.getUserList(userIdList).getData();
            if (!CollectionUtils.isEmpty(userList)) {
                Map<Long, UserDTO> userMap = userList.stream().collect(Collectors.toMap(UserDTO::getId, Function.identity()));
                queryVOList.forEach(experiment -> {
                    experiment.setCreateUser(userMap.get(experiment.getCreateUserId()).getNickName());
                });
            }
        }

        return PageUtil.toPage(page, queryVOList);
    }

    /**
     * ??????????????????
     *
     * @param experimentId ??????ID
     * @return ????????????VO
     */
    @Override
    public ExperimentVO info(Long experimentId) {
        Experiment experiment = baseMapper.selectById(experimentId);
        if (ObjectUtils.isEmpty(experiment) || experiment.getDeleted()) {
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_DOES_NOT_EXIST_ERROR);
        }
        List<ExperimentStage> experimentStages = experimentStageService.getExperimentStageListByExperimentId(experimentId);
        if (ObjectUtils.isEmpty(experimentStages)) {
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_DOES_NOT_EXIST_ERROR);
        }
        Algorithm algorithm = algorithmService.getOneById(experiment.getAlgorithmId());
        AlgorithmVersion algorithmVersion = algorithmVersionService.getOneById(experiment.getAlgorithmVersionId());
        ExperimentVO experimentVO = ExperimentVO.from(experiment, algorithm.getName(), algorithmVersion.getVersionName());
        experimentVO.setStage(experimentStages.stream().map(stage -> {
            String yaml = getExperimentYamlFromMinIO(experimentId, stage.getStageOrder());
            AlgorithmStage algorithmStage = algorithmStageService.selectOneById(stage.getAlgorithmStageId());
            return ExperimentStageVO.from(stage, yaml, algorithmStage);
        }).collect(Collectors.toList()));
        return experimentVO;
    }

    /**
     * ????????????????????????
     *
     * @param experimentId ??????ID
     * @return ????????????VO
     */
    @Override
    public ExperimentVO getDetail(Long experimentId) {
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("???????????????????????????");
        }
        Experiment experiment = baseMapper.selectById(experimentId);
        if (ObjectUtils.isEmpty(experiment)) {
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_DOES_NOT_EXIST_ERROR);
        }
        List<ExperimentStage> experimentStages = experimentStageService.getExperimentStageListByExperimentId(experimentId);
        if (ObjectUtils.isEmpty(experimentStages)) {
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_DOES_NOT_EXIST_ERROR);
        }
        Algorithm algorithm = algorithmService.getOneById(experiment.getAlgorithmId());
        AlgorithmVersion algorithmVersion = algorithmVersionService.getOneById(experiment.getAlgorithmVersionId());

        ExperimentVO experimentVO = ExperimentVO.from(experiment, algorithm.getName(), algorithmVersion.getVersionName());
        experimentVO.setCreateUser(getUserName(experimentVO.getCreateUserId()));
        experimentVO.setBestCheckpointPath(pathUtil.getBestCheckpointPath("",experimentId));
        experimentVO.setStage(experimentStages.stream().map(ExperimentStageVO::from).collect(Collectors.toList()));
        experimentVO.setRunStage();
        experimentVO.setRunTime(0L);
        experimentVO.setBestAccuracy(0.0);
        //??????????????????
        experimentStages.forEach(stage -> {
            Long stageRunTime = stage.getRunTime() == null ? 0 : stage.getRunTime();
            //???????????????????????????????????????now-beginTime???????????????????????????runTime
            if (ExperimentStageStateEnum.RUNNING_EXPERIMENT_STAGE_STATE.getCode().equals(stage.getStatus())
                    && !ObjectUtils.isEmpty(stage.getBeginTime())) {
                stageRunTime = System.currentTimeMillis() - stage.getBeginTime().getTime() + stageRunTime;
            }
            experimentVO.setRunTime(experimentVO.getRunTime() + stageRunTime);
        });

        //??????????????????????????????trial
        LambdaQueryWrapper<TrialData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrialData::getExperimentId, experimentId);
        experimentStages.forEach(stage -> {
            if (stage.getStageOrder() == NumberConstant.NUMBER_3){
                wrapper.eq(TrialData::getStageId,stage.getId());
            }
        });
        List<TrialData> trialDataList = trialDataService.getTrialDataList(wrapper);
        trialDataList.forEach(trialData -> {
            if (trialData.getValue() != null && trialData.getValue() > experimentVO.getBestAccuracy()){
                experimentVO.setBestAccuracy(trialData.getValue());
                experimentVO.setBestTrialSequence(trialData.getSequence());
            }
        });

        return experimentVO;
    }


    /**
     * ????????????
     *
     * @param experimentUpdateDTO ????????????DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ExperimentUpdateDTO experimentUpdateDTO) {
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("???????????????????????????");
        }
        Experiment oldExperiment = baseMapper.selectById(experimentUpdateDTO.getId());
        Integer status = oldExperiment.getStatus();
        if (!ExperimentStatusEnum.TO_RUN_EXPERIMENT_STATE.getValue().equals(status)) {
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_UPDATE_ERROR);
        }
        //??????????????????
        Algorithm algorithm = algorithmService.selectOneById(experimentUpdateDTO.getAlgorithmId());
        // ????????????
        Experiment experiment = new Experiment(experimentUpdateDTO);
        baseMapper.updateById(experiment);
        experimentUpdateDTO.getStage().forEach(v ->
                experimentStageService.updateExperimentStageById(
                        new ExperimentStage(v, experiment)
                )
        );
        // ?????????????????????????????????
        trialService.delete(new LambdaQueryWrapper<Trial>() {{
            eq(Trial::getExperimentId, experiment.getId());
        }});
        // ?????? trial
        //experiment  ???stage1 * 10??? + ???stage2 * 1??? + ???stage * 3???= trialNum
        List<Trial> trial = Trial.from(
                experimentUpdateDTO,
                experimentStageService.getExperimentStageListByExperimentId(experiment.getId()),
                experiment
        );
        trialService.insertList(trial);

        // ?????????????????????????????????
        trialDataService.delete(new LambdaQueryWrapper<TrialData>() {{
            eq(TrialData::getExperimentId, experiment.getId());
        }});

        List<Trial> trialList = trialService.getTrialList(new LambdaQueryWrapper<Trial>() {{
            eq(Trial::getExperimentId, experiment.getId());
        }});
        List<TrialData> trialDataList = TrialData.from(trialList);
        trialDataService.insertList(trialDataList);

        // ??????????????? ?????????
        experimentUpdateDTO.getStage().forEach(stage -> {
            writeExperimentConfiguration(algorithm, experiment.getId(), stage.getStageOrder(), stage.getYaml());
        });
    }

    /**
     * ??????????????????
     *
     * @param algorithm    ????????????
     * @param experimentId ????????????
     * @param stageOrder   ????????????
     * @param yaml         yaml ??????
     */
    public void writeExperimentConfiguration(Algorithm algorithm, Long experimentId, Integer stageOrder, String yaml) {
        //?????????????????????????????????
        boolean copyAlgorithmResult = localFileUtil.copyPath(
                pathUtil.getAlgorithmPath(bucketName, algorithm.getName().toLowerCase() + File.separator + TadlConstant.ALGORITHM_PROJECT_NAME),
                pathUtil.getExperimentAlgorithmPath(bucketName, experimentId)
        );
        if (!copyAlgorithmResult) {
            throw new BusinessException(TadlErrorEnum.FILE_OPERATION_ERROR);
        }
        //????????? yaml ????????? minio
        try {
            minioUtil.writeString(
                    bucketName,
                    pathUtil.getExperimentYamlPath(StringUtils.EMPTY, experimentId)
                            .replaceFirst("/", StringUtils.EMPTY) + StageEnum.getStageName(stageOrder) + TadlConstant.ALGORITHM_CONFIGURATION_FILE_SUFFIX,
                    yaml
            );
        } catch (Exception e) {
            throw new BusinessException(TadlErrorEnum.FILE_OPERATION_ERROR);
        }
    }

    /**
     * ????????????
     *
     * @param experimentCreateDTO ????????????DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ExperimentCreateDTO experimentCreateDTO) {
        UserContext user = userContextService.getCurUser();
        // ????????????
        if (user == null) {
            throw new BusinessException("???????????????????????????");
        }
        checkNameExist(experimentCreateDTO.getName(), user.getId());
        //??????????????????
        Algorithm algorithm = algorithmService.selectOneById(experimentCreateDTO.getAlgorithmId());
        // ????????????
        Experiment experiment = new Experiment(experimentCreateDTO);
        baseMapper.insert(experiment);

        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "??????????????????????????????????????????.", experiment.getId());
        logMonitoringApi.addTadlLogsToEs(experiment.getId(),"Start creating experiment");
        // ??????????????????
        List<ExperimentStage> experimentStageList = experimentCreateDTO.getStage().stream().map(v -> new ExperimentStage(v, experiment)).collect(Collectors.toList());
        experimentStageService.insertExperimentStageList(experimentStageList);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "???????????????????????????.", experiment.getId());

        // ??? trial ???
        //experiment  ???stage1 * 10??? + ???stage2 * 1??? + ???stage * 3???= trialNum
        List<Trial> trial = Trial.from(
                experimentCreateDTO,
                experimentStageService.getExperimentStageListByExperimentId(experiment.getId()),
                experiment
        );
        trialService.insertList(trial);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "??????trial???????????????.", experiment.getId());

        List<Trial> trialList = trialService.getTrialList(new LambdaQueryWrapper<Trial>() {{
            eq(Trial::getExperimentId, experiment.getId());
        }});
        List<TrialData> trialDataList = TrialData.from(trialList);
        trialDataService.insertList(trialDataList);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "??????trial_data???????????????.", experiment.getId());

        // ??????????????? ?????????
        experimentCreateDTO.getStage().forEach(stage -> {
            writeExperimentConfiguration(algorithm, experiment.getId(), stage.getStageOrder(), stage.getYaml());
        });
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "??????????????????????????????.", experiment.getId());

        // ??????????????????->??????startExperiment??????????????????
        if (experimentCreateDTO.getStart()) {
            this.startExperiment(experiment);
            baseMapper.updateById(new Experiment() {{
                setId(experiment.getId());
                setStartTime(new Timestamp(System.currentTimeMillis()));
            }});
        }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "??????????????????.", experiment.getId());

    }

    /**
     * ??????????????????????????????
     *
     * @param name   ????????????
     * @param userId ??????id
     */
    private void checkNameExist(String name, Long userId) {
        int count = baseMapper.selectCount(new LambdaQueryWrapper<Experiment>()
                .eq(Experiment::getName, name)
                        .eq(Experiment::getCreateUserId, userId));
        if (count > NumberConstant.NUMBER_0) {
            throw new BusinessException(TadlErrorEnum.TADL_NAME_EXIST);
        }
    }

    @Override
    public ExperimentAndTrailDTO buildExperimentStageQueueMessage(Long experimentId, ExperimentStage experimentStage) {
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "Begin to build experiment stage queue message", experimentId);
        //????????????
        Algorithm algorithm = algorithmService.selectOneById(baseMapper.selectById(experimentId).getAlgorithmId());

        //??????????????????????????????
        Long messages = stringRedisTemplate.opsForStream().size(RedisKeyConstant.buildStreamStageKey(experimentStage.getExperimentId(), experimentStage.getId()));

        Experiment experiment = this.selectById(experimentId);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "Messages size:{},trial concurrent num:{}", experimentId, experimentStage.getId(), messages, experimentStage.getTrialConcurrentNum());
        if (experimentStage.getTrialConcurrentNum() <= messages) {
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "The number of concurrent experiments is less than the number of messages in the message queue", experiment.getId(), experimentStage.getId());
            return null;
        }
        //???????????????????????????????????????????????????????????? trial ??????
        List<Trial> trials = trialService.getTrialList(
                new LambdaQueryWrapper<Trial>()
                        .eq(Trial::getExperimentId, experimentStage.getExperimentId())
                        .eq(Trial::getStageId, experimentStage.getId())
                        .eq(Trial::getStatus, ExperimentStatusEnum.TO_RUN_EXPERIMENT_STATE.getValue())
        ).stream().limit(experimentStage.getTrialConcurrentNum() - messages).collect(Collectors.toList());
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "Messages size:{},trial concurrent num:{},experiment stage number of trials found:{}", experimentId, experimentStage.getId(), messages, experimentStage.getTrialConcurrentNum(), trials.size());
        if (CollectionUtils.isEmpty(trials)) {
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "The number of experimental trials is zero", experiment.getId(), experimentStage.getId());
            return null;
        }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "The experimental trial begins to assemble", experiment.getId(), experimentStage.getId());
        //?????? Trail ????????? ????????????
        return setExperimentAndTrialData(experiment, experimentStage, algorithm, trials);

    }


    /**
     * ?????? Trail ????????? ????????????
     * @param experiment ??????
     * @param stage ????????????
     * @param algorithm  ??????
     * @param trials trial ??????
     * @return ExperimentAndTrailDTO
     */
    private ExperimentAndTrailDTO setExperimentAndTrialData(Experiment experiment, ExperimentStage stage, Algorithm algorithm, List<Trial> trials) {
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "The experimental trial begin to go assembly", experiment.getId(), stage.getId());
        //?????????????????????????????????????????????
        AlgorithmStage algorithmStage = algorithmStageService.selectOneById(stage.getAlgorithmStageId());
        //????????????
        ExperimentAndTrailDTO experimentAndTrailDTO = new ExperimentAndTrailDTO();
        experimentAndTrailDTO.setExperimentId(stage.getExperimentId());
        experimentAndTrailDTO.setStageId(stage.getId());
        Long executionMaxTime = TimeCalculateUtil.getTime(stage.getMaxExecDurationUnit(), stage.getMaxExecDuration());
        experimentAndTrailDTO.setExecutionMaxTime(executionMaxTime);
        experimentAndTrailDTO.setTrialConcurrentNum(stage.getTrialConcurrentNum());
        experimentAndTrailDTO.setCreateUserId(experiment.getCreateUserId());
        experimentAndTrailDTO.setTrialRunParamDTOList(setRunParamData(stage, algorithmStage, algorithm, experiment, trials));
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG+ "The experimental trial assembly is completed", experiment.getId(), stage.getId());
        return experimentAndTrailDTO;
    }

    /**
     * trial???????????? ??????
     * @param stage ????????????
     * @param algorithmStage ????????????
     * @param algorithm ??????
     * @param experiment ??????
     * @param trials trial ??????
     * @return List<TrialRunParamDTO>
     */
    private List<TrialRunParamDTO> setRunParamData(ExperimentStage stage, AlgorithmStage algorithmStage, Algorithm algorithm, Experiment experiment, List<Trial> trials) {
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG+"The experimental trial data begin to go assembly", experiment.getId(), stage.getId());
        //????????????
        QueryResourceSpecsVO resource = adminServiceClient.queryTadlResourceSpecs(stage.getResourceId()).getData();
        if (ObjectUtils.isEmpty(resource)) {
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG+"The resource info is empty,the experiment failed", experiment.getId(), stage.getId());
            experiment.putStatusDetail(TadlConstant.ADMIN_SERVER_EXCEPTION,"????????????????????????");
            List<StateChangeDTO> stateChangeDTOList = trials.stream().map(trial -> new StateChangeDTO(new Object[]{trial.getId(),experiment.getStatusDetail()},
                    TrialEventMachineConstant.TRIAL_STATE_MACHINE,TrialEventMachineConstant.FAILED_TRIAL_EVENT)).collect(Collectors.toList());
            StateMachineUtil.stateChange(stateChangeDTOList);
            // ????????????????????????????????????????????????
            tadlRedisService.deleteRunningTrial(stage.getId());
            //??????redis????????????
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG+"Because the resource information is empty, please prepare to delete the redis cache information. ", experiment.getId(), stage.getId());
            tadlRedisService.delRedisExperimentInfo(experiment.getId());
            //????????????????????????
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG+"Redis cache deletion completed. ", experiment.getId(), stage.getId());
            throw new BusinessException(TadlErrorEnum.RESOURCE_ERROR);
        }
        LogUtil.info(LogEnum.TADL, "Get the resourceSpecs success!The experiment id:{} ,stage id:{}. ", experiment.getId(), stage.getId());
        //??????trial??????
        List<TrialRunParamDTO> trialRunParamDTOList = new ArrayList<>();
        trials.forEach(trial -> {
            //??????trial????????????
            TrialRunParamDTO trialRunParamDTO = new TrialRunParamDTO();
            trialRunParamDTO.setExperimentId(trial.getExperimentId());
            trialRunParamDTO.setName(experiment.getName());
            trialRunParamDTO.setTrialId(trial.getId());
            trialRunParamDTO.setStageId(trial.getStageId());
            trialRunParamDTO.setDatasetPath(algorithmStage.getDatasetPath());
            trialRunParamDTO.setAlgorithmPath(pathUtil.getExperimentAlgorithmPath(StringUtils.EMPTY, trial.getExperimentId()));
            trialRunParamDTO.setLogPath(pathUtil.getTrialLogPath(
                    StringUtils.EMPTY,
                    trial.getExperimentId(),
                    algorithmStage.getName(),
                    trial.getId()
                    )

            );
            trialRunParamDTO.setTrialPath(pathUtil.getTrialResultPath(
                    StringUtils.EMPTY,
                    trial.getExperimentId(),
                    algorithmStage.getName(),
                    trial.getId()
                    )
            );

            trialRunParamDTO.setGpuNum(resource.getGpuNum());
            // ????????????(0???CPU???1???GPU)
            trialRunParamDTO.setResourcesPoolType(resource.getResourcesPoolType() ? NumberConstant.NUMBER_0 : NumberConstant.NUMBER_1);
            trialRunParamDTO.setMemNum(resource.getMemNum());
            trialRunParamDTO.setCpuNum(resource.getCpuNum());
            trialRunParamDTO.setNamespace(k8sNameTool.getNamespace(experiment.getCreateUserId()));
            trialRunParamDTO.setExperimentPath(pathUtil.getExperimentPath(
                    StringUtils.EMPTY,
                    trial.getExperimentId()
            ));
            //??????????????????
            try {
                trialRunParamDTO.setCommand(
                        cmdUtil.getStartAlgCmd(
                                StageEnum.getStage(stage.getStageOrder()),
                                getExperimentYamlFromMinIO(
                                        trial.getExperimentId(),
                                        stage.getStageOrder()
                                ),
                                algorithmStage,
                                trial.getSequence(),
                                algorithm,
                                trial.getExperimentId()
                        )
                );
            } catch (Exception e) {
                LogUtil.error(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+"????????????????????????,????????????:{}",trial.getExperimentId(),trial.getStageId(),trial.getId(), e.getMessage());
                experiment.putStatusDetail(TadlConstant.TRIAL_STARTUP_COMMAND_ASSEMBLY_EXCEPTION,e.getMessage());
                stateMachineStatusUtil.trialExperimentFailedState(trial.getExperimentId(),trial.getStageId(),trial.getId(),experiment.getStatusDetail());
                throw new BusinessException(TadlErrorEnum.CMD_FORM_ERROR);
            }
            trialRunParamDTOList.add(trialRunParamDTO);
        });
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG+"The experimental trial data assembly is completed", experiment.getId(), stage.getId());
        return trialRunParamDTOList;
    }



    /**
     * ??????minio ???????????? yaml
     *
     * @param experimentId ??????id
     * @param stage        ???????????????
     * @return yaml ?????????
     */
    public String getExperimentYamlFromMinIO(Long experimentId, Integer stage) {
        try {
            return minioUtil.readString(
                    bucketName,
                    pathUtil.getExperimentYamlPath(
                            StringUtils.EMPTY,
                            experimentId
                    ) + StageEnum.getStageName(stage) + TadlConstant.ALGORITHM_CONFIGURATION_FILE_SUFFIX
            );
        } catch (Exception e) {
            throw new BusinessException(TadlErrorEnum.PARAM_ERROR);
        }
    }

    /**
     * ??????????????????
     *
     * @param experimentId ????????????
     * @return ???????????????????????????????????????
     */
    @Override
    public HashMap<String, Object> getConfiguration(Long experimentId) {
        Experiment experiment = baseMapper.selectById(experimentId);
        if (Objects.isNull(experiment) || experiment.getDeleted()) {
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_DOES_NOT_EXIST_ERROR);
        }
        Algorithm algorithm = algorithmService.selectOneById(experiment.getAlgorithmId());
        return new HashMap<String, Object>(NumberConstant.NUMBER_4) {{
            try {
                // ????????????????????????
                for (StageEnum stage : StageEnum.values()) {

                    String filePath = StageEnum.BASE.getName().equals(stage.getName()) ?
                            //   /TADL/experiment/114/algorithm/TADL/pytorch/classic_nas/yaml
                            pathUtil.getExperimentPath(StringUtils.EMPTY, experimentId) + "/algorithm/" + pathUtil.getPytorchAlgorithmYamlPath(algorithm.getName().toLowerCase())
                            //     /TADL/experiment/114/algorithm/yaml/
                            : pathUtil.getExperimentYamlPath(StringUtils.EMPTY, experimentId);

                    String fullFilePath = filePath + stage.getName() + TadlConstant.ALGORITHM_CONFIGURATION_FILE_SUFFIX;
                    // ?????????
                    HashMap<String, Object> yamlConfiguration = KeyNameConvertUtil.convertToCamelStyle(
                            Objects.requireNonNull(
                                    //???json
                                    YamlParseUtil.YamlParse(
                                            minioUtil.readString(
                                                    bucketName,
                                                    fullFilePath
                                            )
                                    )
                            )
                    );
                    yamlConfiguration.put("stageOrder", stage.getStageOrder());
                    // ????????????yaml??????json?????????
                    put(stage.getName(), yamlConfiguration);
                }
            } catch (Exception e) {
                LogUtil.error(LogEnum.TADL, "???????????????????????????????????????:{}", e.getMessage());
                throw new BusinessException(TadlErrorEnum.PARAM_ERROR);
            }
        }};
    }

    /**
     * ??????minio ???????????? search_space??????
     *
     * @param experimentId ????????????
     * @return ????????? search_space??????
     */
    @Override
    public ExperimentFileVO getSearchSpace(Long experimentId) {
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("???????????????????????????");
        }
        if (!queryEmpty(experimentId)) {
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_DOES_NOT_EXIST_ERROR);
        }
        try {
            ExperimentFileVO experimentFileVO = new ExperimentFileVO();
            List<FileDTO> fileDTOList = minioUtil.fileList(bucketName, pathUtil.getStageSearchSpacePath("", experimentId), false);
            if (CollectionUtils.isEmpty(fileDTOList)) {
                LogUtil.info(LogEnum.TADL, "can not find file in path: {}", pathUtil.getStageSearchSpacePath("", experimentId));
            }
            for (FileDTO fileDTO : fileDTOList) {
                if (TadlConstant.SEARCH_SPACE_FILENAME.equals(fileDTO.getName())) {
                    LogUtil.info(LogEnum.TADL, "file info: {}", fileDTO);
                    experimentFileVO.setAbstractUrl(bucketName + fileDTO.getPath());
                    String fileStr = minioUtil.readString(bucketName, fileDTO.getPath());
                    JSONObject jsonObject = JSONObject.parseObject(fileStr);
                    experimentFileVO.setFileStr(jsonObject.toJSONString());
                    break;
                }
            }
            return experimentFileVO;
        } catch (Exception e) {
            LogUtil.error(LogEnum.TADL, "??????searchSpace?????????????????????:{}", e.getMessage());
            throw new BusinessException(TadlErrorEnum.PARAM_ERROR);
        }
    }

    /**
     * ??????minio ???????????? best_selected_space??????
     *
     * @param experimentId ????????????
     * @return ????????? search_space??????
     */
    @Override
    public ExperimentFileVO getBestSelectedSpace(Long experimentId) {
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("???????????????????????????");
        }
        if (!queryEmpty(experimentId)) {
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_DOES_NOT_EXIST_ERROR);
        }
        try {
            ExperimentFileVO experimentFileVO = new ExperimentFileVO();
            List<FileDTO> fileDTOList = minioUtil.fileList(bucketName, pathUtil.getBestSelectedSpacePath("", experimentId), false);
            if (CollectionUtils.isEmpty(fileDTOList)) {
                LogUtil.info(LogEnum.TADL, "can not find file in path: {}", pathUtil.getBestSelectedSpacePath("", experimentId));
            }
            for (FileDTO fileDTO : fileDTOList) {
                if (TadlConstant.BEST_SELECTED_SPACE_FILENAME.equals(fileDTO.getName())) {
                    LogUtil.info(LogEnum.TADL, "file info: {}", fileDTO);
                    experimentFileVO.setAbstractUrl(bucketName + fileDTO.getPath());
                    String fileStr = minioUtil.readString(bucketName, fileDTO.getPath());
                    JSONObject jsonObject = JSONObject.parseObject(fileStr);
                    experimentFileVO.setFileStr(jsonObject.toJSONString());
                    break;
                }
            }
            return experimentFileVO;
        } catch (Exception e) {
            LogUtil.error(LogEnum.TADL, "??????best_selected_space?????????????????????:{}", e.getMessage());
            throw new BusinessException(TadlErrorEnum.PARAM_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restartExperiment(Long experimentId) {
        logMonitoringApi.addTadlLogsToEs(experimentId,"??????????????????");
        Experiment experiment = baseMapper.selectById(experimentId);
        if (Objects.isNull(experiment)) {
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_DOES_NOT_EXIST_ERROR);
        }
        this.startExperiment(experiment);
        logMonitoringApi.addTadlLogsToEs(experimentId,"??????????????????");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startExperiment(Long experimentId) {
        Experiment experiment = baseMapper.selectById(experimentId);
        if (Objects.isNull(experiment)) {
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_DOES_NOT_EXIST_ERROR);
        }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "Experiment start begin.", experiment.getId());
        logMonitoringApi.addTadlLogsToEs(experimentId,"??????????????????");
        this.startExperiment(experiment);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "Experiment start end.", experiment.getId());
        logMonitoringApi.addTadlLogsToEs(experimentId,"??????????????????");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startExperiment(Experiment experiment) {
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "??????????????????.", experiment.getId());

        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("???????????????????????????");
        }

        //??????????????????
        if (ExperimentStatusEnum.WAITING_EXPERIMENT_STATE.getValue().equals(experiment.getStatus()) ||
                ExperimentStatusEnum.RUNNING_EXPERIMENT_STATE.getValue().equals(experiment.getStatus())) {
            LogUtil.error(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG+"{}??????????????????????????????.", experiment.getId(),ExperimentStatusEnum.getState(experiment.getStatus()).getMsg());
            throw new BusinessException(TadlErrorEnum.OPERATION_NOT_ALLOWED);
        }

        //???????????????????????????????????????
        tadlRedisService.delRedisExperimentInfo(experiment.getId());

        restartFailedExperimentStage(experiment);
        //??????????????????????????????
        StateMachineUtil.stateChange(new StateChangeDTO(new Object[]{experiment.getId()},ExperimentEventMachineConstant.EXPERIMENT_STATE_MACHINE,ExperimentEventMachineConstant.WAITING_EXPERIMENT_EVENT));

        List<ExperimentStage> experimentStageList = experimentStageService.getExperimentStageList(new LambdaQueryWrapper<ExperimentStage>()
                .eq(ExperimentStage::getExperimentId, experiment.getId())
                        .eq(ExperimentStage::getStatus, ExperimentStageStateEnum.TO_RUN_EXPERIMENT_STAGE_STATE.getCode())
        );
        LogUtil.debug(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG+" Experiment stage size :{}", experiment.getId(), experimentStageList.size());
        if (CollectionUtils.isEmpty(experimentStageList)){
            LogUtil.error(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG+" Experiment stage size :{}", experiment.getId(), experimentStageList.size());
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_DOES_NOT_EXIST_ERROR);
        }

        ExperimentAndTrailDTO experimentAndTrailDTO = buildExperimentStageQueueMessage(experiment.getId(), experimentStageList.stream().findFirst().get());


        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "build experiment and trial list success", experiment.getId());

        tadlRedisService.pushDataToConsumer(experimentAndTrailDTO);
    }

    /**
     * ?????????????????????????????????????????????
     * @param experiment ??????
     */
    private void restartFailedExperimentStage(Experiment experiment) {
        if (ExperimentStatusEnum.FAILED_EXPERIMENT_STATE.getValue().equals(experiment.getStatus())) {
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "restart operation from failed status." , experiment.getId());
            //??????????????????????????????????????????????????????????????????????????????????????????
            List<Long> experimentStageIdList = experimentStageService.getExperimentStageList(new LambdaQueryWrapper<ExperimentStage>(){
                {
                    eq(ExperimentStage::getExperimentId,experiment.getId())
                            .eq(ExperimentStage::getStatus,ExperimentStageStateEnum.FAILED_EXPERIMENT_STAGE_STATE.getCode());
                }
            }).stream().map(ExperimentStage::getId).collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(experimentStageIdList)){
                //??????????????????????????????????????????
                StateMachineUtil.stateChange(new StateChangeDTO(new Object[]{experimentStageIdList},ExperimentStageEventMachineConstant.EXPERIMENT_STAGE_STATE_MACHINE,ExperimentStageEventMachineConstant.TO_RUN_BATCH_EXPERIMENT_STAGE_EVENT));
            }
            //trial????????????,trial???????????????trial????????????????????????????????????k8s????????????trial???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            List<Integer> originalStateList = Arrays.asList(TrialStatusEnum.FAILED.getVal(), TrialStatusEnum.UNKNOWN.getVal(),TrialStatusEnum.WAITING.getVal(),TrialStatusEnum.RUNNING.getVal());
            changeTrialStatus(experiment.getId(),originalStateList);
        }


    }

    /**
     * ??????trial??????????????????
     * @param experimentId ??????id
     * @param originalStateList ??????????????????
     */
    public void changeTrialStatus(Long experimentId,List<Integer> originalStateList){
        //??????????????????????????????????????????trial ????????????????????????
        Map<Integer, List<Long>> statusTrialIdListMap = trialService.getTrialList(new LambdaQueryWrapper<Trial>()
                .eq(Trial::getExperimentId,experimentId)
                        .in(Trial::getStatus,originalStateList)).stream().collect(Collectors.groupingBy(Trial::getStatus,
                Collectors.mapping(Trial::getId, Collectors.toList())));

        for (Map.Entry<Integer,List<Long>> statusTrialIdList:statusTrialIdListMap.entrySet()){
            if (!CollectionUtils.isEmpty(statusTrialIdList.getValue())) {
                StateMachineUtil.stateChange(new StateChangeDTO(new Object[]{statusTrialIdList.getValue()},TrialEventMachineConstant.TRIAL_STATE_MACHINE,TrialEventMachineConstant.TO_RUN_BATCH_TRIAL_EVENT));
            }
        }


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteExperiment(Long experimentId) {
        logMonitoringApi.addTadlLogsToEs(experimentId,"??????????????????");
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("???????????????????????????");
        }
        //??????????????????
        Experiment experiment = baseMapper.selectById(experimentId);
        if (Objects.isNull(experiment) || experiment.getDeleted()) {
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_DOES_NOT_EXIST_ERROR);
        }
        //??????
        StateMachineUtil.stateChange(new StateChangeDTO(new Object[]{experimentId},ExperimentEventMachineConstant.EXPERIMENT_STATE_MACHINE,ExperimentEventMachineConstant.DELETE_EXPERIMENT_INFO_EVENT));
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "????????????????????????.", experimentId);
        logMonitoringApi.addTadlLogsToEs(experimentId,"????????????????????????");
        try {
            String filePath = k8sNameTool.getAbsolutePath(pathUtil.getStageSearchSpacePath(StringUtils.EMPTY, experimentId));
            createRecycleTask(filePath, experiment);
        } catch (Exception e) {
            LogUtil.error(LogEnum.TADL, "The algorithm delete operation is abnormal.The exception message:{}", e.getMessage());
            throw new BusinessException("RecycleTask error");
        }

        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "??????????????????.", experimentId);
        logMonitoringApi.addTadlLogsToEs(experimentId,"??????????????????");
        //????????????????????????
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "??????????????????????????????.", experimentId);
        logMonitoringApi.addTadlLogsToEs(experimentId,"??????????????????????????????");
        tadlRedisService.delRedisExperimentInfo(experimentId);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "??????????????????????????????.", experimentId);
        logMonitoringApi.addTadlLogsToEs(experimentId,"??????????????????????????????");
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "??????????????????.", experimentId);
        logMonitoringApi.addTadlLogsToEs(experimentId,"??????????????????");
    }

    /**
     * ??????????????????
     * @param recyclePath ????????????
     * @param experiment ?????????????????????
     */
    private void createRecycleTask(String recyclePath, Experiment experiment) {
        RecycleCreateDTO recycleCreateDTO = RecycleCreateDTO.builder()
                .recycleModule(RecycleModuleEnum.BIZ_TADL.getValue())
                .recycleDelayDate(recycleConfig.getTadlValid())  //??????3???
                .recycleNote(RecycleTool.generateRecycleNote("??????????????????", experiment.getName(), experiment.getId()))
                .recycleCustom(RecycleResourceEnum.TADL_EXPERIMENT_RECYCLE_FILE.getClassName())
                .restoreCustom(RecycleResourceEnum.TADL_EXPERIMENT_RECYCLE_FILE.getClassName())
                .remark(String.valueOf(experiment.getId()))
                .build();
        recycleCreateDTO.addRecycleDetailCreateDTO(RecycleDetailCreateDTO.builder()
                .recycleCondition(recyclePath)
                .recycleType(RecycleTypeEnum.FILE.getCode())
                .recycleNote(RecycleTool.generateRecycleNote("??????????????????", experiment.getName(), experiment.getId()))
                .remark(String.valueOf(experiment.getId()))
                .build()
        );
        recycleService.createRecycleTask(recycleCreateDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pauseExperiment(Long experimentId) {
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("???????????????????????????");
        }
        logMonitoringApi.addTadlLogsToEs(experimentId,"??????????????????");
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG+"Pause experiment.", experimentId);
        //?????? ??????????????????
        StateMachineUtil.stateChange(new StateChangeDTO(new Object[]{experimentId},ExperimentEventMachineConstant.EXPERIMENT_STATE_MACHINE,ExperimentEventMachineConstant.PAUSED_EXPERIMENT_EVENT));
        List<ExperimentStage> experimentStages = experimentStageService.getExperimentStageList(new LambdaQueryWrapper<ExperimentStage>()
                .eq(ExperimentStage::getExperimentId,experimentId)
                        .eq(ExperimentStage::getStatus,ExperimentStageStateEnum.RUNNING_EXPERIMENT_STAGE_STATE.getCode())
        );

        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG+"The number of experiment stage.", experimentId,experimentStages.size());
        //?????????????????????????????????,??????????????????????????????
        experimentStages.forEach(experimentStage -> StateMachineUtil.stateChange(new StateChangeDTO(new Object[]{experimentStage.getId()},ExperimentStageEventMachineConstant.EXPERIMENT_STAGE_STATE_MACHINE,ExperimentStageEventMachineConstant.TO_RUN_EXPERIMENT_STAGE_EVENT)));

        //??????????????????trial ??????
        List<Trial> trialList = trialService.getTrialList(new LambdaQueryWrapper<Trial>()
                .eq(Trial::getExperimentId, experimentId)
                .in(Trial::getStatus, TrialStatusEnum.WAITING.getVal(), TrialStatusEnum.RUNNING.getVal()));
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG+"The number of trial:{}.", experimentId,trialList.size());

        //?????????????????????????????????trial??????????????????
        changeTrialStatus(experimentId,Arrays.asList(TrialStatusEnum.WAITING.getVal(),TrialStatusEnum.RUNNING.getVal()));
        logMonitoringApi.addTadlLogsToEs(experimentId,"????????????????????????trial");

        //??????trial?????????????????????????????????????????????????????????????????????????????????
        if (!CollectionUtils.isEmpty(trialList)){
            List<Long> stageIdList = trialList.stream().map(Trial::getStageId).distinct().collect(Collectors.toList());
            stageIdList.forEach(stageId-> tadlRedisService.deleteRunningTrial(stageId));
        }
        logMonitoringApi.addTadlLogsToEs(experimentId,"????????????????????????trial");
        redisUtils.set(RedisKeyConstant.buildPausedKey(experimentId), ExperimentStatusEnum.PAUSED_EXPERIMENT_STATE.getMsg());
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG+"The experiment paused.", experimentId);
        logMonitoringApi.addTadlLogsToEs(experimentId,"??????????????????");
    }

    @Override
    public IntermediateAccuracyVO getIntermediateAccuracy(ExperimentIntermediateAccuracyDTO experimentIntermediateAccuracyDTO) {
        ExperimentStage experimentStage = experimentStageService.selectOne(experimentIntermediateAccuracyDTO.getExperimentId(), experimentIntermediateAccuracyDTO.getStageOrder());
        if (Objects.isNull(experimentStage)) {
            return null;
        }
        List<Integer> statusList = new ArrayList<>();
        statusList.add(TrialStatusEnum.FINISHED.getVal());
        statusList.add(TrialStatusEnum.RUNNING.getVal());
        List<Trial> trialList = trialMapper.queryTrialById(experimentStage.getExperimentId(), experimentStage.getId(), experimentIntermediateAccuracyDTO.getTrialIds(), statusList);
        IntermediateAccuracyVO intermediateAccuracyVO = new IntermediateAccuracyVO(new ArrayList<>());
        trialList.forEach(trial -> {
            try {
                String result = minioUtil.readString(bucketName, "TADL" + File.separator + "experiment" + File.separator
                        + trial.getExperimentId() + File.separator + StageEnum.getStage(experimentStage.getStageOrder()).getName()
                        + File.separator + trial.getSequence() + TadlConstant.RESULT_PATH);
                String jsonArr[] = result.split(SymbolConstant.LINEBREAK);
                TrialIntermediateAccuracyVO trialIntermediateAccuracyVO = new TrialIntermediateAccuracyVO();
                trialIntermediateAccuracyVO.setTrialName(trial.getSequence().toString());
                List<IntermediateAccuracyDataVO> dataListVOS = new ArrayList<>();
                for (int i = 0; i < jsonArr.length; i++) {
                    TrialResultVO trialResultVO = JSONArray.parseObject(jsonArr[i], TrialResultVO.class);
                    // ???????????????type = Accuracy ??????
                    if (TadlConstant.RESULT_JSON_TYPE.equals(trialResultVO.getType().toLowerCase())){
                        IntermediateAccuracyDataVO listDTO = new IntermediateAccuracyDataVO();
                        listDTO.setCategory(trialResultVO.getResult().getCategory());
                        listDTO.setSequence(trialResultVO.getResult().getSequence());
                        listDTO.setValue(Double.parseDouble(trialResultVO.getResult().getValue()));
                        dataListVOS.add(listDTO);
                        intermediateAccuracyVO.getConfig().setXFieldName(trialResultVO.getResult().getCategory());
                        intermediateAccuracyVO.getConfig().setYFieldName(trialResultVO.getType());
                    }
                }
                trialIntermediateAccuracyVO.setList(dataListVOS);
                intermediateAccuracyVO.getData().add(trialIntermediateAccuracyVO);
            } catch (Exception e) {
                LogUtil.error(LogEnum.TADL, "read result.json error. Experiment id: {},message:{}", experimentIntermediateAccuracyDTO.getExperimentId(),e.getMessage());
            }
        });
        return intermediateAccuracyVO;
    }

    @Override
    public BestAccuracyOutVO getBestAccuracy(ExperimentBestAccuracyDTO experimentBestAccuracyDTO) {
        ExperimentStage experimentStage = experimentStageService.selectOne(experimentBestAccuracyDTO.getExperimentId(), experimentBestAccuracyDTO.getStageOrder());
        if (Objects.isNull(experimentStage)) {
            return null;
        }
        List<TrialData> trialDataList = trialMapper.queryTrialDataById(experimentStage.getExperimentId(), experimentStage.getId());
        List<BestAccuracyDataVO> dataVOList = new ArrayList<>();
        trialDataList.forEach(trialData -> {
            BestAccuracyDataVO bestAccuracyDataVO = new BestAccuracyDataVO();
            bestAccuracyDataVO.setTrial(trialData.getSequence());
            bestAccuracyDataVO.setAccuracy(trialData.getValue());
            dataVOList.add(bestAccuracyDataVO);
        });
        return new BestAccuracyOutVO(dataVOList);
    }

    @Override
    public RunTimeOutVO getRunTime(ExperimentRunTimeDTO experimentRunTimeDTO) {
        ExperimentStage experimentStage = experimentStageService.selectOne(experimentRunTimeDTO.getExperimentId(), experimentRunTimeDTO.getStageOrder());
        if (Objects.isNull(experimentStage)) {
            return null;
        }
        List<RumTimeDataVO> rumTimeDataVOList = new ArrayList<>();
        List<Trial> trialList = trialMapper.queryTrialById(experimentStage.getExperimentId(), experimentStage.getId(), null,null);
        trialList.forEach(trial -> {
            RumTimeDataVO rumTimeDataVO = new RumTimeDataVO();
            rumTimeDataVO.setTrial(trial.getSequence());
            double time = 0;
            if (!ObjectUtils.isEmpty(trial.getStartTime()) && !ObjectUtils.isEmpty(trial.getEndTime())) {
                //??????????????????????????????????????????
                time = (trial.getEndTime().getTime() - trial.getStartTime().getTime()) / 1000.0 / 60.0;
            }
            rumTimeDataVO.setTime(time);
            rumTimeDataVOList.add(rumTimeDataVO);
        });

        return new RunTimeOutVO(rumTimeDataVOList);
    }

    @Override
    public void updateExperiment(LambdaUpdateWrapper<Experiment> wrapper) {
        baseMapper.update(null, wrapper);

    }

    @Override
    public void updateExperimentDeletedById(Long experimentId, boolean deleted) {
        experimentMapper.updateExperimentDeletedById(experimentId, deleted);
    }

    @Override
    public ExperimentLogQueryVO queryExperimentLog(ExperimentLogQueryDTO experimentLogQueryDTO) {
        Experiment experiment = baseMapper.selectById(experimentLogQueryDTO.getExperimentId().longValue());

        if (Objects.isNull(experiment)){
            LogUtil.error(LogEnum.TADL,TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG+"Can not find experiment info",experimentLogQueryDTO.getExperimentId());
            throw new BusinessException("can not find experiment info");
        }
        LogMonitoringVO logMonitoringVO = logMonitoringApi.searchTadlLogById(experimentLogQueryDTO.getStartLine(), experimentLogQueryDTO.getLines(), experimentLogQueryDTO.getExperimentId());

        ExperimentLogQueryVO experimentLogQueryVO = new ExperimentLogQueryVO();
        experimentLogQueryVO
                .setContent(logMonitoringVO.getLogs())
                .setStartLine(experimentLogQueryDTO.getStartLine())
                .setEndLine(experimentLogQueryDTO.getStartLine() + logMonitoringVO.getTotalLogs())
                .setLines(logMonitoringVO.getTotalLogs())
                .setExperimentName(experiment.getName());

        return experimentLogQueryVO;
    }

    @Override
    public void updateExperimentFailedByTrialId(Long trialId, Integer trialStatus, String statusDetail) {
        experimentMapper.updateExperimentFailedByTrialId(trialId, trialStatus,
                ExperimentStageStateEnum.FAILED_EXPERIMENT_STAGE_STATE.getCode(),
                ExperimentStatusEnum.FAILED_EXPERIMENT_STATE.getValue(),statusDetail);
    }
}
