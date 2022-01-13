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
 * @description 实验管理服务实现类
 * @date 2021-03-22
 */
@Service
public class ExperimentServiceImpl extends ServiceImpl<ExperimentMapper, Experiment> implements ExperimentService {

    /**
     * 文件操作工具
     */
    @Resource
    private LocalFileUtil localFileUtil;

    /**
     * 用户服务
     */
    @Resource
    private AdminClient adminClient;

    /**
     * trial 服务
     */
    @Resource
    private TadlTrialService trialService;

    /**
     * 实验阶段服务
     */
    @Resource
    private ExperimentStageService experimentStageService;

    /**
     * minIO桶名
     */
    @Value("${minio.bucketName}")
    private String bucketName;

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
     * 算法服务
     */
    @Resource
    private AlgorithmService algorithmService;

    /**
     * 算法版本服务
     */
    @Resource
    private AlgorithmVersionService algorithmVersionService;

    /**
     * 算法阶段服务
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
     * 获取Experiment中所有属性
     */
    private final Field[] fields = Experiment.class.getDeclaredFields();

    /**
     * 根据 id 判断是否存在
     *
     * @param experimentId 实验id
     * @return 实验对象
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
     * 查询单条实验记录
     *
     * @param experimentId 实验id
     * @return 实验对象
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
     * 获取用户昵称
     *
     * @param userId 用户id
     * @return 用户昵称
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
            throw new BusinessException("当前用户信息已失效");
        }
        LogUtil.info(LogEnum.TADL, "User {} queried the experiment list with the query of {}", user.getUsername(), JSONObject.toJSONString(experimentQueryDTO));
        if (experimentQueryDTO.getCurrent() == null || experimentQueryDTO.getSize() == null) {
            throw new BusinessException(TadlErrorEnum.PARAM_ERROR);
        }
        String name = experimentQueryDTO.getName();
        //实验id或名称非空
        if (StringUtils.isNotBlank(name)) {
            //整数匹配
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
        //管理员可以看到全部信息
        if (!BaseService.isAdmin(user)) {
            wrapper.eq("create_user_id", user.getId());
        }
        Page page = new Page(null == experimentQueryDTO.getCurrent() ? NumberConstant.NUMBER_1 : experimentQueryDTO.getCurrent(),
                null == experimentQueryDTO.getSize() ? NumberConstant.NUMBER_10 : experimentQueryDTO.getSize());
        try {
            //排序字段，默认按更新时间降序，否则将驼峰转换为下划线
            String column = experimentQueryDTO.getSort() != null && FILE_NAMES.contains(experimentQueryDTO.getSort()) ? StringUtils.humpToLine(experimentQueryDTO.getSort()) : "update_time";
            //排序方式
            boolean isAsc = StringUtils.isBlank(experimentQueryDTO.getOrder()) || StringUtils.equals(experimentQueryDTO.getOrder(), StringConstant.SORT_DESC) ? false : true;
            wrapper.orderBy(true, isAsc, column);
        } catch (Exception e) {
            LogUtil.error(LogEnum.TADL, "Query experiment list with an exception, query info:{}，exception info:{}", JSONObject.toJSONString(experimentQueryDTO), e);
            throw new BusinessException(TadlErrorEnum.DB_SEARCH_ERROR);
        }
        IPage<Experiment> experiments = baseMapper.selectPage(page, wrapper);

        //查出所有的stage，封装成map
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
            //计算runTime
            queryVO.setRunTime(0L);

            if (!CollectionUtils.isEmpty(experimentStageMap.get(experiment.getId()))) {
                List<ExperimentQueryVO.StageVO> stageVOList = new ArrayList<>();
                experimentStageMap.get(experiment.getId()).forEach(experimentStage -> {
                    ExperimentQueryVO.StageVO stageVO = new ExperimentQueryVO.StageVO();
                    BeanUtils.copyProperties(experimentStage, stageVO);
                    stageVOList.add(stageVO);
                    Long stageRunTime = experimentStage.getRunTime() == null ? 0 : experimentStage.getRunTime();
                    //判断状态，如果是运行中，则now-beginTime，其他状态，则就是runTime
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
     * 获取实验详情
     *
     * @param experimentId 实验ID
     * @return 实验详情VO
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
     * 获取实验详情概览
     *
     * @param experimentId 实验ID
     * @return 实验详情VO
     */
    @Override
    public ExperimentVO getDetail(Long experimentId) {
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
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
        //查询运行时间
        experimentStages.forEach(stage -> {
            Long stageRunTime = stage.getRunTime() == null ? 0 : stage.getRunTime();
            //判断状态，如果是运行中，则now-beginTime，其他状态，则就是runTime
            if (ExperimentStageStateEnum.RUNNING_EXPERIMENT_STAGE_STATE.getCode().equals(stage.getStatus())
                    && !ObjectUtils.isEmpty(stage.getBeginTime())) {
                stageRunTime = System.currentTimeMillis() - stage.getBeginTime().getTime() + stageRunTime;
            }
            experimentVO.setRunTime(experimentVO.getRunTime() + stageRunTime);
        });

        //设置最佳精度和对应的trial
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
     * 编辑实验
     *
     * @param experimentUpdateDTO 实验编辑DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ExperimentUpdateDTO experimentUpdateDTO) {
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        Experiment oldExperiment = baseMapper.selectById(experimentUpdateDTO.getId());
        Integer status = oldExperiment.getStatus();
        if (!ExperimentStatusEnum.TO_RUN_EXPERIMENT_STATE.getValue().equals(status)) {
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_UPDATE_ERROR);
        }
        //获取算法对象
        Algorithm algorithm = algorithmService.selectOneById(experimentUpdateDTO.getAlgorithmId());
        // 更新实验
        Experiment experiment = new Experiment(experimentUpdateDTO);
        baseMapper.updateById(experiment);
        experimentUpdateDTO.getStage().forEach(v ->
                experimentStageService.updateExperimentStageById(
                        new ExperimentStage(v, experiment)
                )
        );
        // 删除之前的，新增现在的
        trialService.delete(new LambdaQueryWrapper<Trial>() {{
            eq(Trial::getExperimentId, experiment.getId());
        }});
        // 更新 trial
        //experiment  （stage1 * 10） + （stage2 * 1） + （stage * 3）= trialNum
        List<Trial> trial = Trial.from(
                experimentUpdateDTO,
                experimentStageService.getExperimentStageListByExperimentId(experiment.getId()),
                experiment
        );
        trialService.insertList(trial);

        // 删除之前的，新增现在的
        trialDataService.delete(new LambdaQueryWrapper<TrialData>() {{
            eq(TrialData::getExperimentId, experiment.getId());
        }});

        List<Trial> trialList = trialService.getTrialList(new LambdaQueryWrapper<Trial>() {{
            eq(Trial::getExperimentId, experiment.getId());
        }});
        List<TrialData> trialDataList = TrialData.from(trialList);
        trialDataService.insertList(trialDataList);

        // 写实验配置 和算法
        experimentUpdateDTO.getStage().forEach(stage -> {
            writeExperimentConfiguration(algorithm, experiment.getId(), stage.getStageOrder(), stage.getYaml());
        });
    }

    /**
     * 写实验的配置
     *
     * @param algorithm    算法对象
     * @param experimentId 实验对象
     * @param stageOrder   阶段排序
     * @param yaml         yaml 字符
     */
    public void writeExperimentConfiguration(Algorithm algorithm, Long experimentId, Integer stageOrder, String yaml) {
        //写算法文件到实验目录下
        boolean copyAlgorithmResult = localFileUtil.copyPath(
                pathUtil.getAlgorithmPath(bucketName, algorithm.getName().toLowerCase() + File.separator + TadlConstant.ALGORITHM_PROJECT_NAME),
                pathUtil.getExperimentAlgorithmPath(bucketName, experimentId)
        );
        if (!copyAlgorithmResult) {
            throw new BusinessException(TadlErrorEnum.FILE_OPERATION_ERROR);
        }
        //写算法 yaml 文件到 minio
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
     * 创建实验
     *
     * @param experimentCreateDTO 创建实验DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ExperimentCreateDTO experimentCreateDTO) {
        UserContext user = userContextService.getCurUser();
        // 参数校验
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        checkNameExist(experimentCreateDTO.getName(), user.getId());
        //获取算法对象
        Algorithm algorithm = algorithmService.selectOneById(experimentCreateDTO.getAlgorithmId());
        // 写实验表
        Experiment experiment = new Experiment(experimentCreateDTO);
        baseMapper.insert(experiment);

        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "实验表写入成功，开始创建实验.", experiment.getId());
        logMonitoringApi.addTadlLogsToEs(experiment.getId(),"Start creating experiment");
        // 写实验阶段表
        List<ExperimentStage> experimentStageList = experimentCreateDTO.getStage().stream().map(v -> new ExperimentStage(v, experiment)).collect(Collectors.toList());
        experimentStageService.insertExperimentStageList(experimentStageList);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "实验阶段表写入成功.", experiment.getId());

        // 写 trial 表
        //experiment  （stage1 * 10） + （stage2 * 1） + （stage * 3）= trialNum
        List<Trial> trial = Trial.from(
                experimentCreateDTO,
                experimentStageService.getExperimentStageListByExperimentId(experiment.getId()),
                experiment
        );
        trialService.insertList(trial);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "实验trial表写入成功.", experiment.getId());

        List<Trial> trialList = trialService.getTrialList(new LambdaQueryWrapper<Trial>() {{
            eq(Trial::getExperimentId, experiment.getId());
        }});
        List<TrialData> trialDataList = TrialData.from(trialList);
        trialDataService.insertList(trialDataList);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "实验trial_data表写入成功.", experiment.getId());

        // 写实验配置 和算法
        experimentCreateDTO.getStage().forEach(stage -> {
            writeExperimentConfiguration(algorithm, experiment.getId(), stage.getStageOrder(), stage.getYaml());
        });
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "实验配置文件写入成功.", experiment.getId());

        // 是否启动实验->调用startExperiment接口启动实验
        if (experimentCreateDTO.getStart()) {
            this.startExperiment(experiment);
            baseMapper.updateById(new Experiment() {{
                setId(experiment.getId());
                setStartTime(new Timestamp(System.currentTimeMillis()));
            }});
        }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "实验创建成功.", experiment.getId());

    }

    /**
     * 校验实验名称是否存在
     *
     * @param name   实验名称
     * @param userId 用户id
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
        //算法获取
        Algorithm algorithm = algorithmService.selectOneById(baseMapper.selectById(experimentId).getAlgorithmId());

        //消息队列消息数量获取
        Long messages = stringRedisTemplate.opsForStream().size(RedisKeyConstant.buildStreamStageKey(experimentStage.getExperimentId(), experimentStage.getId()));

        Experiment experiment = this.selectById(experimentId);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "Messages size:{},trial concurrent num:{}", experimentId, experimentStage.getId(), messages, experimentStage.getTrialConcurrentNum());
        if (experimentStage.getTrialConcurrentNum() <= messages) {
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "The number of concurrent experiments is less than the number of messages in the message queue", experiment.getId(), experimentStage.getId());
            return null;
        }
        //查询实验当前阶段下所有可继续下发的待运行 trial 数据
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
        //实验 Trail 消息体 数据组装
        return setExperimentAndTrialData(experiment, experimentStage, algorithm, trials);

    }


    /**
     * 实验 Trail 消息体 数据组装
     * @param experiment 实验
     * @param stage 实验阶段
     * @param algorithm  算法
     * @param trials trial 实验
     * @return ExperimentAndTrailDTO
     */
    private ExperimentAndTrailDTO setExperimentAndTrialData(Experiment experiment, ExperimentStage stage, Algorithm algorithm, List<Trial> trials) {
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "The experimental trial begin to go assembly", experiment.getId(), stage.getId());
        //查询当前实验阶段所用的算法配置
        AlgorithmStage algorithmStage = algorithmStageService.selectOneById(stage.getAlgorithmStageId());
        //拼装数据
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
     * trial运行参数 组装
     * @param stage 实验阶段
     * @param algorithmStage 算法阶段
     * @param algorithm 算法
     * @param experiment 实验
     * @param trials trial 实验
     * @return List<TrialRunParamDTO>
     */
    private List<TrialRunParamDTO> setRunParamData(ExperimentStage stage, AlgorithmStage algorithmStage, Algorithm algorithm, Experiment experiment, List<Trial> trials) {
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG+"The experimental trial data begin to go assembly", experiment.getId(), stage.getId());
        //获取配置
        QueryResourceSpecsVO resource = adminServiceClient.queryTadlResourceSpecs(stage.getResourceId()).getData();
        if (ObjectUtils.isEmpty(resource)) {
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG+"The resource info is empty,the experiment failed", experiment.getId(), stage.getId());
            experiment.putStatusDetail(TadlConstant.ADMIN_SERVER_EXCEPTION,"资源规格获取异常");
            List<StateChangeDTO> stateChangeDTOList = trials.stream().map(trial -> new StateChangeDTO(new Object[]{trial.getId(),experiment.getStatusDetail()},
                    TrialEventMachineConstant.TRIAL_STATE_MACHINE,TrialEventMachineConstant.FAILED_TRIAL_EVENT)).collect(Collectors.toList());
            StateMachineUtil.stateChange(stateChangeDTOList);
            // 调用异步方法，删除正在运行的任务
            tadlRedisService.deleteRunningTrial(stage.getId());
            //删除redis缓存信息
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG+"Because the resource information is empty, please prepare to delete the redis cache information. ", experiment.getId(), stage.getId());
            tadlRedisService.delRedisExperimentInfo(experiment.getId());
            //删除相关信息完成
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG+"Redis cache deletion completed. ", experiment.getId(), stage.getId());
            throw new BusinessException(TadlErrorEnum.RESOURCE_ERROR);
        }
        LogUtil.info(LogEnum.TADL, "Get the resourceSpecs success!The experiment id:{} ,stage id:{}. ", experiment.getId(), stage.getId());
        //设置trial数据
        List<TrialRunParamDTO> trialRunParamDTOList = new ArrayList<>();
        trials.forEach(trial -> {
            //运行trial参数命令
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
            // 节点类型(0为CPU，1为GPU)
            trialRunParamDTO.setResourcesPoolType(resource.getResourcesPoolType() ? NumberConstant.NUMBER_0 : NumberConstant.NUMBER_1);
            trialRunParamDTO.setMemNum(resource.getMemNum());
            trialRunParamDTO.setCpuNum(resource.getCpuNum());
            trialRunParamDTO.setNamespace(k8sNameTool.getNamespace(experiment.getCreateUserId()));
            trialRunParamDTO.setExperimentPath(pathUtil.getExperimentPath(
                    StringUtils.EMPTY,
                    trial.getExperimentId()
            ));
            //组装启动命令
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
                LogUtil.error(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+"组装启动命令失败,失败原因:{}",trial.getExperimentId(),trial.getStageId(),trial.getId(), e.getMessage());
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
     * 获取minio 中实验的 yaml
     *
     * @param experimentId 算法id
     * @param stage        阶段排序值
     * @return yaml 字符串
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
     * 获取算法配置
     *
     * @param experimentId 算法名称
     * @return 实验所用算法所有阶段的配置
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
                // 获取所有阶段名称
                for (StageEnum stage : StageEnum.values()) {

                    String filePath = StageEnum.BASE.getName().equals(stage.getName()) ?
                            //   /TADL/experiment/114/algorithm/TADL/pytorch/classic_nas/yaml
                            pathUtil.getExperimentPath(StringUtils.EMPTY, experimentId) + "/algorithm/" + pathUtil.getPytorchAlgorithmYamlPath(algorithm.getName().toLowerCase())
                            //     /TADL/experiment/114/algorithm/yaml/
                            : pathUtil.getExperimentYamlPath(StringUtils.EMPTY, experimentId);

                    String fullFilePath = filePath + stage.getName() + TadlConstant.ALGORITHM_CONFIGURATION_FILE_SUFFIX;
                    // 转驼峰
                    HashMap<String, Object> yamlConfiguration = KeyNameConvertUtil.convertToCamelStyle(
                            Objects.requireNonNull(
                                    //转json
                                    YamlParseUtil.YamlParse(
                                            minioUtil.readString(
                                                    bucketName,
                                                    fullFilePath
                                            )
                                    )
                            )
                    );
                    yamlConfiguration.put("stageOrder", stage.getStageOrder());
                    // 存放所有yaml（转json后的）
                    put(stage.getName(), yamlConfiguration);
                }
            } catch (Exception e) {
                LogUtil.error(LogEnum.TADL, "获取算法配置异常，异常信息:{}", e.getMessage());
                throw new BusinessException(TadlErrorEnum.PARAM_ERROR);
            }
        }};
    }

    /**
     * 获取minio 中实验的 search_space内容
     *
     * @param experimentId 算法名称
     * @return 实验的 search_space内容
     */
    @Override
    public ExperimentFileVO getSearchSpace(Long experimentId) {
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
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
            LogUtil.error(LogEnum.TADL, "获取searchSpace异常，异常信息:{}", e.getMessage());
            throw new BusinessException(TadlErrorEnum.PARAM_ERROR);
        }
    }

    /**
     * 获取minio 中实验的 best_selected_space内容
     *
     * @param experimentId 算法名称
     * @return 实验的 search_space内容
     */
    @Override
    public ExperimentFileVO getBestSelectedSpace(Long experimentId) {
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
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
            LogUtil.error(LogEnum.TADL, "获取best_selected_space异常，异常信息:{}", e.getMessage());
            throw new BusinessException(TadlErrorEnum.PARAM_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restartExperiment(Long experimentId) {
        logMonitoringApi.addTadlLogsToEs(experimentId,"开始重启实验");
        Experiment experiment = baseMapper.selectById(experimentId);
        if (Objects.isNull(experiment)) {
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_DOES_NOT_EXIST_ERROR);
        }
        this.startExperiment(experiment);
        logMonitoringApi.addTadlLogsToEs(experimentId,"成功重启实验");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startExperiment(Long experimentId) {
        Experiment experiment = baseMapper.selectById(experimentId);
        if (Objects.isNull(experiment)) {
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_DOES_NOT_EXIST_ERROR);
        }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "Experiment start begin.", experiment.getId());
        logMonitoringApi.addTadlLogsToEs(experimentId,"开始启动实验");
        this.startExperiment(experiment);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "Experiment start end.", experiment.getId());
        logMonitoringApi.addTadlLogsToEs(experimentId,"成功启动实验");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startExperiment(Experiment experiment) {
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "开始启动实验.", experiment.getId());

        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }

        //实验运行校验
        if (ExperimentStatusEnum.WAITING_EXPERIMENT_STATE.getValue().equals(experiment.getStatus()) ||
                ExperimentStatusEnum.RUNNING_EXPERIMENT_STATE.getValue().equals(experiment.getStatus())) {
            LogUtil.error(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG+"{}状态不允许进行此操作.", experiment.getId(),ExperimentStatusEnum.getState(experiment.getStatus()).getMsg());
            throw new BusinessException(TadlErrorEnum.OPERATION_NOT_ALLOWED);
        }

        //清空缓存，建立新的缓存数据
        tadlRedisService.delRedisExperimentInfo(experiment.getId());

        restartFailedExperimentStage(experiment);
        //变更实验状态为等待中
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
     * 变更运行失败的实验阶段为待运行
     * @param experiment 实验
     */
    private void restartFailedExperimentStage(Experiment experiment) {
        if (ExperimentStatusEnum.FAILED_EXPERIMENT_STATE.getValue().equals(experiment.getStatus())) {
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "restart operation from failed status." , experiment.getId());
            //为防止出现多个实验阶段运行失败的异常现象导致实验不能正常重启
            List<Long> experimentStageIdList = experimentStageService.getExperimentStageList(new LambdaQueryWrapper<ExperimentStage>(){
                {
                    eq(ExperimentStage::getExperimentId,experiment.getId())
                            .eq(ExperimentStage::getStatus,ExperimentStageStateEnum.FAILED_EXPERIMENT_STAGE_STATE.getCode());
                }
            }).stream().map(ExperimentStage::getId).collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(experimentStageIdList)){
                //运行失败的实验阶段，变更状态
                StateMachineUtil.stateChange(new StateChangeDTO(new Object[]{experimentStageIdList},ExperimentStageEventMachineConstant.EXPERIMENT_STAGE_STATE_MACHINE,ExperimentStageEventMachineConstant.TO_RUN_BATCH_EXPERIMENT_STAGE_EVENT));
            }
            //trial运行失败,trial未知异常，trial处于等待中及运行中状态时k8s异常导致trial状态未更新，但实验状态因超时被更新成运行失败；处于以上可能状态时，需要能够重新运行实验成功
            List<Integer> originalStateList = Arrays.asList(TrialStatusEnum.FAILED.getVal(), TrialStatusEnum.UNKNOWN.getVal(),TrialStatusEnum.WAITING.getVal(),TrialStatusEnum.RUNNING.getVal());
            changeTrialStatus(experiment.getId(),originalStateList);
        }


    }

    /**
     * 变更trial状态为待运行
     * @param experimentId 实验id
     * @param originalStateList 当前状态集合
     */
    public void changeTrialStatus(Long experimentId,List<Integer> originalStateList){
        //查询运行失败的和因异常失败的trial 变更状态为待运行
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
        logMonitoringApi.addTadlLogsToEs(experimentId,"开始删除实验");
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        //删除实验信息
        Experiment experiment = baseMapper.selectById(experimentId);
        if (Objects.isNull(experiment) || experiment.getDeleted()) {
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_DOES_NOT_EXIST_ERROR);
        }
        //删除
        StateMachineUtil.stateChange(new StateChangeDTO(new Object[]{experimentId},ExperimentEventMachineConstant.EXPERIMENT_STATE_MACHINE,ExperimentEventMachineConstant.DELETE_EXPERIMENT_INFO_EVENT));
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "开始进行文件回收.", experimentId);
        logMonitoringApi.addTadlLogsToEs(experimentId,"开始进行文件回收");
        try {
            String filePath = k8sNameTool.getAbsolutePath(pathUtil.getStageSearchSpacePath(StringUtils.EMPTY, experimentId));
            createRecycleTask(filePath, experiment);
        } catch (Exception e) {
            LogUtil.error(LogEnum.TADL, "The algorithm delete operation is abnormal.The exception message:{}", e.getMessage());
            throw new BusinessException("RecycleTask error");
        }

        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "文件回收成功.", experimentId);
        logMonitoringApi.addTadlLogsToEs(experimentId,"文件回收成功");
        //删除实验缓存信息
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "开始删除实验缓存信息.", experimentId);
        logMonitoringApi.addTadlLogsToEs(experimentId,"开始删除实验缓存信息");
        tadlRedisService.delRedisExperimentInfo(experimentId);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "删除实验缓存信息成功.", experimentId);
        logMonitoringApi.addTadlLogsToEs(experimentId,"删除实验缓存信息成功");
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "删除实验成功.", experimentId);
        logMonitoringApi.addTadlLogsToEs(experimentId,"删除实验成功");
    }

    /**
     * 实验文件回收
     * @param recyclePath 回收路径
     * @param experiment 回收的实验对象
     */
    private void createRecycleTask(String recyclePath, Experiment experiment) {
        RecycleCreateDTO recycleCreateDTO = RecycleCreateDTO.builder()
                .recycleModule(RecycleModuleEnum.BIZ_TADL.getValue())
                .recycleDelayDate(recycleConfig.getTadlValid())  //默认3天
                .recycleNote(RecycleTool.generateRecycleNote("删除实验文件", experiment.getName(), experiment.getId()))
                .recycleCustom(RecycleResourceEnum.TADL_EXPERIMENT_RECYCLE_FILE.getClassName())
                .restoreCustom(RecycleResourceEnum.TADL_EXPERIMENT_RECYCLE_FILE.getClassName())
                .remark(String.valueOf(experiment.getId()))
                .build();
        recycleCreateDTO.addRecycleDetailCreateDTO(RecycleDetailCreateDTO.builder()
                .recycleCondition(recyclePath)
                .recycleType(RecycleTypeEnum.FILE.getCode())
                .recycleNote(RecycleTool.generateRecycleNote("删除实验文件", experiment.getName(), experiment.getId()))
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
            throw new BusinessException("当前用户信息已失效");
        }
        logMonitoringApi.addTadlLogsToEs(experimentId,"开始暂停实验");
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG+"Pause experiment.", experimentId);
        //实验 变更为暂停中
        StateMachineUtil.stateChange(new StateChangeDTO(new Object[]{experimentId},ExperimentEventMachineConstant.EXPERIMENT_STATE_MACHINE,ExperimentEventMachineConstant.PAUSED_EXPERIMENT_EVENT));
        List<ExperimentStage> experimentStages = experimentStageService.getExperimentStageList(new LambdaQueryWrapper<ExperimentStage>()
                .eq(ExperimentStage::getExperimentId,experimentId)
                        .eq(ExperimentStage::getStatus,ExperimentStageStateEnum.RUNNING_EXPERIMENT_STAGE_STATE.getCode())
        );

        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG+"The number of experiment stage.", experimentId,experimentStages.size());
        //若存在运行中的实验阶段,将状态变更为待运行；
        experimentStages.forEach(experimentStage -> StateMachineUtil.stateChange(new StateChangeDTO(new Object[]{experimentStage.getId()},ExperimentStageEventMachineConstant.EXPERIMENT_STAGE_STATE_MACHINE,ExperimentStageEventMachineConstant.TO_RUN_EXPERIMENT_STAGE_EVENT)));

        //删除运行中的trial 实验
        List<Trial> trialList = trialService.getTrialList(new LambdaQueryWrapper<Trial>()
                .eq(Trial::getExperimentId, experimentId)
                .in(Trial::getStatus, TrialStatusEnum.WAITING.getVal(), TrialStatusEnum.RUNNING.getVal()));
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG+"The number of trial:{}.", experimentId,trialList.size());

        //将运行中，等待中状态的trial变更为待运行
        changeTrialStatus(experimentId,Arrays.asList(TrialStatusEnum.WAITING.getVal(),TrialStatusEnum.RUNNING.getVal()));
        logMonitoringApi.addTadlLogsToEs(experimentId,"开始删除运行中的trial");

        //因有trial实验处于等待中，但实验阶段处于待运行状态下进行暂停操作
        if (!CollectionUtils.isEmpty(trialList)){
            List<Long> stageIdList = trialList.stream().map(Trial::getStageId).distinct().collect(Collectors.toList());
            stageIdList.forEach(stageId-> tadlRedisService.deleteRunningTrial(stageId));
        }
        logMonitoringApi.addTadlLogsToEs(experimentId,"成功删除运行中的trial");
        redisUtils.set(RedisKeyConstant.buildPausedKey(experimentId), ExperimentStatusEnum.PAUSED_EXPERIMENT_STATE.getMsg());
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG+"The experiment paused.", experimentId);
        logMonitoringApi.addTadlLogsToEs(experimentId,"成功暂停实验");
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
                    // 只解析获取type = Accuracy 的值
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
                //计算得到运行时间，单位为分钟
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
