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

package org.dubhe.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.dubhe.base.BaseImageDTO;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.dao.PtImageMapper;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.domain.entity.PtImage;
import org.dubhe.enums.ImageSourceEnum;
import org.dubhe.enums.ImageStateEnum;
import org.dubhe.enums.LogEnum;
import org.dubhe.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description: 镜像
 * @date: 2020-06-22
 */
@Component
public class ImageUtil {

    @Autowired
    private PtImageMapper ptImageMapper;

    /**
     * 获取镜像url
     *
     * @param     baseImageDTO 镜像参数
     * @return BaseImageDTO  镜像url
     **/
    public String getImages(BaseImageDTO baseImageDTO, UserDTO user) {
        QueryWrapper<PtImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("image_name", baseImageDTO.getImageName()).eq("image_tag", baseImageDTO.getImageTag())
                .eq("image_status", ImageStateEnum.SUCCESS.getCode());
        List<PtImage> ptImages = ptImageMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(ptImages)) {
            LogUtil.error(LogEnum.BIZ_TRAIN, " User {} gets image ,the imageName is {}, the imageTag is {}, and the result of query image table (PT_image) is empty", user.getUsername(), baseImageDTO.getImageName(), baseImageDTO.getImageTag());
            throw new BusinessException("镜像不存在");
        }
        //获取镜像为用户自定义镜像或预置镜像，且两者自身不能重复
        if (ptImages.size() > MagicNumConstant.TWO) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} got more images than scheduled, the imageName provided is {} and the imageTag is {}. The parameters are illegal", user.getUsername(), baseImageDTO.getImageName(), baseImageDTO.getImageTag());
            throw new BusinessException("镜像不合法");
        }
        for (PtImage ptImage : ptImages) {
            if (ImageSourceEnum.PRE.getCode().equals(ptImage.getImageResource())) {
                baseImageDTO.setImageName(ptImage.getImageUrl());
            } else if (user.getId().equals(ptImage.getCreateUserId())) {
                baseImageDTO.setImageName(ptImage.getImageUrl());
            }
        }
        if (StringUtils.isBlank(baseImageDTO.getImageName())) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "User {} gets image, the imageName provided is {} and the imageTag is {}. The parameters are illegal", user.getUsername(), baseImageDTO.getImageName(), baseImageDTO.getImageTag());
            throw new BusinessException("镜像不合法");
        }
        return baseImageDTO.getImageName();
    }

}
