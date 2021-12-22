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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.collections4.CollectionUtils;
import org.dubhe.biz.base.constant.*;
import org.dubhe.biz.base.context.DataContext;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.dto.CommonPermissionDataDTO;
import org.dubhe.biz.base.dto.UserConfigSaveDTO;
import org.dubhe.biz.base.dto.UserDTO;
import org.dubhe.biz.base.enums.DatasetTypeEnum;
import org.dubhe.biz.base.enums.ImageSourceEnum;
import org.dubhe.biz.base.enums.ImageStateEnum;
import org.dubhe.biz.base.enums.ImageTypeEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.ReflectionUtils;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.UserConfigVO;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.file.config.NfsConfig;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.permission.annotation.DataPermissionMethod;
import org.dubhe.biz.permission.base.BaseService;
import org.dubhe.cloud.authconfig.service.AdminClient;
import org.dubhe.harbor.api.HarborApi;
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
    private HarborApi harborApi;

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

    @Autowired
    private AdminClient adminClient;


    public final static List<String> FIELD_NAMES;

    static {
        FIELD_NAMES = ReflectionUtils.getFieldNames(PtImageQueryVO.class);
    }

    /**
     * 查询镜像
     *
     * @param ptImageQueryDTO 查询镜像条件
     * @return Map<String, Object>  返回镜像分页数据
     **/
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Object> getImage(PtImageQueryDTO ptImageQueryDTO) {
        Page page = ptImageQueryDTO.toPage();

        QueryWrapper<PtImage> query = new QueryWrapper<>();

        if (ptImageQueryDTO.getImageStatus() != null) {
            query.eq("image_status", ptImageQueryDTO.getImageStatus());
        }
        if (ptImageQueryDTO.getImageResource() != null) {
            query.eq("image_resource", ptImageQueryDTO.getImageResource());
        }

        if (StringUtils.isNotEmpty(ptImageQueryDTO.getImageNameOrId())) {
            query.and(x -> x.eq("id", ptImageQueryDTO.getImageNameOrId()).or().like("image_name", ptImageQueryDTO.getImageNameOrId()));
        }
        //排序
        if (ptImageQueryDTO.getSort() != null && FIELD_NAMES.contains(ptImageQueryDTO.getSort())) {
            if (StringConstant.SORT_ASC.equalsIgnoreCase(ptImageQueryDTO.getOrder())) {
                query.orderByAsc(StringUtils.humpToLine(ptImageQueryDTO.getSort()));
            } else {
                query.orderByDesc(StringUtils.humpToLine(ptImageQueryDTO.getSort()));
            }
        } else {
            query.orderByDesc(StringConstant.ID);
        }

        IPage<PtImage> ptImages = ptImageMapper.selectPage(page, query);

        List<PtImageQueryVO> ptImageQueryVOS = convert2VO(ptImages.getRecords());

        return PageUtil.toPage(page, ptImageQueryVOS);
    }

    /**
     * 转换成VO对象
     * @param ptImageList 镜像集合
     * @return
     */
    private List<PtImageQueryVO> convert2VO(Collection<PtImage> ptImageList){
        //从会话中获取用户信息
        UserContext user = userContextService.getCurUser();
        List<Long> userIds = ptImageList.stream().map(PtImage::getCreateUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> idUserNameMap;
        if(CollectionUtils.isEmpty(userIds)){
            idUserNameMap=Collections.emptyMap();
        }else {
            DataResponseBody<List<UserDTO>> result = adminClient.getUserList(userIds);
            idUserNameMap = result.getData().stream().collect(Collectors.toMap(UserDTO::getId, UserDTO::getUsername, (o, n) -> n));
        }

        Long defaultImageId = user.getUserConfig().getDefaultImageId();
        List<PtImageQueryVO> ptImageQueryVOS=new LinkedList<>();
        for (PtImage ptImage : ptImageList) {
            PtImageQueryVO ptImageQueryVO = new PtImageQueryVO();
            BeanUtils.copyProperties(ptImage, ptImageQueryVO);
            ptImageQueryVO.setAsDefault(ptImage.getId().equals(defaultImageId));
            String userName = idUserNameMap.get(ptImage.getCreateUserId());
            ptImageQueryVO.setAuthor(userName);
            ptImageQueryVOS.add(ptImageQueryVO);
        };
        return ptImageQueryVOS;
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
        //0 代表公共数据
        Long originUserId = ImageSourceEnum.PRE.getCode().equals(ptImageUploadDTO.getImageResource()) ? MagicNumConstant.ZERO_LONG : user.getId();

        String imageUrl = harborApi.getImageUrl(originUserId, ptImageUploadDTO.getImageName(), ptImageUploadDTO.getImageTag());

        //存储镜像信息
        PtImage ptImage = new PtImage();
        ptImage.setImageName(ptImageUploadDTO.getImageName())
                .setOriginUserId(originUserId)
                .setImageUrl(imageUrl)
                .setImageResource(ptImageUploadDTO.getImageResource())
                .setImageStatus(ImageStateEnum.MAKING.getCode())
                .setRemark(ptImageUploadDTO.getRemark())
                .setImageTag(ptImageUploadDTO.getImageTag())
                .setCreateUserId(user.getId());


        int count = ptImageMapper.insert(ptImage);
        if (count < 1) {
            imagePushAsync.updateImageStatus(ptImage, ImageStateEnum.FAIL.getCode());
            throw new BusinessException("内部错误!");
        }
        //shell脚本上传镜像
        try {
            String imagePath = nfsConfig.getRootDir() + nfsConfig.getBucket().substring(1) + ptImageUploadDTO.getImagePath();
            imagePushAsync.execShell(imagePath, imageUrl, ptImage);
        } catch (Exception e) {
            LogUtil.error(LogEnum.IMAGE, "Image upload exception {}", e);
            throw new BusinessException("镜像上传失败!");
        }
    }

    /**
     * 获取镜像信息
     *
     * @param ptImageQueryImageDTO 查询条件
     * @return List<String>  通过imageName查询所含镜像版本信息
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public List<PtImage> searchImages(PtImageQueryImageDTO ptImageQueryImageDTO) {
        LambdaQueryWrapper<PtImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PtImage::getImageName, ptImageQueryImageDTO.getImageName())
                .orderByAsc(PtImage::getImageTag)
                .eq(PtImage::getImageStatus, ImageStateEnum.SUCCESS.getCode());
        List<PtImage> ptImages = ptImageMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(ptImages)) {
            throw new BusinessException(ResponseCode.BADREQUEST, "未查询到镜像信息!");
        }

        ptImages.forEach(ptImage -> {
            ptImage.setImageUrl(harborProperties.getAddress() + StrUtil.SLASH + ptImage.getImageUrl());
        });
        return ptImages;
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
     *
     * @param ptImageQueryNameDTO 获取镜像名称列表查询条件
     * @return Set<String> 镜像列表
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Set<String> getImageNameList(PtImageQueryNameDTO ptImageQueryNameDTO) {
        QueryWrapper<PtImage> ptImageLambdaQueryWrapper = new QueryWrapper<>();
        if (!Boolean.FALSE.equals(ptImageQueryNameDTO.getRequireSuccess())) {
            ptImageLambdaQueryWrapper.eq("image_status", ImageStateEnum.SUCCESS.getCode());
        }
        ptImageLambdaQueryWrapper.select(" distinct `image_name` ");
        List imageList = ptImageMapper.selectObjs(ptImageLambdaQueryWrapper);
        return new HashSet<>(imageList);
    }

    /**
     * 修改镜像来源(notebook定制)
     *
     * @param id 镜像id
     */
    @Override
    public void updateDefaultImage(Long id) {
        UserContext user = userContextService.getCurUser();
        //notebook镜像只能由管理员上传
        if (!BaseService.isAdmin(user)) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED, "该用户无权限修改镜像状态!");
        }

        //校验id是否存在
        PtImage image = ptImageMapper.selectById(id);
        //暂定预置镜像才能设置为默认
        if (image == null || !ImageSourceEnum.PRE.getCode().equals(image.getImageResource())) {
            throw new BusinessException(ResponseCode.BADREQUEST, "该镜像不存在或镜像类型不支持!");
        }

        //仅支持[制作成功]状态镜像设置为默认镜像
        if (!ImageStateEnum.SUCCESS.getCode().equals(image.getImageStatus())) {
            throw new BusinessException(ResponseCode.BADREQUEST, "仅支持[制作成功]状态镜像设置为默认镜像!");
        }
        updateDefaultImageId(id);
    }

    /**
     * 修改用户默认镜像Id
     *
     * @param id
     */
    private void updateDefaultImageId(Long id) {
        Long curUserId = userContextService.getCurUserId();
        DataResponseBody<UserConfigVO> userConfigVODataResponseBody = adminClient.getUserConfig(curUserId);

        if (!userConfigVODataResponseBody.succeed()) {
            throw new BusinessException(ResponseCode.BADREQUEST, "查询用户配置失败，默认镜像设置失败。");
        }
        UserConfigVO userConfigVO = userConfigVODataResponseBody.getData();
        UserConfigSaveDTO userConfigSaveDTO = new UserConfigSaveDTO();
        BeanUtils.copyProperties(userConfigVO, userConfigSaveDTO);
        userConfigSaveDTO.setDefaultImageId(id);
        DataResponseBody responseBody = adminClient.setUserConfig(userConfigSaveDTO);
        if (!responseBody.succeed()) {
            throw new BusinessException(ResponseCode.BADREQUEST, "更新用户配置失败，默认镜像设置失败。");
        }
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
        queryWrapper.eq(imageQueryUrlDTO.getId()!=null,PtImage::getId,imageQueryUrlDTO.getId());

        if (imageQueryUrlDTO.getImageResource() != null) {
            queryWrapper.eq(PtImage::getImageResource, imageQueryUrlDTO.getImageResource());
        }
        if (StrUtil.isNotEmpty(imageQueryUrlDTO.getImageName())) {
            queryWrapper.eq(PtImage::getImageName, imageQueryUrlDTO.getImageName());
        }
        if (StrUtil.isNotEmpty(imageQueryUrlDTO.getImageTag())) {
            queryWrapper.eq(PtImage::getImageTag, imageQueryUrlDTO.getImageTag());
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
        queryTerminalWrapper.eq(PtImage::getImageStatus, ImageStateEnum.SUCCESS.getCode());
        if (user != null && !BaseService.isAdmin()) {
            queryTerminalWrapper.and(wrapper -> wrapper.eq(PtImage::getCreateUserId, user.getId()).or().eq(PtImage::getImageResource, ImageSourceEnum.PRE.getCode()));
        }
        return ptImageMapper.selectList(queryTerminalWrapper);
    }


    /**
     * @param ptImageUploadDTO 镜像上传逻辑校验
     * @param user             用户
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

}
