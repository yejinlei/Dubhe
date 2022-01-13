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
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.ResponseCode;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.dto.*;
import org.dubhe.biz.base.enums.AlgorithmStatusEnum;
import org.dubhe.biz.base.enums.DatasetTypeEnum;
import org.dubhe.biz.base.enums.ModelResourceEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.ReflectionUtils;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.vo.*;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.permission.annotation.DataPermissionMethod;
import org.dubhe.biz.permission.base.BaseService;
import org.dubhe.biz.redis.utils.RedisUtils;
import org.dubhe.k8s.api.DistributeTrainApi;
import org.dubhe.k8s.api.PersistentVolumeClaimApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.api.TrainJobApi;
import org.dubhe.k8s.cache.ResourceCache;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.utils.K8sNameTool;
import org.dubhe.k8s.utils.PodUtil;
import org.dubhe.recycle.config.RecycleConfig;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.dto.RecycleDetailCreateDTO;
import org.dubhe.recycle.enums.RecycleModuleEnum;
import org.dubhe.recycle.enums.RecycleTypeEnum;
import org.dubhe.recycle.service.RecycleService;
import org.dubhe.recycle.utils.RecycleTool;
import org.dubhe.train.async.StopTrainJobAsync;
import org.dubhe.train.async.TransactionAsyncManager;
import org.dubhe.train.client.*;
import org.dubhe.train.config.TrainHarborConfig;
import org.dubhe.train.config.TrainJobConfig;
import org.dubhe.train.constant.TrainConstant;
import org.dubhe.train.dao.PtJobParamMapper;
import org.dubhe.train.dao.PtTrainJobMapper;
import org.dubhe.train.dao.PtTrainMapper;
import org.dubhe.train.dao.PtTrainParamMapper;
import org.dubhe.train.domain.dto.*;
import org.dubhe.train.domain.entity.PtJobParam;
import org.dubhe.train.domain.entity.PtTrain;
import org.dubhe.train.domain.entity.PtTrainJob;
import org.dubhe.train.domain.entity.PtTrainParam;
import org.dubhe.train.domain.vo.*;
import org.dubhe.train.enums.TrainJobStatusEnum;
import org.dubhe.train.enums.TrainTypeEnum;
import org.dubhe.train.service.PtTrainJobService;
import org.dubhe.train.utils.ImageUtil;
import org.dubhe.train.utils.KeyUtil;
import org.dubhe.train.utils.TrainUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.sql.Timestamp;
import java.util.*;
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
    private AlgorithmClient algorithmClient;

    @Autowired
    private TrainJobApi trainJobApi;

    @Autowired
    private PersistentVolumeClaimApi persistentVolumeClaimApi;

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
    private DictDetailClient dictDetailClient;

    @Autowired
    private ResourceSpecsClient resourceSpecsClient;

    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;

    @Autowired
    private RecycleConfig recycleConfig;


    @Autowired
    private RecycleService recycleService;

    @Value("${k8s.pod.metrics.grafanaUrl}")
    private String k8sPodMetricsGrafanaUrl;

    @Autowired
    private ModelBranchClient modelBranchClient;

    @Autowired
    private ModelInfoClient modelInfoClient;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private ResourceCache resourceCache;

    @Autowired
    private NoteBookClient noteBookClient;

    @Value("Task:Train:" + "${spring.profiles.active}_train_job_id_")
    private String trainIdPrefix;

    public final static List<String> FIELD_NAMES;

    static {
        FIELD_NAMES = ReflectionUtils.getFieldNames(PtTrainVO.class);
    }

    /**
     * 作业列表展示
     *
     * @param ptTrainQueryDTO 查询作业列表参数
     * @return Map<String, Object>  作业列表分页信息
     **/
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Object> getTrainJob(@NonNull PtTrainQueryDTO ptTrainQueryDTO) {
        Page page = ptTrainQueryDTO.toPage();
        //排序方式
        String order = StringConstant.SORT_ASC.equalsIgnoreCase(ptTrainQueryDTO.getOrder()) ? StringConstant.SORT_ASC : StringConstant.SORT_DESC;
        //排序字段
        String sortField = FIELD_NAMES.contains(ptTrainQueryDTO.getSort()) ? ptTrainQueryDTO.getSort() : StringConstant.ID;
        String sort = StringUtils.humpToLine(sortField);
        //设置管理员可以查询所有数据
        Long userId = userContextService.getCurUserId();
        if (BaseService.isAdmin(userContextService.getCurUser())) {
            userId = null;
        }
        Page<PtTrainVO> pageTrainResult = ptTrainJobMapper.getPageTrain(page, userId, ptTrainQueryDTO.getTrainStatus(), ptTrainQueryDTO.getTrainName(), sort, order);
        List<PtTrainVO> trainResult = pageTrainResult.getRecords();
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
        //校验参数
        checkTrainId(ptTrainJobVersionQueryDTO.getTrainId());
        String sort = null == ptTrainJobVersionQueryDTO.getSort() ? StringConstant.ID : ptTrainJobVersionQueryDTO.getSort();

        QueryWrapper<PtTrainJob> queryTrainJonWrapper = new QueryWrapper<>();
        queryTrainJonWrapper.eq("train_id", ptTrainJobVersionQueryDTO.getTrainId());
        //根据训练状态筛选
        if (ptTrainJobVersionQueryDTO.getTrainStatus() != null) {
            queryTrainJonWrapper.eq("train_status", ptTrainJobVersionQueryDTO.getTrainStatus());
        }
        if (StringConstant.SORT_ASC.equals(ptTrainJobVersionQueryDTO.getOrder())) {
            queryTrainJonWrapper.orderByAsc(StringUtils.humpToLine(sort));
        } else {
            queryTrainJonWrapper.orderByDesc(StringUtils.humpToLine(sort));
        }
        //按照trainId查找
        List<PtTrainJob> ptTrainJobs = ptTrainJobMapper.selectList(queryTrainJonWrapper);
        if (CollectionUtils.isEmpty(ptTrainJobs)) {
            return Collections.emptyList();
        }
        Set<Long> jobIds = ptTrainJobs.stream().map(PtTrainJob::getId).collect(Collectors.toSet());
        QueryWrapper<PtJobParam> queryJobParamWrapper = new QueryWrapper<>();
        queryJobParamWrapper.in("train_job_id", jobIds);
        //找出所有训练参数
        List<PtJobParam> ptJobParams = ptJobParamMapper.selectList(queryJobParamWrapper);
        List<TrainAlgorithmQureyVO> ptTrainAlgorithms = null;
        if (CollectionUtils.isNotEmpty(ptJobParams)) {
            Set<Long> algorithmIds = ptJobParams.stream().map(PtJobParam::getAlgorithmId).filter(x -> x != null).collect(Collectors.toSet());
            ptTrainAlgorithms = selectAllBatchIds(algorithmIds);
        }
        //获取训练信息
        PtTrain ptTrain = ptTrainMapper.selectById(ptTrainJobVersionQueryDTO.getTrainId());
        //结果集处理
        return getTrainJobDetail(ptTrainJobs, ptJobParams, ptTrainAlgorithms, ptTrain);
    }

    /**
     * 查询算法
     *
     * @param algorithmIds 算法id集合
     * @return
     */
    public List<TrainAlgorithmQureyVO> selectAllBatchIds(Set<Long> algorithmIds) {
        if (CollectionUtils.isEmpty(algorithmIds)) {
            return Collections.emptyList();
        }
        TrainAlgorithmSelectAllBatchIdDTO trainAlgorithmSelectAllBatchIdDTO = new TrainAlgorithmSelectAllBatchIdDTO();
        trainAlgorithmSelectAllBatchIdDTO.setIds(algorithmIds);
        DataResponseBody<List<TrainAlgorithmQureyVO>> dataResponseBody = algorithmClient.selectAllBatchIds(trainAlgorithmSelectAllBatchIdDTO);
        if (dataResponseBody.succeed()) {
            return dataResponseBody.getData();
        } else {
            LogUtil.info(LogEnum.BIZ_TRAIN, "Fail to query algorithm. data response body is {}", dataResponseBody);
            throw new BusinessException("算法服务调用失败，请稍后重试~");
        }
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
    private List<PtTrainJobDetailVO> getTrainJobDetail(List<PtTrainJob> ptTrainJobs, List<PtJobParam> ptJobParams, List<TrainAlgorithmQureyVO> ptTrainAlgorithms, PtTrain ptTrain) {
        Map<Long, TrainAlgorithmQureyVO> algorithmMap = new HashedMap<>();

        if (CollectionUtils.isNotEmpty(ptTrainAlgorithms)) {
            ptTrainAlgorithms.forEach(x -> algorithmMap.put(x.getId(), x));
        }

        List<PtTrainJobDetailVO> list = new ArrayList<>();
        Map<Long, PtTrainJobDetailVO> jobParamMap = new HashedMap<>();

        ptTrainJobs.forEach(x -> {
            PtTrainJobDetailVO ptTrainJobDetailVO = new PtTrainJobDetailVO();
            BeanUtil.copyProperties(x, ptTrainJobDetailVO);
            list.add(ptTrainJobDetailVO);
            jobParamMap.put(x.getId(), ptTrainJobDetailVO);
        });


        ptJobParams.forEach(x -> {
            PtTrainJobDetailVO ptTrainJobDetailVO = jobParamMap.get(x.getTrainJobId());
            if (null != ptTrainJobDetailVO) {

                ptTrainJobDetailVO.setAlgorithmId(x.getAlgorithmId())
                        .setAlgorithmUsage(x.getAlgorithmUsage())
                        .setValAlgorithmUsage(x.getValAlgorithmUsage())
                        .setRunCommand(x.getRunCommand())
                        .setImageName(x.getImageName())
                        .setRunParams(x.getRunParams())
                        .setParamF1(x.getParamF1())
                        .setParamCallback(x.getParamCallback())
                        .setNotebookId(x.getNotebookId())
                        .setNotebookName(x.getNotebookName())
                        .setParamPrecise(x.getParamPrecise())
                        .setParamAccuracy(x.getParamAccuracy());
                long nowTime = System.currentTimeMillis();
                //获取训练延时启动倒计时（分钟）
                if (x.getDelayCreateTime() != null
                        && nowTime < x.getDelayCreateTime().getTime()
                        && TrainJobStatusEnum.checkRunStatus(ptTrainJobDetailVO.getTrainStatus())) {
                    ptTrainJobDetailVO.setDelayCreateCountDown(TrainUtil.getCountDown(x.getDelayCreateTime().getTime()));
                }
                //获取训练自动停止倒计时（分钟）
                if (x.getDelayDeleteTime() != null
                        && nowTime < x.getDelayDeleteTime().getTime()
                        && TrainJobStatusEnum.checkRunStatus(ptTrainJobDetailVO.getTrainStatus())) {
                    ptTrainJobDetailVO.setDelayDeleteCountDown(TrainUtil.getCountDown(x.getDelayDeleteTime().getTime()));
                }
                buildImageAndTagInfo(x, ptTrainJobDetailVO);
            }
        });

        for (PtTrainJobDetailVO ptTrainJobDetailVO : list) {
            ptTrainJobDetailVO.setTrainName(ptTrain.getTrainName());
            TrainAlgorithmQureyVO ptTrainAlgorithm = algorithmMap.get(ptTrainJobDetailVO.getAlgorithmId());
            if (null != ptTrainAlgorithm) {
                ptTrainJobDetailVO.setAlgorithmName(ptTrainAlgorithm.getAlgorithmName())
                        .setAlgorithmSource(ptTrainAlgorithm.getAlgorithmSource())
                        .setAccuracy(ptTrainAlgorithm.getAccuracy())
                        .setP4InferenceSpeed(ptTrainAlgorithm.getP4InferenceSpeed());
                //1为我的算法，2为预置算法
                if (ptTrainAlgorithm.getAlgorithmSource() == MagicNumConstant.ONE) {
                    ptTrainJobDetailVO.setAlgorithmCodeDir(ptTrainAlgorithm.getCodeDir());
                }
            }
        }

        return list;
    }

    /**
     * 构建镜像信息
     *
     * @param ptJobParam
     * @param ptTrainJobDetailVO
     */
    public void buildImageAndTagInfo(PtJobParam ptJobParam, PtTrainJobDetailVO ptTrainJobDetailVO) {
        //image信息拼装
        if (StringUtils.isNotBlank(ptJobParam.getImageName())) {
            String imageNameSuffix = ptJobParam.getImageName().substring(ptJobParam.getImageName().lastIndexOf(StrUtil.SLASH) + MagicNumConstant.ONE);
            String[] imageNameSuffixArray = imageNameSuffix.split(StrUtil.COLON);
            ptTrainJobDetailVO.setImageName(imageNameSuffixArray[0]);
            ptTrainJobDetailVO.setImageTag(imageNameSuffixArray[1]);
        }
    }

    /**
     * 校验请求不同版本job所传参数是否合法
     *
     * @param trainId 训练ID
     */
    private void checkTrainId(Long trainId) {
        if (null == trainId || trainId < 1) {
            throw new BusinessException("参数不合法");
        }
        PtTrain ptTrain = ptTrainMapper.selectById(trainId);
        if (null == ptTrain) {
            throw new BusinessException("查询对象不存在或已被删除");
        }
    }

    /**
     * 获取notebook
     *
     * @param id
     * @return
     */
    private NoteBookVO getNoteBook(Long id) {
        DataResponseBody<NoteBookVO> dataResponseBody = noteBookClient.getNoteBook(id);
        if (dataResponseBody.succeed()) {
            NoteBookVO data = dataResponseBody.getData();
            if (data == null) {
                LogUtil.info(LogEnum.BIZ_TRAIN, "There is no such notebook, id is ", id);
                throw new BusinessException("无此NoteBook");
            }
            return dataResponseBody.getData();
        } else {
            LogUtil.info(LogEnum.BIZ_TRAIN, "NoteBook service unreachable! Msg is {}", dataResponseBody.getMsg());
            throw new BusinessException("NoteBook服务调用失败，请稍后重试~");
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

        validatePtTrainJobCreateDTO(ptTrainJobCreateDTO);

        // 判断当前trainName是否已经存在
        checkTrainName(ptTrainJobCreateDTO.getTrainName(), userContextService.getCurUserId());

        // 校验trainParamName是否存在
        if (ptTrainJobCreateDTO.getSaveParams() != null && ptTrainJobCreateDTO.getSaveParams()) {
            checkTrainParamName(ptTrainJobCreateDTO, userContextService.getCurUserId());
            // 保存任务参数到数据库
            saveParamToDb(ptTrainJobCreateDTO, userContextService.getCurUser());
        }

        //jobKey
        String trainKey = KeyUtil.generateTrainKey(userContextService.getCurUserId());

        //版本
        String version = trainJobConfig.getVersionLabel() + String.format(TrainUtil.FOUR_DECIMAL, 1);
        //生成k8s 的job名称
        String jobName = trainKey + trainJobConfig.getSeparator() + version;

        // 获取镜像和算法目录
        PtImageAndAlgorithmVO ptImageAndAlgorithmVO = buildPtImageAndAlgorithmVO(ptTrainJobCreateDTO);

        //生成任务识别标识
        String taskIdentify = StringUtils.getUUID();
        BaseTrainJobDTO baseTrainJobDTO = new BaseTrainJobDTO();
        BeanUtil.copyProperties(ptTrainJobCreateDTO, baseTrainJobDTO);
        baseTrainJobDTO.setJobName(jobName)
                .setPipSitePackagePath(ptImageAndAlgorithmVO.getPipSitePackagePath())
                .setTrainJobSpecsName(ptTrainJobCreateDTO.getTrainJobSpecsName())
                .setResourcesPoolType(ptTrainJobCreateDTO.getResourcesPoolType())
                .setCpuNum(ptTrainJobCreateDTO.getCpuNum())
                .setGpuNum(ptTrainJobCreateDTO.getGpuNum())
                .setMemNum(ptTrainJobCreateDTO.getMemNum())
                .setWorkspaceRequest(ptTrainJobCreateDTO.getWorkspaceRequest())
                .setTaskIdentify(taskIdentify);

        //例如：  将harbor.dubhe.ai/notebook/notebook:v1 去掉 harbor地址
        String userImageName = trimHarborAddress(ptImageAndAlgorithmVO.getImageName());
        //结果集处理
        PtTrainJob ptTrainJob = saveTrainJobTableData(ptTrainJobCreateDTO, userContextService.getCurUser(), userImageName, trainKey, baseTrainJobDTO);
        //添加任务缓存
        resourceCache.addTaskCache(taskIdentify, ptTrainJob.getTrainId(), ptTrainJobCreateDTO.getTrainName(), trainIdPrefix);
        // 提交job
        asyncManager.execute(baseTrainJobDTO, userContextService.getCurUserId(), ptImageAndAlgorithmVO, ptTrainJob);
        return Collections.singletonList(ptTrainJob.getTrainId());
    }

    /**
     * 去掉harbor地址
     *
     * @param imageName
     * @return
     */
    private String trimHarborAddress(String imageName) {
        return StringUtils.isBlank(imageName) ? StringUtils.EMPTY : imageName.replace(trainHarborConfig.getAddress() + StrUtil.SLASH, StringUtils.EMPTY);
    }

    /**
     * 构建镜像和算法目录VO 考虑到无算法创建
     *
     * @param ptTrainJobBaseDTO
     * @return
     */
    private PtImageAndAlgorithmVO buildPtImageAndAlgorithmVO(PtTrainJobBaseDTO ptTrainJobBaseDTO) {
        PtImageAndAlgorithmVO ptImageAndAlgorithmVO;
        //没有算法id则以notebook为主
        if (ptTrainJobBaseDTO.getAlgorithmId() == null) {
            ptImageAndAlgorithmVO = new PtImageAndAlgorithmVO();
            //notebook 信息
            NoteBookVO noteBook = getNoteBook(ptTrainJobBaseDTO.getNotebookId());
            ptImageAndAlgorithmVO.setPipSitePackagePath(noteBook.getPipSitePackagePath());

            ptImageAndAlgorithmVO.setImageName(noteBook.getK8sImageName());
            ptImageAndAlgorithmVO.setIsTrainOut(true);
            ptImageAndAlgorithmVO.setIsTrainModelOut(true);
            //默认可视化输出不输出 python 文件中可以不用接受这个参数
            ptImageAndAlgorithmVO.setIsVisualizedLog(false);
            ptImageAndAlgorithmVO.setCodeDir(noteBook.getK8sPvcPath());
        } else {
            //使用用户创建训练时提供的镜像与运行命令
            String imageUrl = imageUtil.getImageUrl(ptTrainJobBaseDTO, userContextService.getCurUser());
            String userImageName = imageUrl.split(StrUtil.SLASH)[0] + StrUtil.SLASH + ptTrainJobBaseDTO.getImageName() + StrUtil.COLON + ptTrainJobBaseDTO.getImageTag();
            ptImageAndAlgorithmVO = getPtImageByAlgorithmId(ptTrainJobBaseDTO.getAlgorithmId());
            String imageName = trainHarborConfig.getAddress() + StrUtil.SLASH + userImageName;
            ptImageAndAlgorithmVO.setImageName(imageName);
            ptImageAndAlgorithmVO.setImageUrl(trainHarborConfig.getAddress() + StrUtil.SLASH + imageUrl);

        }
        ptImageAndAlgorithmVO.setRunCommand(ptTrainJobBaseDTO.getRunCommand());
        return ptImageAndAlgorithmVO;
    }

    /**
     * 参数校验
     *
     * @param ptTrainJobCreateDTO
     */
    private void validatePtTrainJobCreateDTO(PtTrainJobCreateDTO ptTrainJobCreateDTO) {
        if (ptTrainJobCreateDTO.getAlgorithmId() == null && ptTrainJobCreateDTO.getNotebookId() == null) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "Neither algorithm's id  nor notebook's id can be null  at the same time");
            throw new BusinessException("算法ID或者notebookId不能同时为空！");
        }
        //带算法创建(非notebook发起训练)时校验参数
        if (ptTrainJobCreateDTO.getNotebookId() == null) {
            validateCreateTrainJobWithAlgorithm(ptTrainJobCreateDTO);
        }
    }

    /**
     * 参数校验
     *
     * @param ptTrainJobCreateDTO
     */
    private void validateCreateTrainJobWithAlgorithm(PtTrainJobCreateDTO ptTrainJobCreateDTO) {
        if (ptTrainJobCreateDTO.getAlgorithmId() == null) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "Algorithm id is null");
            throw new BusinessException("算法ID不能为空~");
        }
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
    private PtTrainJob saveTrainJobTableData(PtTrainJobCreateDTO ptTrainJobCreateDTO, UserContext currentUser,
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

        // 保存job参数
        PtJobParam ptJobParam = new PtJobParam();

        // 添加train_job表
        PtTrainJob ptTrainJob = new PtTrainJob();
        BeanUtil.copyProperties(ptTrainJobCreateDTO, ptTrainJob);
        if (ptTrainJobCreateDTO.getNotebookId() != null) {
            NoteBookVO noteBook = getNoteBook(ptTrainJobCreateDTO.getNotebookId());
            ptJobParam.setNotebookName(noteBook.getNoteBookName());
            ptJobParam.setNotebookId(noteBook.getId());
        }
        ptTrainJob.setTrainId(ptTrain.getId())
                .setTrainVersion(trainJobConfig.getVersionLabel().toUpperCase() + String.format(TrainUtil.FOUR_DECIMAL, MagicNumConstant.ONE))
                .setJobName(baseTrainJobDTO.getJobName())
                .setCreateUserId(currentUser.getId());
        int jobResult = ptTrainJobMapper.insert(ptTrainJob);
        if (jobResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} created training Job, failed to insert data in train_job table", currentUser.getUsername());
            throw new BusinessException("内部错误");
        }


        ptJobParam.setTrainJobId(ptTrainJob.getId())
                .setAlgorithmId(ptTrainJobCreateDTO.getAlgorithmId())
                .setRunCommand(ptTrainJobCreateDTO.getRunCommand())
                .setImageName(imageName)
                .setRunParams(ptTrainJobCreateDTO.getRunParams())
                .setCreateUserId(currentUser.getId());
        //保存算法用途
        if (ptTrainJobCreateDTO.getAlgorithmUsage() != null) {
            ptJobParam.setAlgorithmUsage(ptTrainJobCreateDTO.getAlgorithmUsage());
        }
        //保存验证数据集算法用途
        if (ptTrainJobCreateDTO.getValAlgorithmUsage() != null) {
            ptJobParam.setValAlgorithmUsage(ptTrainJobCreateDTO.getValAlgorithmUsage());
        }
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
     * @param currentUser     用户
     * @param baseTrainJobDTO 基础训练参数
     */
    private void checkModelAndSavePath(UserContext currentUser, BaseTrainJobDTO baseTrainJobDTO) {

        Integer modelResource = baseTrainJobDTO.getModelResource();
        if (null == modelResource) {
            if (null == baseTrainJobDTO.getModelId() &&
                    StringUtils.isBlank(baseTrainJobDTO.getStudentModelIds()) &&
                    StringUtils.isBlank(baseTrainJobDTO.getStudentModelIds())) {
                return;
            } else {
                logErrorInfoOnModel(currentUser.getUsername());
            }
        }
        PtModelBranchQueryByIdDTO ptModelBranchQueryByIdDTO = new PtModelBranchQueryByIdDTO();
        PtModelInfoQueryByIdDTO ptModelInfoQueryByIdDTO = new PtModelInfoQueryByIdDTO();
        PtModelInfoConditionQueryDTO ptModelInfoConditionQueryDTO = new PtModelInfoConditionQueryDTO();
        switch (ModelResourceEnum.getType(modelResource)) {
            case MINE:
                if (null == baseTrainJobDTO.getModelBranchId() || null == baseTrainJobDTO.getModelId() ||
                        StringUtils.isNotBlank(baseTrainJobDTO.getTeacherModelIds()) ||
                        StringUtils.isNotBlank(baseTrainJobDTO.getStudentModelIds())) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                ptModelBranchQueryByIdDTO.setId(baseTrainJobDTO.getModelBranchId());
                DataResponseBody<PtModelBranchQueryVO> dataResponseBody = modelBranchClient.getByBranchId(ptModelBranchQueryByIdDTO);
                PtModelBranchQueryVO ptModelBranchQueryVO = null;
                if (dataResponseBody.succeed()) {
                    ptModelBranchQueryVO = dataResponseBody.getData();
                }

                if (null == ptModelBranchQueryVO || ptModelBranchQueryVO.getParentId().compareTo(baseTrainJobDTO.getModelId()) != 0 ||
                        StringUtils.isBlank(ptModelBranchQueryVO.getModelAddress())) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                ptModelInfoQueryByIdDTO.setId(ptModelBranchQueryVO.getParentId());
                DataResponseBody<PtModelInfoQueryVO> modelInfoDataResponseBody = modelInfoClient.getByModelId(ptModelInfoQueryByIdDTO);
                PtModelInfoQueryVO ptModelInfoQueryVO = null;
                if (modelInfoDataResponseBody.succeed()) {
                    ptModelInfoQueryVO = modelInfoDataResponseBody.getData();
                }
                if (null == ptModelInfoQueryVO || ptModelInfoQueryVO.getModelResource().compareTo(baseTrainJobDTO.getModelResource()) != 0) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                baseTrainJobDTO.setModelPath(adjustmentUrl(ptModelBranchQueryVO.getModelAddress()));
                break;
            case PRESET:
                if (null == baseTrainJobDTO.getModelId() || StringUtils.isNotBlank(baseTrainJobDTO.getTeacherModelIds()) ||
                        StringUtils.isNotBlank(baseTrainJobDTO.getStudentModelIds())) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                ptModelInfoQueryByIdDTO.setId(baseTrainJobDTO.getModelId());
                DataResponseBody<PtModelInfoQueryVO> modelInfoPresetDataResponseBody = modelInfoClient.getByModelId(ptModelInfoQueryByIdDTO);
                PtModelInfoQueryVO ptModelInfoPresetQueryVO = null;
                if (modelInfoPresetDataResponseBody.succeed()) {
                    ptModelInfoPresetQueryVO = modelInfoPresetDataResponseBody.getData();
                }
                if (null == ptModelInfoPresetQueryVO || StringUtils.isBlank(ptModelInfoPresetQueryVO.getModelAddress()) ||
                        ptModelInfoPresetQueryVO.getModelResource().compareTo(baseTrainJobDTO.getModelResource()) != 0) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                baseTrainJobDTO.setModelPath(adjustmentUrl(ptModelInfoPresetQueryVO.getModelAddress()));
                break;
            case ATLAS:
                if (StringUtils.isBlank(baseTrainJobDTO.getTeacherModelIds()) || null != baseTrainJobDTO.getModelId()) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                Set<Long> ids = new HashSet<>();
                Set<Long> teacherModelList = new HashSet<>();
                Arrays.stream(baseTrainJobDTO.getTeacherModelIds().trim().split(SymbolConstant.COMMA))
                        .forEach(id -> teacherModelList.add(Long.parseLong(id)));
                ids.addAll(teacherModelList);

                Set<Long> studentModelList = new HashSet<>();
                if (StringUtils.isNotBlank(baseTrainJobDTO.getStudentModelIds())) {
                    Arrays.stream(baseTrainJobDTO.getStudentModelIds().trim().split(SymbolConstant.COMMA))
                            .forEach(id -> studentModelList.add(Long.parseLong(id)));
                    ids.addAll(studentModelList);
                }
                if (ids.isEmpty()) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }

                ptModelInfoConditionQueryDTO.setIds(ids);
                ptModelInfoConditionQueryDTO.setModelResource(baseTrainJobDTO.getModelResource());
                DataResponseBody<List<PtModelInfoQueryVO>> conditionQueryDataResponseBody = modelInfoClient.getConditionQuery(ptModelInfoConditionQueryDTO);
                List<PtModelInfoQueryVO> modelInfoList = null;
                if (conditionQueryDataResponseBody.succeed()) {
                    modelInfoList = conditionQueryDataResponseBody.getData();
                }
                if (null == modelInfoList || modelInfoList.size() < ids.size()) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }

                //保存炼知教师模型路径地址
                baseTrainJobDTO.setTeacherModelPathList(new ArrayList<>());
                modelInfoList.stream()
                        .filter(modelInfo -> teacherModelList.contains(modelInfo.getId()))
                        .forEach(modelInfo -> baseTrainJobDTO.getTeacherModelPathList().add(adjustmentUrl(modelInfo.getModelAddress())));

                //保存炼知学生模型路径地址
                if (!studentModelList.isEmpty()) {
                    baseTrainJobDTO.setStudentModelPathList(new ArrayList<>());
                    modelInfoList.stream()
                            .filter(modelInfo -> studentModelList.contains(modelInfo.getId()))
                            .forEach(modelInfo -> baseTrainJobDTO.getStudentModelPathList().add(adjustmentUrl(modelInfo.getModelAddress())));
                }
                break;
        }
    }


    /**
     * 调整模型地址
     *
     * @param modelUrl 模型地址
     * @return 模型地址
     */
    private String adjustmentUrl(String modelUrl) {
        if (modelUrl.endsWith(SymbolConstant.SLASH)) {
            modelUrl = modelUrl.substring(MagicNumConstant.ZERO, modelUrl.length() - MagicNumConstant.ONE);
        }
        return modelUrl;
    }

    /**
     * 打印训练任务中模型相关的错误日志
     *
     * @param username
     */
    private void logErrorInfoOnModel(String username) {
        LogUtil.error(LogEnum.BIZ_TRAIN, "User {} operating training job, error on model......", username);
        throw new BusinessException("模型参数参数不合法");
    }

    /**
     * 保存任务参数到数据库
     *
     * @param ptTrainJobCreateDTO 创建训练任务DTO
     * @param currentUser         用户
     */
    private void saveParamToDb(PtTrainJobCreateDTO ptTrainJobCreateDTO, UserContext currentUser) {
        PtTrainParam ptTrainParam = new PtTrainParam();
        BeanUtil.copyProperties(ptTrainJobCreateDTO, ptTrainParam);
        //获取镜像url
        String image = imageUtil.getImageUrl(ptTrainJobCreateDTO, currentUser);
        ptTrainParam.setImageName(image);
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
     * 获取镜像和算法目录
     *
     * @param algorithmId 算法
     * @return PtImageAndAlgorithmVO 镜像
     */
    private PtImageAndAlgorithmVO getPtImageByAlgorithmId(Long algorithmId) {
        TrainAlgorithmSelectByIdDTO trainAlgorithmSelectByIdDTO = new TrainAlgorithmSelectByIdDTO();
        trainAlgorithmSelectByIdDTO.setId(algorithmId);
        DataResponseBody<TrainAlgorithmQureyVO> dataResponseBody = algorithmClient.selectById(trainAlgorithmSelectByIdDTO);
        TrainAlgorithmQureyVO ptTrainAlgorithm = null;
        if (dataResponseBody.succeed()) {
            ptTrainAlgorithm = dataResponseBody.getData();
        }
        if (null == ptTrainAlgorithm || StringUtils.isBlank(ptTrainAlgorithm.getCodeDir())) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The record with algorithm training ID {} has no corresponding image or algorithm directory configuration", algorithmId);
            throw new BusinessException(ResponseCode.ERROR, "该id的记录没有相应的镜像或者算法目录配置");
        }

        if (!AlgorithmStatusEnum.SUCCESS.getCode().equals(ptTrainAlgorithm.getAlgorithmStatus())) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The algorithm ID {} algorithmStatus is {} unusual", algorithmId, ptTrainAlgorithm.getAlgorithmStatus());
            throw new BusinessException(ResponseCode.ERROR, "该算法状态异常!");
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
     * @param ptTrainJobUpdateDTO 修改训练job参数
     * @return List<Long>           id集合
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public List<Long> updateTrainJob(PtTrainJobUpdateDTO ptTrainJobUpdateDTO) {
        if (ptTrainJobUpdateDTO.getNotebookId() == null && ptTrainJobUpdateDTO.getAlgorithmId() == null) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "Neither algorithm's id  nor notebook's id can be null  at the same time");
            throw new BusinessException("算法ID或者notebookId不能同时为空！");
        }
        PtTrainJob existPtTrainJob = ptTrainJobMapper.selectById(ptTrainJobUpdateDTO.getId());
        if (null == existPtTrainJob) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "It is illegal for a user {} to modify a training job, jobId, to {}", userContextService.getCurUser().getUsername(), ptTrainJobUpdateDTO.getId());
            throw new BusinessException("您输入的id不存在或已被删除");
        }


        PtTrain ptTrain = ptTrainMapper.selectById(existPtTrainJob.getTrainId());

        String jobName = buildVersion(ptTrain);


        PtImageAndAlgorithmVO ptImageAndAlgorithmVO = buildPtImageAndAlgorithmVO(ptTrainJobUpdateDTO);

        BaseTrainJobDTO baseTrainJobDTO = new BaseTrainJobDTO();
        BeanUtil.copyProperties(ptTrainJobUpdateDTO, baseTrainJobDTO);
        String taskIdentify = resourceCache.getTaskIdentify(ptTrain.getId(), ptTrain.getTrainName(), trainIdPrefix);
        baseTrainJobDTO.setJobName(jobName)
                .setTrainJobSpecsName(ptTrainJobUpdateDTO.getTrainJobSpecsName())
                .setPipSitePackagePath(ptImageAndAlgorithmVO.getPipSitePackagePath())
                .setResourcesPoolType(ptTrainJobUpdateDTO.getResourcesPoolType())
                .setCpuNum(ptTrainJobUpdateDTO.getCpuNum())
                .setGpuNum(ptTrainJobUpdateDTO.getGpuNum())
                .setMemNum(ptTrainJobUpdateDTO.getMemNum())
                .setWorkspaceRequest(ptTrainJobUpdateDTO.getWorkspaceRequest())
                .setTaskIdentify(taskIdentify);

        String userImageName = trimHarborAddress(ptImageAndAlgorithmVO.getImageName());
        //结果集处理
        PtTrainJob ptTrainJob = updateTrainJobTableData(ptTrainJobUpdateDTO, userContextService.getCurUser(), existPtTrainJob, userImageName, ptTrain, baseTrainJobDTO);
        //提交job
        asyncManager.execute(baseTrainJobDTO, ptTrain.getCreateUserId(), ptImageAndAlgorithmVO, ptTrainJob);

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
    private PtTrainJob updateTrainJobTableData(PtTrainJobUpdateDTO ptTrainJobUpdateDTO, UserContext
            currentUser, PtTrainJob existPtTrainJob, String imageName, PtTrain ptTrain, BaseTrainJobDTO baseTrainJobDTO) {

        //检查模型是否合法,合法则保存其路径地址
        checkModelAndSavePath(currentUser, baseTrainJobDTO);
        //保存job参数
        PtJobParam ptJobParam = new PtJobParam();
        //添加train_job表
        PtTrainJob ptTrainJob = new PtTrainJob();
        BeanUtil.copyProperties(ptTrainJobUpdateDTO, ptTrainJob);

        if (ptTrainJobUpdateDTO.getNotebookId() != null) {
            NoteBookVO noteBook = getNoteBook(ptTrainJobUpdateDTO.getNotebookId());
            ptJobParam.setNotebookName(noteBook.getNoteBookName());
            ptJobParam.setNotebookId(noteBook.getId());
        }

        ptTrainJob.setTrainId(ptTrain.getId())
                .setTrainVersion(trainJobConfig.getVersionLabel().toUpperCase() + String.format(TrainUtil.FOUR_DECIMAL, ptTrain.getTotalNum() + 1))
                .setJobName(baseTrainJobDTO.getJobName())
                .setParentTrainVersion(existPtTrainJob.getTrainVersion())
                .setOriginUserId(ptTrain.getCreateUserId())
                .setCreateUserId(ptTrain.getCreateUserId());
        int jobResult = ptTrainJobMapper.insert(ptTrainJob);
        if (jobResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} created training Job, failed to insert data in train_job table", currentUser.getUsername());
            throw new BusinessException("内部错误");
        }


        ptJobParam.setTrainJobId(ptTrainJob.getId())
                .setAlgorithmId(ptTrainJobUpdateDTO.getAlgorithmId())
                .setRunCommand(ptTrainJobUpdateDTO.getRunCommand())
                .setImageName(imageName)
                .setRunParams(ptTrainJobUpdateDTO.getRunParams())
                .setCreateUserId(ptTrain.getCreateUserId());
        //保存算法用途
        if (ptTrainJobUpdateDTO.getAlgorithmUsage() != null) {
            ptJobParam.setAlgorithmUsage(ptTrainJobUpdateDTO.getAlgorithmUsage());
        }
        //保存验证数据集算法用途
        if (ptTrainJobUpdateDTO.getValAlgorithmUsage() != null) {
            ptJobParam.setValAlgorithmUsage(ptTrainJobUpdateDTO.getValAlgorithmUsage());
        }
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
        List<PtTrainJob> jobList = new ArrayList<>();
        PtTrain ptTrain = checkAndReturnPtTrain(ptTrainJobDeleteDTO, userContextService.getCurUser(), jobList);

        Collection<Long> jobIdList = new ArrayList<>();
        String taskIdentify = (String) redisUtils.get(trainIdPrefix + String.valueOf(ptTrain.getId()));
        if (null != ptTrainJobDeleteDTO.getId()) {

            //要删除的训练任务
            PtTrainJob ptTrainJob = ptTrainJobMapper.selectById(ptTrainJobDeleteDTO.getId());
            if (ptTrainJob == null) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted training job, pT_train_job table failed to delete data", userContextService.getCurUser().getUsername());
                throw new BusinessException("训练任务已删除或参数不合法");
            }
            //删除job
            deleteJobs(userContextService.getCurUser(), jobList);
            ptTrainJobMapper.deleteById(ptTrainJobDeleteDTO.getId());

            PtTrain updatePtTrain = new PtTrain();
            updatePtTrain.setVersionNum(ptTrain.getVersionNum() - 1);
            UpdateWrapper<PtTrain> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", ptTrain.getId()).eq("version_num", ptTrain.getVersionNum());
            int updateResult = ptTrainMapper.update(updatePtTrain, updateWrapper);
            if (updateResult < 1) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted the training job and updated the version_num in the PT_train table failed", userContextService.getCurUser().getUsername());
                throw new BusinessException("训练任务已删除或参数不合法");
            }

            if (ptTrain.getVersionNum() == 1) {
                int trainResult = ptTrainMapper.deleteById(ptTrain.getId());
                if (StringUtils.isNotEmpty(taskIdentify)) {
                    redisUtils.del(taskIdentify, trainIdPrefix + String.valueOf(ptTrain.getId()));
                }
                if (trainResult < 1) {
                    LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted training job, pt Train table deleted data failed", userContextService.getCurUser().getUsername());
                    throw new BusinessException("训练任务已删除或参数不合法");
                }
            }
            jobIdList.add(ptTrainJobDeleteDTO.getId());

            //回收已删除训练任务无效文件
            String recyclePath = fileStoreApi.formatPath(fileStoreApi.getRootDir() + fileStoreApi.getBucket() + trainJobConfig.getManage() +
                    File.separator + ptTrainJob.getCreateUserId() + File.separator + ptTrainJob.getJobName());
            recycleTaskWithTrain(recyclePath, ptTrainJobDeleteDTO.getId());

        } else {
            deleteTrainAndJob(ptTrainJobDeleteDTO, userContextService.getCurUser(), jobList, ptTrain, jobIdList);
            if (StringUtils.isNotEmpty(taskIdentify)) {
                redisUtils.del(taskIdentify, trainIdPrefix + String.valueOf(ptTrain.getId()));
            }
        }

        //删除pt_job_param表中相关数据
        UpdateWrapper<PtJobParam> updateJobParamWrapper = new UpdateWrapper<>();
        updateJobParamWrapper.in("train_job_id", jobIdList);
        int jobParamResult = ptJobParamMapper.delete(updateJobParamWrapper);
        if (jobParamResult < jobIdList.size()) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted training job, pT_job_param table failed to delete data", userContextService.getCurUser().getUsername());
            throw new BusinessException("内部错误");
        }

        PtTrainJobDeleteVO ptTrainJobDeleteVO = new PtTrainJobDeleteVO();
        BeanUtil.copyProperties(ptTrainJobDeleteDTO, ptTrainJobDeleteVO);
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
    private void deleteTrainAndJob(PtTrainJobDeleteDTO ptTrainJobDeleteDTO, UserContext
            currentUser, List<PtTrainJob> jobList, PtTrain ptTrain, Collection<Long> jobIdList) {
        QueryWrapper<PtTrainJob> query = new QueryWrapper<>();
        query.eq("train_id", ptTrainJobDeleteDTO.getTrainId());
        List<PtTrainJob> ptTrainJobs = ptTrainJobMapper.selectList(query);
        if (ptTrainJobs.size() < 1) {
            throw new BusinessException("没有待删除的训练任务");
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
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted the training job and updated the version_num in the pt_train table failed", currentUser.getUsername());
            throw new BusinessException("内部错误");
        }

        int trainResult = ptTrainMapper.deleteById(ptTrain.getId());
        if (trainResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted training job, pt_train table deleted data failed", currentUser.getUsername());
            throw new BusinessException("内部错误");
        }

        UpdateWrapper<PtTrainJob> updateJobWrapper = new UpdateWrapper<>();
        updateJobWrapper.eq("train_id", ptTrain.getId());
        int jobResult = ptTrainJobMapper.delete(updateJobWrapper);
        if (jobResult < jobIdList.size()) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted training job, pt_train_job table failed to delete data", currentUser.getUsername());
            throw new BusinessException("内部错误");
        }

        //回收已删除训练任务无效文件
        for (PtTrainJob trainJob : ptTrainJobs) {
            String recyclePath = fileStoreApi.formatPath(fileStoreApi.getRootDir() + fileStoreApi.getBucket() + trainJobConfig.getManage() +
                    StrUtil.SLASH + trainJob.getCreateUserId() + StrUtil.SLASH + trainJob.getJobName());
            recycleTaskWithTrain(recyclePath, trainJob.getId());
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
    private PtTrain checkAndReturnPtTrain(PtTrainJobDeleteDTO ptTrainJobDeleteDTO, UserContext currentUser, List<PtTrainJob> jobList) {
        PtTrain ptTrain = ptTrainMapper.selectById(ptTrainJobDeleteDTO.getTrainId());
        if (null == ptTrain) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} failed to delete training job, invalid parameter, as follows {}", currentUser.getUsername(), ptTrainJobDeleteDTO);
            throw new BusinessException("训练任务不存在或已被删除");
        }
        if (null != ptTrainJobDeleteDTO.getId()) {
            PtTrainJob ptTrainJob = ptTrainJobMapper.selectById(ptTrainJobDeleteDTO.getId());
            if (null == ptTrainJob || !ptTrainJob.getTrainId().equals(ptTrainJobDeleteDTO.getTrainId())) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "User {} failed to delete training job, invalid parameter, as follows {}", currentUser.getUsername(), ptTrainJobDeleteDTO);
                throw new BusinessException("训练任务已删除或参数不合法");
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
    private void checkAndReturnPtTrain(PtTrainJobStopDTO ptTrainJobStopDTO, UserContext currentUser, List<PtTrainJob> jobList) {
        PtTrain ptTrain = ptTrainMapper.selectById(ptTrainJobStopDTO.getTrainId());
        if (null == ptTrain) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} stopped the training job failed, the parameter is illegal, the training does not exist, as follows {}", currentUser.getUsername(), ptTrainJobStopDTO);
            throw new BusinessException("参数不合法，该训练不存在");
        }
        if (null != ptTrainJobStopDTO.getId()) {
            PtTrainJob ptTrainJob = ptTrainJobMapper.selectById(ptTrainJobStopDTO.getId());
            if (null == ptTrainJob || !ptTrainJob.getTrainId().equals(ptTrainJobStopDTO.getTrainId()) ||
                    TrainJobStatusEnum.checkStopStatus(ptTrainJob.getTrainStatus())) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "User {} stopped training job failed, invalid parameter, as follows {}", currentUser.getUsername(), ptTrainJobStopDTO);
                throw new BusinessException("参数不合法，该训练不存在");
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
    private void deleteJobs(UserContext currentUser, List<PtTrainJob> jobList) {
        String namespace;
        try {
            for (PtTrainJob job : jobList) {
                namespace = k8sNameTool.generateNamespace(job.getCreateUserId());
                if (TrainJobStatusEnum.STOP.getStatus().equals(job.getTrainStatus())) {
                    boolean bool = TrainTypeEnum.isDistributeTrain(job.getTrainType()) ?
                            distributeTrainApi.deleteByResourceName(namespace, job.getJobName()).isSuccess() :
                            trainJobApi.delete(namespace, job.getJobName());
                    if (!bool) {
                        LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deletes the training job and K8s fails to execute the delete() method, namespace is {}, resourceName is {}",
                                currentUser.getUsername(), namespace, job.getJobName());
                    }
                }
                PtBaseResult ptBaseResult = persistentVolumeClaimApi.recycle(namespace, job.getJobName());
                if (null == ptBaseResult || !ptBaseResult.isSuccess()) {
                    LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted training job, k8s failed to implement the recycle() method, namespace is {}, resourceName is {}",
                            currentUser.getUsername(), namespace, job.getJobName());
                }
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} delete training job, k8s delete failed,exception:{}", currentUser.getUsername(), e);
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
        //训练job名称集合 用于停止k8s
        List<PtTrainJob> jobList = new ArrayList<>();
        checkAndReturnPtTrain(ptTrainJobStopDTO, userContextService.getCurUser(), jobList);

        if (null != ptTrainJobStopDTO.getId()) {
            //停止job
            stopTrainJobAsync.stopJobs(userContextService.getCurUser(), jobList);

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
                throw new BusinessException("没有待停止的job");
            }

            //停止job
            stopTrainJobAsync.stopJobs(userContextService.getCurUser(), jobList);
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
        // 获取运行中的任务
        Integer runCount = ptTrainJobMapper.selectCount(new LambdaQueryWrapper<PtTrainJob>().eq(PtTrainJob::getTrainStatus, TrainJobStatusEnum.RUNNING.getStatus()));
        // 已经完成的任务
        Integer finishCount = ptTrainJobMapper.selectCount(new LambdaQueryWrapper<PtTrainJob>().in(PtTrainJob::getTrainStatus, TrainJobStatusEnum.FAILED.getStatus(),
                TrainJobStatusEnum.STOP.getStatus(),
                TrainJobStatusEnum.SUCCEEDED.getStatus(), TrainJobStatusEnum.UNKNOWN.getStatus()));
        PtTrainJobStatisticsMineVO vo = new PtTrainJobStatisticsMineVO();
        vo.setRunJobCount(runCount);
        vo.setFinishJobCount(finishCount);
        return vo;
    }

    /**
     * 查询数据集对应训练作业job状态
     *
     * @param ptTrainDataSourceStatusQueryDTO 查询数据集对应训练作业job状态参数
     * @return HashedMap<String, Boolean>     数据集路径-是否可以删除 的map集合
     **/
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Boolean> getTrainDataSourceStatus(PtTrainDataSourceStatusQueryDTO ptTrainDataSourceStatusQueryDTO) {
        if (CollectionUtils.isEmpty(ptTrainDataSourceStatusQueryDTO.getDataSourcePath())) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The dataset set {} is empty", ptTrainDataSourceStatusQueryDTO.getDataSourcePath());
            throw new BusinessException("传入参数为空");
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
        return map;
    }

    /**
     * 查询模型是否在训练中
     *
     * @param ptModelStatusQueryDTO 查询模型对应训练作业job状态参数
     * @return Boolean    模型是在使用（true：使用中；false：未使用）
     **/
    @Override
    public Boolean getTrainModelStatus(PtModelStatusQueryDTO ptModelStatusQueryDTO) {
        if (ptModelStatusQueryDTO == null) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The ptModelStatusQueryDTO set is empty");
            throw new BusinessException("模型为空");
        }

        if (CollectionUtils.isNotEmpty(ptModelStatusQueryDTO.getModelIds()) && CollectionUtils.isNotEmpty(ptModelStatusQueryDTO.getModelBranchIds())) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The Modelid and The modelbranchid cannot be passed in at the same time");
            throw new BusinessException("modelId和ModelBranchId不能同时传入");
        }

        QueryWrapper<PtTrainJob> query = new QueryWrapper<>();
        if (CollectionUtils.isNotEmpty(ptModelStatusQueryDTO.getModelIds())) {
            query.in("model_id", ptModelStatusQueryDTO.getModelIds());
        } else if (CollectionUtils.isNotEmpty(ptModelStatusQueryDTO.getModelBranchIds())) {
            query.in("model_branch_id", ptModelStatusQueryDTO.getModelBranchIds());
        } else {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The Modelid and The modelbranchid cannot set is empty in at the same time");
            throw new BusinessException("模型传入参数不合法");
        }
        List<PtTrainJob> ptTrainJobs = ptTrainJobMapper.selectList(query);
        //结果集处理
        for (PtTrainJob ptTrainJob : ptTrainJobs) {
            if (ptTrainJob.getTrainStatus() < TrainJobStatusEnum.SUCCEEDED.getStatus()) {
                return true;
            }
        }
        return false;
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
        //获取训练job参数
        QueryWrapper<PtTrainJob> trainJobQuery = new QueryWrapper<>();
        trainJobQuery.eq("id", ptTrainJobDetailQueryDTO.getId());
        PtTrainJob ptTrainJob = ptTrainJobMapper.selectOne(trainJobQuery);
        if (ptTrainJob == null) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The jobId for the user {} query does not exist", userContextService.getCurUser().getUsername());
            throw new BusinessException("您查询的id不存在或已被删除");
        }
        //获取训练参数
        PtTrain ptTrain = ptTrainMapper.selectById(ptTrainJob.getTrainId());
        //获取训练任务参数
        QueryWrapper<PtJobParam> jobParamQuery = new QueryWrapper<>();
        jobParamQuery.eq("train_job_id", ptTrainJob.getId());
        PtJobParam ptJobParam = ptJobParamMapper.selectOne(jobParamQuery);
        if (ptJobParam == null || ptJobParam.getAlgorithmId() != null && ptJobParam.getAlgorithmId() < MagicNumConstant.ONE) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The algorithm ID corresponding to the jobId is {} query by the user {} does not exist", userContextService.getCurUser().getUsername(), ptTrainJobDetailQueryDTO.getId());
            throw new BusinessException("您查询的jobId对应的算法id不存在或已被删除");
        }
        //获取算法参数
        TrainAlgorithmQureyVO ptTrainAlgorithm = null;
        if (ptJobParam.getAlgorithmId() != null) {

            TrainAlgorithmSelectAllByIdDTO trainAlgorithmSelectAllByIdDTO = new TrainAlgorithmSelectAllByIdDTO();
            trainAlgorithmSelectAllByIdDTO.setId(ptJobParam.getAlgorithmId());
            DataResponseBody<TrainAlgorithmQureyVO> dataResponseBody = algorithmClient.selectAllById(trainAlgorithmSelectAllByIdDTO);
            if (dataResponseBody.succeed()) {
                ptTrainAlgorithm = dataResponseBody.getData();
            }
        }
        //结果集处理
        PtTrainJobDetailQueryVO ptTrainJobDetailQueryVO = new PtTrainJobDetailQueryVO();
        ptTrainJobDetailQueryVO.setK8sNamespace(k8sNameTool.generateNamespace(ptTrainJob.getCreateUserId()));
        BeanUtils.copyProperties(ptTrainJob, ptTrainJobDetailQueryVO);
        ptTrainJobDetailQueryVO.setTrainName(ptTrain.getTrainName()).setAlgorithmId(ptJobParam.getAlgorithmId()).setRunCommand(ptJobParam.getRunCommand())
                .setRunParams(ptJobParam.getRunParams()).setParamF1(ptJobParam.getParamF1()).setParamCallback(ptJobParam.getParamCallback())
                .setParamPrecise(ptJobParam.getParamPrecise()).setParamAccuracy(ptJobParam.getParamAccuracy())
                .setNotebookId(ptJobParam.getNotebookId())
                .setNotebookName(ptJobParam.getNotebookName())
                .setAlgorithmUsage(ptJobParam.getAlgorithmUsage()).setValAlgorithmUsage(ptJobParam.getValAlgorithmUsage());
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
            //从字典中查询模型框架类型值
            DictDetailQueryByLabelNameDTO dictDetailQueryByLabelNameDTO = new DictDetailQueryByLabelNameDTO();
            dictDetailQueryByLabelNameDTO.setName(imageNameSuffixArray[0]);
            DataResponseBody<List<DictDetailVO>> dictDetailDataResponseBody = dictDetailClient.findDictDetailByName(dictDetailQueryByLabelNameDTO);
            if (dictDetailDataResponseBody.succeed()) {
                List<DictDetailVO> dictDetails = dictDetailDataResponseBody.getData();
                if (CollectionUtils.isNotEmpty(dictDetails)) {
                    ptTrainJobDetailQueryVO.setFrameType(Integer.valueOf(dictDetails.get(0).getValue()));
                }
            }
        }
        //拼装算法信息
        if (ptTrainAlgorithm != null) {
            ptTrainJobDetailQueryVO.setAlgorithmName(ptTrainAlgorithm.getAlgorithmName())
                    .setAlgorithmSource(ptTrainAlgorithm.getAlgorithmSource())
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
        // 检查jobId是否合法
        PtTrainJob ptTrainJob = ptTrainJobMapper.selectById(ptTrainJobResumeDTO.getId());
        if (null == ptTrainJob) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "It is illegal for user {} to resume training job and jobId to be {}", userContextService.getCurUser().getUsername(), ptTrainJobResumeDTO.getId());
            throw new BusinessException("您输入的id不存在或已被删除");
        }

        // 获取算法id和运行参数
        QueryWrapper<PtJobParam> jobParamQuery = new QueryWrapper<>();
        jobParamQuery.eq("train_job_id", ptTrainJob.getId());
        PtJobParam ptJobParam = ptJobParamMapper.selectOne(jobParamQuery);
        if (ptJobParam == null || ptJobParam.getAlgorithmId() != null && ptJobParam.getAlgorithmId() < MagicNumConstant.ONE) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The algorithm ID corresponding to the jobId is {} query by the user {} does not exist", userContextService.getCurUser().getUsername(), ptTrainJobResumeDTO.getId());
            throw new BusinessException("您查询的jobId对应的算法id不存在");
        }
        BaseTrainJobDTO baseTrainJobDTO = new BaseTrainJobDTO();
        BeanUtil.copyProperties(ptTrainJob, baseTrainJobDTO);

        //获取算法
        PtTrainJobBaseDTO ptTrainJobBaseDTO = convertPtTrainJobBaseDTO(ptJobParam);

        PtImageAndAlgorithmVO ptImageAndAlgorithmVO = buildPtImageAndAlgorithmVO(ptTrainJobBaseDTO);

        String[] codeDirResult = ptImageAndAlgorithmVO.getCodeDir().split(StrUtil.SLASH);
        String codeDirName = codeDirResult[codeDirResult.length - 1];
        //处理目录问题
        String noEnvPath = StrUtil.SLASH + trainJobConfig.getManage() + StrUtil.SLASH + ptTrainJob.getCreateUserId() + StrUtil.SLASH
                + ptTrainJob.getJobName();
        String commonPath = fileStoreApi.getBucket() + noEnvPath.substring(1);
        String outPath = commonPath + StrUtil.SLASH + trainJobConfig.getModelPath();
        String loadPath = commonPath + StrUtil.SLASH + trainJobConfig.getLoadPath();
        String codePath = commonPath + StrUtil.SLASH + codeDirName;
        String noEnvOut = noEnvPath + StrUtil.SLASH + trainJobConfig.getModelPath();
        String path = ptTrainJobResumeDTO.getPath();
        if (!path.startsWith(noEnvOut)) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "path: {}", path);
            throw new BusinessException("内部错误");
        }
        String modelLoadDir = path.substring(noEnvOut.length());
        String codeAbsolutePath = fileStoreApi.getRootDir() + codePath;
        String loadAbsolutePath = fileStoreApi.getRootDir() + loadPath;
        String outAbsolutePath = fileStoreApi.getRootDir() + outPath;

        FileUtil.del(loadAbsolutePath);
        FileUtil.del(codeAbsolutePath);

        FileUtil.rename(new File(outAbsolutePath), loadAbsolutePath, false, true);
        //获取训练规格信息
        QueryResourceSpecsDTO queryResourceSpecsDTO = new QueryResourceSpecsDTO();
        queryResourceSpecsDTO.setModule(2).setSpecsName(ptTrainJob.getTrainJobSpecsName());
        DataResponseBody<QueryResourceSpecsVO> dataResponseBody = resourceSpecsClient.queryResourceSpecs(queryResourceSpecsDTO);
        QueryResourceSpecsVO queryResourceSpecsVO = null;
        if (dataResponseBody.succeed()) {
            queryResourceSpecsVO = dataResponseBody.getData();
        }
        // 拼load路径
        JSONObject runParams = ptJobParam.getRunParams();
        runParams.put(trainJobConfig.getLoadKey(),
                trainJobConfig.getDockerTrainPath() + StrUtil.SLASH + trainJobConfig.getLoadPath() + modelLoadDir);
        PtTrain ptTrain = ptTrainMapper.selectById(ptTrainJob.getTrainId());
        baseTrainJobDTO.setTrainJobSpecsName(queryResourceSpecsVO.getSpecsName())
                .setPipSitePackagePath(ptImageAndAlgorithmVO.getPipSitePackagePath())
                .setCpuNum(queryResourceSpecsVO.getCpuNum())
                .setGpuNum(queryResourceSpecsVO.getGpuNum())
                .setMemNum(queryResourceSpecsVO.getMemNum())
                .setWorkspaceRequest(queryResourceSpecsVO.getWorkspaceRequest())
                .setTaskIdentify(resourceCache.getTaskIdentify(ptTrain.getId(), ptTrain.getTrainName(), trainIdPrefix));
        baseTrainJobDTO.setResourcesPoolType(queryResourceSpecsVO.getResourcesPoolType() ? MagicNumConstant.ONE : MagicNumConstant.ZERO);
        baseTrainJobDTO.setRunParams(runParams);

        // 初始化训练时间和状态
        PtTrainJob updatePtTrainJob = new PtTrainJob();
        updatePtTrainJob.setId(ptTrainJob.getId())
                .setRuntime(TrainUtil.INIT_RUNTIME)
                .setTrainStatus(TrainJobStatusEnum.PENDING.getStatus())
                .setUpdateTime(new Timestamp(System.currentTimeMillis()));
        int updateResult = ptTrainJobMapper.updateById(updatePtTrainJob);
        if (updateResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} resumed training job, pt train Job table update failed", userContextService.getCurUser().getUsername());
            throw new BusinessException("内部错误");
        }
        // 此处将ptTrainJob的trainStatus和runTime设为null以避免doJob中再次调用updateById错误更新状态和时间
        ptTrainJob.setTrainStatus(null).setRuntime(null).setCreateTime(null);
        // 提交job
        asyncManager.execute(baseTrainJobDTO, ptTrainJob.getCreateUserId(), ptImageAndAlgorithmVO, ptTrainJob);
    }

    /**
     * PtJobParam转换PtTrainJobBaseDTO
     *
     * @param ptJobParam
     * @return
     */
    private PtTrainJobBaseDTO convertPtTrainJobBaseDTO(PtJobParam ptJobParam) {
        PtTrainJobBaseDTO ptTrainJobBaseDTO = new PtTrainJobBaseDTO();
        ptTrainJobBaseDTO.setAlgorithmId(ptJobParam.getAlgorithmId());
        ptTrainJobBaseDTO.setNotebookId(ptJobParam.getNotebookId());
        ptTrainJobBaseDTO.setRunCommand(ptJobParam.getRunCommand());
        if (ptJobParam != null) {
            String imageNameSuffix = ptJobParam.getImageName().substring(ptJobParam.getImageName().lastIndexOf(StrUtil.SLASH) + MagicNumConstant.ONE);
            String[] imageNameSuffixArray = imageNameSuffix.split(StrUtil.COLON);
            ptTrainJobBaseDTO.setImageName(imageNameSuffixArray[0]);
            ptTrainJobBaseDTO.setImageTag(imageNameSuffixArray[1]);
        }
        return ptTrainJobBaseDTO;
    }

    /**
     * 获取job在grafana监控的地址
     *
     * @param jobId 任务ID
     * @return List<PtJobMetricsGrafanaVO> grafana监控的地址信息
     */
    @Override
    public List<PtJobMetricsGrafanaVO> getGrafanaUrl(Long jobId) {
        //通过jobId获取job相关信息
        PtTrainJob ptTrainJob = ptTrainJobMapper.selectById(jobId);
        if (null == ptTrainJob) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "It is illegal for user {} to get grafanaUrl on Job, jobId for {}", userContextService.getCurUser().getUsername(), jobId);
            throw new BusinessException("您输入的id不存在或已被删除");
        }
        List<PtJobMetricsGrafanaVO> list = new ArrayList<>();
        try {
            List<BizPod> bizPodList = podApi.getListByResourceName(k8sNameTool.generateNamespace(ptTrainJob.getCreateUserId()), ptTrainJob.getJobName());
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
            LogUtil.error(LogEnum.BIZ_K8S, "Failed to obtain grafanaUrl of Pod, namespace={}, resourceName is {}, error:{}",
                    k8sNameTool.generateNamespace(ptTrainJob.getCreateUserId()), ptTrainJob.getJobName(), e);
        }
        return list;
    }

    /**
     * 获取训练使用的模型信息
     *
     * @param ptTrainModelDTO
     * @return PtTrainJobModelVO
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public PtTrainJobModelVO getTrainJobModel(PtTrainModelDTO ptTrainModelDTO) {

        PtTrainJobModelVO<ModelVO> ptTrainJobModelVO = new PtTrainJobModelVO();
        Integer modelResource = ptTrainModelDTO.getModelResource();
        PtModelBranchQueryByIdDTO ptModelBranchQueryByIdDTO = new PtModelBranchQueryByIdDTO();
        PtModelInfoQueryByIdDTO ptModelInfoQueryByIdDTO = new PtModelInfoQueryByIdDTO();
        PtModelInfoConditionQueryDTO ptModelInfoConditionQueryDTO = new PtModelInfoConditionQueryDTO();
        switch (ModelResourceEnum.getType(modelResource)) {
            case MINE:
                if (null == ptTrainModelDTO.getModelBranchId() || null == ptTrainModelDTO.getModelId()) {
                    logErrorInfoOnModel(userContextService.getCurUser().getUsername());
                }
                ptModelBranchQueryByIdDTO.setId(ptTrainModelDTO.getModelBranchId());
                DataResponseBody<PtModelBranchQueryVO> dataResponseBody = modelBranchClient.getByBranchId(ptModelBranchQueryByIdDTO);
                PtModelBranchQueryVO ptModelBranch = null;
                if (dataResponseBody.succeed()) {
                    ptModelBranch = dataResponseBody.getData();
                }
                if (null == ptModelBranch) {
                    break;
                }
                if (ptModelBranch.getParentId().compareTo(ptTrainModelDTO.getModelId()) != 0 ||
                        StringUtils.isBlank(ptModelBranch.getModelAddress())) {
                    logErrorInfoOnModel(userContextService.getCurUser().getUsername());
                }
                ptModelInfoQueryByIdDTO.setId(ptModelBranch.getParentId());
                DataResponseBody<PtModelInfoQueryVO> modelInfoDataResponseBody = modelInfoClient.getByModelId(ptModelInfoQueryByIdDTO);
                PtModelInfoQueryVO ptModelInfo = null;
                if (modelInfoDataResponseBody.succeed()) {
                    ptModelInfo = modelInfoDataResponseBody.getData();
                }
                if (null == ptModelInfo || ptModelInfo.getModelResource().compareTo(ptTrainModelDTO.getModelResource()) != 0) {
                    logErrorInfoOnModel(userContextService.getCurUser().getUsername());
                }
                ptTrainJobModelVO.setModelList(new ArrayList<>());
                ptTrainJobModelVO.getModelList()
                        .add(new ModelVO(ptModelInfo.getName(), ptModelBranch.getVersion(), adjustmentUrl(ptModelBranch.getModelAddress())));
                break;
            case PRESET:
                if (null == ptTrainModelDTO.getModelId()) {
                    logErrorInfoOnModel(userContextService.getCurUser().getUsername());
                }
                ptModelInfoQueryByIdDTO.setId(ptTrainModelDTO.getModelId());
                DataResponseBody<PtModelInfoQueryVO> modelInfoPresetDataResponseBody = modelInfoClient.getByModelId(ptModelInfoQueryByIdDTO);
                PtModelInfoQueryVO ptModelInfoPreset = null;
                if (modelInfoPresetDataResponseBody.succeed()) {
                    ptModelInfoPreset = modelInfoPresetDataResponseBody.getData();
                }
                if (null == ptModelInfoPreset) {
                    break;
                }
                if (StringUtils.isBlank(ptModelInfoPreset.getModelAddress()) ||
                        ptModelInfoPreset.getModelResource().compareTo(ptTrainModelDTO.getModelResource()) != 0) {
                    logErrorInfoOnModel(userContextService.getCurUser().getUsername());
                }
                ptTrainJobModelVO.setModelList(new ArrayList<>());
                ptTrainJobModelVO.getModelList()
                        .add(new ModelVO(ptModelInfoPreset.getName(), ptModelInfoPreset.getVersion(), adjustmentUrl(ptModelInfoPreset.getModelAddress())));
                break;
            case ATLAS:
                if (StringUtils.isBlank(ptTrainModelDTO.getTeacherModelIds())) {
                    logErrorInfoOnModel(userContextService.getCurUser().getUsername());
                }
                Set<Long> ids = new HashSet<>();
                Set<Long> teacherModelList = new HashSet<>();
                Arrays.stream(ptTrainModelDTO.getTeacherModelIds().trim().split(SymbolConstant.COMMA))
                        .forEach(id -> teacherModelList.add(Long.parseLong(id)));
                ids.addAll(teacherModelList);

                Set<Long> studentModelList = new HashSet<>();
                if (StringUtils.isNotBlank(ptTrainModelDTO.getStudentModelIds())) {
                    Arrays.stream(ptTrainModelDTO.getStudentModelIds().trim().split(SymbolConstant.COMMA))
                            .forEach(id -> studentModelList.add(Long.parseLong(id)));
                    ids.addAll(studentModelList);
                }
                if (ids.isEmpty()) {
                    logErrorInfoOnModel(userContextService.getCurUser().getUsername());
                }
                ptModelInfoConditionQueryDTO.setIds(ids);
                ptModelInfoConditionQueryDTO.setModelResource(ptTrainModelDTO.getModelResource());
                DataResponseBody<List<PtModelInfoQueryVO>> conditionQueryDataResponseBody = modelInfoClient.getConditionQuery(ptModelInfoConditionQueryDTO);
                List<PtModelInfoQueryVO> modelInfoList = null;
                if (conditionQueryDataResponseBody.succeed()) {
                    modelInfoList = conditionQueryDataResponseBody.getData();
                }
                if (null == modelInfoList || modelInfoList.isEmpty()) {
                    break;
                }

                //保存炼知教师模型信息
                ptTrainJobModelVO.setTeacherModelList(new ArrayList<>());
                List<ModelVO> teacherModelVOS = ptTrainJobModelVO.getTeacherModelList();
                modelInfoList.stream()
                        .filter(modelInfo -> teacherModelList.contains(modelInfo.getId()))
                        .forEach(modelInfo -> teacherModelVOS
                                .add(new ModelVO(modelInfo.getName(), modelInfo.getVersion(), adjustmentUrl(modelInfo.getModelAddress()))));

                //保存炼知学生模型信息
                if (!studentModelList.isEmpty()) {
                    ptTrainJobModelVO.setStudentModelList(new ArrayList<>());
                    List<ModelVO> studentModelVOS = ptTrainJobModelVO.getStudentModelList();
                    modelInfoList.stream()
                            .filter(modelInfo -> studentModelList.contains(modelInfo.getId()))
                            .forEach(modelInfo -> studentModelVOS
                                    .add(new ModelVO(modelInfo.getName(), modelInfo.getVersion(), adjustmentUrl(modelInfo.getModelAddress()))));
                }
                break;
        }

        return ptTrainJobModelVO;
    }

    /**
     * 回收训练任务
     * @param recyclePath 文件路径
     * @param id          训练id
     */
    public void recycleTaskWithTrain(String recyclePath, long id) {
        //创建已删除训练任务的无效文件回收任务
        RecycleCreateDTO recycleCreateDTO = RecycleCreateDTO.builder()
                .recycleModule(RecycleModuleEnum.BIZ_TRAIN.getValue())
                .recycleDelayDate(recycleConfig.getTrainValid())
                .recycleNote(RecycleTool.generateRecycleNote("回收已删除训练任务文件", id))
                .build();
        recycleCreateDTO.addRecycleDetailCreateDTO(RecycleDetailCreateDTO.builder()
                .recycleType(RecycleTypeEnum.FILE.getCode())
                .recycleCondition(recyclePath)
                .recycleNote(RecycleTool.generateRecycleNote("回收已删除训练任务文件", id))
                .build()
        );
        recycleService.createRecycleTask(recycleCreateDTO);
    }

    /**
     * 查询可视化训练列表
     * @param visualTrainQueryDTO  可视化训练查询请求实体
     * @return List<PtTrainJobDetailVO> 可视化训练列表及分页信息
     */
    @Override
    public Map<String, Object> getVisualTrainList(VisualTrainQueryDTO visualTrainQueryDTO) {
        Page page = visualTrainQueryDTO.toPage();
        //排序字段
        String sort = null == visualTrainQueryDTO.getSort() ? TrainConstant.CREATE_TIME : visualTrainQueryDTO.getSort();
        QueryWrapper<PtTrainJob> queryTrainJobWrapper = new QueryWrapper<>();
        queryTrainJobWrapper.isNotNull("visualized_log_path").ne("visualized_log_path", "");
        if (StringConstant.SORT_ASC.equals(visualTrainQueryDTO.getOrder())) {
            queryTrainJobWrapper.orderByAsc(StringUtils.humpToLine(sort));
        } else {
            queryTrainJobWrapper.orderByDesc(StringUtils.humpToLine(sort));
        }
        //根据训练名称筛选
        if (visualTrainQueryDTO.getTrainName() != null) {
            QueryWrapper<PtTrain> queryTrainWrapper = new QueryWrapper<>();
            queryTrainWrapper.like("train_name", visualTrainQueryDTO.getTrainName());
            List<PtTrain> ptTrains = ptTrainMapper.selectList(queryTrainWrapper);
            if (CollectionUtils.isNotEmpty(ptTrains)) {
                List<Long> ptTrainIds = ptTrains.stream().map(PtTrain::getId
                ).collect(Collectors.toList());
                queryTrainJobWrapper.in(true, "train_id", ptTrainIds);
            }
        }
        //根据创建时间筛选
        if (visualTrainQueryDTO.getCreateTime() != null) {
            Timestamp startTime = new Timestamp(Collections.min(visualTrainQueryDTO.getCreateTime()));
            Timestamp endTime = new Timestamp(Collections.max(visualTrainQueryDTO.getCreateTime()));
            queryTrainJobWrapper.ge("create_time", startTime).le("create_time", endTime);
        }
        //根据训练状态筛选
        queryTrainJobWrapper.eq(visualTrainQueryDTO.getTrainStatus() != null, "train_status", visualTrainQueryDTO.getTrainStatus());
        //分页查询
        Page<PtTrainJob> pageTrainResult = ptTrainJobMapper.selectPage(page, queryTrainJobWrapper);
        //结果集处理
        //查询结果数
        page.setTotal(pageTrainResult.getTotal());
        List<PtTrainJob> ptTrainJobs = pageTrainResult.getRecords();
        List<VisualTrainQueryVO> visualTrainQueryVOs = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ptTrainJobs)) {
            //获取训练名称
            List<Long> ptTrainIds = ptTrainJobs.stream().map(PtTrainJob::getTrainId
            ).collect(Collectors.toList());
            List<PtTrain> ptTrains = ptTrainMapper.selectBatchIds(ptTrainIds);
            Map<Long, String> ptTrainMap = ptTrains.stream().collect(Collectors.toMap(PtTrain::getId, PtTrain::getTrainName));
            visualTrainQueryVOs = ptTrainJobs.stream().map(x -> {
                VisualTrainQueryVO visualTrainQueryVO = new VisualTrainQueryVO();
                BeanUtils.copyProperties(x, visualTrainQueryVO);
                if (ptTrainMap.containsKey(x.getTrainId())) {
                    visualTrainQueryVO.setTrainName(ptTrainMap.get(x.getTrainId()));
                }
                return visualTrainQueryVO;
            }).collect(Collectors.toList());
        }
        return PageUtil.toPage(page, visualTrainQueryVOs);
    }

    /**
     * 一键停止所有训练job
     *
     */
    @Override
    public void batchStopTrainJob() {
        //查询所有处于待处理或运行中的训练
        QueryWrapper<PtTrainJob> queryTrainJobWrapper = new QueryWrapper<>();
        queryTrainJobWrapper.in("train_status", TrainJobStatusEnum.PENDING.getStatus(), TrainJobStatusEnum.RUNNING.getStatus());
        List<PtTrainJob> ptTrainJobs = ptTrainJobMapper.selectList(queryTrainJobWrapper);
        if (ptTrainJobs.size() < 1) {
            throw new BusinessException("没有待停止的job");
        }
        //停止job
        stopTrainJobAsync.stopJobs(userContextService.getCurUser(), ptTrainJobs);
    }
}
