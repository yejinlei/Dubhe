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

package org.dubhe.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.dubhe.annotation.DataPermissionMethod;
import org.dubhe.async.StopTrainJobAsync;
import org.dubhe.async.TransactionAsyncManager;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.base.ResponseCode;
import org.dubhe.config.NfsConfig;
import org.dubhe.config.RecycleConfig;
import org.dubhe.config.TrainHarborConfig;
import org.dubhe.config.TrainJobConfig;
import org.dubhe.constant.AlgorithmSourceEnum;
import org.dubhe.constant.SymbolConstant;
import org.dubhe.dao.DictDetailMapper;
import org.dubhe.dao.PtJobParamMapper;
import org.dubhe.dao.PtModelBranchMapper;
import org.dubhe.dao.PtModelInfoMapper;
import org.dubhe.dao.PtTrainAlgorithmMapper;
import org.dubhe.dao.PtTrainJobMapper;
import org.dubhe.dao.PtTrainJobSpecsMapper;
import org.dubhe.dao.PtTrainMapper;
import org.dubhe.dao.PtTrainParamMapper;
import org.dubhe.data.constant.Constant;
import org.dubhe.domain.PtModelBranch;
import org.dubhe.domain.PtModelInfo;
import org.dubhe.domain.dto.BaseTrainJobDTO;
import org.dubhe.domain.dto.PtTrainDataSourceStatusQueryDTO;
import org.dubhe.domain.dto.PtTrainJobCreateDTO;
import org.dubhe.domain.dto.PtTrainJobDeleteDTO;
import org.dubhe.domain.dto.PtTrainJobDetailQueryDTO;
import org.dubhe.domain.dto.PtTrainJobResumeDTO;
import org.dubhe.domain.dto.PtTrainJobStopDTO;
import org.dubhe.domain.dto.PtTrainJobUpdateDTO;
import org.dubhe.domain.dto.PtTrainJobVersionQueryDTO;
import org.dubhe.domain.dto.PtTrainModelDTO;
import org.dubhe.domain.dto.PtTrainQueryDTO;
import org.dubhe.domain.dto.RecycleTaskCreateDTO;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.domain.entity.DictDetail;
import org.dubhe.domain.entity.PtJobParam;
import org.dubhe.domain.entity.PtTrain;
import org.dubhe.domain.entity.PtTrainAlgorithm;
import org.dubhe.domain.entity.PtTrainJob;
import org.dubhe.domain.entity.PtTrainJobSpecs;
import org.dubhe.domain.entity.PtTrainParam;
import org.dubhe.domain.vo.ModelVO;
import org.dubhe.domain.vo.PtImageAndAlgorithmVO;
import org.dubhe.domain.vo.PtJobMetricsGrafanaVO;
import org.dubhe.domain.vo.PtTrainDataSourceStatusQueryVO;
import org.dubhe.domain.vo.PtTrainJobDeleteVO;
import org.dubhe.domain.vo.PtTrainJobDetailQueryVO;
import org.dubhe.domain.vo.PtTrainJobDetailVO;
import org.dubhe.domain.vo.PtTrainJobModelVO;
import org.dubhe.domain.vo.PtTrainJobStatisticsMineVO;
import org.dubhe.domain.vo.PtTrainJobStopVO;
import org.dubhe.domain.vo.PtTrainVO;
import org.dubhe.enums.AlgorithmStatusEnum;
import org.dubhe.enums.DatasetTypeEnum;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.ModelResourceEnum;
import org.dubhe.enums.RecycleModuleEnum;
import org.dubhe.enums.RecycleTypeEnum;
import org.dubhe.enums.TrainJobStatusEnum;
import org.dubhe.enums.TrainTypeEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.k8s.api.DistributeTrainApi;
import org.dubhe.k8s.api.PersistentVolumeClaimApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.api.TrainJobApi;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.utils.PodUtil;
import org.dubhe.service.PtTrainJobService;
import org.dubhe.service.RecycleTaskService;
import org.dubhe.utils.ImageUtil;
import org.dubhe.utils.JwtUtils;
import org.dubhe.utils.K8sNameTool;
import org.dubhe.utils.KeyUtil;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.NfsUtil;
import org.dubhe.utils.PageUtil;
import org.dubhe.utils.ReflectionUtils;
import org.dubhe.utils.SqlUtil;
import org.dubhe.utils.StringUtils;
import org.dubhe.utils.TrainUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description 训练作业job服务实现类
 * @date 2020-04-27
 */
@Service
public class PtTrainJobServiceImpl implements PtTrainJobService {

    @Autowired
    private PtTrainMapper ptTrainMapper;

    @Autowired
    private PtTrainJobMapper ptTrainJobMapper;

    @Autowired
    private PtJobParamMapper ptJobParamMapper;

    @Autowired
    private PtTrainParamMapper ptTrainParamMapper;

    @Autowired
    private PtTrainAlgorithmMapper ptTrainAlgorithmMapper;

    @Autowired
    private TrainJobApi trainJobApi;

    @Autowired
    private PersistentVolumeClaimApi persistentVolumeClaimApi;

    @Autowired
    private PtTrainJobSpecsMapper ptTrainJobSpecsMapper;

    @Autowired
    private PodApi podApi;

    @Autowired
    private TrainJobConfig trainJobConfig;

    @Autowired
    private TrainHarborConfig trainHarborConfig;

    @Autowired
    private K8sNameTool k8sNameTool;

    @Autowired
    private ImageUtil imageUtil;

    @Autowired
    private TransactionAsyncManager asyncManager;

    @Autowired
    private StopTrainJobAsync stopTrainJobAsync;

    @Autowired
    private DistributeTrainApi distributeTrainApi;

    @Autowired
    private DictDetailMapper dictDetailMapper;

    @Autowired
    private NfsConfig nfsConfig;

    @Autowired
    private NfsUtil nfsUtil;

    @Autowired
    private RecycleConfig recycleConfig;

    @Autowired
    private RecycleTaskService recycleTaskService;

    @Value("${k8s.pod.metrics.grafanaUrl}")
    private String k8sPodMetricsGrafanaUrl;

    @Autowired
    private PtModelInfoMapper ptModelInfoMapper;

    @Autowired
    private PtModelBranchMapper ptModelBranchMapper;

    public final static List<String> FIELD_NAMES;

    static {
        FIELD_NAMES = ReflectionUtils.getFieldNames(PtTrainVO.class);
    }

    /**
     * 作业列表展示
     *
     * @param ptTrainQueryDTO       查询作业列表参数
     * @return Map<String, Object>  作业列表分页信息
     **/
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Object> getTrainJob(@NonNull PtTrainQueryDTO ptTrainQueryDTO) {
        Page<PtTrainVO> pageTrainResult;
        UserDTO currentUser = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} query job list display, received parameters are {}", currentUser.getUsername(), ptTrainQueryDTO);

        Page page = ptTrainQueryDTO.toPage();
        String order;
        String sort;
        try {
            //排序方式
            order = Constant.SORT_ASC.equalsIgnoreCase(ptTrainQueryDTO.getOrder()) ? Constant.SORT_ASC : Constant.SORT_DESC;
            //排序字段
            String sortField = FIELD_NAMES.contains(ptTrainQueryDTO.getSort()) ? ptTrainQueryDTO.getSort() : Constant.ID;
            sort = StringUtils.humpToLine(sortField);
            pageTrainResult = ptTrainJobMapper.getPageTrain(page, currentUser.getId(), ptTrainQueryDTO.getTrainStatus(), ptTrainQueryDTO.getTrainName(), sort, order);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "Query job list shows exception {}, receive parameter {}", e, ptTrainQueryDTO);
            throw new BusinessException("查询作业列表展示异常");
        }
        List<PtTrainVO> trainResult = pageTrainResult.getRecords();
        if (CollectionUtils.isNotEmpty(trainResult)) {
            LogUtil.info(LogEnum.BIZ_TRAIN, "The user {} query job list is displayed and the result is as follows {}.", currentUser.getUsername(), trainResult);
        }
        return PageUtil.toPage(page, trainResult);

    }

    /**
     * 作业不同版本job列表展示
     *
     * @param ptTrainJobVersionQueryDTO 查询作业不同版本job列表参数
     * @return List<PtTrainJobDetailVO> 训练详情集合
     **/
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public List<PtTrainJobDetailVO> getTrainJobVersion(PtTrainJobVersionQueryDTO ptTrainJobVersionQueryDTO) {
        UserDTO currentUser = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} queries different versions of job list display, received parameter trainId is {}", currentUser.getUsername(), ptTrainJobVersionQueryDTO.getTrainId());

        //校验参数
        checkTrainId(ptTrainJobVersionQueryDTO.getTrainId(), currentUser);
        String sort = null == ptTrainJobVersionQueryDTO.getSort() ? Constant.ID : ptTrainJobVersionQueryDTO.getSort();

        QueryWrapper<PtTrainJob> queryTrainJonWrapper = new QueryWrapper<>();
        queryTrainJonWrapper.eq("train_id", ptTrainJobVersionQueryDTO.getTrainId());
        //根据训练状态筛选
        if (ptTrainJobVersionQueryDTO.getTrainStatus() != null) {
            queryTrainJonWrapper.eq("train_status", ptTrainJobVersionQueryDTO.getTrainStatus());
        }
        if (Constant.SORT_ASC.equals(ptTrainJobVersionQueryDTO.getOrder())) {
            queryTrainJonWrapper.orderByAsc(StringUtils.humpToLine(sort));
        } else {
            queryTrainJonWrapper.orderByDesc(StringUtils.humpToLine(sort));
        }
        //按照trainId查找
        List<PtTrainJob> ptTrainJobs = ptTrainJobMapper.selectList(queryTrainJonWrapper);
        if (CollectionUtils.isEmpty(ptTrainJobs)) {
            LogUtil.info(LogEnum.BIZ_TRAIN, "No training task with trainId of {} and training status of {} was found by user {}", currentUser.getUsername(), ptTrainJobVersionQueryDTO.getTrainId(), ptTrainJobVersionQueryDTO.getTrainStatus());
            return Collections.emptyList();
        }
        Set<Long> jobIds = ptTrainJobs.stream().map(PtTrainJob::getId).collect(Collectors.toSet());
        QueryWrapper<PtJobParam> queryJobParamWrapper = new QueryWrapper<>();
        queryJobParamWrapper.in("train_job_id", jobIds);
        //找出所有训练参数
        List<PtJobParam> ptJobParams = ptJobParamMapper.selectList(queryJobParamWrapper);
        List<Long> algorithmIds = ptJobParams.stream().map(PtJobParam::getAlgorithmId).distinct().collect(Collectors.toList());
        List<PtTrainAlgorithm> ptTrainAlgorithms = ptTrainAlgorithmMapper.selectAllBatchIds(algorithmIds);
        //获取训练信息

        PtTrain ptTrain = ptTrainMapper.selectById(ptTrainJobVersionQueryDTO.getTrainId());
        //结果集处理
        List<PtTrainJobDetailVO> list = getTrainJobDetail(ptTrainJobs, ptJobParams, ptTrainAlgorithms, ptTrain);
        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} query different version of job list display completed, return result {}", currentUser.getUsername(), list);
        return list;
    }

    /**
     * 结果集处理 拼装PtTrainJobDetailVO
     *
     * @param ptTrainJobs       训练任务集合
     * @param ptJobParams       训练参数集合
     * @param ptTrainAlgorithms 训练算法集合
     * @param ptTrain           训练
     * @return List<PtTrainJobDetailVO> 训练版本查询详情集合
     */
    private List<PtTrainJobDetailVO> getTrainJobDetail(List<PtTrainJob> ptTrainJobs, List<PtJobParam> ptJobParams, List<PtTrainAlgorithm> ptTrainAlgorithms, PtTrain ptTrain) {
        List<PtTrainJobDetailVO> list = new ArrayList<>();
        Map<Long, Integer> jobParamMap = new HashedMap<>();
        ptTrainJobs.forEach(x -> {
            PtTrainJobDetailVO ptTrainJobDetailVO = new PtTrainJobDetailVO();
            BeanUtil.copyProperties(x, ptTrainJobDetailVO);
            list.add(ptTrainJobDetailVO);
            jobParamMap.put(x.getId(), list.size());
        });

        ptJobParams.forEach(x -> {
            PtTrainJobDetailVO ptTrainJobDetailVO = list.get(jobParamMap.get(x.getTrainJobId()) - 1);
            if (null != ptTrainJobDetailVO) {

                ptTrainJobDetailVO.setAlgorithmId(x.getAlgorithmId()).setRunCommand(x.getRunCommand()).setImageName(x.getImageName())
                        .setRunParams(x.getRunParams())
                        .setParamF1(x.getParamF1()).setParamCallback(x.getParamCallback())
                        .setParamPrecise(x.getParamPrecise()).setParamAccuracy(x.getParamAccuracy());
                long nowTime = System.currentTimeMillis();
                //获取训练延时启动倒计时（分钟）
                if (x.getDelayCreateTime() != null && nowTime < x.getDelayCreateTime().getTime() && TrainJobStatusEnum.checkRunStatus(ptTrainJobDetailVO.getTrainStatus())) {
                    ptTrainJobDetailVO.setDelayCreateCountDown(TrainUtil.getCountDown(x.getDelayCreateTime().getTime()));
                }
                //获取训练自动停止倒计时（分钟）
                if (x.getDelayDeleteTime() != null && nowTime < x.getDelayDeleteTime().getTime() && TrainJobStatusEnum.checkRunStatus(ptTrainJobDetailVO.getTrainStatus())) {
                    ptTrainJobDetailVO.setDelayDeleteCountDown(TrainUtil.getCountDown(x.getDelayDeleteTime().getTime()));
                }
                //image信息拼装
                if (StringUtils.isNotBlank(x.getImageName())) {
                    String imageNameSuffix = x.getImageName().substring(x.getImageName().lastIndexOf(StrUtil.SLASH) + MagicNumConstant.ONE);
                    String[] imageNameSuffixArray = imageNameSuffix.split(StrUtil.COLON);
                    ptTrainJobDetailVO.setImageName(imageNameSuffixArray[0]);
                    ptTrainJobDetailVO.setImageTag(imageNameSuffixArray[1]);
                }
            }
        });

        Map<Long, PtTrainAlgorithm> algorithmMap = new HashedMap<>();
        ptTrainAlgorithms.forEach(x -> algorithmMap.put(x.getId(), x));

        for (PtTrainJobDetailVO ptTrainJobDetailVO : list) {
            PtTrainAlgorithm ptTrainAlgorithm = algorithmMap.get(ptTrainJobDetailVO.getAlgorithmId());
            if (null != ptTrainAlgorithm) {
                ptTrainJobDetailVO.setAlgorithmName(ptTrainAlgorithm.getAlgorithmName())
                        .setAlgorithmSource(ptTrainAlgorithm.getAlgorithmSource())
                        .setAlgorithmUsage(ptTrainAlgorithm.getAlgorithmUsage())
                        .setAccuracy(ptTrainAlgorithm.getAccuracy())
                        .setP4InferenceSpeed(ptTrainAlgorithm.getP4InferenceSpeed());
                if (ptTrainAlgorithm.getAlgorithmSource() == MagicNumConstant.ONE) {
                    ptTrainJobDetailVO.setAlgorithmCodeDir(ptTrainAlgorithm.getCodeDir());
                }
            }
        }

        list.forEach(x -> x.setTrainName(ptTrain.getTrainName()));
        return list;
    }

    /**
     * 校验请求不同版本job所传参数是否合法
     *
     * @param trainId     训练ID
     * @param currentUser 当前用户
     */
    private void checkTrainId(Long trainId, UserDTO currentUser) {
        if (null == trainId || trainId < 1) {
            LogUtil.info(LogEnum.BIZ_TRAIN, "User {} queries different versions of job list display, request parameter trainId is {}, it is illegal", currentUser.getUsername(), trainId);
            throw new BusinessException("参数不合法");
        }
        PtTrain ptTrain = ptTrainMapper.selectById(trainId);
        if (null == ptTrain || !currentUser.getId().equals(ptTrain.getCreateUserId())) {
            LogUtil.info(LogEnum.BIZ_TRAIN, "User {} queries different versions of job list display, request parameter trainId is {}, it is illegal", currentUser.getUsername(), trainId);
            throw new BusinessException("参数不合法");
        }
    }

    /**
     * 创建训练job
     *
     * @param ptTrainJobCreateDTO 创建训练job参数
     * @return List<Long>         id集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public List<Long> createTrainJobVersion(PtTrainJobCreateDTO ptTrainJobCreateDTO) {

        UserDTO currentUser = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} creates a training job and receives {} as an argument", currentUser.getUsername(), ptTrainJobCreateDTO);

        // 判断当前trainName是否已经存在
        checkTrainName(ptTrainJobCreateDTO.getTrainName(), currentUser.getId());
        // 校验trainParamName是否存在
        if (ptTrainJobCreateDTO.getSaveParams() != null && ptTrainJobCreateDTO.getSaveParams()) {
            checkTrainParamName(ptTrainJobCreateDTO, currentUser.getId());
            // 保存任务参数到数据库
            saveParamToDb(ptTrainJobCreateDTO, currentUser);
        }
        // 获取镜像和算法目录
        PtImageAndAlgorithmVO ptImageAndAlgorithmVO = getPtImageByAlgorithmId(ptTrainJobCreateDTO.getAlgorithmId(),
                currentUser.getId());
        //使用用户创建训练时提供的镜像与运行命令
        String images = imageUtil.getImageUrl(ptTrainJobCreateDTO, currentUser);
        ptImageAndAlgorithmVO.setImageName(trainHarborConfig.getAddress() + StrUtil.SLASH + images).setRunCommand(ptTrainJobCreateDTO.getRunCommand());

        //jobKey
        String trainKey = KeyUtil.generateTrainKey(currentUser.getId());

        //获取规格
        PtTrainJobSpecs ptTrainJobSpecs = new PtTrainJobSpecs();

        ptTrainJobSpecs.setResourcesPoolType(ptTrainJobCreateDTO.getResourcesPoolType());
        ptTrainJobSpecs.setSpecsName(ptTrainJobCreateDTO.getTrainJobSpecsName());
        ptTrainJobSpecs.setSpecsInfo(JSONObject.parseObject(ptTrainJobCreateDTO.getTrainJobSpecsInfo()));

        //版本
        String version = trainJobConfig.getVersionLabel() + String.format(TrainUtil.FOUR_DECIMAL, 1);
        //生成k8s 的job名称
        String jobName = trainKey + trainJobConfig.getSeparator() + version;

        BaseTrainJobDTO baseTrainJobDTO = new BaseTrainJobDTO();
        BeanUtil.copyProperties(ptTrainJobCreateDTO, baseTrainJobDTO);
        baseTrainJobDTO.setJobName(jobName);

        baseTrainJobDTO.setPtTrainJobSpecs(ptTrainJobSpecs);

        //结果集处理
        PtTrainJob ptTrainJob = saveTrainJobTableData(ptTrainJobCreateDTO, currentUser, images, trainKey, baseTrainJobDTO);
        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} creates training job, returns result {}", currentUser.getUsername(), ptTrainJob.getTrainId());

        // 提交job
        asyncManager.execute(baseTrainJobDTO, currentUser, ptImageAndAlgorithmVO, ptTrainJob);
        return Collections.singletonList(ptTrainJob.getTrainId());
    }

    /**
     * 保存训练任务数据
     *
     * @param ptTrainJobCreateDTO 创建训练任务DTO
     * @param currentUser         用户
     * @param imageName           镜像名称
     * @param trainKey            训练key
     * @param baseTrainJobDTO     基础训练参数
     * @return PtTrain            训练
     */
    private PtTrainJob saveTrainJobTableData(PtTrainJobCreateDTO ptTrainJobCreateDTO, UserDTO currentUser,
                                             String imageName, String trainKey, BaseTrainJobDTO baseTrainJobDTO) {
        // 添加train表
        PtTrain ptTrain = new PtTrain();
        ptTrain.setTrainName(ptTrainJobCreateDTO.getTrainName())
                .setTrainKey(trainKey)
                .setCreateUserId(currentUser.getId());
        int trainResult = ptTrainMapper.insert(ptTrain);
        if (trainResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} creates training job, pt Train table insert data failed", currentUser.getUsername());
            throw new BusinessException("内部错误");
        }

        //检查模型是否合法,合法则保存其路径地址
        checkModelAndSavePath(currentUser, baseTrainJobDTO);

        // 添加train_job表
        PtTrainJob ptTrainJob = new PtTrainJob();
        BeanUtil.copyProperties(ptTrainJobCreateDTO, ptTrainJob);
        ptTrainJob.setTrainId(ptTrain.getId())
                .setTrainVersion(trainJobConfig.getVersionLabel().toUpperCase() + String.format(TrainUtil.FOUR_DECIMAL, TrainUtil.NUMBER_ONE))
                .setJobName(baseTrainJobDTO.getJobName())
                .setCreateUserId(currentUser.getId());
        int jobResult = ptTrainJobMapper.insert(ptTrainJob);
        if (jobResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} created training Job, failed to insert data in train_job table", currentUser.getUsername());
            throw new BusinessException("内部错误");
        }

        // 保存job参数
        PtJobParam ptJobParam = new PtJobParam();
        ptJobParam.setTrainJobId(ptTrainJob.getId())
                .setAlgorithmId(ptTrainJobCreateDTO.getAlgorithmId())
                .setRunCommand(ptTrainJobCreateDTO.getRunCommand())
                .setImageName(imageName)
                .setRunParams(ptTrainJobCreateDTO.getRunParams())
                .setCreateUserId(currentUser.getId());
        //保存训练延时启动时间
        if (ptTrainJobCreateDTO.getDelayCreateTime() != null && ptTrainJobCreateDTO.getDelayCreateTime() > 0) {
            ptJobParam.setDelayCreateTime(TrainUtil.getDelayTime(ptTrainJobCreateDTO.getDelayCreateTime()));
        }
        //保存训练自动停止时间
        if (ptTrainJobCreateDTO.getDelayDeleteTime() != null && ptTrainJobCreateDTO.getDelayDeleteTime() > 0) {
            if (ptTrainJobCreateDTO.getDelayCreateTime() != null && ptTrainJobCreateDTO.getDelayCreateTime() > 0) {
                ptJobParam.setDelayDeleteTime(TrainUtil.getDelayTime(ptTrainJobCreateDTO.getDelayCreateTime() + ptTrainJobCreateDTO.getDelayDeleteTime()));
            } else {
                ptJobParam.setDelayDeleteTime(TrainUtil.getDelayTime(ptTrainJobCreateDTO.getDelayDeleteTime()));
            }
        }
        int jobParamResult = ptJobParamMapper.insert(ptJobParam);
        if (jobParamResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} created training job, pT_job_parAM table insert data failed", currentUser.getUsername());
            throw new BusinessException("内部错误");
        }
        return ptTrainJob;
    }

    /**
     * 检查模型是否合法,合法则保存其路径地址
     *
     * @param currentUser         用户
     * @param baseTrainJobDTO     基础训练参数
     */
    private void checkModelAndSavePath(UserDTO currentUser, BaseTrainJobDTO baseTrainJobDTO) {

        Integer modelResource = baseTrainJobDTO.getModelResource();
        if(null == modelResource ) {
            if(null == baseTrainJobDTO.getModelId() &&
                    StringUtils.isBlank(baseTrainJobDTO.getStudentModelIds()) &&
                    StringUtils.isBlank(baseTrainJobDTO.getStudentModelIds())) {
                return;
            } else {
                logErrorInfoOnModel(currentUser.getUsername());
            }
        }
        switch (ModelResourceEnum.getType(modelResource)) {
            case MINE:
                if(null == baseTrainJobDTO.getModelBranchId() || null == baseTrainJobDTO.getModelId() ||
                        StringUtils.isNotBlank(baseTrainJobDTO.getTeacherModelIds()) ||
                        StringUtils.isNotBlank(baseTrainJobDTO.getStudentModelIds())) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                PtModelBranch ptModelBranch = ptModelBranchMapper.selectById(baseTrainJobDTO.getModelBranchId());
                if(null == ptModelBranch || ptModelBranch.getParentId().compareTo(baseTrainJobDTO.getModelId()) != 0 ||
                        StringUtils.isBlank(ptModelBranch.getModelAddress())) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                PtModelInfo ptModelInfo = ptModelInfoMapper.selectById(ptModelBranch.getParentId());
                if(null == ptModelInfo || ptModelInfo.getModelResource().compareTo(baseTrainJobDTO.getModelResource()) != 0){
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                baseTrainJobDTO.setModelPath(adjustmentUrl(ptModelBranch.getModelAddress()));
                break;
            case PRESET:
                if(null == baseTrainJobDTO.getModelId() || StringUtils.isNotBlank(baseTrainJobDTO.getTeacherModelIds()) ||
                        StringUtils.isNotBlank(baseTrainJobDTO.getStudentModelIds())) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                PtModelInfo ptModelInfoPreset = ptModelInfoMapper.selectById(baseTrainJobDTO.getModelId());
                if(null == ptModelInfoPreset || StringUtils.isBlank(ptModelInfoPreset.getUrl()) ||
                        ptModelInfoPreset.getModelResource().compareTo(baseTrainJobDTO.getModelResource()) != 0) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                baseTrainJobDTO.setModelPath(adjustmentUrl(ptModelInfoPreset.getUrl()));
                break;
            case ATLAS:
                if(StringUtils.isBlank(baseTrainJobDTO.getTeacherModelIds()) || null != baseTrainJobDTO.getModelId() ){
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                Set<Long> ids = new HashSet<>();
                Set<Long> teacherModelList = new HashSet<>();
                Arrays.stream(baseTrainJobDTO.getTeacherModelIds().trim().split(SymbolConstant.COMMA))
                        .forEach(id -> teacherModelList.add(Long.parseLong(id)));
                ids.addAll(teacherModelList);

                Set<Long> studentModelList = new HashSet<>();
                if(StringUtils.isNotBlank(baseTrainJobDTO.getStudentModelIds())) {
                    Arrays.stream(baseTrainJobDTO.getStudentModelIds().trim().split(SymbolConstant.COMMA))
                            .forEach(id -> studentModelList.add(Long.parseLong(id)));
                    ids.addAll(studentModelList);
                }
                if(ids.isEmpty()) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                LambdaQueryWrapper<PtModelInfo> query = new LambdaQueryWrapper<>();
                query.eq(PtModelInfo::getModelResource, baseTrainJobDTO.getModelResource())
                        .in(PtModelInfo::getId, ids).isNotNull(PtModelInfo::getUrl).ne(PtModelInfo::getUrl, SymbolConstant.BLANK);
                List<PtModelInfo> modelInfoList = ptModelInfoMapper.selectList(query);
                if(null == modelInfoList || modelInfoList.size() < ids.size()) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }

                //保存炼知教师模型路径地址
                baseTrainJobDTO.setTeacherModelPathList(new ArrayList<>());
                modelInfoList.stream()
                        .filter(modelInfo -> teacherModelList.contains(modelInfo.getId()))
                        .forEach(modelInfo -> baseTrainJobDTO.getTeacherModelPathList().add(adjustmentUrl(modelInfo.getUrl())));

                //保存炼知学生模型路径地址
                if(!studentModelList.isEmpty()) {
                    baseTrainJobDTO.setStudentModelPathList(new ArrayList<>());
                    modelInfoList.stream()
                            .filter(modelInfo -> studentModelList.contains(modelInfo.getId()))
                            .forEach(modelInfo -> baseTrainJobDTO.getStudentModelPathList().add(adjustmentUrl(modelInfo.getUrl())));
                }
                break;
        }
    }



    /**
     * 调整模型地址
     *
     * @param modelUrl  模型地址
     * @return
     */
    private String adjustmentUrl(String modelUrl) {
        if(modelUrl.endsWith(SymbolConstant.SLASH)) {
            modelUrl = modelUrl.substring(TrainUtil.NUMBER_ZERO, modelUrl.length() - TrainUtil.NUMBER_ONE);
        }
        return modelUrl;
    }

    /**
     * 打印训练任务中模型相关的错误日志
     *
     * @param username
     */
    private void logErrorInfoOnModel(String username){
        LogUtil.error(LogEnum.BIZ_TRAIN, "User {} operating training job, error on model......", username);
        throw new BusinessException("模型参数参数不合法");
    }

    /**
     * 保存任务参数到数据库
     *
     * @param ptTrainJobCreateDTO 创建训练任务DTO
     * @param currentUser         用户
     */
    private void saveParamToDb(PtTrainJobCreateDTO ptTrainJobCreateDTO, UserDTO currentUser) {
        PtTrainParam ptTrainParam = new PtTrainParam();
        BeanUtil.copyProperties(ptTrainJobCreateDTO, ptTrainParam);
        //获取镜像url
        String images = imageUtil.getImageUrl(ptTrainJobCreateDTO, currentUser);
        ptTrainParam.setImageName(images);
        ptTrainParam.setParamName(ptTrainJobCreateDTO.getTrainParamName())
                .setDescription(ptTrainJobCreateDTO.getTrainParamDesc())
                .setRunParams(ptTrainJobCreateDTO.getRunParams())
                .setCreateUserId(currentUser.getId());
        int trainParamResult = ptTrainParamMapper.insert(ptTrainParam);
        if (trainParamResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} created training job, pT_param_param table failed to insert data", currentUser.getUsername());
            throw new BusinessException("内部错误");
        }
    }

    /**
     * 获取训练作业规格
     *
     * @param trainJobSpecsId  规格类型Id
     * @param currentUser      用户
     * @return PtTrainJobSpecs 训练作业规格
     */
    private PtTrainJobSpecs getSpecs(Integer trainJobSpecsId, UserDTO currentUser) {

        PtTrainJobSpecs ptTrainJobSpecs = ptTrainJobSpecsMapper.selectById(trainJobSpecsId);
        if (ptTrainJobSpecs == null) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The PT_train_job_specs table specification type Id for the user {} query does not exist", currentUser.getUsername(), trainJobSpecsId);
            throw new BusinessException("内部错误");
        }
        return ptTrainJobSpecs;
    }

    /**
     * 获取镜像和算法目录
     *
     * @param algorithmId            算法ID
     * @param userId                 用户ID
     * @return PtImageAndAlgorithmVO 镜像
     */
    private PtImageAndAlgorithmVO getPtImageByAlgorithmId(Long algorithmId, Long userId) {

        PtTrainAlgorithm ptTrainAlgorithm = ptTrainAlgorithmMapper.selectById(algorithmId);
        if (null == ptTrainAlgorithm || StringUtils.isBlank(ptTrainAlgorithm.getCodeDir())) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The record with algorithm training ID {} has no corresponding image or algorithm directory configuration", algorithmId);
            throw new BusinessException(ResponseCode.ERROR, "该id的记录没有相应的镜像或者算法目录配置");
        }

        if (!AlgorithmStatusEnum.SUCCESS.getCode().equals(ptTrainAlgorithm.getAlgorithmStatus())) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The algorithm ID {} algorithmStatus is{} unusual", algorithmId, ptTrainAlgorithm.getAlgorithmStatus());
            throw new BusinessException(ResponseCode.ERROR, "该算法状态异常!");
        }

        if (!(userId.equals(ptTrainAlgorithm.getCreateUserId()) || AlgorithmSourceEnum.PRE.getStatus().equals(ptTrainAlgorithm.getAlgorithmSource()))) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The data {} does not belong to the user {}!", ptTrainAlgorithm, userId);
            throw new BusinessException(ResponseCode.ERROR, "该数据不属于该用户!");
        }

        PtImageAndAlgorithmVO ptImageAndAlgorithmVO = new PtImageAndAlgorithmVO();
        BeanUtil.copyProperties(ptTrainAlgorithm, ptImageAndAlgorithmVO);

        return ptImageAndAlgorithmVO;
    }

    /**
     * 校验trainName
     *
     * @param trainName 校验trainName
     * @param userId    返回用户ID
     */
    private void checkTrainName(String trainName, Long userId) {
        QueryWrapper<PtTrain> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("train_name", trainName)
                .eq("create_user_id", userId);
        Integer ptTrainCountResult = ptTrainMapper.selectCount(queryWrapper);
        if (ptTrainCountResult > 0) {
            throw new BusinessException("当前训练名称已经存在");
        }
    }

    /**
     * 校验trainParamName
     *
     * @param ptTrainJobCreateDTO 创建训练DTO
     * @param userId              用户ID
     */
    private void checkTrainParamName(PtTrainJobCreateDTO ptTrainJobCreateDTO, Long userId) {

        String trainParamName = ptTrainJobCreateDTO.getTrainParamName();

        QueryWrapper<PtTrainParam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("param_name", trainParamName)
                .eq("create_user_id", userId);
        Integer ptTrainParamCountResult = ptTrainParamMapper.selectCount(queryWrapper);
        if (ptTrainParamCountResult > 0) {
            throw new BusinessException("当前trainParamName已经存在");
        }
    }

    /**
     * 生成k8s 的job名称
     *
     * @param ptTrain 训练
     * @return String 版本
     */
    private String buildVersion(PtTrain ptTrain) {
        return ptTrain.getTrainKey() + trainJobConfig.getSeparator() + trainJobConfig.getVersionLabel() + String.format(TrainUtil.FOUR_DECIMAL, ptTrain.getTotalNum() + 1);
    }

    /**
     * 修改训练job
     *
     * @param ptTrainJobUpdateDTO   修改训练job参数
     * @return List<Long>           id集合
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public List<Long> updateTrainJob(PtTrainJobUpdateDTO ptTrainJobUpdateDTO) {

        UserDTO currentUser = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} modifies the training job and receives the parameter {}", ptTrainJobUpdateDTO);

        PtTrainJob existPtTrainJob = ptTrainJobMapper.selectById(ptTrainJobUpdateDTO.getId());
        if (null == existPtTrainJob || !currentUser.getId().equals(existPtTrainJob.getCreateUserId())) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "It is illegal for a user {} to modify a training job, jobId, to {}", currentUser.getUsername(), ptTrainJobUpdateDTO.getId());
            throw new BusinessException(ResponseCode.SUCCESS, "您输入的id不存在或已被删除");
        }
        //获取算法
        PtImageAndAlgorithmVO ptImageAndAlgorithmVO = getPtImageByAlgorithmId(ptTrainJobUpdateDTO.getAlgorithmId(), currentUser.getId());
        //使用用户修改训练时提供的镜像与运行命令
        //获取镜像url
        String images = imageUtil.getImageUrl(ptTrainJobUpdateDTO, currentUser);
        ptImageAndAlgorithmVO.setImageName(trainHarborConfig.getAddress() + StrUtil.SLASH + images).setRunCommand(ptTrainJobUpdateDTO.getRunCommand());

        //获取规格
        PtTrainJobSpecs ptTrainJobSpecs = new PtTrainJobSpecs();
        ptTrainJobSpecs.setResourcesPoolType(ptTrainJobUpdateDTO.getResourcesPoolType());
        ptTrainJobSpecs.setSpecsName(ptTrainJobUpdateDTO.getTrainJobSpecsName());
        ptTrainJobSpecs.setSpecsInfo(JSONObject.parseObject(ptTrainJobUpdateDTO.getTrainJobSpecsInfo()));

        PtTrain ptTrain = ptTrainMapper.selectById(existPtTrainJob.getTrainId());

        String jobName = buildVersion(ptTrain);

        BaseTrainJobDTO baseTrainJobDTO = new BaseTrainJobDTO();
        BeanUtil.copyProperties(ptTrainJobUpdateDTO, baseTrainJobDTO);
        baseTrainJobDTO.setJobName(jobName);
        baseTrainJobDTO.setPtTrainJobSpecs(ptTrainJobSpecs);
        //结果集处理
        PtTrainJob ptTrainJob = updateTrainJobTableData(ptTrainJobUpdateDTO, currentUser, existPtTrainJob, images, ptTrain, baseTrainJobDTO);
        //提交job
        asyncManager.execute(baseTrainJobDTO, currentUser, ptImageAndAlgorithmVO, ptTrainJob);

        return Collections.singletonList(ptTrainJob.getId());
    }

    /**
     * 结果集处理
     *
     * @param ptTrainJobUpdateDTO 更新训练任务DTO
     * @param currentUser         当前用户
     * @param existPtTrainJob     存在的训练任务
     * @param imageName           镜像名称
     * @param ptTrain             训练
     * @param baseTrainJobDTO     基本训练信息
     * @return PtTrainJob         训练任务
     */
    private PtTrainJob updateTrainJobTableData(PtTrainJobUpdateDTO ptTrainJobUpdateDTO, UserDTO
            currentUser, PtTrainJob existPtTrainJob, String imageName, PtTrain ptTrain, BaseTrainJobDTO baseTrainJobDTO) {

        //检查模型是否合法,合法则保存其路径地址
        checkModelAndSavePath(currentUser, baseTrainJobDTO);

        //添加train_job表
        PtTrainJob ptTrainJob = new PtTrainJob();
        BeanUtil.copyProperties(ptTrainJobUpdateDTO, ptTrainJob);
        ptTrainJob.setTrainId(ptTrain.getId()).setTrainVersion(trainJobConfig.getVersionLabel().toUpperCase() + String.format(TrainUtil.FOUR_DECIMAL, ptTrain.getTotalNum() + 1))
                .setJobName(baseTrainJobDTO.getJobName()).setParentTrainVersion(existPtTrainJob.getTrainVersion())
                .setCreateUserId(currentUser.getId());
        int jobResult = ptTrainJobMapper.insert(ptTrainJob);
        if (jobResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} created training Job, failed to insert data in train_job table", currentUser.getUsername());
            throw new BusinessException("内部错误");
        }

        //保存job参数
        PtJobParam ptJobParam = new PtJobParam();
        ptJobParam.setTrainJobId(ptTrainJob.getId())
                .setAlgorithmId(ptTrainJobUpdateDTO.getAlgorithmId())
                .setRunCommand(ptTrainJobUpdateDTO.getRunCommand())
                .setImageName(imageName)
                .setRunParams(ptTrainJobUpdateDTO.getRunParams())
                .setCreateUserId(currentUser.getId());
        //保存训练延时启动时间
        if (ptTrainJobUpdateDTO.getDelayCreateTime() != null && ptTrainJobUpdateDTO.getDelayCreateTime() > 0) {
            ptJobParam.setDelayCreateTime(TrainUtil.getDelayTime(ptTrainJobUpdateDTO.getDelayCreateTime()));
        }
        //保存训练自动停止时间
        if (ptTrainJobUpdateDTO.getDelayDeleteTime() != null && ptTrainJobUpdateDTO.getDelayDeleteTime() > 0) {
            if (ptTrainJobUpdateDTO.getDelayCreateTime() != null && ptTrainJobUpdateDTO.getDelayCreateTime() > 0) {
                ptJobParam.setDelayDeleteTime(TrainUtil.getDelayTime(ptTrainJobUpdateDTO.getDelayCreateTime() + ptTrainJobUpdateDTO.getDelayDeleteTime()));
            } else {
                ptJobParam.setDelayDeleteTime(TrainUtil.getDelayTime(ptTrainJobUpdateDTO.getDelayDeleteTime()));
            }
        }
        int jobParamResult = ptJobParamMapper.insert(ptJobParam);
        if (jobParamResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} created training job, pT_job_parAM table insert data failed", currentUser.getUsername());
            throw new BusinessException("内部错误");
        }

        //更新pt_train
        PtTrain updatePtTrain = new PtTrain();
        updatePtTrain.setId(ptTrain.getId()).setVersionNum(ptTrain.getVersionNum() + 1)
                .setTotalNum(ptTrain.getTotalNum() + 1).setUpdateUserId(currentUser.getId());
        int updateResult = ptTrainMapper.updateById(updatePtTrain);
        if (updateResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} created training job, pT_train table failed to update version number", currentUser.getUsername());
            throw new BusinessException("内部错误");
        }
        return ptTrainJob;
    }

    /**
     * 删除训练job
     *
     * @param ptTrainJobDeleteDTO 删除训练job参数
     * @return PtTrainJobDeleteVO 删除训练任务结果
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public PtTrainJobDeleteVO deleteTrainJob(PtTrainJobDeleteDTO ptTrainJobDeleteDTO) {
        UserDTO currentUser = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} deletes the training job and receives the parameter {}", currentUser.getUsername(), ptTrainJobDeleteDTO);

        List<PtTrainJob> jobList = new ArrayList<>();
        PtTrain ptTrain = checkAndReturnPtTrain(ptTrainJobDeleteDTO, currentUser, jobList);

        Collection<Long> jobIdList = new ArrayList<>();
        if (null != ptTrainJobDeleteDTO.getId()) {

            //要删除的训练任务
            PtTrainJob ptTrainJob = ptTrainJobMapper.selectById(ptTrainJobDeleteDTO.getId());
            if (ptTrainJob == null) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted training job, pT_train_job table failed to delete data", currentUser.getUsername());
                throw new BusinessException(ResponseCode.SUCCESS, "训练任务已删除或参数不合法");
            }
            //删除job
            deleteJobs(currentUser, jobList);
            ptTrainJobMapper.deleteById(ptTrainJobDeleteDTO.getId());

            PtTrain updatePtTrain = new PtTrain();
            updatePtTrain.setVersionNum(ptTrain.getVersionNum() - 1);
            UpdateWrapper<PtTrain> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", ptTrain.getId()).eq("version_num", ptTrain.getVersionNum());
            int updateResult = ptTrainMapper.update(updatePtTrain, updateWrapper);
            if (updateResult < 1) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted the training job and updated the version_num in the PT_train table failed", currentUser.getUsername());
                throw new BusinessException(ResponseCode.SUCCESS, "训练任务已删除或参数不合法");
            }

            if (ptTrain.getVersionNum() == 1) {
                int trainResult = ptTrainMapper.deleteById(ptTrain.getId());
                if (trainResult < 1) {
                    LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted training job, pt Train table deleted data failed", currentUser.getUsername());
                    throw new BusinessException(ResponseCode.SUCCESS, "训练任务已删除或参数不合法");
                }
            }
            jobIdList.add(ptTrainJobDeleteDTO.getId());

            //回收已删除训练任务无效文件
            String recyclePath = nfsUtil.formatPath(nfsConfig.getRootDir() + nfsConfig.getBucket() + trainJobConfig.getManage() +
                    File.separator + ptTrainJob.getCreateUserId() + File.separator + ptTrainJob.getJobName());
            recycleTaskWithTrain(recyclePath);

        } else {
            deleteTrainAndJob(ptTrainJobDeleteDTO, currentUser, jobList, ptTrain, jobIdList);

        }

        //删除pt_job_param表中相关数据
        UpdateWrapper<PtJobParam> updateJobParamWrapper = new UpdateWrapper<>();
        updateJobParamWrapper.in("train_job_id", jobIdList);
        int jobParamResult = ptJobParamMapper.delete(updateJobParamWrapper);
        if (jobParamResult < jobIdList.size()) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted training job, pT_job_param table failed to delete data", currentUser.getUsername());
            throw new BusinessException("内部错误");
        }

        PtTrainJobDeleteVO ptTrainJobDeleteVO = new PtTrainJobDeleteVO();
        BeanUtil.copyProperties(ptTrainJobDeleteDTO, ptTrainJobDeleteVO);
        LogUtil.info(LogEnum.BIZ_TRAIN, "Deleting the training job completes and the result returned is {}", ptTrainJobDeleteVO);
        return ptTrainJobDeleteVO;
    }

    /**
     * 删除训练和任务
     *
     * @param ptTrainJobDeleteDTO 删除训练任务DTO
     * @param currentUser         用户
     * @param jobList             任务集合
     * @param ptTrain             训练
     * @param jobIdList           任务ID集合
     */
    private void deleteTrainAndJob(PtTrainJobDeleteDTO ptTrainJobDeleteDTO, UserDTO
            currentUser, List<PtTrainJob> jobList, PtTrain ptTrain, Collection<Long> jobIdList) {
        QueryWrapper<PtTrainJob> query = new QueryWrapper<>();
        query.eq("train_id", ptTrainJobDeleteDTO.getTrainId());
        List<PtTrainJob> ptTrainJobs = ptTrainJobMapper.selectList(query);
        if (ptTrainJobs.size() < 1) {
            throw new BusinessException(ResponseCode.SUCCESS, "没有待删除的训练任务");
        }
        ptTrainJobs.forEach(x -> {
            jobList.add(x);
            jobIdList.add(x.getId());
        });

        //删除job
        deleteJobs(currentUser, jobList);

        PtTrain updatePtTrain = new PtTrain();
        updatePtTrain.setVersionNum(0);
        UpdateWrapper<PtTrain> updateTrainWrapper = new UpdateWrapper<>();
        updateTrainWrapper.eq("id", ptTrain.getId()).eq("version_num", ptTrain.getVersionNum());
        int updateResult = ptTrainMapper.update(updatePtTrain, updateTrainWrapper);
        if (updateResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted the training job and updated the version_num in the PT_train table failed", currentUser.getUsername());
            throw new BusinessException("内部错误");
        }

        int trainResult = ptTrainMapper.deleteById(ptTrain.getId());
        if (trainResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted training job, pt Train table deleted data failed", currentUser.getUsername());
            throw new BusinessException("内部错误");
        }

        UpdateWrapper<PtTrainJob> updateJobWrapper = new UpdateWrapper<>();
        updateJobWrapper.eq("train_id", ptTrain.getId());
        int jobResult = ptTrainJobMapper.delete(updateJobWrapper);
        if (jobResult < jobIdList.size()) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted training job, pT_train_job table failed to delete data", currentUser.getUsername());
            throw new BusinessException("内部错误");
        }

        //回收已删除训练任务无效文件
        for (PtTrainJob trainJob : ptTrainJobs) {
            String recyclePath = nfsUtil.formatPath(nfsConfig.getRootDir() + nfsConfig.getBucket() + trainJobConfig.getManage() +
                    StrUtil.SLASH + trainJob.getCreateUserId() + StrUtil.SLASH + trainJob.getJobName());
            recycleTaskWithTrain(recyclePath);
        }
    }

    /**
     * 校验并返回PtTrain
     *
     * @param ptTrainJobDeleteDTO 删除训练任务DTO
     * @param currentUser         用户
     * @param jobList             任务集合
     * @return PtTrain            训练
     */
    private PtTrain checkAndReturnPtTrain(PtTrainJobDeleteDTO ptTrainJobDeleteDTO, UserDTO currentUser, List<PtTrainJob> jobList) {
        PtTrain ptTrain = ptTrainMapper.selectById(ptTrainJobDeleteDTO.getTrainId());
        if (null == ptTrain || !ptTrain.getCreateUserId().equals(currentUser.getId())) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} failed to delete training job, invalid parameter, as follows {}", currentUser.getUsername(), ptTrainJobDeleteDTO);
            throw new BusinessException(ResponseCode.SUCCESS, "训练任务已删除或参数不合法");
        }

        if (null != ptTrainJobDeleteDTO.getId()) {
            PtTrainJob ptTrainJob = ptTrainJobMapper.selectById(ptTrainJobDeleteDTO.getId());
            if (null == ptTrainJob || !ptTrainJob.getTrainId().equals(ptTrainJobDeleteDTO.getTrainId())) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "User {} failed to delete training job, invalid parameter, as follows {}", currentUser.getUsername(), ptTrainJobDeleteDTO);
                throw new BusinessException(ResponseCode.SUCCESS, "训练任务已删除或参数不合法");
            }
            jobList.add(ptTrainJob);
        }
        return ptTrain;
    }

    /**
     * 检测停止训练任务
     *
     * @param ptTrainJobStopDTO 停止训练DTO
     * @param currentUser       用户
     * @param jobList           任务集合
     */
    private void checkAndReturnPtTrain(PtTrainJobStopDTO ptTrainJobStopDTO, UserDTO currentUser, List<PtTrainJob> jobList) {
        PtTrain ptTrain = ptTrainMapper.selectById(ptTrainJobStopDTO.getTrainId());
        if (null == ptTrain || !ptTrain.getCreateUserId().equals(currentUser.getId())) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} stopped the training job failed, the parameter is illegal, the training does not exist, as follows {}", currentUser.getUsername(), ptTrainJobStopDTO);
            throw new BusinessException(ResponseCode.SUCCESS, "参数不合法，该训练不存在");
        }

        if (null != ptTrainJobStopDTO.getId()) {
            PtTrainJob ptTrainJob = ptTrainJobMapper.selectById(ptTrainJobStopDTO.getId());
            if (null == ptTrainJob || !ptTrainJob.getTrainId().equals(ptTrainJobStopDTO.getTrainId()) ||
                    TrainJobStatusEnum.checkStopStatus(ptTrainJob.getTrainStatus())) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "User {} stopped training job failed, invalid parameter, as follows {}", currentUser.getUsername(), ptTrainJobStopDTO);
                throw new BusinessException(ResponseCode.SUCCESS, "参数不合法，该训练不存在");
            }
            jobList.add(ptTrainJob);
        }
    }

    /**
     * 删除任务
     *
     * @param currentUser 用户
     * @param jobList     任务集合
     */
    private void deleteJobs(UserDTO currentUser, List<PtTrainJob> jobList) {
        String namespace = k8sNameTool.generateNamespace(currentUser.getId());
        try {
            for (PtTrainJob job : jobList) {
                if (TrainJobStatusEnum.STOP.getStatus().equals(job.getTrainStatus())) {
                    boolean bool = TrainTypeEnum.isDistributeTrain(job.getTrainType()) ?
                            distributeTrainApi.deleteByResourceName(namespace, job.getJobName()).isSuccess() :
                            trainJobApi.delete(namespace, job.getJobName());
                    if (!bool) {
                        LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deletes the training job and K8s fails to execute the delete() method, namespace为{}, resourceName为{}",
                                currentUser.getUsername(), namespace, job.getJobName());
                    }
                }
                PtBaseResult ptBaseResult = persistentVolumeClaimApi.recycle(namespace, job.getJobName());
                if (null == ptBaseResult || !ptBaseResult.isSuccess()) {
                    LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted training job, k8s failed to implement the recycle() method, namespace为{}, resourceName为{}",
                            currentUser.getUsername(), namespace, job.getJobName());
                }
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} delete training job, k8s delete failed,exception:{}", e);
            throw new BusinessException("内部错误");
        }
    }

    /**
     * 停止训练job
     *
     * @param ptTrainJobStopDTO 停止训练job参数
     * @return PtTrainJobStopVO 停止训练任务结果
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public PtTrainJobStopVO stopTrainJob(PtTrainJobStopDTO ptTrainJobStopDTO) {
        UserDTO currentUser = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} stops training Job and receives the parameter {}", currentUser.getUsername(), ptTrainJobStopDTO);
        //训练job名称集合 用于停止k8s
        List<PtTrainJob> jobList = new ArrayList<>();
        checkAndReturnPtTrain(ptTrainJobStopDTO, currentUser, jobList);

        if (null != ptTrainJobStopDTO.getId()) {
            //停止job
            stopTrainJobAsync.stopJobs(currentUser, jobList);

        } else if (null != ptTrainJobStopDTO.getTrainId()) {
            QueryWrapper<PtTrainJob> queryTrainJonWrapper = new QueryWrapper<>();
            queryTrainJonWrapper.eq("train_id", ptTrainJobStopDTO.getTrainId());
            List<PtTrainJob> trainJobs = ptTrainJobMapper.selectList(queryTrainJonWrapper);

            for (PtTrainJob trainJob : trainJobs) {
                if (!TrainJobStatusEnum.checkStopStatus(trainJob.getTrainStatus())) {
                    jobList.add(trainJob);
                }
            }

            if (jobList.size() < 1) {
                throw new BusinessException(ResponseCode.SUCCESS, "没有待停止的job");
            }

            //停止job
            stopTrainJobAsync.stopJobs(currentUser, jobList);
        }

        PtTrainJobStopVO ptTrainJobStopVO = new PtTrainJobStopVO();
        ptTrainJobStopVO.setTrainId(ptTrainJobStopDTO.getTrainId());
        ptTrainJobStopVO.setId(ptTrainJobStopDTO.getId());
        return ptTrainJobStopVO;
    }

    /**
     * 任务统计
     *
     * @return PtTrainJobStatisticsMineVO  我的训练任务统计结果
     **/
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public PtTrainJobStatisticsMineVO statisticsMine() {
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        // 获取运行中的任务
        Integer runCount = ptTrainJobMapper.selectCountByStatus(userDTO.getId(),
                SqlUtil.integerlistToString(new Integer[]{TrainJobStatusEnum.RUNNING.getStatus()}));

        // 已经完成的任务
        Integer finishCount = ptTrainJobMapper.selectCountByStatus(userDTO.getId(),
                SqlUtil.integerlistToString(
                        new Integer[]{TrainJobStatusEnum.FAILED.getStatus(), TrainJobStatusEnum.STOP.getStatus(),
                                TrainJobStatusEnum.SUCCEEDED.getStatus(), TrainJobStatusEnum.UNKNOWN.getStatus()}));

        PtTrainJobStatisticsMineVO vo = new PtTrainJobStatisticsMineVO();
        vo.setRunJobCount(runCount);
        vo.setFinishJobCount(finishCount);
        return vo;
    }

    /**
     * 查询训练作业job状态
     *
     * @param ptTrainDataSourceStatusQueryDTO 查询训练作业job状态参数
     * @return HashedMap<String, Boolean>     数据集路径-是否可以删除 的map集合
     **/
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Boolean> getTrainDataSourceStatus(PtTrainDataSourceStatusQueryDTO ptTrainDataSourceStatusQueryDTO) {
        UserDTO currentUser = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "The user {} queries the state of the dataset starting with the received parameter {}", currentUser.getUsername(), ptTrainDataSourceStatusQueryDTO);
        if (CollectionUtils.isEmpty(ptTrainDataSourceStatusQueryDTO.getDataSourcePath())) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The dataset set {} is empty", ptTrainDataSourceStatusQueryDTO.getDataSourcePath());
            throw new BusinessException("传入参数为空，请重新输入");
        }
        //去重
        List<String> dataSourceList = ptTrainDataSourceStatusQueryDTO.getDataSourcePath().stream().distinct().collect(Collectors.toList());
        QueryWrapper<PtTrainJob> query = new QueryWrapper<>();
        query.in("data_source_path", dataSourceList);
        List<PtTrainJob> ptTrainJobs = ptTrainJobMapper.selectList(query);
        //结果集处理
        List<PtTrainDataSourceStatusQueryVO> ptTrainDataSourceStatusQueryList = ptTrainJobs.stream().map(x -> {
            PtTrainDataSourceStatusQueryVO ptTrainDataSourceStatusQuery = new PtTrainDataSourceStatusQueryVO();
            ptTrainDataSourceStatusQuery.setDataSourcePath(x.getDataSourcePath());
            ptTrainDataSourceStatusQuery.setStatus(x.getTrainStatus() >= TrainJobStatusEnum.SUCCEEDED.getStatus());
            return ptTrainDataSourceStatusQuery;
        }).distinct().collect(Collectors.toList());
        //结果去重
        HashMap<String, Boolean> map = new HashMap<>();
        for (PtTrainDataSourceStatusQueryVO ptTrainDataSourceStatusQuery : ptTrainDataSourceStatusQueryList) {
            if (map.containsKey(ptTrainDataSourceStatusQuery.getDataSourcePath())) {
                if (!ptTrainDataSourceStatusQuery.getStatus()) {
                    map.put(ptTrainDataSourceStatusQuery.getDataSourcePath(), ptTrainDataSourceStatusQuery.getStatus());
                }
            } else {
                map.put(ptTrainDataSourceStatusQuery.getDataSourcePath(), ptTrainDataSourceStatusQuery.getStatus());
            }
        }
        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} query data set state ends, return result is {}", currentUser.getUsername(), map);
        return map;
    }

    /**
     * 根据jobId查询训练任务详情查询
     *
     * @param ptTrainJobDetailQueryDTO 根据jobId查询训练任务详情查询条件
     * @return PtTrainQueryJobDetailVO 根据jobId查询训练任务详情返回结果
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public PtTrainJobDetailQueryVO getTrainJobDetail(PtTrainJobDetailQueryDTO ptTrainJobDetailQueryDTO) {
        //从会话中获取用户信息
        UserDTO user = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "The user {} starts by querying jobId={} for training task details", user.getUsername(), ptTrainJobDetailQueryDTO.getId());
        //获取训练job参数
        QueryWrapper<PtTrainJob> trainJobQuery = new QueryWrapper<>();
        trainJobQuery.eq("create_user_id", user.getId());
        trainJobQuery.eq("id", ptTrainJobDetailQueryDTO.getId());
        PtTrainJob ptTrainJob = ptTrainJobMapper.selectOne(trainJobQuery);
        if (ptTrainJob == null) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The jobId for the user {} query does not exist", user.getUsername());
            throw new BusinessException(ResponseCode.SUCCESS, "您查询的id不存在或已被删除");
        }
        //获取训练参数
        PtTrain ptTrain = ptTrainMapper.selectById(ptTrainJob.getTrainId());
        //获取训练任务参数
        QueryWrapper<PtJobParam> jobParamQuery = new QueryWrapper<>();
        jobParamQuery.eq("train_job_id", ptTrainJob.getId());
        PtJobParam ptJobParam = ptJobParamMapper.selectOne(jobParamQuery);
        if (ptJobParam == null || ptJobParam.getAlgorithmId() < MagicNumConstant.ONE) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The algorithm ID corresponding to the jobId={} query by the user {} does not exist", user.getUsername(), ptTrainJobDetailQueryDTO.getId());
            throw new BusinessException(ResponseCode.SUCCESS, "您查询的jobId对应的算法id不存在或已被删除");
        }
        //获取算法参数
        PtTrainAlgorithm ptTrainAlgorithm = ptTrainAlgorithmMapper.selectAllById(ptJobParam.getAlgorithmId());
        //结果集处理
        PtTrainJobDetailQueryVO ptTrainJobDetailQueryVO = new PtTrainJobDetailQueryVO();
        BeanUtils.copyProperties(ptTrainJob, ptTrainJobDetailQueryVO);
        ptTrainJobDetailQueryVO.setTrainName(ptTrain.getTrainName()).setAlgorithmId(ptJobParam.getAlgorithmId()).setRunCommand(ptJobParam.getRunCommand())
                .setRunParams(ptJobParam.getRunParams()).setParamF1(ptJobParam.getParamF1()).setParamCallback(ptJobParam.getParamCallback())
                .setParamPrecise(ptJobParam.getParamPrecise()).setParamAccuracy(ptJobParam.getParamAccuracy());
        long nowTime = System.currentTimeMillis();
        //获取训练延时启动倒计时（分钟）
        if (ptJobParam.getDelayCreateTime() != null && nowTime < ptJobParam.getDelayCreateTime().getTime() && TrainJobStatusEnum.checkRunStatus(ptTrainJob.getTrainStatus())) {
            ptTrainJobDetailQueryVO.setDelayCreateCountDown(TrainUtil.getCountDown(ptJobParam.getDelayCreateTime().getTime()));
        }
        //获取训练自动停止倒计时（分钟）
        if (ptJobParam.getDelayDeleteTime() != null && nowTime < ptJobParam.getDelayDeleteTime().getTime() && TrainJobStatusEnum.checkRunStatus(ptTrainJob.getTrainStatus())) {
            ptTrainJobDetailQueryVO.setDelayDeleteCountDown(TrainUtil.getCountDown(ptJobParam.getDelayDeleteTime().getTime()));
        }
        //拼装镜像信息
        if (StringUtils.isNotBlank(ptJobParam.getImageName())) {
            String imageNameSuffix = ptJobParam.getImageName().substring(ptJobParam.getImageName().lastIndexOf(StrUtil.SLASH) + MagicNumConstant.ONE);
            String[] imageNameSuffixArray = imageNameSuffix.split(StrUtil.COLON);
            ptTrainJobDetailQueryVO.setImageName(imageNameSuffixArray[0]);
            ptTrainJobDetailQueryVO.setImageTag(imageNameSuffixArray[1]);
        }
        //拼装算法信息
        if (ptTrainAlgorithm != null) {
            ptTrainJobDetailQueryVO.setAlgorithmName(ptTrainAlgorithm.getAlgorithmName())
                    .setAlgorithmSource(ptTrainAlgorithm.getAlgorithmSource())
                    .setAlgorithmUsage(ptTrainAlgorithm.getAlgorithmUsage())
                    .setAccuracy(ptTrainAlgorithm.getAccuracy())
                    .setP4InferenceSpeed(ptTrainAlgorithm.getP4InferenceSpeed());
            if (ptTrainAlgorithm.getAlgorithmSource() == MagicNumConstant.ONE) {
                ptTrainJobDetailQueryVO.setAlgorithmCodeDir(ptTrainAlgorithm.getCodeDir());
            }
        }

        return ptTrainJobDetailQueryVO;
    }

    /**
     * 恢复训练
     *
     * @param ptTrainJobResumeDTO 恢复训练请求参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public void resumeTrainJob(PtTrainJobResumeDTO ptTrainJobResumeDTO) {
        //从会话中获取用户信息
        UserDTO currentUser = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "user {} according to jobId={} started to check the training task details", currentUser.getUsername(), ptTrainJobResumeDTO.getId());
        // 检查jobId是否合法
        PtTrainJob ptTrainJob = ptTrainJobMapper.selectById(ptTrainJobResumeDTO.getId());
        if (null == ptTrainJob || !currentUser.getId().equals(ptTrainJob.getCreateUserId())) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "It is illegal for user {} to resume training job and jobId to be {}", currentUser.getUsername(), ptTrainJobResumeDTO.getId());
            throw new BusinessException(ResponseCode.SUCCESS, "您输入的id不存在或已被删除，请重新输入");
        }
        // 获取算法id和运行参数
        QueryWrapper<PtJobParam> jobParamQuery = new QueryWrapper<>();
        jobParamQuery.eq("train_job_id", ptTrainJob.getId());
        PtJobParam ptJobParam = ptJobParamMapper.selectOne(jobParamQuery);
        if (ptJobParam == null || ptJobParam.getAlgorithmId() < MagicNumConstant.ONE) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The algorithm ID corresponding to the jobId={} query by the user {} does not exist", currentUser.getUsername(), ptTrainJobResumeDTO.getId());
            throw new BusinessException(ResponseCode.SUCCESS, "您查询的jobId对应的算法id不存在");
        }
        //获取镜像
        PtImageAndAlgorithmVO ptImageAndAlgorithmVO = getPtImageByAlgorithmId(ptJobParam.getAlgorithmId(), currentUser.getId());
        //使用用户训练时提供的镜像与运行命令
        ptImageAndAlgorithmVO.setImageName(trainHarborConfig.getAddress() + StrUtil.SLASH + ptJobParam.getImageName()).setRunCommand(ptJobParam.getRunCommand());
        String[] codeDirResult = ptImageAndAlgorithmVO.getCodeDir().split(StrUtil.SLASH);
        String codeDirName = codeDirResult[codeDirResult.length - 1];

        //处理目录问题
        String noEnvPath = StrUtil.SLASH + trainJobConfig.getManage() + StrUtil.SLASH + currentUser.getId() + StrUtil.SLASH
                + ptTrainJob.getJobName();
        String commonPath = nfsConfig.getBucket() + noEnvPath.substring(1);
        String outPath = commonPath + StrUtil.SLASH + trainJobConfig.getOutPath();
        String loadPath = commonPath + StrUtil.SLASH + trainJobConfig.getLoadPath();
        String codePath = commonPath + StrUtil.SLASH + codeDirName;
        String noEnvOut = noEnvPath + StrUtil.SLASH + trainJobConfig.getOutPath();
        String path = ptTrainJobResumeDTO.getPath();
        if (!path.startsWith(noEnvOut)) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "path: {}", path);
            throw new BusinessException("内部错误");
        }
        String modelLoadDir = path.substring(noEnvOut.length());
        FileUtil.del(nfsConfig.getRootDir() + loadPath);
        FileUtil.del(nfsConfig.getRootDir() + codePath);
        FileUtil.rename(new File(nfsConfig.getRootDir() + outPath), nfsConfig.getRootDir() + loadPath, false, true);

        //获取训练规格信息
        PtTrainJobSpecs ptTrainJobSpecs = new PtTrainJobSpecs();
        List<DictDetail> dictDetails = dictDetailMapper.selectList(new LambdaQueryWrapper<DictDetail>().eq(DictDetail::getLabel, ptTrainJob.getTrainJobSpecsName()));
        ptTrainJobSpecs.setResourcesPoolType(ptTrainJob.getResourcesPoolType());
        ptTrainJobSpecs.setSpecsName(ptTrainJob.getTrainJobSpecsName());
        ptTrainJobSpecs.setSpecsInfo(JSONObject.parseObject(dictDetails.get(0).getValue()));

        // 拼load路径
        JSONObject runParams = ptJobParam.getRunParams();
        runParams.put(trainJobConfig.getLoadKey(), trainJobConfig.getDockerTrainPath() + StrUtil.SLASH +
                trainJobConfig.getLoadPath() + modelLoadDir);
        BaseTrainJobDTO baseTrainJobDTO = new BaseTrainJobDTO();
        BeanUtil.copyProperties(ptTrainJob, baseTrainJobDTO);
        baseTrainJobDTO.setPtTrainJobSpecs(ptTrainJobSpecs);
        baseTrainJobDTO.setRunParams(runParams);

        // 初始化训练时间和状态
        PtTrainJob updatePtTrainJob = new PtTrainJob();
        updatePtTrainJob.setId(ptTrainJob.getId()).setRuntime(TrainUtil.INIT_RUNTIME)
                .setTrainStatus(TrainJobStatusEnum.PENDING.getStatus())
                .setUpdateTime(new Timestamp(System.currentTimeMillis()));
        int updateResult = ptTrainJobMapper.updateById(updatePtTrainJob);
        if (updateResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} resumed training job, pt train Job table update failed", currentUser.getUsername());
            throw new BusinessException("内部错误");
        }
        // 此处将ptTrainJob的trainStatus和runTime设为null以避免doJob中再次调用updateById错误更新状态和时间
        ptTrainJob.setTrainStatus(null).setRuntime(null).setCreateTime(null);
        // 提交job
        asyncManager.execute(baseTrainJobDTO, currentUser, ptImageAndAlgorithmVO, ptTrainJob);
    }

    /**
     * 获取job在grafana监控的地址
     *
     * @param jobId                        任务ID
     * @return List<PtJobMetricsGrafanaVO> grafana监控的地址信息
     */
    @Override
    public List<PtJobMetricsGrafanaVO> getGrafanaUrl(Long jobId) {

        UserDTO currentUser = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} gets grafanaUrl of Job and receives parameter [jobId] {}", currentUser.getUsername(), jobId);

        //通过jobId获取job相关信息
        PtTrainJob ptTrainJob = ptTrainJobMapper.selectById(jobId);
        if (null == ptTrainJob || !currentUser.getId().equals(ptTrainJob.getCreateUserId())) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "It is illegal for user {} to get grafanaUrl on Job, jobId for {}", currentUser.getUsername(), jobId);
            throw new BusinessException(ResponseCode.SUCCESS, "您输入的id不存在或已被删除，请重新输入");
        }

        List<PtJobMetricsGrafanaVO> list = new ArrayList<>();
        try {
            List<BizPod> bizPodList = podApi.getListByResourceName(k8sNameTool.generateNamespace(currentUser.getId()), ptTrainJob.getJobName());
            bizPodList.stream()
                    .filter(bizPod -> bizPod.getPhase().equalsIgnoreCase(TrainJobStatusEnum.RUNNING.getMessage()))
                    .forEach(bizPod -> {
                        String podName = bizPod.getName();
                        PtJobMetricsGrafanaVO ptJobMetricsGrafanaVO = new PtJobMetricsGrafanaVO();
                        ptJobMetricsGrafanaVO.setJobMetricsGrafanaUrl(k8sPodMetricsGrafanaUrl.concat(podName));
                        ptJobMetricsGrafanaVO.setJobPodName(podName);
                        if (ptTrainJob.getTrainType() == 1 && PodUtil.isMaster(podName)) {
                            list.add(0, ptJobMetricsGrafanaVO);
                        } else {
                            list.add(ptJobMetricsGrafanaVO);
                        }
                    });
        } catch (Exception e) {
            LogUtil.info(LogEnum.BIZ_K8S, "Failed to obtain grafanaUrl of Pod, params:[namespace]={}, [resourceName]={}, error:{}",
                    k8sNameTool.generateNamespace(currentUser.getId()), ptTrainJob.getJobName(), e);
        }

        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} completes getting grafanaUrl on job, receives {} parameter [jobId], returns {} result",
                currentUser.getUsername(), jobId, JSONObject.toJSONString(list));
        return list;
    }

    /**
     * 获取训练使用的模型信息
     *
     * @param  ptTrainModelDTO
     * @return PtTrainJobModelVO
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public PtTrainJobModelVO getTrainJobModel(PtTrainModelDTO ptTrainModelDTO) {

        PtTrainJobModelVO<ModelVO> ptTrainJobModelVO = new PtTrainJobModelVO();
        UserDTO currentUser = JwtUtils.getCurrentUserDto();
        Integer modelResource = ptTrainModelDTO.getModelResource();
        switch (ModelResourceEnum.getType(modelResource)) {
            case MINE:
                if(null == ptTrainModelDTO.getModelBranchId() || null == ptTrainModelDTO.getModelId()) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                PtModelBranch ptModelBranch = ptModelBranchMapper.selectById(ptTrainModelDTO.getModelBranchId());
                if(null == ptModelBranch) {
                    break;
                }
                if(ptModelBranch.getParentId().compareTo(ptTrainModelDTO.getModelId()) != 0 ||
                        StringUtils.isBlank(ptModelBranch.getModelAddress())) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                PtModelInfo ptModelInfo = ptModelInfoMapper.selectById(ptModelBranch.getParentId());
                if(null == ptModelInfo || ptModelInfo.getModelResource().compareTo(ptTrainModelDTO.getModelResource()) != 0){
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                ptTrainJobModelVO.setModelList(new ArrayList<>());
                ptTrainJobModelVO.getModelList()
                        .add(new ModelVO(ptModelInfo.getName(), ptModelBranch.getVersionNum(), adjustmentUrl(ptModelBranch.getModelAddress())));
                break;
            case PRESET:
                if(null == ptTrainModelDTO.getModelId()) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                PtModelInfo ptModelInfoPreset = ptModelInfoMapper.selectById(ptTrainModelDTO.getModelId());
                if(null == ptModelInfoPreset) {
                    break;
                }
                if(StringUtils.isBlank(ptModelInfoPreset.getUrl()) ||
                        ptModelInfoPreset.getModelResource().compareTo(ptTrainModelDTO.getModelResource()) != 0) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                ptTrainJobModelVO.setModelList(new ArrayList<>());
                ptTrainJobModelVO.getModelList()
                        .add(new ModelVO(ptModelInfoPreset.getName(), ptModelInfoPreset.getVersionNum(), adjustmentUrl(ptModelInfoPreset.getUrl())));
                break;
            case ATLAS:
                if(StringUtils.isBlank(ptTrainModelDTO.getTeacherModelIds())){
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                Set<Long> ids = new HashSet<>();
                Set<Long> teacherModelList = new HashSet<>();
                Arrays.stream(ptTrainModelDTO.getTeacherModelIds().trim().split(SymbolConstant.COMMA))
                        .forEach(id -> teacherModelList.add(Long.parseLong(id)));
                ids.addAll(teacherModelList);

                Set<Long> studentModelList = new HashSet<>();
                if(StringUtils.isNotBlank(ptTrainModelDTO.getStudentModelIds())) {
                    Arrays.stream(ptTrainModelDTO.getStudentModelIds().trim().split(SymbolConstant.COMMA))
                            .forEach(id -> studentModelList.add(Long.parseLong(id)));
                    ids.addAll(studentModelList);
                }
                if(ids.isEmpty()) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                LambdaQueryWrapper<PtModelInfo> query = new LambdaQueryWrapper<>();
                query.eq(PtModelInfo::getModelResource, ptTrainModelDTO.getModelResource())
                        .in(PtModelInfo::getId, ids).isNotNull(PtModelInfo::getUrl).ne(PtModelInfo::getUrl, SymbolConstant.BLANK);
                List<PtModelInfo> modelInfoList = ptModelInfoMapper.selectList(query);
                if(null == modelInfoList || modelInfoList.isEmpty()) {
                    break;
                }

                //保存炼知教师模型信息
                ptTrainJobModelVO.setTeacherModelList(new ArrayList<>());
                List<ModelVO> teacherModelVOS = ptTrainJobModelVO.getTeacherModelList();
                modelInfoList.stream()
                        .filter(modelInfo -> teacherModelList.contains(modelInfo.getId()))
                        .forEach(modelInfo -> teacherModelVOS
                                .add(new ModelVO(modelInfo.getName(), modelInfo.getVersionNum(), adjustmentUrl(modelInfo.getUrl()))));

                //保存炼知学生模型信息
                if(!studentModelList.isEmpty()) {
                    ptTrainJobModelVO.setStudentModelList(new ArrayList<>());
                    List<ModelVO> studentModelVOS = ptTrainJobModelVO.getStudentModelList();
                    modelInfoList.stream()
                            .filter(modelInfo -> studentModelList.contains(modelInfo.getId()))
                            .forEach(modelInfo -> studentModelVOS
                                    .add(new ModelVO(modelInfo.getName(), modelInfo.getVersionNum(), adjustmentUrl(modelInfo.getUrl()))));
                }
                break;
        }

        return ptTrainJobModelVO;
    }

    public void recycleTaskWithTrain(String recyclePath) {
        //创建已删除训练任务的无效文件回收任务
        RecycleTaskCreateDTO recycleTask = new RecycleTaskCreateDTO();
        recycleTask.setRecycleModule(RecycleModuleEnum.BIZ_TRAIN.getValue())
                .setRecycleType(RecycleTypeEnum.FILE.getCode())
                .setRecycleDelayDate(recycleConfig.getTrainValid())
                .setRecycleCondition(recyclePath)
                .setRecycleNote("回收已删除训练任务文件");
        LogUtil.info(LogEnum.BIZ_TRAIN, "delete train job add recycle task:{}", recycleTask);
        recycleTaskService.createRecycleTask(recycleTask);
    }
}
