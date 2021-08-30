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

package org.dubhe.train.utils;

import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.dto.PtImageQueryUrlDTO;
import org.dubhe.biz.base.enums.ImageTypeEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.train.client.ImageClient;
import org.dubhe.train.domain.dto.BaseImageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description 镜像
 * @date 2020-06-22
 */
@Component
public class ImageUtil {

    @Autowired
    private ImageClient imageClient;

    /**
     * 获取镜像url
     *
     * @param     baseImageDTO 镜像参数
     * @return BaseImageDTO  镜像url
     **/
    public String getImageUrl(BaseImageDTO baseImageDTO, UserContext user) {
        PtImageQueryUrlDTO ptImageQueryUrlDTO = new PtImageQueryUrlDTO();
        ptImageQueryUrlDTO.setImageTag(baseImageDTO.getImageTag());
        ptImageQueryUrlDTO.setImageName(baseImageDTO.getImageName());
        DataResponseBody<String> dataResponseBody = imageClient.getImageUrl(ptImageQueryUrlDTO);
        if (!dataResponseBody.succeed()) {
            LogUtil.error(LogEnum.BIZ_TRAIN, " User {} gets image ,the imageName is {}, the imageTag is {}, and the result of dubhe-image service call failed", user.getUsername(), baseImageDTO.getImageName(), baseImageDTO.getImageTag());
            throw new BusinessException("镜像服务调用失败");
        }
        String ptImage = dataResponseBody.getData();
        // 镜像路径
        if (StringUtils.isBlank(ptImage)) {
            LogUtil.error(LogEnum.BIZ_TRAIN, " User {} gets image ,the imageName is {}, the imageTag is {}, and the result of query image table (PT_image) is empty", user.getUsername(), baseImageDTO.getImageName(), baseImageDTO.getImageTag());
            throw new BusinessException("镜像不存在");
        }
        return ptImage;
    }

}
