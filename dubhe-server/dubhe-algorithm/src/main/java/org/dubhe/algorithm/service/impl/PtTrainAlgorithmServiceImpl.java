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

package org.dubhe.algorithm.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.dubhe.algorithm.async.TrainAlgorithmUploadAsync;
import org.dubhe.algorithm.client.ImageClient;
import org.dubhe.algorithm.client.NoteBookClient;
import org.dubhe.algorithm.constant.AlgorithmConstant;
import org.dubhe.algorithm.constant.TrainAlgorithmConfig;
import org.dubhe.algorithm.dao.PtTrainAlgorithmMapper;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmCreateDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmDeleteDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmQueryDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmUpdateDTO;
import org.dubhe.algorithm.domain.entity.PtTrainAlgorithm;
import org.dubhe.algorithm.domain.vo.PtTrainAlgorithmQueryVO;
import org.dubhe.algorithm.service.PtTrainAlgorithmService;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.dto.*;
import org.dubhe.biz.base.enums.AlgorithmSourceEnum;
import org.dubhe.biz.base.enums.DatasetTypeEnum;
import org.dubhe.biz.base.enums.ImageTypeEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.ReflectionUtils;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.ModelOptAlgorithmQureyVO;
import org.dubhe.biz.base.vo.TrainAlgorithmQureyVO;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.file.enums.BizPathEnum;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.permission.annotation.DataPermissionMethod;
import org.dubhe.biz.permission.base.BaseService;
import org.dubhe.k8s.utils.K8sNameTool;
import org.dubhe.recycle.config.RecycleConfig;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.dto.RecycleDetailCreateDTO;
import org.dubhe.recycle.enums.RecycleModuleEnum;
import org.dubhe.recycle.enums.RecycleResourceEnum;
import org.dubhe.recycle.enums.RecycleTypeEnum;
import org.dubhe.recycle.service.RecycleService;
import org.dubhe.recycle.utils.RecycleTool;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @description 训练算法 服务实现类
 * @date 2020-04-27
 */
@Service
public class PtTrainAlgorithmServiceImpl implements PtTrainAlgorithmService {

    @Autowired
    private PtTrainAlgorithmMapper ptTrainAlgorithmMapper;

    @Autowired
    private ImageClient imageClient;

    @Autowired
    private K8sNameTool k8sNameTool;

    @Autowired
    private TrainAlgorithmConfig trainAlgorithmConstant;

    @Autowired
    private NoteBookClient noteBookClient;

    @Autowired
    private TrainAlgorithmUploadAsync algorithmUpdateAsync;

    @Autowired
    private RecycleService recycleService;

    @Autowired
    private RecycleConfig recycleConfig;

    @Autowired
    private UserContextService userContext;

    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;

    public final static List<String> FIELD_NAMES;

    static {
        FIELD_NAMES = ReflectionUtils.getFieldNames(PtTrainAlgorithmQueryVO.class);
    }

    /**
     * 查询数据分页
     *
     * @param ptTrainAlgorithmQueryDTO 条件
     * @return Map<String, Object>  返回查询数据
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Object> queryAll(PtTrainAlgorithmQueryDTO ptTrainAlgorithmQueryDTO) {
        //获取用户信息
        UserContext user = userContext.getCurUser();
        //当算法来源为空时，设置默认算法来源
        if (ptTrainAlgorithmQueryDTO.getAlgorithmSource() == null) {
            ptTrainAlgorithmQueryDTO.setAlgorithmSource(trainAlgorithmConstant.getAlgorithmSource());
        }
        QueryWrapper<PtTrainAlgorithm> wrapper = new QueryWrapper<>();
        wrapper.eq("algorithm_source", ptTrainAlgorithmQueryDTO.getAlgorithmSource());
        //判断算法来源
        if (AlgorithmSourceEnum.MINE.getStatus().equals(ptTrainAlgorithmQueryDTO.getAlgorithmSource())) {
            if (!BaseService.isAdmin(user)) {
                wrapper.eq("create_user_id", userContext.getCurUserId());
            }
        }
        //根据算法用途筛选
        if (ptTrainAlgorithmQueryDTO.getAlgorithmUsage() != null) {
            wrapper.like("algorithm_usage", ptTrainAlgorithmQueryDTO.getAlgorithmUsage());
        }
        //根据算法是否可推理筛选
        if (ptTrainAlgorithmQueryDTO.getInference() != null) {
            wrapper.eq("inference", ptTrainAlgorithmQueryDTO.getInference());
        }
        if (!StringUtils.isEmpty(ptTrainAlgorithmQueryDTO.getAlgorithmName())) {
            wrapper.and(qw -> qw.eq("id", ptTrainAlgorithmQueryDTO.getAlgorithmName()).or().like("algorithm_name",
                    ptTrainAlgorithmQueryDTO.getAlgorithmName()));
        }

        Page page = ptTrainAlgorithmQueryDTO.toPage();
        IPage<PtTrainAlgorithm> ptTrainAlgorithms;
        try {
            if (ptTrainAlgorithmQueryDTO.getSort() != null && FIELD_NAMES.contains(ptTrainAlgorithmQueryDTO.getSort())) {
                if (AlgorithmConstant.SORT_ASC.equalsIgnoreCase(ptTrainAlgorithmQueryDTO.getOrder())) {
                    wrapper.orderByAsc(StringUtils.humpToLine(ptTrainAlgorithmQueryDTO.getSort()));
                } else {
                    wrapper.orderByDesc(StringUtils.humpToLine(ptTrainAlgorithmQueryDTO.getSort()));
                }
            } else {
                wrapper.orderByDesc(AlgorithmConstant.ID);
            }
            ptTrainAlgorithms = ptTrainAlgorithmMapper.selectPage(page, wrapper);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_ALGORITHM, "Query training algorithm list display exceptions :{}, request information :{}", e,
                    ptTrainAlgorithmQueryDTO);
            throw new BusinessException("查询训练算法列表展示异常");
        }
        List<PtTrainAlgorithmQueryVO> ptTrainAlgorithmQueryResult = ptTrainAlgorithms.getRecords().stream().map(x -> {
            PtTrainAlgorithmQueryVO ptTrainAlgorithmQueryVO = new PtTrainAlgorithmQueryVO();
            BeanUtils.copyProperties(x, ptTrainAlgorithmQueryVO);
            //获取镜像名称与版本
            getImageNameAndImageTag(x, ptTrainAlgorithmQueryVO);
            return ptTrainAlgorithmQueryVO;
        }).collect(Collectors.toList());
        return PageUtil.toPage(page, ptTrainAlgorithmQueryResult);
    }

    /**
     * 新增算法
     *
     * @param ptTrainAlgorithmCreateDTO 新增算法条件
     * @return idList  返回新增算法
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> create(PtTrainAlgorithmCreateDTO ptTrainAlgorithmCreateDTO) {
        //获取用户信息
        UserContext user = userContext.getCurUser();
        //获取镜像url
        BaseImageDTO baseImageDTO = new BaseImageDTO();
        BeanUtils.copyProperties(ptTrainAlgorithmCreateDTO, baseImageDTO);
        if (StringUtils.isNotBlank(ptTrainAlgorithmCreateDTO.getImageName()) && StringUtils.isNotBlank(ptTrainAlgorithmCreateDTO.getImageTag())) {
            ptTrainAlgorithmCreateDTO.setImageName(getImageUrl(baseImageDTO, user));
        }
        //创建算法校验DTO并设置默认值
        setAlgorithmDtoDefault(ptTrainAlgorithmCreateDTO);
        //算法路径
        String path = fileStoreApi.getBucket() + ptTrainAlgorithmCreateDTO.getCodeDir();
        if (!fileStoreApi.fileOrDirIsExist(fileStoreApi.getRootDir() + path)) {
            LogUtil.error(LogEnum.BIZ_ALGORITHM, "The user {} upload path {} does not exist", user.getUsername(), path);
            throw new BusinessException("算法文件或路径不存在");
        }
        //保存算法
        PtTrainAlgorithm ptTrainAlgorithm = new PtTrainAlgorithm();
        BeanUtils.copyProperties(ptTrainAlgorithmCreateDTO, ptTrainAlgorithm);
        //创建我的算法
        if (BaseService.isAdmin(user) && AlgorithmSourceEnum.PRE.getStatus().equals(ptTrainAlgorithmCreateDTO.getAlgorithmSource())) {
            ptTrainAlgorithm.setAlgorithmSource(AlgorithmSourceEnum.PRE.getStatus());
            ptTrainAlgorithm.setOriginUserId(0L);
        } else {
            ptTrainAlgorithm.setAlgorithmSource(AlgorithmSourceEnum.MINE.getStatus());
        }
        ptTrainAlgorithm.setCreateUserId(user.getId());

        //算法名称校验
        QueryWrapper<PtTrainAlgorithm> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("algorithm_name", ptTrainAlgorithmCreateDTO.getAlgorithmName()).and(wrapper -> wrapper.eq("create_user_id", user.getId()).or().eq("origin_user_id", 0L));
        Integer countResult = ptTrainAlgorithmMapper.selectCount(queryWrapper);
        //如果是通过【保存至算法】接口创建算法，名称重复可用随机数生成新算法名，待后续客户自主修改
        if (countResult > 0) {
            if (ptTrainAlgorithmCreateDTO.getNoteBookId() != null) {
                String randomStr = RandomUtil.randomNumbers(MagicNumConstant.FOUR);
                ptTrainAlgorithm.setAlgorithmName(ptTrainAlgorithmCreateDTO.getAlgorithmName() + randomStr);
            } else {
                LogUtil.error(LogEnum.BIZ_ALGORITHM, "The algorithm name ({}) already exists", ptTrainAlgorithmCreateDTO.getAlgorithmName());
                throw new BusinessException("算法名称已存在，请重新输入");
            }
        }
        //校验path是否带有压缩文件，如有，则解压至算法文件夹下并删除压缩文件
        if (path.toLowerCase().endsWith(AlgorithmConstant.COMPRESS_ZIP)) {
            unZip(user, path, ptTrainAlgorithm, ptTrainAlgorithmCreateDTO);
        }

        //校验上传算法是否支持推理，如有，则拷贝至算法文件夹下
        if (ptTrainAlgorithmCreateDTO.getInference()) {
            //可推理的算法文件拷贝
            copyFile(user, path, ptTrainAlgorithm, ptTrainAlgorithmCreateDTO);
        }

        try {
            //算法未保存成功，抛出异常，并返回失败信息
            ptTrainAlgorithmMapper.insert(ptTrainAlgorithm);
            //设置子线程共享
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            RequestContextHolder.setRequestAttributes(servletRequestAttributes, true);
            //上传算法异步处理
            algorithmUpdateAsync.createTrainAlgorithm(userContext.getCurUser(), ptTrainAlgorithm, ptTrainAlgorithmCreateDTO);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_ALGORITHM, "The user {} saving algorithm was not successful. Failure reason :{}", user.getUsername(), e.getMessage());
            throw new BusinessException("算法未保存成功");
        }
        return Collections.singletonList(ptTrainAlgorithm.getId());
    }

    /**
     * 修改算法
     *
     * @param ptTrainAlgorithmUpdateDTO 修改算法条件
     * @return PtTrainAlgorithmUpdateVO  返回修改算法
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> update(PtTrainAlgorithmUpdateDTO ptTrainAlgorithmUpdateDTO) {
        //获取用户信息
        UserContext user = userContext.getCurUser();
        //权限校验
        PtTrainAlgorithm ptTrainAlgorithm = ptTrainAlgorithmMapper.selectById(ptTrainAlgorithmUpdateDTO.getId());
        if (null == ptTrainAlgorithm) {
            LogUtil.error(LogEnum.BIZ_ALGORITHM, "It is illegal for the user {} to modify the algorithm with id {}", user.getUsername(), ptTrainAlgorithmUpdateDTO.getId());
            throw new BusinessException("您修改的算法不存在或已被删除");
        }
        PtTrainAlgorithm updatePtAlgorithm = new PtTrainAlgorithm();
        updatePtAlgorithm.setId(ptTrainAlgorithm.getId()).setUpdateUserId(user.getId());
        //判断是否修改算法名称
        if (StringUtils.isNotBlank(ptTrainAlgorithmUpdateDTO.getAlgorithmName())) {
            //算法名称校验
            QueryWrapper<PtTrainAlgorithm> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("algorithm_name", ptTrainAlgorithmUpdateDTO.getAlgorithmName())
                    .ne("id", ptTrainAlgorithmUpdateDTO.getId());
            Integer countResult = ptTrainAlgorithmMapper.selectCount(queryWrapper);
            if (countResult > 0) {
                LogUtil.error(LogEnum.BIZ_ALGORITHM, "The algorithm name ({}) already exists", ptTrainAlgorithmUpdateDTO.getAlgorithmName());
                throw new BusinessException("算法名称已存在，请重新输入");
            }
            updatePtAlgorithm.setAlgorithmName(ptTrainAlgorithmUpdateDTO.getAlgorithmName());
        }
        //判断是否修改算法描述
        if (ptTrainAlgorithmUpdateDTO.getDescription() != null) {
            updatePtAlgorithm.setDescription(ptTrainAlgorithmUpdateDTO.getDescription());
        }
        //判断是否修改算法用途
        if (ptTrainAlgorithmUpdateDTO.getAlgorithmUsage() != null) {
            updatePtAlgorithm.setAlgorithmUsage(ptTrainAlgorithmUpdateDTO.getAlgorithmUsage());
        }
        //判断是否修改训练输出
        if (ptTrainAlgorithmUpdateDTO.getIsTrainOut() != null) {
            updatePtAlgorithm.setIsTrainOut(ptTrainAlgorithmUpdateDTO.getIsTrainOut());
        }
        //判断是否修改可视化日志
        if (ptTrainAlgorithmUpdateDTO.getIsVisualizedLog() != null) {
            updatePtAlgorithm.setIsVisualizedLog(ptTrainAlgorithmUpdateDTO.getIsVisualizedLog());
        }
        try {
            //算法未修改成功，抛出异常，并返回失败信息
            ptTrainAlgorithmMapper.updateById(updatePtAlgorithm);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_ALGORITHM, "User {} failed to modify the algorithm. Pt_train_algorithm table modification operation failed. Failure reason :{}", user.getUsername(), e.getMessage());
            throw new BusinessException("修改失败");
        }
        return Collections.singletonList(ptTrainAlgorithm.getId());
    }

    /**
     * 可推理的算法文件拷贝
     *
     * @param user             用户
     * @param path             文件路径
     * @param ptTrainAlgorithm 算法参数
     */
    private void copyFile(UserContext user, String path, PtTrainAlgorithm ptTrainAlgorithm, PtTrainAlgorithmCreateDTO ptTrainAlgorithmCreateDTO) {
        //目标路径
        String targetPath = null;
        if (BaseService.isAdmin(user) && AlgorithmSourceEnum.PRE.getStatus().equals(ptTrainAlgorithmCreateDTO.getAlgorithmSource())) {
            targetPath = k8sNameTool.getPrePath(BizPathEnum.ALGORITHM, user.getId());
        } else {
            targetPath = k8sNameTool.getPath(BizPathEnum.ALGORITHM, user.getId());
        }
        boolean copyFile;
        if (fileStoreApi.isDirectory(fileStoreApi.getRootDir() + path)) {
            copyFile = fileStoreApi.copyPath(fileStoreApi.getRootDir() + path, fileStoreApi.getRootDir() + fileStoreApi.getBucket() + targetPath);
        } else {
            copyFile = fileStoreApi.copyFile(fileStoreApi.getRootDir() + path, fileStoreApi.getRootDir() + fileStoreApi.getBucket() + targetPath);
        }
        if (!copyFile) {
            LogUtil.error(LogEnum.BIZ_ALGORITHM, "User {} failed to inference copyFile", user.getUsername());
            throw new BusinessException("文件拷贝失败");
        }
        //算法路径
        ptTrainAlgorithm.setCodeDir(targetPath);
        //算法文件可推理
        ptTrainAlgorithm.setInference(true);
    }

    /**
     * 解压缩zip压缩包
     *
     * @param user             用户
     * @param path             文件路径
     * @param ptTrainAlgorithm 算法参数
     */
    private void unZip(UserContext user, String path, PtTrainAlgorithm ptTrainAlgorithm, PtTrainAlgorithmCreateDTO ptTrainAlgorithmCreateDTO) {
        //目标路径
        String targetPath = null;
        if (BaseService.isAdmin(user) && AlgorithmSourceEnum.PRE.getStatus().equals(ptTrainAlgorithmCreateDTO.getAlgorithmSource())) {
            targetPath = k8sNameTool.getPrePath(BizPathEnum.ALGORITHM, user.getId());
        } else {
            targetPath = k8sNameTool.getPath(BizPathEnum.ALGORITHM, user.getId());
        }
        boolean unzip = fileStoreApi.unzip(path, fileStoreApi.getBucket() + targetPath);
        if (!unzip) {
            LogUtil.error(LogEnum.BIZ_ALGORITHM, "User {} failed to unzip", user.getUsername());
            throw new BusinessException("内部错误");
        }
        //算法路径
        ptTrainAlgorithm.setCodeDir(targetPath);
    }

    /**
     * 删除算法
     *
     * @param ptTrainAlgorithmDeleteDTO 删除算法条件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public void deleteAll(PtTrainAlgorithmDeleteDTO ptTrainAlgorithmDeleteDTO) {
        //获取用户信息
        UserContext user = userContext.getCurUser();
        Set<Long> idList = ptTrainAlgorithmDeleteDTO.getIds();
        //权限校验
        QueryWrapper<PtTrainAlgorithm> query = new QueryWrapper<>();
        //非管理员不可删除预置算法
        if (!BaseService.isAdmin(user)) {
            query.eq("algorithm_source", 1);
        }
        query.in("id", idList);
        List<PtTrainAlgorithm> algorithmList = ptTrainAlgorithmMapper.selectList(query);
        if (algorithmList.size() < idList.size()) {
            LogUtil.error(LogEnum.BIZ_ALGORITHM, "User {} delete algorithm failed, no permission to delete the corresponding data in the algorithm table", user.getUsername());
            throw new BusinessException("您删除的ID不存在或已被删除");
        }
        int deleteCountResult = ptTrainAlgorithmMapper.deleteBatchIds(idList);
        if (deleteCountResult < idList.size()) {
            LogUtil.error(LogEnum.BIZ_ALGORITHM, "The user {} deletion algorithm failed, and the algorithm table deletion operation based on the ID array {} failed", user.getUsername(), ptTrainAlgorithmDeleteDTO.getIds());
            throw new BusinessException("删除算法未成功");
        }
        //同步更新noteBook表中algorithmId=0
        NoteBookAlgorithmQueryDTO noteBookAlgorithmQueryDTO = new NoteBookAlgorithmQueryDTO();
        List<Long> ids = new ArrayList<>();
        idList.stream().forEach(id -> {
            ids.add(id);
        });
        noteBookAlgorithmQueryDTO.setAlgorithmIdList(ids);
        DataResponseBody<List<Long>> dataResponseBody = noteBookClient.getNoteBookIdByAlgorithm(noteBookAlgorithmQueryDTO);
        if (dataResponseBody.succeed()) {
            List<Long> noteBookIdList = dataResponseBody.getData();
            if (!CollectionUtils.isEmpty(noteBookIdList)) {
                //根据算法
                NoteBookAlgorithmUpdateDTO noteBookAlgorithmUpdateDTO = new NoteBookAlgorithmUpdateDTO();
                noteBookAlgorithmUpdateDTO.setNotebookIdList(noteBookIdList);
                noteBookAlgorithmUpdateDTO.setAlgorithmId(0L);
                noteBookClient.updateNoteBookAlgorithm(noteBookAlgorithmUpdateDTO);
            }
        }
        //定时任务删除相应的算法文件
        for (PtTrainAlgorithm algorithm : algorithmList) {
            RecycleCreateDTO recycleCreateDTO = new RecycleCreateDTO();
            recycleCreateDTO.setRecycleModule(RecycleModuleEnum.BIZ_ALGORITHM.getValue())
                    .setRecycleDelayDate(recycleConfig.getAlgorithmValid())
                    .setRecycleNote(RecycleTool.generateRecycleNote("删除算法文件", algorithm.getAlgorithmName(), algorithm.getId()))
                    .setRemark(algorithm.getId().toString())
                    .setRestoreCustom(RecycleResourceEnum.ALGORITHM_RECYCLE_FILE.getClassName());
            RecycleDetailCreateDTO detail = new RecycleDetailCreateDTO();
            detail.setRecycleType(RecycleTypeEnum.FILE.getCode())
                    .setRecycleCondition(fileStoreApi.formatPath(fileStoreApi.getRootDir() + fileStoreApi.getBucket() + algorithm.getCodeDir()));
            recycleCreateDTO.addRecycleDetailCreateDTO(detail);
            recycleService.createRecycleTask(recycleCreateDTO);
        }
    }

    /**
     * 查询算法个数
     *
     * @return count  返回个数
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Object> getAlgorithmCount() {
        QueryWrapper<PtTrainAlgorithm> wrapper = new QueryWrapper();
        wrapper.eq("algorithm_source", AlgorithmSourceEnum.MINE.getStatus());
        Integer countResult = ptTrainAlgorithmMapper.selectCount(wrapper);
        return new HashedMap() {{
            put("count", countResult);
        }};
    }

    /**
     * 根据Id查询所有数据(包含已被软删除的数据)
     * @param trainAlgorithmSelectAllByIdDTO 算法id
     * @return TrainAlgorithmQureyVO返回查询数据(包含已被软删除的数据)
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public TrainAlgorithmQureyVO selectAllById(TrainAlgorithmSelectAllByIdDTO trainAlgorithmSelectAllByIdDTO) {
        PtTrainAlgorithm ptTrainAlgorithm = ptTrainAlgorithmMapper.selectAllById(trainAlgorithmSelectAllByIdDTO.getId());
        TrainAlgorithmQureyVO trainAlgorithmQureyVO = new TrainAlgorithmQureyVO();
        BeanUtils.copyProperties(ptTrainAlgorithm, trainAlgorithmQureyVO);
        return trainAlgorithmQureyVO;
    }

    /**
     * 根据Id查询
     * @param  trainAlgorithmSelectByIdDTO 算法id
     * @return TrainAlgorithmQureyVO 返回查询数据
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public TrainAlgorithmQureyVO selectById(TrainAlgorithmSelectByIdDTO trainAlgorithmSelectByIdDTO) {
        PtTrainAlgorithm ptTrainAlgorithm = ptTrainAlgorithmMapper.selectById(trainAlgorithmSelectByIdDTO.getId());
        TrainAlgorithmQureyVO trainAlgorithmQureyVO = new TrainAlgorithmQureyVO();
        BeanUtils.copyProperties(ptTrainAlgorithm, trainAlgorithmQureyVO);
        return trainAlgorithmQureyVO;
    }

    /**
     * 根据Id批量查询
     * @param trainAlgorithmSelectAllBatchIdDTO 算法ids
     * @return List<TrainAlgorithmQureyVO> 返回查询数据
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public List<TrainAlgorithmQureyVO> selectAllBatchIds(TrainAlgorithmSelectAllBatchIdDTO trainAlgorithmSelectAllBatchIdDTO) {
        List<PtTrainAlgorithm> ptTrainAlgorithms = ptTrainAlgorithmMapper.selectAllBatchIds(trainAlgorithmSelectAllBatchIdDTO.getIds());
        List<TrainAlgorithmQureyVO> trainAlgorithmQureyVOS = ptTrainAlgorithms.stream().map(x -> {
            TrainAlgorithmQureyVO trainAlgorithmQureyVO = new TrainAlgorithmQureyVO();
            BeanUtils.copyProperties(x, trainAlgorithmQureyVO);
            return trainAlgorithmQureyVO;
        }).collect(Collectors.toList());
        return trainAlgorithmQureyVOS;
    }

    /**
     * 获取镜像名称与版本
     *
     * @param trainAlgorithm          镜像URL
     * @param ptTrainAlgorithmQueryVO 镜像名称与版本
     */
    private void getImageNameAndImageTag(PtTrainAlgorithm trainAlgorithm, PtTrainAlgorithmQueryVO ptTrainAlgorithmQueryVO) {
        if (StringUtils.isNotBlank(trainAlgorithm.getImageName())) {
            String imageNameSuffix = trainAlgorithm.getImageName().substring(trainAlgorithm.getImageName().lastIndexOf(StrUtil.SLASH) + MagicNumConstant.ONE);
            String[] imageNameSuffixArray = imageNameSuffix.split(StrUtil.COLON);
            ptTrainAlgorithmQueryVO.setImageName(imageNameSuffixArray[0]);
            ptTrainAlgorithmQueryVO.setImageTag(imageNameSuffixArray[1]);
        }
    }


    /**
     * 创建算法DTO校验并设置默认值
     *
     * @param dto 校验DTO
     **/
    private void setAlgorithmDtoDefault(PtTrainAlgorithmCreateDTO dto) {

        //设置fork默认值（fork:创建算法来源）
        if (dto.getFork() == null) {
            dto.setFork(trainAlgorithmConstant.getFork());
        }
        //设置inference默认值(inference:上传算法是否支持推理)
        if (dto.getInference() == null) {
            dto.setInference(trainAlgorithmConstant.getInference());
        }
        //设置是否输出训练信息
        if (dto.getIsTrainOut() == null) {
            dto.setIsTrainOut(trainAlgorithmConstant.getIsTrainOut());
        }
        //设置是否输出训练结果
        if (dto.getIsTrainModelOut() == null) {
            dto.setIsTrainModelOut(trainAlgorithmConstant.getIsTrainModelOut());
        }
        //设置是否输出可视化日志
        if (dto.getIsVisualizedLog() == null) {
            dto.setIsVisualizedLog(trainAlgorithmConstant.getIsVisualizedLog());
        }
    }

    /**
     * 获取镜像url
     *
     * @param baseImageDto 镜像参数
     * @return BaseImageDTO  镜像url
     **/
    private String getImageUrl(BaseImageDTO baseImageDto, UserContext user) {

        PtImageQueryUrlDTO ptImageQueryUrlDTO = new PtImageQueryUrlDTO();
        ptImageQueryUrlDTO.setImageTag(baseImageDto.getImageTag());
        ptImageQueryUrlDTO.setImageName(baseImageDto.getImageName());
        ptImageQueryUrlDTO.setProjectType(ImageTypeEnum.TRAIN.getType());
        DataResponseBody<String> dataResponseBody = imageClient.getImageUrl(ptImageQueryUrlDTO);
        if (!dataResponseBody.succeed()) {
            LogUtil.error(LogEnum.BIZ_TRAIN, " User {} gets image ,the imageName is {}, the imageTag is {}, and the result of dubhe-image service call failed", user.getUsername(), baseImageDto.getImageName(), baseImageDto.getImageTag());
            throw new BusinessException("镜像服务调用失败");
        }
        String ptImage = dataResponseBody.getData();
        // 镜像路径
        if (StringUtils.isBlank(ptImage)) {
            LogUtil.error(LogEnum.BIZ_ALGORITHM, "User {} gets image ,the imageName is {}, the imageTag is {}, and the result of query image table (PT_image) is empty", user.getUsername(), baseImageDto.getImageName(), baseImageDto.getImageTag());
            throw new BusinessException("镜像不存在");
        }
        return ptImage;
    }

    /**
     *
     * @param modelOptAlgorithmCreateDTO 模型优化上传算法入参
     * @return PtTrainAlgorithm 新增算法信息
     */
    @Override
    public ModelOptAlgorithmQureyVO modelOptimizationUploadAlgorithm(ModelOptAlgorithmCreateDTO modelOptAlgorithmCreateDTO) {
        PtTrainAlgorithmCreateDTO ptTrainAlgorithmCreateDTO = new PtTrainAlgorithmCreateDTO();
        ptTrainAlgorithmCreateDTO.setAlgorithmName(modelOptAlgorithmCreateDTO.getName()).setCodeDir(modelOptAlgorithmCreateDTO.getPath()).setAlgorithmUsage("模型优化").setIsTrainModelOut(false).setIsTrainOut(false).setIsVisualizedLog(false);
        List<Long> ids = create(ptTrainAlgorithmCreateDTO);
        PtTrainAlgorithm ptTrainAlgorithm = ptTrainAlgorithmMapper.selectById(ids.get(NumberConstant.NUMBER_0));
        ModelOptAlgorithmQureyVO modelOptAlgorithmQureyVO = new ModelOptAlgorithmQureyVO();
        BeanUtils.copyProperties(ptTrainAlgorithm, modelOptAlgorithmQureyVO);
        return modelOptAlgorithmQureyVO;
    }

    /**
     * 算法删除文件还原
     * @param dto 还原实体
     */
    @Override
    public void algorithmRecycleFileRollback(RecycleCreateDTO dto) {
        //获取用户信息
        UserContext user = userContext.getCurUser();
        if (dto == null) {
            LogUtil.error(LogEnum.BIZ_ALGORITHM, "User {} restore algorithm failed to delete the file because RecycleCreateDTO is null", user.getUsername());
            throw new BusinessException("非法入参");
        }
        Long algorithmId = Long.valueOf(dto.getRemark());
        PtTrainAlgorithm ptTrainAlgorithm = ptTrainAlgorithmMapper.selectAllById(algorithmId);
        QueryWrapper<PtTrainAlgorithm> wrapper = new QueryWrapper<>();
        wrapper.eq("algorithm_name", ptTrainAlgorithm.getAlgorithmName());
        if (ptTrainAlgorithmMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("算法已存在");
        }
        try {
            ptTrainAlgorithmMapper.updateStatusById(algorithmId, false);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_ALGORITHM, "User {} restore algorithm failed to delete the file because:{}", user.getUsername(), e);
            throw new BusinessException("还原失败");
        }
    }

    /**
     * 查询可推理算法
     * @return List<PtTrainAlgorithmQueryVO> 返回可推理算法集合
     */
    @Override
    public List<PtTrainAlgorithmQueryVO> getInferenceAlgorithm() {
        //获取用户信息
        UserContext user = userContext.getCurUser();
        QueryWrapper<PtTrainAlgorithm> wrapper = new QueryWrapper<>();
        wrapper.eq("inference", true).orderByDesc("id");
        List<PtTrainAlgorithm> ptTrainAlgorithms = ptTrainAlgorithmMapper.selectList(wrapper);
        List<PtTrainAlgorithmQueryVO> ptTrainAlgorithmQueryResult = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ptTrainAlgorithms)) {
            ptTrainAlgorithmQueryResult = ptTrainAlgorithms.stream().map(x -> {
                PtTrainAlgorithmQueryVO ptTrainAlgorithmQueryVO = new PtTrainAlgorithmQueryVO();
                BeanUtils.copyProperties(x, ptTrainAlgorithmQueryVO);
                //获取镜像名称与版本
                getImageNameAndImageTag(x, ptTrainAlgorithmQueryVO);
                return ptTrainAlgorithmQueryVO;
            }).collect(Collectors.toList());
        }

        //非管理员用户查询可推理预置算法
        if (!BaseService.isAdmin(user)) {
            List<PtTrainAlgorithm> preAlgorithms = ptTrainAlgorithmMapper.selectPreAlgorithm();
            List<PtTrainAlgorithmQueryVO> preAlgorithmQueryResult = preAlgorithms.stream().map(x -> {
                PtTrainAlgorithmQueryVO ptTrainAlgorithmQueryVO = new PtTrainAlgorithmQueryVO();
                BeanUtils.copyProperties(x, ptTrainAlgorithmQueryVO);
                //获取镜像名称与版本
                getImageNameAndImageTag(x, ptTrainAlgorithmQueryVO);
                return ptTrainAlgorithmQueryVO;
            }).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(preAlgorithmQueryResult)) {
                ptTrainAlgorithmQueryResult.addAll(preAlgorithmQueryResult);
            }
        }
        return ptTrainAlgorithmQueryResult;
    }

}
