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

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.dubhe.annotation.DataPermissionMethod;
import org.dubhe.async.TrainAlgorithmUploadAsync;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.base.ResponseCode;
import org.dubhe.config.NfsConfig;
import org.dubhe.config.RecycleConfig;
import org.dubhe.constant.AlgorithmSourceEnum;
import org.dubhe.constant.TrainAlgorithmConstant;
import org.dubhe.dao.NoteBookMapper;
import org.dubhe.dao.PtImageMapper;
import org.dubhe.dao.PtTrainAlgorithmMapper;
import org.dubhe.data.constant.Constant;
import org.dubhe.domain.dto.*;
import org.dubhe.domain.entity.NoteBook;
import org.dubhe.domain.entity.PtImage;
import org.dubhe.domain.entity.PtTrainAlgorithm;
import org.dubhe.domain.vo.PtTrainAlgorithmQueryVO;
import org.dubhe.enums.*;
import org.dubhe.exception.BusinessException;
import org.dubhe.service.NoteBookService;
import org.dubhe.service.PtTrainAlgorithmService;
import org.dubhe.service.RecycleTaskService;
import org.dubhe.utils.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private PtImageMapper ptImageMapper;

    @Autowired
    private NfsUtil nfsUtil;

    @Autowired
    private LocalFileUtil localFileUtil;

    @Autowired
    private K8sNameTool k8sNameTool;

    @Autowired
    private NfsConfig nfsConfig;

    @Autowired
    private TrainAlgorithmConstant trainAlgorithmConstant;

    @Autowired
    private NoteBookService noteBookService;

    @Autowired
    private NoteBookMapper noteBookMapper;

    @Autowired
    private TrainAlgorithmUploadAsync algorithmUpdateAsync;

    @Autowired
    private RecycleTaskService recycleTaskService;

    @Autowired
    private RecycleConfig recycleConfig;

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
        //从会话中获取用户信息
        UserDTO user = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "The display of user {} query training algorithm list begins, and the parameters received are {}.", user.getId(), ptTrainAlgorithmQueryDTO);
        //当算法来源为空时，设置默认算法来源
        if (ptTrainAlgorithmQueryDTO.getAlgorithmSource() == null) {
            ptTrainAlgorithmQueryDTO.setAlgorithmSource(trainAlgorithmConstant.getAlgorithmSource());
        }
        QueryWrapper<PtTrainAlgorithm> wrapper = new QueryWrapper<>();
        wrapper.eq("algorithm_source", ptTrainAlgorithmQueryDTO.getAlgorithmSource());
        //判断算法来源
        if (AlgorithmSourceEnum.MINE.getStatus().equals(ptTrainAlgorithmQueryDTO.getAlgorithmSource())) {
            wrapper.eq("create_user_id", user.getId());
        }
        //根据算法用途筛选
        if (ptTrainAlgorithmQueryDTO.getAlgorithmUsage() != null) {
            wrapper.like("algorithm_usage", ptTrainAlgorithmQueryDTO.getAlgorithmUsage());
        }
        if (!StringUtils.isEmpty(ptTrainAlgorithmQueryDTO.getAlgorithmName())) {
            wrapper.and(qw -> qw.eq("id", ptTrainAlgorithmQueryDTO.getAlgorithmName()).or().like("algorithm_name",
                    ptTrainAlgorithmQueryDTO.getAlgorithmName()));
        }

        Page page = ptTrainAlgorithmQueryDTO.toPage();
        IPage<PtTrainAlgorithm> ptTrainAlgorithms;
        try {
            if (ptTrainAlgorithmQueryDTO.getSort() != null && FIELD_NAMES.contains(ptTrainAlgorithmQueryDTO.getSort())) {
                if (Constant.SORT_ASC.equalsIgnoreCase(ptTrainAlgorithmQueryDTO.getOrder())) {
                    wrapper.orderByAsc(StringUtils.humpToLine(ptTrainAlgorithmQueryDTO.getSort()));
                } else {
                    wrapper.orderByDesc(StringUtils.humpToLine(ptTrainAlgorithmQueryDTO.getSort()));
                }
            } else {
                wrapper.orderByDesc(Constant.ID);
            }
            ptTrainAlgorithms = ptTrainAlgorithmMapper.selectPage(page, wrapper);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "Query training algorithm list display exceptions :{}, request information :{}", e,
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
        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} query training algorithm list display ends, the result is {}", user.getUsername(), ptTrainAlgorithmQueryResult);
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
    public Long create(PtTrainAlgorithmCreateDTO ptTrainAlgorithmCreateDTO) {
        //从会话中获取用户信息
        UserDTO user = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "Save the new algorithm and receive the parameter {}", ptTrainAlgorithmCreateDTO);
        //获取镜像url
        if (StringUtils.isNotBlank(ptTrainAlgorithmCreateDTO.getImageName()) && StringUtils.isNotBlank(ptTrainAlgorithmCreateDTO.getImageTag())) {
            ptTrainAlgorithmCreateDTO.setImageName(getImageUrl(ptTrainAlgorithmCreateDTO, user));
        }
        //创建算法校验DTO并设置默认值
        setAlgorithmDtoDefault(ptTrainAlgorithmCreateDTO);
        //算法路径
        String path = nfsConfig.getBucket() + ptTrainAlgorithmCreateDTO.getCodeDir();
        if (nfsUtil.fileOrDirIsEmpty(path)) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The user {} upload path {} does not exist", user.getUsername(), path);
            throw new BusinessException("算法文件或路径不存在");
        }
        //保存算法
        PtTrainAlgorithm ptTrainAlgorithm = new PtTrainAlgorithm();
        BeanUtils.copyProperties(ptTrainAlgorithmCreateDTO, ptTrainAlgorithm);
        ptTrainAlgorithm.setAlgorithmSource(AlgorithmSourceEnum.MINE.getStatus())
                .setCreateUserId(user.getId());

        //算法名称校验
        QueryWrapper<PtTrainAlgorithm> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("algorithm_name", ptTrainAlgorithmCreateDTO.getAlgorithmName());
        Integer countResult = ptTrainAlgorithmMapper.selectCount(queryWrapper);
        //如果是通过【保存至算法】接口创建算法，名称重复可用随机数生成新算法名，待后续客户自主修改
        if (countResult > 0) {
            if (ptTrainAlgorithmCreateDTO.getNoteBookId() != null) {
                String randomStr = RandomUtil.randomNumbers(MagicNumConstant.FOUR);
                ptTrainAlgorithm.setAlgorithmName(ptTrainAlgorithmCreateDTO.getAlgorithmName() + randomStr);
            } else {
                LogUtil.error(LogEnum.BIZ_TRAIN, "The algorithm name ({}) already exists", ptTrainAlgorithmCreateDTO.getAlgorithmName());
                throw new BusinessException("算法名称已存在，请重新输入");
            }
        }
        //校验path是否带有压缩文件，如有，则解压至当前文件夹并删除压缩文件
        if (path.toLowerCase().endsWith(Constant.COMPRESS_ZIP)) {
            unZip(user, path, ptTrainAlgorithm);
        }
        try {
            //算法未保存成功，抛出异常，并返回失败信息
            ptTrainAlgorithmMapper.insert(ptTrainAlgorithm);

            //上传算法异步处理
            algorithmUpdateAsync.createTrainAlgorithm(user, ptTrainAlgorithm, ptTrainAlgorithmCreateDTO);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The user {} saving algorithm was not successful. Failure reason :{}", user.getUsername(), e.getMessage());
            throw new BusinessException("算法未保存成功");
        }

        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} saves the new algorithm and returns the new algorithm id as {}.", user.getUsername(), ptTrainAlgorithm.getId());
        return ptTrainAlgorithm.getId();
    }

    /**
     * 修改算法
     *
     * @param ptTrainAlgorithmUpdateDTO 修改算法条件
     * @return PtTrainAlgorithmUpdateVO  返回修改算法
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long update(PtTrainAlgorithmUpdateDTO ptTrainAlgorithmUpdateDTO) {
        UserDTO currentUser = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} modifies the algorithm and receives {} as the parameter", currentUser.getUsername(), ptTrainAlgorithmUpdateDTO);
        //权限校验
        PtTrainAlgorithm ptTrainAlgorithm = ptTrainAlgorithmMapper.selectById(ptTrainAlgorithmUpdateDTO.getId());
        if (null == ptTrainAlgorithm || ptTrainAlgorithm.getCreateUserId().compareTo(currentUser.getId()) != 0) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "It is illegal for the user {} to modify the algorithm with id {}", currentUser.getUsername(), ptTrainAlgorithmUpdateDTO.getId());
            throw new BusinessException(ResponseCode.SUCCESS, "您修改的算法不存在或已被删除");
        }
        PtTrainAlgorithm updatePtAlgorithm = new PtTrainAlgorithm();
        updatePtAlgorithm.setId(ptTrainAlgorithm.getId()).setUpdateUserId(currentUser.getId());
        //判断是否修改算法名称
        if (StringUtils.isNotBlank(ptTrainAlgorithmUpdateDTO.getAlgorithmName())) {
            //算法名称校验
            QueryWrapper<PtTrainAlgorithm> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("algorithm_name", ptTrainAlgorithmUpdateDTO.getAlgorithmName())
                    .ne("id", ptTrainAlgorithmUpdateDTO.getId());
            Integer countResult = ptTrainAlgorithmMapper.selectCount(queryWrapper);
            if (countResult > 0) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "The algorithm name ({}) already exists", ptTrainAlgorithmUpdateDTO.getAlgorithmName());
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
        //判断是否修改输出日志
        if (ptTrainAlgorithmUpdateDTO.getIsTrainLog() != null) {
            updatePtAlgorithm.setIsTrainLog(ptTrainAlgorithmUpdateDTO.getIsTrainLog());
        }
        //判断是否修改可视化日志
        if (ptTrainAlgorithmUpdateDTO.getIsVisualizedLog() != null) {
            updatePtAlgorithm.setIsVisualizedLog(ptTrainAlgorithmUpdateDTO.getIsVisualizedLog());
        }
        try {
            //算法未修改成功，抛出异常，并返回失败信息
            ptTrainAlgorithmMapper.updateById(updatePtAlgorithm);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} failed to modify the algorithm. Pt_train_algorithm table modification operation failed. Failure reason :{}", currentUser.getUsername(), e.getMessage());
            throw new BusinessException("修改失败");
        }
        LogUtil.info(LogEnum.BIZ_TRAIN, "End of user {} modification algorithm, return modification algorithm ID={}", currentUser.getUsername(), ptTrainAlgorithm.getId());
        return ptTrainAlgorithm.getId();
    }

    /**
     * 解压缩zip压缩包
     *
     * @param user             用户
     * @param path             文件路径
     * @param ptTrainAlgorithm 算法参数
     */
    private void unZip(UserDTO user, String path, PtTrainAlgorithm ptTrainAlgorithm) {
        //目标路径
        String targetPath = k8sNameTool.getNfsPath(BizNfsEnum.ALGORITHM, user.getId());
        boolean unzip = localFileUtil.unzipLocalPath(path, nfsConfig.getBucket() + targetPath);
        if (!unzip) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} failed to unzip", user.getUsername());
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
        //从会话中获取用户信息
        UserDTO user = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} delete algorithm, the parameter received is {}", user.getUsername(), ptTrainAlgorithmDeleteDTO.getIds());
        Set<Long> idList = ptTrainAlgorithmDeleteDTO.getIds();
        //权限校验
        QueryWrapper<PtTrainAlgorithm> query = new QueryWrapper<>();
        query.in("id", idList);
        List<PtTrainAlgorithm> algorithmList = ptTrainAlgorithmMapper.selectList(query);
        if (algorithmList.size() < idList.size()) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} delete algorithm failed, no permission to delete the corresponding data in the algorithm table", user.getUsername());
            throw new BusinessException(ResponseCode.SUCCESS, "您删除的ID不存在或已被删除");
        }
        int deleteCountResult = ptTrainAlgorithmMapper.deleteBatchIds(idList);
        if (deleteCountResult < idList.size()) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The user {} deletion algorithm failed, and the algorithm table deletion operation based on the ID array {} failed", user.getUsername(), ptTrainAlgorithmDeleteDTO.getIds());
            throw new BusinessException(ResponseCode.SUCCESS, "删除算法未成功");
        }
        //同步更新noteBook表中algorithmId=0
        QueryWrapper<NoteBook> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("algorithm_id", idList);
        List<NoteBook> noteBookList = noteBookMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(noteBookList)) {
            noteBookList.forEach(noteBook -> {
                noteBookService.updateTrainIdByNoteBookId(noteBook.getId(), null);
            });
        }
        //定时任务删除相应的算法文件
        for (PtTrainAlgorithm algorithm : algorithmList) {
            RecycleTaskCreateDTO recycleTask = new RecycleTaskCreateDTO();
            recycleTask.setRecycleModule(RecycleModuleEnum.BIZ_ALGORITHM.getValue())
                    .setRecycleType(RecycleTypeEnum.FILE.getCode())
                    .setRecycleDelayDate(recycleConfig.getAlgorithmValid())
                    .setRecycleCondition(nfsUtil.formatPath(nfsConfig.getRootDir() + nfsConfig.getBucket() + algorithm.getCodeDir()))
                    .setRecycleNote("删除算法文件");
            recycleTaskService.createRecycleTask(recycleTask);
        }

        LogUtil.info(LogEnum.BIZ_TRAIN, "User {} delete algorithm end, delete algorithm ID array IDS ={}", user.getUsername(), idList);
    }

    /**
     * 查询算法个数
     *
     * @return count  返回个数
     */
    @Override
    public Map<String, Object> getAlgorithmCount() {
        //从会话中获取用户信息
        UserDTO user = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "The user {} queries his algorithm number", user.getUsername());
        QueryWrapper<PtTrainAlgorithm> wrapper = new QueryWrapper();
        wrapper.eq("algorithm_source", AlgorithmSourceEnum.MINE.getStatus());
        Integer countResult = ptTrainAlgorithmMapper.selectCount(wrapper);
        return new HashedMap() {{
            put("count", countResult);
        }};
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

        //设置fork默认值
        if (dto.getFork() == null) {
            dto.setFork(trainAlgorithmConstant.getFork());
        }
        //设置是否输出训练日志
        if (dto.getIsTrainLog() == null) {
            dto.setIsTrainLog(trainAlgorithmConstant.getIsTrainLog());
        }
        //设置是否输出训练结果
        if (dto.getIsTrainOut() == null) {
            dto.setIsTrainOut(trainAlgorithmConstant.getIsTrainOut());
        }
        //设置是否输出可视化日志
        if (dto.getIsVisualizedLog() == null) {
            dto.setIsVisualizedLog(trainAlgorithmConstant.getIsVisualizedLog());
        }
    }

    /**
     * 获取镜像url
     *
     * @param ptTrainAlgorithmCreateDTO 获取镜像
     * @param user                      用户
     * @return String  返回镜像路径
     **/
    private String getImageUrl(PtTrainAlgorithmCreateDTO ptTrainAlgorithmCreateDTO, UserDTO user) {
        //获取镜像url
        QueryWrapper<PtImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("image_name", ptTrainAlgorithmCreateDTO.getImageName())
                .eq("image_tag", ptTrainAlgorithmCreateDTO.getImageTag())
                .eq("image_status", ImageStateEnum.SUCCESS.getCode()).last(" limit 1 ");
        ;
        PtImage ptImage = ptImageMapper.selectOne(queryWrapper);
        if (ptImage == null || StringUtils.isBlank(ptImage.getImageUrl())) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} gets image ,the imageName is {}, the imageTag is {}, and the result of query image table (PT_image) is empty", user.getUsername(), ptTrainAlgorithmCreateDTO.getImageName(), ptTrainAlgorithmCreateDTO.getImageTag());
            throw new BusinessException("镜像不存在");
        }
        return ptImage.getImageUrl();
    }

    /**
     *
     * @param ptModelAlgorithmCreateDTO 模型优化上传算法入参
     * @return PtTrainAlgorithm 新增算法信息
     */
    @Override
    public PtTrainAlgorithm modelOptimizationUploadAlgorithm(PtModelAlgorithmCreateDTO ptModelAlgorithmCreateDTO) {
        PtTrainAlgorithmCreateDTO ptTrainAlgorithmCreateDTO = new PtTrainAlgorithmCreateDTO();
        ptTrainAlgorithmCreateDTO.setAlgorithmName(ptModelAlgorithmCreateDTO.getName()).setCodeDir(ptModelAlgorithmCreateDTO.getPath()).setAlgorithmUsage("模型优化").setIsTrainOut(false).setIsTrainLog(false).setIsVisualizedLog(false);
        Long id = create(ptTrainAlgorithmCreateDTO);
        PtTrainAlgorithm ptTrainAlgorithm = ptTrainAlgorithmMapper.selectById(id);
        return ptTrainAlgorithm;
    }

}
