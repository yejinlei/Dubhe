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
 * @description ????????????job???????????????
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
     * ??????????????????
     *
     * @param ptTrainQueryDTO ????????????????????????
     * @return Map<String, Object>  ????????????????????????
     **/
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Object> getTrainJob(@NonNull PtTrainQueryDTO ptTrainQueryDTO) {
        Page page = ptTrainQueryDTO.toPage();
        //????????????
        String order = StringConstant.SORT_ASC.equalsIgnoreCase(ptTrainQueryDTO.getOrder()) ? StringConstant.SORT_ASC : StringConstant.SORT_DESC;
        //????????????
        String sortField = FIELD_NAMES.contains(ptTrainQueryDTO.getSort()) ? ptTrainQueryDTO.getSort() : StringConstant.ID;
        String sort = StringUtils.humpToLine(sortField);
        //???????????????????????????????????????
        Long userId = userContextService.getCurUserId();
        if (BaseService.isAdmin(userContextService.getCurUser())) {
            userId = null;
        }
        Page<PtTrainVO> pageTrainResult = ptTrainJobMapper.getPageTrain(page, userId, ptTrainQueryDTO.getTrainStatus(), ptTrainQueryDTO.getTrainName(), sort, order);
        List<PtTrainVO> trainResult = pageTrainResult.getRecords();
        return PageUtil.toPage(page, trainResult);
    }

    /**
     * ??????????????????job????????????
     *
     * @param ptTrainJobVersionQueryDTO ????????????????????????job????????????
     * @return List<PtTrainJobDetailVO> ??????????????????
     **/
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public List<PtTrainJobDetailVO> getTrainJobVersion(PtTrainJobVersionQueryDTO ptTrainJobVersionQueryDTO) {
        //????????????
        checkTrainId(ptTrainJobVersionQueryDTO.getTrainId());
        String sort = null == ptTrainJobVersionQueryDTO.getSort() ? StringConstant.ID : ptTrainJobVersionQueryDTO.getSort();

        QueryWrapper<PtTrainJob> queryTrainJonWrapper = new QueryWrapper<>();
        queryTrainJonWrapper.eq("train_id", ptTrainJobVersionQueryDTO.getTrainId());
        //????????????????????????
        if (ptTrainJobVersionQueryDTO.getTrainStatus() != null) {
            queryTrainJonWrapper.eq("train_status", ptTrainJobVersionQueryDTO.getTrainStatus());
        }
        if (StringConstant.SORT_ASC.equals(ptTrainJobVersionQueryDTO.getOrder())) {
            queryTrainJonWrapper.orderByAsc(StringUtils.humpToLine(sort));
        } else {
            queryTrainJonWrapper.orderByDesc(StringUtils.humpToLine(sort));
        }
        //??????trainId??????
        List<PtTrainJob> ptTrainJobs = ptTrainJobMapper.selectList(queryTrainJonWrapper);
        if (CollectionUtils.isEmpty(ptTrainJobs)) {
            return Collections.emptyList();
        }
        Set<Long> jobIds = ptTrainJobs.stream().map(PtTrainJob::getId).collect(Collectors.toSet());
        QueryWrapper<PtJobParam> queryJobParamWrapper = new QueryWrapper<>();
        queryJobParamWrapper.in("train_job_id", jobIds);
        //????????????????????????
        List<PtJobParam> ptJobParams = ptJobParamMapper.selectList(queryJobParamWrapper);
        List<TrainAlgorithmQureyVO> ptTrainAlgorithms = null;
        if (CollectionUtils.isNotEmpty(ptJobParams)) {
            Set<Long> algorithmIds = ptJobParams.stream().map(PtJobParam::getAlgorithmId).filter(x -> x != null).collect(Collectors.toSet());
            ptTrainAlgorithms = selectAllBatchIds(algorithmIds);
        }
        //??????????????????
        PtTrain ptTrain = ptTrainMapper.selectById(ptTrainJobVersionQueryDTO.getTrainId());
        //???????????????
        return getTrainJobDetail(ptTrainJobs, ptJobParams, ptTrainAlgorithms, ptTrain);
    }

    /**
     * ????????????
     *
     * @param algorithmIds ??????id??????
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
            throw new BusinessException("??????????????????????????????????????????~");
        }
    }

    /**
     * ??????????????? ??????PtTrainJobDetailVO
     *
     * @param ptTrainJobs       ??????????????????
     * @param ptJobParams       ??????????????????
     * @param ptTrainAlgorithms ??????????????????
     * @param ptTrain           ??????
     * @return List<PtTrainJobDetailVO> ??????????????????????????????
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
                //?????????????????????????????????????????????
                if (x.getDelayCreateTime() != null
                        && nowTime < x.getDelayCreateTime().getTime()
                        && TrainJobStatusEnum.checkRunStatus(ptTrainJobDetailVO.getTrainStatus())) {
                    ptTrainJobDetailVO.setDelayCreateCountDown(TrainUtil.getCountDown(x.getDelayCreateTime().getTime()));
                }
                //?????????????????????????????????????????????
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
                //1??????????????????2???????????????
                if (ptTrainAlgorithm.getAlgorithmSource() == MagicNumConstant.ONE) {
                    ptTrainJobDetailVO.setAlgorithmCodeDir(ptTrainAlgorithm.getCodeDir());
                }
            }
        }

        return list;
    }

    /**
     * ??????????????????
     *
     * @param ptJobParam
     * @param ptTrainJobDetailVO
     */
    public void buildImageAndTagInfo(PtJobParam ptJobParam, PtTrainJobDetailVO ptTrainJobDetailVO) {
        //image????????????
        if (StringUtils.isNotBlank(ptJobParam.getImageName())) {
            String imageNameSuffix = ptJobParam.getImageName().substring(ptJobParam.getImageName().lastIndexOf(StrUtil.SLASH) + MagicNumConstant.ONE);
            String[] imageNameSuffixArray = imageNameSuffix.split(StrUtil.COLON);
            ptTrainJobDetailVO.setImageName(imageNameSuffixArray[0]);
            ptTrainJobDetailVO.setImageTag(imageNameSuffixArray[1]);
        }
    }

    /**
     * ????????????????????????job????????????????????????
     *
     * @param trainId ??????ID
     */
    private void checkTrainId(Long trainId) {
        if (null == trainId || trainId < 1) {
            throw new BusinessException("???????????????");
        }
        PtTrain ptTrain = ptTrainMapper.selectById(trainId);
        if (null == ptTrain) {
            throw new BusinessException("????????????????????????????????????");
        }
    }

    /**
     * ??????notebook
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
                throw new BusinessException("??????NoteBook");
            }
            return dataResponseBody.getData();
        } else {
            LogUtil.info(LogEnum.BIZ_TRAIN, "NoteBook service unreachable! Msg is {}", dataResponseBody.getMsg());
            throw new BusinessException("NoteBook????????????????????????????????????~");
        }
    }

    /**
     * ????????????job
     *
     * @param ptTrainJobCreateDTO ????????????job??????
     * @return List<Long>         id??????
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public List<Long> createTrainJobVersion(PtTrainJobCreateDTO ptTrainJobCreateDTO) {

        validatePtTrainJobCreateDTO(ptTrainJobCreateDTO);

        // ????????????trainName??????????????????
        checkTrainName(ptTrainJobCreateDTO.getTrainName(), userContextService.getCurUserId());

        // ??????trainParamName????????????
        if (ptTrainJobCreateDTO.getSaveParams() != null && ptTrainJobCreateDTO.getSaveParams()) {
            checkTrainParamName(ptTrainJobCreateDTO, userContextService.getCurUserId());
            // ??????????????????????????????
            saveParamToDb(ptTrainJobCreateDTO, userContextService.getCurUser());
        }

        //jobKey
        String trainKey = KeyUtil.generateTrainKey(userContextService.getCurUserId());

        //??????
        String version = trainJobConfig.getVersionLabel() + String.format(TrainUtil.FOUR_DECIMAL, 1);
        //??????k8s ???job??????
        String jobName = trainKey + trainJobConfig.getSeparator() + version;

        // ???????????????????????????
        PtImageAndAlgorithmVO ptImageAndAlgorithmVO = buildPtImageAndAlgorithmVO(ptTrainJobCreateDTO);

        //????????????????????????
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

        //?????????  ???harbor.dubhe.ai/notebook/notebook:v1 ?????? harbor??????
        String userImageName = trimHarborAddress(ptImageAndAlgorithmVO.getImageName());
        //???????????????
        PtTrainJob ptTrainJob = saveTrainJobTableData(ptTrainJobCreateDTO, userContextService.getCurUser(), userImageName, trainKey, baseTrainJobDTO);
        //??????????????????
        resourceCache.addTaskCache(taskIdentify, ptTrainJob.getTrainId(), ptTrainJobCreateDTO.getTrainName(), trainIdPrefix);
        // ??????job
        asyncManager.execute(baseTrainJobDTO, userContextService.getCurUserId(), ptImageAndAlgorithmVO, ptTrainJob);
        return Collections.singletonList(ptTrainJob.getTrainId());
    }

    /**
     * ??????harbor??????
     *
     * @param imageName
     * @return
     */
    private String trimHarborAddress(String imageName) {
        return StringUtils.isBlank(imageName) ? StringUtils.EMPTY : imageName.replace(trainHarborConfig.getAddress() + StrUtil.SLASH, StringUtils.EMPTY);
    }

    /**
     * ???????????????????????????VO ????????????????????????
     *
     * @param ptTrainJobBaseDTO
     * @return
     */
    private PtImageAndAlgorithmVO buildPtImageAndAlgorithmVO(PtTrainJobBaseDTO ptTrainJobBaseDTO) {
        PtImageAndAlgorithmVO ptImageAndAlgorithmVO;
        //????????????id??????notebook??????
        if (ptTrainJobBaseDTO.getAlgorithmId() == null) {
            ptImageAndAlgorithmVO = new PtImageAndAlgorithmVO();
            //notebook ??????
            NoteBookVO noteBook = getNoteBook(ptTrainJobBaseDTO.getNotebookId());
            ptImageAndAlgorithmVO.setPipSitePackagePath(noteBook.getPipSitePackagePath());

            ptImageAndAlgorithmVO.setImageName(noteBook.getK8sImageName());
            ptImageAndAlgorithmVO.setIsTrainOut(true);
            ptImageAndAlgorithmVO.setIsTrainModelOut(true);
            //?????????????????????????????? python ???????????????????????????????????????
            ptImageAndAlgorithmVO.setIsVisualizedLog(false);
            ptImageAndAlgorithmVO.setCodeDir(noteBook.getK8sPvcPath());
        } else {
            //?????????????????????????????????????????????????????????
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
     * ????????????
     *
     * @param ptTrainJobCreateDTO
     */
    private void validatePtTrainJobCreateDTO(PtTrainJobCreateDTO ptTrainJobCreateDTO) {
        if (ptTrainJobCreateDTO.getAlgorithmId() == null && ptTrainJobCreateDTO.getNotebookId() == null) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "Neither algorithm's id  nor notebook's id can be null  at the same time");
            throw new BusinessException("??????ID??????notebookId?????????????????????");
        }
        //???????????????(???notebook????????????)???????????????
        if (ptTrainJobCreateDTO.getNotebookId() == null) {
            validateCreateTrainJobWithAlgorithm(ptTrainJobCreateDTO);
        }
    }

    /**
     * ????????????
     *
     * @param ptTrainJobCreateDTO
     */
    private void validateCreateTrainJobWithAlgorithm(PtTrainJobCreateDTO ptTrainJobCreateDTO) {
        if (ptTrainJobCreateDTO.getAlgorithmId() == null) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "Algorithm id is null");
            throw new BusinessException("??????ID????????????~");
        }
    }

    /**
     * ????????????????????????
     *
     * @param ptTrainJobCreateDTO ??????????????????DTO
     * @param currentUser         ??????
     * @param imageName           ????????????
     * @param trainKey            ??????key
     * @param baseTrainJobDTO     ??????????????????
     * @return PtTrain            ??????
     */
    private PtTrainJob saveTrainJobTableData(PtTrainJobCreateDTO ptTrainJobCreateDTO, UserContext currentUser,
                                             String imageName, String trainKey, BaseTrainJobDTO baseTrainJobDTO) {
        // ??????train???
        PtTrain ptTrain = new PtTrain();
        ptTrain.setTrainName(ptTrainJobCreateDTO.getTrainName())
                .setTrainKey(trainKey)
                .setCreateUserId(currentUser.getId());
        int trainResult = ptTrainMapper.insert(ptTrain);
        if (trainResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} creates training job, pt Train table insert data failed", currentUser.getUsername());
            throw new BusinessException("????????????");
        }

        //????????????????????????,??????????????????????????????
        checkModelAndSavePath(currentUser, baseTrainJobDTO);

        // ??????job??????
        PtJobParam ptJobParam = new PtJobParam();

        // ??????train_job???
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
            throw new BusinessException("????????????");
        }


        ptJobParam.setTrainJobId(ptTrainJob.getId())
                .setAlgorithmId(ptTrainJobCreateDTO.getAlgorithmId())
                .setRunCommand(ptTrainJobCreateDTO.getRunCommand())
                .setImageName(imageName)
                .setRunParams(ptTrainJobCreateDTO.getRunParams())
                .setCreateUserId(currentUser.getId());
        //??????????????????
        if (ptTrainJobCreateDTO.getAlgorithmUsage() != null) {
            ptJobParam.setAlgorithmUsage(ptTrainJobCreateDTO.getAlgorithmUsage());
        }
        //?????????????????????????????????
        if (ptTrainJobCreateDTO.getValAlgorithmUsage() != null) {
            ptJobParam.setValAlgorithmUsage(ptTrainJobCreateDTO.getValAlgorithmUsage());
        }
        //??????????????????????????????
        if (ptTrainJobCreateDTO.getDelayCreateTime() != null && ptTrainJobCreateDTO.getDelayCreateTime() > 0) {
            ptJobParam.setDelayCreateTime(TrainUtil.getDelayTime(ptTrainJobCreateDTO.getDelayCreateTime()));
        }
        //??????????????????????????????
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
            throw new BusinessException("????????????");
        }
        return ptTrainJob;
    }

    /**
     * ????????????????????????,??????????????????????????????
     *
     * @param currentUser     ??????
     * @param baseTrainJobDTO ??????????????????
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

                //????????????????????????????????????
                baseTrainJobDTO.setTeacherModelPathList(new ArrayList<>());
                modelInfoList.stream()
                        .filter(modelInfo -> teacherModelList.contains(modelInfo.getId()))
                        .forEach(modelInfo -> baseTrainJobDTO.getTeacherModelPathList().add(adjustmentUrl(modelInfo.getModelAddress())));

                //????????????????????????????????????
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
     * ??????????????????
     *
     * @param modelUrl ????????????
     * @return ????????????
     */
    private String adjustmentUrl(String modelUrl) {
        if (modelUrl.endsWith(SymbolConstant.SLASH)) {
            modelUrl = modelUrl.substring(MagicNumConstant.ZERO, modelUrl.length() - MagicNumConstant.ONE);
        }
        return modelUrl;
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param username
     */
    private void logErrorInfoOnModel(String username) {
        LogUtil.error(LogEnum.BIZ_TRAIN, "User {} operating training job, error on model......", username);
        throw new BusinessException("???????????????????????????");
    }

    /**
     * ??????????????????????????????
     *
     * @param ptTrainJobCreateDTO ??????????????????DTO
     * @param currentUser         ??????
     */
    private void saveParamToDb(PtTrainJobCreateDTO ptTrainJobCreateDTO, UserContext currentUser) {
        PtTrainParam ptTrainParam = new PtTrainParam();
        BeanUtil.copyProperties(ptTrainJobCreateDTO, ptTrainParam);
        //????????????url
        String image = imageUtil.getImageUrl(ptTrainJobCreateDTO, currentUser);
        ptTrainParam.setImageName(image);
        ptTrainParam.setParamName(ptTrainJobCreateDTO.getTrainParamName())
                .setDescription(ptTrainJobCreateDTO.getTrainParamDesc())
                .setRunParams(ptTrainJobCreateDTO.getRunParams())
                .setCreateUserId(currentUser.getId());
        int trainParamResult = ptTrainParamMapper.insert(ptTrainParam);
        if (trainParamResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} created training job, pT_param_param table failed to insert data", currentUser.getUsername());
            throw new BusinessException("????????????");
        }
    }

    /**
     * ???????????????????????????
     *
     * @param algorithmId ??????
     * @return PtImageAndAlgorithmVO ??????
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
            throw new BusinessException(ResponseCode.ERROR, "???id??????????????????????????????????????????????????????");
        }

        if (!AlgorithmStatusEnum.SUCCESS.getCode().equals(ptTrainAlgorithm.getAlgorithmStatus())) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The algorithm ID {} algorithmStatus is {} unusual", algorithmId, ptTrainAlgorithm.getAlgorithmStatus());
            throw new BusinessException(ResponseCode.ERROR, "?????????????????????!");
        }

        PtImageAndAlgorithmVO ptImageAndAlgorithmVO = new PtImageAndAlgorithmVO();
        BeanUtil.copyProperties(ptTrainAlgorithm, ptImageAndAlgorithmVO);

        return ptImageAndAlgorithmVO;
    }

    /**
     * ??????trainName
     *
     * @param trainName ??????trainName
     * @param userId    ????????????ID
     */
    private void checkTrainName(String trainName, Long userId) {
        QueryWrapper<PtTrain> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("train_name", trainName)
                .eq("create_user_id", userId);
        Integer ptTrainCountResult = ptTrainMapper.selectCount(queryWrapper);
        if (ptTrainCountResult > 0) {
            throw new BusinessException("??????????????????????????????");
        }
    }

    /**
     * ??????trainParamName
     *
     * @param ptTrainJobCreateDTO ????????????DTO
     * @param userId              ??????ID
     */
    private void checkTrainParamName(PtTrainJobCreateDTO ptTrainJobCreateDTO, Long userId) {

        String trainParamName = ptTrainJobCreateDTO.getTrainParamName();

        QueryWrapper<PtTrainParam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("param_name", trainParamName)
                .eq("create_user_id", userId);
        Integer ptTrainParamCountResult = ptTrainParamMapper.selectCount(queryWrapper);
        if (ptTrainParamCountResult > 0) {
            throw new BusinessException("??????trainParamName????????????");
        }
    }

    /**
     * ??????k8s ???job??????
     *
     * @param ptTrain ??????
     * @return String ??????
     */
    private String buildVersion(PtTrain ptTrain) {
        return ptTrain.getTrainKey() + trainJobConfig.getSeparator() + trainJobConfig.getVersionLabel() + String.format(TrainUtil.FOUR_DECIMAL, ptTrain.getTotalNum() + 1);
    }

    /**
     * ????????????job
     *
     * @param ptTrainJobUpdateDTO ????????????job??????
     * @return List<Long>           id??????
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public List<Long> updateTrainJob(PtTrainJobUpdateDTO ptTrainJobUpdateDTO) {
        if (ptTrainJobUpdateDTO.getNotebookId() == null && ptTrainJobUpdateDTO.getAlgorithmId() == null) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "Neither algorithm's id  nor notebook's id can be null  at the same time");
            throw new BusinessException("??????ID??????notebookId?????????????????????");
        }
        PtTrainJob existPtTrainJob = ptTrainJobMapper.selectById(ptTrainJobUpdateDTO.getId());
        if (null == existPtTrainJob) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "It is illegal for a user {} to modify a training job, jobId, to {}", userContextService.getCurUser().getUsername(), ptTrainJobUpdateDTO.getId());
            throw new BusinessException("????????????id????????????????????????");
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
        //???????????????
        PtTrainJob ptTrainJob = updateTrainJobTableData(ptTrainJobUpdateDTO, userContextService.getCurUser(), existPtTrainJob, userImageName, ptTrain, baseTrainJobDTO);
        //??????job
        asyncManager.execute(baseTrainJobDTO, ptTrain.getCreateUserId(), ptImageAndAlgorithmVO, ptTrainJob);

        return Collections.singletonList(ptTrainJob.getId());
    }

    /**
     * ???????????????
     *
     * @param ptTrainJobUpdateDTO ??????????????????DTO
     * @param currentUser         ????????????
     * @param existPtTrainJob     ?????????????????????
     * @param imageName           ????????????
     * @param ptTrain             ??????
     * @param baseTrainJobDTO     ??????????????????
     * @return PtTrainJob         ????????????
     */
    private PtTrainJob updateTrainJobTableData(PtTrainJobUpdateDTO ptTrainJobUpdateDTO, UserContext
            currentUser, PtTrainJob existPtTrainJob, String imageName, PtTrain ptTrain, BaseTrainJobDTO baseTrainJobDTO) {

        //????????????????????????,??????????????????????????????
        checkModelAndSavePath(currentUser, baseTrainJobDTO);
        //??????job??????
        PtJobParam ptJobParam = new PtJobParam();
        //??????train_job???
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
            throw new BusinessException("????????????");
        }


        ptJobParam.setTrainJobId(ptTrainJob.getId())
                .setAlgorithmId(ptTrainJobUpdateDTO.getAlgorithmId())
                .setRunCommand(ptTrainJobUpdateDTO.getRunCommand())
                .setImageName(imageName)
                .setRunParams(ptTrainJobUpdateDTO.getRunParams())
                .setCreateUserId(ptTrain.getCreateUserId());
        //??????????????????
        if (ptTrainJobUpdateDTO.getAlgorithmUsage() != null) {
            ptJobParam.setAlgorithmUsage(ptTrainJobUpdateDTO.getAlgorithmUsage());
        }
        //?????????????????????????????????
        if (ptTrainJobUpdateDTO.getValAlgorithmUsage() != null) {
            ptJobParam.setValAlgorithmUsage(ptTrainJobUpdateDTO.getValAlgorithmUsage());
        }
        //??????????????????????????????
        if (ptTrainJobUpdateDTO.getDelayCreateTime() != null && ptTrainJobUpdateDTO.getDelayCreateTime() > 0) {
            ptJobParam.setDelayCreateTime(TrainUtil.getDelayTime(ptTrainJobUpdateDTO.getDelayCreateTime()));
        }
        //??????????????????????????????
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
            throw new BusinessException("????????????");
        }

        //??????pt_train
        PtTrain updatePtTrain = new PtTrain();
        updatePtTrain.setId(ptTrain.getId()).setVersionNum(ptTrain.getVersionNum() + 1)
                .setTotalNum(ptTrain.getTotalNum() + 1).setUpdateUserId(currentUser.getId());
        int updateResult = ptTrainMapper.updateById(updatePtTrain);
        if (updateResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} created training job, pT_train table failed to update version number", currentUser.getUsername());
            throw new BusinessException("????????????");
        }
        return ptTrainJob;
    }

    /**
     * ????????????job
     *
     * @param ptTrainJobDeleteDTO ????????????job??????
     * @return PtTrainJobDeleteVO ????????????????????????
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

            //????????????????????????
            PtTrainJob ptTrainJob = ptTrainJobMapper.selectById(ptTrainJobDeleteDTO.getId());
            if (ptTrainJob == null) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted training job, pT_train_job table failed to delete data", userContextService.getCurUser().getUsername());
                throw new BusinessException("???????????????????????????????????????");
            }
            //??????job
            deleteJobs(userContextService.getCurUser(), jobList);
            ptTrainJobMapper.deleteById(ptTrainJobDeleteDTO.getId());

            PtTrain updatePtTrain = new PtTrain();
            updatePtTrain.setVersionNum(ptTrain.getVersionNum() - 1);
            UpdateWrapper<PtTrain> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", ptTrain.getId()).eq("version_num", ptTrain.getVersionNum());
            int updateResult = ptTrainMapper.update(updatePtTrain, updateWrapper);
            if (updateResult < 1) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted the training job and updated the version_num in the PT_train table failed", userContextService.getCurUser().getUsername());
                throw new BusinessException("???????????????????????????????????????");
            }

            if (ptTrain.getVersionNum() == 1) {
                int trainResult = ptTrainMapper.deleteById(ptTrain.getId());
                if (StringUtils.isNotEmpty(taskIdentify)) {
                    redisUtils.del(taskIdentify, trainIdPrefix + String.valueOf(ptTrain.getId()));
                }
                if (trainResult < 1) {
                    LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted training job, pt Train table deleted data failed", userContextService.getCurUser().getUsername());
                    throw new BusinessException("???????????????????????????????????????");
                }
            }
            jobIdList.add(ptTrainJobDeleteDTO.getId());

            //???????????????????????????????????????
            String recyclePath = fileStoreApi.formatPath(fileStoreApi.getRootDir() + fileStoreApi.getBucket() + trainJobConfig.getManage() +
                    File.separator + ptTrainJob.getCreateUserId() + File.separator + ptTrainJob.getJobName());
            recycleTaskWithTrain(recyclePath, ptTrainJobDeleteDTO.getId());

        } else {
            deleteTrainAndJob(ptTrainJobDeleteDTO, userContextService.getCurUser(), jobList, ptTrain, jobIdList);
            if (StringUtils.isNotEmpty(taskIdentify)) {
                redisUtils.del(taskIdentify, trainIdPrefix + String.valueOf(ptTrain.getId()));
            }
        }

        //??????pt_job_param??????????????????
        UpdateWrapper<PtJobParam> updateJobParamWrapper = new UpdateWrapper<>();
        updateJobParamWrapper.in("train_job_id", jobIdList);
        int jobParamResult = ptJobParamMapper.delete(updateJobParamWrapper);
        if (jobParamResult < jobIdList.size()) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted training job, pT_job_param table failed to delete data", userContextService.getCurUser().getUsername());
            throw new BusinessException("????????????");
        }

        PtTrainJobDeleteVO ptTrainJobDeleteVO = new PtTrainJobDeleteVO();
        BeanUtil.copyProperties(ptTrainJobDeleteDTO, ptTrainJobDeleteVO);
        return ptTrainJobDeleteVO;
    }

    /**
     * ?????????????????????
     *
     * @param ptTrainJobDeleteDTO ??????????????????DTO
     * @param currentUser         ??????
     * @param jobList             ????????????
     * @param ptTrain             ??????
     * @param jobIdList           ??????ID??????
     */
    private void deleteTrainAndJob(PtTrainJobDeleteDTO ptTrainJobDeleteDTO, UserContext
            currentUser, List<PtTrainJob> jobList, PtTrain ptTrain, Collection<Long> jobIdList) {
        QueryWrapper<PtTrainJob> query = new QueryWrapper<>();
        query.eq("train_id", ptTrainJobDeleteDTO.getTrainId());
        List<PtTrainJob> ptTrainJobs = ptTrainJobMapper.selectList(query);
        if (ptTrainJobs.size() < 1) {
            throw new BusinessException("??????????????????????????????");
        }
        ptTrainJobs.forEach(x -> {
            jobList.add(x);
            jobIdList.add(x.getId());
        });

        //??????job
        deleteJobs(currentUser, jobList);

        PtTrain updatePtTrain = new PtTrain();
        updatePtTrain.setVersionNum(0);
        UpdateWrapper<PtTrain> updateTrainWrapper = new UpdateWrapper<>();
        updateTrainWrapper.eq("id", ptTrain.getId()).eq("version_num", ptTrain.getVersionNum());
        int updateResult = ptTrainMapper.update(updatePtTrain, updateTrainWrapper);
        if (updateResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted the training job and updated the version_num in the pt_train table failed", currentUser.getUsername());
            throw new BusinessException("????????????");
        }

        int trainResult = ptTrainMapper.deleteById(ptTrain.getId());
        if (trainResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted training job, pt_train table deleted data failed", currentUser.getUsername());
            throw new BusinessException("????????????");
        }

        UpdateWrapper<PtTrainJob> updateJobWrapper = new UpdateWrapper<>();
        updateJobWrapper.eq("train_id", ptTrain.getId());
        int jobResult = ptTrainJobMapper.delete(updateJobWrapper);
        if (jobResult < jobIdList.size()) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted training job, pt_train_job table failed to delete data", currentUser.getUsername());
            throw new BusinessException("????????????");
        }

        //???????????????????????????????????????
        for (PtTrainJob trainJob : ptTrainJobs) {
            String recyclePath = fileStoreApi.formatPath(fileStoreApi.getRootDir() + fileStoreApi.getBucket() + trainJobConfig.getManage() +
                    StrUtil.SLASH + trainJob.getCreateUserId() + StrUtil.SLASH + trainJob.getJobName());
            recycleTaskWithTrain(recyclePath, trainJob.getId());
        }
    }

    /**
     * ???????????????PtTrain
     *
     * @param ptTrainJobDeleteDTO ??????????????????DTO
     * @param currentUser         ??????
     * @param jobList             ????????????
     * @return PtTrain            ??????
     */
    private PtTrain checkAndReturnPtTrain(PtTrainJobDeleteDTO ptTrainJobDeleteDTO, UserContext currentUser, List<PtTrainJob> jobList) {
        PtTrain ptTrain = ptTrainMapper.selectById(ptTrainJobDeleteDTO.getTrainId());
        if (null == ptTrain) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} failed to delete training job, invalid parameter, as follows {}", currentUser.getUsername(), ptTrainJobDeleteDTO);
            throw new BusinessException("????????????????????????????????????");
        }
        if (null != ptTrainJobDeleteDTO.getId()) {
            PtTrainJob ptTrainJob = ptTrainJobMapper.selectById(ptTrainJobDeleteDTO.getId());
            if (null == ptTrainJob || !ptTrainJob.getTrainId().equals(ptTrainJobDeleteDTO.getTrainId())) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "User {} failed to delete training job, invalid parameter, as follows {}", currentUser.getUsername(), ptTrainJobDeleteDTO);
                throw new BusinessException("???????????????????????????????????????");
            }
            jobList.add(ptTrainJob);
        }
        return ptTrain;
    }

    /**
     * ????????????????????????
     *
     * @param ptTrainJobStopDTO ????????????DTO
     * @param currentUser       ??????
     * @param jobList           ????????????
     */
    private void checkAndReturnPtTrain(PtTrainJobStopDTO ptTrainJobStopDTO, UserContext currentUser, List<PtTrainJob> jobList) {
        PtTrain ptTrain = ptTrainMapper.selectById(ptTrainJobStopDTO.getTrainId());
        if (null == ptTrain) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} stopped the training job failed, the parameter is illegal, the training does not exist, as follows {}", currentUser.getUsername(), ptTrainJobStopDTO);
            throw new BusinessException("????????????????????????????????????");
        }
        if (null != ptTrainJobStopDTO.getId()) {
            PtTrainJob ptTrainJob = ptTrainJobMapper.selectById(ptTrainJobStopDTO.getId());
            if (null == ptTrainJob || !ptTrainJob.getTrainId().equals(ptTrainJobStopDTO.getTrainId()) ||
                    TrainJobStatusEnum.checkStopStatus(ptTrainJob.getTrainStatus())) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "User {} stopped training job failed, invalid parameter, as follows {}", currentUser.getUsername(), ptTrainJobStopDTO);
                throw new BusinessException("????????????????????????????????????");
            }
            jobList.add(ptTrainJob);
        }
    }

    /**
     * ????????????
     *
     * @param currentUser ??????
     * @param jobList     ????????????
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
            throw new BusinessException("????????????");
        }
    }

    /**
     * ????????????job
     *
     * @param ptTrainJobStopDTO ????????????job??????
     * @return PtTrainJobStopVO ????????????????????????
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public PtTrainJobStopVO stopTrainJob(PtTrainJobStopDTO ptTrainJobStopDTO) {
        //??????job???????????? ????????????k8s
        List<PtTrainJob> jobList = new ArrayList<>();
        checkAndReturnPtTrain(ptTrainJobStopDTO, userContextService.getCurUser(), jobList);

        if (null != ptTrainJobStopDTO.getId()) {
            //??????job
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
                throw new BusinessException("??????????????????job");
            }

            //??????job
            stopTrainJobAsync.stopJobs(userContextService.getCurUser(), jobList);
        }

        PtTrainJobStopVO ptTrainJobStopVO = new PtTrainJobStopVO();
        ptTrainJobStopVO.setTrainId(ptTrainJobStopDTO.getTrainId());
        ptTrainJobStopVO.setId(ptTrainJobStopDTO.getId());
        return ptTrainJobStopVO;
    }

    /**
     * ????????????
     *
     * @return PtTrainJobStatisticsMineVO  ??????????????????????????????
     **/
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public PtTrainJobStatisticsMineVO statisticsMine() {
        // ????????????????????????
        Integer runCount = ptTrainJobMapper.selectCount(new LambdaQueryWrapper<PtTrainJob>().eq(PtTrainJob::getTrainStatus, TrainJobStatusEnum.RUNNING.getStatus()));
        // ?????????????????????
        Integer finishCount = ptTrainJobMapper.selectCount(new LambdaQueryWrapper<PtTrainJob>().in(PtTrainJob::getTrainStatus, TrainJobStatusEnum.FAILED.getStatus(),
                TrainJobStatusEnum.STOP.getStatus(),
                TrainJobStatusEnum.SUCCEEDED.getStatus(), TrainJobStatusEnum.UNKNOWN.getStatus()));
        PtTrainJobStatisticsMineVO vo = new PtTrainJobStatisticsMineVO();
        vo.setRunJobCount(runCount);
        vo.setFinishJobCount(finishCount);
        return vo;
    }

    /**
     * ?????????????????????????????????job??????
     *
     * @param ptTrainDataSourceStatusQueryDTO ?????????????????????????????????job????????????
     * @return HashedMap<String, Boolean>     ???????????????-?????????????????? ???map??????
     **/
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Boolean> getTrainDataSourceStatus(PtTrainDataSourceStatusQueryDTO ptTrainDataSourceStatusQueryDTO) {
        if (CollectionUtils.isEmpty(ptTrainDataSourceStatusQueryDTO.getDataSourcePath())) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The dataset set {} is empty", ptTrainDataSourceStatusQueryDTO.getDataSourcePath());
            throw new BusinessException("??????????????????");
        }
        //??????
        List<String> dataSourceList = ptTrainDataSourceStatusQueryDTO.getDataSourcePath().stream().distinct().collect(Collectors.toList());
        QueryWrapper<PtTrainJob> query = new QueryWrapper<>();
        query.in("data_source_path", dataSourceList);
        List<PtTrainJob> ptTrainJobs = ptTrainJobMapper.selectList(query);
        //???????????????
        List<PtTrainDataSourceStatusQueryVO> ptTrainDataSourceStatusQueryList = ptTrainJobs.stream().map(x -> {
            PtTrainDataSourceStatusQueryVO ptTrainDataSourceStatusQuery = new PtTrainDataSourceStatusQueryVO();
            ptTrainDataSourceStatusQuery.setDataSourcePath(x.getDataSourcePath());
            ptTrainDataSourceStatusQuery.setStatus(x.getTrainStatus() >= TrainJobStatusEnum.SUCCEEDED.getStatus());
            return ptTrainDataSourceStatusQuery;
        }).distinct().collect(Collectors.toList());
        //????????????
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
     * ??????????????????????????????
     *
     * @param ptModelStatusQueryDTO ??????????????????????????????job????????????
     * @return Boolean    ?????????????????????true???????????????false???????????????
     **/
    @Override
    public Boolean getTrainModelStatus(PtModelStatusQueryDTO ptModelStatusQueryDTO) {
        if (ptModelStatusQueryDTO == null) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The ptModelStatusQueryDTO set is empty");
            throw new BusinessException("????????????");
        }

        if (CollectionUtils.isNotEmpty(ptModelStatusQueryDTO.getModelIds()) && CollectionUtils.isNotEmpty(ptModelStatusQueryDTO.getModelBranchIds())) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The Modelid and The modelbranchid cannot be passed in at the same time");
            throw new BusinessException("modelId???ModelBranchId??????????????????");
        }

        QueryWrapper<PtTrainJob> query = new QueryWrapper<>();
        if (CollectionUtils.isNotEmpty(ptModelStatusQueryDTO.getModelIds())) {
            query.in("model_id", ptModelStatusQueryDTO.getModelIds());
        } else if (CollectionUtils.isNotEmpty(ptModelStatusQueryDTO.getModelBranchIds())) {
            query.in("model_branch_id", ptModelStatusQueryDTO.getModelBranchIds());
        } else {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The Modelid and The modelbranchid cannot set is empty in at the same time");
            throw new BusinessException("???????????????????????????");
        }
        List<PtTrainJob> ptTrainJobs = ptTrainJobMapper.selectList(query);
        //???????????????
        for (PtTrainJob ptTrainJob : ptTrainJobs) {
            if (ptTrainJob.getTrainStatus() < TrainJobStatusEnum.SUCCEEDED.getStatus()) {
                return true;
            }
        }
        return false;
    }

    /**
     * ??????jobId??????????????????????????????
     *
     * @param ptTrainJobDetailQueryDTO ??????jobId????????????????????????????????????
     * @return PtTrainQueryJobDetailVO ??????jobId????????????????????????????????????
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public PtTrainJobDetailQueryVO getTrainJobDetail(PtTrainJobDetailQueryDTO ptTrainJobDetailQueryDTO) {
        //????????????job??????
        QueryWrapper<PtTrainJob> trainJobQuery = new QueryWrapper<>();
        trainJobQuery.eq("id", ptTrainJobDetailQueryDTO.getId());
        PtTrainJob ptTrainJob = ptTrainJobMapper.selectOne(trainJobQuery);
        if (ptTrainJob == null) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The jobId for the user {} query does not exist", userContextService.getCurUser().getUsername());
            throw new BusinessException("????????????id????????????????????????");
        }
        //??????????????????
        PtTrain ptTrain = ptTrainMapper.selectById(ptTrainJob.getTrainId());
        //????????????????????????
        QueryWrapper<PtJobParam> jobParamQuery = new QueryWrapper<>();
        jobParamQuery.eq("train_job_id", ptTrainJob.getId());
        PtJobParam ptJobParam = ptJobParamMapper.selectOne(jobParamQuery);
        if (ptJobParam == null || ptJobParam.getAlgorithmId() != null && ptJobParam.getAlgorithmId() < MagicNumConstant.ONE) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The algorithm ID corresponding to the jobId is {} query by the user {} does not exist", userContextService.getCurUser().getUsername(), ptTrainJobDetailQueryDTO.getId());
            throw new BusinessException("????????????jobId???????????????id????????????????????????");
        }
        //??????????????????
        TrainAlgorithmQureyVO ptTrainAlgorithm = null;
        if (ptJobParam.getAlgorithmId() != null) {

            TrainAlgorithmSelectAllByIdDTO trainAlgorithmSelectAllByIdDTO = new TrainAlgorithmSelectAllByIdDTO();
            trainAlgorithmSelectAllByIdDTO.setId(ptJobParam.getAlgorithmId());
            DataResponseBody<TrainAlgorithmQureyVO> dataResponseBody = algorithmClient.selectAllById(trainAlgorithmSelectAllByIdDTO);
            if (dataResponseBody.succeed()) {
                ptTrainAlgorithm = dataResponseBody.getData();
            }
        }
        //???????????????
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
        //?????????????????????????????????????????????
        if (ptJobParam.getDelayCreateTime() != null && nowTime < ptJobParam.getDelayCreateTime().getTime() && TrainJobStatusEnum.checkRunStatus(ptTrainJob.getTrainStatus())) {
            ptTrainJobDetailQueryVO.setDelayCreateCountDown(TrainUtil.getCountDown(ptJobParam.getDelayCreateTime().getTime()));
        }
        //?????????????????????????????????????????????
        if (ptJobParam.getDelayDeleteTime() != null && nowTime < ptJobParam.getDelayDeleteTime().getTime() && TrainJobStatusEnum.checkRunStatus(ptTrainJob.getTrainStatus())) {
            ptTrainJobDetailQueryVO.setDelayDeleteCountDown(TrainUtil.getCountDown(ptJobParam.getDelayDeleteTime().getTime()));
        }
        //??????????????????
        if (StringUtils.isNotBlank(ptJobParam.getImageName())) {
            String imageNameSuffix = ptJobParam.getImageName().substring(ptJobParam.getImageName().lastIndexOf(StrUtil.SLASH) + MagicNumConstant.ONE);
            String[] imageNameSuffixArray = imageNameSuffix.split(StrUtil.COLON);
            ptTrainJobDetailQueryVO.setImageName(imageNameSuffixArray[0]);
            ptTrainJobDetailQueryVO.setImageTag(imageNameSuffixArray[1]);
            //???????????????????????????????????????
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
        //??????????????????
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
     * ????????????
     *
     * @param ptTrainJobResumeDTO ????????????????????????
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public void resumeTrainJob(PtTrainJobResumeDTO ptTrainJobResumeDTO) {
        // ??????jobId????????????
        PtTrainJob ptTrainJob = ptTrainJobMapper.selectById(ptTrainJobResumeDTO.getId());
        if (null == ptTrainJob) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "It is illegal for user {} to resume training job and jobId to be {}", userContextService.getCurUser().getUsername(), ptTrainJobResumeDTO.getId());
            throw new BusinessException("????????????id????????????????????????");
        }

        // ????????????id???????????????
        QueryWrapper<PtJobParam> jobParamQuery = new QueryWrapper<>();
        jobParamQuery.eq("train_job_id", ptTrainJob.getId());
        PtJobParam ptJobParam = ptJobParamMapper.selectOne(jobParamQuery);
        if (ptJobParam == null || ptJobParam.getAlgorithmId() != null && ptJobParam.getAlgorithmId() < MagicNumConstant.ONE) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The algorithm ID corresponding to the jobId is {} query by the user {} does not exist", userContextService.getCurUser().getUsername(), ptTrainJobResumeDTO.getId());
            throw new BusinessException("????????????jobId???????????????id?????????");
        }
        BaseTrainJobDTO baseTrainJobDTO = new BaseTrainJobDTO();
        BeanUtil.copyProperties(ptTrainJob, baseTrainJobDTO);

        //????????????
        PtTrainJobBaseDTO ptTrainJobBaseDTO = convertPtTrainJobBaseDTO(ptJobParam);

        PtImageAndAlgorithmVO ptImageAndAlgorithmVO = buildPtImageAndAlgorithmVO(ptTrainJobBaseDTO);

        String[] codeDirResult = ptImageAndAlgorithmVO.getCodeDir().split(StrUtil.SLASH);
        String codeDirName = codeDirResult[codeDirResult.length - 1];
        //??????????????????
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
            throw new BusinessException("????????????");
        }
        String modelLoadDir = path.substring(noEnvOut.length());
        String codeAbsolutePath = fileStoreApi.getRootDir() + codePath;
        String loadAbsolutePath = fileStoreApi.getRootDir() + loadPath;
        String outAbsolutePath = fileStoreApi.getRootDir() + outPath;

        FileUtil.del(loadAbsolutePath);
        FileUtil.del(codeAbsolutePath);

        FileUtil.rename(new File(outAbsolutePath), loadAbsolutePath, false, true);
        //????????????????????????
        QueryResourceSpecsDTO queryResourceSpecsDTO = new QueryResourceSpecsDTO();
        queryResourceSpecsDTO.setModule(2).setSpecsName(ptTrainJob.getTrainJobSpecsName());
        DataResponseBody<QueryResourceSpecsVO> dataResponseBody = resourceSpecsClient.queryResourceSpecs(queryResourceSpecsDTO);
        QueryResourceSpecsVO queryResourceSpecsVO = null;
        if (dataResponseBody.succeed()) {
            queryResourceSpecsVO = dataResponseBody.getData();
        }
        // ???load??????
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

        // ??????????????????????????????
        PtTrainJob updatePtTrainJob = new PtTrainJob();
        updatePtTrainJob.setId(ptTrainJob.getId())
                .setRuntime(TrainUtil.INIT_RUNTIME)
                .setTrainStatus(TrainJobStatusEnum.PENDING.getStatus())
                .setUpdateTime(new Timestamp(System.currentTimeMillis()));
        int updateResult = ptTrainJobMapper.updateById(updatePtTrainJob);
        if (updateResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} resumed training job, pt train Job table update failed", userContextService.getCurUser().getUsername());
            throw new BusinessException("????????????");
        }
        // ?????????ptTrainJob???trainStatus???runTime??????null?????????doJob???????????????updateById???????????????????????????
        ptTrainJob.setTrainStatus(null).setRuntime(null).setCreateTime(null);
        // ??????job
        asyncManager.execute(baseTrainJobDTO, ptTrainJob.getCreateUserId(), ptImageAndAlgorithmVO, ptTrainJob);
    }

    /**
     * PtJobParam??????PtTrainJobBaseDTO
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
     * ??????job???grafana???????????????
     *
     * @param jobId ??????ID
     * @return List<PtJobMetricsGrafanaVO> grafana?????????????????????
     */
    @Override
    public List<PtJobMetricsGrafanaVO> getGrafanaUrl(Long jobId) {
        //??????jobId??????job????????????
        PtTrainJob ptTrainJob = ptTrainJobMapper.selectById(jobId);
        if (null == ptTrainJob) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "It is illegal for user {} to get grafanaUrl on Job, jobId for {}", userContextService.getCurUser().getUsername(), jobId);
            throw new BusinessException("????????????id????????????????????????");
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
     * ?????????????????????????????????
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

                //??????????????????????????????
                ptTrainJobModelVO.setTeacherModelList(new ArrayList<>());
                List<ModelVO> teacherModelVOS = ptTrainJobModelVO.getTeacherModelList();
                modelInfoList.stream()
                        .filter(modelInfo -> teacherModelList.contains(modelInfo.getId()))
                        .forEach(modelInfo -> teacherModelVOS
                                .add(new ModelVO(modelInfo.getName(), modelInfo.getVersion(), adjustmentUrl(modelInfo.getModelAddress()))));

                //??????????????????????????????
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
     * ??????????????????
     * @param recyclePath ????????????
     * @param id          ??????id
     */
    public void recycleTaskWithTrain(String recyclePath, long id) {
        //??????????????????????????????????????????????????????
        RecycleCreateDTO recycleCreateDTO = RecycleCreateDTO.builder()
                .recycleModule(RecycleModuleEnum.BIZ_TRAIN.getValue())
                .recycleDelayDate(recycleConfig.getTrainValid())
                .recycleNote(RecycleTool.generateRecycleNote("?????????????????????????????????", id))
                .build();
        recycleCreateDTO.addRecycleDetailCreateDTO(RecycleDetailCreateDTO.builder()
                .recycleType(RecycleTypeEnum.FILE.getCode())
                .recycleCondition(recyclePath)
                .recycleNote(RecycleTool.generateRecycleNote("?????????????????????????????????", id))
                .build()
        );
        recycleService.createRecycleTask(recycleCreateDTO);
    }

    /**
     * ???????????????????????????
     * @param visualTrainQueryDTO  ?????????????????????????????????
     * @return List<PtTrainJobDetailVO> ????????????????????????????????????
     */
    @Override
    public Map<String, Object> getVisualTrainList(VisualTrainQueryDTO visualTrainQueryDTO) {
        Page page = visualTrainQueryDTO.toPage();
        //????????????
        String sort = null == visualTrainQueryDTO.getSort() ? TrainConstant.CREATE_TIME : visualTrainQueryDTO.getSort();
        QueryWrapper<PtTrainJob> queryTrainJobWrapper = new QueryWrapper<>();
        queryTrainJobWrapper.isNotNull("visualized_log_path").ne("visualized_log_path", "");
        if (StringConstant.SORT_ASC.equals(visualTrainQueryDTO.getOrder())) {
            queryTrainJobWrapper.orderByAsc(StringUtils.humpToLine(sort));
        } else {
            queryTrainJobWrapper.orderByDesc(StringUtils.humpToLine(sort));
        }
        //????????????????????????
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
        //????????????????????????
        if (visualTrainQueryDTO.getCreateTime() != null) {
            Timestamp startTime = new Timestamp(Collections.min(visualTrainQueryDTO.getCreateTime()));
            Timestamp endTime = new Timestamp(Collections.max(visualTrainQueryDTO.getCreateTime()));
            queryTrainJobWrapper.ge("create_time", startTime).le("create_time", endTime);
        }
        //????????????????????????
        queryTrainJobWrapper.eq(visualTrainQueryDTO.getTrainStatus() != null, "train_status", visualTrainQueryDTO.getTrainStatus());
        //????????????
        Page<PtTrainJob> pageTrainResult = ptTrainJobMapper.selectPage(page, queryTrainJobWrapper);
        //???????????????
        //???????????????
        page.setTotal(pageTrainResult.getTotal());
        List<PtTrainJob> ptTrainJobs = pageTrainResult.getRecords();
        List<VisualTrainQueryVO> visualTrainQueryVOs = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ptTrainJobs)) {
            //??????????????????
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
     * ????????????????????????job
     *
     */
    @Override
    public void batchStopTrainJob() {
        //????????????????????????????????????????????????
        QueryWrapper<PtTrainJob> queryTrainJobWrapper = new QueryWrapper<>();
        queryTrainJobWrapper.in("train_status", TrainJobStatusEnum.PENDING.getStatus(), TrainJobStatusEnum.RUNNING.getStatus());
        List<PtTrainJob> ptTrainJobs = ptTrainJobMapper.selectList(queryTrainJobWrapper);
        if (ptTrainJobs.size() < 1) {
            throw new BusinessException("??????????????????job");
        }
        //??????job
        stopTrainJobAsync.stopJobs(userContextService.getCurUser(), ptTrainJobs);
    }
}
