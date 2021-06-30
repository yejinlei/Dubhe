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
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.dto.*;
import org.dubhe.biz.base.enums.DatasetTypeEnum;
import org.dubhe.biz.base.enums.ModelResourceEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.PtModelBranchQueryVO;
import org.dubhe.biz.base.vo.PtModelInfoQueryVO;
import org.dubhe.biz.base.vo.TrainAlgorithmQureyVO;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.permission.annotation.DataPermissionMethod;
import org.dubhe.train.client.AlgorithmClient;
import org.dubhe.train.client.ModelBranchClient;
import org.dubhe.train.client.ModelInfoClient;
import org.dubhe.train.config.TrainJobConfig;
import org.dubhe.train.dao.PtTrainParamMapper;
import org.dubhe.train.domain.dto.*;
import org.dubhe.train.domain.entity.PtTrainParam;
import org.dubhe.train.domain.vo.PtTrainParamQueryVO;
import org.dubhe.train.service.PtTrainParamService;
import org.dubhe.train.utils.ImageUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description 任务参数服务实现类
 * @date 2020-04-27
 */
@Service
public class PtTrainParamServiceImpl implements PtTrainParamService {

    @Autowired
    private PtTrainParamMapper ptTrainParamMapper;

    @Autowired
    private AlgorithmClient algorithmClient;

    @Autowired
    private ImageUtil imageUtil;

    @Autowired
    private ModelBranchClient modelBranchClient;

    @Autowired
    private ModelInfoClient modelInfoClient;

    @Autowired
    private UserContextService userContextService;

    /**
     * 参数列表展示
     *
     * @param ptTrainParamQueryDTO  任务参数列表展示条件
     * @return Map<String, Object>  任务参数列表分页数据
     **/
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Object> getTrainParam(PtTrainParamQueryDTO ptTrainParamQueryDTO) {
        Page page = ptTrainParamQueryDTO.toPage();
        //查询任务参数列表
        QueryWrapper<PtTrainParam> query = new QueryWrapper<>();
        //根据任务参数名称模糊搜索
        if (ptTrainParamQueryDTO.getParamName() != null) {
            query.like("param_name", ptTrainParamQueryDTO.getParamName());
        }
        //根据类型筛选
        if (ptTrainParamQueryDTO.getResourcesPoolType() != null) {
            query.eq("resources_pool_type", ptTrainParamQueryDTO.getResourcesPoolType());
        }
        IPage<PtTrainParam> ptTrainParams;
        try {
            if (ptTrainParamQueryDTO.getSort() == null || ptTrainParamQueryDTO.getSort().equalsIgnoreCase(TrainJobConfig.ALGORITHM_NAME)) {
                query.orderByDesc(StringConstant.ID);
            } else {
                if (StringConstant.SORT_ASC.equalsIgnoreCase(ptTrainParamQueryDTO.getOrder())) {
                    query.orderByAsc(StringUtils.humpToLine(ptTrainParamQueryDTO.getSort()));
                } else {
                    query.orderByDesc(StringUtils.humpToLine(ptTrainParamQueryDTO.getSort()));
                }
            }
            ptTrainParams = ptTrainParamMapper.selectPage(page, query);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "Query task parameter list shows exception {}", e);
            throw new BusinessException("内部错误");
        }
        //结果集的处理
        //获取算法id
        Set<Long> algorithmIds = ptTrainParams.getRecords().stream().map(PtTrainParam::getAlgorithmId).collect(Collectors.toSet());
        List<PtTrainParamQueryVO> ptTrainParamQueryResult = new ArrayList<>();
        if (algorithmIds.size() < 1) {
            return PageUtil.toPage(page, ptTrainParamQueryResult);
        }
        TrainAlgorithmSelectAllBatchIdDTO trainAlgorithmSelectAllBatchIdDTO = new TrainAlgorithmSelectAllBatchIdDTO();
        trainAlgorithmSelectAllBatchIdDTO.setIds(algorithmIds);
        DataResponseBody<List<TrainAlgorithmQureyVO>> dataResponseBody = algorithmClient.selectAllBatchIds(trainAlgorithmSelectAllBatchIdDTO);
        List<TrainAlgorithmQureyVO> ptTrainAlgorithms = null;
        if (dataResponseBody.succeed()) {
            ptTrainAlgorithms = dataResponseBody.getData();
        }
        //获取算法id对应的算法名称并封装至map集合中
        Map<Long, String> ptTrainAlgorithmMap = ptTrainAlgorithms.stream().collect(Collectors.toMap(TrainAlgorithmQureyVO::getId, TrainAlgorithmQureyVO::getAlgorithmName, (o, n) -> n));
        ptTrainParamQueryResult = ptTrainParams.getRecords().stream().map(x -> {
            PtTrainParamQueryVO ptTrainParamQueryVO = new PtTrainParamQueryVO();
            BeanUtils.copyProperties(x, ptTrainParamQueryVO);
            ptTrainParamQueryVO.setAlgorithmName(ptTrainAlgorithmMap.get(x.getAlgorithmId()));
            //获取镜像名称与版本
            getImageNameAndImageTag(x, ptTrainParamQueryVO);
            return ptTrainParamQueryVO;
        }).collect(Collectors.toList());
        return PageUtil.toPage(page, ptTrainParamQueryResult);
    }

    /**
     * 保存任务参数
     *
     * @param ptTrainParamCreateDTO 保存任务参数条件
     * @return List<Long>           保存任务参数id集合
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public List<Long> createTrainParam(PtTrainParamCreateDTO ptTrainParamCreateDTO) {
        //参数校验
        TrainAlgorithmQureyVO ptTrainAlgorithm = checkCreateTrainParam(ptTrainParamCreateDTO, userContextService.getCurUser());
        //获取算法来源（1为我的算法，2为预置算法）
        Integer algorithmSource = ptTrainAlgorithm.getAlgorithmSource();
        //保存任务参数
        PtTrainParam ptTrainParam = new PtTrainParam();
        //模型检测
        BaseTrainParamDTO baseTrainParamDTO = new BaseTrainParamDTO();
        BeanUtil.copyProperties(ptTrainParamCreateDTO, baseTrainParamDTO);
        checkModel(userContextService.getCurUser(), baseTrainParamDTO);

        BeanUtils.copyProperties(ptTrainParamCreateDTO, ptTrainParam);
        //获取镜像
        String images = imageUtil.getImageUrl(ptTrainParamCreateDTO, userContextService.getCurUser());
        ptTrainParam.setImageName(images).setAlgorithmSource(algorithmSource).setCreateUserId(userContextService.getCurUserId());
        int insertResult = ptTrainParamMapper.insert(ptTrainParam);
        //任务参数未保存成功，抛出异常，并返回失败信息
        if (insertResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The user {} saved the task parameters successfully, and the pt_train_param table insert operation failed", userContextService.getCurUser().getUsername());
            throw new BusinessException("内部错误");
        }
        //返回新增任务参数id
        return Collections.singletonList(ptTrainParam.getId());
    }

    /**
     * 检查模型是否合法
     *
     * @param currentUser         用户
     * @param baseTrainParamDTO   基础训练模板参数
     */
    private void checkModel(UserContext currentUser, BaseTrainParamDTO baseTrainParamDTO) {

        Integer modelResource = baseTrainParamDTO.getModelResource();
        if (null == modelResource) {
            if (null == baseTrainParamDTO.getModelId() &&
                    StringUtils.isBlank(baseTrainParamDTO.getStudentModelIds()) &&
                    StringUtils.isBlank(baseTrainParamDTO.getStudentModelIds())) {
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
                if (null == baseTrainParamDTO.getModelBranchId() || null == baseTrainParamDTO.getModelId() ||
                        StringUtils.isNotBlank(baseTrainParamDTO.getTeacherModelIds()) ||
                        StringUtils.isNotBlank(baseTrainParamDTO.getStudentModelIds())) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                ptModelBranchQueryByIdDTO.setId(baseTrainParamDTO.getModelBranchId());
                DataResponseBody<PtModelBranchQueryVO> dataResponseBody = modelBranchClient.getByBranchId(ptModelBranchQueryByIdDTO);
                PtModelBranchQueryVO ptModelBranch = null;
                if (dataResponseBody.succeed()) {
                    ptModelBranch = dataResponseBody.getData();
                }
                if (null == ptModelBranch || ptModelBranch.getParentId().compareTo(baseTrainParamDTO.getModelId()) != 0 ||
                        StringUtils.isBlank(ptModelBranch.getModelAddress())) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                ptModelInfoQueryByIdDTO.setId(ptModelBranch.getParentId());
                DataResponseBody<PtModelInfoQueryVO> modelInfoDataResponseBody = modelInfoClient.getByModelId(ptModelInfoQueryByIdDTO);
                PtModelInfoQueryVO ptModelInfo = null;
                if (modelInfoDataResponseBody.succeed()) {
                    ptModelInfo = modelInfoDataResponseBody.getData();
                }
                if (null == ptModelInfo || ptModelInfo.getModelResource().compareTo(baseTrainParamDTO.getModelResource()) != 0) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                break;
            case PRESET:
                if (null == baseTrainParamDTO.getModelId() || StringUtils.isNotBlank(baseTrainParamDTO.getTeacherModelIds()) ||
                        StringUtils.isNotBlank(baseTrainParamDTO.getStudentModelIds())) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                ptModelInfoQueryByIdDTO.setId(baseTrainParamDTO.getModelId());
                DataResponseBody<PtModelInfoQueryVO> modelInfoPresetDataResponseBody = modelInfoClient.getByModelId(ptModelInfoQueryByIdDTO);
                PtModelInfoQueryVO ptModelInfoPreset = null;
                if (modelInfoPresetDataResponseBody.succeed()) {
                    ptModelInfoPreset = modelInfoPresetDataResponseBody.getData();
                }
                if (null == ptModelInfoPreset || StringUtils.isBlank(ptModelInfoPreset.getModelAddress()) ||
                        ptModelInfoPreset.getModelResource().compareTo(baseTrainParamDTO.getModelResource()) != 0) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                break;
            case ATLAS:
                if (StringUtils.isBlank(baseTrainParamDTO.getTeacherModelIds()) || null != baseTrainParamDTO.getModelId()) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                Set<Long> ids = new HashSet<>();
                Set<Long> teacherModelList = new HashSet<>();
                Arrays.stream(baseTrainParamDTO.getTeacherModelIds().trim().split(SymbolConstant.COMMA))
                        .forEach(id -> teacherModelList.add(Long.parseLong(id)));
                ids.addAll(teacherModelList);

                Set<Long> studentModelList = new HashSet<>();
                if (StringUtils.isNotBlank(baseTrainParamDTO.getStudentModelIds())) {
                    Arrays.stream(baseTrainParamDTO.getStudentModelIds().trim().split(SymbolConstant.COMMA))
                            .forEach(id -> studentModelList.add(Long.parseLong(id)));
                    ids.addAll(studentModelList);
                }
                if (ids.isEmpty()) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                ptModelInfoConditionQueryDTO.setIds(ids);
                ptModelInfoConditionQueryDTO.setModelResource(baseTrainParamDTO.getModelResource());
                DataResponseBody<List<PtModelInfoQueryVO>> conditionQueryDataResponseBody = modelInfoClient.getConditionQuery(ptModelInfoConditionQueryDTO);
                List<PtModelInfoQueryVO> modelInfoList = null;
                if (conditionQueryDataResponseBody.succeed()) {
                    modelInfoList = conditionQueryDataResponseBody.getData();
                }
                if (null == modelInfoList || modelInfoList.size() < ids.size()) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                break;
        }
    }

    /**
     * 打印训练模板中模型相关的错误日志
     *
     * @param username
     */
    private void logErrorInfoOnModel(String username) {
        LogUtil.error(LogEnum.BIZ_TRAIN, "User {} operating training param, error on model......", username);
        throw new BusinessException("模型参数不合法");
    }

    /**
     * 修改任务参数
     *
     * @param ptTrainParamUpdateDTO 修改任务参数条件
     * @return List<Long>           修改任务参数id集合
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public List<Long> updateTrainParam(PtTrainParamUpdateDTO ptTrainParamUpdateDTO) {
        //参数校验
        checkUpdateTrainParam(ptTrainParamUpdateDTO, userContextService.getCurUser());
        //修改任务参数
        PtTrainParam ptTrainParam = new PtTrainParam();
        //模型检测
        BaseTrainParamDTO baseTrainParamDTO = new BaseTrainParamDTO();
        BeanUtil.copyProperties(ptTrainParamUpdateDTO, baseTrainParamDTO);
        checkModel(userContextService.getCurUser(), baseTrainParamDTO);

        BeanUtils.copyProperties(ptTrainParamUpdateDTO, ptTrainParam);
        ptTrainParam.setUpdateUserId(userContextService.getCurUserId());
        //获取镜像url
        String images = imageUtil.getImageUrl(ptTrainParamUpdateDTO, userContextService.getCurUser());
        //添加镜像url
        ptTrainParam.setImageName(images);
        try {
            //任务参数未修改成功，抛出异常，并返回失败信息
            ptTrainParamMapper.updateById(ptTrainParam);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The user {} failed to modify the task parameters. The modify pt_train_param failed. The reason is {}", userContextService.getCurUser().getUsername(), e.getMessage());
            throw new BusinessException("内部错误");
        }

        //返回修改任务参数id
        return Collections.singletonList(ptTrainParam.getId());
    }

    /**
     * 删除任务参数
     *
     * @param ptTrainParamDeleteDTO 删除任务参数条件
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public void deleteTrainParam(PtTrainParamDeleteDTO ptTrainParamDeleteDTO) {
        Set<Long> idList = ptTrainParamDeleteDTO.getIds();
        //参数校验
        checkDeleteTrainParam(ptTrainParamDeleteDTO, userContextService.getCurUser(), idList);
        //删除任务参数
        int deleteCountResult = ptTrainParamMapper.deleteBatchIds(idList);
        //任务参数未删除成功,抛出异常，并返回失败信息
        if (deleteCountResult < idList.size()) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The user {} failed to delete the task parameter, and delete pt_train_param  failed, ids are {}", userContextService.getCurUser().getUsername(), ptTrainParamDeleteDTO.getIds());
            throw new BusinessException("内部错误");
        }
    }

    /**
     * 获取镜像名称与版本
     *
     * @param trainParam                   镜像URL
     * @param ptTrainParamQueryVO          镜像名称与版本
     */
    private void getImageNameAndImageTag(PtTrainParam trainParam, PtTrainParamQueryVO ptTrainParamQueryVO) {
        if (StringUtils.isNotBlank(trainParam.getImageName())) {
            String imageNameSuffix = trainParam.getImageName().substring(trainParam.getImageName().lastIndexOf(StrUtil.SLASH) + MagicNumConstant.ONE);
            String[] imageNameSuffixArray = imageNameSuffix.split(StrUtil.COLON);
            ptTrainParamQueryVO.setImageName(imageNameSuffixArray[0]);
            ptTrainParamQueryVO.setImageTag(imageNameSuffixArray[1]);
        }
    }

    /**
     * 创建训练参数模板参数校验
     *
     * @param ptTrainParamCreateDTO 任务参数创建条件
     * @param user                  用户
     * @return PtTrainAlgorithm     算法
     */
    private TrainAlgorithmQureyVO checkCreateTrainParam(PtTrainParamCreateDTO ptTrainParamCreateDTO, UserContext user) {
        //算法id校验
        TrainAlgorithmSelectAllByIdDTO trainAlgorithmSelectAllByIdDTO = new TrainAlgorithmSelectAllByIdDTO();
        trainAlgorithmSelectAllByIdDTO.setId(ptTrainParamCreateDTO.getAlgorithmId());
        DataResponseBody<TrainAlgorithmQureyVO> dataResponseBody = algorithmClient.selectAllById(trainAlgorithmSelectAllByIdDTO);
        TrainAlgorithmQureyVO ptTrainAlgorithm = null;
        if (dataResponseBody.succeed()) {
            ptTrainAlgorithm = dataResponseBody.getData();
        }
        if (ptTrainAlgorithm == null) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "Algorithm ID  {} does not exist", ptTrainParamCreateDTO.getAlgorithmId());
            throw new BusinessException("算法不存在或已被删除");
        }
        //任务参数名称校验
        QueryWrapper<PtTrainParam> query = new QueryWrapper<>();
        query.eq("param_name", ptTrainParamCreateDTO.getParamName());
        Integer trainParamCountResult = ptTrainParamMapper.selectCount(query);
        if (trainParamCountResult > 0) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The task parameter name {} already exists", ptTrainParamCreateDTO.getParamName());
            throw new BusinessException("任务参数名称已存在");
        }
        return ptTrainAlgorithm;
    }

    /**
     * 修改训练参数模板参数校验
     *
     * @param ptTrainParamUpdateDTO 训练参数模板参数
     * @param user                  用户
     */
    private void checkUpdateTrainParam(PtTrainParamUpdateDTO ptTrainParamUpdateDTO, UserContext user) {
        //任务参数id校验
        PtTrainParam ptTrainParam = ptTrainParamMapper.selectById(ptTrainParamUpdateDTO.getId());
        if (ptTrainParam == null) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The task parameter ID {} does not exist", ptTrainParamUpdateDTO.getId());
            throw new BusinessException("任务不存在或已被删除");
        }
        //算法id校验
        TrainAlgorithmSelectByIdDTO trainAlgorithmSelectByIdDTO = new TrainAlgorithmSelectByIdDTO();
        trainAlgorithmSelectByIdDTO.setId(ptTrainParamUpdateDTO.getAlgorithmId());
        DataResponseBody<TrainAlgorithmQureyVO> dataResponseBody = algorithmClient.selectById(trainAlgorithmSelectByIdDTO);
        TrainAlgorithmQureyVO ptTrainAlgorithm = null;
        if (dataResponseBody.succeed()) {
            ptTrainAlgorithm = dataResponseBody.getData();
        }
        if (ptTrainAlgorithm == null) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "Algorithm ID {} does not exist", ptTrainParamUpdateDTO.getAlgorithmId());
            throw new BusinessException("算法id不存在");
        }
        //权限校验
        QueryWrapper<PtTrainParam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", ptTrainParamUpdateDTO.getId());
        Integer countResult = ptTrainParamMapper.selectCount(queryWrapper);
        if (countResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The user {} failed to modify the task parameters and has no permission to modify the corresponding data in the pt_train_param table", user.getUsername());
            throw new BusinessException("您修改的ID不存在或已被删除");
        }
        //任务参数名称校验
        QueryWrapper<PtTrainParam> query = new QueryWrapper<>();
        query.eq("param_name", ptTrainParamUpdateDTO.getParamName());
        PtTrainParam trainParam = ptTrainParamMapper.selectOne(query);
        if (trainParam != null && !ptTrainParamUpdateDTO.getId().equals(trainParam.getId())) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The task parameter name {} already exists", ptTrainParamUpdateDTO.getParamName());
            throw new BusinessException("任务参数名称已存在");
        }
    }

    /**
     * 删除训练参数模板参数校验
     *
     * @param ptTrainParamDeleteDTO 训练参数模板参数
     * @param user                  用户
     * @param idList                训练参数id集合
     **/
    private void checkDeleteTrainParam(PtTrainParamDeleteDTO ptTrainParamDeleteDTO, UserContext user, Set<Long> idList) {
        //id校验
        List<PtTrainParam> ptTrainParams = ptTrainParamMapper.selectBatchIds(idList);
        if (ptTrainParams.size() == 0 || ptTrainParams.size() != idList.size()) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} failed to delete the task parameters, request parameters ids ={} cannot query the corresponding data in pt_train_param table, the parameters are illegal", user.getUsername(), ptTrainParamDeleteDTO.getIds());
            throw new BusinessException("您删除的ID不存在或已被删除");
        }
        //权限校验
        QueryWrapper<PtTrainParam> query = new QueryWrapper<>();
        query.in("id", idList);
        Integer queryCountResult = ptTrainParamMapper.selectCount(query);
        if (queryCountResult < idList.size()) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} failed to delete the task parameters and has no permission to delete the corresponding data in the pt_train_param table", user.getUsername());
            throw new BusinessException("您删除的ID不存在或已被删除");
        }
    }

}
