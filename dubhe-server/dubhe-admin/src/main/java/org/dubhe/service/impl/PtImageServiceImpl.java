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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.annotation.DataPermissionMethod;
import org.dubhe.async.HarborImagePushAsync;
import org.dubhe.base.DataContext;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.base.ResponseCode;
import org.dubhe.config.NfsConfig;
import org.dubhe.config.TrainHarborConfig;
import org.dubhe.constatnts.UserConstant;
import org.dubhe.dao.PtImageMapper;
import org.dubhe.data.constant.Constant;
import org.dubhe.domain.dto.*;
import org.dubhe.domain.entity.PtImage;
import org.dubhe.domain.vo.PtImageQueryVO;
import org.dubhe.enums.*;
import org.dubhe.exception.BusinessException;
import org.dubhe.harbor.api.HarborApi;
import org.dubhe.service.PtImageService;
import org.dubhe.utils.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
    private HarborApi harborApi;

    @Autowired
    private NfsConfig nfsConfig;

    @Autowired
    private HarborImagePushAsync imagePushAsync;

    @Autowired
    private TrainHarborConfig trainHarborConfig;

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
        UserDTO user = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.IMAGE, "The user {} query image list display begins, and the received parameters are :{}", user.getId(), ptImageQueryDTO);
        Page page = ptImageQueryDTO.toPage();

        QueryWrapper<PtImage> query = new QueryWrapper<>();

        if (ptImageQueryDTO.getProjectType().equals(BizEnum.NOTEBOOK.getCreateResource())) {
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
                if (Constant.SORT_ASC.equalsIgnoreCase(ptImageQueryDTO.getOrder())) {
                    query.orderByAsc(StringUtils.humpToLine(ptImageQueryDTO.getSort()));
                } else {
                    query.orderByDesc(StringUtils.humpToLine(ptImageQueryDTO.getSort()));
                }
            } else {
                query.orderByDesc(Constant.ID);
            }
            ptImages = ptImageMapper.selectPage(page, query);
        } catch (Exception e) {
            LogUtil.error(LogEnum.IMAGE, "User {} query mirror list display exception :{}, request information :{}", user.getId(), e, ptImageQueryDTO);
            throw new BusinessException("查询镜像列表展示异常");
        }
        List<PtImageQueryVO> ptImageQueryResult = ptImages.getRecords().stream().map(x -> {
            PtImageQueryVO ptImageQueryVO = new PtImageQueryVO();
            BeanUtils.copyProperties(x, ptImageQueryVO);
            return ptImageQueryVO;
        }).collect(Collectors.toList());
        LogUtil.info(LogEnum.IMAGE, "User {} query mirror list display ends, the result is {}", user.getId(), ptImageQueryResult);
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
        LogUtil.info(LogEnum.IMAGE, "Upload image to harbor to receive parameters :{}", ptImageUploadDTO);
        UserDTO currentUser = JwtUtils.getCurrentUserDto();

        //notebook镜像只能由管理员上传
        if (ptImageUploadDTO.getProjectType().equals(BizEnum.NOTEBOOK.getBizCode()) && UserConstant.ADMIN_USER_ID != currentUser.getId()) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED, "该用户不支持上传noteBook镜像!");
        }

        //校验用户自定义镜像不能和预置镜像重名
        List<PtImage> resList = checkUploadImage(ptImageUploadDTO, null, ImageSourceEnum.PRE.getCode());
        if (!CollectionUtils.isEmpty(resList)) {
            LogUtil.info(LogEnum.IMAGE, "Preset image [{}:{}] already exists, no repeat allowed!", ptImageUploadDTO.getImageName(), ptImageUploadDTO.getImageTag());
            throw new BusinessException(ResponseCode.BADREQUEST, "不允许和预置镜像信息重复，请重新上传!");
        }

        //同一用户上传镜像的(userId+imageName+imageTag)存在的情况下是不能重复上传的
        List<PtImage> imageList = checkUploadImage(ptImageUploadDTO, currentUser, ImageSourceEnum.MINE.getCode());
        if (!CollectionUtils.isEmpty(imageList) && ImageStateEnum.SUCCESS.getCode().equals(imageList.get(0).getImageStatus())) {
            LogUtil.info(LogEnum.IMAGE, "The mirror [id:{}] already exists", imageList.get(0).getId());
            throw new BusinessException(ResponseCode.BADREQUEST, "镜像信息已存在，不允许重复上传!");
        }

        String projectName = ImageTypeEnum.getType(ptImageUploadDTO.getProjectType());

        String harborImagePath = projectName + StrUtil.SLASH + ptImageUploadDTO.getImageName() + StrUtil.DASHED + currentUser.getId() +
                StrUtil.COLON + ptImageUploadDTO.getImageTag();
        //存储镜像信息
        PtImage ptImage = new PtImage();
        ptImage.setImageName(ptImageUploadDTO.getImageName())
                .setProjectName(projectName)
                .setImageUrl(harborImagePath)
                .setImageResource(ImageSourceEnum.MINE.getCode())
                .setImageStatus(ImageStateEnum.MAKING.getCode())
                .setRemark(ptImageUploadDTO.getRemark())
                .setImageTag(ptImageUploadDTO.getImageTag())
                .setCreateUserId(currentUser.getId());
        if (ptImageUploadDTO.getProjectType().equals(BizEnum.NOTEBOOK.getCreateResource())) {
            //notebook镜像所有用户都可以查看和使用
            ptImage.setOriginUserId(MagicNumConstant.ZERO_LONG);
        } else {
            ptImage.setOriginUserId(currentUser.getId());
        }
        int count = ptImageMapper.insert(ptImage);
        if (count < 1) {
            imagePushAsync.updateImageStatus(ptImage, ImageStateEnum.FAIL.getCode());
            LogUtil.info(LogEnum.IMAGE, "User {} failed to store image information!", currentUser.getUsername());
            throw new BusinessException("内部错误!");
        }
        //shell脚本上传镜像
        try {
            String imagePath = nfsConfig.getRootDir() + nfsConfig.getBucket().substring(1) + ptImageUploadDTO.getImagePath();
            String imageNameAndTag = ptImageUploadDTO.getImageName() + StrUtil.DASHED + currentUser.getId() + StrUtil.COLON + ptImageUploadDTO.getImageTag();
            imagePushAsync.execShell(imagePath, imageNameAndTag, ptImage);
        } catch (Exception e) {
            LogUtil.error(LogEnum.IMAGE, "Image upload exception :{}", e);
            throw new BusinessException("镜像上传失败!");
        }
    }

    /**
     * 根据镜像获取信息
     *
     * @param imageName      镜像名
     * @return List<String>  通过imageName查询所含镜像版本信息
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public List<PtImage> searchImages(Integer projectType, String imageName) {
        LambdaQueryWrapper<PtImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PtImage::getProjectName, resourcetoName(projectType))
                .eq(PtImage::getImageName, imageName)
                .eq(PtImage::getImageStatus, ImageStateEnum.SUCCESS.getCode());
        List<PtImage> ptImages = ptImageMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(ptImages)) {
            LogUtil.info(LogEnum.IMAGE, "No mirroring information for imageName is :{}", imageName);
            throw new BusinessException(ResponseCode.SUCCESS, "未查询到镜像信息!");
        }
        List<PtImage> list = new ArrayList<>();
        ptImages = ptImages.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                new TreeSet<>(Comparator.comparing(PtImage::getImageTag))), ArrayList::new));
        ptImages.stream().forEach(ptImage -> {
            ptImage.setImageUrl(trainHarborConfig.getAddress() + StrUtil.SLASH + ptImage.getImageUrl());
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

        UserDTO user = JwtUtils.getCurrentUserDto();
        List<PtImage> imageList = ptImageMapper.selectList(new LambdaQueryWrapper<PtImage>()
                .in(PtImage::getId, imageDeleteDTO.getIds()));

        imageList.forEach(image -> {
            //禁止删除预置镜像
            if (!BizEnum.NOTEBOOK.getBizCode().equals(image.getProjectName()) && ImageSourceEnum.PRE.getCode().equals(image.getImageResource())) {
                throw new BusinessException("禁止删除预置镜像");
            }
            if (ImageStateEnum.SUCCESS.getCode().equals(image.getImageStatus())) {
                String imageUrl = trainHarborConfig.getAddress() + StrUtil.SLASH + image.getImageUrl();
                LogUtil.info(LogEnum.IMAGE, "delete harbor image url:{}", imageUrl);
                //同步删除harbor镜像
                harborApi.deleteImageByTag(imageUrl);
            }
        });


        //删除本地镜像
        int deleteSum = ptImageMapper.deleteBatchIds(imageDeleteDTO.getIds());
        if (deleteSum < imageDeleteDTO.getIds().size()) {
            LogUtil.error(LogEnum.IMAGE, "The user {} failed to delete image, and the pt_image table deletion operation failed according to the id array {}", user.getId(), imageDeleteDTO.getIds());
            throw new BusinessException("内部错误");
        }
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

        UserDTO user = JwtUtils.getCurrentUserDto();

        List<PtImage> imageList = ptImageMapper.selectList(new LambdaQueryWrapper<PtImage>()
                .in(PtImage::getId, imageUpdateDTO.getIds()));

        if (CollectionUtils.isEmpty(imageList)) {
            LogUtil.error(LogEnum.IMAGE, "The user{} update image failed,inquire condition ids{} not result", user.getId(), imageUpdateDTO.getIds());
            throw new BusinessException("内部错误");
        }
        for (PtImage image : imageList) {
            //禁止修改预置镜像
            if (ImageSourceEnum.PRE.getCode().equals(image.getImageResource())) {
                throw new BusinessException("无法修改预置镜像信息");
            }
            image.setRemark(imageUpdateDTO.getRemark());
            LogUtil.info(LogEnum.IMAGE, "The user{}update image,update image info:{}", user.getId(), image);
            ptImageMapper.updateById(image);
        }
    }

    /**
     * 获取镜像名称列表
     * @param projectType 镜像项目类型
     * @return Set<String> 镜像列表
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Set<String> getImageNameList(Integer projectType) {
        List<PtImage> imageList = ptImageMapper.selectList(new LambdaQueryWrapper<PtImage>()
                .eq(PtImage::getProjectName, ImageTypeEnum.getType(projectType))
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
        UserDTO userDto = JwtUtils.getCurrentUserDto();
        UpdateWrapper<PtImage> updateWrapper = new UpdateWrapper<>();
        //notebook镜像只能由管理员上传
        if (UserConstant.ADMIN_USER_ID != userDto.getId()) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED, "该用户无权限修改镜像状态!");
        }

        //校验id是否存在
        PtImage image = ptImageMapper.selectById(id);
        if (image == null || !BizEnum.NOTEBOOK.getBizCode().equals(image.getProjectName())) {
            throw new BusinessException(ResponseCode.BADREQUEST, "该镜像不存在或镜像类型不支持!");
        }

        //仅支持[制作成功]状态镜像设置为默认镜像
        if (!ImageStateEnum.SUCCESS.getCode().equals(image.getImageStatus())) {
            throw new BusinessException(ResponseCode.BADREQUEST, "仅支持[制作成功]状态镜像设置为默认镜像!");
        }

        //修改该用户的notebook镜像为"我的镜像"
        updateWrapper.eq("project_name", BizEnum.NOTEBOOK.getBizCode());
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
     * @return 镜像完整路径
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public String getImageUrl(PtImageQueryUrlDTO imageQueryUrlDTO) {
        UserDTO userDto = JwtUtils.getCurrentUserDto();

        if (imageQueryUrlDTO.getProjectType().equals(BizEnum.NOTEBOOK.getCreateResource())) {
            DataContext.set(CommonPermissionDataDTO.builder().type(true).build());
        }
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
        if (StrUtil.isNotEmpty(imageQueryUrlDTO.getIamgeTag())) {
            queryWrapper.eq(PtImage::getImageTag, imageQueryUrlDTO.getIamgeTag());
        }
        queryWrapper.eq(PtImage::getProjectName, resourcetoName(imageQueryUrlDTO.getProjectType()))
                .eq(PtImage::getImageStatus, ImageStateEnum.SUCCESS.getCode());
        List<PtImage> imageList = ptImageMapper.selectList(queryWrapper);

        if (CollUtil.isEmpty(imageList)) {
            LogUtil.error(LogEnum.IMAGE, "The user{} update image failed,inquire condition {} not result", userDto.getId(), imageQueryUrlDTO);
            throw new BusinessException("未查询到镜像信息");
        }
        String imageUrl = trainHarborConfig.getAddress() + StrUtil.SLASH + imageList.get(0).getImageUrl();
        DataContext.remove();
        return imageUrl;
    }

    /**
     * @param ptImageUploadDTO  镜像上传逻辑校验
     * @param user              用户
     * @return List<PtImage>    镜像列表
     **/
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    private List<PtImage> checkUploadImage(PtImageUploadDTO ptImageUploadDTO, UserDTO user, Integer source) {

        LambdaQueryWrapper<PtImage> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(PtImage::getProjectName, resourcetoName(ptImageUploadDTO.getProjectType()))
                .eq(PtImage::getImageName, ptImageUploadDTO.getImageName())
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
