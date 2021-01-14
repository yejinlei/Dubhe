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
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.annotation.DataPermissionMethod;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.base.ResponseCode;
import org.dubhe.config.TrainJobConfig;
import org.dubhe.constant.SymbolConstant;
import org.dubhe.dao.PtModelBranchMapper;
import org.dubhe.dao.PtModelInfoMapper;
import org.dubhe.dao.PtTrainAlgorithmMapper;
import org.dubhe.dao.PtTrainParamMapper;
import org.dubhe.data.constant.Constant;
import org.dubhe.domain.PtModelBranch;
import org.dubhe.domain.PtModelInfo;
import org.dubhe.domain.dto.BaseTrainParamDTO;
import org.dubhe.domain.dto.PtTrainParamCreateDTO;
import org.dubhe.domain.dto.PtTrainParamDeleteDTO;
import org.dubhe.domain.dto.PtTrainParamQueryDTO;
import org.dubhe.domain.dto.PtTrainParamUpdateDTO;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.domain.entity.PtTrainAlgorithm;
import org.dubhe.domain.entity.PtTrainParam;
import org.dubhe.domain.vo.PtTrainParamQueryVO;
import org.dubhe.enums.DatasetTypeEnum;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.ModelResourceEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.service.PtTrainParamService;
import org.dubhe.utils.ImageUtil;
import org.dubhe.utils.JwtUtils;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.PageUtil;
import org.dubhe.utils.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private PtTrainAlgorithmMapper ptTrainAlgorithmMapper;

    @Autowired
    private ImageUtil imageUtil;

    @Autowired
    private PtModelInfoMapper ptModelInfoMapper;

    @Autowired
    private PtModelBranchMapper ptModelBranchMapper;

    /**
     * 参数列表展示
     *
     * @param ptTrainParamQueryDTO  任务参数列表展示条件
     * @return Map<String, Object>  任务参数列表分页数据
     **/
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Object> getTrainParam(PtTrainParamQueryDTO ptTrainParamQueryDTO) {
        //从会话中获取用户信息
        UserDTO user = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "The display of user {} query task parameter list begins. The parameters received are {}.", user.getUsername(), ptTrainParamQueryDTO);
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
                query.orderByDesc(Constant.ID);
            } else {
                if (Constant.SORT_ASC.equalsIgnoreCase(ptTrainParamQueryDTO.getOrder())) {
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
        List<Long> algorithmIds = ptTrainParams.getRecords().stream().map(PtTrainParam::getAlgorithmId).collect(Collectors.toList());
        if (algorithmIds.size() < 1) {
            LogUtil.info(LogEnum.BIZ_TRAIN, "User {} query task parameter list is empty, the result is {}", user.getUsername(), ptTrainParams);
            List<PtTrainParamQueryVO> ptTrainParamQueryResult = new ArrayList<>();
            return PageUtil.toPage(page, ptTrainParamQueryResult);
        }
        List<PtTrainAlgorithm> ptTrainAlgorithms = ptTrainAlgorithmMapper.selectAllBatchIds(algorithmIds);
        //获取算法id对应的算法名称并封装至map集合中
        Map<Long, String> ptTrainAlgorithmMap = ptTrainAlgorithms.stream().collect(Collectors.toMap(PtTrainAlgorithm::getId, PtTrainAlgorithm::getAlgorithmName, (o, n) -> n));
        List<PtTrainParamQueryVO> ptTrainParamQueryResult = ptTrainParams.getRecords().stream().map(x -> {
            PtTrainParamQueryVO ptTrainParamQueryVO = new PtTrainParamQueryVO();
            BeanUtils.copyProperties(x, ptTrainParamQueryVO);
            ptTrainParamQueryVO.setAlgorithmName(ptTrainAlgorithmMap.get(x.getAlgorithmId()));
            //获取镜像名称与版本
            getImageNameAndImageTag(x, ptTrainParamQueryVO);
            return ptTrainParamQueryVO;
        }).collect(Collectors.toList());
        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} query task parameter list display ends, the result is {}", user.getUsername(), ptTrainParamQueryResult);
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
        //从会话中获取用户信息
        UserDTO user = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "The user {} saves the task parameter and receives the parameter {}.", user.getUsername(), ptTrainParamCreateDTO);
        //参数校验
        PtTrainAlgorithm ptTrainAlgorithm = checkCreateTrainParam(ptTrainParamCreateDTO, user);
        //获取算法来源（1为我的算法，2为预置算法）
        Integer algorithmSource = ptTrainAlgorithm.getAlgorithmSource();
        //保存任务参数
        PtTrainParam ptTrainParam = new PtTrainParam();
        //模型检测
        BaseTrainParamDTO baseTrainParamDTO = new BaseTrainParamDTO();
        BeanUtil.copyProperties(ptTrainParamCreateDTO, baseTrainParamDTO);
        checkModel(user, baseTrainParamDTO);

        BeanUtils.copyProperties(ptTrainParamCreateDTO, ptTrainParam);
        //获取镜像
        String images = imageUtil.getImageUrl(ptTrainParamCreateDTO, user);
        ptTrainParam.setImageName(images).setAlgorithmSource(algorithmSource).setCreateUserId(user.getId());
        int insertResult = ptTrainParamMapper.insert(ptTrainParam);
        //任务参数未保存成功，抛出异常，并返回失败信息
        if (insertResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The user {} saved the task parameters successfully, and the pt_train_param table insert operation failed", user.getUsername());
            throw new BusinessException("内部错误");
        }
        //返回新增任务参数id
        LogUtil.info(LogEnum.BIZ_TRAIN, "End of user {} saving task parameters, return new task parameter ID ={}", user.getUsername(), ptTrainParam.getId());
        return Collections.singletonList(ptTrainParam.getId());
    }

    /**
     * 检查模型是否合法
     *
     * @param currentUser         用户
     * @param baseTrainParamDTO   基础训练模板参数
     */
    private void checkModel(UserDTO currentUser, BaseTrainParamDTO baseTrainParamDTO) {

        Integer modelResource = baseTrainParamDTO.getModelResource();
        if(null == modelResource ) {
            if(null == baseTrainParamDTO.getModelId() &&
                    StringUtils.isBlank(baseTrainParamDTO.getStudentModelIds()) &&
                    StringUtils.isBlank(baseTrainParamDTO.getStudentModelIds())) {
                return;
            } else {
                logErrorInfoOnModel(currentUser.getUsername());
            }
        }
        switch (ModelResourceEnum.getType(modelResource)) {
            case MINE:
                if(null == baseTrainParamDTO.getModelBranchId() || null == baseTrainParamDTO.getModelId() ||
                        StringUtils.isNotBlank(baseTrainParamDTO.getTeacherModelIds()) ||
                        StringUtils.isNotBlank(baseTrainParamDTO.getStudentModelIds())) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                PtModelBranch ptModelBranch = ptModelBranchMapper.selectById(baseTrainParamDTO.getModelBranchId());
                if(null == ptModelBranch || ptModelBranch.getParentId().compareTo(baseTrainParamDTO.getModelId()) != 0 ||
                        StringUtils.isBlank(ptModelBranch.getModelAddress())) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                PtModelInfo ptModelInfo = ptModelInfoMapper.selectById(ptModelBranch.getParentId());
                if(null == ptModelInfo || ptModelInfo.getModelResource().compareTo(baseTrainParamDTO.getModelResource()) != 0){
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                break;
            case PRESET:
                if(null == baseTrainParamDTO.getModelId() || StringUtils.isNotBlank(baseTrainParamDTO.getTeacherModelIds()) ||
                        StringUtils.isNotBlank(baseTrainParamDTO.getStudentModelIds())) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                PtModelInfo ptModelInfoPreset = ptModelInfoMapper.selectById(baseTrainParamDTO.getModelId());
                if(null == ptModelInfoPreset || StringUtils.isBlank(ptModelInfoPreset.getUrl()) ||
                        ptModelInfoPreset.getModelResource().compareTo(baseTrainParamDTO.getModelResource()) != 0) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                break;
            case ATLAS:
                if(StringUtils.isBlank(baseTrainParamDTO.getTeacherModelIds()) || null != baseTrainParamDTO.getModelId()){
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                Set<Long> ids = new HashSet<>();
                Set<Long> teacherModelList = new HashSet<>();
                Arrays.stream(baseTrainParamDTO.getTeacherModelIds().trim().split(SymbolConstant.COMMA))
                        .forEach(id -> teacherModelList.add(Long.parseLong(id)));
                ids.addAll(teacherModelList);

                Set<Long> studentModelList = new HashSet<>();
                if(StringUtils.isNotBlank(baseTrainParamDTO.getStudentModelIds())) {
                    Arrays.stream(baseTrainParamDTO.getStudentModelIds().trim().split(SymbolConstant.COMMA))
                            .forEach(id -> studentModelList.add(Long.parseLong(id)));
                    ids.addAll(studentModelList);
                }
                if(ids.isEmpty()) {
                    logErrorInfoOnModel(currentUser.getUsername());
                }
                LambdaQueryWrapper<PtModelInfo> query = new LambdaQueryWrapper<>();
                query.eq(PtModelInfo::getModelResource, baseTrainParamDTO.getModelResource())
                        .in(PtModelInfo::getId, ids).isNotNull(PtModelInfo::getUrl).ne(PtModelInfo::getUrl, SymbolConstant.BLANK);
                List<PtModelInfo> modelInfoList = ptModelInfoMapper.selectList(query);
                if(null == modelInfoList || modelInfoList.size() < ids.size()) {
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
    private void logErrorInfoOnModel(String username){
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
        //从会话中获取用户信息
        UserDTO user = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} modifies the task parameters and receives the parameters as {}.", user.getUsername(), ptTrainParamUpdateDTO);
        //参数校验
        checkUpdateTrainParam(ptTrainParamUpdateDTO, user);
        //修改任务参数
        PtTrainParam ptTrainParam = new PtTrainParam();
        //模型检测
        BaseTrainParamDTO baseTrainParamDTO = new BaseTrainParamDTO();
        BeanUtil.copyProperties(ptTrainParamUpdateDTO, baseTrainParamDTO);
        checkModel(user, baseTrainParamDTO);

        BeanUtils.copyProperties(ptTrainParamUpdateDTO, ptTrainParam);
        ptTrainParam.setUpdateUserId(user.getId());
        //获取镜像url
        String images = imageUtil.getImageUrl(ptTrainParamUpdateDTO, user);
        //添加镜像url
        ptTrainParam.setImageName(images);
        try {
            //任务参数未修改成功，抛出异常，并返回失败信息
            ptTrainParamMapper.updateById(ptTrainParam);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The user {} failed to modify the task parameters. The modification operation of pt_train_param table failed. The failure reason :{}", user.getUsername(), e.getMessage());
            throw new BusinessException("内部错误");
        }

        //返回修改任务参数id
        LogUtil.info(LogEnum.BIZ_TRAIN, "End of user {} saving task parameters, return modify task parameter ID ={}", user.getUsername(), ptTrainParam.getId());
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
        //从会话中获取用户信息
        UserDTO user = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} deletes the task parameter and receives the parameter {}", user.getUsername(), ptTrainParamDeleteDTO.getIds());
        Set<Long> idList = ptTrainParamDeleteDTO.getIds();
        //参数校验
        checkDeleteTrainParam(ptTrainParamDeleteDTO, user, idList);
        //删除任务参数
        int deleteCountResult = ptTrainParamMapper.deleteBatchIds(idList);
        //任务参数未删除成功,抛出异常，并返回失败信息
        if (deleteCountResult < idList.size()) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The user {} failed to delete the task parameter, and the pt_train_param table deletion operation failed according to the id array {}", user.getUsername(), ptTrainParamDeleteDTO.getIds());
            throw new BusinessException("内部错误");
        }
        //返回删除任务参数id数组
        LogUtil.info(LogEnum.BIZ_TRAIN, "End of user {} deleting task parameters,the array of delete task parameters IDS ={}", user.getUsername(), ptTrainParamDeleteDTO.getIds());
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
    private PtTrainAlgorithm checkCreateTrainParam(PtTrainParamCreateDTO ptTrainParamCreateDTO, UserDTO user) {
        //算法id校验
        PtTrainAlgorithm ptTrainAlgorithm = ptTrainAlgorithmMapper.selectAllById(ptTrainParamCreateDTO.getAlgorithmId());
        if (ptTrainAlgorithm == null) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "Algorithm ID ={} does not exist", ptTrainParamCreateDTO.getAlgorithmId());
            throw new BusinessException("算法不存在或已被删除");
        }
        //任务参数名称校验
        QueryWrapper<PtTrainParam> query = new QueryWrapper<>();
        query.eq("param_name", ptTrainParamCreateDTO.getParamName());
        Integer trainParamCountResult = ptTrainParamMapper.selectCount(query);
        if (trainParamCountResult > 0) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The task parameter name ({}) already exists", ptTrainParamCreateDTO.getParamName());
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
    private void checkUpdateTrainParam(PtTrainParamUpdateDTO ptTrainParamUpdateDTO, UserDTO user) {
        //任务参数id校验
        if (ptTrainParamMapper.selectById(ptTrainParamUpdateDTO.getId()) == null) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The task parameter ID ={} does not exist", ptTrainParamUpdateDTO.getId());
            throw new BusinessException(ResponseCode.SUCCESS, "任务参数id不存在");
        }
        //算法id校验
        PtTrainAlgorithm ptTrainAlgorithm = ptTrainAlgorithmMapper.selectById(ptTrainParamUpdateDTO.getAlgorithmId());
        if (ptTrainAlgorithm == null) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "Algorithm ID ={} does not exist", ptTrainParamUpdateDTO.getAlgorithmId());
            throw new BusinessException(ResponseCode.SUCCESS, "算法id不存在");
        }
        //权限校验
        QueryWrapper<PtTrainParam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", ptTrainParamUpdateDTO.getId());
        Integer countResult = ptTrainParamMapper.selectCount(queryWrapper);
        if (countResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The user {} failed to modify the task parameters and has no permission to modify the corresponding data in the pt_train_param table", user.getUsername());
            throw new BusinessException(ResponseCode.SUCCESS, "您修改的ID不存在或已被删除");
        }
        //任务参数名称校验
        QueryWrapper<PtTrainParam> query = new QueryWrapper<>();
        query.eq("param_name", ptTrainParamUpdateDTO.getParamName());
        PtTrainParam trainParam = ptTrainParamMapper.selectOne(query);
        if (trainParam != null && !ptTrainParamUpdateDTO.getId().equals(trainParam.getId())) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The task parameter name ({}) already exists", ptTrainParamUpdateDTO.getParamName());
            throw new BusinessException(ResponseCode.SUCCESS, "任务参数名称已存在");
        }
    }

    /**
     * 删除训练参数模板参数校验
     *
     * @param ptTrainParamDeleteDTO 训练参数模板参数
     * @param user                  用户
     * @param idList                训练参数id集合
     **/
    private void checkDeleteTrainParam(PtTrainParamDeleteDTO ptTrainParamDeleteDTO, UserDTO user, Set<Long> idList) {
        //id校验
        List<PtTrainParam> ptTrainParams = ptTrainParamMapper.selectBatchIds(idList);
        if (ptTrainParams.size() == 0 || ptTrainParams.size() != idList.size()) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} failed to delete the task parameters, request parameters IDS ={} cannot query the corresponding data in pt_train_param table, the parameters are illegal", user.getUsername(), ptTrainParamDeleteDTO.getIds());
            throw new BusinessException(ResponseCode.SUCCESS, "您删除的ID不存在或已被删除");
        }
        //权限校验
        QueryWrapper<PtTrainParam> query = new QueryWrapper<>();
        query.in("id", idList);
        Integer queryCountResult = ptTrainParamMapper.selectCount(query);
        if (queryCountResult < idList.size()) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} failed to delete the task parameters and has no permission to delete the corresponding data in the pt_train_param table", user.getUsername());
            throw new BusinessException(ResponseCode.SUCCESS, "您删除的ID不存在或已被删除");
        }
    }

}
