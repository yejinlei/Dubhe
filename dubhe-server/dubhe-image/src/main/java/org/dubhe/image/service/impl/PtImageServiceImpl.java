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

package org.dubhe.image.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.biz.base.constant.HarborProperties;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.ResponseCode;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.context.DataContext;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.dto.CommonPermissionDataDTO;
import org.dubhe.biz.base.enums.DatasetTypeEnum;
import org.dubhe.biz.base.enums.ImageSourceEnum;
import org.dubhe.biz.base.enums.ImageStateEnum;
import org.dubhe.biz.base.enums.ImageTypeEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.ReflectionUtils;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.file.config.NfsConfig;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.permission.annotation.DataPermissionMethod;
import org.dubhe.biz.permission.base.BaseService;
import org.dubhe.image.async.HarborImagePushAsync;
import org.dubhe.image.dao.PtImageMapper;
import org.dubhe.image.domain.dto.*;
import org.dubhe.image.domain.entity.PtImage;
import org.dubhe.image.domain.vo.PtImageQueryVO;
import org.dubhe.image.service.PtImageService;
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description 镜像服务实现类
 * @date 2020-06-22
 */
@Service
public class PtImageServiceImpl implements PtImageService {

    @Autowired
    private PtImageMapper ptImageMapper;


    @Autowired
    private NfsConfig nfsConfig;

    @Autowired
    private HarborImagePushAsync imagePushAsync;

    @Autowired
    private HarborProperties harborProperties;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private RecycleService recycleService;

    @Autowired
    private RecycleConfig recycleConfig;


    public final static List<String> FIELD_NAMES;

    static {
        FIELD_NAMES = ReflectionUtils.getFieldNames(PtImageQueryVO.class);
    }

    /**
     * 查询镜像
     *
     * @param ptImageQueryDTO       查询镜像条件
     * @return Map<String, Object>  返回镜像分页数据
     **/
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Object> getImage(PtImageQueryDTO ptImageQueryDTO) {

        //从会话中获取用户信息
        UserContext user = userContextService.getCurUser();
        Page page = ptImageQueryDTO.toPage();

        QueryWrapper<PtImage> query = new QueryWrapper<>();

        if (ptImageQueryDTO.getProjectType().equals(ImageTypeEnum.NOTEBOOK.getType())) {
            ptImageQueryDTO.setSort("imageResource");
            DataContext.set(CommonPermissionDataDTO.builder().type(true).build());
        }
        if (ptImageQueryDTO.getImageStatus() != null) {
            query.eq("image_status", ptImageQueryDTO.getImageStatus());
        }
        if (ptImageQueryDTO.getImageResource() != null) {
            query.eq("image_resource", ptImageQueryDTO.getImageResource());
        }
        query.eq("project_name", resourcetoName(ptImageQueryDTO.getProjectType()));

        if (StringUtils.isNotEmpty(ptImageQueryDTO.getImageNameOrId())) {
            query.and(x -> x.eq("id", ptImageQueryDTO.getImageNameOrId()).or().like("image_name", ptImageQueryDTO.getImageNameOrId()));
        }


        //排序
        IPage<PtImage> ptImages;
        try {
            if (ptImageQueryDTO.getSort() != null && FIELD_NAMES.contains(ptImageQueryDTO.getSort())) {
                if (StringConstant.SORT_ASC.equalsIgnoreCase(ptImageQueryDTO.getOrder())) {
                    query.orderByAsc(StringUtils.humpToLine(ptImageQueryDTO.getSort()));
                } else {
                    query.orderByDesc(StringUtils.humpToLine(ptImageQueryDTO.getSort()));
                }
            } else {
                query.orderByDesc(StringConstant.ID);
            }
            ptImages = ptImageMapper.selectPage(page, query);
        } catch (Exception e) {
            LogUtil.error(LogEnum.IMAGE, "User {} query image list failed，exception {}", user.getId(), e);
            throw new BusinessException("查询镜像列表展示异常");
        }
        List<PtImageQueryVO> ptImageQueryResult = ptImages.getRecords().stream().map(x -> {
            PtImageQueryVO ptImageQueryVO = new PtImageQueryVO();
            BeanUtils.copyProperties(x, ptImageQueryVO);
            return ptImageQueryVO;
        }).collect(Collectors.toList());
        DataContext.remove();
        return PageUtil.toPage(page, ptImageQueryResult);
    }

    /**
     * 上传镜像到harbor
     *
     * @param ptImageUploadDTO 上传条件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadImage(PtImageUploadDTO ptImageUploadDTO) {
        UserContext user = userContextService.getCurUser();

        //普通用户不支持上传预置镜像
        if (ImageSourceEnum.PRE.getCode().equals(ptImageUploadDTO.getImageResource()) &&
                !BaseService.isAdmin(user)) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED, "普通用户不支持上传预置镜像!");
        }
        //notebook镜像只能由管理员上传
        if (ptImageUploadDTO.getProjectType().equals(ImageTypeEnum.NOTEBOOK.getCode()) && !BaseService.isAdmin(user)) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED, "该用户不支持上传noteBook镜像!");
        }

        //校验用户自定义镜像不能和预置镜像重名
        List<PtImage> resList = checkUploadImage(ptImageUploadDTO, null, ImageSourceEnum.PRE.getCode());
        if (CollUtil.isNotEmpty(resList)) {
            throw new BusinessException(ResponseCode.BADREQUEST, "不允许和预置镜像信息重复，请重新上传!");
        }

        //同一用户上传镜像的(userId+imageName+imageTag)存在的情况下是不能重复上传的
        List<PtImage> imageList = checkUploadImage(ptImageUploadDTO, user, ImageSourceEnum.MINE.getCode());
        if (CollUtil.isNotEmpty(imageList) && ImageStateEnum.SUCCESS.getCode().equals(imageList.get(0).getImageStatus())) {
            throw new BusinessException(ResponseCode.BADREQUEST, "镜像信息已存在，不允许重复上传!");
        }

        String projectName = ImageTypeEnum.getType(ptImageUploadDTO.getProjectType());

        String harborImagePath = projectName + StrUtil.SLASH + ptImageUploadDTO.getImageName() + StrUtil.DASHED + user.getId() +
                StrUtil.COLON + ptImageUploadDTO.getImageTag();
        //存储镜像信息
        PtImage ptImage = new PtImage();
        ptImage.setImageName(ptImageUploadDTO.getImageName())
                .setProjectName(projectName)
                .setImageUrl(harborImagePath)
                .setImageResource(ptImageUploadDTO.getImageResource())
                .setImageStatus(ImageStateEnum.MAKING.getCode())
                .setRemark(ptImageUploadDTO.getRemark())
                .setImageTag(ptImageUploadDTO.getImageTag())
                .setCreateUserId(user.getId());
        if (ImageSourceEnum.PRE.getCode().equals(ptImageUploadDTO.getImageResource())) {
            ptImage.setOriginUserId(MagicNumConstant.ZERO_LONG);
        } else {
            ptImage.setOriginUserId(user.getId());
        }
        //设置notebook镜像为预置镜像
        if (ptImageUploadDTO.getProjectType().equals(ImageTypeEnum.NOTEBOOK.getCode()) && BaseService.isAdmin(user)) {
            ptImage.setOriginUserId(0L);
        }
        int count = ptImageMapper.insert(ptImage);
        if (count < 1) {
            imagePushAsync.updateImageStatus(ptImage, ImageStateEnum.FAIL.getCode());
            throw new BusinessException("内部错误!");
        }
        //shell脚本上传镜像
        try {
            String imagePath = nfsConfig.getRootDir() + nfsConfig.getBucket().substring(1) + ptImageUploadDTO.getImagePath();
            String imageNameAndTag = ptImageUploadDTO.getImageName() + StrUtil.DASHED + user.getId() + StrUtil.COLON + ptImageUploadDTO.getImageTag();
            imagePushAsync.execShell(imagePath, imageNameAndTag, ptImage);
        } catch (Exception e) {
            LogUtil.error(LogEnum.IMAGE, "Image upload exception {}", e);
            throw new BusinessException("镜像上传失败!");
        }
    }

    /**
     * 获取镜像信息
     *
     * @param ptImageQueryImageDTO      查询条件
     * @return List<String>  通过imageName查询所含镜像版本信息
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public List<PtImage> searchImages(PtImageQueryImageDTO ptImageQueryImageDTO) {
        LambdaQueryWrapper<PtImage> queryWrapper = new LambdaQueryWrapper<>();
        if (ptImageQueryImageDTO.getProjectType() != null) {
            queryWrapper.eq(PtImage::getProjectName, resourcetoName(ptImageQueryImageDTO.getProjectType()));
        }
        queryWrapper.eq(PtImage::getImageName, ptImageQueryImageDTO.getImageName())
                .eq(PtImage::getImageStatus, ImageStateEnum.SUCCESS.getCode());
        List<PtImage> ptImages = ptImageMapper.selectList(queryWrapper);
        List<PtImage> list = new ArrayList<>();
        if (CollUtil.isEmpty(ptImages)) {
            throw new BusinessException(ResponseCode.BADREQUEST, "未查询到镜像信息!");
        }

        ptImages = ptImages.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                new TreeSet<>(Comparator.comparing(PtImage::getImageTag))), ArrayList::new));
        ptImages.stream().forEach(ptImage -> {
            ptImage.setImageUrl(harborProperties.getAddress() + StrUtil.SLASH + ptImage.getImageUrl());
            list.add(ptImage);
        });
        return list;
    }


    /**
     * 删除镜像
     *
     * @param imageDeleteDTO 删除镜像条件参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public void deleteTrainImage(PtImageDeleteDTO imageDeleteDTO) {
        List<PtImage> imageList = ptImageMapper.selectList(new LambdaQueryWrapper<PtImage>()
                .in(PtImage::getId, imageDeleteDTO.getIds()));

        //删除本地镜像
        imageList.forEach(image -> {
            ptImageMapper.deleteById(image.getId());
            //创建镜像回收任务
            createRecycleTask(image);
        });
    }

    /**
     * 文件定时清理
     *
     * @param ptImage 镜像实体对象
     */
    private void createRecycleTask(PtImage ptImage) {

        String imageUrl = ImageStateEnum.SUCCESS.getCode().equals(ptImage.getImageStatus()) ? ptImage.getImageUrl() : "";
        RecycleCreateDTO recycleCreateDTO = RecycleCreateDTO.builder()
                .recycleModule(RecycleModuleEnum.BIZ_IMAGE.getValue())
                .recycleDelayDate(recycleConfig.getImageValid())
                .recycleNote(RecycleTool.generateRecycleNote("删除镜像", ptImage.getImageName(), ptImage.getId()))
                .recycleCustom(RecycleResourceEnum.IMAGE_RECYCLE_FILE.getClassName())
                .restoreCustom(RecycleResourceEnum.IMAGE_RECYCLE_FILE.getClassName())
                .remark(String.valueOf(ptImage.getId()))
                .build();
        recycleCreateDTO.addRecycleDetailCreateDTO(RecycleDetailCreateDTO.builder()
                .recycleCondition(imageUrl)
                .recycleType(RecycleTypeEnum.FILE.getCode())
                .recycleNote(RecycleTool.generateRecycleNote("删除镜像", ptImage.getImageName(), ptImage.getId()))
                .remark(String.valueOf(ptImage.getId()))
                .build()
        );
        recycleService.createRecycleTask(recycleCreateDTO);
    }

    /**
     * 修改镜像信息
     *
     * @param imageUpdateDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public void updateTrainImage(PtImageUpdateDTO imageUpdateDTO) {

        UserContext curUser = userContextService.getCurUser();
        List<PtImage> imageList = ptImageMapper.selectList(new LambdaQueryWrapper<PtImage>()
                .in(PtImage::getId, imageUpdateDTO.getIds()));

        if (CollUtil.isEmpty(imageList)) {
            throw new BusinessException("内部错误");
        }
        for (PtImage image : imageList) {
            //禁止修改预置镜像
            if (ImageSourceEnum.PRE.getCode().equals(image.getImageResource())) {
                throw new BusinessException("无法修改预置镜像信息");
            }
            image.setRemark(imageUpdateDTO.getRemark());
            ptImageMapper.updateById(image);
        }
    }

    /**
     * 获取镜像名称列表
     * @param ptImageQueryNameDTO 获取镜像名称列表查询条件
     * @return Set<String> 镜像列表
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Set<String> getImageNameList(PtImageQueryNameDTO ptImageQueryNameDTO) {
        List<String> projectTypes = new ArrayList<>();
        ptImageQueryNameDTO.getProjectTypes().forEach(x ->
                projectTypes.add(ImageTypeEnum.getType(x)));
        List<PtImage> imageList = ptImageMapper.selectList(new LambdaQueryWrapper<PtImage>()
                .in(PtImage::getProjectName, projectTypes)
                .eq(PtImage::getImageStatus, ImageStateEnum.SUCCESS.getCode()));
        Set<String> imageNames = new HashSet<>();
        imageList.forEach(image -> {
            imageNames.add(image.getImageName());
        });
        return imageNames;
    }

    /**
     * 修改镜像来源(notebook定制)
     *
     * @param id 镜像id
     */
    @Override
    public void updImageResource(Long id) {
        UserContext user = userContextService.getCurUser();
        UpdateWrapper<PtImage> updateWrapper = new UpdateWrapper<>();
        //notebook镜像只能由管理员上传
        if (!BaseService.isAdmin(user)) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED, "该用户无权限修改镜像状态!");
        }

        //校验id是否存在
        PtImage image = ptImageMapper.selectById(id);
        if (image == null || !ImageTypeEnum.NOTEBOOK.getCode().equals(image.getProjectName())) {
            throw new BusinessException(ResponseCode.BADREQUEST, "该镜像不存在或镜像类型不支持!");
        }

        //仅支持[制作成功]状态镜像设置为默认镜像
        if (!ImageStateEnum.SUCCESS.getCode().equals(image.getImageStatus())) {
            throw new BusinessException(ResponseCode.BADREQUEST, "仅支持[制作成功]状态镜像设置为默认镜像!");
        }

        //修改该用户的notebook镜像为"我的镜像"
        updateWrapper.eq("project_name", ImageTypeEnum.NOTEBOOK.getCode());
        updateWrapper.eq("image_resource", ImageSourceEnum.PRE.getCode());
        updateWrapper.set("image_resource", ImageSourceEnum.MINE.getCode());
        ptImageMapper.update(null, updateWrapper);

        PtImage ptImage = new PtImage();
        ptImage.setId(id);
        ptImage.setImageResource(ImageSourceEnum.PRE.getCode());
        ptImageMapper.updateById(ptImage);
    }

    /**
     * 获取镜像URL
     *
     * @param imageQueryUrlDTO 查询镜像路径DTO
     * @return String 镜像url
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public String getImageUrl(PtImageQueryUrlDTO imageQueryUrlDTO) {
        LambdaQueryWrapper<PtImage> queryWrapper = new LambdaQueryWrapper<>();
        if (imageQueryUrlDTO.getProjectType() != null && ImageTypeEnum.NOTEBOOK.getType().equals(imageQueryUrlDTO.getProjectType())) {
            DataContext.set(CommonPermissionDataDTO.builder().type(true).build());
        }
        if (imageQueryUrlDTO.getImageResource() != null) {
            queryWrapper.eq(PtImage::getImageResource, imageQueryUrlDTO.getImageResource());
        }
        if (StrUtil.isNotEmpty(imageQueryUrlDTO.getImageName())) {
            queryWrapper.eq(PtImage::getImageName, imageQueryUrlDTO.getImageName());
        }
        if (StrUtil.isNotEmpty(imageQueryUrlDTO.getImageTag())) {
            queryWrapper.eq(PtImage::getImageTag, imageQueryUrlDTO.getImageTag());
        }
        if (imageQueryUrlDTO.getProjectType() != null) {
            queryWrapper.eq(PtImage::getProjectName, resourcetoName(imageQueryUrlDTO.getProjectType()));
        }
        queryWrapper.eq(PtImage::getImageStatus, ImageStateEnum.SUCCESS.getCode());
        List<PtImage> imageList = ptImageMapper.selectList(queryWrapper);

        if (CollUtil.isEmpty(imageList)) {
            throw new BusinessException("未查询到镜像信息");
        }
        String imageUrl = imageList.get(0).getImageUrl();
        DataContext.remove();
        return imageUrl;
    }

    /**
     * 自定义镜像回收的还原实现
     *
     * @param dto 资源回收DTO对象
     */
    @Override
    public void recycleRollback(RecycleCreateDTO dto) {
        String imageId = dto.getRemark();
        ptImageMapper.updateDeletedById(Long.valueOf(imageId), false);
    }

    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public List<PtImage> getTerminalImageList() {
        UserContext user = userContextService.getCurUser();
        LambdaQueryWrapper<PtImage> queryTerminalWrapper = new LambdaQueryWrapper<>();
        queryTerminalWrapper.eq(PtImage::getProjectName, ImageTypeEnum.TERMINAL.getCode())
                .eq(PtImage::getImageStatus, ImageStateEnum.SUCCESS.getCode());
        if (user != null && !BaseService.isAdmin()) {
            queryTerminalWrapper.and(wrapper -> wrapper.eq(PtImage::getCreateUserId, user.getId()).or().eq(PtImage::getImageResource, ImageSourceEnum.PRE.getCode()));
        }

        List<PtImage> terminalImages = ptImageMapper.selectList(queryTerminalWrapper);

        List<PtImage> list = new ArrayList<>();
        if (CollUtil.isEmpty(terminalImages)) {
            return new ArrayList<>();
        }

        terminalImages.stream().forEach(ptImage -> {
            ptImage.setImageUrl(ptImage.getImageUrl());
            list.add(ptImage);
        });
        return list;
    }


    /**
     * @param ptImageUploadDTO  镜像上传逻辑校验
     * @param user              用户
     * @return List<PtImage>    镜像列表
     **/
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    private List<PtImage> checkUploadImage(PtImageUploadDTO ptImageUploadDTO, UserContext user, Integer source) {

        LambdaQueryWrapper<PtImage> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(PtImage::getImageName, ptImageUploadDTO.getImageName())
                .eq(PtImage::getImageTag, ptImageUploadDTO.getImageTag())
                .eq(PtImage::getImageResource, source);

        if (user != null) {
            queryWrapper.eq(PtImage::getCreateUserId, user.getId());
        }
        List<PtImage> imageList = ptImageMapper.selectList(queryWrapper);
        return imageList;
    }

    /**
     * 项目类型转换为项目名称
     *
     * @param projectType 项目类型
     * @return String 镜像项目名称
     */
    private static String resourcetoName(Integer projectType) {
        String projectName = ImageTypeEnum.getType(projectType);
        if (StrUtil.isEmpty(projectName)) {
            throw new BusinessException("上传镜像项目类型不支持");
        }
        return projectName;
    }
}
