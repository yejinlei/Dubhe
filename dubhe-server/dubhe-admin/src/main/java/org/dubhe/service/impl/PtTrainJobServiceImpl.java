/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.base.ResponseCode;
import org.dubhe.config.TrainHarborConfig;
import org.dubhe.constant.AlgorithmSourceEnum;
import org.dubhe.constant.TrainJobConstant;
import org.dubhe.dao.*;
import org.dubhe.data.constant.Constant;
import org.dubhe.domain.dto.*;
import org.dubhe.domain.entity.*;
import org.dubhe.domain.vo.*;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.TrainJobStatusEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.k8s.api.PersistentVolumeClaimApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.api.TrainJobApi;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.service.PtTrainJobService;
import org.dubhe.task.TransactionAsyncManager;
import org.dubhe.utils.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
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
    private TrainJobConstant trainJobConstant;

    @Autowired
    private NfsUtil nfsUtil;

    @Autowired
    private TrainHarborConfig trainHarborConfig;

    @Autowired
    private K8sNameTool k8sNameTool;

    @Autowired
    private ImageUtil imageUtil;

    @Autowired
    private TransactionAsyncManager asyncManager;

    public final static List<String> filedNames;

    static {
        filedNames = ReflectionUtils.getFieldNames(PtTrainVO.class);
    }

    /**
     * 作业列表展示
     *
     * @param ptTrainQueryDTO       查询作业列表参数
     * @return Map<String, Object>  作业列表分页信息
     **/
    @Override
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
            String sortField = filedNames.contains(ptTrainQueryDTO.getSort()) ? ptTrainQueryDTO.getSort() : Constant.ID;
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
     * 计算job训练时长
     *
     * @param bizPod   pod信息
     * @return String 训练时长
     */
    private String calculateRuntime(BizPod bizPod) {
        return calculateRuntime(bizPod, (x) -> {
        });
    }

    /**
     * 计算job训练时长
     *
     * @param bizPod
     * @param consumer  pod已经完成状态的回调函数
     * @return res      返回训练时长
     */
    private String calculateRuntime(BizPod bizPod, Consumer<String> consumer) {
        Long completedTime;
        if (StringUtils.isBlank(bizPod.getStartTime())) {
            return "";
        }
        Long startTime = transformTime(bizPod.getStartTime());
        boolean hasCompleted = StringUtils.isNotBlank(bizPod.getCompletedTime());
        completedTime = hasCompleted ? transformTime(bizPod.getCompletedTime()) : LocalDateTime.now().toEpochSecond(ZoneOffset.of(trainJobConstant.getPlusEight()));
        Long time = completedTime - startTime;
        String res = DubheDateUtil.convert2Str(time);
        if (hasCompleted) {
            consumer.accept(res);
        }
        return res;
    }

    /**
     * 时间转换
     *
     * @param  time 时间
     * @return Long 时间戳
     */
    private Long transformTime(String time) {
        LocalDateTime localDateTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        //没有根据时区做处理, 默认当前为东八区
        localDateTime = localDateTime.plusHours(Long.valueOf(trainJobConstant.getEight()));
        return localDateTime.toEpochSecond(ZoneOffset.of(trainJobConstant.getPlusEight()));
    }

    /**
     * 作业不同版本job列表展示
     *
     * @param ptTrainJobVersionQueryDTO 查询作业不同版本job列表参数
     * @return List<PtTrainJobDetailVO> 训练详情集合
     **/
    @Override
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
        String images = imageUtil.getImages(ptTrainJobCreateDTO, currentUser);
        ptImageAndAlgorithmVO.setImageName(trainHarborConfig.getAddress() + StrUtil.SLASH + images).setRunCommand(ptTrainJobCreateDTO.getRunCommand());

        // 获取规格
        PtTrainJobSpecs ptTrainJobSpecs = getSpecs(ptTrainJobCreateDTO.getTrainJobSpecsId(), currentUser);
        //jobKey
        String trainKey = KeyUtil.generateTrainKey(currentUser.getId());
        //版本
        String version = trainJobConstant.getVersionLabel() + String.format(TrainUtil.FOUR_DECIMAL, 1);
        //生成k8s 的job名称
        String jobName = trainKey + trainJobConstant.getSeparator() + version;

        BaseTrainJobDTO baseTrainJobDTO = new BaseTrainJobDTO();
        BeanUtil.copyProperties(ptTrainJobCreateDTO, baseTrainJobDTO);
        baseTrainJobDTO.setJobName(jobName);
        baseTrainJobDTO.setPtTrainJobSpecs(ptTrainJobSpecs);

        //结果集处理
        PtTrainJob ptTrainJob = saveTrainJobTableData(ptTrainJobCreateDTO, currentUser, images, trainKey, jobName);
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
     * @param jobName             训练名称
     * @return PtTrain            训练
     */
    private PtTrainJob saveTrainJobTableData(PtTrainJobCreateDTO ptTrainJobCreateDTO, UserDTO currentUser, String imageName, String trainKey, String jobName) {
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

        // 添加train_job表
        PtTrainJob ptTrainJob = new PtTrainJob();
        BeanUtil.copyProperties(ptTrainJobCreateDTO, ptTrainJob);
        ptTrainJob.setTrainId(ptTrain.getId())
                .setTrainVersion(trainJobConstant.getVersionLabel().toUpperCase() + String.format(TrainUtil.FOUR_DECIMAL, 1))
                .setJobName(jobName)
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
        int jobParamResult = ptJobParamMapper.insert(ptJobParam);
        if (jobParamResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} created training job, pT_job_parAM table insert data failed", currentUser.getUsername());
            throw new BusinessException("内部错误");
        }
        return ptTrainJob;
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
        String images = imageUtil.getImages(ptTrainJobCreateDTO, currentUser);
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
            throw new BusinessException(ResponseCode.SUCCESS, "该id的记录没有相应的镜像或者算法目录配置");
        }
        if (!(userId.equals(ptTrainAlgorithm.getCreateUserId()) || AlgorithmSourceEnum.PRE.getStatus().equals(ptTrainAlgorithm.getAlgorithmSource()))) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The data {} does not belong to the user {}!", ptTrainAlgorithm, userId);
            throw new BusinessException(ResponseCode.SUCCESS, "该数据不属于该用户!");
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
        return ptTrain.getTrainKey() + trainJobConstant.getSeparator() + trainJobConstant.getVersionLabel() + String.format(TrainUtil.FOUR_DECIMAL, ptTrain.getTotalNum() + 1);
    }

    /**
     * 修改训练job
     *
     * @param ptTrainJobUpdateDTO   修改训练job参数
     * @return List<Long>           id集合
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
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
        String images = imageUtil.getImages(ptTrainJobUpdateDTO, currentUser);
        ptImageAndAlgorithmVO.setImageName(trainHarborConfig.getAddress() + StrUtil.SLASH + images).setRunCommand(ptTrainJobUpdateDTO.getRunCommand());

        //获取规格
        PtTrainJobSpecs ptTrainJobSpecs = getSpecs(ptTrainJobUpdateDTO.getTrainJobSpecsId(), currentUser);

        PtTrain ptTrain = ptTrainMapper.selectById(existPtTrainJob.getTrainId());

        String jobName = buildVersion(ptTrain);

        BaseTrainJobDTO baseTrainJobDTO = new BaseTrainJobDTO();
        BeanUtil.copyProperties(ptTrainJobUpdateDTO, baseTrainJobDTO);
        baseTrainJobDTO.setJobName(jobName);
        baseTrainJobDTO.setPtTrainJobSpecs(ptTrainJobSpecs);
        //结果集处理
        PtTrainJob ptTrainJob = updateTrainJobTableData(ptTrainJobUpdateDTO, currentUser, existPtTrainJob, images, ptTrain, jobName);
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
     * @param jobName             任务名称
     * @return PtTrainJob         训练任务
     */
    private PtTrainJob updateTrainJobTableData(PtTrainJobUpdateDTO ptTrainJobUpdateDTO, UserDTO
            currentUser, PtTrainJob existPtTrainJob, String imageName, PtTrain ptTrain, String jobName) {
        //添加train_job表
        PtTrainJob ptTrainJob = new PtTrainJob();
        BeanUtil.copyProperties(ptTrainJobUpdateDTO, ptTrainJob);
        ptTrainJob.setTrainId(ptTrain.getId()).setTrainVersion(trainJobConstant.getVersionLabel().toUpperCase() + String.format(TrainUtil.FOUR_DECIMAL, ptTrain.getTotalNum() + 1))
                .setJobName(jobName).setParentTrainVersion(existPtTrainJob.getTrainVersion())
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
    public PtTrainJobDeleteVO deleteTrainJob(PtTrainJobDeleteDTO ptTrainJobDeleteDTO) {
        UserDTO currentUser = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} deletes the training job and receives the parameter {}", currentUser.getUsername(), ptTrainJobDeleteDTO);

        List<PtTrainJob> jobList = new ArrayList<>();
        PtTrain ptTrain = checkAndReturnPtTrain(ptTrainJobDeleteDTO, currentUser, jobList);

        Collection<Long> jobIdList = new ArrayList<>();
        if (null != ptTrainJobDeleteDTO.getId()) {

            //删除job
            deleteJobs(currentUser, jobList);

            int jobResult = ptTrainJobMapper.deleteById(ptTrainJobDeleteDTO.getId());
            if (jobResult < 1) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "User {} deleted training job, pT_train_job table failed to delete data", currentUser.getUsername());
                throw new BusinessException(ResponseCode.SUCCESS, "训练任务已删除或参数不合法");
            }

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
     * 检测停止训练任务DTO
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
        String namespace = k8sNameTool.generateNameSpace(currentUser.getId());
        try {
            for (PtTrainJob job : jobList) {
                if (TrainJobStatusEnum.STOP.getStatus().equals(job.getTrainStatus())) {
                    boolean bool = trainJobApi.delete(namespace, job.getJobName());
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
    public PtTrainJobStopVO stopTrainJob(PtTrainJobStopDTO ptTrainJobStopDTO) {
        UserDTO currentUser = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} stops training Job and receives the parameter {}", currentUser.getUsername(), ptTrainJobStopDTO);
        //训练job名称集合 用于停止k8s
        List<PtTrainJob> jobList = new ArrayList<>();
        checkAndReturnPtTrain(ptTrainJobStopDTO, currentUser, jobList);

        if (null != ptTrainJobStopDTO.getId()) {
            //停止job
            stopJobs(currentUser, jobList);

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
            stopJobs(currentUser, jobList);
        }

        //更新job状态
        updateJobStatus(currentUser, jobList);

        PtTrainJobStopVO ptTrainJobStopVO = new PtTrainJobStopVO();
        ptTrainJobStopVO.setTrainId(ptTrainJobStopDTO.getTrainId());
        ptTrainJobStopVO.setId(ptTrainJobStopDTO.getId());
        return ptTrainJobStopVO;
    }

    /**
     * 更新训练状态
     *
     * @param currentUser 用户
     * @param jobList     任务集合
     */
    private void updateJobStatus(UserDTO currentUser, List<PtTrainJob> jobList) {
        for (PtTrainJob ptTrainJob : jobList) {
            PtTrainJob updateTrainJob = new PtTrainJob();
            updateTrainJob.setId(ptTrainJob.getId()).setRuntime(ptTrainJob.getRuntime()).setTrainStatus(TrainJobStatusEnum.STOP.getStatus());
            int updateResult = ptTrainJobMapper.updateById(updateTrainJob);
            if (updateResult < 1) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "User {} stops training job, pT_train_job table fails to update status, the information is as follows {}", currentUser.getUsername(), updateTrainJob);
                throw new BusinessException(ResponseCode.SUCCESS, "没有待停止的job");
            }
        }
    }

    /**
     * 任务统计
     *
     * @return PtTrainJobStatisticsMineVO  我的训练任务统计结果
     **/
    @Override
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
     * 停止任务
     *
     * @param currentUser 用户
     * @param jobList     任务集合
     */
    void stopJobs(UserDTO currentUser, List<PtTrainJob> jobList) {
        String namespace = k8sNameTool.generateNameSpace(currentUser.getId());
        jobList.forEach(job -> {
            BizPod bizPod = podApi.getWithResourceName(namespace, job.getJobName());
            if (!bizPod.isSuccess()) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "User {} stops training Job return code:{},message:{}", currentUser.getUsername(), Integer.valueOf(bizPod.getCode()), bizPod.getMessage());
            }
            boolean bool = trainJobApi.delete(namespace, job.getJobName());
            if (!bool) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "User {} stops training Job and K8S fails in the stop process, namespace为{}, resourceName为{}",
                        currentUser.getUsername(), namespace, job.getJobName());
            }
            job.setRuntime(calculateRuntime(bizPod));
        });
    }

    /**
     * 查询训练作业job状态
     *
     * @param ptTrainDataSourceStatusQueryDTO 查询训练作业job状态参数
     * @return HashedMap<String, Boolean>     数据集路径-是否可以删除 的map集合
     **/
    @Override
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
    public PtTrainJobDetailQueryVO getTrainJobDetail(PtTrainJobDetailQueryDTO ptTrainJobDetailQueryDTO) {
        //从会话中获取用户信息
        UserDTO user = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "The user {} starts by querying jobId={} for training task details", user.getUsername(), ptTrainJobDetailQueryDTO.getId());
        QueryWrapper<PtTrainJob> trainJobQuery = new QueryWrapper<>();
        trainJobQuery.eq("create_user_id", user.getId());
        trainJobQuery.eq("id", ptTrainJobDetailQueryDTO.getId());
        //获取训练
        PtTrainJob ptTrainJob = ptTrainJobMapper.selectOne(trainJobQuery);
        if (ptTrainJob == null) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The jobId for the user {} query does not exist", user.getUsername());
            throw new BusinessException(ResponseCode.SUCCESS, "您查询的id不存在或已被删除");
        }
        QueryWrapper<PtJobParam> jobParamQuery = new QueryWrapper<>();
        jobParamQuery.eq("train_job_id", ptTrainJob.getId());
        //获取训练任务参数
        PtJobParam ptJobParam = ptJobParamMapper.selectOne(jobParamQuery);
        if (ptJobParam == null || ptJobParam.getAlgorithmId() < MagicNumConstant.ONE) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The algorithm ID corresponding to the jobId={} query by the user {} does not exist", user.getUsername(), ptTrainJobDetailQueryDTO.getId());
            throw new BusinessException(ResponseCode.SUCCESS, "您查询的jobId对应的算法id不存在或已被删除");
        }
        //获取算法
        PtTrainAlgorithm ptTrainAlgorithm = ptTrainAlgorithmMapper.selectAllById(ptJobParam.getAlgorithmId());
        //拼装job Detail信息
        PtTrainJobDetailQueryVO ptTrainJobDetailQueryVO = new PtTrainJobDetailQueryVO();
        BeanUtils.copyProperties(ptTrainJob, ptTrainJobDetailQueryVO);
        ptTrainJobDetailQueryVO.setAlgorithmId(ptJobParam.getAlgorithmId()).setRunCommand(ptJobParam.getRunCommand()).setRunParams(ptJobParam.getRunParams())
                .setParamF1(ptJobParam.getParamF1()).setParamCallback(ptJobParam.getParamCallback()).setParamPrecise(ptJobParam.getParamPrecise()).setParamAccuracy(ptJobParam.getParamAccuracy());
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
        }

        return ptTrainJobDetailQueryVO;
    }

    /**
     * 恢复训练
     *
     * @param ptTrainJobResumeDTO 恢复训练请求参数
     */
    @Override
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
        ptImageAndAlgorithmVO.setImageName(ptJobParam.getImageName()).setRunCommand(ptJobParam.getRunCommand());
        //获取规格
        PtTrainJobSpecs ptTrainJobSpecs = getSpecs(ptTrainJob.getTrainJobSpecsId(), currentUser);

        //处理目录问题
        String commonPath = nfsUtil.getNfsConfig().getBucket() + trainJobConstant.getManage() + StrUtil.SLASH
                + currentUser.getId() + StrUtil.SLASH + ptTrainJob.getJobName();
        String outPath = commonPath + StrUtil.SLASH + trainJobConstant.getOutPath();
        String loadPath = commonPath + StrUtil.SLASH + trainJobConstant.getLoadPath();
        String modelLoadDirName = nfsUtil.find2ndNewDir(outPath);
        if ("".equals(modelLoadDirName)) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "outPath: {}", outPath);
            throw new BusinessException(ResponseCode.ERROR, "该任务没有前序结果可以继续训练");
        }
        nfsUtil.deleteDirOrFile(loadPath);
        nfsUtil.renameDir(outPath, loadPath);

        // 拼load路径
        JSONObject runParams = ptJobParam.getRunParams();
        runParams.put(trainJobConstant.getLoadKey(), trainJobConstant.getDockerTrainPath() + StrUtil.SLASH +
                trainJobConstant.getLoadPath() + StrUtil.SLASH + modelLoadDirName);
        BaseTrainJobDTO baseTrainJobDTO = new BaseTrainJobDTO();
        BeanUtil.copyProperties(ptTrainJob, baseTrainJobDTO);
        baseTrainJobDTO.setPtTrainJobSpecs(ptTrainJobSpecs);
        baseTrainJobDTO.setRunParams(runParams);

        // 初始化训练时间和状态
        PtTrainJob updatePtTrainJob = new PtTrainJob();
        updatePtTrainJob.setId(ptTrainJob.getId()).setRuntime("").setTrainStatus(TrainJobStatusEnum.PENDING.getStatus());
        int updateResult = ptTrainJobMapper.updateById(updatePtTrainJob);
        if (updateResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} resumed training job, pt train Job table update failed", currentUser.getUsername());
            throw new BusinessException("内部错误");
        }
        // 提交job
        asyncManager.execute(baseTrainJobDTO, currentUser, ptImageAndAlgorithmVO, ptTrainJob);
    }

    /**
     * 获取job在grafana监控的地址
     *
     * @param jobId                  任务ID
     * @return PtJobMetricsGrafanaVO grafana监控的地址信息
     */
    @Override
    public PtJobMetricsGrafanaVO getGrafanaUrl(Long jobId) {

        UserDTO currentUser = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} gets grafanaUrl of Job and receives parameter [jobId] {}", currentUser.getUsername(), jobId);

        //通过jobId获取job相关信息
        PtTrainJob ptTrainJob = ptTrainJobMapper.selectById(jobId);
        if (null == ptTrainJob || !currentUser.getId().equals(ptTrainJob.getCreateUserId())) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "It is illegal for user {} to get grafanaUrl on Job, jobId for {}", currentUser.getUsername(), jobId);
            throw new BusinessException(ResponseCode.SUCCESS, "您输入的id不存在或已被删除，请重新输入");
        }

        String podMetricsGrafanaUrl = podApi
                .getPodMetricsGrafanaUrl(k8sNameTool.generateNameSpace(currentUser.getId()), ptTrainJob.getJobName());

        PtJobMetricsGrafanaVO ptJobMetricsGrafanaVO = new PtJobMetricsGrafanaVO();
        ptJobMetricsGrafanaVO.setJobMetricsGrafanaUrl(podMetricsGrafanaUrl);
        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} completes getting grafanaUrl on job, receives {} parameter [jobId], returns {} result",
                currentUser.getUsername(), jobId, ptJobMetricsGrafanaVO);
        return ptJobMetricsGrafanaVO;
    }
}
